package com.ownimage.perception.util;

/* This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */

import java.awt.Color;
import java.util.List;

import com.ownimage.framework.util.Version;
import com.ownimage.perception.view.javafx.app.FXPerception;

public class KColor {

	public final static Version mVersion = new Version(4, 0, 1, "2014/05/30 20:48");

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
		if (pColors.length == 0) { return FXPerception.getAppControl().getProperties().getColorOOB(); }

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
	 * @param value
	 *            the value
	 * @return the bounded value
	 */
	private static int bound(final int pValue) {
		if (pValue <= 0) { return 0; }
		if (pValue >= 255) { return 255; }
		return pValue;
	}

	public static int brighter(final int pC, final double pAmount) {
		int delta = 255 - pC;
		delta = (int) (255 - delta * (1 - pAmount));
		return delta;
	}

	public static int brighter(final int pColor1, final int pColor2, final double pAmount) {
		final int delta = pColor2 - pColor1;
		final int newValue = (int) (pColor1 + delta * pAmount);
		return newValue;
	}

	public static Color fade(final Color pColor1, final Color pColor2, final double pAmount) {
		final int red = bound(brighter(pColor1.getRed(), pColor2.getRed(), pAmount));
		final int green = bound(brighter(pColor1.getGreen(), pColor2.getGreen(), pAmount));
		final int blue = bound(brighter(pColor1.getBlue(), pColor2.getBlue(), pAmount));
		final Color color = new Color(red, green, blue);
		return color;
	}

	public static Color fade(final Color pColor, final double pAmount) {
		final int red = brighter(pColor.getRed(), pAmount);
		final int green = brighter(pColor.getGreen(), pAmount);
		final int blue = brighter(pColor.getBlue(), pAmount);
		final Color color = new Color(red, green, blue);
		return color;
	}

	public static Color invert(final Color pColor) {
		final Color c = new Color(255 - pColor.getRed(), 255 - pColor.getGreen(), 255 - pColor.getBlue());
		return c;
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

}
