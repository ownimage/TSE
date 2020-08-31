package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.transform.CannyEdgeTransform;
import lombok.NonNull;
import org.immutables.value.Value;

import java.awt.*;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value.Immutable
public interface PixelChain extends Serializable {

    @Value.Parameter(order = 1)
    ImmutableVectorClone<Pixel> pixels();

    @Value.Parameter(order = 2)
    ImmutableVectorClone<Vertex> vertexes();

    @Value.Parameter(order = 3)
    ImmutableVectorClone<Segment> segments();

    @Value.Parameter(order = 4)
    double length();

    @Value.Parameter(order = 5)
    Thickness thickness();

    @Value
    Optional<Color> color();

    default Stream<Pixel> streamPixels() {
        return pixels().stream();
    }

    default Stream<Segment> streamSegments() {
        return segments().stream();
    }

    default int pixelCount() {
        return pixels().size();
    }

    default int maxPixelIndex() {
        return pixelCount() - 1;
    }

    default Segment getSegment(int i) {
        if (segments().size() <= i || i < 0) {
            return null;
        }
        return segments().get(i);
    }

    default Vertex getVertex(int i) {
        if (vertexes().size() <= i || i < 0) {
            return null;
        }
        return vertexes().get(i);
    }

    default Optional<Pixel> optionalPixel(int pIndex) {
        if (pIndex < 0 || pIndex > pixelCount()) {
            return Optional.empty();
        }
        return Optional.of(pixels().get(pIndex));
    }

    default int segmentCount() {
        return segments().size();
    }

    default int vertexCount() {
        return vertexes().size();
    }

    /**
     * Gets the UHVW value of the Pixel at the specified position.
     *
     * @param pPixelMap
     * @param pIndex    the index
     * @return the UHVW Point
     */
    default Point getUHVWPoint(PixelMap pPixelMap, int pIndex) {
        if (pIndex < 0 || pIndex > pixelCount()) {
            String msg = "pIndex, currently: %s, must be between 0 and the pixelLength of mPixels, currently: %s";
            throw new IllegalArgumentException(String.format(msg, pIndex, pixelCount()));
        }
        return pixels().get(pIndex).getUHVWMidPoint(pPixelMap.height());
    }

    default Optional<Segment> optionalLastSegment() {
        return segments().lastElement();
    }

    private double actualCurvedThickness(IPixelMapTransformSource pTransformSource, double pFraction) {
        var c = pTransformSource.getLineEndThickness() * width(pTransformSource);
        var a = c - width(pTransformSource);
        var b = -2.0 * a;
        return a * pFraction * pFraction + b * pFraction + c;
    }

    private double actualSquareThickness(IPixelMapTransformSource pTransformSource, double pFraction) {
        return width(pTransformSource);
    }

    private double actualStraightThickness(IPixelMapTransformSource pTransformSource, double pFraction) {
        var min = pTransformSource.getLineEndThickness() * width(pTransformSource);
        var max = width(pTransformSource);
        return min + pFraction * (max - min);
    }

    /**
     * Gets the actual thickness of the line. This allows for the tapering of the line at the ends.
     *
     * @param pPosition the position
     * @return the actual thickness
     */
    default double actualThickness(IPixelMapTransformSource pTransformSource, double pPosition) {
        // TODO needs refinement should not really pass the pTolerance in as this can be determined from the PixelChain.
        // TODO this could be improved for performance
        double fraction = actualThicknessEndFraction(pTransformSource, pPosition);

        switch (pTransformSource.getLineEndShape()) {
            case Curved:
                return actualCurvedThickness(pTransformSource, fraction);
            case Square:
                return actualSquareThickness(pTransformSource, fraction);
        }
        // fall through to straight
        return actualStraightThickness(pTransformSource, fraction);
    }

    /**
     * Gets fraction of the way along the end segment that the pPosition is. 0 would mean at the thinnest end. 1 would mean full thickness.
     *
     * @param pPosition the position
     * @return the actual thickness end fraction
     */
    private double actualThicknessEndFraction(IPixelMapTransformSource pTransformSource, double pPosition) {

        var end2 = length() - pPosition;
        var closestEnd = Math.min(pPosition, end2);

        if (pTransformSource.getLineEndLengthType() == CannyEdgeTransform.LineEndLengthType.Percent) {
            var closestPercent = 100.0d * closestEnd / length();
            return Math.min(closestPercent / pTransformSource.getLineEndLengthPercent(), 1.0d);
        }

        // type is Pixels
        var fraction = pTransformSource.getHeight() * closestEnd / pTransformSource.getLineEndLengthPixel();
        return Math.min(fraction, 1.0d);

    }

    default double width(IPixelMapTransformSource pIPMTS) {
        switch (thickness()) {
            case Thin:
                return pIPMTS.getShortLineThickness() / pIPMTS.getHeight();
            case Normal:
                return pIPMTS.getMediumLineThickness() / pIPMTS.getHeight();
            case Thick:
                return pIPMTS.getLongLineThickness() / pIPMTS.getHeight();
        }
        return 0.0d;
    }

    default Segment firstSegment() {
        return segments().firstElement().orElse(null);
    }

    default Segment lastSegment() {
        return segments().lastElement().orElse(null);
    }

    default Vertex lastVertex() {
        return vertexes().lastElement().orElse(null);
    }

    default ImmutablePixelChain changePixels(Function<ImmutableVectorClone<Pixel>, ImmutableVectorClone<Pixel>> fn) {
        return ImmutablePixelChain.copyOf(this).withPixels(fn.apply(pixels()));
    }

    default ImmutablePixelChain changeSegments(Function<ImmutableVectorClone<Segment>, ImmutableVectorClone<Segment>> fn) {
        return ImmutablePixelChain.copyOf(this).withSegments(fn.apply(segments()));
    }

    default ImmutablePixelChain changeVertexes(Function<ImmutableVectorClone<Vertex>, ImmutableVectorClone<Vertex>> fn) {
        return ImmutablePixelChain.copyOf(this).withVertexes(fn.apply(vertexes()));
    }

    default ImmutablePixelChain setThickness(@NonNull Thickness thickness) {
        return ImmutablePixelChain.copyOf(this).withThickness(thickness);
    }

    default ImmutablePixelChain setVertex(Vertex pVertex) {
        return ImmutablePixelChain.copyOf(this).withVertexes(vertexes().set(pVertex.getVertexIndex(), pVertex));
    }

    default ImmutablePixelChain setSegment(Segment pSegment) {
        return changeSegments(v -> v.set(pSegment.getSegmentIndex(), pSegment));
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

    default String toReadableString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");
        sb.append(pixels().stream().map(Pixel::toXYString).collect(Collectors.joining(", ")));
        sb.append(" ]\n");
        return sb.toString();
    }

}
