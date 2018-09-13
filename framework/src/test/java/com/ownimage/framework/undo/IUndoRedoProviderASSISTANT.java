package com.ownimage.framework.undo;

public class IUndoRedoProviderASSISTANT implements IUndoRedoBufferProvider {

    private final UndoRedoBuffer mUndoRedoBuffer = new UndoRedoBuffer(100);

    @Override
    public UndoRedoBuffer getUndoRedoBuffer() {
        return mUndoRedoBuffer;
    }

}
