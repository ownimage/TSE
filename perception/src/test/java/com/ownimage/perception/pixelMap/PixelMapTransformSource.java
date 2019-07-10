package com.ownimage.perception.pixelMap;

import com.ownimage.perception.transform.CannyEdgeTransform;

import java.awt.*;

public class PixelMapTransformSource implements IPixelMapTransformSource {
    final private int mHeight;
    final private double mLineTolerance;
    final private double mLineCurvePreference;

    public PixelMapTransformSource(int pHeight, double pLineTolerance, double pLineCurvePreference) {
        mHeight = pHeight;
        mLineTolerance = pLineTolerance;
        mLineCurvePreference = pLineCurvePreference;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }

    @Override
    public Color getLineColor() {
        return null;
    }

    @Override
    public double getLineCurvePreference() {
        return mLineCurvePreference;
    }

    @Override
    public int getLineEndLengthPercent() {
        return 0;
    }

    @Override
    public int getLineEndLengthPixel() {
        return 0;
    }

    @Override
    public CannyEdgeTransform.LineEndLengthType getLineEndLengthType() {
        return null;
    }

    @Override
    public CannyEdgeTransform.LineEndShape getLineEndShape() {
        return null;
    }

    @Override
    public double getLineEndThickness() {
        return 0;
    }

    @Override
    public double getLineOpacity() {
        return 0;
    }

    @Override
    public double getLineTolerance() {
        return mLineTolerance;
    }

    @Override
    public double getLongLineThickness() {
        return 0;
    }

    @Override
    public double getMediumLineThickness() {
        return 0;
    }

    @Override
    public Color getPixelColor() {
        return null;
    }

    @Override
    public Color getShadowColor() {
        return null;
    }

    @Override
    public double getShadowOpacity() {
        return 0;
    }

    @Override
    public double getShadowThickness() {
        return 0;
    }

    @Override
    public double getShadowXOffset() {
        return 0;
    }

    @Override
    public double getShadowYOffset() {
        return 0;
    }

    @Override
    public double getShortLineThickness() {
        return 0;
    }

    @Override
    public boolean getShowPixels() {
        return false;
    }

    @Override
    public boolean getShowLines() {
        return false;
    }

    @Override
    public boolean getShowShadow() {
        return false;
    }
}
