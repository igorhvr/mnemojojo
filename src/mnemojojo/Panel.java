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

class Panel
    extends gr.fire.core.Panel
{
    public Panel(Container cnt, int scrollbarPolicy, boolean showDecorations)
    {
	super(cnt, scrollbarPolicy, showDecorations);
    }

    protected void keyReleased(int keyCode)
    {
	if (keyListener != null) {
	    keyListener.keyReleased(keyCode, this);
	}
	/*
	if (   keyCode == FireScreen.KEY_NUM0
	    || keyCode == FireScreen.KEY_NUM1
	    || keyCode == FireScreen.KEY_NUM2
	    || keyCode == FireScreen.KEY_NUM3
	    || keyCode == FireScreen.KEY_NUM4
	    || keyCode == FireScreen.KEY_NUM5
	    || keyCode == FireScreen.KEY_STAR
	    || keyCode == FireScreen.KEY_POUND)
	{
	    if (keyListener != null) {
		keyListener.keyReleased(keyCode, this);
	    }
	} else {
	    switch (keyCode)
	    {
	    case FireScreen.KEY_NUM6: keyCode = FireScreen.KEY_NUM2; break;
	    case FireScreen.KEY_NUM7: keyCode = FireScreen.KEY_NUM4; break;
	    case FireScreen.KEY_NUM8: keyCode = FireScreen.KEY_NUM6; break;
	    case FireScreen.KEY_NUM9: keyCode = FireScreen.KEY_NUM8; break;
	    default: break;
	    }
	    super.keyReleased(keyCode);
	}
	*/
    }
}

