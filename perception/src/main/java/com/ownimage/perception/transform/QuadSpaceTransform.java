/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;

public class QuadSpaceTransform extends BaseTransform {
    // This seems to be a very sharp change at the boundaries ... perhaps it should be smoothilicious, or at least have a
    // on-and-offable option


    private final static Logger mLogger = Framework.getLogger();
    double[] mDividers;

    private final DoubleControl mOneControl;

    private final DoubleControl mTwoControl;
    private final DoubleControl mThreeControl;
    private final DoubleControl mFourControl;

    public QuadSpaceTransform(final Perception pPerception) {
        super("Quad Space", "quadSpaceTransform");

        mOneControl = new DoubleControl("One", "one", getContainer(), 0.125);
        mTwoControl = new DoubleControl("Two", "two", getContainer(), 0.375);
        mThreeControl = new DoubleControl("Three", "three", getContainer(), 0.625);
        mFourControl = new DoubleControl("Four", "four", getContainer(), 0.875);

        setValues();

        addXControl(mOneControl);
        addXControl(mTwoControl);
        addXControl(mThreeControl);
        addXControl(mFourControl);
    }

    @Override
    public void grafitti(final GrafittiHelper pGrafittiHelper) {
        pGrafittiHelper.drawVerticalLineWithLabel(mOneControl, getGrafitiColor1(), isControlSelected(mOneControl));
        pGrafittiHelper.drawVerticalLineWithLabel(mTwoControl, getGrafitiColor1(), isControlSelected(mTwoControl));
        pGrafittiHelper.drawVerticalLineWithLabel(mThreeControl, getGrafitiColor1(), isControlSelected(mThreeControl));
        pGrafittiHelper.drawVerticalLineWithLabel(mFourControl, getGrafitiColor1(), isControlSelected(mFourControl));
    }

    @Override
    public void setValues() {
        final double[] t = {mOneControl.getValue(), mTwoControl.getValue(), mThreeControl.getValue(), mFourControl.getValue()};
        Arrays.sort(t);

        mDividers = new double[]{t[0], t[1], t[2], t[3], t[0] + 1};
    }

    @Override
    public void transform(final ITransformResult pRenderResult) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pRenderResult, "pRenderResult");

        final double x = pRenderResult.getX();

        try {
            final int segment;
            if (x < 0.25) {
                segment = 0;
            } else if (x < 0.5) {
                segment = 1;
            } else if (x < 0.75) {
                segment = 2;
            } else {
                segment = 3;
            }

            final double ratio = (x - 0.25 * segment) / 0.25;
            final double xFrom = mod1(mDividers[segment] + ratio * (mDividers[segment + 1] - mDividers[segment]));

            pRenderResult.setX(xFrom);

        } catch (final Exception pEx) {
            if (mLogger.isLoggable(Level.FINEST)) {
                mLogger.finest("Oops");
                mLogger.finest(FrameworkLogger.throwableToString(pEx));
            }
            pRenderResult.setColor(getOOBColor());
        }

        Framework.logExit(mLogger);
    }
}
