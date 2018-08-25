/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.control.layout;

import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.control.event.EventDispatcher;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.factory.ViewFactory;

public class ContainerList implements IContainerList {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final String mPropertyName;
    private final String mDisplayName;

    private int mSelectedIndex = 0;
    private final Vector<IContainer> mContainers = new Vector<>();
    EventDispatcher<ISingleSelectView> mViews = new EventDispatcher<>(this);

    public ContainerList(final String pDisplayName, final String pPropertyName) {
        ControlBase.validate(pDisplayName, pPropertyName);
        mDisplayName = pDisplayName;
        mPropertyName = pPropertyName;
    }

    public IContainer add(final IContainer pContainer) {
        mContainers.add(pContainer);
        return pContainer;
    }

    @Override
    public ISingleSelectView createView() {
        ISingleSelectView view = ViewFactory.getInstance().createView(this);
        mViews.addListener(view);
        return view;
    }

    @Override
    public IContainer getContainer(final int pTab) {
        return mContainers.get(pTab);
    }

    @Override
    public int getCount() {
        return mContainers.size();
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    @Override
    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    private void selectedIndexCheck(final int pSelectedIndex) {
        if (pSelectedIndex < -1 || pSelectedIndex > mContainers.size()) {
            throw new IllegalArgumentException(
                    String.format("pSelectedIndex must be -1 or greater and less than mContainers.size.  pSelectedIndex = %s. mContaers.size = %s.", pSelectedIndex, mContainers.size()));
        }
    }

    @Override
    public void setSelectedIndex(IContainer pContainer) {
        int index = mContainers.indexOf(pContainer);
        if (index >= 0) {
            setSelectedIndex(index);
            return;
        }
        throw new IllegalStateException("ContainerList does not container specified container.");
    }

    @Override
    public void setSelectedIndex(final int pSelectedIndex) {
        mLogger.fine(() -> String.format("setSelectedIndex(%s).", pSelectedIndex));
        selectedIndexCheck(pSelectedIndex);
        mSelectedIndex = pSelectedIndex;
        mViews.invokeAll((v) -> v.setSelectedIndex(mSelectedIndex));
    }

    @Override
    public void setSelectedIndex(final int pSelectedIndex, final ISingleSelectView pView) {
        mLogger.fine(() -> String.format("setSelectedIndex(%s, %s).", pSelectedIndex, pView));
        selectedIndexCheck(pSelectedIndex);
        mSelectedIndex = pSelectedIndex;
        mLogger.fine(() -> "SelectedIndex = " + mSelectedIndex);
        mViews.invokeAllExcept(pView, (v) -> v.setSelectedIndex(mSelectedIndex));
    }
}
