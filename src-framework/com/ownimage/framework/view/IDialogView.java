package com.ownimage.framework.view;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.IUndoRedoBuffer;
import com.ownimage.framework.util.Framework;

public interface IDialogView {

    public void showModal();

}
