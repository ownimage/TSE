/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.undo;

public interface IUndoRedoBuffer {


    public interface Undo {
        public void undo();
    }

    public interface Redo {
        public void redo();
    }

    default void add(String pDescription, Undo pUndo, Redo pRedo) {
        IUndoRedoAction undoRedoAction = new IUndoRedoAction() {
            @Override
            public String getDescription() {
                return pDescription;
            }

            @Override
            public void redo() {
                pRedo.redo();
            }

            @Override
            public void undo() {
                pUndo.undo();
            }
        };
        add(undoRedoAction);
    }

    public void add(IUndoRedoAction pAction);

    /**
     * Redo the next action in the stack..
     *
     * @return true, if an action has been done. If there are no more actions to do it returns false.
     */
    public boolean redo();

    /**
     * Undo the next action in the stack.
     *
     * @return true, if an action has been undone. If there are no more actions to undo it returns false.
     */
    public boolean undo();

}
