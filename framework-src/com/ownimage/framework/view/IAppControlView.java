package com.ownimage.framework.view;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.IUndoRedoBuffer;

public interface IAppControlView {

	public class DialogOptions {

	}

	public void exit();

	public void redraw();

	public void showDialog(IViewable pViewable, DialogOptions pOptions, ActionControl... pButtons);

	public void showDialog(IViewable pViewable, DialogOptions pOptions, final IUndoRedoBuffer mUndoRedo, ActionControl... pButtons);

}
