/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.undo;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SavePointBuffer implements IUndoRedoAction {


    public final static Logger mLogger = Framework.getLogger();

    private boolean mLocked;
    private boolean mCanUndo;

    private final String mDescription;

    private final Vector<IUndoRedoAction> mActions = new Vector<IUndoRedoAction>();
    private final Id mId;

    public SavePointBuffer(final String pDescription, final Id pId) {
        mDescription = pDescription;
        mId = pId;
    }

    public synchronized void add(final IUndoRedoAction pAction) {

        if (mLocked) {
            final String msg = "SavePointBuffer " + getDescription() + " has been locked.  add is not allowed.";
            mLogger.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }

        mActions.add(pAction);

    }

    @Override
    public synchronized String getDescription() {
        return mDescription;
    }

    public Id getId() {
        return mId;
    }

    /**
     * Lock prevents any further changes to the SavePointBuffer
     */
    public synchronized void lock() {
        mLocked = true;
        mCanUndo = true;
    }

    @Override
    public synchronized void redo() {
        if (!mLocked) {
            final String msg = "SavePointBuffer " + getDescription() + " has been not been locked.  redo is not allowed.";
            mLogger.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }

        if (mCanUndo) {
            final String msg = "SavePointBuffer " + getDescription() + " has not had undo called last.  redo is not allowed.";
            mLogger.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }

        for (int i = 0; i < mActions.size(); i++) {
            mActions.get(i).redo();
        }
    }

    @Override
    public synchronized void undo() {
        if (!mLocked) {
            final String msg = "SavePointBuffer " + getDescription() + " has been not been locked.  undo is not allowed.";
            mLogger.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }

        if (!mCanUndo) {
            final String msg = "SavePointBuffer " + getDescription() + " has not had redo called last.  undo is not allowed.";
            mLogger.log(Level.SEVERE, msg);
            throw new IllegalStateException(msg);
        }

        for (int i = mActions.size() - 1; i >= 0; i--) {
            mActions.get(i).undo();
        }

        mCanUndo = false;
    }

}