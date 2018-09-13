/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.app.menu;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.util.Iterator;
import java.util.Vector;

public class MenuControl implements IMenuItem, IViewable {

    private String mDisplayName;
    private final Vector<IMenuItem> mMenuItems = new Vector<>(10);

    private IView mView;

    /**
     * Instantiates a new MenuBar. This is a top level Menu that can only have Menus as children and is used as the main menu for an
     * application.
     */
    private MenuControl() {
        mDisplayName = "";
    }

    public static class Builder {

        private MenuControl mMenuControl;

        public Builder() {
            mMenuControl = new MenuControl();
        }

        public Builder(MenuControl pMenuControl) {
            mMenuControl = new MenuControl();
            mMenuControl.mDisplayName = pMenuControl.mDisplayName;
            mMenuControl.mMenuItems.addAll(pMenuControl.mMenuItems);
        }

        public Builder setDisplayName(final String pDisplayName) {
            if (pDisplayName == null || pDisplayName.length() == 0) {
                throw new IllegalArgumentException("pDisplayName must not be null or zero length.");
            }
            mMenuControl.mDisplayName = pDisplayName;
            return this;
        }

        public Builder addAction(final ActionControl pAction) {
            if (pAction == null) {
                throw new IllegalArgumentException("pAction must not be null");
            }

            mMenuControl.mMenuItems.add(pAction);
            return this;
        }

        public Builder addMenu(final MenuControl pMenu) {
            if (pMenu == null) {
                throw new IllegalArgumentException("pMenu must not be null");
            }

            if (pMenu.isMenuBar()) {
                throw new IllegalArgumentException("Cannot add a Menu which is a MenuBar to a Menu.");
            }

            mMenuControl.mMenuItems.add(pMenu);
            return this;
        }

        public MenuControl build() {
            return new Builder(mMenuControl).mMenuControl;
        }
    }

    public IView createMenuView() {
        mView = ViewFactory.getInstance().createMenuView(this);
        return mView;
    }

    @Override
    public IView createView() {
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


    public boolean isMenuBar() {
        return "".equals(mDisplayName);
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
