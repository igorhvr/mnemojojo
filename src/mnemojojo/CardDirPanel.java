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
import javax.microedition.lcdui.Font;

import gr.fire.core.FireScreen;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.CommandListener;
import gr.fire.core.Container;
import gr.fire.core.BoxLayout;
import gr.fire.ui.TextComponent;
import gr.fire.ui.InputComponent;

import mnemogogo.mobile.hexcsv.FindCardDir;

class CardDirPanel
    extends SubPanel
{
    public static final String carddirTitle = "Select card directory";
    protected static final String lookingText = "Searching for cards...";
    protected static final String nocardsText = "No cards found!\nPlease export from Mnemosyne.";
    protected ProgressGauge progress;

    protected Command cmdFatal;

    public String cardpath;

    public CardDirPanel(FireScreen s, CommandListener li, Command cmd)
    {
	super(carddirTitle, s, li, cmd);
	progress = new ProgressGauge();
	cmdFatal = new Command("invisible", Command.OK, 1);
    }

    public void makeList()
    {
	int selected = -1;

	progress.showGauge(lookingText);
	String[] dbs = FindCardDir.checkStandard();
	if (dbs == null) {
	    dbs = FindCardDir.list();
	}
	progress.hideGauge();

	if (dbs == null || dbs.length == 0) {
	    screen.showAlert(nocardsText, gr.fire.ui.Alert.TYPE_ERROR,
			     gr.fire.ui.Alert.USER_SELECTED_OK, cmdFatal, this);
	    return;
	}

	Container buttons = new Container(new BoxLayout(BoxLayout.Y_AXIS));
	
	for (int i=0; i < dbs.length; ++i) {
	    buttons.add(buttonRow(dbs[i]));
	}

	set(buttons);
    }

    public void commandAction(javax.microedition.lcdui.Command cmd,
			      Component c)
    {
	if (cmdFatal.equals(cmd)) {
	    cardpath = null;
	    exitPanel();
	}

	if (cmdButton.equals(cmd)) {
	    InputComponent input = (InputComponent)c;
	    cardpath = input.getValue();
	    exitPanel();
	}
    }
}

