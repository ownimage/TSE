/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.control.type.DoubleType;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class DoubleControl extends ControlBase<DoubleControl, DoubleType, DoubleMetaType, Double, IView> {

    public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
    public final static String mClassname = ControlBase.class.getName();
    public final static Logger mLogger = Logger.getLogger(mClassname);
    public final static long serialVersionUID = 1L;

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final double pValue) {
        super(pDisplayName, pPropertyName, pContainer, new DoubleType(pValue));
    }

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final double pValue, final double pMin, final double pMax) {
        this(pDisplayName, pPropertyName, pContainer, pValue, new DoubleMetaType(pMin, pMax));
    }

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final double pValue, final DoubleMetaType pMetaType) {
        super(pDisplayName, pPropertyName, pContainer, new DoubleType(pValue, pMetaType));
    }

    public DoubleControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final DoubleType pValue) {
        super(pDisplayName, pPropertyName, pContainer, pValue.clone());
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
        IView view = ViewFactory.getInstance().createView(this);
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

}
