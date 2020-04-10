/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view;

import com.ownimage.framework.undo.UndoRedoBuffer;

public interface IDialogView {

    void showModal();

    void showModal(final UndoRedoBuffer pUndoRedo);

    void setEnabled(boolean pEnabled);

}
