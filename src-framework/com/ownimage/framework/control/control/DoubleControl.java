/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.DoubleType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IDoubleView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class DoubleControl extends ControlBase<DoubleControl, DoubleType, DoubleMetaType, Double, IDoubleView> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private DoubleMetaType.DisplayType mDisplayType;

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final double pValue) {
        this(pDisplayName, pPropertyName, pContainer, new DoubleType(pValue));
    }

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final double pValue, final double pMin, final double pMax) {
        this(pDisplayName, pPropertyName, pContainer, pValue, new DoubleMetaType(pMin, pMax));
    }

    public DoubleMetaType.DisplayType getDisplayType() {
        return mDisplayType;
    }

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final double pValue, final DoubleMetaType pMetaType) {
        this(pDisplayName, pPropertyName, pContainer, new DoubleType(pValue, pMetaType));
    }

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final DoubleType pValue) {
        super(pDisplayName, pPropertyName, pContainer, pValue.clone());
        mDisplayType = pValue.getMetaModel().getDisplayType();
    }

    @Override
    public DoubleControl clone(final IContainer pContainer) {
        if (pContainer == null) {
            throw new IllegalArgumentException("pContainer MUST not be null.");
        }

        return new DoubleControl(getDisplayName(), getPropertyName(), pContainer, getValue(), getMetaType());
    }

    @Override
    public IView createView() {
        IDoubleView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    @Override
    public double getNormalizedValue() {
        return mValue.getNormalizedValue();
    }

    @Override
    public boolean setNormalizedValue(final double pNormalizedValue) {
        DoubleMetaType metaType = getMetaType();

        if (metaType == null) {
            throw new IllegalStateException("Cannot setNormalizedValue for an IntegerControl that does not have an IntegerMetaType.");
        }

        Double value = metaType.getValueForNormalizedValue(pNormalizedValue);
        setValue(value);
        return true;
    }

    @Override
    public String toString() {
        return String.format("DoubleControl:(value=%s, min=%s, max=%s)", getValue(), getMetaType().getMin(), getMetaType().getMax());
    }

     public void setDisplayType(final DoubleMetaType.DisplayType pDisplayType, final IDoubleView pFromView) {
        mDisplayType = pDisplayType;
        mViews.invokeAllExcept(pFromView, v -> v.setDisplayType(pDisplayType));
     }

    @Override
    public void read(final IPersistDB pDB, final String pId) {
        super.read(pDB, pId);
        String value = null;
        try {
            if (isPersistent()) {
                value = pDB.read(getPrefix(pId) + getPropertyName() + ".displayType");
                if (value != null) {
                    mDisplayType = DoubleMetaType.DisplayType.valueOf(value);
                }
            }
        } catch (RuntimeException pRE) {
            mLogger.severe(String.format("ERROR in getting displayType for %s, value read is %s", pId, value));
        }
    }

    @Override
    public void write(final IPersistDB pDB, final String pId) {
        super.write(pDB, pId);
        if (isPersistent()) {
            pDB.write(getPrefix(pId) + getPropertyName() + ".displayType", mDisplayType.name());
        }
    }
}
