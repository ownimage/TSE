/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import java.awt.*;

import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CannyEdgeTransform.LineEndShape;

/**
 * The Interface IPixelMapTransformSource adds additional methods to an ITtransformSource to get the PixelMap related information.
 */
public interface IPixelMapTransformSource {


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

	public double getLongLineThickness();

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

	public boolean getShowLines();

	public boolean getShowShadow();

}
