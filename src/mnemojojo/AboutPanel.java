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
import java.io.IOException;
import java.util.Date; // XXX
import java.util.TimeZone; // XXX
import java.util.Calendar; // XXX

import mnemogogo.mobile.hexcsv.Debug;	// XXX

// TODO: Prune...
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Image;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

// TODO: Prune...
import gr.fire.browser.Browser;
import gr.fire.browser.util.Page;
import gr.fire.ui.FireTheme;
import gr.fire.core.FireScreen;
import gr.fire.core.KeyListener;
import gr.fire.core.Component;
import gr.fire.util.Log;
import gr.fire.util.FireConnector;
import gr.fire.ui.ProgressbarAnimation;

// for buttons: XXX
import gr.fire.core.BoxLayout;
import gr.fire.core.Container;
import gr.fire.core.GridLayout;
import gr.fire.ui.InputComponent;
import gr.fire.ui.ImageComponent;
import gr.fire.ui.TextComponent;

public class AboutPanel
    extends Panel
    implements CommandListener,
	       gr.fire.core.CommandListener
{
    protected FireScreen screen;
    protected Command cmdButton;
    protected Command cmdLeftRightDone;
    protected Command cmdKeysDone;

    protected gr.fire.core.CommandListener masterPanel;
    protected Command cmdLeft;
    protected Command cmdRight;

    protected InputComponent smallRadio;
    protected InputComponent mediumRadio;
    protected InputComponent largeRadio;
    protected InputComponent touchscreenCheck;

    protected Font sectionFont;
    protected Font titleFont;
    protected Font labelFont;
    protected Font textFont;

    protected int sectionFontHeight;
    protected int titleFontHeight;
    protected int labelFontHeight;
    protected int textFontHeight;
    protected int buttonHeight;

    protected int screenWidth;

    protected final int controlGap = 10;

    protected final String authorText = "Author";
    protected final String mnemosyneText = "Mnemosyne";
    protected final String sm2Text = "SM-2 Algorithm";
    protected final String fireguiText = "Fire GUI library";

    protected final String configTitleText = "Configuration Options";
    protected final String changeCardsText= "Change card directory";
    protected final String touchscreenText= "Show touchscreen buttons";
    protected final String leftrightText= "Configure left and right keys";
    protected final String gradingkeysText= "Configure grading keys";
    protected final String cardfontText= "Card font size:";
    protected final String smallText= "small";
    protected final String mediumText= "medium";
    protected final String largeText= "large";

    public int fontSize = Font.SIZE_SMALL;
    public boolean touchScreen = true;
    public int keys[];

    public AboutPanel(FireScreen screen, String versionInfo,
		      gr.fire.core.CommandListener masterPanel,
		      Command cmdLeft, Command cmdRight)
    {
	super(null, Panel.VERTICAL_SCROLLBAR, true);

	this.masterPanel = masterPanel;
	this.cmdLeft = cmdLeft;
	this.cmdRight = cmdRight;
	setCommandListener(this);	
	setLeftSoftKeyCommand(cmdLeft);
	setRightSoftKeyCommand(cmdRight);

	keys = new int[MapKeysPanel.keyQuery.length];

	this.screen = screen;
	screenWidth = screen.getWidth();

	cmdButton = new Command("invisible", Command.OK, 1);
	cmdLeftRightDone = new Command("invisible", Command.OK, 1);
	cmdKeysDone = new Command("invisible", Command.OK, 1);
	Container aboutCnt = new Container(new BoxLayout(BoxLayout.Y_AXIS));

	// setup fonts
	sectionFont = Font.getFont(Font.FACE_SYSTEM,
				   Font.STYLE_BOLD,
				   Font.SIZE_LARGE);
	sectionFontHeight = sectionFont.getHeight();

	titleFont = Font.getFont(Font.FACE_SYSTEM,
				 Font.STYLE_BOLD,
				 Font.SIZE_MEDIUM);
	titleFontHeight = titleFont.getHeight();

	textFont = Font.getFont(Font.FACE_SYSTEM,
				Font.STYLE_PLAIN,
				Font.SIZE_MEDIUM);
	textFontHeight = textFont.getHeight();

	labelFont = titleFont;
	labelFontHeight = titleFontHeight;

	buttonHeight = labelFontHeight * 2;

	// title image
	try {
	    Image img = Image.createImage("/mnemosyne.png");
	    ImageComponent imgCmp = new ImageComponent(img, "");
	    imgCmp.setLayout(FireScreen.VCENTER | FireScreen.CENTER);
	    imgCmp.setPrefSize(screen.getWidth(), img.getHeight() + 15);
	    imgCmp.validate();
	    aboutCnt.add(imgCmp);
	} catch (IOException e) {
	    System.out.println("IO Exception!"); // XXX
	}

	// credits
	aboutCnt.add(fieldRow(authorText, "Timothy Bourke"));
	aboutCnt.add(fieldRow(mnemosyneText, "Peter Bienstman"));
	aboutCnt.add(fieldRow(sm2Text, "Piotr Wozniak"));
	aboutCnt.add(fieldRow(fireguiText, "Pashalis Padeleris"));

	aboutCnt.add(titleRow(configTitleText, labelFontHeight * 2));

	aboutCnt.add(buttonRow(changeCardsText));

	touchscreenCheck = checkboxRow(touchscreenText, aboutCnt);
	aboutCnt.add(buttonRow(leftrightText));
	aboutCnt.add(buttonRow(gradingkeysText));

	aboutCnt.add(fontsizeRow());

	setLabel(versionInfo);
	// FIXME: update these
	//this.setLeftSoftKeyCommand(cmdOk);
	//this.setRightSoftKeyCommand(cmdExit);

	// XXX Testing XXX
	Calendar cal = Calendar.getInstance();
	TimeZone tz = cal.getTimeZone();
	long tzoff = tz.getRawOffset() /* / 3600000 */;
	Debug.logln("tzoff=" + Long.toString(tzoff));
	// XXX Testing XXX

	set(aboutCnt);
	repaintControls();
    }

    private Container titleRow(String title, int extraGap)
    {
	Container row = new Container(new BoxLayout(BoxLayout.Y_AXIS));

	int titleWidth = screenWidth / 2;
	
	TextComponent titleCmp = new TextComponent(title, screenWidth);
	titleCmp.setFont(sectionFont);

	int valign = FireScreen.TOP;
	if (extraGap > 0) {
	    valign = FireScreen.BOTTOM;
	}

	titleCmp.setLayout(valign | FireScreen.CENTER);
	titleCmp.validate();

	row.add(titleCmp);
	row.setPrefSize(screenWidth, sectionFontHeight + extraGap);

	return row;
    }

    private Container fieldRow(String title, String text)
    {
	Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));

	int titleWidth = screenWidth / 2;
	
	TextComponent titleCmp = new TextComponent(title + ":", titleWidth);
	titleCmp.setFont(titleFont);
	titleCmp.setLayout(FireScreen.TOP | FireScreen.LEFT);
	titleCmp.validate();

	TextComponent textCmp = new TextComponent(text, titleWidth);
	textCmp.setFont(textFont);
	textCmp.setLayout(FireScreen.TOP | FireScreen.LEFT);
	textCmp.validate();

	row.add(titleCmp);
	row.add(textCmp);
	row.setPrefSize(screenWidth, titleFontHeight);

	return row;
    }

    private InputComponent checkboxRow(String text, Container cnt)
    {
	Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));

	InputComponent checkbox = new InputComponent(InputComponent.CHECKBOX);
	checkbox.setValue(text);
	checkbox.setCommandListener(this);
	checkbox.setCommand(cmdButton);
	checkbox.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
	checkbox.setBackgroundColor(0xaaaaaa); // FIXME: adjust
	checkbox.setPrefSize(InputComponent.RADIO_WIDTH,
			     InputComponent.RADIO_HEIGHT);
	checkbox.setLeftSoftKeyCommand(cmdLeft);
	checkbox.setRightSoftKeyCommand(cmdRight);
	checkbox.validate();

	TextComponent title = new TextComponent("  " + text,
				screenWidth - InputComponent.RADIO_WIDTH);
	title.setFont(labelFont);
	title.setLayout(FireScreen.LEFT | FireScreen.VCENTER);
	title.validate();

	row.add(checkbox);
	row.add(title);
	row.setPrefSize(screenWidth, labelFontHeight + controlGap);
	row.validate();

	cnt.add(row);

	return checkbox;
    }

    private Container buttonRow(String text)
    {
	Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));

	InputComponent button = new InputComponent(InputComponent.BUTTON);
	button.setValue(text);
	button.setCommandListener(this);
	button.setCommand(cmdButton);
	button.setFont(labelFont);
	button.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
	button.setForegroundColor(0x000000); // FIXME: adjust
	button.setBackgroundColor(0xaaaaaa); // FIXME: adjust
	button.setPrefSize(screenWidth, buttonHeight);
	button.setLeftSoftKeyCommand(cmdLeft);
	button.setRightSoftKeyCommand(cmdRight);
	button.validate();

	row.add(button);
	row.setPrefSize(screenWidth, buttonHeight + controlGap);
	row.validate();

	return row;
    }

    private InputComponent radioRow(String text, int width, Container cnt)
    {
	Container row = new Container(new BoxLayout(BoxLayout.X_AXIS));

	int inputHeight = Math.max(labelFontHeight,
				   InputComponent.RADIO_HEIGHT);

	InputComponent radio = new InputComponent(InputComponent.RADIO);
	radio.setValue(text);
	radio.setCommandListener(this);
	radio.setCommand(cmdButton);
	radio.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
	radio.setBackgroundColor(0xaaaaaa); // FIXME: adjust
	radio.setPrefSize(InputComponent.RADIO_WIDTH,
			  InputComponent.RADIO_HEIGHT);
	radio.setLeftSoftKeyCommand(cmdLeft);
	radio.setRightSoftKeyCommand(cmdRight);
	radio.validate();

	TextComponent title = new TextComponent("  " + text,
				width - InputComponent.RADIO_WIDTH);
	title.setFont(labelFont);
	title.setLayout(FireScreen.LEFT | FireScreen.VCENTER);
	title.validate();

	row.add(radio);
	row.add(title);
	row.setLayout(FireScreen.LEFT | FireScreen.VCENTER);
	row.setPrefSize(width, inputHeight);

	cnt.add(row);

	return radio;
    }

    private Container fontsizeRow()
    {
	Container row = new Container(new GridLayout(3, 2));

	int titleWidth = labelFont.stringWidth(cardfontText) + 5;
	TextComponent title = new TextComponent(cardfontText, titleWidth);
	title.setFont(labelFont);
	title.setLayout(FireScreen.LEFT | FireScreen.TOP);
	title.validate();

	TextComponent blank1 = new TextComponent("");
	TextComponent blank2 = new TextComponent("");

	// FIXME: implement this by getTheme(), setFontProperty(), setTheme()

	int radioWidth = screenWidth - titleWidth;
	row.add(title);
	smallRadio = radioRow(smallText, radioWidth, row);
	row.add(blank1);
	mediumRadio = radioRow(mediumText, radioWidth, row);
	row.add(blank2);
	largeRadio = radioRow(largeText, radioWidth, row);

	row.setPrefSize(screenWidth, labelFontHeight * 3 + controlGap);

	return row;
    }

    private void repaintControls()
    {
	touchscreenCheck.setChecked(touchScreen);
	touchscreenCheck.repaint();

	smallRadio.setChecked(fontSize == Font.SIZE_SMALL);
	smallRadio.repaint();
	mediumRadio.setChecked(fontSize == Font.SIZE_MEDIUM);
	mediumRadio.repaint();
	largeRadio.setChecked(fontSize == Font.SIZE_LARGE);
	largeRadio.repaint();
    }

    public void commandAction(javax.microedition.lcdui.Command cmd, Displayable d)
    {
	System.out.println("--AboutPanel.commandAction(Displayable)"); // XXX
    }

    public void commandAction(javax.microedition.lcdui.Command cmd, Component c)
    {
	System.out.println("--AboutPanel.commandAction(Component)"); // XXX

	if (cmdButton.equals(cmd)) {
	    InputComponent input = (InputComponent)c;
	    String val = input.getValue();

	    if (changeCardsText.equals(val)) {
		// FIXME: todo

	    } else if (touchscreenText.equals(val)) {
		touchScreen = !input.isChecked();

	    } else if (leftrightText.equals(val)) {
		MapCommandKeysPanel mkp =
		    new MapCommandKeysPanel(screen, this, cmdLeftRightDone);
		screen.setCurrent(mkp);

	    } else if (gradingkeysText.equals(val)) {
		MapKeysPanel mkp = new MapKeysPanel(screen, this, cmdKeysDone);
		screen.setCurrent(mkp);

	    } else if (smallText.equals(val)) {
		fontSize = Font.SIZE_SMALL;
	    } else if (mediumText.equals(val)) {
		fontSize = Font.SIZE_MEDIUM;
	    } else if (largeText.equals(val)) {
		fontSize = Font.SIZE_LARGE;
	    }

	    repaintControls();

	} else if (cmdLeftRightDone.equals(cmd)) {
	    screen.setCurrent(this);
	    repaintControls();

	} else if (cmdKeysDone.equals(cmd)) {
	    MapKeysPanel mkp = (MapKeysPanel)c;
	    for (int i=0; i < mkp.keyCode.length; ++i) {
		keys[i] = mkp.keyCode[i];
	    }
	    screen.setCurrent(this);
	    repaintControls();

	} else if (cmdLeft.equals(cmd) || cmdRight.equals(cmd)) {
	    masterPanel.commandAction(cmd, (Component)this);
	}
    }
}

