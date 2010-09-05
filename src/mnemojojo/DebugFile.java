/*
 * Copyright (C) 2009 Timothy Bourke
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package mnemojojo;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.io.file.FileConnection;

import gr.fire.util.Logger;

public class DebugFile
    implements Logger
{
    public static boolean open = false;
    public static PrintStream logFile = null;

    public void open()
    {
        StringBuffer path = new StringBuffer("file://");
        Enumeration roots = FileSystemRegistry.listRoots();

        while (roots.hasMoreElements()) {
            try {
                path.delete(7, path.length());
                path.append("/");
                path.append((String)roots.nextElement());
                path.append("mnemojojo.log");

                FileConnection fconn = (FileConnection)Connector.open(path.toString());
                if (!fconn.exists()) {
                    fconn.create();
                }
                logFile = new PrintStream(fconn.openOutputStream());

                if (logFile != null) {
                    break;
                }
            } catch (SecurityException e) {
            } catch (IOException e) { }
        }

        open = true;
    }

    public void println(String msg)
    {
        if (!open) open();

        if (logFile != null) {
            logFile.print(msg);
            logFile.print("\n");
            logFile.flush();
        }
    }

    public void stopLog()
    {
        if (logFile != null) {
            logFile.close();
            logFile = null;
            open = false;
        }
    }
}

