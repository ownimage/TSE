package com.ownimage.perception.undo;

import com.ownimage.framework.undo.IUndoRedoAction;

public class UndoRedoActionASSISTANT implements IUndoRedoAction {

	private final String mDescription;
	private boolean mRedoFired;
	private boolean mUndoFired;

	public UndoRedoActionASSISTANT(final String pDescription) {
		mDescription = pDescription;
		mRedoFired = false;
		mUndoFired = false;
	}

	@Override
	public String getDescription() {
		return mDescription;
	}

	@Override
	public void redo() {
		mRedoFired = true;
	}

	public boolean redoFired() {
		return mRedoFired;
	}

	public void reset() {
		mRedoFired = false;
		mUndoFired = false;
	}

	@Override
	public void undo() {
		mUndoFired = true;
	}

	public boolean undoFired() {
		return mUndoFired;
	}

}
