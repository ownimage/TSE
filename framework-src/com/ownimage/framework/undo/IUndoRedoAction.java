/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.undo;

import com.ownimage.framework.util.Version;

public interface IUndoRedoAction {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	public String getDescription();

	public void redo();

	public void undo();
}
