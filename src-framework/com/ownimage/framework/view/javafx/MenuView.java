package com.ownimage.framework.view.javafx;

import java.util.Iterator;
import java.util.logging.Logger;

import com.ownimage.framework.app.menu.IMenuItem;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;

import javafx.application.Platform;
import javafx.scene.control.Menu;

public class MenuView implements IView {

    public final static Logger mLogger = Framework.getLogger();

	private final MenuControl mMenuControl;
	private Menu mUI;

	public MenuView(final MenuControl pMenuControl) {
		if (pMenuControl == null) {
			throw new IllegalArgumentException("pMenuBar must not be null.");
		}

		if (pMenuControl.isMenuBar()) {
			throw new IllegalArgumentException("pMenuBar must be a Menu with the isMenuBar not set.");
		}

		if (!pMenuControl.isLocked()) {
			throw new IllegalArgumentException("pMenuBar must locked.");
		}
		mMenuControl = pMenuControl;

		createView();
	}

	@Override
	public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
		// TODO Auto-generated method stub

	}

	private Menu createSubMenu(final MenuControl pMenu) {
		Menu menu = new Menu(pMenu.getDisplayName());

		Iterator<IMenuItem> childIterator = pMenu.getChildIterator();
		while (childIterator.hasNext()) {
			IMenuItem child = childIterator.next();

			if (child instanceof ActionControl) {
				ActionControl actionControl = (ActionControl) child;
				MenuItemView menuItem = (MenuItemView) actionControl.createMenuItemView();
				menu.getItems().add(menuItem.getUI());
                mLogger.info(() -> "Menu created: " + actionControl.getDisplayName());

			} else if (child instanceof MenuControl) {
				MenuControl menuControl = (MenuControl) child;
                mLogger.info(() -> "Menu created: " + menuControl.getDisplayName());
				MenuView subMenu = (MenuView) menuControl.createMenuView();
				menu.getItems().add(subMenu.getUI());
			}
		}

		return menu;
	}

	private void createView() {
		mUI = createSubMenu(mMenuControl);
	}

	public Menu getUI() {
		return mUI;
	}

	@Override
	public void redraw() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setEnabled(final boolean pEnabled) {
		Platform.runLater(()->mUI.setDisable(!pEnabled));
	}

	@Override
	public void setVisible(final boolean pVisible) {
		Platform.runLater(()->mUI.setVisible(pVisible));
	}

}
