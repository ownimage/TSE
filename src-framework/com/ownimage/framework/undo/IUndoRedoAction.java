/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.undo;

public interface IUndoRedoAction {


    public String getDescription();

	public void redo();

	public void undo();
}
