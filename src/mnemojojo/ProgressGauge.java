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

import mnemogogo.mobile.hexcsv.Progress;

import gr.fire.core.FireScreen;
import gr.fire.ui.ProgressbarAnimation;

public class ProgressGauge
    implements Progress
{
    private ProgressbarAnimation progressGauge=null;
    private int progressValue = 0;
    private int progressTotal = 0;

    public ProgressGauge()
    {
    }

    private void setGauge(String msg)
    {
        progressGauge = new ProgressbarAnimation(msg);
        progressGauge.setWidth(FireScreen.getScreen().getWidth());
        progressGauge.setHeight(
            FireScreen.getTheme().getFontProperty("titlebar.font").getHeight());
        progressGauge.setPosition(0,0);
    }

    public void startOperation(int length, String msg)
    {
        showGauge(msg);
        progressValue = 0;
        progressTotal = length;
    }

    public void updateOperation(int delta)
    {
        progressValue += delta;
        if (progressGauge != null) {
            progressGauge.progress((100 * progressValue) / progressTotal);
            FireScreen.getScreen().serviceRepaints();
        }
    }

    public void stopOperation()
    {
        hideGauge();
    }

    void showGauge(String msg)
    {
        if (progressGauge != null) {
            progressGauge.setMessage(msg);
        } else {
            setGauge(msg);
            FireScreen.getScreen().addComponent(progressGauge, 6);
        }
        FireScreen.getScreen().serviceRepaints();
    }
    
    void hideGauge()
    {
        if (progressGauge != null) {
            FireScreen.getScreen().removeComponent(progressGauge);
            progressGauge = null;
        }
    }
}

