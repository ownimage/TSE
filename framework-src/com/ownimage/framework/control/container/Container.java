/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.container;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.event.ControlEventDispatcher;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.ViewableBase;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

/**
 * The Class Container. Events propagate from a Control to its Container and other registered EventListeners. They do NOT propagate
 * from a Container to its parent.
 * 
 */
public class Container extends ViewableBase<IViewable, IView> implements IContainer {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	private final IUndoRedoBufferProvider mGetUndoRedoBuffer;

	private final IContainer mParent;
	private final String mPropertyName;
	private final String mDisplayName;

	private final Vector<IControl<?, ?, ?, ?>> mChlidControls = new Vector<IControl<?, ?, ?, ?>>();
	private final Vector<IContainer> mChildContainers = new Vector<IContainer>();
	private final Vector<IViewable<?>> mAllChildren = new Vector<IViewable<?>>();

	private final ControlEventDispatcher mEventDispatcher = new ControlEventDispatcher(this);

	public Container(final String pDisplayName, final String pPropertyName, final IUndoRedoBufferProvider pIGetUndoRedoBuffer) {
		this(pDisplayName, pPropertyName, pIGetUndoRedoBuffer, null);
	}

	public Container(final String pDisplayName, final String pPropertyName, final IUndoRedoBufferProvider pGetUndoRedoBuffer, final IContainer pParent) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pGetUndoRedoBuffer, "pIGetUndoRedoBuffer");

		ControlBase.validate(pDisplayName, pPropertyName);

		mGetUndoRedoBuffer = pGetUndoRedoBuffer;
		mDisplayName = pDisplayName;
		mPropertyName = pPropertyName;
		mParent = pParent;

		if (mParent != null) {
			mParent.addContainer(this);
		}

		Framework.logExit(mLogger);
	}

	@Override
	public void addContainer(final IContainer pChild) {
		if (pChild.getParent() != this) { throw new IllegalArgumentException("the parent of pChild needs to be this object."); }
		if (mChildContainers.contains(pChild)) { throw new IllegalArgumentException("this child has already been added"); }

		mChildContainers.add(pChild);
		mAllChildren.add(pChild);
	}

	@Override
	public IContainer addControl(final IControl<?, ?, ?, ?> pControl) {
		if (pControl == null) { throw new NullPointerException("pControl must not be null"); }

		mChlidControls.add(pControl);
		mAllChildren.add(pControl);
		pControl.addControlChangeListener(this);

		return this;
	}

	@Override
	public void addControlChangeListener(final IControlChangeListener pListener) {
		mEventDispatcher.addControlChangeListener(pListener);
	}

	@Override
	public void addControlValidator(final IControlValidator pValidator) {
		mEventDispatcher.addControlValidator(pValidator);
	}

	@Override
	public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
		fireControlChangeEvent(pControl, null, pIsMutating);
	}

	@Override
	public IView createView() { // TODO can we push this into the base class
		IView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	@Override
	public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl) {
		mEventDispatcher.fireControlChangeEvent(pControl);
	}

	@Override
	public void fireControlChangeEvent(final IControl<?, ?, ?, ?> pControl, final IView pView, final boolean pIsMutating) {
		mEventDispatcher.fireControlChangeEvent(pControl, pView, pIsMutating);
	}

	@Override
	public boolean fireControlValidate(final IControl<?, ?, ?, ?> pControl) {
		return mEventDispatcher.fireControlValidate(pControl);
	}

	@Override
	public String getDisplayName() {
		return mDisplayName;
	}

	@Override
	public IContainer getParent() {
		return mParent;
	}

	@Override
	public String getPropertyName() {
		return mPropertyName;
	}

	@Override
	public UndoRedoBuffer getUndoRedoBuffer() {
		return mGetUndoRedoBuffer.getUndoRedoBuffer();
	}

	@Override
	public Iterator<IViewable<?>> getViewableChildrenIterator() {
		return mAllChildren.iterator();
	}

	// public Iterator<IViewable> getViewableChildrenIterator() {
	// return new Iterator<IViewable>() {
	// Iterator<IViewable> mIterator = mAllChildren.iterator();
	//
	// @Override
	// public boolean hasNext() {
	// return mIterator.hasNext();
	// }
	//
	// @Override
	// public IViewable next() {
	// return mIterator.next();
	// }
	//
	// };
	// }

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public void read(final IPersistDB pDB, final String pId) {
		if (pDB == null) { throw new IllegalArgumentException("pDB must not be null"); }
		if (pId == null) { throw new IllegalArgumentException("pId must not be null"); }

		final String id = pId == "" ? mPropertyName : pId + "." + mPropertyName;

		for (final IControl<?, ?, ?, ?> c : mChlidControls) {
			c.read(pDB, id);
		}

		for (final IPersist control : mChildContainers) {
			control.read(pDB, id);
		}

		setValues();
	}

	@Override
	public void removeControlChangeListener(final IControlChangeListener pListener) {
		mEventDispatcher.removeControlChangeListener(pListener);
	}

	@Override
	public void removeControlValidator(final IControlValidator pValidator) {
		mEventDispatcher.removeControlValidator(pValidator);
	}

	/**
	 * Transfers the values in the controls to normal Java types to improve performance. This would be overridden in child classes,
	 * i.e. a Transform.
	 */
	protected void setValues() {
	}

	@Override
	// When a container does not have a parent it writes its controls out without any additional prefix. This is so that the
	// properties of a trasnform, e.g. Rotate can be written out without
	// additional intermediate prefixes and just a name e.g. Transform.0.angle=0.1 rather than
	// Transform.0.ContainerProperty.angle=0.1
	public void write(final IPersistDB pDB, final String pId) {
		if (pDB == null) { throw new IllegalArgumentException("pDB must not be null"); }
		if (pId == null) { throw new IllegalArgumentException("pId must not be null"); }

		final String id = pId == "" ? mPropertyName : pId + "." + mPropertyName;

		for (final IPersist c : mChlidControls) {
			c.write(pDB, id);
		}

		for (final IPersist control : mChildContainers) {
			control.write(pDB, id);
		}
	}

}
