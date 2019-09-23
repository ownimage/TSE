package com.ownimage.framework.app.menu;

import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;
import lombok.NonNull;

public class MenuAction implements IMenuItem {

    private String mDisplayName;
    private IAction mAction;

    public MenuAction(@NonNull final String pDisplayName, @NonNull final IAction pAction) {
        mDisplayName = pDisplayName;
        mAction = pAction;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    public IAction getAction() {
        return mAction;
    }

    public IView createMenuItemView() { // TODO can we push this into the base class
        return ViewFactory.getInstance().createMenuItemView(this);
    }

}
