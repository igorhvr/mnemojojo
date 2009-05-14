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
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

public class Configuration
{
    public String cardPath;
    public long cards_mtime;

    public Configuration() {
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
	cardPath = readRecord("cardpath");
	if (cardPath == null) {
	    cardPath = new String("");
	}

	String v = readRecord("cards_mtime");
	if (v == null) {
	    cards_mtime = 0;
	} else {
	    cards_mtime = Long.parseLong(v);
	}
    }

    public void save()
    {
	writeRecord("cardpath", cardPath);
	writeRecord("cards_mtime", Long.toString(cards_mtime));
    }
}

