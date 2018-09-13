/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.BooleanType;
import com.ownimage.framework.control.type.IMetaType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.util.logging.Logger;

public class BooleanControl extends ControlBase<BooleanControl, BooleanType, IMetaType<Boolean>, Boolean, IView> {

    public class BooleanProperty {
        public boolean getValue() {
            return BooleanControl.this.getValue();
        }
    }


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    public BooleanControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final boolean pValue) {

        super(pDisplayName, pPropertyName, pContainer, new BooleanType(pValue));
    }

    @Override
    public IView createView() { // TODO can we push this into the base class
        final IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    public BooleanProperty getProperty() {
        return new BooleanProperty();
    }

}
