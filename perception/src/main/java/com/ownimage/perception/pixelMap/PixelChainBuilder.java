package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.PegCounter;
import com.ownimage.framework.util.immutable.IImmutableVector;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;

import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelChainBuilder implements IPixelChain {

    public final static Logger mLogger = Framework.getLogger();

    private ImmutableVectorClone<Pixel> mPixels;
    private ImmutableVectorClone<ISegment> mSegments;
    private ImmutableVectorClone<IVertex> mVertexes;
    private double mLength;
    private PixelChain.Thickness mThickness;

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

    @Override
    public double getLength() {
        return mLength;
    }

    @Override
    public PixelChain.Thickness getThickness() {
        return mThickness;
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

    public IImmutableVector<Pixel> getPixels() {
        return mPixels;
    }

    public IImmutableVector<ISegment> getSegments() {
        return mSegments;
    }

    public IImmutableVector<IVertex> getVertexes() {
        return mVertexes;
    }

    public PixelChain build(final PixelMap pPixelMap) {
        return new PixelChain(pPixelMap, mPixels, mSegments, mVertexes, mLength, mThickness);
    }


    public void setVertex(final IVertex pVertex) {
        changeVertexes(v->v.set(pVertex.getVertexIndex(), pVertex));
    }

    public void setSegment(final ISegment pSegment) {
        changeSegments(v->v.set(pSegment.getSegmentIndex(),pSegment));
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
                changeVertexes(v->v.set(originalEndVertex.getVertexIndex(), originalEndVertex));
                var lambda = (double) i / nextSegmentPixelLength;
                var controlPointStart = originalNextSegment.getPointFromLambda(pPixelMap, this, lambda);
                var candidateVertex = Vertex.createVertex(this, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
                changeVertexes(v->v.set(candidateVertex.getVertexIndex(), candidateVertex));
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
            setVertex( bestCandidateVertex);
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
}

