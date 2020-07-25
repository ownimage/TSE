package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.Services;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelChainBuilder implements IPixelChain {

    private final static Logger mLogger = Framework.getLogger();
    private static Services services = Services.getDefaultServices();
    private static PixelChainService pixelChainService = services.getPixelChainService();

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

    public PixelChainBuilder(@NotNull IPixelChain pixelChain) {
        mPixels = pixelChain.getPixels();
        mVertexes = pixelChain.getVertexes();
        mSegments = pixelChain.getSegments();
        mLength = pixelChain.getLength();
        mThickness = pixelChain.getThickness();
    }

     public PixelChain build() {
        return new PixelChain(
                mPixels,
                mSegments,
                mVertexes,
                mLength,
                mThickness
        );
    }


    public void refine03_matchCurves(PixelMapData pPixelMap, PixelChain pPixelChain, double tolerance, double lineCurvePreference) {

        if (getSegmentCount() == 1) {
            return;
        }
        streamSegments().forEach(currentSegment -> {
            if (currentSegment == getFirstSegment()) {
                setValuesFrom(pixelChainService.refine03FirstSegment(pPixelMap, this, lineCurvePreference, currentSegment));
            } else if (currentSegment == getLastSegment()) {
                setValuesFrom(pixelChainService.refine03LastSegment(pPixelMap, this, lineCurvePreference, currentSegment));
            } else {
                setValuesFrom(pixelChainService.refine03MidSegment(pPixelMap, this, lineCurvePreference, currentSegment));
            }
        });
    }




    public void refine03MidSegmentEatForward(
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
                setValuesFrom(setVertex(originalEndVertex));
                setValuesFrom(setSegment(pCurrentSegment));
                setValuesFrom(setSegment(originalNextSegment));
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
                setValuesFrom(setVertex(candidateVertex));
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        setValuesFrom(setSegment(candidateSegment));
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
            setValuesFrom(setVertex(bestCandidateVertex));
            setValuesFrom(setSegment(bestCandidateSegment));
            // System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this)); // TODO
        }
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



    private void setValuesFrom(PixelChain pixelChain) {
        mPixels = pixelChain.getPixels();
        mSegments = pixelChain.getSegments();
        mVertexes = pixelChain.getVertexes();
        mLength = pixelChain.getLength();
        mThickness = pixelChain.getThickness();
    }

    public void approximateCurvesOnly(
            PixelMapData pPixelMap,
            double pTolerance,
            double pLineCurvePreference
    ) {
        if (getPixelCount() <= 4) {
            return;
        }
        setValuesFrom(changeVertexes(ImmutableVectorClone::clear));
        setValuesFrom(changeSegments(ImmutableVectorClone::clear));
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
        setValuesFrom(changeVertexes(v -> v.add(null)));
        setValuesFrom(changeSegments(s -> s.add(null)));
        Tuple3<Integer, ISegment, IVertex> bestFit;

        for (int i = startPixelIndex; i < getPixelCount(); i++) {
            try {
                var candidateVertex = services.getVertexService().createVertex(pixelMap, this, vertexIndex, i);
                var lt3 = vertexService.calcLocalTangent(pixelMap, this, candidateVertex, 3);
                var startTangent = vertexService.getStartSegment( this, startVertex).getEndTangent(pixelMap, this);
                var p = lt3.intersect(startTangent);
                if (p != null) {
                    setValuesFrom(setVertex(candidateVertex));
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
            setValuesFrom(setSegment(best.get()._1));
            setValuesFrom(setVertex(best.get()._2));
            if (best.get()._1 == null || getPixelCount() - startPixelIndex == 1) {
                setValuesFrom(setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, segmentIndex)));
            }
        } else {
            setValuesFrom(setVertex(vertexService.createVertex(pixelMap, this, vertexIndex, getMaxPixelIndex())));
            setValuesFrom(setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, segmentIndex)));
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

        setValuesFrom(changeVertexes(v -> v.add(services.getVertexService().createVertex(pixelMap, this, 0, 0))));
        val vertexIndex = getVertexCount();
        val segmentIndex = getSegmentCount();
        val best = new StrongReference<Tuple2<ISegment, IVertex>>(null);
        setValuesFrom(changeVertexes(v -> v.add(null)));
        setValuesFrom(changeSegments(s -> s.add(null)));
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
                    setValuesFrom(setVertex(candidateVertex));
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
            setValuesFrom(setSegment(best.get()._1));
            setValuesFrom(setVertex(best.get()._2));
            if (getSegment(0) == null) {
                setValuesFrom(setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, 0)));
            }
        } else {
            setValuesFrom(setVertex(services.getVertexService().createVertex(pixelMap, this, vertexIndex, getMaxPixelIndex())));
            setValuesFrom(setSegment(SegmentFactory.createTempStraightSegment(pixelMap, this, segmentIndex)));
        }
    }

}
