package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.PegCounter;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.immutable.IVertex;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.CannyEdgeTransform;
import lombok.NonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public interface IPixelChain {

    default PegCounter getPegCounter() {
        return Services.getServices().getPegCounter();
    }

    ImmutableVectorClone<Pixel> getPixels();

    ImmutableVectorClone<ISegment> getSegments();

    ImmutableVectorClone<IVertex> getVertexes();

    default Stream<Pixel> streamPixels() {
        return getPixels().stream();
    }

    default Stream<ISegment> streamSegments() {
        return getSegments().stream();
    }

    default int getPixelCount() {
        return getPixels().size();
    }

    default int getMaxPixelIndex() {
        return getPixelCount() - 1;
    }

    default ISegment getSegment(int i) {
        if (getSegments().size() <= i || i < 0) {
            return null;
        }
        return getSegments().get(i);
    }

    default IVertex getVertex(int i) {
        if (getVertexes().size() <= i || i < 0) {
            return null;
        }
        return getVertexes().get(i);
    }

    @Deprecated
    default Pixel getPixel(int pIndex) {
        if (pIndex < 0 || pIndex > getPixelCount()) {
            String msg = "pIndex, currently: %s, must be between 0 and the pixelLength of mPixels, currently: %s";
            throw new IllegalArgumentException(String.format(msg, pIndex, getPixelCount()));
        }

        return getPixels().get(pIndex);
    }

    default Optional<Pixel> getOptionalPixel(int pIndex) {
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
     * @param pPixelMap
     * @param pIndex    the index
     * @return the UHVW Point
     */
    default Point getUHVWPoint(PixelMapData pPixelMap, int pIndex) {
        if (pIndex < 0 || pIndex > getPixelCount()) {
            String msg = "pIndex, currently: %s, must be between 0 and the pixelLength of mPixels, currently: %s";
            throw new IllegalArgumentException(String.format(msg, pIndex, getPixelCount()));
        }
        return getPixels().get(pIndex).getUHVWMidPoint(pPixelMap.height());
    }

    default Optional<ISegment> getOptionalLastSegment() {
        return getSegments().lastElement();
    }

    private double getActualCurvedThickness(IPixelMapTransformSource pTransformSource, double pFraction) {
        var c = pTransformSource.getLineEndThickness() * getWidth(pTransformSource);
        var a = c - getWidth(pTransformSource);
        var b = -2.0 * a;
        return a * pFraction * pFraction + b * pFraction + c;
    }

    private double getActualSquareThickness(IPixelMapTransformSource pTransformSource, double pFraction) {
        return getWidth(pTransformSource);
    }

    private double getActualStraightThickness(IPixelMapTransformSource pTransformSource, double pFraction) {
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
    default double getActualThickness(IPixelMapTransformSource pTransformSource, double pPosition) {
        // TODO needs refinement should not really pass the pTolerance in as this can be determined from the PixelChain.
        // TODO this could be improved for performance
        double fraction = getActualThicknessEndFraction(pTransformSource, pPosition);

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
    private double getActualThicknessEndFraction(IPixelMapTransformSource pTransformSource, double pPosition) {

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

    default double getWidth(IPixelMapTransformSource pIPMTS) {
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

    default ISegment getFirstSegment() {
        return getSegments().firstElement().orElse(null);
    }

    default ISegment getLastSegment() {
        return getSegments().lastElement().orElse(null);
    }

    default IVertex getLastVertex() {
        return getVertexes().lastElement().orElse(null);
    }

    enum Thickness {
        Thick, Normal() {
            public String toString() {
                return "Medium";
            }
        }, Thin, None
    }

    enum PegCounters {
        StartSegmentStraightToCurveAttempted,
        StartSegmentStraightToCurveSuccessful,
        RefineCornersAttempted,
        RefineCornersSuccessful,
        MidSegmentEatForwardAttempted,
        MidSegmentEatForwardSuccessful,
        refine01FirstSegmentAttempted,
        refine01FirstSegmentSuccessful
    }

    default PixelChain changePixels(Function<ImmutableVectorClone<Pixel>, ImmutableVectorClone<Pixel>> fn) {
        return new PixelChain(
                fn.apply(getPixels()),
                getSegments(),
                getVertexes(),
                getLength(),
                getThickness()
        );
    }

    default PixelChain changeSegments(Function<ImmutableVectorClone<ISegment>, ImmutableVectorClone<ISegment>> fn) {
        return new PixelChain(
                getPixels(),
                fn.apply(getSegments()),
                getVertexes(),
                getLength(),
                getThickness()
        );
    }


    default PixelChain changeVertexes(Function<ImmutableVectorClone<IVertex>, ImmutableVectorClone<IVertex>> fn) {
        return new PixelChain(
                getPixels(),
                getSegments(),
                fn.apply(getVertexes()),
                getLength(),
                getThickness()
        );
    }

    default PixelChain setLength(double length) {
        return new PixelChain(
                getPixels(),
                getSegments(),
                getVertexes(),
                length,
                getThickness()
        );
    }

    default PixelChain setThickness(@NonNull Thickness thickness) {
        return new PixelChain(
                getPixels(),
                getSegments(),
                getVertexes(),
                getLength(),
                thickness
        );
    }

    default PixelChain setVertex(IVertex pVertex) {
        return changeVertexes(v -> v.set(pVertex.getVertexIndex(), pVertex));
    }

    default PixelChain setSegment(ISegment pSegment) {
        return changeSegments(v -> v.set(pSegment.getSegmentIndex(), pSegment));
    }
}
