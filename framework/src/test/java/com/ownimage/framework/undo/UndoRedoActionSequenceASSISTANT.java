package com.ownimage.framework.undo;

public class UndoRedoActionSequenceASSISTANT extends UndoRedoActionASSISTANT {

    private static int mGlobalSequence = 0;
    private int mSequence;

    public UndoRedoActionSequenceASSISTANT(final String pDescription) {
        super(pDescription);
    }

    private static int getNextGlobalSequence() {
        return ++mGlobalSequence;
    }

    public static void resetGlobalSequence() {
        mGlobalSequence = 0;
    }

    public int getFiredSequence() {
        return mSequence;
    }

    @Override
    public void redo() {
        super.redo();
        mSequence = getNextGlobalSequence();
    }

    @Override
    public void reset() {
        super.reset();
        mSequence = 0;
    }

    @Override
    public void undo() {
        super.undo();
        mSequence = getNextGlobalSequence();
    }
}
