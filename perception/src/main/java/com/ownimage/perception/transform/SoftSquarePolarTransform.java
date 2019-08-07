/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.PolarCoordinates;
import com.ownimage.framework.math.RTheta;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;
import lombok.NonNull;

import java.util.logging.Logger;

public class SoftSquarePolarTransform extends BaseTransform {


    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

    private final DoubleControl mRotateControl;

    double mRotate;
    private final DoubleControl mPowerControl;

    private double mPower;

    public SoftSquarePolarTransform(final Perception pPerception) {
        super("Soft Square Polar", "softSquarePolar");

        mRotateControl = new DoubleControl("Rotate", "rotate", getContainer(), 0.5d, 0.0d, 1.0d);
        mPowerControl = new DoubleControl("Power", "power", getContainer(), 1.0d, 1.0d, 3.0d);

        setValues();
        addXYControlPair(mRotateControl, mPowerControl);
    }

    @Override
    public void graffiti(final GrafittiHelper pGrafittiHelper) {
        pGrafittiHelper.drawHorizontalLine((mPower - 1.0d) / 2.0d, getGrafitiColor3(), isControlSelected(mPowerControl));
        pGrafittiHelper.drawString(mPowerControl, new Point(0.0d, (mPower - 1.0d) / 2.0d));
        pGrafittiHelper.drawVerticalLine(mod1(mRotate), getGrafitiColor1());
        pGrafittiHelper.drawString(mRotateControl, new Point(mRotate, 0.0d));
    }

    public Point map(final Point pPoint) {

        final double piOver2 = Math.PI / 2.0d;

        final Point sourceABPoint = new Point(pPoint.getX(), 1.0d);
        final RTheta targetABPoint = new RTheta(1.0d, pPoint.getX() * piOver2);

        final Point sourceBCPoint = new Point(1.0d, pPoint.getY());
        final RTheta targetBCPoint = new RTheta(1.0d, piOver2 + (1.0d - pPoint.getY()) * piOver2);

        final Point sourceCDPoint = new Point(pPoint.getX(), 0.0d);
        final RTheta targetCDPoint = new RTheta(1.0d, 2 * piOver2 + (1.0d - pPoint.getX()) * piOver2);

        final Point sourceDAPoint = new Point(0.0d, pPoint.getY());
        final RTheta targetDAPoint = new RTheta(1.0d, 3 * piOver2 + pPoint.getY() * piOver2);

        final Point sourceCenterPoint = Point.Point0505;

        final double reciprocalAB = 1.0d / Math.pow(sourceABPoint.minus(pPoint).length(), mPower);
        final double reciprocalBC = 1.0d / Math.pow(sourceBCPoint.minus(pPoint).length(), mPower);
        final double reciprocalCD = 1.0d / Math.pow(sourceCDPoint.minus(pPoint).length(), mPower);
        final double reciprocalDA = 1.0d / Math.pow(sourceDAPoint.minus(pPoint).length(), mPower);
        final double reciprocalCenter = 1.0d / Math.pow(pPoint.minus(sourceCenterPoint).length(), mPower);

        final double sumOfReciprocals = reciprocalCenter + reciprocalAB + reciprocalBC + reciprocalCD + reciprocalDA;

        final PolarCoordinates pcOrigin = new PolarCoordinates();
        Point target = pcOrigin.getCartesian(targetABPoint).multiply(reciprocalAB);
        target = target.add(pcOrigin.getCartesian(targetBCPoint).multiply(reciprocalBC));
        target = target.add(pcOrigin.getCartesian(targetCDPoint).multiply(reciprocalCD));
        target = target.add(pcOrigin.getCartesian(targetDAPoint).multiply(reciprocalDA));

        final double targetX = mod1(pcOrigin.getPolarCoordinate(target).getTheta() / (2 * Math.PI));

        Point pOut = Point.Point00;
        pOut = pOut.add(new Point(targetX + mRotate - 1.0d / 8.0d, (reciprocalAB + reciprocalBC + reciprocalCD + reciprocalDA) / sumOfReciprocals));
        pOut = new Point(mod1(pOut.getX()), mod1(pOut.getY()));
        return pOut;

    }

    @Override
    public void setValues() {
        super.setValues();
        mPower = mPowerControl.getValue();
        mRotate = mRotateControl.getValue();
    }

    @Override
    public void transform(@NonNull final ITransformResult pRenderResult) {
        Framework.logEntry(mLogger);

        pRenderResult.setPoint(map(pRenderResult.getPoint()));

        Framework.logExit(mLogger);
    }

}
