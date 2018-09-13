/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.view.event.IUIEvent;

public interface IUIEventListener {

    default void mouseClickEvent(final IUIEvent pEvent) {
    }

    default void mouseDoubleClickEvent(final IUIEvent pEvent) {
    }

    default void mouseDragEndEvent(final IUIEvent pEvent) {
    }

    default void mouseDragEvent(final IUIEvent pEvent) {
    }

    default void mouseDragStartEvent(final IUIEvent pEvent) {
    }

    default void mouseMoveEvent(final IUIEvent pEvent) {
    }

    default void scrollEvent(final IUIEvent pEvent) {
    }

    default void keyPressed(final IUIEvent pEvent) {
    }

    default void keyReleased(final IUIEvent pEvent) {
    }

    default void keyTyped(final IUIEvent pEvent) {
    }

}
