/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.undo;

public interface IUndoRedoAction {

    String getDescription();

    void redo();

    void undo();
}
