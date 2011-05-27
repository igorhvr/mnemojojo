/*
 * Copyright (C) 2010 Timothy Bourke
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

            } else if (gameCode == Canvas.LEFT || gameCode == Canvas.RIGHT) {
                scrollPanel.scroll(gameCode, normalHScrollLength);

            } else {
                super.keyReleased(keyCode);
            }

        } else {
            super.keyReleased(keyCode);
        }
    }

    public void screenSizeChanged(int newWidth, int newHeight)
    {
        // side effect: recalculates position of label
        setLabel(getLabel());
    }
}

