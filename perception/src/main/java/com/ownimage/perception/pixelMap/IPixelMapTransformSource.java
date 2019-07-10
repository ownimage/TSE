/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CannyEdgeTransform.LineEndShape;

import java.awt.*;

/**
 * The Interface IPixelMapTransformSource adds additional methods to an ITtransformSource to get the PixelMap related information.
 */
public interface IPixelMapTransformSource {
    int getHeight();

    Color getLineColor();

    double getLineCurvePreference();

    int getLineEndLengthPercent();

    int getLineEndLengthPixel();

    CannyEdgeTransform.LineEndLengthType getLineEndLengthType();

    LineEndShape getLineEndShape();

    double getLineEndThickness();

    double getLineOpacity();

    double getLineTolerance();

    double getLongLineThickness();

    double getMediumLineThickness();

    Color getPixelColor();

    Color getShadowColor();

    double getShadowOpacity();

    double getShadowThickness();

    double getShadowXOffset();

    double getShadowYOffset();

    double getShortLineThickness();

    boolean getShowPixels();

    boolean getShowLines();

    boolean getShowShadow();
}
