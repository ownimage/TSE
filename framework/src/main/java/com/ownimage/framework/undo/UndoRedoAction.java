package com.ownimage.framework.undo;

import com.ownimage.framework.control.control.IAction;

public class UndoRedoAction implements IUndoRedoAction {

    private final String mDesctiption;
    private final IAction mRedo;
    private final IAction mUndo;

    public UndoRedoAction(String mDesctiption, IAction mRedo, IAction mUndo) {
        this.mDesctiption = mDesctiption;
        this.mRedo = mRedo;
        this.mUndo = mUndo;
    }

    @Override
    public String getDescription() {
        return mDesctiption;
    }

    @Override
    public void redo() {
        mRedo.performAction();
    }

    @Override
    public void undo() {
        mUndo.performAction();
    }
}
