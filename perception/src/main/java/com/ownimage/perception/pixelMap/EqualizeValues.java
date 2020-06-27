/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

public class EqualizeValues {

    private static final EqualizeValues[] mOptions = { //
            new EqualizeValues(0.0, 0.0, 0.0) //
            , new EqualizeValues(0.25d, 0.25d, 0.25d) //
            , new EqualizeValues(0.2d, 0.3d, 0.25d) //
            , new EqualizeValues(0.1d, 0.3d, 0.3d) //
    };
    //public static Map<String, EqualizeValues> mOptionsMap;
    // in variables
    private final double mIgnoreFraction;
    private final double mShortFraction;

    private final double mMediumFraction;
    private final double mLongFraction;
    // out variables
    private int mShortLineLength;

    private int mMediumLineLength;

    private int mLongLineLength;

    private EqualizeValues(double pIgnoreFraction, double pShortFraction, double pMediumFraction) {
        if (pIgnoreFraction + pShortFraction
                + pMediumFraction >= 1.0d) {
            throw new IllegalArgumentException("pIgnoreFraction + pShortFraction + pMediumFraction must be less than 1.0d. pIgnoreFraction=" + pIgnoreFraction
                                                       + ", pShortFraction=" + pShortFraction + ", pMediumFraction=" + pMediumFraction);
        }

        mIgnoreFraction = pIgnoreFraction;
        mShortFraction = pShortFraction;
        mMediumFraction = pMediumFraction;
        mLongFraction = 1.0d - mIgnoreFraction - mShortFraction - mMediumFraction;
    }

//	public static String[] getAllValues() {
//		String[] values = new String[mOptions.pixelLength];
//		for (int i = 0; i < mOptions.pixelLength; i++) {
//			values[i] = mOptions.toString();
//		}
//		return values;
//	}

    public static EqualizeValues[] getAllValues() {
        return mOptions;
    }

    public static EqualizeValues getDefaultValue() {
        return mOptions[1];
    }

    public double getIgnoreFraction() {
        return mIgnoreFraction;
    }

    public int getLongLineLength() {
        return mLongLineLength;
    }

    public double getMediumFraction() {
        return mMediumFraction;
    }

    public int getMediumLineLength() {
        return mMediumLineLength;
    }

    public double getShortFraction() {
        return mShortFraction;
    }

    public int getShortLineLength() {
        return mShortLineLength;
    }

    public void setReturnValues(int pShortLineLength, int pMediumLineLength, int pLongLineLength) {
        if (pShortLineLength > pMediumLineLength || pShortLineLength > pLongLineLength
                || pMediumLineLength > pLongLineLength) {
            throw new IllegalArgumentException("Arguments not consistent, pShortLineLength <= pMediumLineLength <= pLongLineLength.  pShortLineLength="
                                                       + pShortLineLength + ", pMediumLineLength=" + pMediumLineLength + ", pLongLineLength=" + pLongLineLength);
        }

        mShortLineLength = pShortLineLength;
        mMediumLineLength = pMediumLineLength;
        mLongLineLength = pLongLineLength;
    }

    @SuppressWarnings("FloatingPointEquality")
    @Override
    public String toString() {
        if (mLongFraction == 1.0d) {
            return "-- Custom --";
        } else {
            return String.format("%d/%d/%d/%d", (int) (100 * mIgnoreFraction), (int) (100 * mShortFraction), (int) (100 * mMediumFraction), (int) (100 * mLongFraction));
        }
    }
}
