/*
 * Copyright (c) 2009 Timothy Bourke
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the "BSD License" which is distributed with the
 * software in the file LICENSE.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
 * License for more details.
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

    public MapCommandKeysPanel(FireScreen s, CommandListener li, Command cmd)
    {
	super(configureKeysTitle, s, li, cmd);
	
	Container cnt = new Container();
	cnt.add(new TextComponent(pressInfoText));
	set(cnt);

	setLeftSoftKeyCommand(pressCmd);
	showingLeft = true;

	screen.leftSoftKey = 0;
	screen.rightSoftKey = 0;
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
}

