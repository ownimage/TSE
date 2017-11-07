package com.ownimage.framework.view.javafx;

import java.util.Iterator;

import javafx.scene.control.MenuBar;

import com.ownimage.framework.app.menu.IMenuItem;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.view.IView;

public class MenuBarView implements IView {

	private final MenuControl mMenuControl;
	private MenuBar mUI;

	public MenuBarView(final MenuControl pMenuControl) {
		if (pMenuControl == null) { throw new IllegalArgumentException("pMenuBar must not be null."); }

		if (!pMenuControl.isMenuBar()) { throw new IllegalArgumentException("pMenuBar must be a Menu with the isMenuBar set."); }

		if (!pMenuControl.isLocked()) { throw new IllegalArgumentException("pMenuBar must locked."); }
		mMenuControl = pMenuControl;

		createView();
	}

	@Override
	public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
		// TODO Auto-generated method stub

	}

	private void createView() {
		mUI = new MenuBar();

		Iterator<IMenuItem> menuIterator = mMenuControl.getChildIterator();
		while (menuIterator.hasNext()) {
			MenuControl menuControl = (MenuControl) menuIterator.next();
			mUI.getMenus().add(((MenuView) menuControl.createMenuView()).getUI());
			System.out.println("Menu created: " + menuControl.getDisplayName());
		}

		mUI.useSystemMenuBarProperty().set(true);
	}

	public MenuBar getUI() {
		return mUI;
	}

	@Override
	public void redraw() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVisible(final boolean pVisible) {
		// TODO Auto-generated method stub

	}

}
