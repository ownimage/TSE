/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.event;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.view.event.UIEvent.EventType;

import java.util.Date;

public interface IUIEvent {

    public Integer getDeltaX();

    public Integer getDeltaY();

    public EventType getEventType();

    public Integer getHeight();

    double getNormalizedDeltaX();

    double getNormalizedDeltaY();

    double getNormalizedX();

    double getNormalizedY();

    public int getScroll();

    public IControl getSource();

    public Date getWhen();

    public Integer getWidth();

    public Integer getX();

    public Integer getY();

    public String getKey();

    public boolean isAlt();

    public boolean isCtrl();

    public boolean isNormal();

    public boolean isShift();

    public void setDelta(IUIEvent pDragStartEvent);

}
