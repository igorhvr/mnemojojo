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
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Date; // XXX
import java.util.TimeZone; // XXX
import java.util.Calendar; // XXX
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;

import mnemogogo.mobile.hexcsv.Card;
import mnemogogo.mobile.hexcsv.HexCsv;
import mnemogogo.mobile.hexcsv.FindCardDir;
import mnemogogo.mobile.hexcsv.Progress;
import mnemogogo.mobile.hexcsv.Debug;	// XXX

import gr.fire.browser.Browser;
import gr.fire.browser.util.Page;
import gr.fire.ui.FireTheme;
import gr.fire.core.FireScreen;
import gr.fire.core.KeyListener;
import gr.fire.core.Component;
import gr.fire.core.Theme;
import gr.fire.util.Log;
import gr.fire.util.FireConnector;

// for buttons:
import gr.fire.core.BoxLayout;
import gr.fire.core.Container;
import gr.fire.core.GridLayout;
import gr.fire.ui.InputComponent;

public class FireMIDlet
    extends Core
    implements CommandListener,
	       gr.fire.core.CommandListener,
	       KeyListener
{
    boolean initialized = false;

    StringBuffer path;
    int pathLen;

    Display display;
    HttpClient httpClient;
    Browser browser;
    FireScreen screen;

    Panel questionPanel;
    Panel answerPanel;

    Panel showButtons;
    Panel gradeButtons;

    ProgressGauge progressGauge;

    Command cmdOk;
    Command cmdExit;
    Command cmdShow;
    Command cmdShowQ;
    Command cmdShowA;
    Command cmdButton;

    private final boolean debug = false;

    private int current;
    private final int CARD_DIRS = 0;
    private final int ABOUT = 1;
    private final int QUESTION = 2;
    private final int ANSWER = 3;
    private final int WAIT = 4;
    private final int KEYMAP = 5;

    protected final int BUTTONS_NONE = 0;
    protected final int BUTTONS_SHOW = 1;
    protected final int BUTTONS_GRADE = 2;

    Runtime rt = Runtime.getRuntime(); // XXX

    public void showMemory(String msg) // XXX
    {
	System.gc();
	Debug.logln(msg + ": freeMemory=" + Long.toString(rt.freeMemory())
			    + " / " +  Long.toString(rt.totalMemory()));
    }

    public FireMIDlet()
    {
	if (debug) Debug.logln("FireMIDlet()"); // XXX
	showMemory("before anything"); // XXX
	//try { // XXX
	Log.showDebug = debug;

	display = Display.getDisplay(this);

	// initialize a browser instance
	screen = FireScreen.getScreen(display);
	screen.setFullScreenMode(true);
	try {
	    screen.setTheme(new FireTheme("file://theme.properties"));
	} catch (Exception e) {}

	progressGauge = new ProgressGauge();
	progressHandler = (Progress)progressGauge;

	httpClient = new HttpClient(new FireConnector());
	browser = new Browser(httpClient);

	cmdOk = new Command(okText, Command.OK, 1); 
	cmdExit = new Command(exitText, Command.EXIT, 5);
	cmdShow = new Command(showText, Command.ITEM, 1);
	cmdShowQ = new Command(closeText, Command.ITEM, 1);
	cmdShowA = new Command(closeText, Command.ITEM, 1);
	cmdButton = new Command("invisible", Command.OK, 1);
	//} catch (Exception e) { // XXX
	//    Debug.logln("FireMIDlet(): exception: " + e.toString()); // XXX
	//    e.printStackTrace(); // XXX
	//} // XXX
	//
	showMemory("after midlet init"); // XXX
    }

    public void startApp()
	throws MIDletStateChangeException
    {
	//Debug.logln("startApp()"); // XXX
	//try { // XXX
	if (!initialized) {
	    if (config.leftSoftKey != 0) {
		screen.leftSoftKey = config.leftSoftKey;
	    }
	    if (config.rightSoftKey != 0) {
		screen.rightSoftKey = config.rightSoftKey;
	    }
	    showAbout();
	    initialized = true;
	}
	//} catch (Exception e) { // XXX
	//    Debug.logln("startApp(): exception: " + e.toString()); // XXX
	//    e.printStackTrace(); // XXX
	//} // XXX
    }

    public void destroyApp(boolean unconditional)
    {
	progressGauge.showGauge(savingText);
	saveCards();
	super.destroyApp(unconditional);
	screen.destroy();
	notifyDestroyed();
    }

    public void setCardDir(String cardpath)
	throws IOException
    {
	httpClient.setUrlPrefix(cardpath);
	path = new StringBuffer(cardpath);
	pathLen = path.length();
    }

    private Page makePage(String contents)
    {
	showMemory("makePage"); // XXX

	try {
	    ByteArrayInputStream in =
		new ByteArrayInputStream(contents.getBytes("UTF-8"));
	    Page page = browser.loadPage(in, "UTF-8");
	    in.close();

	    return page;
	} catch (Exception e) {
	    showFatal(e.toString(), false);
	}

	return null;
    }

    private Page loadPage(String pagePath)
    {
	try {
	    Page page = browser.loadPage(pagePath, HttpConnection.GET, null, null);
	    return page;
	} catch (Exception e) {
	    showFatal(e.toString(), false);
	}

	return null;
    }

    StringBuffer makeCardHtml(boolean includeAnswer)
    {
	StringBuffer msg = new StringBuffer(
	    "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<body><p>");

	if (curCard.question == null || curCard.answer == null) {
	    msg.append(nocardloadedText);

	} else if (includeAnswer) {
	    if (!curCard.overlay) {
		msg.append(curCard.question);
		msg.append("<hr/>");
	    }

	    msg.append(curCard.answer);

	} else {
	    msg.append(curCard.question);
	}

	msg.append("</p></body>");

	return msg;

    }

    // from Fire demo: SimpleCalc.java
    private Panel makeButtonRow(String symbols[],
				Command cmdLeft, Command cmdRight)
    {
	Container pad = new Container(new GridLayout(1, symbols.length));
	InputComponent button;
	Font buttonFont = Font.getFont(Font.FACE_SYSTEM,
				       Font.STYLE_BOLD,
				       Font.SIZE_MEDIUM);
	
	for(int i = 0; i<symbols.length; ++i) {
	    button = new InputComponent(InputComponent.BUTTON);
	    button.setValue(symbols[i]); 
	    button.setCommandListener(this);
	    button.setKeyListener(this);
	    button.setCommand(cmdButton);
	    button.setLeftSoftKeyCommand(cmdLeft);
	    button.setRightSoftKeyCommand(cmdRight);
	    button.setForegroundColor(
		FireScreen.getTheme().getIntProperty("button.fg.color"));
	    button.setBackgroundColor(
		FireScreen.getTheme().getIntProperty("button.bg.color"));
	    button.setFont(buttonFont);
	    button.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
	    pad.add(button);
	}
	
	Panel padPane = new Panel(pad, Panel.NO_SCROLLBAR, false);		
	padPane.setShowBackground(true);
	padPane.setBackgroundColor(
	    FireScreen.getTheme().getIntProperty("titlebar.bg.color"));
	padPane.setPrefSize(screen.getWidth(), buttonFont.getHeight() * 2);

	return padPane;
    }

    private Panel makeGradeButtons(Command cmdLeft, Command cmdRight)
    {
	String buttons[] = {"0", "1", "2", "3", "4", "5"};
	return makeButtonRow(buttons, cmdLeft, cmdRight);
    }

    private Panel makeShowButtons(Command cmdLeft, Command cmdRight)
    {
	String buttons[] = {showAnswerText, showStatsText, skipCardText};
	return makeButtonRow(buttons, cmdLeft, cmdRight);
    }

    private Panel makeDisplay(Page htmlPage, int buttonMode,
			      Command cmdLeft, Command cmdRight)
    {
	if (!config.showButtons) {
	    buttonMode = BUTTONS_NONE;
	}
	boolean htmlDecorations = (buttonMode == BUTTONS_NONE);

	// create a panel to display cards / information
	Panel htmlPanel = new Panel(htmlPage.getPageContainer(),
				    Panel.VERTICAL_SCROLLBAR, htmlDecorations);
	htmlPanel.setCommandListener(this);
	htmlPanel.setDragScroll(true);
	htmlPanel.setKeyListener(this);
	htmlPanel.setLeftSoftKeyCommand(cmdLeft);
	htmlPanel.setRightSoftKeyCommand(cmdRight);

	if (buttonMode == BUTTONS_NONE) {
	    return htmlPanel;
	}

	Container controls = new Container(new BoxLayout(BoxLayout.Y_AXIS));
	controls.add(htmlPanel);

	switch (buttonMode) {
	case BUTTONS_SHOW:
	    if (showButtons == null) {
		showButtons = makeShowButtons(cmdLeft, cmdRight);
	    }
	    controls.add(showButtons);
	    break;

	case BUTTONS_GRADE:
	    if (gradeButtons == null) {
		gradeButtons = makeGradeButtons(cmdLeft, cmdRight);
	    }
	    controls.add(gradeButtons);
	    break;
	}

	Panel outer = new Panel(controls, Panel.NO_SCROLLBAR, true);
	outer.setCommandListener(this);
	outer.setKeyListener(this);
	outer.setLeftSoftKeyCommand(cmdLeft);
	outer.setRightSoftKeyCommand(cmdRight);

	return outer;
    }

    public void setCard(Card card, int numLeft)
	throws Exception, IOException
    {
	showMemory("setCard"); // XXX
	String curTitle = card.categoryName() + "\t"
			    + Integer.toString(numLeft);

	questionPanel = makeDisplay(makePage(makeCardHtml(false).toString()),
				    BUTTONS_SHOW, cmdShow, cmdExit);
	questionPanel.setLeftSoftKeyCommand(cmdShow);
	questionPanel.setRightSoftKeyCommand(cmdExit);
	questionPanel.setKeyListener(this);
	questionPanel.setLabel(curTitle);

	answerPanel = makeDisplay(makePage(makeCardHtml(true).toString()),
				  BUTTONS_GRADE, null, cmdExit);
	answerPanel.setRightSoftKeyCommand(cmdExit);
	answerPanel.setKeyListener(this);
	answerPanel.setLabel(curTitle);
    }

    void checkExportTime()
    {
	int days_left = carddb.daysLeft();
	byte icon;
	String msg;

	if (days_left <= 0) {
	    msg = updateOverdueText;
	    icon = gr.fire.ui.Alert.TYPE_WARNING;
	} else if (days_left == 1) {
	    msg = updateTomorrowText;
	    icon = gr.fire.ui.Alert.TYPE_INFO;
	} else {
	    return;
	}

	screen.showAlert(msg, icon,
			 gr.fire.ui.Alert.USER_SELECTED_OK, cmdShowQ, this);
    }

    void showFatal(String msg, boolean exit)
    {
	Command cmdAfter = null;
	if (exit) {
	    cmdAfter = cmdExit;
	}

	screen.showAlert(msg, gr.fire.ui.Alert.TYPE_ERROR,
			 gr.fire.ui.Alert.USER_SELECTED_OK, cmdAfter, this);
    }

    void showDone()
    {
	String msg = doneText;
	screen.showAlert(msg,
			 gr.fire.ui.Alert.TYPE_INFO,
			 gr.fire.ui.Alert.USER_SELECTED_OK,
			 cmdExit, this);
    }

    private void addStatRow(StringBuffer msg, String name, String stat)
    {
	msg.append("<tr><td>");
	msg.append(name);
	msg.append("</td><td>");
	msg.append(stat);
	msg.append("</td></tr>");
    }

    private void addStatRow(StringBuffer msg, String name, int stat)
    {
	addStatRow(msg, name, Integer.toString(stat));
    }

    private void addStatRow(StringBuffer msg, String name, float stat)
    {
	addStatRow(msg, name, Float.toString(stat));
    }

    private void addStatRow(StringBuffer msg, String name, long stat)
    {
	addStatRow(msg, name, Long.toString(stat));
    }

    private void futureScheduleText(StringBuffer r)
    {
	int[] indays = carddb.getFutureSchedule();
	if (indays == null) {
	    return;
	}

	r.append("<table>");
	r.append("<tr><td>" + forDaysText + ":</td><td/></tr>");
	for (int i=0; i < indays.length; ++i) {
	    r.append("<tr><td>");
	    r.append(inText);
	    r.append(" ");
	    r.append(i + 1);
	    r.append(" ");
	    r.append(daysText);
	    r.append(": </td><td>");
	    r.append(indays[i]);
	    r.append("</td></tr>");
	}
	r.append("</table>");
    }

    void showStats(int returnTo)
    {
	StringBuffer msg = new StringBuffer("<body><p><table>");
	addStatRow(msg, "", config.cardpath);
	addStatRow(msg, daysRemainingText + ": ", carddb.daysLeft());
	addStatRow(msg, freeMemoryText + ": ", rt.freeMemory());
	addStatRow(msg, totalMemoryText + ": ", rt.totalMemory());
	msg.append("</table>");

	msg.append("<br/><br/>");
	futureScheduleText(msg);

	if (curCard != null) {
	    msg.append("<br/><br/><table>");
	    addStatRow(msg, gradeText + ": ", curCard.grade);
	    addStatRow(msg, easinessText + ": ", curCard.feasiness());
	    addStatRow(msg, repetitionsText + ": ", curCard.repetitions());
	    addStatRow(msg, lapsesText + ": ", curCard.lapses);
	    addStatRow(msg, daysSinceLastText + ": ",
		curCard.daysSinceLastRep(carddb.days_since_start));
	    addStatRow(msg, daysUntilNextText + ": ",
		curCard.daysUntilNextRep(carddb.days_since_start));
	    msg.append("</table>");
	}

	msg.append("</p></body>");

	Command cmdAction;
	if (returnTo == ANSWER) {
	    cmdAction = cmdShowA;
	} else {
	    cmdAction = cmdShowQ;
	}

	Panel statPanel = makeDisplay(makePage(msg.toString()), BUTTONS_NONE,
				       null, cmdAction);

	if (statPanel != null) {
	    statPanel.setRightSoftKeyCommand(cmdAction);
	    statPanel.setLabel(statisticsText);
	    screen.setCurrent(statPanel);
	}
    }

    void showAbout()
    {
	AboutPanel aboutPanel = new AboutPanel(screen, versionInfo,
					this, cmdOk, cmdExit);

	// copy across the current configured values
	if (config.cardpath != null && FindCardDir.isCardDir(config.cardpath)) {
	    aboutPanel.cardpath = config.cardpath;
	} else {
	    config.cardpath = null;
	    aboutPanel.cardpath = null;
	}
	aboutPanel.fontSize = config.fontSize;
	aboutPanel.touchScreen = config.showButtons;
	int i = 0;
	while (i < 6) {
	    aboutPanel.keys[i] = config.gradeKey[i];
	    ++i;
	}
	aboutPanel.keys[i++] = config.statKey;
	aboutPanel.keys[i++] = config.skipKey;

	current = ABOUT;
	screen.setCurrent(aboutPanel);
	aboutPanel.repaintControls();
    }

    void showQuestionScreen()
    {
	if (questionPanel != null) {
	    screen.setCurrent(questionPanel);
	    current = QUESTION;
	} else {
	    showDone();
	}
    }

    void showAnswerScreen()
    {
	screen.setCurrent(answerPanel);
	current = ANSWER;
    }

    private void showNextQuestion()
    {
	if (nextQuestion()) {
	    //System.out.println(carddb.toString());
	    showQuestionScreen();
	    startThinking();
	} else {
	    showDone();
	}
    }

    public void updateFont(int fontsize)
    {
	Theme theme = screen.getTheme();
	Font cur_font = theme.getFontProperty("xhtml.font");
	Font new_font = Font.getFont(cur_font.getFace(),
				     cur_font.getStyle(),
				     fontsize);
	theme.setFontProperty("xhtml.font", new_font);
	screen.setTheme(theme);
    }

    public void commandAction(javax.microedition.lcdui.Command cmd, Component c)
    {
	String label = cmd.getLabel();

	if (cmd.equals(cmdShowQ)) {
	    showQuestionScreen();
	    unpauseThinking();
	    return;

	} else if (cmd.equals(cmdShowA)) {
	    showAnswerScreen();
	    return;

	} else if (cmd.equals(cmdButton)) {
	    String val = ((InputComponent)c).getValue();
	    System.out.println("button: " + val);

	    if (showAnswerText.equals(val)) {
		showAnswerScreen();

	    } else if (skipCardText.equals(val)) {
		curCard.skip = true;
		showNextQuestion();

	    } else if (showStatsText.equals(val)) {
		showStats(current);

	    } else {
		try {
		    doGrade(Integer.parseInt(val));
		    showNextQuestion();
		} catch (NumberFormatException e) { }
	    }

	}

	if (current == ABOUT) {
	    // Save the settings on both Ok and Exit
	    AboutPanel aboutPanel = (AboutPanel)c;

	    if (aboutPanel.dirty) {
		config.cardpath = aboutPanel.cardpath;
		config.fontSize = aboutPanel.fontSize;
		config.showButtons = aboutPanel.touchScreen;
		int i = 0;
		while (i < 6) {
		    config.gradeKey[i] = aboutPanel.keys[i];
		    ++i;
		}
		config.statKey = aboutPanel.keys[i++];
		config.skipKey = aboutPanel.keys[i++];

		config.leftSoftKey = screen.leftSoftKey;
		config.rightSoftKey = screen.rightSoftKey;

		progressGauge.showGauge(savingConfigText);
		config.save((Progress)progressGauge);
	    }
	}

	if ((current == ABOUT) && (label.equals(okText))) {

	    updateFont(config.fontSize);

	    if (config.cardpath == null) {
		showFatal(selectCarddir, false);

	    } else {
		progressGauge.showGauge(loadingText);
		showMemory("before loadCards()"); // XXX
		loadCards();
		showMemory("after loadCards()"); // XXX
		progressGauge.hideGauge();
		//carddb.dumpCards();
		showNextQuestion();
		checkExportTime();
	    }

	} else if (label.equals(showText)) {
	    showAnswerScreen();

	} else if (label.equals(exitText)) {
	    progressGauge.showGauge(savingText);
	    saveCards();
	    progressGauge.hideGauge();
	    notifyDestroyed();
	}
    }

    public void commandAction(javax.microedition.lcdui.Command cmd, Displayable dis)
    {
	String title = dis.getTitle();
	String label = cmd.getLabel();

	if (cmd.equals(cmdShowQ)) {
	    showQuestionScreen();
	    unpauseThinking();
	    return;

	} else if (cmd.equals(cmdShowA)) {
	    showAnswerScreen();
	    return;
	}
    }

    public void keyReleased(int code, Component src)
    {
	switch (current) {
	case QUESTION:
	    if (code == config.skipKey) {
		curCard.skip = true;
		showNextQuestion();

	    } else if (code == config.statKey) {
		showStats(QUESTION);
	    }
	    break;

	case ANSWER:
	    int grade = -1;

	    for (int i=0; i < config.gradeKey.length; ++i) {
		if (code == config.gradeKey[i]) {
		    grade = i;
		    break;
		}
	    }

	    if (grade != -1) {
		doGrade(grade);
		showNextQuestion();

	    } else if (code == config.statKey) {
		showStats(ANSWER);
	    }
	
	default:
	    break;
	}
    }

    public void keyRepeated(int code, Component src) {	}
    public void keyPressed(int code, Component src) { }
}

