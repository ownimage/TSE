/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;
import lombok.NonNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SquarePolarTransform extends BaseTransform {


    private final static Logger mLogger = Framework.getLogger();

    private final DoubleControl mRotateSlider;

    private double mRotate;

    public SquarePolarTransform(final Perception pPerception) {
        super("Square Polar", "squarePolar");

        mRotateSlider = new DoubleControl("Rotate", "rotate", getContainer(), 180.0d, 0.0d, 359.0d);

        setValues();

        addXControl(mRotateSlider);
    }

    @Override
    public void graffiti(final GrafittiHelper pGrafittiHelper) {
        mLogger.info(() -> "mRotateSlider: " + mRotateSlider.getNormalizedValue());
        pGrafittiHelper.drawVerticalLine(mRotateSlider.getNormalizedValue(), getGrafitiColor1(), isControlSelected(mRotateSlider));
    }

    private double maxR(final double pTheta) {
        final double rv = 0.5 / Math.max(Math.abs(Math.sin(pTheta)), Math.abs(Math.cos(pTheta)));
        return rv;
    }

    @Override
    public void setValues() {
        mRotate = mRotateSlider.getValue();
    }

    @Override
    public void transform(@NonNull final ITransformResult pRenderResult) {
        Framework.logEntry(mLogger);

        double fx = 0;
        double fy = 0;

        try {
            final double dx = pRenderResult.getX() - 0.5;
            final double dy = pRenderResult.getY() - 0.5;

            double theta = Math.atan(dx / dy);
            if (dy < 0) {
                theta = theta + Math.PI;
            }
            if (theta > 2 * Math.PI) {
                theta -= 2.0 * Math.PI;
            }

            fx = mod1((theta + Math.toRadians(mRotate)) / (2.0 * Math.PI));
            fy = Math.sqrt(dx * dx + dy * dy) / maxR(theta);

            if (fy >= 1.0) {
                pRenderResult.setColor(getProperties().getColorOOB());

            } else {
                final Point point = new Point(fx, fy);
                pRenderResult.setPoint(point);
            }
        } catch (final Exception pEx) {
            if (mLogger.isLoggable(Level.FINEST)) {
                mLogger.log(Level.FINEST, "unknown error", pEx);
                ;
            }
        }

        Framework.logExit(mLogger);
    }

}
