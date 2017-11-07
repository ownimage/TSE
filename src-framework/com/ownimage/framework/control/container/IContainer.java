/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.container;

import java.util.Iterator;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlEventDispatcher;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;

public interface IContainer extends IPersist, IUndoRedoBufferProvider, IViewable<IView>, IControlEventDispatcher<IControl<?, ?, ?, ?>>, IControlChangeListener<IControl<?, ?, ?, ?>> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static long serialVersionUID = 1L;

	/**
	 * Adds a child Container. A child container will be persisted when the parent is persisted. Events do not trigger from a child
	 * container to its parent. This method should only be called by a container whose parent is set to this object.
	 * 
	 * @param pChild
	 *            the child
	 */
	public void addContainer(IContainer pChild);

	/**
	 * Adds the control to the container. The control is NOT duplicated so it is still linked to the original control. This means
	 * that if the same control is added to multiple containers the values will be linked ... until the container is saved and
	 * restored when they will be separate.
	 * 
	 * @param pControl
	 *            the control to be added
	 * @return the container so operations can be chained together.
	 */
	public IContainer addControl(IControl<?, ?, ?, ?> pControl);

	@Override
	public void fireControlChangeEvent(IControl<?, ?, ?, ?> pControl);

	@Override
	public void fireControlChangeEvent(IControl<?, ?, ?, ?> pControl, IView pView, boolean pIsMutating);

	@Override
	public boolean fireControlValidate(IControl<?, ?, ?, ?> pControl);

	@Override
	String getDisplayName();

	public IContainer getParent();

	@Override
	public String getPropertyName();

	public Iterator<IViewable<?>> getViewableChildrenIterator();
}
