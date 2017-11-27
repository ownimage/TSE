package com.ownimage.framework.control.control;

import com.ownimage.framework.view.event.IUIEvent;

public interface IUIEventListener {

	default void mouseClickEvent(final IUIEvent pEvent) {
	};

	default void mouseDoubleClickEvent(final IUIEvent pEvent) {
	};

	default void mouseDragEndEvent(final IUIEvent pEvent) {
	};

	default void mouseDragEvent(final IUIEvent pEvent) {
	};

	default void mouseDragStartEvent(final IUIEvent pEvent) {
	};

	default void scrollEvent(final IUIEvent pEvent) {
	};
}
