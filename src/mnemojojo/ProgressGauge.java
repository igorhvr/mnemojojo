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

import mnemogogo.mobile.hexcsv.Progress;

import gr.fire.ui.FireTheme;
import gr.fire.core.FireScreen;
import gr.fire.core.Component;
import gr.fire.ui.ProgressbarAnimation;

import javax.microedition.lcdui.Font;

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

