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
import com.ownimage.framework.view.IView;

public interface IContainer extends IPersist, IUndoRedoBufferProvider, IViewable<IView>, IControlEventDispatcher<IControl<?, ?, ?, ?>>, IControlChangeListener<IControl<?, ?, ?, ?>> {


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
	 * Adds a child Container. A child container will be persisted when the parent is persisted.
	 * Events will trigger from the child container to its parent based on the value of pListenForEvents.
	 * This method should only be called by a container whose parent is set to this object.
	 *
	 * @param pChild
	 *            the child
	 * @param pListenForEvents
	 *            specifies whether this container will listen for control change events from the child
	 */
	public void addContainer(IContainer pChild, boolean pListenForEvents);

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

	public IContainer getParent();


	public Iterator<IViewable<?>> getViewableChildrenIterator();
}
