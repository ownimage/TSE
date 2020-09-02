/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.ObjectMetaType;
import com.ownimage.framework.control.type.ObjectType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.logging.Logger;

public class ObjectControl<R> extends ControlBase<ObjectControl<R>, ObjectType<R>, ObjectMetaType<R>, R, IView> {

    public static Logger mLogger = Framework.getLogger();
    public static long serialVersionUID = 1L;

    public ObjectControl(String pDisplayName, String pPropertyName, IContainer pContainer, ObjectType<R> pValue) {
        super(pDisplayName, pPropertyName, pContainer, pValue);
    }

    public ObjectControl(String pDisplayName, String pPropertyName, IContainer pContainer, R pValue, Collection<R> pListOfValues) {
        super(pDisplayName, pPropertyName, pContainer, new ObjectType<>(pValue, new ObjectMetaType<>(pListOfValues)));
    }

    public ObjectControl(String pDisplayName, String pPropertyName, IContainer pContainer, R pValue, R[] pListOfValues) {
        super(pDisplayName, pPropertyName, pContainer, new ObjectType<>(pValue, new ObjectMetaType<>(Arrays.asList(pListOfValues))));
    }

    public ObjectControl(String pDisplayName, String pPropertyName, IContainer pContainer, R pValue, R[] pListOfValues, Function<R, String> toString) {
        super(pDisplayName, pPropertyName, pContainer, new ObjectType<>(pValue, new ObjectMetaType<>(Arrays.asList(pListOfValues)) {
            @Override
            public String getString(Object object) {
                return toString.apply((R) object);
            }
        }));
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
    @SuppressWarnings("unchecked")
    public boolean setValue(@NonNull Object pObject) {
        R rObject = (R) pObject;
        boolean b = super.setValue(rObject);
        return b;
    }

    // this is required as the UI can not support the Generic type
    @Override
    @SuppressWarnings("unchecked")
    public boolean setValue(Object pObject, IView pView, boolean pIsMutating) {
        R rObject = (R) pObject;
        boolean b = super.setValue(rObject, pView, pIsMutating);
        return b;
    }

    @Override
    public void write(@NotNull IPersistDB pDB, @NotNull String pId) {
        if (isPersistent()) {
            pDB.write(getPrefix(pId) + getPropertyName(), getMetaType().getString(getValue()));
        }
    }

}
