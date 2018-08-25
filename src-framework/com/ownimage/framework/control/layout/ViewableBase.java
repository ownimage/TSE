/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.control.layout;

import java.util.function.Consumer;

import com.ownimage.framework.control.event.EventDispatcher;
import com.ownimage.framework.view.IView;

public abstract class ViewableBase<Viewable, View extends IView> implements IViewable<View> {

    private final EventDispatcher<View> mViews = new EventDispatcher<View>(this);
    private boolean mEnabled = true;
    private boolean mVisible = true;

    protected void addView(final View pView) {
        mViews.addListener(pView);
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    public void invokeOnAllViews(final Consumer<View> pFunction) {
        mViews.invokeAll(pFunction);
    }

    public void invokeOnAllViewsExcept(final View pView, final Consumer<View> pFunction) {
        mViews.invokeAllExcept(pView, pFunction);
    }

    public Viewable redraw() {
        mViews.invokeAll(ui -> ui.redraw());
        return (Viewable) this;
    }

    public Viewable setEnabled(final boolean pEnabled) {
        mEnabled = pEnabled;
        mViews.invokeAll(ui -> ui.setEnabled(pEnabled));
        return (Viewable) this;
    }

    public Viewable setVisible(final boolean pVisible) {
        mVisible = pVisible;
        mViews.invokeAll(ui -> ui.setVisible(pVisible));
        return (Viewable) this;
    }

}
