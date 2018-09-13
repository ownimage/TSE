/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.layout;

import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class HFlowLayout extends ViewableBase<IViewable, IView> {

    Vector<IViewable<?>> mViewables = new Vector<IViewable<?>>();

    public HFlowLayout(final IViewable<?>... pViewables) {
        mViewables.addAll(Arrays.asList(pViewables));
    }

    @Override
    public IView createView() {
        final IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    public Iterator<IViewable<?>> getViewableChildrenIterator() {
        return mViewables.iterator();
    }

}
