package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.PegCounter;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelChainBuilder;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelChainService {

    private final static Logger mLogger = Framework.getLogger();
    private static PixelMapService pixelMapService = Services.getDefaultServices().getPixelMapService();
    private static PixelMapTransformService pixelMapTransformService = Services.getDefaultServices().getPixelMapTransformService();
    private static VertexService vertexService = Services.getDefaultServices().getVertexService();
    private static PegCounter pegCounterService = com.ownimage.perception.app.Services.getServices().getPegCounter();

    public PixelChain fixNullPositionVertexes(int height, PixelChain pixelChain) {
        var mappedVertexes = pixelChain.getVertexes().stream()
                .map(v -> {
                    var p = v.getPosition();
                    if (p == null) {
                        p = vertexService.getPixel(pixelChain, v).getUHVWMidPoint(height);
                        return vertexService.createVertex(pixelChain, v.getVertexIndex(), v.getPixelIndex(), p);
                    }
                    return v;
                })
                .collect(Collectors.toList());
        var vertexes = new ImmutableVectorClone<IVertex>().addAll(mappedVertexes);
        return new PixelChain(pixelChain.getPixels(), pixelChain.getSegments(), vertexes, pixelChain.getLength(), pixelChain.getThickness());
    }

    private PixelChainBuilder builder(PixelChain pixelChain) {
        return new PixelChainBuilder(
                pixelChain.getPixels().toVector(),
                pixelChain.getVertexes().toVector(),
                pixelChain.getSegments().toVector(),
                pixelChain.getLength(),
                pixelChain.getThickness()
        );
    }

    public PixelChain add(IPixelChain pixelChain, Pixel pPixel) {
        return pixelChain.changePixels(p -> p.add(pPixel));
    }

    public PixelChain approximateCurvesOnly(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference
    ) {
        if (pixelChain.getPixelCount() <= 4) {
            return pixelChain;
        }

        var result = pixelChain;
        result = result.changeVertexes(ImmutableVectorClone::clear);
        result = result.changeSegments(ImmutableVectorClone::clear);
        var builder = builder(result);
        builder.approximateCurvesOnly_firstSegment(pixelMap, tolerance, lineCurvePreference);
        result = builder.build();
        while (result.getLastVertex().getPixelIndex() != result.getMaxPixelIndex()) {
            result = approximateCurvesOnly_subsequentSegments(pixelMap, result, tolerance, lineCurvePreference);
        }
        return result;
    }

    /**
     * Creates a copy of this PixelChain with the order of the pixels in the pixel chain.
     * The original PixelChain is not altered.
     * This means reversing the start and end nodes.
     * All of the segments in the line are also reversed and replaced with new straight line
     * segments.
     *
     * @param pixelMap the PixelMap this chain belongs to
     * @return a new PixelChain with the elements reversed
     */
    public PixelChain reverse(PixelMapData pixelMap, PixelChain pixelChain) {
        // note that this uses direct access to the data members as the public setters have other side effects
        //validate("reverse");
        IPixelChain builder = builder(pixelChain);

        // reverse pixels
        Vector<Pixel> pixels = builder.getPixels().toVector();
        Collections.reverse(pixels);
        builder = builder.changePixels(p -> p.clear().addAll(pixels));

        // reverse vertexes
        int maxPixelIndex = builder.getPixels().size() - 1;
        Vector<IVertex> vertexes = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            IVertex vertex = builder.getVertexes().get(i);
            IVertex v = vertexService.createVertex(pixelMap, builder, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
        }
        builder = builder.changeVertexes(v -> v.clear().addAll(vertexes));

        // reverse segments
        Vector<ISegment> segments = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            if (i != pixelChain.getVertexes().size() - 1) {
                StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pixelMap, builder, segments.size());
                segments.add(newSegment);
            }
        }
        return builder.changeSegments(s -> s.clear().addAll(segments));
    }

    public PixelChain refine(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference) {
        var result = refine01_matchCurves(pixelMap, pixelChain, lineCurvePreference);
        return refine03_matchCurves(pixelMap, result, tolerance, lineCurvePreference);
    }

    public PixelChain refine03FirstSegment(
            PixelMapData pixelMap,
            IPixelChain pixelChain,
            double lineCurvePreference,
            ISegment currentSegment
    ) {
        // this only works if this or the next segment are straight
        var originalNextSegment = currentSegment.getNextSegment(pixelChain);
        if (!((currentSegment instanceof StraightSegment) || (originalNextSegment instanceof StraightSegment))) {
            return PixelChain.of(pixelChain);
        }

        var bestCandidateSegment = currentSegment;
        var bestCandidateVertex = currentSegment.getEndVertex(pixelChain);
        var originalEndVertex = currentSegment.getEndVertex(pixelChain);
        var result = PixelChain.of(pixelChain);

        try {
            pegCounterService.increase(IPixelChain.PegCounters.StartSegmentStraightToCurveAttempted);
            var lowestError = currentSegment.calcError(pixelMap, result) * 1000 * lineCurvePreference; // TODO
            var nextSegmentPixelLength = originalNextSegment.getPixelLength(result);
            var controlPointEnd = originalEndVertex.getPosition()
                    .add(
                            originalNextSegment.getStartTangent(pixelMap, result)
                                    .getAB()
                                    .normalize()
                                    .multiply(currentSegment.getLength(pixelMap, result)
                                    )
                    );
            var length = currentSegment.getLength(pixelMap, result) / originalNextSegment.getLength(pixelMap, result);
            controlPointEnd = originalNextSegment.getPointFromLambda(pixelMap, result, -length);
            for (int i = nextSegmentPixelLength / 2; i >= 0; i--) {
                result = result.changeVertexes(v -> v.set(originalEndVertex.getVertexIndex(), originalEndVertex));
                var lambda = (double) i / nextSegmentPixelLength;
                var controlPointStart = originalNextSegment.getPointFromLambda(pixelMap, result, lambda);
                var candidateVertex = vertexService.createVertex(result, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
                result = result.changeVertexes(v -> v.set(candidateVertex.getVertexIndex(), candidateVertex));
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, result, currentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        result = result.setSegment(candidateSegment);
                        var candidateError = candidateSegment.calcError(pixelMap, result);

                        if (isValid(pixelMap, result, candidateSegment) && candidateError < lowestError) {
                            lowestError = candidateError;
                            bestCandidateSegment = candidateSegment;
                            bestCandidateVertex = candidateVertex;
                        }
                    }
                }
            }
        } finally {
            if (bestCandidateSegment != currentSegment) {
                pegCounterService.increase(IPixelChain.PegCounters.StartSegmentStraightToCurveSuccessful);
            }
            result = result.setVertex(bestCandidateVertex);
            result = result.setSegment(bestCandidateSegment);
        }
        return result;
    }

    public PixelChain refine03_matchCurves(
            @NotNull PixelMapData pPixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference) {

        if (pixelChain.getSegmentCount() == 1) {
            return PixelChain.of(pixelChain);
        }

        var result = StrongReference.of(PixelChain.of(pixelChain));
        result.get().streamSegments().forEach(currentSegment -> {
            if (currentSegment == result.get().getFirstSegment()) {
                result.update(r -> refine03FirstSegment(pPixelMap, r, lineCurvePreference, currentSegment));
            } else if (currentSegment == result.get().getLastSegment()) {
                result.update(r -> refine03LastSegment(pPixelMap, r, lineCurvePreference, currentSegment));
            } else {
                result.update(r-> refine03MidSegment(pPixelMap, r, lineCurvePreference, currentSegment));
            }
        });
        return result.get();
    }

    public PixelChain refine03LastSegment(
            @NotNull PixelMapData pixelMap,
            @NotNull IPixelChain pixelChain,
            double lineCurvePreference,
            @NotNull ISegment currentSegment) {
        var originalPrevSegment = currentSegment.getPreviousSegment(pixelChain);
        // this only works if this or the previous segment are straight
        if (!((currentSegment instanceof StraightSegment) || (originalPrevSegment instanceof StraightSegment))) {
            return PixelChain.of(pixelChain);
        }

        var bestCandidateSegment = currentSegment;
        var bestCandidateVertex = currentSegment.getEndVertex(pixelChain);
        var originalStartVertex = currentSegment.getStartVertex(pixelChain);
        var result = PixelChain.of(pixelChain);

        try {
            pegCounterService.increase(IPixelChain.PegCounters.StartSegmentStraightToCurveAttempted);
            var lowestError = currentSegment.calcError(pixelMap, result) * 1000 * lineCurvePreference; // TODO
            var prevSegmentPixelLength = originalPrevSegment.getPixelLength(result);
            var controlPointEnd = originalStartVertex.getPosition()
                    .add(
                            originalPrevSegment.getEndTangent(pixelMap, result)
                                    .getAB()
                                    .normalize()
                                    .multiply(currentSegment.getLength(pixelMap, result)
                                    )
                    );
            var length = currentSegment.getLength(pixelMap, result) / originalPrevSegment.getLength(pixelMap, result);
            controlPointEnd = originalPrevSegment.getPointFromLambda(pixelMap, result, 1.0d + length);
            for (int i = (prevSegmentPixelLength / 2) - 1; i >= 0; i--) {
                result = result.setVertex(originalStartVertex);
                var lambda = 1.0d - (double) i / prevSegmentPixelLength; // TODO
                var controlPointStart = originalPrevSegment.getPointFromLambda(pixelMap, result, lambda);
                var candidateVertex = vertexService.createVertex(result, originalStartVertex.getVertexIndex(), originalStartVertex.getPixelIndex() - i, controlPointStart);
                result = result.setVertex(candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                // TODO below should refactor this
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, result, currentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        result = result.setSegment(candidateSegment);
                        var candidateError = candidateSegment.calcError(pixelMap, result);

                        if (isValid(pixelMap, result, candidateSegment) && candidateError < lowestError) {
                            lowestError = candidateError;
                            bestCandidateSegment = candidateSegment;
                            bestCandidateVertex = candidateVertex;
                        }
                    }
                }
            }
        } finally {
            if (bestCandidateSegment != currentSegment) {
                pegCounterService.increase(IPixelChain.PegCounters.StartSegmentStraightToCurveSuccessful);
            }
            result = result.setVertex(bestCandidateVertex);
            result = result.setSegment(bestCandidateSegment);
            // TODO System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this));
        }
        return result;
    }

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    public Pixel firstPixel(PixelChain pixelChain) {
        return pixelChain.getPixels().firstElement().orElseThrow();
    }

    public Optional<Node> getEndNode(ImmutablePixelMapData pixelMap, PixelChain pixelChain) {
        return pixelMapService.getNode(pixelMap, pixelChain.getPixels().lastElement().orElseThrow());
    }


    public Optional<Node> getStartNode(ImmutablePixelMapData pixelMap, PixelChain pixelChain) {
        return pixelMapService.getNode(pixelMap, pixelChain.getPixels().firstElement().orElseThrow());
    }

    public IVertex getStartVertex(IPixelChain pixelChain) {
        return pixelChain.getVertexes().firstElement().orElse(null);
    }

    /**
     * Sets thickness.  If pThickness is null then it sets the thickness to None.  If there is no change to the thickness
     * then this method returns this object, otherwise it will return a new PixelChain with the new thickness.
     *
     * @param thickness the p thickness
     * @return the PixeclChain
     */
    public PixelChain withThickness(@NotNull PixelChain pixelChain, @NotNull IPixelChain.Thickness thickness) {
        if (thickness == pixelChain.getThickness()) {
            return pixelChain;
        }
        // TODO what is the best way to do this
        return new PixelChain(pixelChain.getPixels(), pixelChain.getSegments(), pixelChain.getVertexes(), pixelChain.getLength(), thickness);
    }


    public Tuple2<ImmutablePixelMapData, PixelChain> setEndNode(@NotNull PixelMapData pixelMap, @NotNull PixelChain pixelChain, @NonNull Node pNode) {
        var pixelMapResult = ImmutablePixelMapData.copyOf(pixelMap);
        IPixelChain builder = pixelChain.changePixels(p -> p.add(pNode));

        // need to do a check here to see if we are clobbering over another chain
        // if pixel end-2 is a neighbour of pixel end then pixel end-1 needs to be set as notVisited and removed from the chain
        if (builder.getPixelCount() >= 3 && pNode.isNeighbour(builder.getPixel(builder.getPixelCount() - 3))) {
            var index = builder.getPixelCount() - 2;
            pixelMapResult = pixelMapService.setVisited(pixelMapResult, builder.getPixel(index), false);
            builder = builder.changePixels(p -> p.remove(index));
        }
        return new Tuple2(pixelMapResult, builder);
    }

    public IPixelChain.Thickness getThickness(
            @NotNull PixelChain pixelChain, int thinLength, int normalLength, int longLength) {
        var pixelLength = getPixelLength(pixelChain);
        if (pixelLength < thinLength) {
            return IPixelChain.Thickness.None;
        } else if (pixelLength < normalLength) {
            return IPixelChain.Thickness.Thin;
        } else if (pixelLength < longLength) {
            return IPixelChain.Thickness.Normal;
        }
        return IPixelChain.Thickness.Thick;
    }

    public int getPixelLength(@NotNull PixelChain pixelChain) {
        return pixelChain.getPixels().size();
    }

    public PixelChain withThickness(
            @NotNull PixelChain pixelChain, int thinLength, int normalLength, int longLength) {
        IPixelChain.Thickness thickness = getThickness(pixelChain, thinLength, normalLength, longLength);
        return withThickness(pixelChain, thickness);
    }

    /**
     * Adds the two pixel chains together. It allocates all of the pixels from the otherChain to this, unattaches both chains from the middle node, and adds all of the segments from the second chain
     * to the first (joining at the appropriate vertex in the middle, and using the correct offset for the new vertexes). Note that the new segments that are copied from the otherChain are all
     * LineApproximations.
     *
     * @param pixelMap   the pixelMap
     * @param otherChain the other chain
     */
    public PixelChain merge(@NotNull PixelMapData pixelMap, @NotNull PixelChain thisChain, @NotNull PixelChain otherChain) {
        StrongReference<IPixelChain> builder = StrongReference.of(thisChain);

        validate(thisChain, false, "add otherChain");
        mLogger.fine(() -> String.format("builder.getPixels().size() = %s", builder.get().getPixels().size()));
        mLogger.fine(() -> String.format("pixelLength(otherChain) = %s", pixelLength(otherChain)));
        mLogger.fine(() -> String.format("this.mSegments.size() = %s", builder.get().getSegments().size()));
        mLogger.fine(() -> String.format("otherChain.mSegments.size() = %s", otherChain.getSegments().size()));
        if (mLogger.isLoggable(Level.FINE)) {
            builder.get().streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartVertex(thisChain).getPixelIndex(), s.getEndVertex(thisChain).getPixelIndex())));
            builder.get().streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartIndex(thisChain), s.getEndIndex(thisChain))));
            otherChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("otherChain.mSegment[%s, %s]", s.getStartVertex(otherChain).getPixelIndex(), s.getEndVertex(otherChain).getPixelIndex())));
            otherChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("otherChain.mSegment[%s, %s]", s.getStartIndex(otherChain), s.getEndIndex(otherChain))));
        }

        validate(builder.get(), false, "merge");
        validate(otherChain, false, "merge");

        // TODO this should be a pixelChainService mergable
        if (!builder.get().getPixels().lastElement().orElseThrow().equals(firstPixel(otherChain))) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + otherChain);
        }

        int offset = builder.get().getPixels().size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed
        builder.update(b -> b.changePixels(p -> p.remove(builder.get().getPixels().size() - 1))); // need to remove the last pixel as it will be duplicated on the other chain;
        builder.update(b -> b.changePixels(p -> p.addAll(otherChain.getPixels())));
        mLogger.fine(() -> String.format("offset = %s", offset));

        otherChain.getSegments().forEach(segment -> {
            IVertex end = vertexService.createVertex(pixelMap, builder.get(), builder.get().getVertexes().size(), segment.getEndIndex(otherChain) + offset);
            builder.update(b -> b.changeVertexes(v -> v.add(end)));
            StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pixelMap, builder.get(), builder.get().getSegments().size());
            builder.update(b -> b.changeSegments(s -> s.add(newSegment)));
        });

        mLogger.fine(() -> String.format("copy.mPixels.size() = %s", builder.get().getPixels().size()));
        mLogger.fine(() -> String.format("copy.mSegments.size() = %s", builder.get().getPixels().size()));
        thisChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("out.mSegment[%s, %s]", s.getStartVertex(thisChain).getPixelIndex(), s.getEndVertex(thisChain).getPixelIndex())));
        thisChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("out.is.mSegment[%s, %s]", s.getStartIndex(thisChain), s.getEndIndex(thisChain))));
        // TODO should recalculate thickness from source values
        var thickness = thisChain.getPixelCount() > otherChain.getPixelCount() ? thisChain.getThickness() : otherChain.getThickness();
        return builder.get().setThickness(thickness);
    }

    public void validate(@NotNull IPixelChain pixelChain, boolean pFull, @NotNull String pMethodName) {
        try {
            if (getStartVertex(pixelChain).getPixelIndex() != 0) {
                throw new IllegalStateException("getStartVertex().getPixelIndex() != 0");
            }
            var vertexSize = pixelChain.getVertexes().size();
            var segmentSize = pixelChain.getSegments().size();
            var pixelSize = pixelChain.getPixels().size();

            if (vertexSize == 0 && segmentSize != 0) {
                throw new IllegalStateException(String.format("vertexSize = %s && segmentSize = %s", vertexSize, segmentSize));
            }

            if (vertexSize != 0 && segmentSize + 1 != vertexSize) {
                throw new IllegalStateException(String.format("vertexSize = %s && segmentSize = %s", vertexSize, segmentSize));
            }

            int nextStartIndex[] = {0};

            pixelChain.getSegments().forEach(segment -> {
                if (segment.getStartIndex(pixelChain) != nextStartIndex[0]) { //
                    throw new IllegalStateException("segments not linked properly");
                }
                nextStartIndex[0] = segment.getEndIndex(pixelChain);
            });

            if (segmentSize != 0 && pixelChain.getSegments().lastElement().orElseThrow().getEndIndex(pixelChain) != pixelSize - 1) { //
                throw new IllegalStateException(String.format("last segment not linked properly, %s, %s, %s", segmentSize, pixelChain.getSegments().lastElement().orElseThrow().getEndIndex(pixelChain), pixelSize - 1));
            }

            checkAllVertexesAttached();

            IVertex vertex = getStartVertex(pixelChain);
            int index = 0;
            while (vertex != null) {
                if (pixelChain.getVertexes().get(vertex.getVertexIndex()) != vertex) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                if (vertex.getVertexIndex() != index) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                index++;
                vertex = vertexService.getEndSegment(pixelChain, vertex) != null
                        ? vertexService.getEndSegment(pixelChain, vertex).getEndVertex(pixelChain)
                        : null;
            }

            if (vertexSize != 0) {
                if (vertexService.getStartSegment(pixelChain, pixelChain.getVertexes().firstElement().orElseThrow()) != null) {
                    throw new RuntimeException("wrong start vertex");
                }
                if (vertexService.getEndSegment(pixelChain, pixelChain.getVertexes().lastElement().orElseThrow()) != null) {
                    throw new RuntimeException("wrong end vertex");
                }
            }

            int currentMax = -1;
            for (int i = 0; i < vertexSize; i++) {
                IVertex v = pixelChain.getVertexes().get(i);
                if (i == 0 && v.getPixelIndex() != 0) {
                    throw new IllegalStateException("First vertex wrong)");
                }
                if (i == vertexSize - 1 && v.getPixelIndex() != pixelSize - 1 && pFull) {
                    throw new IllegalStateException("Last vertex wrong)");
                }
                if (v.getPixelIndex() <= currentMax) {
                    throw new IllegalStateException("Wrong pixel index order");
                }
                currentMax = v.getPixelIndex();
                if (i != 0 && vertexService.getStartSegment(pixelChain, v) != pixelChain.getSegments().get(i - 1)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
                if (i != vertexSize - 1 && vertexService.getEndSegment(pixelChain, v) != pixelChain.getSegments().get(i)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
            }
        } catch (Exception pT) {
            printVertexs();
            throw pT;
        }
    }


    private void printVertexs() {
//        StringBuilder sb = new StringBuilder()
//                .append(String.format("mVertexes.size() = %s, mSegments.size() = %s", mVertexes.size(), mSegments.size()))
//                .append("\nArrray\n");
//
//        for (int i = 0; i < mVertexes.size(); i++) {
//            sb.append(String.format("i = %s, mVertexes.get(i).getVertexIndex() = %s\n", i, mVertexes.get(i).getVertexIndex()));
//        }
//
//        sb.append("\n\nWalking\n");
//        IVertex vertex = getStartVertex();
//        int index = 0;
//        while (vertex != null) {
//            sb.append(String.format("index = %s, vertex.getVertexIndex() = %s\n", index, vertex.getVertexIndex()));
//            index++;
//            vertex = vertex.getEndSegment(this) != null
//                    ? vertex.getEndSegment(this).getEndVertex(this)
//                    : null;
//        }
//
//        mLogger.severe(sb::toString);
    }

    private void checkAllVertexesAttached() {
//        mSegments.forEach(segment -> {
//            try {
//                if (mLogger.isLoggable(Level.SEVERE)) {
//                    if (segment.getStartVertex(this).getEndSegment(this) != segment) {
//                        mLogger.severe("start Vertex not attached");
//                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
//                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
//                    }
//                    if (segment.getEndVertex(this).getStartSegment(this) != segment) {
//                        mLogger.severe("end Vertex not attached");
//                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
//                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
//                    }
//                }
//            } catch (Exception pT) {
//                mLogger.log(Level.SEVERE, "Unxepected error", pT);
//            }
//        });
    }

    //    private PixelChainBuilder builder() {
//        return new PixelChainBuilder(mPixels.toVector(), mVertexes.toVector(), mSegments.toVector(), mLength, mThickness);
//    }
//
//
//
//
//
//
    public boolean contains(PixelChain pixelChain, Pixel pPixel) {
        return pixelChain.getPixels()
                .stream()
                .anyMatch(p -> p.samePosition(pPixel));
    }

    @Deprecated // this modifies the pixelmap there is a better version in
    public Tuple2<ImmutablePixelMapData, PixelChain> indexSegments(
            @NotNull ImmutablePixelMapData pixelMap, @NotNull PixelChain pixelChain, boolean add) {
        var result = StrongReference.of(pixelMap);
        if (add) {
            StrongReference<IPixelChain> builder = StrongReference.of(pixelChain);
            StrongReference<Double> startPosition = StrongReference.of(0.0d);
            pixelChain.getSegments().forEach(segment -> {
                ISegment segmentClone = segment.withStartPosition(startPosition.get());
                builder.update(b -> b.changeSegments(s -> s.set(segmentClone.getSegmentIndex(), segmentClone)));
                startPosition.update(s -> s += segment.getLength(pixelMap, builder.get()));
            });
            var newPixelChain = builder.get().setLength(startPosition.get());
            newPixelChain.streamSegments().forEach(segment -> {
                result.update(r -> index(r, newPixelChain, segment, true));
            });
            return new Tuple2<>(result.get(), newPixelChain);
        } else {
            pixelChain.getSegments().forEach(segment -> {
                result.update(r -> index(r, pixelChain, segment, false));
            });
            return new Tuple2<>(result.get(), pixelChain);
        }
    }

    public ImmutablePixelMapData index(
            @NotNull ImmutablePixelMapData pixelMap, @NotNull PixelChain pPixelChain, ISegment pSegment, boolean pAdd) {
        var result = StrongReference.of(pixelMap.withSegmentCount(pixelMap.segmentCount() + 1));
        // // TODO make assumption that this is 360
        // // mSegmentIndex.add(pLineSegment);
        //
        int minX = (int) Math.floor(pSegment.getMinX(pixelMap, pPixelChain) * pixelMap.width() / pixelMap.getAspectRatio()) - 1;
        minX = Math.max(minX, 0);
        minX = Math.min(minX, pixelMap.width() - 1);
        int maxX = (int) Math.ceil(pSegment.getMaxX(pixelMap, pPixelChain) * pixelMap.width() / pixelMap.getAspectRatio()) + 1;
        maxX = Math.min(maxX, pixelMap.width() - 1);
        int minY = (int) Math.floor(pSegment.getMinY(pixelMap, pPixelChain) * pixelMap.height()) - 1;
        minY = Math.max(minY, 0);
        minY = Math.min(minY, pixelMap.height() - 1);
        int maxY = (int) Math.ceil(pSegment.getMaxY(pixelMap, pPixelChain) * pixelMap.height()) + 1;
        maxY = Math.min(maxY, pixelMap.height() - 1);

        new Range2D(minX, maxX, minY, maxY).stream().forEach(i -> {
            Pixel pixel = pixelMapService.getPixelAt(result.get(), i.getX(), i.getY());
            Point centre = pixel.getUHVWMidPoint(pixelMap.height());
            if (pSegment.closerThan(result.get(), pPixelChain, centre, pixelMapService.getUHVWHalfPixel(pixelMap).length())) {
                val segments = new HashSet();
                pixelMapTransformService.getSegments(result.get(), i.getX(), i.getY())
                        .map(ImmutableSet::toCollection).ifPresent(segments::addAll);
                if (pAdd) {
                    segments.add(new Tuple2<>(pPixelChain, pSegment));
                } else {
                    segments.remove(new Tuple2<>(pPixelChain, pSegment));
                    System.out.println("########################### PixelMap  remove " + i);
                }
                result.update(r -> r.withSegmentIndex(r.segmentIndex()
                        .set(i.getX(), i.getY(), new ImmutableSet<Tuple2<PixelChain, ISegment>>().addAll(segments))));
            }
        });
        return result.get();
    }

//
//    /**
//     * @deprecated TODO: explain
//     */
//    @Deprecated
//    private Pixel lastPixel() {
//        // happy for this to throw exception
//        return mPixels.lastElement().orElseThrow();
//    }
//

    /**
     * Length of the PixelChain. This is the number of Pixels that it contains.
     *
     * @return the number of Pixels in the PixelChain.
     */
    public int pixelLength(@NotNull PixelChain pixelChain) {
        return pixelChain.getPixels().size();
    }


    /**
     * Merges two pixel chains together that share a common Node. The result is one PixelChain with a vertex where the Node was. The chain will have correctly attached itself to the node at either
     * end. This needs to be done before after the segments are generated so that the vertex for the node can be created.
     *
     * @param pPixelMap  the PixelMap
     * @param otherChain the other chain
     * @param pNode      the node
     */
    public PixelChain merge(ImmutablePixelMapData pPixelMap, PixelChain thisChain, PixelChain otherChain, Node pNode) {
        mLogger.fine("merge");
//        if (!(getStartNode(pPixelMap) == pNode || getEndNode(pPixelMap) == pNode) || !(otherChain.getStartNode(pPixelMap) == pNode || otherChain.getEndNode(pPixelMap) == pNode)) {
//            throw new IllegalArgumentException("Either this PixelChain: " + this + ", and otherChain: " + otherChain + ", must share the following node:" + pNode);
//        }

        StrongReference<PixelChain> one = new StrongReference<>(otherChain);
        getEndNode(pPixelMap, otherChain).filter(n -> n == pNode).ifPresent(n -> one.set(reverse(pPixelMap, otherChain)));

        StrongReference<PixelChain> other = new StrongReference<>(otherChain);
        getStartNode(pPixelMap, otherChain).filter(n -> n == pNode).ifPresent(n -> other.set(reverse(pPixelMap, otherChain)));

//        if (one.get().getEndNode(pPixelMap) != pNode || other.getStartNode(pPixelMap) != pNode) {
//            throw new RuntimeException("This PixelChain: " + this + " should end on the same node as the other PixelChain: " + otherChain + " starts with.");
//        }

        return merge(pPixelMap, thisChain, otherChain);
    }

    public PixelChain approximate(@NotNull PixelMapData pixelMap, @NotNull PixelChain pixelChain, double tolerance) {
        var result = approximate01_straightLines(pixelMap, pixelChain, tolerance);
        result = approximate02_refineCorners(pixelMap, result);
        return result;
    }

    public PixelChain approximate01_straightLines(
            @NotNull PixelMapData pixelMap, @NotNull PixelChain pixelChain, double tolerance) {
        // note that this is version will find the longest line that is close to all pixels.
        // there are cases where a line of pixelLength n will be close enough, a line of pixelLength n+1 will not be, but there exists an m such that a line of pixelLength m is close enough.
        if (pixelChain.getPixelCount() <= 1) {
            return pixelChain;
        }

        var builder = PixelChain.of(pixelChain);
        builder = builder.changeSegments(ImmutableVectorClone::clear);
        builder = builder.changeVertexes(ImmutableVectorClone::clear);

        var startVertex = vertexService.createVertex(pixelMap, pixelChain, 0, 0);
        builder = builder.changeVertexes(v -> v.add(startVertex));

        int maxIndex = 0;
        IVertex maxVertex = null;
        ISegment maxSegment = null;

        int endIndex = 1;

        while (endIndex < builder.getPixelCount()) {
            var vertexIndex = builder.getVertexCount();
            builder = builder.changeVertexes(v -> v.add(null));
            var segmentIndex = builder.getSegmentCount();
            builder = builder.changeSegments(s -> s.add(null));

            for (int index = endIndex; index < builder.getPixelCount(); index++) {
                var candidateVertex = vertexService.createVertex(pixelMap, builder, vertexIndex, index);
                builder = builder.changeVertexes(v -> v.set(vertexIndex, candidateVertex));
                var candidateSegment = SegmentFactory.createTempStraightSegment(pixelMap, builder, segmentIndex);
                builder = builder.changeSegments(s -> s.set(segmentIndex, candidateSegment));

                if (candidateSegment.noPixelFurtherThan(pixelMap, builder, tolerance)) {
                    maxIndex = index;
                    maxVertex = candidateVertex;
                    maxSegment = candidateSegment;
                    continue;
                }
                break;
            }

            builder = builder.setVertex(maxVertex);
            builder = builder.setSegment(maxSegment);
            endIndex = maxIndex + 1;
        }
        return PixelChain.of(builder);
    }

    public PixelChain approximate02_refineCorners(@NotNull PixelMapData pixelMap, @NotNull PixelChain pixelChain) {
        if (pixelChain.getSegmentCount() <= 1) {
            return pixelChain;
        }

        PixelChain result = PixelChain.of(pixelChain);
        // the for loop means that I am processing the current state of the builder, not the 'old' stream state
        // this is important as the builder is being mutated.
        for (int i = 0; i < pixelChain.getSegmentCount() - 1; i++) { // do not process last segment
            var segment = pixelChain.getSegment(i);

            var firstSegmentIndex = segment.getSegmentIndex();
            var secondSegmentIndex = firstSegmentIndex + 1;
            var joinPixelIndex = segment.getEndIndex(pixelChain);


            //TODO can probably remove these [] here as the lambdas have gone
            IVertex[] joinVertex = new IVertex[]{pixelChain.getVertex(secondSegmentIndex)};
            ISegment[] firstSegment = new ISegment[]{segment};
            ISegment[] secondSegment = new ISegment[]{pixelChain.getSegment(secondSegmentIndex)};

            var minPixelIndex = (segment.getStartVertex(pixelChain).getPixelIndex() + segment.getEndVertex(pixelChain).getPixelIndex()) / 2;
            var maxPixelIndex = (secondSegment[0].getStartVertex(pixelChain).getPixelIndex() + secondSegment[0].getEndVertex(pixelChain).getPixelIndex()) / 2;

            var currentError = segment.calcError(pixelMap, pixelChain) + secondSegment[0].calcError(pixelMap, pixelChain);
            var best = new Tuple4<>(currentError, firstSegment[0], joinVertex[0], secondSegment[0]);

            pixelChain.getPegCounter().increase(IPixelChain.PegCounters.RefineCornersAttempted);
            // the check below is needed as some segments may only be one index pixelLength so generating a midpoint might generate an invalid segment
            if (minPixelIndex < joinPixelIndex && joinPixelIndex < maxPixelIndex) {
                var refined = false;
                for (int candidateIndex = minPixelIndex + 1; candidateIndex < maxPixelIndex; candidateIndex++) {
                    joinVertex[0] = vertexService.createVertex(pixelMap, pixelChain, secondSegmentIndex, candidateIndex);
                    result = result.setVertex(joinVertex[0]);
                    firstSegment[0] = SegmentFactory.createTempStraightSegment(pixelMap, result, firstSegmentIndex);
                    result = result.setSegment(firstSegment[0]);
                    secondSegment[0] = SegmentFactory.createTempStraightSegment(pixelMap, result, secondSegmentIndex);
                    result = result.setSegment(secondSegment[0]);

                    currentError = segment.calcError(pixelMap, result) + secondSegment[0].calcError(pixelMap, result);

                    if (currentError < best._1) {
                        best = new Tuple4<>(currentError, firstSegment[0], joinVertex[0], secondSegment[0]);
                        refined = true;
                    }
                }
                if (refined &&
                        // TODO not sure why there is this extra check here
                        best._2.getEndTangentVector(pixelMap, result)
                                .dot(best._4.getStartTangentVector(pixelMap, result))
                                < 0.5d
                ) {
                    pegCounterService.increase(IPixelChain.PegCounters.RefineCornersSuccessful);
                }
                var finalBest = best;
                result = result.setVertex(finalBest._3);
                result = result.setSegment(finalBest._2);
                result = result.setSegment(finalBest._4);
            }
        }
        return result;
    }

    public PixelChain refine01_matchCurves(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double lineCurvePreference) {
        if (pixelChain.getSegmentCount() == 1) {
            return pixelChain;
        }
        var result = StrongReference.of(pixelChain);
        pixelChain.streamSegments().forEach(segment -> {
            if (segment == pixelChain.getFirstSegment()) {
                result.update(r -> refine01FirstSegment(pixelMap, r, lineCurvePreference, segment));
            } else if (segment == pixelChain.getLastSegment()) {
                result.update(r -> refine01EndSegment(pixelMap, r, lineCurvePreference, segment));
            } else {
                result.update(r -> refine01MidSegment(pixelMap, r, lineCurvePreference, segment));
            }
        });
        return result.get();
    }

    public PixelChain refine01MidSegment(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double lineCurvePreference,
            ISegment currentSegment
    ) {
        var result = pixelChain;
        // get tangent at start and end
        // calculate intersection
        // what if they are parallel ? -- ignore as the initial estimate is not good enough
        // see if it is closer than the line
        // // method 1 - looking at blending the gradients
        var bestSegment = currentSegment;
        try {
            double lowestError = currentSegment.calcError(pixelMap, result);
            lowestError *= lineCurvePreference;
            Line startTangent = vertexService.calcTangent(pixelMap, result, currentSegment.getStartVertex(result));
            Line endTangent = vertexService.calcTangent(pixelMap, result, currentSegment.getEndVertex(result));

            if (startTangent != null && endTangent != null) {
                Point p1 = startTangent.intersect(endTangent);
                if (p1 != null && startTangent.closestLambda(p1) > 0.0d && endTangent.closestLambda(p1) < 0.0d) {
                    Line newStartTangent = new Line(p1, currentSegment.getStartVertex(result).getPosition());
                    Line newEndTangent = new Line(p1, currentSegment.getEndVertex(result).getPosition());
                    p1 = newStartTangent.intersect(newEndTangent);
                    // if (p1 != null && newStartTangent.getAB().dot(startTangent.getAB()) > 0.0d && newEndTangent.getAB().dot(endTangent.getAB()) > 0.0d) {
                    ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, result, currentSegment.getSegmentIndex(), p1);
                    if (candidateSegment != null) {
                        double candidateError = candidateSegment.calcError(pixelMap, result);
                        if (isValid(pixelMap, result, candidateSegment) && candidateError < lowestError) {
                            lowestError = candidateError;
                            bestSegment = candidateSegment;
                        }
                    }
                }
            }
        } catch (Exception pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        }
        return result.setSegment(bestSegment);
    }

    public PixelChain refine03MidSegment(
            @NotNull PixelMapData pixelMap,
            @NotNull IPixelChain pixelChain,
            double lineCurvePreference,
            ISegment currentSegment
    ) {
        // instrument
        // Assumption that we are only going to smooth forward
        // i.e. we are not going to move the start point - not sure that this will remain true forever
        // and we will match the final gradient of the last segment
        //
        // If the next segment is a straight line then we can eat half of it
        return refine03MidSegmentEatForward(pixelMap, pixelChain, lineCurvePreference, currentSegment);
        //
        // If the next segment is a curve then
        //  1) try matching with a curve
        //  2) try eating it up to half
        //  2) try matching with a double curve
        //
        // Question 1 what are we going to do with fixed points
    }

    public PixelChain refine03MidSegmentEatForward(
            @NotNull PixelMapData pixelMap,
            @NotNull IPixelChain pixelChain,
            double lineCurvePreference,
            ISegment currentSegment
    ) {
        var originalNextSegment = currentSegment.getNextSegment(pixelChain);
        if (currentSegment instanceof CurveSegment && originalNextSegment instanceof CurveSegment) {
            return PixelChain.of(pixelChain);
        }
        var bestCandidateSegment = currentSegment;
        var bestCandidateVertex = currentSegment.getEndVertex(pixelChain);
        var originalEndVertex = currentSegment.getEndVertex(pixelChain);
        var result = PixelChain.of(pixelChain);

        try {
            pegCounterService.increase(IPixelChain.PegCounters.MidSegmentEatForwardAttempted);
            var nextSegmentPixelLength = originalNextSegment.getPixelLength(result);
            var controlPointEnd = originalEndVertex.getPosition()
                    .add(
                            originalNextSegment.getStartTangent(pixelMap, result)
                                    .getAB()
                                    .normalize()
                                    .multiply(currentSegment.getLength(pixelMap, result)
                                    )
                    );
            var length = currentSegment.getLength(pixelMap, result) / originalNextSegment.getLength(pixelMap, result);
            controlPointEnd = originalNextSegment.getPointFromLambda(pixelMap, result, -length);
            for (int i = nextSegmentPixelLength / 2; i >= 0; i--) {
                result = result.setVertex(originalEndVertex);
                result = result.setSegment(currentSegment);
                result = result.setSegment(originalNextSegment);
                var lowestErrorPerPixel = calcError(
                        pixelMap,
                        result,
                        currentSegment.getStartIndex(result),
                        currentSegment.getEndIndex(result) + i,
                        currentSegment,
                        originalNextSegment
                );

                var lambda = (double) i / nextSegmentPixelLength;
                var controlPointStart = originalNextSegment.getPointFromLambda(pixelMap, result, lambda);
                var candidateVertex = vertexService.createVertex(result, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
                result = result.setVertex(candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, result, currentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        result = result.setSegment(candidateSegment);
                        var candidateErrorPerPixel = calcError(
                                pixelMap,
                                result,
                                currentSegment.getStartIndex(result),
                                currentSegment.getEndIndex(result) + i,
                                candidateSegment,
                                originalNextSegment
                        );
                        if (isValid(pixelMap, result, candidateSegment) && candidateErrorPerPixel < lowestErrorPerPixel) {
                            lowestErrorPerPixel = candidateErrorPerPixel;
                            bestCandidateSegment = candidateSegment;
                            bestCandidateVertex = candidateVertex;
                        }
                    }
                }
            }
        } finally {
            if (bestCandidateSegment != currentSegment) {
                pegCounterService.increase(IPixelChain.PegCounters.MidSegmentEatForwardSuccessful);
            }
            result = result.setVertex(bestCandidateVertex);
            result = result.setSegment(bestCandidateSegment);
            // System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this)); // TODO
        }
        return result;
    }

    public double calcError(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            int startPixelIndex,
            int endPixelIndex,
            @NotNull ISegment startSegment,
            @NotNull ISegment endSegment
    ) {
        var error = 0d;
        for (var i = startPixelIndex; i <= endPixelIndex; i++) {
            var p = pixelChain.getPixel(i);
            if (startSegment.containsPixelIndex(pixelChain, i)) {
                var d = startSegment.calcError(pixelMap, pixelChain, p);
                error += d * d;
            } else if (endSegment.containsPixelIndex(pixelChain, i)) {
                var d = endSegment.calcError(pixelMap, pixelChain, p);
                error += d * d;
            } else {
                throw new IllegalArgumentException("Not in Range");
            }
        }
        return error;
    }

    // need to make sure that not only the pixels are close to the line but the line is close to the pixels
    public boolean isValid(@NotNull PixelMapData pixelMap,
                           @NotNull PixelChain pixelChain,
                           @NotNull ISegment segment) {
        if (segment == null) {
            return false;
        }
        if (segment.getPixelLength(pixelChain) < 4) {
            return true;
        }

        int startIndexPlus = segment.getStartIndex(pixelChain) + 1;
        Point startPointPlus = pixelChain.getPixel(startIndexPlus).getUHVWMidPoint(pixelMap.height());
        double startPlusLambda = segment.closestLambda(pixelMap, pixelChain, startPointPlus);

        int endIndexMinus = segment.getEndIndex(pixelChain) - 1;
        Point endPointMinus = pixelChain.getPixel(endIndexMinus).getUHVWMidPoint(pixelMap.width());
        double endMinusLambda = segment.closestLambda(pixelMap, pixelChain, endPointMinus);

        return startPlusLambda < 0.5d && endMinusLambda > 0.5d;
    }

    public PixelChain refine01EndSegment(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double lineCurvePreference,
            @NotNull ISegment currentSegment
    ) {
        var result = pixelChain;
        var bestSegment = currentSegment;

        try {
            double lowestError = currentSegment.calcError(pixelMap, result);
            lowestError *= lineCurvePreference;
            // calculate start tangent
            Line tangent = vertexService.calcTangent(pixelMap, result, currentSegment.getStartVertex(result));
            Point closest = tangent.closestPoint(currentSegment.getEndUHVWPoint(pixelMap, result));
            // divide this line (tangentRuler) into the number of pixels in the segment
            // for each of the points on the division find the lowest error
            Line tangentRuler = new Line(currentSegment.getStartUHVWPoint(pixelMap, result), closest);
            for (int i = 1; i < currentSegment.getPixelLength(result); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                try {
                    double lambda = (double) i / currentSegment.getPixelLength(result);
                    Point p1 = tangentRuler.getPoint(lambda);
                    ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, result, currentSegment.getSegmentIndex(), p1);
                    if (candidateSegment != null) {
                        double candidateError = candidateSegment.calcError(pixelMap, result);

                        if (isValid(pixelMap, result, candidateSegment) && candidateError < lowestError) {
                            lowestError = candidateError;
                            bestSegment = candidateSegment;
                        }
                    }
                } catch (Exception pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                }
            }
        } catch (Exception pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        }
        return result.setSegment(bestSegment);
    }

    public PixelChain refine01FirstSegment(
            PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double lineCurvePreference,
            ISegment segment
    ) {
        var result = pixelChain;
        var bestSegment = segment;
        pegCounterService.increase(IPixelChain.PegCounters.refine01FirstSegmentAttempted);
        try {
            // get error values from straight line to start the compare
            double lowestError = segment.calcError(pixelMap, result);
            lowestError *= lineCurvePreference;
            // calculate end tangent
            Line tangent = vertexService.calcTangent(pixelMap, result, segment.getEndVertex(result));


            // find closest point between start point and tangent line
            Point closest = tangent.closestPoint(segment.getStartUHVWPoint(pixelMap, result));
            // divide this line (tangentRuler) into the number of pixels in the segment
            // for each of the points on the division find the lowest error
            LineSegment tangentRuler = new LineSegment(closest, segment.getEndUHVWPoint(pixelMap, result));
            for (int i = 1; i < segment.getPixelLength(result); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                try {
                    double lambda = (double) i / segment.getPixelLength(result);
                    Point p1 = tangentRuler.getPoint(lambda);
                    ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, result, segment.getSegmentIndex(), p1);
                    if (candidateSegment == null) {
                        continue;
                    }
                    result = result.setSegment(candidateSegment);
                    double candidateError = candidateSegment != null ? candidateSegment.calcError(pixelMap, result) : 0.0d;

                    if (isValid(pixelMap, result, candidateSegment) && candidateError < lowestError) {
                        lowestError = candidateError;
                        bestSegment = candidateSegment;
                    }

                } catch (Exception pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                }
            }
            if (bestSegment != segment) {
                pegCounterService.increase(IPixelChain.PegCounters.refine01FirstSegmentSuccessful);
            }
        } catch (Exception pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        }
        return result.setSegment(bestSegment);
    }

    public PixelChain approximateCurvesOnly_subsequentSegments(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference
    ) {
        var startVertex = pixelChain.getLastVertex();
        var startPixelIndex = pixelChain.getLastVertex().getPixelIndex() + 1;
        var vertexIndex = pixelChain.getVertexCount();
        var segmentIndex = pixelChain.getSegmentCount();
        var best = new StrongReference<Tuple2<ISegment, IVertex>>(null);
        var result = StrongReference.of(pixelChain);
        result.update(r -> r.changeVertexes(v -> v.add(null)));
        result.update(r -> r.changeSegments(s -> s.add(null)));
        Tuple3<Integer, ISegment, IVertex> bestFit;

        for (int i = startPixelIndex; i < result.get().getPixelCount(); i++) {
            try {
                var candidateVertex = vertexService.createVertex(pixelMap, result.get(), vertexIndex, i);
                var lt3 = vertexService.calcLocalTangent(pixelMap, result.get(), candidateVertex, 3);
                var startTangent = vertexService.getStartSegment( result.get(), startVertex).getEndTangent(pixelMap, result.get());
                var p = lt3.intersect(startTangent);
                if (p != null) {
                    result.update(r -> r.setVertex(candidateVertex));
                    SegmentFactory.createOptionalTempCurveSegmentTowards(pixelMap, result.get(), segmentIndex, p)
                            .filter(s -> s.noPixelFurtherThan(pixelMap, result.get(), tolerance * lineCurvePreference))
                            .filter(s -> segmentMidpointValid(pixelMap, result.get(), s, tolerance * lineCurvePreference))
                            .ifPresent(s -> best.set(new Tuple2<>(s, candidateVertex)));
                }
            } catch (Exception pT) {
                mLogger.info(pT::getMessage);
                mLogger.info(pT::toString);
            }
            if (best.get() != null && best.get()._2.getPixelIndex() - 15 > i) {
                break;
            }
        }
        if (best.get() != null) {
            result.update(r -> r.setSegment(best.get()._1));
            result.update(r -> r.setVertex(best.get()._2));
            if (best.get()._1 == null || result.get().getPixelCount() - startPixelIndex == 1) {
                result.update(r -> r.setSegment(SegmentFactory.createTempStraightSegment(pixelMap, result.get(), segmentIndex)));
            }
        } else {
            result.update(r -> r.setVertex(vertexService.createVertex(pixelMap, r, vertexIndex, r.getMaxPixelIndex())));
            result.update(r -> r.setSegment(SegmentFactory.createTempStraightSegment(pixelMap, r, segmentIndex)));
        }
        return result.get();
    }


    public boolean segmentMidpointValid(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            @NotNull CurveSegment segment,
            double distance) {
        Point curveMidPoint = segment.getPointFromLambda(pixelMap, pixelChain, 0.5d);
        return pixelChain.getPixels().stream()
                .anyMatch(p -> p.getUHVWMidPoint(pixelMap.height()).distance(curveMidPoint) < distance);
    }
}
