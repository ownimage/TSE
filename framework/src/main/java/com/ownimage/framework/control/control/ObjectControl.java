/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.ObjectMetaType;
import com.ownimage.framework.control.type.ObjectType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

public class ObjectControl<R> extends ControlBase<ObjectControl<R>, ObjectType<R>, ObjectMetaType<R>, R, IView> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public ObjectControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final ObjectType<R> pValue) {
        super(pDisplayName, pPropertyName, pContainer, pValue);
    }

    public ObjectControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final R pValue, final Collection<R> pListOfValues) {
        super(pDisplayName, pPropertyName, pContainer, new ObjectType<>(pValue, new ObjectMetaType<>(pListOfValues)));
    }

    public ObjectControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final R pValue, final R[] pListOfValues) {
        super(pDisplayName, pPropertyName, pContainer, new ObjectType<>(pValue, new ObjectMetaType<>(Arrays.asList(pListOfValues))));
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public IView createView() { // TODO can we push this into the base class
        IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    // this is required as the UI can not support the Generic type
    @Override
    public boolean setValue(final Object pObject) {
        Framework.checkParameterNotNull(mLogger, pObject, "pObject");

        R rObject = (R) pObject;
        boolean b = super.setValue(rObject);
        return b;
    }

    // this is required as the UI can not support the Generic type
    @Override
    public boolean setValue(final Object pObject, final IView pView, final boolean pIsMutating) {
        R rObject = (R) pObject;
        boolean b = super.setValue(rObject, pView, pIsMutating);
        return b;
    }
}
