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

class MapKeysPanel
    extends gr.fire.core.Panel
{
    public final String configureKeysTitle = "Configure Keys";
    public final String pressInfoText = "Please press the key to ";

    public final static String keyQuery[] = {
	    "assign grade 0",
	    "assign grade 1",
	    "assign grade 2",
	    "assign grade 3",
	    "assign grade 4",
	    "assign grade 5",
	    "show statistics",
	    "skip a card"
	};
    public int currentKey = 0;
    public int numKeys;
    public int keyCode[];

    protected Font labelFont;
    protected int labelFontHeight;

    protected FireScreen screen;
    protected CommandListener listener;
    protected Command cmdDone;

    public MapKeysPanel(FireScreen s, CommandListener li, Command cmd)
    {
	super(null, Panel.VERTICAL_SCROLLBAR, true);
	
	screen = s;
	listener = li;
	cmdDone = cmd;
	setLabel(configureKeysTitle);

	numKeys = keyQuery.length;
	keyCode = new int[numKeys];

	labelFont = Font.getFont(Font.FACE_SYSTEM,
				 Font.STYLE_PLAIN,
				 Font.SIZE_MEDIUM);
	labelFontHeight = labelFont.getHeight();

	showQuery();
    }

    private void showQuery()
    {
	System.out.println("showQuery: " + Integer.toString(currentKey));
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
	System.out.println("--keyReleased: " + Integer.toString(keyCode)); // XXX
	this.keyCode[currentKey] = keyCode;
	currentKey++;

	if (currentKey == numKeys) {
	    listener.commandAction(cmdDone, (Component)this);
	} else {
	    showQuery();
	}
    }
}

