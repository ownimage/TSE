package com.ownimage.perception.pixelMap;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.immutable.IImmutableVector;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import io.vavr.Tuple4;
import lombok.Getter;
import lombok.val;

import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelChainBuilder implements IPixelChain {

    public final static Logger mLogger = Framework.getLogger();

    @Getter private ImmutableVectorClone<Pixel> mPixels;
    @Getter private ImmutableVectorClone<ISegment> mSegments;
    @Getter private ImmutableVectorClone<IVertex> mVertexes;
    @Getter private double mLength;
    @Getter private PixelChain.Thickness mThickness;

    public PixelChainBuilder(
            ImmutableVectorClone<Pixel> pPixels,
            ImmutableVectorClone<IVertex> pVertexes,
            ImmutableVectorClone<ISegment> pSegments,
            double pLength,
            PixelChain.Thickness pThickness
    ) {
        mPixels = pPixels;
        mVertexes = pVertexes;
        mSegments = pSegments;
        mLength = pLength;
        mThickness = pThickness;
    }

    public PixelChainBuilder changePixels(Function<ImmutableVectorClone<Pixel>, ImmutableVectorClone<Pixel>> pFn) {
        mPixels = pFn.apply(mPixels);
        return this;
    }

    public PixelChainBuilder changeVertexes(Function<ImmutableVectorClone<IVertex>, ImmutableVectorClone<IVertex>> pFn) {
        mVertexes = pFn.apply(mVertexes);
        return this;
    }

    public PixelChainBuilder changeSegments(Function<ImmutableVectorClone<ISegment>, ImmutableVectorClone<ISegment>> pFn) {
        mSegments = pFn.apply(mSegments);
        return this;
    }

    public PixelChainBuilder setLength(double pLength) {
        mLength = pLength;
        return this;
    }

    public PixelChainBuilder setThickness(PixelChain.Thickness pThickness) {
        Framework.checkParameterNotNull(mLogger, pThickness, "pThickness");
        mThickness = pThickness;
        return this;
    }

    public PixelChain build(final PixelMap pPixelMap) {
        return new PixelChain(pPixelMap, mPixels, mSegments, mVertexes, mLength, mThickness);
    }


    public void setVertex(final IVertex pVertex) {
        changeVertexes(v -> v.set(pVertex.getVertexIndex(), pVertex));
    }

    public void setSegment(final ISegment pSegment) {
        changeSegments(v -> v.set(pSegment.getSegmentIndex(), pSegment));
    }


    public void refine03_matchCurves(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {

        if (getSegmentCount() == 1) {
            return;
        }

        streamSegments().forEach(currentSegment -> {
            if (currentSegment == getFirstSegment()) {
                refine03FirstSegment(pPixelMap, pSource, currentSegment);
            } else if (currentSegment == getLastSegment()) {
                refine03LastSegment(pPixelMap, pSource, currentSegment);
            } else {
                refine03MidSegment(pPixelMap, pSource, currentSegment);
            }
        });
    }

    private void refine03FirstSegment
            (
                    PixelMap pPixelMap,
                    final IPixelMapTransformSource pSource,
                    final ISegment pCurrentSegment
            ) {
        var bestCandidateSegment = pCurrentSegment;
        var bestCandidateVertex = pCurrentSegment.getEndVertex(this);
        var originalNextSegment = pCurrentSegment.getNextSegment(this);
        var originalEndVertex = pCurrentSegment.getEndVertex(this);

        // this only works if this or the next segment are straight
        if (!(
                (pCurrentSegment instanceof StraightSegment) || (originalNextSegment instanceof StraightSegment)
        )) {
            return;
        }

        try {
            getPegCounter().increase(IPixelChain.PegCounters.StartSegmentStraightToCurveAttempted);
            var lowestError = pCurrentSegment.calcError(pPixelMap, this) * 1000 * pSource.getLineCurvePreference(); // TODO
            var nextSegmentPixelLength = originalNextSegment.getPixelLength(this);
            var controlPointEnd = originalEndVertex.getUHVWPoint(pPixelMap, this)
                    .add(
                            originalNextSegment.getStartTangent(pPixelMap, this)
                                    .getAB()
                                    .normalize()
                                    .multiply(pCurrentSegment.getLength(pPixelMap, this)
                                    )
                    );
            var length = pCurrentSegment.getLength(pPixelMap, this) / originalNextSegment.getLength(pPixelMap, this);
            controlPointEnd = originalNextSegment.getPointFromLambda(pPixelMap, this, -length);
            for (int i = nextSegmentPixelLength / 2; i >= 0; i--) {
                changeVertexes(v -> v.set(originalEndVertex.getVertexIndex(), originalEndVertex));
                var lambda = (double) i / nextSegmentPixelLength;
                var controlPointStart = originalNextSegment.getPointFromLambda(pPixelMap, this, lambda);
                var candidateVertex = Vertex.createVertex(this, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
                changeVertexes(v -> v.set(candidateVertex.getVertexIndex(), candidateVertex));
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        setSegment(candidateSegment);
                        var candidateError = candidateSegment.calcError(pPixelMap, this);

                        if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                            lowestError = candidateError;
                            bestCandidateSegment = candidateSegment;
                            bestCandidateVertex = candidateVertex;
                        }
                    }
                }
            }
        } finally {
            if (bestCandidateSegment != pCurrentSegment) {
                getPegCounter().increase(IPixelChain.PegCounters.StartSegmentStraightToCurveSuccessful);
            }
            setVertex(bestCandidateVertex);
            setSegment(bestCandidateSegment);
            // System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this)); // TODO
        }
    }

    private void refine03LastSegment(
            PixelMap pPixelMap,
            final IPixelMapTransformSource pSource,
            final ISegment pCurrentSegment
    ) {
        var bestCandidateSegment = pCurrentSegment;
        var bestCandidateVertex = pCurrentSegment.getEndVertex(this);
        var originalPrevSegment = pCurrentSegment.getPreviousSegment(this);
        var originalStartVertex = pCurrentSegment.getStartVertex(this);

        // this only works if this or the previous segment are straight
        if (!(
                (pCurrentSegment instanceof StraightSegment) || (originalPrevSegment instanceof StraightSegment)
        )) {
            return;
        }

        try {
            getPegCounter().increase(IPixelChain.PegCounters.StartSegmentStraightToCurveAttempted);
            var lowestError = pCurrentSegment.calcError(pPixelMap, this) * 1000 * pSource.getLineCurvePreference(); // TODO
            var prevSegmentPixelLength = originalPrevSegment.getPixelLength(this);
            var controlPointEnd = originalStartVertex.getUHVWPoint(pPixelMap, this)
                    .add(
                            originalPrevSegment.getEndTangent(pPixelMap, this)
                                    .getAB()
                                    .normalize()
                                    .multiply(pCurrentSegment.getLength(pPixelMap, this)
                                    )
                    );
            var length = pCurrentSegment.getLength(pPixelMap, this) / originalPrevSegment.getLength(pPixelMap, this);
            controlPointEnd = originalPrevSegment.getPointFromLambda(pPixelMap, this, 1.0d + length);
            for (int i = (prevSegmentPixelLength / 2) - 1; i >= 0; i--) {
                setVertex(originalStartVertex);
                var lambda = 1.0d - (double) i / prevSegmentPixelLength; // TODO
                var controlPointStart = originalPrevSegment.getPointFromLambda(pPixelMap, this, lambda);
                var candidateVertex = Vertex.createVertex(this, originalStartVertex.getVertexIndex(), originalStartVertex.getPixelIndex() - i, controlPointStart);
                setVertex(candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                // TODO below should refactor this
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        setSegment(candidateSegment);
                        var candidateError = candidateSegment.calcError(pPixelMap, this);

                        if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                            lowestError = candidateError;
                            bestCandidateSegment = candidateSegment;
                            bestCandidateVertex = candidateVertex;
                        }
                    }
                }
            }
        } finally {
            if (bestCandidateSegment != pCurrentSegment) {
                getPegCounter().increase(IPixelChain.PegCounters.StartSegmentStraightToCurveSuccessful);
            }
            setVertex(bestCandidateVertex);
            setSegment(bestCandidateSegment);
            // TODO System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this));
        }
    }

    private void refine03MidSegmentEatForward(
            PixelMap pPixelMap,
            IPixelMapTransformSource pSource,
            ISegment pCurrentSegment
    ) {
        var bestCandidateSegment = pCurrentSegment;
        var bestCandidateVertex = pCurrentSegment.getEndVertex(this);
        var originalNextSegment = pCurrentSegment.getNextSegment(this);
        var originalEndVertex = pCurrentSegment.getEndVertex(this);

        if (pCurrentSegment instanceof CurveSegment && originalNextSegment instanceof CurveSegment) {
            return;
        }

        try {
            getPegCounter().increase(IPixelChain.PegCounters.MidSegmentEatForwardAttempted);
            var nextSegmentPixelLength = originalNextSegment.getPixelLength(this);
            var controlPointEnd = originalEndVertex.getUHVWPoint(pPixelMap, this)
                    .add(
                            originalNextSegment.getStartTangent(pPixelMap, this)
                                    .getAB()
                                    .normalize()
                                    .multiply(pCurrentSegment.getLength(pPixelMap, this)
                                    )
                    );
            var length = pCurrentSegment.getLength(pPixelMap, this) / originalNextSegment.getLength(pPixelMap, this);
            controlPointEnd = originalNextSegment.getPointFromLambda(pPixelMap, this, -length);
            for (int i = nextSegmentPixelLength / 2; i >= 0; i--) {
                setVertex(originalEndVertex);
                setSegment(pCurrentSegment);
                setSegment(originalNextSegment);
                var lowestErrorPerPixel = calcError(
                        pPixelMap,
                        pCurrentSegment.getStartIndex(this),
                        pCurrentSegment.getEndIndex(this) + i,
                        pCurrentSegment,
                        originalNextSegment
                );

                var lambda = (double) i / nextSegmentPixelLength;
                var controlPointStart = originalNextSegment.getPointFromLambda(pPixelMap, this, lambda);
                var candidateVertex = Vertex.createVertex(this, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
                setVertex(candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        setSegment(candidateSegment);
                        var candidateErrorPerPixel = calcError(
                                pPixelMap,
                                pCurrentSegment.getStartIndex(this),
                                pCurrentSegment.getEndIndex(this) + i,
                                candidateSegment,
                                originalNextSegment
                        );
                        if (isValid(pPixelMap, candidateSegment) && candidateErrorPerPixel < lowestErrorPerPixel) {
                            lowestErrorPerPixel = candidateErrorPerPixel;
                            bestCandidateSegment = candidateSegment;
                            bestCandidateVertex = candidateVertex;
                        }
                    }
                }
            }
        } finally {
            if (bestCandidateSegment != pCurrentSegment) {
                getPegCounter().increase(IPixelChain.PegCounters.MidSegmentEatForwardSuccessful);
            }
            setVertex(bestCandidateVertex);
            setSegment(bestCandidateSegment);
            // System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this)); // TODO
        }
    }


    private void refine03MidSegment(
            PixelMap pPixelMap,
            final IPixelMapTransformSource pSource,
            final ISegment pCurrentSegment
    ) {
        // instrument
        // Assumption that we are only going to smooth forward
        // i.e. we are not going to move the start point - not sure that this will remain true forever
        // and we will match the final gradient of the last segment
        //
        // If the next segment is a straight line then we can eat half of it
        refine03MidSegmentEatForward(pPixelMap, pSource, pCurrentSegment);
        //
        // If the next segment is a curve then
        //  1) try matching with a curve
        //  2) try eating it up to half
        //  2) try matching with a double curve
        //
        // Question 1 what are we going to do with fixed points
    }

    private boolean isValid(final PixelMap pPixelMap, final ISegment pSegment) { // need to make sure that not only the pixels are close to the line but the line is close to the pixels
        if (pSegment == null) return false;
        if (pSegment.getPixelLength(this) < 4) return true;

        final int startIndexPlus = pSegment.getStartIndex(this) + 1;
        final Point startPointPlus = getPixel(startIndexPlus).getUHVWMidPoint(pPixelMap);
        final double startPlusLambda = pSegment.closestLambda(startPointPlus, this, pPixelMap);

        final int endIndexMinus = pSegment.getEndIndex(this) - 1;
        final Point endPointMinus = getPixel(endIndexMinus).getUHVWMidPoint(pPixelMap);
        final double endMinusLambda = pSegment.closestLambda(endPointMinus, this, pPixelMap);

        return startPlusLambda < 0.5d && endMinusLambda > 0.5d;
    }

    private double calcError(
            final PixelMap pPixelMap,
            final int pStartPixelIndex,
            final int pEndPixelIndex,
            final ISegment pStartSegment,
            final ISegment pEndSegment
    ) {
        var error = 0d;
        for (var i = pStartPixelIndex; i <= pEndPixelIndex; i++) {
            var p = this.getPixel(i);
            if (pStartSegment.containsPixelIndex(this, i)) {
                var d = pStartSegment.calcError(pPixelMap, this, p);
                error += d * d;
            } else if (pEndSegment.containsPixelIndex(this, i)) {
                var d = pEndSegment.calcError(pPixelMap, this, p);
                error += d * d;
            } else {
                throw new IllegalArgumentException("Not in Range");
            }
        }
        return error;
    }

    void approximate(final PixelMap pPixelMap, final double pTolerance) {
        approximate01_straightLines(pPixelMap, pTolerance);
        approximate02_refineCorners(pPixelMap);
    }


    private void approximate01_straightLines(final PixelMap pPixelMap, final double pTolerance) {
        // note that this is version will find the longest line that is close to all pixels.
        // there are cases where a line of length n will be close enough, a line of length n+1 will not be, but there exists an m such that a line of length m is close enough.
        if (getPixelCount() <= 1) {
            return;
        }

        changeSegments(s -> s.clear());
        changeVertexes(v -> v.clear());

        val startVertex = Vertex.createVertex(this, 0, 0);
        changeVertexes(v -> v.add(startVertex));

        int maxIndex = 0;
        IVertex maxVertex = null;
        ISegment maxSegment = null;

        int endIndex = 1;

        while (endIndex < getPixelCount()) {
            val vertexIndex = getVertexCount();
            changeVertexes(v -> v.add(null));
            val segmentIndex = getSegmentCount();
            changeSegments(s -> s.add(null));

            for (int index = endIndex; index < getPixelCount(); index++) {
                val candidateVertex = Vertex.createVertex(this, vertexIndex, index);
                changeVertexes(v -> v.set(vertexIndex, candidateVertex));
                val candidateSegment = SegmentFactory.createTempStraightSegment(pPixelMap, this, segmentIndex);
                changeSegments(s -> s.set(segmentIndex, candidateSegment));

                if (candidateSegment.noPixelFurtherThan(pPixelMap, this, pTolerance)) {
                    maxIndex = index;
                    maxVertex = candidateVertex;
                    maxSegment = candidateSegment;
                    continue;
                }
                break;
            }

            setVertex(maxVertex);
            setSegment(maxSegment);
            endIndex = maxIndex + 1;
        }
    }

    private void approximate02_refineCorners(final PixelMap pPixelMap) {
        if (getSegmentCount() <= 1) {
            return;
        }
        // the for loop means that I am processing the current state of the builder, not the 'old' stream state
        // this is important as the builder is being mutated.
        for (int i = 0; i < getSegmentCount() - 1; i++) { // do not process last segment
            var segment = getSegment(i);

            val firstSegmentIndex = segment.getSegmentIndex();
            val secondSegmentIndex = firstSegmentIndex + 1;
            val joinIndex = secondSegmentIndex;
            val joinPixelIndex = segment.getEndIndex(this);


            //TODO can probably remove these [] here as the lambdas have gone
            IVertex[] joinVertex = new IVertex[]{getVertex(joinIndex)};
            ISegment[] firstSegment = new ISegment[]{segment};
            ISegment[] secondSegment = new ISegment[]{getSegment(secondSegmentIndex)};

            val minPixelIndex = (segment.getStartVertex(this).getPixelIndex() + segment.getEndVertex(this).getPixelIndex()) / 2;
            val maxPixelIndex = (secondSegment[0].getStartVertex(this).getPixelIndex() + secondSegment[0].getEndVertex(this).getPixelIndex()) / 2;

            var currentError = segment.calcError(pPixelMap, this) + secondSegment[0].calcError(pPixelMap, this);
            var best = new Tuple4<>(currentError, firstSegment[0], joinVertex[0], secondSegment[0]);

            getPegCounter().increase(IPixelChain.PegCounters.RefineCornersAttempted);
            // the check below is needed as some segments may only be one index length so generating a midpoint might generate an invalid segment
            if (minPixelIndex < joinPixelIndex && joinPixelIndex < maxPixelIndex) {
                var refined = false;
                for (int candidateIndex = minPixelIndex + 1; candidateIndex < maxPixelIndex; candidateIndex++) {
                    joinVertex[0] = Vertex.createVertex(this, joinIndex, candidateIndex);
                    setVertex(joinVertex[0]);
                    firstSegment[0] = SegmentFactory.createTempStraightSegment(pPixelMap, this, firstSegmentIndex);
                    setSegment(firstSegment[0]);
                    secondSegment[0] = SegmentFactory.createTempStraightSegment(pPixelMap, this, secondSegmentIndex);
                    setSegment(secondSegment[0]);

                    currentError = segment.calcError(pPixelMap, this) + secondSegment[0].calcError(pPixelMap, this);

                    if (currentError < best._1) {
                        best = new Tuple4<>(currentError, firstSegment[0], joinVertex[0], secondSegment[0]);
                        refined = true;
                    }
                }
                if (refined &&
                        // TODO not sure why there is this extra check here
                        best._2.getEndTangentVector(pPixelMap, this)
                                .dot(best._4.getStartTangentVector(pPixelMap, this))
                                < 0.5d
                ) {
                    getPegCounter().increase(IPixelChain.PegCounters.RefineCornersSuccessful);
                }
                val finalBest = best;
                setVertex(finalBest._3);
                setSegment(finalBest._2);
                setSegment(finalBest._4);
            }
        }
    }

    public void refine(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {
        // TODO dont really want to have to pass a IMPTS in here
        refine01_matchCurves(pPixelMap, pSource);
        refine03_matchCurves(pPixelMap, pSource);
    }

    private void refine01_matchCurves(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {

        if (getSegmentCount() == 1) {
            return;
        }
        streamSegments().forEach(segment -> {
            val index = segment.getSegmentIndex();
            if (segment == getFirstSegment()) {
                refine01FirstSegment(pPixelMap, pSource, segment);
            } else if (segment == getLastSegment()) {
                refine01EndSegment(pPixelMap, pSource, segment);
            } else {
                refine01MidSegment(pPixelMap, pSource, segment);
            }
        });
    }

    private void refine01MidSegment(
            PixelMap pPixelMap,
            final IPixelMapTransformSource pSource,
            final ISegment pCurrentSegment
    ) {
        // get tangent at start and end
        // calculate intersection
        // what if they are parallel ? -- ignore as the initial estimate is not good enough
        // see if it is closer than the line
        // // method 1 - looking at blending the gradients
        ISegment bestSegment = pCurrentSegment;
        try {
            double lowestError = pCurrentSegment.calcError(pPixelMap, this);
            lowestError *= pSource.getLineCurvePreference();
            final Line startTangent = pCurrentSegment.getStartVertex(this).calcTangent(this, pPixelMap);
            final Line endTangent = pCurrentSegment.getEndVertex(this).calcTangent(this, pPixelMap);

            if (startTangent != null && endTangent != null) {
                Point p1 = startTangent.intersect(endTangent);
                if (p1 != null && startTangent.closestLambda(p1) > 0.0d && endTangent.closestLambda(p1) < 0.0d) {
                    final Line newStartTangent = new Line(p1, pCurrentSegment.getStartVertex(this).getUHVWPoint(pPixelMap, this));
                    final Line newEndTangent = new Line(p1, pCurrentSegment.getEndVertex(this).getUHVWPoint(pPixelMap, this));
                    p1 = newStartTangent.intersect(newEndTangent);
                    // if (p1 != null && newStartTangent.getAB().dot(startTangent.getAB()) > 0.0d && newEndTangent.getAB().dot(endTangent.getAB()) > 0.0d) {
                    final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), p1);
                    final double candidateError = candidateSegment.calcError(pPixelMap, this);

                    if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                        lowestError = candidateError;
                        bestSegment = candidateSegment;
                    }
                }

            }
        } catch (final Throwable pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        } finally {
            setSegment(bestSegment);
        }
    }

    private void refine01EndSegment(
            PixelMap pPixelMap,
            final IPixelMapTransformSource pSource,
            final ISegment pCurrentSegment
    ) {
        ISegment bestSegment = pCurrentSegment;

        try {
            double lowestError = pCurrentSegment.calcError(pPixelMap, this);
            lowestError *= pSource.getLineCurvePreference();
            // calculate start tangent
            final Line tangent = pCurrentSegment.getStartVertex(this).calcTangent(this, pPixelMap);
            final Point closest = tangent.closestPoint(pCurrentSegment.getEndUHVWPoint(pPixelMap, this));
            // divide this line (tangentRuler) into the number of pixels in the segment
            // for each of the points on the division find the lowest error
            final Line tangentRuler = new Line(pCurrentSegment.getStartUHVWPoint(pPixelMap, this), closest);
            for (int i = 1; i < pCurrentSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                try {
                    final double lambda = (double) i / pCurrentSegment.getPixelLength(this);
                    final Point p1 = tangentRuler.getPoint(lambda);
                    final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), p1);
                    if (candidateSegment != null) {
                        final double candidateError = candidateSegment.calcError(pPixelMap, this);

                        if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                            lowestError = candidateError;
                            bestSegment = candidateSegment;
                        }
                    }
                } catch (final Throwable pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                }
            }
        } catch (final Throwable pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        } finally {
            setSegment(bestSegment);
        }
    }

    private void refine01FirstSegment(
            PixelMap pPixelMap,
            final IPixelMapTransformSource pSource,
            final ISegment pSegment
    ) {
        getPegCounter().increase(PegCounters.refine01FirstSegmentAttempted);
        var bestSegment = pSegment;
        try {
            // get error values from straight line to start the compare
            double lowestError = pSegment.calcError(pPixelMap, this);
            lowestError *= pSource.getLineCurvePreference();
            // calculate end tangent
            final Line tangent = pSegment.getEndVertex(this).calcTangent(this, pPixelMap);

            // find closest point between start point and tangent line
            final Point closest = tangent.closestPoint(pSegment.getStartUHVWPoint(pPixelMap, this));
            // divide this line (tangentRuler) into the number of pixels in the segment
            // for each of the points on the division find the lowest error
            final LineSegment tangentRuler = new LineSegment(closest, pSegment.getEndUHVWPoint(pPixelMap, this));
            for (int i = 1; i < pSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                try {
                    final double lambda = (double) i / pSegment.getPixelLength(this);
                    final Point p1 = tangentRuler.getPoint(lambda);
                    final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pSegment.getSegmentIndex(), p1);
                    if (candidateSegment == null) continue;
                    setSegment(candidateSegment);
                    final double candidateError = candidateSegment != null ? candidateSegment.calcError(pPixelMap, this) : 0.0d;

                    if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                        lowestError = candidateError;
                        bestSegment = candidateSegment;
                    }

                } catch (final Throwable pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                }
            }
            if (bestSegment != pSegment) {
                getPegCounter().increase(PegCounters.refine01FirstSegmentSuccessful);
            }
        } catch (final Throwable pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        } finally {
            setSegment(bestSegment);
        }
    }


}

