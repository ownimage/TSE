package com.ownimage.framework.app.menu;

import java.util.Iterator;
import java.util.Vector;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class MenuControl implements IMenuItem, IViewable {

	private final Vector<IMenuItem> mMenuItems = new Vector<IMenuItem>(10);

	private String mDisplayName;
	private boolean mIsLocked = false;
	private boolean mIsMenuBar = false;
	private IView mView;

	/**
	 * Instantiates a new MenuBar. This is a top level Menu that can only have Menus as children and is used as the main menu for an
	 * application.
	 */
	public MenuControl() {
		mDisplayName = "";
		mIsMenuBar = true;
	}

	private MenuControl(final boolean pIsMenuBar) {
		mIsMenuBar = pIsMenuBar;
	}

	/**
	 * Instantiates a new Menu.
	 *
	 * @param pDisplayName
	 *            the display name
	 */
	public MenuControl(final String pDisplayName) {
		if (pDisplayName == null || pDisplayName.length() == 0) {
			throw new IllegalArgumentException("pDisplayName must not be null or zero length.");
		}

		mDisplayName = pDisplayName;
	}

	public MenuControl addAction(final ActionControl pAction) {
		if (mIsLocked) {
			throw new IllegalStateException("Cannot add Action as this Menu is locked.");
		}

		if (pAction == null) {
			throw new IllegalArgumentException("pAction must not be null");
		}

		mMenuItems.add(pAction);
		return this;
	}

	public MenuControl addMenu(final MenuControl pMenu) {
		if (pMenu == null) {
			throw new IllegalArgumentException("pMenu must not be null");
		}

		if (isLocked()) {
			throw new IllegalStateException("Cannot add Action as this Menu is locked.");
		}

		if (!pMenu.isLocked()) {
			throw new IllegalArgumentException("pMenu must be locked");
		}

		if (pMenu.isMenuBar()) {
			throw new IllegalArgumentException("Cannot add a Menu which is a MenuBar to a Menu.");
		}

		mMenuItems.add(pMenu);
		return this;
	}

	public IView createMenuView() {
		if (!isLocked()) {
			throw new IllegalArgumentException("pMenu must be locked to createView");
		}

		mView = ViewFactory.getInstance().createMenuView(this);
		return mView;
	}

	@Override
	public IView createView() {
		if (!isLocked()) {
			throw new IllegalArgumentException("pMenu must be locked to createView");
		}

		mView = ViewFactory.getInstance().createView(this);
		return mView;
	}

	public Iterator<IMenuItem> getChildIterator() {
		return mMenuItems.iterator();
	}

	@Override
	public String getDisplayName() {
		return mDisplayName;
	}

	public boolean isLocked() {
		return mIsLocked;
	}

	public boolean isMenuBar() {
		return mIsMenuBar;
	}

	public MenuControl lock() {
		mIsLocked = true;
		return this;
	}

	public MenuControl setEnabled(final boolean pEnabled) {
		if (mView != null) {
			mView.setEnabled(pEnabled);
		}
		return this;
	}

	public MenuControl setVisible(final boolean pVisible) {
		if (mView != null) {
			mView.setVisible(pVisible);
		}
		return this;
	}

}
