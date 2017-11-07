/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap;

import java.awt.Color;

import com.ownimage.framework.util.Version;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CannyEdgeTransform.LineEndShape;

/**
 * The Interface IPixelMapTransformSource adds additional methods to an ITtransformSource to get the PixelMap related information.
 */
public interface IPixelMapTransformSource {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	public int getHeight();

	public Color getLineColor();

	public double getLineCurvePreference();

	public int getLineEndLengthPercent();

	public int getLineEndLengthPixel();

	public CannyEdgeTransform.LineEndLengthType getLineEndLengthType();

	public LineEndShape getLineEndShape();

	public double getLineEndThickness();

	public double getLineOpacity();

	public double getLineTolerance();

	public int getLongLineLength();

	public double getLongLineThickness();

	public int getMediumLineLength();

	public double getMediumLineThickness();

	public Color getPixelColor();

	public Color getShadowColor();

	public double getShadowOpacity();

	public double getShadowThickness();

	public double getShadowXOffset();

	public double getShadowYOffset();

	public int getShortLineLength();

	public double getShortLineThickness();

	public boolean getShowPixels();

	public boolean getShowShadow();

}
