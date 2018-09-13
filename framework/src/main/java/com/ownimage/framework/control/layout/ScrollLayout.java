/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.layout;

import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class ScrollLayout implements IViewable<IView> {

    public enum Policy {
        ALWAYS, NEVER, AS_NEEDED
    }

    private final IViewable<?> mContent;
    private final Policy mHorizPolicy;

    private final Policy mVertPolicy;

    public ScrollLayout(final IViewable<?> pContent, final Policy pHorizPolicy, final Policy pVertPolicy) {
        mContent = pContent;
        mHorizPolicy = pHorizPolicy;
        mVertPolicy = pVertPolicy;
    }

    public ScrollLayout(final PictureControl pPreviewControl) {
        this(pPreviewControl, Policy.AS_NEEDED, Policy.AS_NEEDED);
    }

    @Override
    public IView createView() {
        final IView view = ViewFactory.getInstance().createView(this);
        return view;
    }

    public IViewable<?> getContent() {
        return mContent;
    }

    @Override
    public String getDisplayName() {
        return "";
    }

    public Policy getHorizPolicy() {
        return mHorizPolicy;
    }

    public Policy getVertPolicy() {
        return mVertPolicy;
    }
}
