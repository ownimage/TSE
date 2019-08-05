package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.immutable.IImmutableVector;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.CannyEdgeTransform;

import java.util.Optional;
import java.util.stream.Stream;

public interface IPixelChain {

    IImmutableVector<Pixel> getPixels();

    IImmutableVector<ISegment> getSegments();

    IImmutableVector<IVertex> getVertexes();


    default Stream<Pixel> streamPixels() {
        return getPixels().stream();
    }

    default Stream<ISegment> streamSegments() {
        return getSegments().stream();
    }

    default int getPixelCount() {
        return getPixels().size();
    }


    default ISegment getSegment(final int i) {
        if (getSegments().size() <= i || i < 0) return null;
        return getSegments().get(i);
    }

    default IVertex getVertex(final int i) {
        if (getVertexes().size() <= i || i < 0) return null;
        return getVertexes().get(i);
    }

    @Deprecated
    default Pixel getPixel(final int pIndex) {
        if (pIndex < 0 || pIndex > getPixelCount()) {
            String msg = "pIndex, currently: %s, must be between 0 and the length of mPixels, currently: %s";
            throw new IllegalArgumentException(String.format(msg, pIndex, getPixelCount()));
        }

        return getPixels().get(pIndex);
    }

    default Optional<Pixel> getOptionalPixel(final int pIndex) {
        if (pIndex < 0 || pIndex > getPixelCount()) {
            return Optional.empty();
        }
        return Optional.of(getPixels().get(pIndex));
    }

    default int getSegmentCount() {
        return getSegments().size();
    }

    default int getVertexCount() {
        return getVertexes().size();
    }

    /**
     * Gets the UHVW value of the Pixel at the specified position.
     *
     * @param pIndex    the index
     * @param pPixelMap
     * @return the UHVW Point
     */
    default Point getUHVWPoint(final int pIndex, final PixelMap pPixelMap) {
        if (pIndex < 0 || pIndex > getPixelCount()) {
            String msg = "pIndex, currently: %s, must be between 0 and the length of mPixels, currently: %s";
            throw new IllegalArgumentException(String.format(msg, pIndex, getPixelCount()));
        }
        return getPixels().get(pIndex).getUHVWMidPoint(pPixelMap);
    }

    default Optional<ISegment> getOptionalLastSegment() {
        return getSegments().lastElement();
    }

    private double getActualCurvedThickness(final IPixelMapTransformSource pTransformSource, final double pFraction) {
        var c = pTransformSource.getLineEndThickness() * getWidth(pTransformSource);
        var a = c - getWidth(pTransformSource);
        var b = -2.0 * a;
        return a * pFraction * pFraction + b * pFraction + c;
    }

    private double getActualSquareThickness(final IPixelMapTransformSource pTransformSource, final double pFraction) {
        return getWidth(pTransformSource);
    }

    private double getActualStraightThickness(final IPixelMapTransformSource pTransformSource, final double pFraction) {
        var min = pTransformSource.getLineEndThickness() * getWidth(pTransformSource);
        var max = getWidth(pTransformSource);
        return min + pFraction * (max - min);
    }

    /**
     * Gets the actual thickness of the line. This allows for the tapering of the line at the ends.
     *
     * @param pPosition the position
     * @return the actual thickness
     */
    default double getActualThickness(final IPixelMapTransformSource pTransformSource, final double pPosition) {
        // TODO needs refinement should not really pass the pTolerance in as this can be determined from the PixelChain.
        // TODO this could be improved for performance
        final double fraction = getActualThicknessEndFraction(pTransformSource, pPosition);

        switch (pTransformSource.getLineEndShape()) {
            case Curved:
                return getActualCurvedThickness(pTransformSource, fraction);
            case Square:
                return getActualSquareThickness(pTransformSource, fraction);
        }
        // fall through to straight
        return getActualStraightThickness(pTransformSource, fraction);
    }

    /**
     * Gets fraction of the way along the end segment that the pPosition is. 0 would mean at the thinnest end. 1 would mean full thickness.
     *
     * @param pPosition the position
     * @return the actual thickness end fraction
     */
    private double getActualThicknessEndFraction(final IPixelMapTransformSource pTransformSource, final double pPosition) {

        var end2 = getLength() - pPosition;
        var closestEnd = Math.min(pPosition, end2);

        if (pTransformSource.getLineEndLengthType() == CannyEdgeTransform.LineEndLengthType.Percent) {
            var closestPercent = 100.0d * closestEnd / getLength();
            return Math.min(closestPercent / pTransformSource.getLineEndLengthPercent(), 1.0d);
        }

        // type is Pixels
        var fraction = pTransformSource.getHeight() * closestEnd / pTransformSource.getLineEndLengthPixel();
        return Math.min(fraction, 1.0d);

    }

    default double getWidth(final IPixelMapTransformSource pIPMTS) {
        switch (getThickness()) {
            case Thin:
                return pIPMTS.getShortLineThickness() / pIPMTS.getHeight();
            case Normal:
                return pIPMTS.getMediumLineThickness() / pIPMTS.getHeight();
            case Thick:
                return pIPMTS.getLongLineThickness() / pIPMTS.getHeight();
        }
        return 0.0d;
    }

    PixelChain.Thickness getThickness();

    double getLength();

    public enum Thickness {
        None, Thin, Normal, Thick
    }

    public enum PegCounters {
        StartSegmentStraightToCurveAttempted,
        StartSegmentStraightToCurveSuccessful,
        RefineCornersAttempted,
        RefineCornersSuccessful,
        MidSegmentEatForwardAttempted,
        MidSegmentEatForwardSuccessful
    }
}
