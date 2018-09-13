/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.awt.*;
import java.util.List;

public class KColor {


    private Color mColor;

    public KColor(final Color pColor) {
        this(pColor, false);
    }

    // the imutable flag meanst hat this could will not be affected by changes to the original color
    public KColor(final Color pColor, final boolean pImmutable) {
        if (pImmutable) {
            mColor = new Color(pColor.getRed(), pColor.getGreen(), pColor.getBlue());
        } else {
            mColor = pColor;
        }
    }

    public KColor(final int pR, final int pG, final int pB) {
        mColor = new Color(pR, pG, pB);
    }

    public static Color average(final Color... pColors) {
        Framework.checkParameterGreaterThanEqual(Framework.mLogger, 0, pColors.length, "pColors.length");

        int red = 0;
        int blue = 0;
        int green = 0;
        for (final Color c : pColors) {
            red += c.getRed();
            green += c.getGreen();
            blue += c.getBlue();
        }

        return new Color(red / pColors.length, green / pColors.length, blue / pColors.length);
    }

    public static Color average(final List<Color> pColors) {
        final Color[] colors = pColors.toArray(new Color[0]);
        return average(colors);
    }

    /**
     * Returns a value in the range of 0 .. 255 depending on pValue. If pValue <= 0 returns 0, if pValue >= 255 returns 255,
     * otherwise returns pValue
     *
     * @param pValue the value
     * @return the bounded value
     */
    private static int bound(final int pValue) {
        if (pValue <= 0) {
            return 0;
        }
        if (pValue >= 255) {
            return 255;
        }
        return pValue;
    }

    public static int brighter(final int pC, final double pAmount) {
        int delta = 255 - pC;
        delta = (int) (255 - delta * (1 - pAmount));
        return delta;
    }

    public static int brighter(final int pColor1, final int pColor2, final double pAmount) {
        final int delta = pColor2 - pColor1;
        return (int) (pColor1 + delta * pAmount);
    }

    /**
     * Creates a blend of the two colors provided.
     * Color1 layers over the top with an opacity,
     * then Color2 is underneath with it's own opacity in the color, but also at a strength given by Amount.
     * The result is the resultant Color with the appropriate opacity.
     *
     * @param pColor1 topColor including opacity
     * @param pColor2 bottomColor including opacity
     * @param pAmount amount of blend
     * @return new Color
     */
    public static Color fade(final Color pColor1, final Color pColor2, final double pAmount) {
        float amount = (float) pAmount;
        final float[] c1 = pColor1.getComponents(new float[4]);
        final float[] c2 = pColor2.getComponents(new float[4]);
        final float red = relativeColor(c1[0], c1[3], c2[0], c2[3] * amount);
        final float green = relativeColor(c1[1], c1[3], c2[1], c2[3] * amount);
        final float blue = relativeColor(c1[2], c1[3], c2[2], c2[3] * amount);
        final float alpha = c1[3] + (1.0f - c1[3]) * c2[3] * amount;
        return new Color(red, green, blue, alpha);
    }

    private static float relativeColor(final float c1, final float a1, final float c2, final float a2) {
        float omaa = (1.0f - a1) * a2;
        return (c1 * a1 + c2 * omaa);
    }

    public static Color fade(final Color pColor, final double pAmount) {
        final int red = brighter(pColor.getRed(), pAmount);
        final int green = brighter(pColor.getGreen(), pAmount);
        final int blue = brighter(pColor.getBlue(), pAmount);
        return new Color(red, green, blue);
    }

    public static Color invert(final Color pColor) {
        return new Color(255 - pColor.getRed(), 255 - pColor.getGreen(), 255 - pColor.getBlue());
    }

    public static double luminance(final Color pColor) {
        return 0.2126d * pColor.getRed() + 0.7152d * pColor.getGreen() + 0.0722d * pColor.getBlue();
    }

    public int getBlue() {
        return mColor.getBlue();
    }

    public int getGreen() {
        return mColor.getGreen();
    }

    public int getRed() {
        return mColor.getRed();
    }

    public double luminance() {
        return 0.2126d * getRed() + 0.7152d * getGreen() + 0.0722d * getBlue();
    }

    public static String toHex(final Color pC) {
        return String.format("#%02x%02x%02x", pC.getRed(), pC.getGreen(), pC.getBlue());
    }
}
