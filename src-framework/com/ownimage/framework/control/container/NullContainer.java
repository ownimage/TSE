package com.ownimage.framework.control.container;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;

public class NullContainer implements IContainer {


    public final static Logger mLogger = Framework.getLogger();

	public final static NullContainer NullContainer = new NullContainer();

	private final UndoRedoBuffer mUndoRedoBuffer = new UndoRedoBuffer(1);
	private final IContainer mContainer = new Container("No Container", "noContainer", mUndoRedoBuffer);

	private NullContainer() {
	}

	@Override
	public void addContainer(final IContainer pChild) {
		mContainer.addContainer(pChild);
	}

	@Override
	public void addContainer(IContainer pChild, boolean pListenForEvents) {
		mContainer.addContainer(pChild, pListenForEvents);
	}

	@Override
	public IContainer addControl(final IControl<?, ?, ?, ?> pControl) {
		return mContainer.addControl(pControl);
	}

	@Override
	public void addControlChangeListener(final IControlChangeListener<IControl<?, ?, ?, ?>> pListener) {
		mContainer.addControlChangeListener(pListener);
	}

	@Override
	public void addControlValidator(final IControlValidator pValidator) {
		mContainer.addControlValidator(pValidator);
	}

	@Override
	public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
		mContainer.controlChangeEvent(pControl, pIsMutating);
	}

	@Override
	public IView createView() {
		return mContainer.createView();
	}

	@Override
	public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl) {
		mContainer.fireControlChangeEvent(pControl);
	}

	@Override
	public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl, final IView pView, final boolean pIsMutating) {
		mContainer.fireControlChangeEvent(pControl, pView, pIsMutating);
	}

	@Override
	public boolean fireControlValidate(final IControl<?, ?, ?, ?> pControl) {
		return mContainer.fireControlValidate(pControl);
	}

	@Override
	public String getDisplayName() {
		return mContainer.getDisplayName();
	}

	@Override
	public IContainer getParent() {
		return mContainer.getParent();
	}

	@Override
	public String getPropertyName() {
		return mContainer.getPropertyName();
	}

	@Override
	public UndoRedoBuffer getUndoRedoBuffer() {
		return mContainer.getUndoRedoBuffer();
	}

	@Override
	public Iterator<IViewable<?>> getViewableChildrenIterator() {
		return mContainer.getViewableChildrenIterator();
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	@Override
	public void read(final IPersistDB pDB, final String pId) {
		mContainer.read(pDB, pId);
	}

	@Override
	public void removeControlChangeListener(final IControlChangeListener<IControl<?, ?, ?, ?>> pLIstener) {
		mContainer.removeControlChangeListener(pLIstener);
	}

	@Override
	public void removeControlValidator(final IControlValidator pValidator) {
		mContainer.removeControlValidator(pValidator);
	}

	@Override
	public void write(final IPersistDB pDB, final String pId) throws IOException {
		mContainer.write(pDB, pId);
	}

}
