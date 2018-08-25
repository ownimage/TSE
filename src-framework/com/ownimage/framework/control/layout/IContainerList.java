/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.layout;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.view.ISingleSelectView;

public interface IContainerList extends IViewable<ISingleSelectView> {

    public IContainer getContainer(final int pTab);

    public int getCount();

    public int getSelectedIndex();

    default IContainer getSelectedContainer() {
        return getContainer(getSelectedIndex());
    }

    public void setSelectedIndex(IContainer pContainer);

    public void setSelectedIndex(final int pSelectedIndex);

    default void setSelectedIndex(final String pSelectedIndex) {
        try {
            int index = Integer.parseInt(pSelectedIndex);
            setSelectedIndex(index);
        } catch (RuntimeException pRE) {
        }
    }

    public void setSelectedIndex(final int pSelectedIndex, final ISingleSelectView pView);

}
