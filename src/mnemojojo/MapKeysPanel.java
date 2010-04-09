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
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;

import gr.fire.core.FireScreen;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.CommandListener;
import gr.fire.core.Container;
import gr.fire.core.BoxLayout;
import gr.fire.ui.TextComponent;

class MapKeysPanel
    extends SubPanel
{
    public static final String configureKeysTitle = "Configure Keys";
    public static final String pressInfoText = "Press the key to ";

    public final static String keyQuery[] = {
            "assign grade 0",
            "assign grade 1",
            "assign grade 2",
            "assign grade 3",
            "assign grade 4",
            "assign grade 5",
            "show statistics",
            "skip a card",
            "replay sounds"
        };
    public int currentKey = 0;
    public int numKeys;
    public int keyCode[];

    public MapKeysPanel(FireScreen s, CommandListener li, Command cmd,
                        Configuration config)
    {
        super(configureKeysTitle, s, li, cmd, config);
        
        numKeys = keyQuery.length;
        keyCode = new int[numKeys];

        showQuery();
    }

    private void showQuery()
    {
        Container display = new Container(new BoxLayout(BoxLayout.Y_AXIS));

        TextComponent text = new TextComponent(pressInfoText
                                    + keyQuery[currentKey]);
        text.setFont(labelFont);
        text.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
        text.validate();

        display.add(text);
        display.setPrefSize(screen.getWidth(), labelFontHeight * 4);

        set(display);
        repaint();
    }

    protected void keyReleased(int keyCode)
    {
        this.keyCode[currentKey] = keyCode;
        currentKey++;

        if (currentKey == numKeys) {
            exitPanel();
        } else {
            showQuery();
        }
    }

    public void screenSizeChanged(int newWidth, int newHeight)
    {
        super.screenSizeChanged(newWidth, newHeight);
        showQuery();
    }
}

