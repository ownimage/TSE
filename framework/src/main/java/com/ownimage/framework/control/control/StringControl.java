/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.type.StringMetaType;
import com.ownimage.framework.control.type.StringType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.io.File;
import java.util.logging.Logger;

public class StringControl extends ControlBase<StringControl, StringType, StringMetaType, String, IView> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public StringControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final String pValue) {
        super(pDisplayName, pPropertyName, pContainer, new StringType(pValue));
    }

    public StringControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final StringType pValue) {
        super(pDisplayName, pPropertyName, pContainer, pValue);
    }

    public static StringControl createTitle(final String pTitle) {
        return new StringControl("Title", "title", NullContainer.NullContainer,
                new StringType(pTitle, StringType.TITLE));
    }

    @Override
    public IView createView() { // TODO can we push this into the base class
        final IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    public File getFile() {
        return new File(getValue());
    }

}
