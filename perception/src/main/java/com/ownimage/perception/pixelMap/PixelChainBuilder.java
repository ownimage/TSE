package com.ownimage.perception.pixelMap;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.pixelMap.services.Services;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelChainBuilder implements IPixelChain {

    private final static Logger mLogger = Framework.getLogger();

    @Getter
    private ImmutableVectorClone<Pixel> mPixels;
    @Getter
    private ImmutableVectorClone<ISegment> mSegments;
    @Getter
    private ImmutableVectorClone<IVertex> mVertexes;
    @Getter
    private double mLength;
    @Getter
    private Thickness mThickness;

    private Services services = Services.getDefaultServices();

    public PixelChainBuilder(
            Collection<Pixel> pPixels,
            Collection<IVertex> pVertexes,
            Collection<ISegment> pSegments,
            double pLength,
            Thickness pThickness
    ) {
        mPixels = new ImmutableVectorClone<Pixel>().addAll(pPixels);
        mVertexes = new ImmutableVectorClone<IVertex>().addAll(pVertexes);
        mSegments = new ImmutableVectorClone<ISegment>().addAll(pSegments);
        mLength = pLength;
        mThickness = pThickness;
    }

    public PixelChainBuilder(@NotNull PixelChain pixelChain) {
        mPixels = pixelChain.getPixels();
        mVertexes = pixelChain.getVertexes();
        mSegments = pixelChain.getSegments();
        mLength = pixelChain.getLength();
        mThickness = pixelChain.getThickness();
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

    public PixelChainBuilder setThickness(@NonNull Thickness pThickness) {
        mThickness = pThickness;
        return this;
    }

    public PixelChain build() {
        return new PixelChain(
                new ImmutableVectorClone<Pixel>().addAll(mPixels.toVector()),
                new ImmutableVectorClone<ISegment>().addAll(mSegments.toVector()),
                new ImmutableVectorClone<IVertex>().addAll(mVertexes.toVector()),
                mLength,
                mThickness
        );
    }


    private void setVertex(IVertex pVertex) {
        changeVertexes(v -> v.set(pVertex.getVertexIndex(), pVertex));
    }

    private void setSegment(ISegment pSegment) {
        changeSegments(v -> v.set(pSegment.getSegmentIndex(), pSegment));
    }


    private void refine03_matchCurves(PixelMapData pPixelMap, PixelChain pPixelChain, double tolerance, double lineCurvePreference) {

        if (getSegmentCount() == 1) {
            return;
        }

        streamSegments().forEach(currentSegment -> {
            if (currentSegment == getFirstSegment()) {
                refine03FirstSegment(pPixelMap, pPixelChain, lineCurvePreference, currentSegment);
            } else if (currentSegment == getLastSegment()) {
                refine03LastSegment(pPixelMap, lineCurvePreference, currentSegment);
            } else {
                refine03MidSegment(pPixelMap, pPixelChain, lineCurvePreference, currentSegment);
            }
        });
    }

    private void refine03FirstSegment(
            PixelMapData pPixelMap,
            PixelChain pPixelChain,
            double lineCurvePreference,
            ISegment pCurrentSegment
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
            var lowestError = pCurrentSegment.calcError(pPixelMap, this) * 1000 * lineCurvePreference; // TODO
            var nextSegmentPixelLength = originalNextSegment.getPixelLength(this);
            var controlPointEnd = originalEndVertex.getPosition()
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
                var candidateVertex = services.getVertexService().createVertex(this, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
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
            PixelMapData pPixelMap,
            double lineCurvePreference,
            ISegment pCurrentSegment
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
            var lowestError = pCurrentSegment.calcError(pPixelMap, this) * 1000 * lineCurvePreference; // TODO
            var prevSegmentPixelLength = originalPrevSegment.getPixelLength(this);
            var controlPointEnd = originalStartVertex.getPosition()
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
                var candidateVertex = services.getVertexService().createVertex(this, originalStartVertex.getVertexIndex(), originalStartVertex.getPixelIndex() - i, controlPointStart);
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
            PixelMapData pPixelMap,
            IPixelChain pPixelChain, double lineCurvePreference,
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
            var controlPointEnd = originalEndVertex.getPosition()
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
                var candidateVertex = services.getVertexService().createVertex(this, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
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
            PixelMapData pPixelMap,
            IPixelChain pixelChain,
            double lineCurvePreference,
            ISegment pCurrentSegment
    ) {
        // instrument
        // Assumption that we are only going to smooth forward
        // i.e. we are not going to move the start point - not sure that this will remain true forever
        // and we will match the final gradient of the last segment
        //
        // If the next segment is a straight line then we can eat half of it
        refine03MidSegmentEatForward(pPixelMap, pixelChain, lineCurvePreference, pCurrentSegment);
        //
        // If the next segment is a curve then
        //  1) try matching with a curve
        //  2) try eating it up to half
        //  2) try matching with a double curve
        //
        // Question 1 what are we going to do with fixed points
    }

    private boolean isValid(PixelMapData pPixelMap, ISegment pSegment) { // need to make sure that not only the pixels are close to the line but the line is close to the pixels
        if (pSegment == null) {
            return false;
        }
        if (pSegment.getPixelLength(this) < 4) {
            return true;
        }

        int startIndexPlus = pSegment.getStartIndex(this) + 1;
        Point startPointPlus = getPixel(startIndexPlus).getUHVWMidPoint(pPixelMap.height());
        double startPlusLambda = pSegment.closestLambda(pPixelMap, this, startPointPlus);

        int endIndexMinus = pSegment.getEndIndex(this) - 1;
        Point endPointMinus = getPixel(endIndexMinus).getUHVWMidPoint(pPixelMap.width());
        double endMinusLambda = pSegment.closestLambda(pPixelMap, this, endPointMinus);

        return startPlusLambda < 0.5d && endMinusLambda > 0.5d;
    }

    private double calcError(
            PixelMapData pPixelMap,
            int pStartPixelIndex,
            int pEndPixelIndex,
            ISegment pStartSegment,
            ISegment pEndSegment
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

    public void approximate(PixelMapData pPixelMap, double pTolerance) {
        approximate01_straightLines(pPixelMap, pTolerance);
        approximate02_refineCorners(pPixelMap);
    }

    public void approximateCurvesOnly(
            PixelMapData pPixelMap,
            double pTolerance,
            double pLineCurvePreference
    ) {
        if (getPixelCount() <= 4) {
            return;
        }
        changeVertexes(ImmutableVectorClone::clear);
        changeSegments(ImmutableVectorClone::clear);
        approximateCurvesOnly_firstSegment(pPixelMap, pTolerance, pLineCurvePreference);
        while (getLastVertex().getPixelIndex() != getMaxPixelIndex()) {
            approximateCurvesOnly_subsequentSegments(pPixelMap, pTolerance, pLineCurvePreference);
        }
    }

    private void approximateCurvesOnly_subsequentSegments(
            PixelMapData pixelMap,
            double tolerance,
            double lineCurvePreference
    ) {
        var vertexService = services.getVertexService();

        val startVertex = getLastVertex();
        val startPixelIndex = getLastVertex().getPixelIndex() + 1;
        val vertexIndex = getVertexCount();
        val segmentIndex = getSegmentCount();
        val best = new StrongReference<Tuple2<ISegment, IVertex>>(null);
        changeVertexes(v -> v.add(null));
        changeSegments(s -> s.add(null));
        Tuple3<Integer, ISegment, IVertex> bestFit;

        for (int i = startPixelIndex; i < getPixelCount(); i++) {
            try {
                var candidateVertex = services.getVertexService().createVertex(pixelMap, this, vertexIndex, i);
                var lt3 = vertexService.calcLocalTangent(pixelMap, this, candidateVertex, 3);
                var startTangent = vertexService.getStartSegment( this, startVertex).getEndTangent(pixelMap, this);
                var p = lt3.intersect(startTangent);
                if (p != null) {
                    setVertex(candidateVertex);
                    SegmentFactory.createOptionalTempCurveSegmentTowards(pixelMap, this, segmentIndex, p)
                            .filter(s -> s.noPixelFurtherThan(pixelMap, this, tolerance * lineCurvePreference))
                            .filter(s -> segmentMidpointValid(pixelMap, s, tolerance * lineCurvePreference))
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
            setSegment(best.get()._1);
            setVertex(best.get()._2);
            if (best.get()._1 == null || getPixelCount() - startPixelIndex == 1) {
                setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, segmentIndex));
            }
        } else {
            setVertex(vertexService.createVertex(pixelMap, this, vertexIndex, getMaxPixelIndex()));
            setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, segmentIndex));
        }
    }

    private boolean segmentMidpointValid(PixelMapData pPixelMap, CurveSegment pSegment, double pDistance) {
        Point curveMidPoint = pSegment.getPointFromLambda(pPixelMap, this, 0.5d);
        return mPixels.stream().anyMatch(p -> p.getUHVWMidPoint(pPixelMap.height()).distance(curveMidPoint) < pDistance);
    }

    private void approximateCurvesOnly_firstSegment(
            PixelMapData pixelMap,
            double tolerance,
            double lineCurvePreference
    ) {
        var vertexService = services.getVertexService();

        changeVertexes(v -> v.add(services.getVertexService().createVertex(pixelMap, this, 0, 0)));
        val vertexIndex = getVertexCount();
        val segmentIndex = getSegmentCount();
        val best = new StrongReference<Tuple2<ISegment, IVertex>>(null);
        changeVertexes(v -> v.add(null));
        changeSegments(s -> s.add(null));
        Tuple3<Integer, ISegment, IVertex> bestFit;

        for (int i = 4; i < getPixelCount(); i++) {
            try {
                val candidateVertex = services.getVertexService().createVertex(pixelMap, this, vertexIndex, i);
                val lineAB = new Line(getUHVWPoint(pixelMap, 0), getUHVWPoint(pixelMap, i));
                var lt3 = vertexService.calcLocalTangent(pixelMap, this, candidateVertex, 3);
                val pointL = lineAB.getPoint(0.25d);
                val pointN = lineAB.getPoint(0.75d);
                val normal = lineAB.getANormal();
                val lineL = new Line(pointL, pointL.add(normal));
                val lineN = new Line(pointN, pointN.add(normal));
                var pointC = lt3.intersect(lineL);
                var pointE = lt3.intersect(lineN);
                if (pointC != null && pointE != null) {
                    var lineCE = new Line(pointC, pointE);
                    setVertex(candidateVertex);
                    lineCE.streamFromCenter(20)
                            .map(p -> SegmentFactory.createOptionalTempCurveSegmentTowards(pixelMap, this, segmentIndex, p))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(s -> s.noPixelFurtherThan(pixelMap, this, tolerance * lineCurvePreference))
                            .findFirst()
                            .ifPresent(s -> best.set(new Tuple2<>(s, candidateVertex)));
                }
            } catch (Exception pT) {
            }
            if (best.get() != null && best.get()._2.getPixelIndex() - 15 > i) {
                break;
            }
        }
        if (best.get() != null) {
            setSegment(best.get()._1);
            setVertex(best.get()._2);
            if (getSegment(0) == null) {
                setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, 0));
            }
        } else {
            setVertex(services.getVertexService().createVertex(pixelMap, this, vertexIndex, getMaxPixelIndex()));
            setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, segmentIndex));
        }
    }

    private void approximate01_straightLines(PixelMapData pPixelMap, double pTolerance) {
        // note that this is version will find the longest line that is close to all pixels.
        // there are cases where a line of pixelLength n will be close enough, a line of pixelLength n+1 will not be, but there exists an m such that a line of pixelLength m is close enough.
        if (getPixelCount() <= 1) {
            return;
        }

        changeSegments(ImmutableVectorClone::clear);
        changeVertexes(ImmutableVectorClone::clear);

        var startVertex = services.getVertexService().createVertex(pPixelMap, this, 0, 0);
        changeVertexes(v -> v.add(startVertex));

        int maxIndex = 0;
        IVertex maxVertex = null;
        ISegment maxSegment = null;

        int endIndex = 1;

        while (endIndex < getPixelCount()) {
            var vertexIndex = getVertexCount();
            changeVertexes(v -> v.add(null));
            var segmentIndex = getSegmentCount();
            changeSegments(s -> s.add(null));

            for (int index = endIndex; index < getPixelCount(); index++) {
                var candidateVertex = services.getVertexService().createVertex(pPixelMap, this, vertexIndex, index);
                changeVertexes(v -> v.set(vertexIndex, candidateVertex));
                var candidateSegment = SegmentFactory.createTempStraightSegment(pPixelMap, this, segmentIndex);
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

    private void approximate02_refineCorners(PixelMapData pPixelMap) {
        if (getSegmentCount() <= 1) {
            return;
        }
        // the for loop means that I am processing the current state of the builder, not the 'old' stream state
        // this is important as the builder is being mutated.
        for (int i = 0; i < getSegmentCount() - 1; i++) { // do not process last segment
            var segment = getSegment(i);

            var firstSegmentIndex = segment.getSegmentIndex();
            var secondSegmentIndex = firstSegmentIndex + 1;
            var joinPixelIndex = segment.getEndIndex(this);


            //TODO can probably remove these [] here as the lambdas have gone
            IVertex[] joinVertex = new IVertex[]{getVertex(secondSegmentIndex)};
            ISegment[] firstSegment = new ISegment[]{segment};
            ISegment[] secondSegment = new ISegment[]{getSegment(secondSegmentIndex)};

            var minPixelIndex = (segment.getStartVertex(this).getPixelIndex() + segment.getEndVertex(this).getPixelIndex()) / 2;
            var maxPixelIndex = (secondSegment[0].getStartVertex(this).getPixelIndex() + secondSegment[0].getEndVertex(this).getPixelIndex()) / 2;

            var currentError = segment.calcError(pPixelMap, this) + secondSegment[0].calcError(pPixelMap, this);
            var best = new Tuple4<>(currentError, firstSegment[0], joinVertex[0], secondSegment[0]);

            getPegCounter().increase(IPixelChain.PegCounters.RefineCornersAttempted);
            // the check below is needed as some segments may only be one index pixelLength so generating a midpoint might generate an invalid segment
            if (minPixelIndex < joinPixelIndex && joinPixelIndex < maxPixelIndex) {
                var refined = false;
                for (int candidateIndex = minPixelIndex + 1; candidateIndex < maxPixelIndex; candidateIndex++) {
                    joinVertex[0] = services.getVertexService().createVertex(pPixelMap, this, secondSegmentIndex, candidateIndex);
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
                var finalBest = best;
                setVertex(finalBest._3);
                setSegment(finalBest._2);
                setSegment(finalBest._4);
            }
        }
    }

    public void refine(
            @NotNull PixelMapData pixelMap,
            PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference) {
        // TODO dont really want to have to pass a IMPTS in here
        refine01_matchCurves(pixelMap, pixelChain, tolerance, lineCurvePreference);
        refine03_matchCurves(pixelMap, pixelChain, tolerance, lineCurvePreference);
//        var tolerance = pTransformSource.getLineTolerance() / pTransformSource.getHeight();
//        var lineCurvePreference = pTransformSource.getLineCurvePreference();
//        approximateCurvesOnly(pixelMap, tolerance, lineCurvePreference);
    }

    private void refine01_matchCurves(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference) {

        if (getSegmentCount() == 1) {
            return;
        }
        streamSegments().forEach(segment -> {
            var index = segment.getSegmentIndex();
            if (segment == getFirstSegment()) { // TODO remove IPMTS
                refine01FirstSegment(pixelMap, lineCurvePreference, segment);
            } else if (segment == getLastSegment()) {
                refine01EndSegment(pixelMap, pixelChain, lineCurvePreference, segment);
            } else {
                refine01MidSegment(pixelMap, pixelChain, lineCurvePreference, segment);
            }
        });
    }

    private void refine01MidSegment(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double lineCurvePreference,
            ISegment currentSegment
    ) {

        // get tangent at start and end
        // calculate intersection
        // what if they are parallel ? -- ignore as the initial estimate is not good enough
        // see if it is closer than the line
        // // method 1 - looking at blending the gradients
        ISegment bestSegment = currentSegment;
        try {
            double lowestError = currentSegment.calcError(pixelMap, this);
            lowestError *= lineCurvePreference;
            Line startTangent = services.getVertexService().calcTangent(pixelMap, this, currentSegment.getStartVertex(this));
            Line endTangent = services.getVertexService().calcTangent(pixelMap, this, currentSegment.getEndVertex(this));

            if (startTangent != null && endTangent != null) {
                Point p1 = startTangent.intersect(endTangent);
                if (p1 != null && startTangent.closestLambda(p1) > 0.0d && endTangent.closestLambda(p1) < 0.0d) {
                    Line newStartTangent = new Line(p1, currentSegment.getStartVertex(this).getPosition());
                    Line newEndTangent = new Line(p1, currentSegment.getEndVertex(this).getPosition());
                    p1 = newStartTangent.intersect(newEndTangent);
                    // if (p1 != null && newStartTangent.getAB().dot(startTangent.getAB()) > 0.0d && newEndTangent.getAB().dot(endTangent.getAB()) > 0.0d) {
                    ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, this, currentSegment.getSegmentIndex(), p1);
                    double candidateError = candidateSegment.calcError(pixelMap, this);

                    if (isValid(pixelMap, candidateSegment) && candidateError < lowestError) {
                        lowestError = candidateError;
                        bestSegment = candidateSegment;
                    }
                }

            }
        } catch (Exception pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        } finally {
            setSegment(bestSegment);
        }
    }

    private void refine01EndSegment(
            @NotNull PixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            double lineCurvePreference,
            @NotNull ISegment currentSegment
    ) {
        ISegment bestSegment = currentSegment;

        try {
            double lowestError = currentSegment.calcError(pixelMap, this);
            lowestError *= lineCurvePreference;
            // calculate start tangent
            Line tangent = services.getVertexService().calcTangent(pixelMap, this, currentSegment.getStartVertex(this));
            Point closest = tangent.closestPoint(currentSegment.getEndUHVWPoint(pixelMap, this));
            // divide this line (tangentRuler) into the number of pixels in the segment
            // for each of the points on the division find the lowest error
            Line tangentRuler = new Line(currentSegment.getStartUHVWPoint(pixelMap, this), closest);
            for (int i = 1; i < currentSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                try {
                    double lambda = (double) i / currentSegment.getPixelLength(this);
                    Point p1 = tangentRuler.getPoint(lambda);
                    ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, this, currentSegment.getSegmentIndex(), p1);
                    if (candidateSegment != null) {
                        double candidateError = candidateSegment.calcError(pixelMap, this);

                        if (isValid(pixelMap, candidateSegment) && candidateError < lowestError) {
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
        } finally {
            setSegment(bestSegment);
        }
    }

    private void refine01FirstSegment(
            PixelMapData pixelMap,
            double lineCurvePreference,
            ISegment segment
    ) {
        getPegCounter().increase(PegCounters.refine01FirstSegmentAttempted);
        var bestSegment = segment;
        try {
            // get error values from straight line to start the compare
            double lowestError = segment.calcError(pixelMap, this);
            lowestError *= lineCurvePreference;
            // calculate end tangent
            Line tangent = services.getVertexService().calcTangent(pixelMap, this, segment.getEndVertex(this));


            // find closest point between start point and tangent line
            Point closest = tangent.closestPoint(segment.getStartUHVWPoint(pixelMap, this));
            // divide this line (tangentRuler) into the number of pixels in the segment
            // for each of the points on the division find the lowest error
            LineSegment tangentRuler = new LineSegment(closest, segment.getEndUHVWPoint(pixelMap, this));
            for (int i = 1; i < segment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                try {
                    double lambda = (double) i / segment.getPixelLength(this);
                    Point p1 = tangentRuler.getPoint(lambda);
                    ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pixelMap, this, segment.getSegmentIndex(), p1);
                    if (candidateSegment == null) {
                        continue;
                    }
                    setSegment(candidateSegment);
                    double candidateError = candidateSegment != null ? candidateSegment.calcError(pixelMap, this) : 0.0d;

                    if (isValid(pixelMap, candidateSegment) && candidateError < lowestError) {
                        lowestError = candidateError;
                        bestSegment = candidateSegment;
                    }

                } catch (Exception pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                }
            }
            if (bestSegment != segment) {
                getPegCounter().increase(PegCounters.refine01FirstSegmentSuccessful);
            }
        } catch (Exception pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        } finally {
            setSegment(bestSegment);
        }
    }

}

