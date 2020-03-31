/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.view.event.ImmutableUIEvent;

public interface IUIEventListener {

    default void mouseClickEvent(final ImmutableUIEvent pEvent) {
    }

    default void mouseDoubleClickEvent(final ImmutableUIEvent pEvent) {
    }

    default void mouseDragEndEvent(final ImmutableUIEvent pEvent) {
    }

    default void mouseDragEvent(final ImmutableUIEvent pEvent) {
    }

    default void mouseDragStartEvent(final ImmutableUIEvent pEvent) {
    }

    default void mouseMoveEvent(final ImmutableUIEvent pEvent) {
    }

    default void scrollEvent(final ImmutableUIEvent pEvent) {
    }

    default void keyPressed(final ImmutableUIEvent pEvent) {
    }

    default void keyReleased(final ImmutableUIEvent pEvent) {
    }

    default void keyTyped(final ImmutableUIEvent pEvent) {
    }

}
