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

/*
 * TODO:
 *  - sign and run on phone (get rid of security warnings)
 *  - does not stop at end of new cards!
 *  
 */
package mnemojojo;

import java.lang.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

public class FireMIDlet
    extends Core
    implements CommandListener,
	       gr.fire.core.CommandListener,
	       Runnable,
	       KeyListener,
	       Progress
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

    List activeList;

    private ProgressbarAnimation progressGauge=null;
    private int progressValue = 0;
    private int progressTotal = 0;

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

    private final int TASK_NONE = 0;
    private final int TASK_LOAD = 1;
    private int task = TASK_NONE;

    protected final int BUTTONS_NONE = 0;
    protected final int BUTTONS_SHOW = 1;
    protected final int BUTTONS_GRADE = 2;

    public FireMIDlet()
    {
	Debug.logln("FireMIDlet()"); // XXX
	//try { // XXX
	Log.showDebug = debug;

	display = Display.getDisplay(this);

	// initialize a browser instance
	screen = FireScreen.getScreen(display);
	screen.setFullScreenMode(true);
	try {
	    screen.setTheme(new FireTheme("file://theme.properties"));
	} catch (Exception e) {}

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
	startWait(savingText, 1, 0);
	saveCards();
	screen.destroy();
	notifyDestroyed();
    }

    public void setCardDir(String cardPath)
	throws IOException
    {
	httpClient.setUrlPrefix(cardPath);
	path = new StringBuffer(cardPath);
	pathLen = path.length();
    }

    private Page makePage(String contents)
    {
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
    private Panel makeButtonRow(String symbols[])
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
	    button.setCommand(cmdButton);
	    button.setLeftSoftKeyCommand(cmdButton); // FIXME: keep this?
	    button.setForegroundColor(0xFF0000); // FIXME: adjust
	    button.setFont(buttonFont);
	    button.setLayout(FireScreen.CENTER | FireScreen.VCENTER);
	    pad.add(button);
	}
	
	Panel padPane = new Panel(pad, Panel.NO_SCROLLBAR, false);		
	padPane.setShowBackground(true);
	padPane.setPrefSize(screen.getWidth(), buttonFont.getHeight() * 2);

	return padPane;
    }

    private Panel makeGradeButtons()
    {
	String buttons[] = {"0", "1", "2", "3", "4", "5"};
	return makeButtonRow(buttons);
    }

    private Panel makeShowButtons()
    {
	String buttons[] = {showAnswerText};
	return makeButtonRow(buttons);
    }

    private Panel makeDisplay(Page htmlPage, int buttonMode)
    {
	boolean htmlDecorations = (buttonMode == BUTTONS_NONE);

	// create a panel to display cards / information
	Panel htmlPanel = new Panel(htmlPage.getPageContainer(),
				    Panel.VERTICAL_SCROLLBAR, htmlDecorations);
	htmlPanel.setCommandListener(this);
	htmlPanel.setDragScroll(true);

	if (buttonMode == BUTTONS_NONE) {
	    return htmlPanel;
	}

	Container controls = new Container(new BoxLayout(BoxLayout.Y_AXIS));
	controls.add(htmlPanel);

	switch (buttonMode) {
	case BUTTONS_SHOW:
	    controls.add(makeShowButtons()); // FIXME: cache these buttons
	    break;

	case BUTTONS_GRADE:
	    controls.add(makeGradeButtons()); // FIXME: cache these buttons
	    break;
	}

	return new Panel(controls, Panel.NO_SCROLLBAR, true);
    }

    public void setCard(Card card, int numLeft)
	throws Exception, IOException
    {
	String curTitle = card.categoryName() + "\t"
			    + Integer.toString(numLeft);

	questionPanel = makeDisplay(makePage(makeCardHtml(false).toString()),
				    BUTTONS_SHOW);
	questionPanel.setLeftSoftKeyCommand(cmdShow);
	questionPanel.setRightSoftKeyCommand(cmdExit);
	questionPanel.setKeyListener(this);
	questionPanel.setLabel(curTitle);

	answerPanel = makeDisplay(makePage(makeCardHtml(true).toString()),
				  BUTTONS_GRADE);
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

    public void startOperation(int length)
    {
	progressValue = 0;
	progressTotal = length;
    }

    public void updateOperation(int delta)
    {
	progressValue += delta;
	if (progressGauge != null) {
	    progressGauge.progress((100 * progressValue) / progressTotal);
	}
    }

    void startWait(String msg, int length, int initial)
    {
	showGauge(msg);
    }

    private void showCardDirList()
    {
	int selected = -1;

	startWait(lookingText, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
	String[] dbs = FindCardDir.checkStandard();
	if (dbs == null) {
	    dbs = FindCardDir.list();
	}

	if (dbs == null || dbs.length == 0) {
	    showFatal(nocardsText, true);
	    return;
	    //dbs = FindCardDir.standardList();
	}

	activeList = new List(openTitle, Choice.IMPLICIT);
	for (int i=0; i < dbs.length; ++i) {
	    activeList.append(dbs[i].substring(7), null);
	    if (config.cardPath.equals(dbs[i])) {
		selected = i;
	    }
	}

	Command selectCommand = new Command(openText, Command.ITEM, 1);
	activeList.setSelectCommand(selectCommand);

	if (selected > 0) {
	    activeList.setSelectedIndex(selected, true);
	}

	activeList.addCommand(
	    new Command(exitText, Command.EXIT, 2));
	activeList.setCommandListener(this);

	display.setCurrent(activeList);
	current = CARD_DIRS;
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
	addStatRow(msg, "", config.cardPath);
	addStatRow(msg, daysRemainingText + ": ", carddb.daysLeft());
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

	Panel statPanel = makeDisplay(makePage(msg.toString()), BUTTONS_NONE);

	if (statPanel != null) {
	    if (returnTo == ANSWER) {
		statPanel.setRightSoftKeyCommand(cmdShowA);
	    } else {
		statPanel.setRightSoftKeyCommand(cmdShowQ);
	    }

	    statPanel.setLabel(statisticsText);
	    screen.setCurrent(statPanel);
	}
    }

    void showAbout()
    {
	AboutPanel aboutPanel = new AboutPanel(screen, versionInfo);
	current = ABOUT;
	screen.setCurrent(aboutPanel);
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
	    // FIXME:
	    String val = ((InputComponent)c).getValue();
	    System.out.println("button: " + val);

	    if (val.equals("Show touchscreen buttons")) {
		InputComponent check = (InputComponent)c;
		if (check.isChecked()) {
		    System.out.println("was checked");
		} else {
		    System.out.println("wasn't checked");
		}
		check.setChecked(!check.isChecked());
		check.repaint();
	    }
	}

	if (current == ABOUT && label.equals(okText)) {
	    if (config.cardPath.equals("")
		|| !(FindCardDir.isCardDir(new StringBuffer(config.cardPath))))
	    {
		showCardDirList();
	    } else {
		startWait(loadingText, 1, 0);
		loadCards();
		//carddb.dumpCards();
		showNextQuestion();
		checkExportTime();
	    }
	} else if (current == KEYMAP && label.equals(okText)) {
	    config.leftSoftKey = screen.leftSoftKey;
	    config.rightSoftKey = screen.rightSoftKey;
	    config.save();
	    showAbout();

	} else if (label.equals(showText)) {
	    showAnswerScreen();

	} else if (label.equals(exitText)) {
	    startWait(savingText, 1, 0);
	    saveCards();
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

	if (label.equals(exitText)) {
	    startWait(savingText, 1, 0);
	    saveCards();
	    notifyDestroyed();
	    return;

	} else if (label.equals(openText)) {
	    task = TASK_LOAD;
	    showAbout();
	    startWait(loadingText, 1, 0);
	    display.callSerially(this);
	    return;
	}
    }

    public void keyReleased(int code, Component src)
    {
	switch (current) {
	case QUESTION:
	    switch (code)
	    {
	    case FireScreen.KEY_STAR:
		curCard.skip = true;
		showNextQuestion();
		break;

	    case FireScreen.KEY_POUND:
		showStats(QUESTION);
		break;
	    }
	    break;

	case ANSWER:
	    int grade = -1;

	    switch(code)
	    {
	    case FireScreen.KEY_NUM0: grade = 0; break;
	    case FireScreen.KEY_NUM1: grade = 1; break;
	    case FireScreen.KEY_NUM2: grade = 2; break;
	    case FireScreen.KEY_NUM3: grade = 3; break;
	    case FireScreen.KEY_NUM4: grade = 4; break;
	    case FireScreen.KEY_NUM5: grade = 5; break;
	    case FireScreen.KEY_POUND: showStats(ANSWER); break;
	    }

	    if (grade != -1) {
		doGrade(grade);
		showNextQuestion();
	    }

	    break;
	
	default:
	    break;
	}
    }

    public void keyRepeated(int code, Component src) {	}
    public void keyPressed(int code, Component src) { }

    public void run()
    {
	switch (task) {
	case TASK_LOAD:
	    StringBuffer file = new StringBuffer("file://");
	    file.append(activeList.getString(activeList.getSelectedIndex()));
	    String newCardPath = file.toString();
	    activeList = null;

	    config.cardPath = newCardPath;
	    config.save();

	    try {
		//setCardDir("file://");
		setCardDir(config.cardPath.toString());
	    } catch (IOException e) {
		showFatal(e.toString(), true);
		return;
	    }

	    loadCards();
	    showNextQuestion();

	    checkExportTime();

	    task = TASK_NONE;
	    break;

	default:
	    break;
	}
    }

    private void showGauge(String message)
    {
	if (progressGauge != null) {
	    hideGauge();
	}
	
	progressGauge = new ProgressbarAnimation(message);
	
	Font font = FireScreen.getTheme().getFontProperty("titlebar.font");
	int sw = screen.getWidth();
	int mw = font.stringWidth(message);

	progressGauge.setWidth(sw);
	progressGauge.setHeight(font.getHeight());
	screen.addComponent(progressGauge, 6);
    }
    
    private void hideGauge()
    {
	if (progressGauge != null) {
	    screen.removeComponent(progressGauge);
	    progressGauge=null;
	}
    }
}

