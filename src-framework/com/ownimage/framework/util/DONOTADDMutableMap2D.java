/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.util.logging.Logger;


public class DONOTADDMutableMap2D<T> {

    public static Logger mLogger = Framework.getLogger();
    private static final int DENSITY = 1000;

    private final int mWidth;
    private final int mHeight;
    private Object[][] mData;


    public DONOTADDMutableMap2D(int pX, int pY, T pDefaultValue) {
        Framework.checkParameterGreaterThan(mLogger, pX, 0, "pX");
        Framework.checkParameterGreaterThan(mLogger, pY, 0, "pY");

        mWidth = pX;
        mHeight = pY;
        mData = new Object[pX][pY];
        new Range2D(pX, pY).forEach((x, y) -> mData[x][y] = pDefaultValue);
    }

    public T get(int pX, int pY) {
        checkXY(pX, pY);
        return (T) mData[pX][pY];
    }

    public DONOTADDMutableMap2D set(int pX, int pY, T pValue) {
        checkXY(pX, pY);
        mData[pX][pY] = pValue;
        return this;
    }

    public DONOTADDMutableMap2D getVersion() {
        return this;
    }

    private void checkXY(int pX, int pY) {
        Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
        Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
        Framework.checkParameterLessThan(mLogger, pX, mWidth, "pX");
        Framework.checkParameterLessThan(mLogger, pY, mHeight, "pY");
    }

}

