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
    extends gr.fire.core.Panel
{
    public String pressText = "Press Me";
    public String configureKeysTitle = "Configure Command Keys";
    public String pressInfoText = "Please press the key indicated below.";

    private boolean showingLeft;
    protected FireScreen screen;
    protected CommandListener listener;
    protected Command doneCmd;

    protected Command pressCmd = new Command(pressText, Command.OK, 1);

    public MapCommandKeysPanel(FireScreen s, CommandListener li, Command cmd)
    {
	super(null, Panel.NO_SCROLLBAR, true);
	
	screen = s;
	listener = li;
	doneCmd = cmd;
	setLabel(configureKeysTitle);

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
	    listener.commandAction(doneCmd, (Component)null);
	}
    }
}

