package com.ownimage.perception.undo;

import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.undo.UndoRedoBuffer;

public class IUndoRedoProviderASSISTANT implements IUndoRedoBufferProvider {

	private final UndoRedoBuffer mUndoRedoBuffer = new UndoRedoBuffer(100);

	@Override
	public UndoRedoBuffer getUndoRedoBuffer() {
		return mUndoRedoBuffer;
	}

}
