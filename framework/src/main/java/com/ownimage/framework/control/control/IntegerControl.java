/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.control.type.IntegerType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.util.logging.Logger;

public class IntegerControl extends ControlBase<IntegerControl, IntegerType, IntegerMetaType, Integer, IView> {

    public class IntegerProperty {
        public int getValue() {
            return IntegerControl.this.getValue();
        }
    }


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    public IntegerControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final int pValue) {
        super(pDisplayName, pPropertyName, pContainer, new IntegerType(pValue));
    }

    public IntegerControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final int pValue, final int pMin, final int pMax, final int pStep) {
        super(pDisplayName, pPropertyName, pContainer, new IntegerType(pValue, new IntegerMetaType(pMin, pMax, pStep)));
    }

    public IntegerControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final int pValue, final IntegerMetaType pMetaType) {
        super(pDisplayName, pPropertyName, pContainer, new IntegerType(pValue, pMetaType));
    }

    public IntegerControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final IntegerType pValue) {
        super(pDisplayName, pPropertyName, pContainer, pValue.clone());
    }

    @Override
    public IntegerControl clone(final IContainer pContainer) {
        if (pContainer == null) {
            throw new IllegalArgumentException("pContainer MUST not be null.");
        }

        return new IntegerControl(getDisplayName(), getPropertyName(), pContainer, getValue(), getMetaType());
    }

    @Override
    public IView createView() {
        IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    @Override
    public double getNormalizedValue() {
        return mValue.getNormalizedValue();
    }

    public IntegerProperty getProperty() {
        return new IntegerProperty();
    }

    @Override
    public boolean setNormalizedValue(final double pNormalizedValue) {
        IntegerMetaType metaType = getMetaType();

        if (metaType == null) {
            throw new IllegalStateException("Cannot setNormalizedValue for an IntegerControl that does not have an IntegerMetaType.");
        }

        Integer value = metaType.getValueForNormalizedValue(pNormalizedValue);
        setValue(value);
        return true;
    }

}
