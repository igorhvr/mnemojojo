/*
 * Fire is a fast, themable UI toolkit and xHTML/CSS renderer for mobile application 
 * and game development. It is an eye-candy alternative to the standard midp2 UI 
 * components and unlike them it produces a superior UI result on all mobile devices!
 *  
 * Copyright (C) 2006,2007,2008,2009,2010 Pashalis Padeleris (padeler at users.sourceforge.net)
 * 
 * This file is part of Fire.
 *
 * Fire is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Fire.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mnemojojo;

import gr.fire.core.Component;
import gr.fire.util.Log;
import gr.fire.util.Logger;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * @author padeler
 *
 */
public class Console extends TextBox implements Logger,CommandListener
{
    
    private StringBuffer buffer;
    private int size=5000;
    private Command main,refresh;
    private gr.fire.core.CommandListener parent;
    private Display disp;
    
    public Console(Display disp, gr.fire.core.CommandListener parent,Command main,int size)
    {
        super("Console","",size,TextField.ANY);
        this.disp = disp;
        this.size=size;
        this.main = main;
        this.parent = parent;
        
        buffer = new StringBuffer(size);
        refresh = new Command("Refresh",Command.OK,2);
        
        setCommandListener(this);
        addCommand(main);
        // addCommand(new javax.microedition.lcdui.Command("Clear", Command.OK, 1));
        // addCommand(refresh);
    }

    public void println(String txt)
    {
        try{
            synchronized (buffer)
            {
                trim(txt.length());
                buffer.append(txt);
                buffer.append('\n');
            }
        }catch(Exception e){
            // logging failed.
            if(Log.removeLogDestination(this))
                Log.logError("Exception inside logger.", e);
            else {
                System.out.println("Exception inside logger");
                e.printStackTrace();
            }
        }
    }
    
    private void trim(int s)
    {
        int len= buffer.length();
        if(len+s>size)
        {
            int ts = 2*s; // remove twice the length of the text to avoid continued trimming.
            if(len-ts>0) buffer.delete(0, ts);
            else buffer.delete(0,len); // delete all.
        }
    }
    
    
    
    public void print()
    {
        delete(0,size());
        insert(buffer.toString(),0);
    }
    
    /* (non-Javadoc)
     * @see javax.microedition.lcdui.CommandListener#commandAction(javax.microedition.lcdui.Command, javax.microedition.lcdui.Displayable)
     */
    public void commandAction(javax.microedition.lcdui.Command c, Displayable d)
    {
        if(c==main)
        {
            parent.commandAction(main, new Component());
            return;
        }
        if(c==refresh)
        {
            print();
            return;
        }
        
        // else c==clear
        synchronized (buffer)
        {
            buffer.delete(0, buffer.length());
        }
    }

}
