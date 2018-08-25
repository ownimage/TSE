/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import java.awt.*;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Point;

public class TransformResult implements ITransformResult {


    public final static Logger mLogger = Framework.getLogger();

    private final TransformResultBatch mTransformResultBatch;
    private final int mIndex;

    private final int mXPixel;
    private final int mYPixel;

    private double mX;
    private double mY;

    private float mR;
    private float mG;
    private float mB;
    private float mA;

    public TransformResult(final TransformResultBatch pTRB, final int pIndex, final int pXPixel, final int pYPixel, final double pX, final double pY, final float pR, final float pG, final float pB,
                           final float pA) {
        Framework.checkParameterNotNull(mLogger, pTRB, "pTRB");
        Framework.checkParameterGreaterThanEqual(mLogger, pIndex, 0, "pIndex");
        Framework.checkParameterLessThan(mLogger, pIndex, pTRB.getBatchSize(), "pIndex");

        mTransformResultBatch = pTRB;
        mIndex = pIndex;

        mXPixel = pXPixel;
        mYPixel = pYPixel;
        mX = pX;
        mY = pY;
        mR = pR;
        mG = pG;
        mB = pB;
        mA = pA;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.renderEngine.IRenderResult#getA()
     */
    @Override
    public float getA() {
        return mA;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.renderEngine.IRenderResult#getB()
     */
    @Override
    public float getB() {
        return mB;
    }

    @Override
    public Color getColor() {
        return new Color(mR, mG, mB, mA);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.renderEngine.IRenderResult#getG()
     */
    @Override
    public float getG() {
        return mG;
    }

    @Override
    public Point getPoint() {
        return new Point(mX, mY);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.renderEngine.IRenderResult#getR()
     */
    @Override
    public float getR() {
        return mR;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.renderEngine.IRenderResult#getX()
     */
    @Override
    public double getX() {
        return mX;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.renderEngine.IRenderResult#getY()
     */
    @Override
    public double getY() {
        return mY;
    }

    @Override
    public void setColor(final Color pColor) {
        Framework.checkParameterNotNull(mLogger, pColor, "pColor");
        float[] c = pColor.getRGBComponents(null);
        setRGBA(c[0], c[1], c[2], c[3]);
    }

    @Override
    public void setPoint(final Point pPoint) {
        setXY(pPoint.getX(), pPoint.getY());
    }

    @Override
    public void setRGBA(final float pR, final float pG, final float pB, final float pA) {
        // TODO there is some speed improvements that can be made here by taking out out (1.0f - mA)*pA
        mR = mA * mR + (1.0f - mA) * pA * pR;
        mG = mA * mG + (1.0f - mA) * pA * pG;
        mB = mA * mB + (1.0f - mA) * pA * pB;
        mA = mA + (1.0f - mA) * pA;

        mTransformResultBatch.getR()[mIndex] = mR;
        mTransformResultBatch.getG()[mIndex] = mG;
        mTransformResultBatch.getB()[mIndex] = mB;
        mTransformResultBatch.getA()[mIndex] = mA;
    }

    @Override
    public void setX(final double pX) {
        mX = pX;
        mTransformResultBatch.getX()[mIndex] = pX;
    }

    @Override
    public void setXY(final double pX, final double pY) {
        setX(pX);
        setY(pY);
    }

    @Override
    public void setY(final double pY) {
        mY = pY;
        mTransformResultBatch.getY()[mIndex] = pY;
    }
}
