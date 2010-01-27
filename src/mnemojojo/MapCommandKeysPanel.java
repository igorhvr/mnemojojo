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
import gr.fire.core.FireScreen;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.CommandListener;
import gr.fire.ui.TextComponent;

class MapCommandKeysPanel
    extends SubPanel
{
    public static final String pressText = "Press Me";
    public static final String configureKeysTitle = "Configure Command Keys";
    public static final String pressInfoText = "Please press the key indicated below.";

    private boolean showingLeft;

    protected Command pressCmd = new Command(pressText, Command.OK, 1);

    public MapCommandKeysPanel(FireScreen s, CommandListener li, Command cmd,
                               Configuration config)
    {
        super(configureKeysTitle, s, li, cmd, config);
        
        buildPanel();

        setLeftSoftKeyCommand(pressCmd);
        showingLeft = true;

        screen.leftSoftKey = 0;
        screen.rightSoftKey = 0;
    }

    protected void buildPanel()
    {
        Container cnt = new Container();
        cnt.add(new TextComponent(pressInfoText));
        set(cnt);
    }

    protected void keyReleased(int keyCode)
    {
        if (showingLeft) {
            screen.leftSoftKey = keyCode;
            setLeftSoftKeyCommand(null);
            setRightSoftKeyCommand(pressCmd);
            showingLeft = false;
        } else {
            screen.rightSoftKey = keyCode;
            exitPanel();
        }
    }

    public void screenSizeChanged(int newWidth, int newHeight)
    {
        super.screenSizeChanged(newWidth, newHeight);
        buildPanel();
    }
}

