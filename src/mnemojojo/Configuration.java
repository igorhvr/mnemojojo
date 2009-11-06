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

import mnemogogo.mobile.hexcsv.Progress;

import java.lang.*;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.lcdui.Font;

import gr.fire.core.FireScreen;

public class Configuration
    implements CheckKeys
{
    public String cardpath;
    public long cards_mtime;
    public int leftSoftKey;
    public int rightSoftKey;

    public boolean showButtons;
    public int gradeKey[];
    public int skipKey;
    public int statKey;
    public int fontSize;
    public int cardsToLoad;

    private static final String writingConfigText = "Writing configuration...";

    protected final static int defaultGradeKey[] = {
	    FireScreen.KEY_NUM0,
	    FireScreen.KEY_NUM1,
	    FireScreen.KEY_NUM2,
	    FireScreen.KEY_NUM3,
	    FireScreen.KEY_NUM4,
	    FireScreen.KEY_NUM5
	};

    public Configuration() {
	gradeKey = new int[defaultGradeKey.length];
	load();
    }

    private String readRecord(String name) {
	String r;

	try {
	    RecordStore rs = RecordStore.openRecordStore(name, false);
	    r = new String(rs.getRecord(1));
	    rs.closeRecordStore();
	} catch (RecordStoreException e) {
	    r = null;
	}

	return r;
    }

    private void writeRecord(String name, String value) {
	if (value == null) {
	    return;
	}

	byte[] data = value.getBytes();

	try {
	    RecordStore rs = RecordStore.openRecordStore(name, true);
	    if (rs.getNumRecords() == 0) {
		rs.addRecord(data, 0, data.length);
	    } else {
		rs.setRecord(1, data, 0, data.length);
	    }
	    rs.closeRecordStore();
	} catch (RecordStoreException e) { }
    }

    public void load() {
	cardpath = readRecord("cardpath");

	String v = readRecord("cards_mtime");
	if (v == null) {
	    cards_mtime = 0;
	} else {
	    cards_mtime = Long.parseLong(v);
	}

	v = readRecord("left_soft_key");
	if (v == null) {
	    leftSoftKey = 0;
	} else {
	    leftSoftKey = Integer.parseInt(v);
	}

	v = readRecord("right_soft_key");
	if (v == null) {
	    rightSoftKey = 0;
	} else {
	    rightSoftKey = Integer.parseInt(v);
	}

	v = readRecord("show_buttons");
	if (v == null) {
	    showButtons = false;
	} else {
	    showButtons = v.equals("true");
	}

	for (int i=0; i < gradeKey.length; ++i) {
	    v = readRecord("grade_" + Integer.toString(i));
	    if (v == null) {
		gradeKey[i] = defaultGradeKey[i];
	    } else {
		gradeKey[i] = Integer.parseInt(v);
	    }
	}

	v = readRecord("skip_key");
	if (v == null) {
	    skipKey = FireScreen.KEY_STAR;
	} else {
	    skipKey = Integer.parseInt(v);
	}

	v = readRecord("stat_key");
	if (v == null) {
	    statKey = FireScreen.KEY_POUND;
	} else {
	    statKey = Integer.parseInt(v);
	}

	v = readRecord("font_size");
	if (v == null) {
	    fontSize = Font.SIZE_SMALL;
	} else {
	    fontSize = Integer.parseInt(v);
	}

	v = readRecord("cards_to_load");
	if (v == null) {
	    cardsToLoad = 50;
	} else {
	    cardsToLoad = Integer.parseInt(v);
	}
    }

    public void save(Progress progress)
    {
	progress.startOperation(9 + gradeKey.length, writingConfigText);

	writeRecord("cardpath", cardpath);
	progress.updateOperation(1);

	writeRecord("cards_mtime", Long.toString(cards_mtime));
	progress.updateOperation(1);

	writeRecord("left_soft_key", Integer.toString(leftSoftKey));
	progress.updateOperation(1);

	writeRecord("right_soft_key", Integer.toString(rightSoftKey));
	progress.updateOperation(1);

	writeRecord("show_buttons", showButtons?"true":"false");
	progress.updateOperation(1);


	for (int i=0; i < gradeKey.length; ++i) {
	    writeRecord("grade_" + Integer.toString(i),
			Integer.toString(gradeKey[i]));
	    progress.updateOperation(1);
	}

	writeRecord("skip_key", Integer.toString(skipKey));
	progress.updateOperation(1);

	writeRecord("stat_key", Integer.toString(statKey));
	progress.updateOperation(1);

	writeRecord("font_size", Integer.toString(fontSize));
	progress.updateOperation(1);

	writeRecord("cards_to_load", Integer.toString(cardsToLoad));
	progress.updateOperation(1);

	progress.stopOperation();
    }

    public boolean catchKey(int keyCode)
    {
	if ((keyCode == skipKey) || (keyCode == statKey)) {
	    return true;
	}

	for (int i=0; i < gradeKey.length; ++i) {
	    if (keyCode == gradeKey[i]) {
		return true;
	    }
	}

	return false;
    }
}

