/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.event;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.view.event.UIEvent.EventType;

import java.util.Date;
import java.util.Optional;

public interface IUIEvent {

    EventType getEventType();

    Date getWhen();

    Integer getX();

    Integer getY();

    Integer getWidth();

    Integer getHeight();

    int getScroll();

    IControl getSource();

    Optional<Double> getNormalizedDeltaX();

    Optional<Double> getNormalizedDeltaY();

    double getNormalizedX();

    double getNormalizedY();    Optional<Integer> getDeltaX();

    Optional<Integer> getDeltaY();

    String getKey();

    boolean isAlt();

    boolean isCtrl();

    boolean isNormal();

    boolean isShift();
}
