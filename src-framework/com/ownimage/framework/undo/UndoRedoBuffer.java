/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.undo;

import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;

public class UndoRedoBuffer implements IUndoRedoBuffer, IUndoRedoBufferProvider {

    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    /**
     * The mPointer always points to the position that the next elements would be added to.
     */
    private int mPointer;
    private final int mSize;

    private Vector<IUndoRedoAction> mStack;
    private Vector<Id> mSavePointIds;
    private SavePointBuffer mSavePoint;

    public UndoRedoBuffer(final int pSize) {
        mSize = pSize;
        mStack = new Vector<>(mSize);
        mPointer = 0;
    }

    @Override
    public synchronized void add(final IUndoRedoAction pAction) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pAction", pAction);

        if (pAction == null) {
            throw new IllegalArgumentException("You must not add a null pAction");
        }

        if (mSavePoint == null) {
            addToMainBuffer(pAction);
        } else {
            mSavePoint.add(pAction);
        }

        Framework.logExit(mLogger);
    }

    private synchronized void addToMainBuffer(final IUndoRedoAction pAction) {
        Framework.logEntry(mLogger);

        if (mPointer == mSize) { // need to discard the oldest element
            mStack.remove(0);
            mPointer--;
        }

        // remove elements that may have been undone
        final int size = mStack.size();
        for (int i = mPointer; i < size; i++) {
            mStack.remove(mPointer);
        }

        mStack.add(mPointer, pAction);
        mPointer++;

        Framework.logExit(mLogger);
    }

    public void endSavepoint(final Id pId) {

        if (mSavePoint == null) {
            throw new IllegalArgumentException("No savepoint active");
        }

        if (mSavePoint.getId() == pId) {
            if (mSavePointIds.size() != 0) {
                throw new IllegalArgumentException("other savepoints are active");
            }

            mSavePoint.lock();
            addToMainBuffer(mSavePoint);
            mSavePoint = null;
            mSavePointIds = null;

        } else {
            if (!mSavePointIds.remove(pId)) {
                throw new IllegalArgumentException("savepoint not in currenlty active set");
            }
        }
    }

    @Override
    public UndoRedoBuffer getUndoRedoBuffer() {
        return this;
    }

    @Override
    public synchronized boolean redo() {
        Framework.logEntry(mLogger);

        if (mSavePoint != null) {
            throw new IllegalStateException("Cannot call redo when a savepoint is active.");
        }

        boolean redo = false;

        if (mPointer < mStack.size()) {
            final IUndoRedoAction undoRedoAction = mStack.get(mPointer);
            undoRedoAction.redo();
            mPointer++;
            redo = true;
        }

        Framework.logExit(mLogger, redo);
        return redo;
    }

    /**
     * Reset and destroy all buffers. For test purposes
     */
    public void resetAndDestroyAllBuffers() {
        // TODO this can probably be removed ... as the UndoRedoBuffer is no longer a singleton.
        mStack = new Vector<IUndoRedoAction>(mSize);
        mPointer = 0;
        mSavePoint = null;
        mSavePointIds = null;
    }

    public Id startSavepoint(final String pString) {
        Framework.checkParameterNotNullOrEmpty(mLogger, pString, "pString");

        Id id = new Id(pString);
        if (mSavePoint == null) {
            mSavePoint = new SavePointBuffer(pString, id);
            mSavePointIds = new Vector<Id>();
        } else {
            mSavePointIds.add(id);
        }

        return id;
    }

    @Override
    public synchronized boolean undo() {
        Framework.logEntry(mLogger);

        if (mSavePoint != null) {
            throw new IllegalStateException("Cannot call redo when a savepoint is active.");
        }

        boolean undo = false;

        if (mPointer > 0) {
            mPointer--;
            final IUndoRedoAction undoRedoAction = mStack.get(mPointer);
            undoRedoAction.undo();
            undo = true;
        }
        Framework.logExit(mLogger, undo);
        return undo;
    }
}
