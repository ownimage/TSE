/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.ColorType;
import com.ownimage.framework.control.type.IMetaType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.awt.*;
import java.util.logging.Logger;

public class ColorControl extends ControlBase<ColorControl, ColorType, IMetaType<Color>, Color, IView> {

    public class ColorProperty {
        public Color getValue() {
            return ColorControl.this.getValue();
        }
    }


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    public ColorControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final Color pValue) {
        super(pDisplayName, pPropertyName, pContainer, new ColorType(pValue));
    }

    @Override
    public ColorControl clone(final IContainer pContainer) {
        if (pContainer == null) {
            throw new IllegalArgumentException("pContainer MUST not be null.");
        }

        return new ColorControl(getDisplayName(), getPropertyName(), pContainer, getValue());
    }

    @Override
    public IView createView() {
        final IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    public ColorProperty getProperty() {
        return new ColorProperty();
    }

}
