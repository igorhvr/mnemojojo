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
import gr.fire.core.FireScreen;
import gr.fire.core.Container;
import javax.microedition.lcdui.Canvas;

class Panel
    extends gr.fire.core.Panel
{
    CheckKeys keyChecker;
    Panel scrollPanel;

    public Panel(Container cnt, int scrollbarPolicy, boolean showDecorations,
		 CheckKeys keyChecker)
    {
	super(cnt, scrollbarPolicy, showDecorations);
	this.keyChecker = keyChecker;
    }

    public Panel(Container cnt, int scrollbarPolicy, boolean showDecorations)
    {
	super(cnt, scrollbarPolicy, showDecorations);
    }

    public int[] getPrefSize()
    {
	if ((prefWidth == -1) && (prefHeight == -1)) {
	    return null;
	}

	if (prefWidth == -1) {
	    return new int[]{FireScreen.getScreen().getWidth(),prefHeight};
	}

	if (prefHeight == -1) {
	    return new int[]{prefWidth, FireScreen.getScreen().getHeight()};
	}

	return new int[]{prefWidth, prefHeight};
    }

    protected void keyReleased(int keyCode)
    {
	if ((keyListener != null)
	    && (keyChecker != null)
	    && (keyChecker.catchKey(keyCode)))
	{
	    keyListener.keyReleased(keyCode, this);

	} else if (scrollPanel != null) {
	    FireScreen screen = FireScreen.getScreen();
	    int gameCode = screen.getGameAction(keyCode);

	    if (gameCode == Canvas.UP || gameCode == Canvas.DOWN) {
		scrollPanel.scroll(gameCode, normalVScrollLength);
	    } else {
		super.keyReleased(keyCode);
	    }

	} else {
	    super.keyReleased(keyCode);
	}
    }
}

