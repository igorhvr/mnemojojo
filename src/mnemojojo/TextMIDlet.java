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

// This MIDlet has been superseded by FireMIDlet!

/*
 * TODO:
 * - better exception catching and reporting.
 * - ensure that cards are saved before loading a new directory
 */

package mnemojojo;

import java.lang.*;
import java.util.Date;
import java.io.IOException;
import mnemogogo.mobile.hexcsv.HexCsv;
import mnemogogo.mobile.hexcsv.FindCardDir;
import mnemogogo.mobile.hexcsv.Progress;
import mnemogogo.mobile.hexcsv.Card;
import mnemogogo.mobile.hexcsv.LoadedCard;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.lcdui.*;

public class TextMIDlet
    extends Core
    implements CommandListener
{
    Display display;

    private LoadedCard curCard;

    private Form questionForm;
    private Form answerForm;

    private StringItem category;
    private StringItem categoryA;
    private StringItem question;
    private StringItem questionA;
    private boolean questionApresent;
    private int questionAitemNum;
    private StringItem answer;
    private StringItem numleft;
    private StringItem numleftA;

    private String categoryLabel = "Category: ";
    private String numleftLabel = "";
    private String questionLabel = "Q.";
    private String answerLabel = "A.";

    private int gradePriority[] = {4, 3, 2, 2, 1, 5};

    private Displayable prevScreen;
    private Alert curAlert;
    private Gauge curAlertGauge;

    Command cmdExit;
    Command cmdAbout;
    Command cmdOpen;

    public TextMIDlet() {
	System.out.println("jojo: created!");

	cmdExit = new Command(exitText, Command.EXIT, 5);
	cmdAbout = new Command(aboutText, Command.HELP, 6);
	cmdOpen = new Command(openText, Command.SCREEN, 6);
	Command cmdSkip = new Command(skipText, Command.ITEM, 5);

	// reviews forms
	category = new StringItem(categoryLabel, "");
	numleft = new StringItem(numleftLabel, "");
	question = new StringItem(questionLabel, "");
	categoryA = new StringItem(categoryLabel, "");
	numleftA = new StringItem(numleftLabel, "");
	questionA = new StringItem(questionLabel, "");
	answer = new StringItem(answerLabel, "");

	questionForm = new Form(reviewTitle);
	questionForm.append(category);
	questionForm.append(numleft);
	questionForm.append(question);

	answerForm = new Form(reviewTitle);
	answerForm.append(categoryA);
	answerForm.append(numleftA);
	answerForm.append(questionA);
	questionAitemNum = 3;
	questionApresent = true;
	answerForm.append(answer);

	questionForm.addCommand(cmdExit);
	questionForm.addCommand(cmdAbout);
	questionForm.addCommand(cmdOpen);
	questionForm.addCommand(new Command(showText, Command.ITEM, 1));
	questionForm.addCommand(cmdSkip);

	answerForm.addCommand(cmdExit);
	answerForm.addCommand(cmdAbout);
	answerForm.addCommand(cmdOpen);
	for (int i=0; i < 6; ++i) {
	    answerForm.addCommand(
		new Command(gradeText[i], Command.ITEM, gradePriority[i]));
	}
	answerForm.addCommand(cmdSkip);

	questionForm.setCommandListener(this);
	answerForm.setCommandListener(this);
    }

    public void startApp()
	throws MIDletStateChangeException
    {
	System.out.println("jojo: startApp!");
	display = Display.getDisplay(this);

	if (config.cardPath.equals("")
	    || !(FindCardDir.isCardDir(new StringBuffer(config.cardPath))))
	{
	    showCardDirList();
	} else {
	    loadCards();
	    if (nextQuestion()) {
		showQuestionScreen();
	    }
	}

	unpauseThinking();
    }

    public void commandAction(Command com, Displayable dis)
    {
	String title = dis.getTitle();
	String label = com.getLabel();

	if (label.equals(exitText)) {
	    saveCards();
	    notifyDestroyed();
	    return;
	} else if (label.equals(aboutText)) {
	    pauseThinking();
	    showAbout();
	    return;
	}

	if (title.equals(reviewTitle)) {
	    reviewAction(com, dis);
	    return;

	} else if (title.equals(openTitle)) {
	    openAction(com, dis);
	    return;

	}

	if (label.equals(okText)) {
	    unpauseThinking();
	    display.setCurrent(prevScreen);
	    return;
	}
    }

    public void openAction(Command com, Displayable dis)
    {
	String label = com.getLabel();
	List list = (List)dis;

	if (label.equals(okText)) {
	    pauseThinking();
	    StringBuffer file = new StringBuffer("file://");
	    file.append(list.getString(list.getSelectedIndex()));
	    String newCardPath = file.toString();

	    System.out.print("old cardPath:"); //XXX
	    System.out.println(config.cardPath); // XXX
	    System.out.print("new cardPath:"); //XXX
	    System.out.println(newCardPath); // XXX

	    if (!newCardPath.equals(config.cardPath)) {
		System.out.println("...loading..."); //XXX
		saveCards();
		config.cardPath = newCardPath;
		config.save();
		loadCards();
		try {
		    setCardDir(config.cardPath.toString());
		} catch (IOException e) {
		    showFatal(e.toString(), true);
		}
		if (nextQuestion()) {
		    showQuestionScreen();
		}
	    }

	    unpauseThinking();
	    display.setCurrent(prevScreen);
	}
    } 

    public void reviewAction(Command com, Displayable dis)
    {
	String label = com.getLabel();

	if (label.equals(showText)) {
	    stopThinking();
	    showAnswerScreen();

	} else if (label.equals(openText)) {
	    pauseThinking();
	    showCardDirList();
	} else if (label.equals(skipText)) {
	    if (nextQuestion()) {
		showQuestionScreen();
	    }

	} else {
	    for (int i=0; i < 6; ++i) {
		if (label.equals(gradeText[i])) {
		    if (doGrade(i)) {
			if (nextQuestion()) {
			    showQuestionScreen();
			}
		    }
		    break;
		}
	    }
	}
    }

    private void showQuestionScreen() {
	prevScreen = questionForm;
	display.setCurrent(prevScreen);
    }

    private void showAnswerScreen() {
	prevScreen = answerForm;
	display.setCurrent(prevScreen);
    }

    private void showAbout()
    {
	// about info
	StringBuffer about = new StringBuffer();
	for (int i=0; i < aboutLines.length; ++i) {
	    about.append(aboutLines[i]);
	}

	// about alert
	Alert alert = new Alert(aboutTitle);
	alert.setType(AlertType.INFO);
	alert.setString(about.toString());
	alert.setTimeout(Alert.FOREVER);
	alert.addCommand(
	    new Command(okText, Command.OK, 1));
	alert.setCommandListener(this);
	display.setCurrent(alert);
    }

    void showFatal(String msg, boolean exit)
    {
	System.out.print("showFatal: "); // XXX
	System.out.println(msg); // XXX

	Alert err = new Alert(errTitle);
	err.setType(AlertType.ERROR);
	err.setString(msg);
	err.setTimeout(Alert.FOREVER);
	err.addCommand(
	    new Command(exitText, Command.EXIT, 1));
	err.setCommandListener(this);
	display.setCurrent(err);
    }

    private void showCardDirList()
    {
	int selected = -1;

	startWait(lookingText, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
	String[] dbs = FindCardDir.list();

	if (dbs == null || dbs.length == 0) {
	    showFatal(nocardsText, true);
	    return;
	}

	List cardDirs = new List(openTitle, Choice.EXCLUSIVE);
	for (int i=0; i < dbs.length; ++i) {
	    cardDirs.append(dbs[i].substring(7), null);
	    if (config.cardPath.equals(dbs[i])) {
		selected = i;
	    }
	}

	if (selected > 0) {
	    cardDirs.setSelectedIndex(selected, true);
	}

	cardDirs.addCommand(
	    new Command(exitText, Command.EXIT, 2));
	cardDirs.addCommand(
	    new Command(okText, Command.OK, 1));
	cardDirs.setCommandListener(this);

	display.setCurrent(cardDirs);
    }

    public void startWait(String msg, int length, int initial) {
	curAlert = new Alert(waitTitle);
	curAlert.setType(AlertType.INFO);
	curAlert.setString(msg);
	curAlert.setTimeout(Alert.FOREVER);
	curAlertGauge = new Gauge(null, false, length, initial);
	curAlert.setIndicator(curAlertGauge);
	display.setCurrent(curAlert);
    }

    public void startOperation(int length) {
	curAlertGauge.setMaxValue(length);
    }

    public void updateOperation(int delta) {
	curAlertGauge.setValue(curAlertGauge.getValue() + delta);
    }

    public void setCardDir(String cardPath)
	throws IOException
    {
	curCard = new LoadedCard(cardPath);
    }

    public void setCard(Card card, int numLeft)
	throws IOException
    {
	curCard.loadCard(card);

	category.setText(card.categoryName());
	categoryA.setText(card.categoryName());

	numleft.setText(Integer.toString(numLeft));
	numleftA.setText(Integer.toString(numLeft));

	String qtext = curCard.question.toString();
	question.setText(qtext);
	questionA.setText(qtext);
	answer.setText(curCard.answer.toString());

	if (curCard.isOverlay) {
	    if (questionApresent) {
		answerForm.delete(questionAitemNum);
		questionApresent = false;
	    }
	} else {
	    if (!questionApresent) {
		answerForm.insert(questionAitemNum, questionA);
		questionApresent = true;
	    }
	}
    }

    void showDone()
    {
	Form doneForm = new Form(doneTitle);
	doneForm.append(new StringItem(doneTitle, doneText));
	doneForm.addCommand(cmdExit);
	doneForm.addCommand(cmdAbout);
	doneForm.addCommand(cmdOpen);

	prevScreen = doneForm;
    }
}

