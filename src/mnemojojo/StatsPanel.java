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

// TODO: tidy up the imports
import java.lang.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
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

import mnemogogo.mobile.hexcsv.HexCsvJ2ME;
import mnemogogo.mobile.hexcsv.Card;
import mnemogogo.mobile.hexcsv.FindCardDirJ2ME;
import mnemogogo.mobile.hexcsv.Progress;

import gr.fire.browser.Browser;
import gr.fire.browser.util.Page;
import gr.fire.ui.FireTheme;
import gr.fire.core.FireScreen;
import gr.fire.core.KeyListener;
import gr.fire.core.Component;
import gr.fire.core.Container;
import gr.fire.core.Theme;
import gr.fire.util.Log;
import gr.fire.util.FireConnector;
import gr.fire.core.FireListener;

public class StatsPanel
    extends Panel
{
    public static Browser browser;

    protected static final String forDaysText
            = "Scheduled cards for the next days";
    protected static final String inText = "In";
    protected static final String daysText = "day(s)";

    protected static final String freeMemoryText = "Free bytes: ";
    protected static final String totalMemoryText = "Total bytes: ";

    protected static final String cardsdirText = "Cards: ";

    protected static final String gradeText = "Grade";
    protected static final String statisticsText = "Statistics";
    protected static final String currentCardText = "Current Card";
    protected static final String easinessText = "Easiness";
    protected static final String repetitionsText = "Repetitions";
    protected static final String lapsesText = "Lapses";
    protected static final String daysSinceLastText
            = "Days since last repetition";
    protected static final String daysUntilNextText
            = "Days until next repetition";

    protected static final String updateOverdueText
            = "An export from Mnemosyne is overdue!";
    protected static final String updateTodayText
            = "An export from Mnemosyne is due today.";

    protected String html;

    public StatsPanel(Card curCard,
                      HexCsvJ2ME carddb,
                      Configuration config)
    {
        super((Container)null, Panel.VERTICAL_SCROLLBAR, true, config);
        setLabel(statisticsText);

        html = makeStatsText(carddb, curCard, config.cardpath);
        setLabel(statisticsText);
        makeDisplay();
    }

    protected void makeDisplay()
    {
        set(makePage(html).getPageContainer());
    }

    private Page makePage(String contents)
    {
        try {
            ByteArrayInputStream in =
                new ByteArrayInputStream(contents.getBytes("UTF-8"));
            Page page = browser.loadPage(in, "UTF-8");
            in.close();

            return page;
        } catch (Exception e) { }

        return null;
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

    private void futureScheduleText(HexCsvJ2ME carddb, StringBuffer r)
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

    String makeStatsText(HexCsvJ2ME carddb, Card curCard, String cardpath)
    {
        int daysLeft = carddb.daysLeft();
        StringBuffer msg = new StringBuffer(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<body><p>");

        if (daysLeft < 0) {
            msg.append(updateOverdueText);
        } else if (daysLeft == 0) {
            msg.append(updateTodayText);
        } else {
            futureScheduleText(carddb, msg);
        }

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

        Runtime rt = Runtime.getRuntime();
        msg.append("<br/><br/><table>");
        addStatRow(msg, cardsdirText, cardpath);
        addStatRow(msg, freeMemoryText, rt.freeMemory());
        addStatRow(msg, totalMemoryText, rt.totalMemory());
        msg.append("</table></p></body>");

        return msg.toString();
    }

    public void screenSizeChanged(int newWidth, int newHeight)
    {
        super.screenSizeChanged(newWidth, newHeight);
        makeDisplay();
    }
}
