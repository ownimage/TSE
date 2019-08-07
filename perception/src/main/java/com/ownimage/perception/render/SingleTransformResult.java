/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import lombok.NonNull;

import java.awt.*;
import java.util.logging.Logger;

public class SingleTransformResult implements ITransformResult {


    public final static Logger mLogger = Framework.getLogger();

    private double mX;
    private double mY;

    private float mR;
    private float mG;
    private float mB;
    private float mA;

    public SingleTransformResult(final double pX, final double pY) {
        mX = pX;
        mY = pY;
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
    public void setColor(@NonNull final Color pColor) {
        final float[] c = pColor.getRGBComponents(null);
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
    }

    @Override
    public void setX(final double pX) {
        mX = pX;
    }

    @Override
    public void setXY(final double pX, final double pY) {
        setX(pX);
        setY(pY);
    }

    @Override
    public void setY(final double pY) {
        mY = pY;
    }
}
