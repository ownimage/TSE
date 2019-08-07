/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;
import lombok.NonNull;

import java.awt.*;
import java.util.logging.Logger;

public class CircleMaskTransform extends BaseTransform {


    private final static Logger mLogger = Framework.getLogger();

    /**
     * The Diameter control is the outer radius i.e. it includes the blend zone. Everything inside that radius will get mapped into
     * a unit circle inside a unit square.
     */
    private final DoubleControl mDiameterControl;
    /**
     * The Blend is the radius of the circle that is faded transparent. 0 means no blend, 1.0 means all blended.
     */
    private final DoubleControl mBlendControl;

    private final ColorControl mColor;

    private double mRadius;
    private double mBlend;

    public CircleMaskTransform(final Perception pPerception) {
        super("Circle Mask", "circleMask");

        mDiameterControl = new DoubleControl("Diameter", "diameter", getContainer(), 0.3d, 0.0d, 1.0d);
        mBlendControl = new DoubleControl("Blend", "blend", getContainer(), 0.45d, 0.0d, 1.0d);
        mColor = new ColorControl("Color", "color", getContainer(), Color.WHITE);

        setValues();

        addXYControlPair(mDiameterControl, mBlendControl);
        addXControl(mDiameterControl);
        addYControl(mBlendControl);

    }

    @Override
    public void graffiti(final GrafittiHelper pGrafittiHelper) {
        pGrafittiHelper.drawCircle(0.5d, 0.5d, mRadius, getGrafitiColor1(), isControlSelected(mDiameterControl));
        pGrafittiHelper.drawCircle(0.5d, 0.5d, mBlend, getGrafitiColor2(), isControlSelected(mBlendControl));
    }

    @Override
    public void setValues() {
        mRadius = mDiameterControl.getValue() / 2.0d;
        mBlend = mRadius * mBlendControl.getValue();
    }

    @Override
    public void transform(@NonNull final ITransformResult pRenderResult) {
        Framework.logEntry(mLogger);

        final Point delta = pRenderResult.getPoint().minus(Point.Point0505);
        final double radius = delta.length();

        if (radius > mRadius) {
            pRenderResult.setColor(mColor.getValue());

        } else if (radius > mBlend) {
            double d = (radius - mBlend) / (mRadius - mBlend);
            d = 1.0d - d;
            final float a = (float) KMath.sigma(d);
            final Color c = mColor.getValue();
            final float r = c.getRed() / 256.0f;
            final float g = c.getGreen() / 256.0f;
            final float b = c.getBlue() / 256.0f;
            final Color cnew = new Color(r, g, b, a);
            pRenderResult.setColor(cnew);
        }
        Framework.logExit(mLogger);
    }
}
