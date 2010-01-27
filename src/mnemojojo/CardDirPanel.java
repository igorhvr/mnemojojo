/*
 * Copyright (C) 2010 Timothy Bourke
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package mnemojojo;

import java.lang.*;
import java.util.Vector;
import java.util.Enumeration;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.io.file.FileSystemRegistry;

import gr.fire.core.FireScreen;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.CommandListener;
import gr.fire.core.Container;
import gr.fire.core.BoxLayout;
import gr.fire.ui.TextComponent;
import gr.fire.ui.InputComponent;

import mnemogogo.mobile.hexcsv.FindCardDirJ2ME;

class CardDirPanel
    extends SubPanel
{
    public static final String carddirTitle = "Select card directory";
    protected static final String lookingText = "Searching for cards";
    protected static final String autoText = "Full automatic search";
    protected static final String manualText = "Browse manually";

    protected static final String cancelText = "Cancel";
    protected static final String chooseText = "Ok";

    protected ProgressGauge progress;
    protected Command cmdChoose;
    protected Command cmdCancel;

    public String cardpath = null;
    protected String browsepath = null;

    public CardDirPanel(FireScreen s, CommandListener li, Command cmdDone,
                        Configuration config)
    {
        super(carddirTitle, s, li, cmdDone, config);
        progress = new ProgressGauge();

        cmdCancel = new Command(cancelText, Command.CANCEL, 4);

        setCommandListener(this);       
        setRightSoftKeyCommand(cmdCancel);
    }

    public void makeList(boolean full)
    {
        String dbs[] = null;

        progress.showGauge(lookingText);
        if (full) {
            dbs = FindCardDirJ2ME.list();
        } else {
            dbs = FindCardDirJ2ME.checkStandard();
        }
        progress.hideGauge();

        Container buttons = new Container(new BoxLayout(BoxLayout.Y_AXIS));
        
        if (dbs != null) {
            for (int i=0; i < dbs.length; ++i) {
                buttons.add(buttonRow(dbs[i]));
            }
        }

        if (!full) {
            buttons.add(buttonRow(autoText));
        }
        buttons.add(buttonRow(manualText));

        set(buttons);
    }

    protected void listDirs(String path)
    {
        boolean isCardDir = false;
        Enumeration e;

        progress.showGauge(lookingText);
        if (path == null) {
            browsepath = "file:///";
            e = FileSystemRegistry.listRoots();

        } else {
            browsepath = path;
            Vector subdirs = new Vector();
            isCardDir = FindCardDirJ2ME.isCardDir(path, subdirs);
            e = subdirs.elements();
        }
        progress.hideGauge();

        Container buttons = new Container(new BoxLayout(BoxLayout.Y_AXIS));

        while (e.hasMoreElements()) {
            buttons.add(buttonRow((String)e.nextElement()));
        }

        set(buttons);

        if (isCardDir) {
            cmdChoose = new Command(chooseText, Command.OK, 3);
            setLeftSoftKeyCommand(cmdChoose);
        }
    }

    public void commandAction(javax.microedition.lcdui.Command cmd, Component c)
    {
        if (cmdButton.equals(cmd)) {
            InputComponent input = (InputComponent)c;
            String label = input.getValue();

            // Note: root names should not equal autoText or manualText!

            if (browsepath != null) {
                CardDirPanel cdp = new CardDirPanel(screen, listener, cmdDone,
                                                    config);
                cdp.cardpath = cardpath;
                cdp.listDirs(browsepath + label);
                screen.setCurrent(cdp);

            } else if (autoText.equals(label)) {
                CardDirPanel cdp = new CardDirPanel(screen, listener, cmdDone,
                                                    config);
                cdp.cardpath = cardpath;
                cdp.makeList(true);
                screen.setCurrent(cdp);

            } else if (manualText.equals(label)) {
                CardDirPanel cdp = new CardDirPanel(screen, listener, cmdDone,
                                                    config);
                cdp.cardpath = cardpath;
                cdp.listDirs(null);
                screen.setCurrent(cdp);

            } else {
                cardpath = label;
                exitPanel();
            }
        } else if (cmdCancel.equals(cmd)) {
            exitPanel();

        } else if (cmdChoose.equals(cmd)) {
            cardpath = browsepath;
            exitPanel();
        }
    }

    public void screenSizeChanged(int newWidth, int newHeight) {
        super.screenSizeChanged(newWidth, newHeight);

        Container container = (Container)getComponent(0);

        int cw = columnWidth(newWidth);
        int n = container.countComponents();
        for (int i=0; i < n; ++i) {
            Component c = container.getComponent(i);
            int[] size = c.getPrefSize();
            if ((size != null) && (size[1] > -1)) {
                c.setPrefSize(cw, size[1]);
            }
        }
    }
}

