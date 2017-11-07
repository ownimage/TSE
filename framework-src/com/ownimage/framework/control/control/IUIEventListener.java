package com.ownimage.framework.control.control;

import com.ownimage.framework.view.event.IUIEvent;

public interface IUIEventListener {

	public void mouseClickEvent(final IUIEvent pEvent);

	public void mouseDoubleClickEvent(final IUIEvent pEvent);

	public void mouseDragEndEvent(final IUIEvent pEvent);

	public void mouseDragEvent(final IUIEvent pEvent);

	public void mouseDragStartEvent(final IUIEvent pEvent);

	public void scrollEvent(final IUIEvent pEvent);
}
