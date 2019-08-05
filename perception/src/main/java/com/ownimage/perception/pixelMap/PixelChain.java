/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Counter;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.PegCounter;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.IImmutableVector;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import io.vavr.Tuple4;
import lombok.val;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The Class PixelChain. The following shows how a PixelChain would be constructed, populated with Pixels and ISegments
 * <p>
 * <code>
 * <br/>PixelChain chain = new PixelChain(this, pStartNode);
 * <br/>
 * <br/>for (Pixel pixel : ...) {
 * <br/>	chain.add(pixel);
 * <br/>}
 * <br/>
 * <br/>// then at the end
 * <br/>chain.setEndNode(getNode(endPixel));
 * <br/>
 * <br/>// then set up the segments
 * <br/>
 * <br/>mSegments = new Vector<ISegment>(); // mSegments is a private member of PixelChain
 * <br/>for each segment
 * <br/>// note that the first segment needs to be attached to getStartVertex()
 * <br/>// then each segment needs to be attached to the previous endVertex
 * <br/>{
 * <br/>    segment.attachToVertexes(false);
 * <br/>	mSegments.add(segment);
 * <br/>}
 * </code>
 */
public class PixelChain implements Serializable, Cloneable, IPixelChain {

    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 2L;

    private final ImmutableVectorClone<Pixel> mPixels;
    private  ImmutableVectorClone<ISegment> mSegmentsX;
    private  ImmutableVectorClone<IVertex> mVertexesX;
    transient private double mLength;
    private Thickness mThickness;

    /**
     * Instantiates a new pixel chain.
     *
     * @param pStartNode the start node
     */
    public PixelChain(final Node pStartNode) {
        if (pStartNode == null) {
            throw new IllegalArgumentException("pStartNode must not be null");
        }
        mPixels = new ImmutableVectorClone<Pixel>().add(pStartNode);
        mSegmentsX = new ImmutableVectorClone<>();
        mVertexesX = new ImmutableVectorClone<>();
        mThickness = IPixelChain.Thickness.Normal;

        setStartVertex(Vertex.createVertex(this, mVertexesX.size(), 0));
    }

    public PixelChain(
            final PixelMap pPixelMap,
            final ImmutableVectorClone<Pixel> pPixels,
            final ImmutableVectorClone<ISegment> pSegments,
            final ImmutableVectorClone<IVertex> pVertexes,
            final double pLength,
            final Thickness pThickness
    ) {
        Framework.checkParameterNotNull(mLogger, pPixels, "pPixels");
        Framework.checkParameterNotNull(mLogger, pSegments, "pSegments");
        Framework.checkParameterNotNull(mLogger, pVertexes, "pVertexes");
        Framework.checkParameterNotNull(mLogger, pThickness, "pThickness");

        mPixels = pPixels;
        mSegmentsX = pSegments;
        mVertexesX = pVertexes;
        mLength = pLength;
        mThickness = pThickness;

        validate(pPixelMap, false, "PixelChain");
    }

    private PixelChainBuilder builder() {
        return new PixelChainBuilder(mPixels, mVertexesX, mSegmentsX, mLength, mThickness);
    }

    private PegCounter getPegCounter() {
        return Services.getServices().getPegCounter();
    }

    @Override
    public PixelChain clone() {
        try {
            return (PixelChain) super.clone();
        } catch (CloneNotSupportedException pE) {
            throw new RuntimeException("CloneNotSupportedException", pE);
        }
    }

    public PixelChain add(final PixelMap pPixelMap, final Pixel pPixel) {
        val builder = builder();
        builder.changePixels(p -> p.add(pPixel));
        return builder.build(pPixelMap);
    }

    public IImmutableVector<Pixel> getPixels() { return mPixels;}

    public IImmutableVector<ISegment> getSegments() { return mSegmentsX;}

    public IImmutableVector<IVertex> getVertexes() { return mVertexesX;}

    /**
     * Adds the two pixel chains together. It allocates all of the pixels from the pOtherChain to this, unattaches both chains from the middle node, and adds all of the segments from the second chain
     * to the first (joining at the appropriate vertex in the middle, and using the correct offset for the new vertexes). Note that the new segments that are copied from the pOtherChain are all
     * LineApproximations.
     *
     * @param pPixelMap   the pixelMap
     * @param pOtherChain the other chain
     */
    private PixelChain merge(final PixelMap pPixelMap, final PixelChain pOtherChain) {
        final val builder = builder();

        pOtherChain.validate(pPixelMap, false, "add pOtherChain");
        mLogger.fine(() -> String.format("this.mPixels.size() = %s", builder.getPixels().size()));
        mLogger.fine(() -> String.format("pOtherChain.mPixels.size() = %s", pOtherChain.mPixels.size()));
        mLogger.fine(() -> String.format("this.mSegments.size() = %s", builder.getSegments().size()));
        mLogger.fine(() -> String.format("pOtherChain.mSegments.size() = %s", pOtherChain.mSegmentsX.size()));
        if (mLogger.isLoggable(Level.FINE)) {
            builder.streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
            builder.streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
            pOtherChain.mSegmentsX.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartVertex(pOtherChain).getPixelIndex(), s.getEndVertex(pOtherChain).getPixelIndex())));
            pOtherChain.mSegmentsX.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartIndex(pOtherChain), s.getEndIndex(pOtherChain))));
        }

        builder.build(pPixelMap).validate(pPixelMap, false, "merge");
        pOtherChain.validate(pPixelMap, false, "merge");

        if (!builder.getPixels().lastElement().get().equals(pOtherChain.firstPixel())) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + pOtherChain);
        }

        final int offset = builder.getPixels().size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed
        builder.changePixels(p -> p.remove(builder.getPixels().size() - 1)); // need to remove the last pixel as it will be duplicated on the other chain;
        builder.changePixels(p -> p.addAll(pOtherChain.mPixels.toVector()));
        mLogger.fine(() -> String.format("offset = %s", offset));

        pOtherChain.mSegmentsX.forEach(segment -> {
            final IVertex end = Vertex.createVertex(builder.build(pPixelMap), builder.getVertexes().size(), segment.getEndIndex(pOtherChain) + offset);
            builder.changeVertexes(v -> v.add(end));
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder.build(pPixelMap), builder.getSegments().size());
            builder.changeSegments(s -> s.add(newSegment));
        });

        mLogger.fine(() -> String.format("clone.mPixels.size() = %s", builder.getPixels().size()));
        mLogger.fine(() -> String.format("clone.mSegments.size() = %s", builder.getPixels().size()));
        mSegmentsX.forEach(s -> mLogger.fine(() -> String.format("out.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
        mSegmentsX.forEach(s -> mLogger.fine(() -> String.format("out.is.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
        return builder.build(pPixelMap);
    }

    PixelChain approximate(final PixelMap pPixelMap, final IPixelMapTransformSource pTransformSource) {
        var tolerance = pTransformSource.getLineTolerance() / pTransformSource.getHeight();
        var copy = clone();
        copy = copy.approximate01_straightLines(pPixelMap, tolerance);
        copy = copy.approximate02_refineCorners(pPixelMap);
        copy.checkAllVertexesAttached();
        return copy;
    }

    PixelChain approximate01_straightLines(final PixelMap pPixelMap, final double pTolerance) {
        // note that this is version will find the longest line that is close to all pixels.
        // there are cases where a line of length n will be close enough, a line of length n+1 will not be, but there exists an m such that a line of length m is close enough.
        if (getPixelCount() <= 1) {
            return this;
        }

        var builder = builder();
        builder.changeSegments(s -> s.clear());
        builder.changeVertexes(v -> v.clear());

        val startVertex = Vertex.createVertex(builder, 0, 0);
        builder.changeVertexes(v -> v.add(startVertex));

        int maxIndex = 0;
        val maxVertex = new IVertex[]{null};
        val maxSegment = new StraightSegment[]{null};

        int endIndex = 1;

        while (endIndex < builder.getPixelCount()) {
            val vertexIndex = builder.getVertexCount();
            builder.changeVertexes(v -> v.add(null));
            val segmentIndex = builder.getSegmentCount();
            builder.changeSegments(s -> s.add(null));

            for (int index = endIndex; index < builder.getPixelCount(); index++) {
                val candidateVertex = Vertex.createVertex(builder, vertexIndex, index);
                builder.changeVertexes(v -> v.set(vertexIndex, candidateVertex));
                val candidateSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder, segmentIndex);
                builder.changeSegments(s -> s.set(segmentIndex, candidateSegment));

                if (candidateSegment.noPixelFurtherThan(pPixelMap, builder, pTolerance)) {
                    maxIndex = index;
                    maxVertex[0] = candidateVertex;
                    maxSegment[0] = candidateSegment;
                    continue;
                }
                break;
            }

            builder.changeVertexes(v -> v.set(vertexIndex, maxVertex[0]));
            builder.changeSegments(s -> s.set(segmentIndex, maxSegment[0]));
            endIndex = maxIndex + 1;
        }
        return builder.build(pPixelMap);
    }

    private PixelChain approximate02_refineCorners(final PixelMap pPixelMap) {
        if (getSegmentCount() <= 1) {
            return this;
        }
        val builder = builder();
        // the for loop means that I am processing the current state of the builder, not the 'old' stream state
        // this is important as the builder is being mutated.
        for (int i = 0; i < builder.getSegmentCount() - 1; i++) { // do not process last segment
            var segment = builder.getSegment(i);

            val firstSegmentIndex = segment.getSegmentIndex();
            val secondSegmentIndex = firstSegmentIndex + 1;
            val joinIndex = secondSegmentIndex;
            val joinPixelIndex = segment.getEndIndex(builder);

            IVertex[] joinVertex = new IVertex[]{builder.getVertex(joinIndex)};
            ISegment[] firstSegment = new ISegment[]{segment};
            ISegment[] secondSegment = new ISegment[]{builder.getSegment(secondSegmentIndex)};

            val minPixelIndex = (segment.getStartVertex(builder).getPixelIndex() + segment.getEndVertex(builder).getPixelIndex()) / 2;
            val maxPixelIndex = (secondSegment[0].getStartVertex(builder).getPixelIndex() + secondSegment[0].getEndVertex(builder).getPixelIndex()) / 2;

            var currentError = segment.calcError(pPixelMap, builder) + secondSegment[0].calcError(pPixelMap, builder);
            var best = new Tuple4<>(currentError, firstSegment[0], joinVertex[0], secondSegment[0]);

            getPegCounter().increase(IPixelChain.PegCounters.RefineCornersAttempted);
            // the check below is needed as some segments may only be one index length so generating a midpoint might generate an invalid segment
            if (minPixelIndex < joinPixelIndex && joinPixelIndex < maxPixelIndex) {
                var refined = false;
                for (int candidateIndex = minPixelIndex + 1; candidateIndex < maxPixelIndex; candidateIndex++) {
                    joinVertex[0] = Vertex.createVertex(builder, joinIndex, candidateIndex);
                    builder.changeVertexes(v -> v.set(joinIndex, joinVertex[0]));
                    firstSegment[0] = SegmentFactory.createTempStraightSegment(pPixelMap, builder, firstSegmentIndex);
                    builder.changeSegments(s -> s.set(firstSegmentIndex, firstSegment[0]));
                    secondSegment[0] = SegmentFactory.createTempStraightSegment(pPixelMap, builder, secondSegmentIndex);
                    builder.changeSegments(s -> s.set(secondSegmentIndex, secondSegment[0]));

                    currentError = segment.calcError(pPixelMap, builder) + secondSegment[0].calcError(pPixelMap, builder);

                    if (currentError < best._1) {
                        best = new Tuple4<>(currentError, firstSegment[0], joinVertex[0], secondSegment[0]);
                        refined = true;
                    }
                }
                if (refined &&
                        best._2.getEndTangentVector(pPixelMap, builder)
                                .dot(best._4.getStartTangentVector(pPixelMap, builder))
                                < 0.5d
                ) {
                    getPegCounter().increase(IPixelChain.PegCounters.RefineCornersSuccessful);
                }
                val finalBest = best;
                builder.changeVertexes(v -> v.set(joinIndex, finalBest._3));
                builder.changeSegments(s -> s.set(firstSegmentIndex, finalBest._2));
                builder.changeSegments(s -> s.set(secondSegmentIndex, finalBest._4));
            }
        }
        return builder.build(pPixelMap);
    }

    private void checkAllVertexesAttached() {
        mSegmentsX.forEach(segment -> {
            try {
                if (mLogger.isLoggable(Level.SEVERE)) {
                    if (segment.getStartVertex(this).getEndSegment(this) != segment) {
                        mLogger.severe("start Vertex not attached");
                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
                    }
                    if (segment.getEndVertex(this).getStartSegment(this) != segment) {
                        mLogger.severe("end Vertex not attached");
                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
                    }
                }
            } catch (final Throwable pT) {
                mLogger.log(Level.SEVERE, "Unxepected error", pT);
            }
        });
    }

    boolean contains(final Pixel pPixel) {
//        if (mMinX > pPixel.getX() || pPixel.getX() > mMaxX || mMinY > pPixel.getY() || pPixel.getY() > mMaxY)
//            return false;

        return mPixels
                .stream()
                .filter(p -> p.samePosition(pPixel))
                .findAny()
                .isPresent();
    }

    @Deprecated
    Pixel firstPixel() {
        // happy for this to throw exception if first element does not exist
        return mPixels.firstElement().get();
    }

    Optional<Node> getEndNode(PixelMap pPixelMap) {
        return mPixels.lastElement().flatMap(pPixelMap::getNode);
    }

    @Deprecated
    private ISegment getFirstSegment() {
        return mSegmentsX.firstElement().orElse(null);
    }

    @Deprecated
    private ISegment getLastSegment() {
        return mSegmentsX.lastElement().orElse(null);
    }

    @Override
    public double getLength() {
        return mLength;
    }

    Optional<Node> getStartNode(final PixelMap pPixelMap) {
        return pPixelMap.getNode(mPixels.firstElement().get());
    }

    private IVertex getStartVertex() {
        return mVertexesX.firstElement().orElse(null);
    }

    PixelChain indexSegments(final PixelMap pPixelMap) {
        val builder = builder();
        final double[] startPosition = {0.0d};
        streamSegments().forEach(segment -> {
            final ISegment segmentClone = segment.withStartPosition(startPosition[0]);
            builder.changeSegments(s -> s.set(segmentClone.getSegmentIndex(), segmentClone));
            startPosition[0] += segment.getLength(pPixelMap, builder);
        });
        builder.setLength(startPosition[0]);
        val newPixelChain = builder.build(pPixelMap);
        newPixelChain.streamSegments().forEach(segment -> pPixelMap.index(newPixelChain, segment));
        return newPixelChain;
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

    @Deprecated
    private Pixel lastPixel() {
        // happy for this to throw exception
        return mPixels.lastElement().get();
    }

    /**
     * Length of the PixelChain. This is the number of Pixels that it contains.
     *
     * @return the number of Pixels in the PixelChain.
     */
    public int length() {
        return mPixels.size();
    }

    /**
     * Merges two pixel chains together that share a common Node. The result is one PixelChain with a vertex where the Node was. The chain will have correctly attached itself to the node at either
     * end. This needs to be done before after the segments are generated so that the vertex for the node can be created.
     *
     * @param pPixelMap   the PixelMap
     * @param pOtherChain the other chain
     * @param pNode       the node
     */
    PixelChain merge(final PixelMap pPixelMap, final PixelChain pOtherChain, final Node pNode) {
        mLogger.fine("merge");
//        if (!(getStartNode(pPixelMap) == pNode || getEndNode(pPixelMap) == pNode) || !(pOtherChain.getStartNode(pPixelMap) == pNode || pOtherChain.getEndNode(pPixelMap) == pNode)) {
//            throw new IllegalArgumentException("Either this PixelChain: " + this + ", and pOtherChain: " + pOtherChain + ", must share the following node:" + pNode);
//        }

        StrongReference<PixelChain> one = new StrongReference<>(this);
        getEndNode(pPixelMap).filter(n -> n == pNode).ifPresent(n -> one.set(reverse(pPixelMap)));

        StrongReference<PixelChain> other = new StrongReference<>(pOtherChain);
        pOtherChain.getStartNode(pPixelMap).filter(n -> n == pNode).ifPresent(n -> other.set(pOtherChain.reverse(pPixelMap)));

//        if (one.get().getEndNode(pPixelMap) != pNode || other.getStartNode(pPixelMap) != pNode) {
//            throw new RuntimeException("This PixelChain: " + this + " should end on the same node as the other PixelChain: " + pOtherChain + " starts with.");
//        }

        // TODO should recalculate thickness from source values
        mThickness = getPixelCount() > pOtherChain.getPixelCount() ? mThickness : pOtherChain.mThickness;
        return merge(pPixelMap, pOtherChain);
    }

    public PixelChain refine(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {
        var copy = clone();
        copy.refine01_matchCurves(pPixelMap, pSource);
        copy.refine03_matchCurves(pPixelMap, pSource);

//        if (copy.containsStraightSegment()) {
//            PixelChain reversed = copy.reverse(pPixelMap);
//            reversed.refine01_matchCurves(pPixelMap, pSource);
//            reversed.refine02_matchDoubleCurves(pSource, pPixelMap);
//            reversed.refine03_matchCurves(pPixelMap, pSource);
//            if (reversed.countStraightSegments() < copy.countStraightSegments()) {
//                return reversed;
//            }
//        }

        return copy;
    }

    public Thickness getThickness() {
        if (mThickness == null) {
            mThickness = IPixelChain.Thickness.Normal;
        }
        return mThickness;
    }

    private boolean containsStraightSegment() {
        return mSegmentsX.stream()
                .filter(s -> (s instanceof StraightSegment))
                .findFirst()
                .isPresent();
    }

    private int countStraightSegments() {
        var count = Counter.createCounter();
        mSegmentsX.stream()
                .forEach(s -> count.increase());
        return count.getCount();
    }

    private void refine03_matchCurves(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {

        if (mSegmentsX.size() == 1) {
            return;
        }

        mSegmentsX.forEach(currentSegment -> {
            if (currentSegment == mSegmentsX.firstElement().get()) {
                refine03FirstSegment(pPixelMap, pSource, currentSegment);
            } else if (currentSegment == mSegmentsX.lastElement().get()) {
                refine03LastSegment(pPixelMap, pSource, currentSegment);
            } else {
                refine03MidSegment(pPixelMap, pSource, currentSegment);
            }
        });
        validate(pPixelMap, false, "refine03_matchCurves");
    }

    private void refine01_matchCurves(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {

        if (mSegmentsX.size() == 1) {
            return;
        }

        mSegmentsX.forEach(currentSegment -> {
            if (currentSegment == mSegmentsX.firstElement().get()) {
                refine01FirstSegment(pPixelMap, pSource, currentSegment);
            } else if (currentSegment == mSegmentsX.lastElement().get()) {
                refine01EndSegment(pPixelMap, pSource, currentSegment);
            } else {
                refine01MidSegment(pPixelMap, pSource, currentSegment);
            }
        });
        validate(pPixelMap, false, "refine01_matchCurves");
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
            mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), bestSegment);
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
            mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), bestSegment);
        }
    }

    private void refine01FirstSegment(
            PixelMap pPixelMap,
            final IPixelMapTransformSource pSource,
            final ISegment pCurrentSegment
    ) {
        var bestSegment = pCurrentSegment;
        try {
            // get error values from straight line to start the compare
            double lowestError = pCurrentSegment.calcError(pPixelMap, this);
            lowestError *= pSource.getLineCurvePreference();
            // calculate end tangent
            final Line tangent = pCurrentSegment.getEndVertex(this).calcTangent(this, pPixelMap);

            // find closest point between start point and tangent line
            final Point closest = tangent.closestPoint(pCurrentSegment.getStartUHVWPoint(pPixelMap, this));
            // divide this line (tangentRuler) into the number of pixels in the segment
            // for each of the points on the division find the lowest error
            final LineSegment tangentRuler = new LineSegment(closest, pCurrentSegment.getEndUHVWPoint(pPixelMap, this));
            for (int i = 1; i < pCurrentSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                try {
                    final double lambda = (double) i / pCurrentSegment.getPixelLength(this);
                    final Point p1 = tangentRuler.getPoint(lambda);
                    final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), p1);
                    mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), candidateSegment);
                    final double candidateError = candidateSegment != null ? candidateSegment.calcError(pPixelMap, this) : 0.0d;

                    if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                        lowestError = candidateError;
                        bestSegment = candidateSegment;
                    }

                } catch (final Throwable pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                }
            }
        } catch (final Throwable pT) {
            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
        } finally {
            mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), bestSegment);
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

    private void refine03MidSegmentEatForward(
            PixelMap pPixelMap,
            IPixelMapTransformSource pSource,
            ISegment pCurrentSegment
    ) {
        var bestCandidateSegment = pCurrentSegment;
        var bestCandidateVertex = pCurrentSegment.getEndVertex(this);
        var originalNextSegment = pCurrentSegment.getNextSegment(this);
        var originalEndVertex = pCurrentSegment.getEndVertex(this);

        if (!(
                (pCurrentSegment instanceof StraightSegment) || (originalNextSegment instanceof StraightSegment)
        )) {
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
                mVertexesX = mVertexesX.set(originalEndVertex.getVertexIndex(), originalEndVertex);
                mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), pCurrentSegment);
                mSegmentsX = mSegmentsX.set(originalNextSegment.getSegmentIndex(), originalNextSegment);
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
                mVertexesX = mVertexesX.set(candidateVertex.getVertexIndex(), candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), candidateSegment);
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
            mVertexesX = mVertexesX.set(bestCandidateVertex.getVertexIndex(), bestCandidateVertex);
            mSegmentsX = mSegmentsX.set(bestCandidateSegment.getSegmentIndex(), bestCandidateSegment);
            // System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this)); // TODO
        }
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
                mVertexesX = mVertexesX.set(originalEndVertex.getVertexIndex(), originalEndVertex);
                var lambda = (double) i / nextSegmentPixelLength;
                var controlPointStart = originalNextSegment.getPointFromLambda(pPixelMap, this, lambda);
                var candidateVertex = Vertex.createVertex(this, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
                mVertexesX = mVertexesX.set(candidateVertex.getVertexIndex(), candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), candidateSegment);
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
            mVertexesX = mVertexesX.set(bestCandidateVertex.getVertexIndex(), bestCandidateVertex);
            mSegmentsX = mSegmentsX.set(bestCandidateSegment.getSegmentIndex(), bestCandidateSegment);
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
                mVertexesX = mVertexesX.set(originalStartVertex.getVertexIndex(), originalStartVertex);
                var lambda = 1.0d - (double) i / prevSegmentPixelLength; // TODO
                var controlPointStart = originalPrevSegment.getPointFromLambda(pPixelMap, this, lambda);
                var candidateVertex = Vertex.createVertex(this, originalStartVertex.getVertexIndex(), originalStartVertex.getPixelIndex() - i, controlPointStart);
                mVertexesX = mVertexesX.set(candidateVertex.getVertexIndex(), candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList()); // TODO
                // TODO below should refactor this
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        mSegmentsX = mSegmentsX.set(pCurrentSegment.getSegmentIndex(), candidateSegment);
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
            mVertexesX = mVertexesX.set(bestCandidateVertex.getVertexIndex(), bestCandidateVertex);
            mSegmentsX = mSegmentsX.set(bestCandidateSegment.getSegmentIndex(), bestCandidateSegment);
            // TODO System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this));
        }
    }

    private Line calcStartTangent(PixelMap pPixelMap, final ISegment pSegment, boolean pInitialRun) {
        var previousSegment = pSegment.getStartVertex(this).getStartSegment(this);
        return pInitialRun || !(previousSegment instanceof StraightSegment)
                ? pSegment.getStartVertex(this).calcTangent(this, pPixelMap)
                : previousSegment.getEndTangent(pPixelMap, this);
    }

    private Line calcEndTangent(PixelMap pPixelMap, final ISegment pSegment, boolean pInitialRun) {
        var nextSegment = pSegment.getEndVertex(this).getEndSegment(this);
        return pInitialRun || !(nextSegment instanceof StraightSegment)
                ? pSegment.getEndVertex(this).calcTangent(this, pPixelMap)
                : nextSegment.getStartTangent(pPixelMap, this);
    }

    private Line getDCStartTangent(final PixelMap pPixelMap, final ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex startVertex = pSegment.getStartVertex(this);
        if (startVertex.getStartSegment(this) instanceof StraightSegment) {
            return startVertex.getStartSegment(this).getEndTangent(pPixelMap, this);
        }

        return startVertex.calcTangent(this, pPixelMap);
    }

    private Line getDCEndTangent(final PixelMap pPixelMap, final ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex endVertex = pSegment.getEndVertex(this);
        if (endVertex.getEndSegment(this) instanceof StraightSegment) {
            return endVertex.getEndSegment(this).getStartTangent(pPixelMap, this);
        }

        return endVertex.calcTangent(this, pPixelMap);
    }

    /**
     * Creates a copy of this PixelChain with the order of the pixels in the pixel chain.
     * The original PixelChain is not altered.
     * This means reversing the start and end nodes.
     * All of the segments in the line are also reversed and replaced with new straight line
     * segments.
     *
     * @param pPixelMap
     * @return a new PixelChain with the elements reversed
     */
    public PixelChain reverse(final PixelMap pPixelMap) {
        // note that this uses direct access to the data members as the public setters have other side effects
        //validate("reverse");
        val builder = builder();

        // reverse pixels
        Vector<Pixel> pixels = builder.getPixels().toVector();
        Collections.reverse(pixels);
        builder.changePixels(p -> p.clear().addAll(pixels));

        // reverse vertexes
        final int maxPixelIndex = builder.getPixels().size() - 1;
        final Vector<IVertex> vertexes = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            final IVertex vertex = builder.getVertexes().get(i);
            final IVertex v = Vertex.createVertex(builder, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
        }
        builder.changeVertexes(v -> v.clear().addAll(vertexes));

        // reverse segments
        final Vector<ISegment> segments = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            if (i != mVertexesX.size() - 1) {
                final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder.build(pPixelMap), segments.size());
                segments.add(newSegment);
            }
        }
        builder.changeSegments(s -> s.clear().addAll(segments));
        return builder.build(pPixelMap);
    }

    PixelChain setEndNode(final PixelMap pPixelMap, final Node pNode) {
        Framework.checkParameterNotNull(mLogger, pNode, "pNode");

        val builder = builder();
        builder.changePixels(p-> p.add(pNode));

        // need to do a check here to see if we are clobbering over another chain
        // if pixel end-2 is a neighbour of pixel end then pixel end-1 needs to be set as notVisited and removed from the chain
        if (builder.getPixelCount() >= 3 && pNode.isNeighbour(builder.getPixel(builder.getPixelCount() - 3))) {
            val index = builder.getPixelCount() - 2;
            builder.getPixel(index).setVisited(pPixelMap, false);
            builder.changePixels(p -> p.remove(index));
        }
        return builder.build(pPixelMap);
    }

    private void setLength(final double pLength) {
        mLength = pLength;
    }

    private void setStartVertex(final IVertex pVertex) {
        if (mVertexesX.size() >= 1) {
            throw new IllegalStateException("can not setStartVertex when Vertexes already exist");
        }
        mVertexesX = mVertexesX.add(0, pVertex);
    }

    PixelChain setThickness(final int pThinLength, final int pNormalLength, final int pLongLength) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pNormalLength, pLongLength", pNormalLength, pLongLength);

        Thickness newThickness;
        if (length() < pThinLength) {
            newThickness = IPixelChain.Thickness.None;
        } else if (length() < pNormalLength) {
            newThickness = IPixelChain.Thickness.Thin;
        } else if (length() < pLongLength) {
            newThickness = IPixelChain.Thickness.Normal;
        } else {
            newThickness = IPixelChain.Thickness.Thick;
        }

        PixelChain result;
        if (newThickness == mThickness) result = this;
        else {
            result = clone();
            result.mThickness = newThickness;
        }
        return result;
    }

    public PixelChain setThickness(final Thickness pThickness) {
        Framework.logEntry(mLogger);
        if (pThickness == mThickness) return this;
        PixelChain clone = clone();
        clone.mThickness = pThickness;
        return clone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");
        sb.append(mPixels.stream().map(Pixel::toString).collect(Collectors.joining(", ")));
        sb.append(" ]\n");

        return sb.toString();
    }

    void validate(final PixelMap pPixelMap, boolean pFull, final String pMethodName) {
        try {
            if (getStartVertex().getPixelIndex() != 0) {
                throw new IllegalStateException("getStartVertex().getPixelIndex() != 0");
            }

            if ((mVertexesX.size() == 0 && mSegmentsX.size() != 0)) {
                throw new IllegalStateException(String.format("mVertexes.size() = %s && mSegments.size() = %s", mVertexesX.size(), mSegmentsX.size()));
            }

            if (mVertexesX.size() != 0 && mSegmentsX.size() + 1 != mVertexesX.size()) {
                throw new IllegalStateException(String.format("mVertexes.size() = %s && mSegments.size() = %s", mVertexesX.size(), mSegmentsX.size()));
            }

            final int nextStartIndex[] = {0};

            mSegmentsX.forEach(segment -> {
                if (segment.getStartIndex(this) != nextStartIndex[0]) { //
                    throw new IllegalStateException("segments not linked properly");
                }
                nextStartIndex[0] = segment.getEndIndex(this);
            });

            if (mSegmentsX.size() != 0 && mSegmentsX.lastElement().get().getEndIndex(this) != mPixels.size() - 1) { //
                throw new IllegalStateException(String.format("last segment not linked properly, %s, %s, %s", mSegmentsX.size(), mSegmentsX.lastElement().get().getEndIndex(this), mPixels.size() - 1));
            }

            checkAllVertexesAttached();

            IVertex vertex = getStartVertex();
            int index = 0;
            while (vertex != null) {
                if (mVertexesX.get(vertex.getVertexIndex()) != vertex) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                if (vertex.getVertexIndex() != index) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                index++;
                vertex = vertex.getEndSegment(this) != null
                        ? vertex.getEndSegment(this).getEndVertex(this)
                        : null;
            }

            if (mVertexesX.size() != 0) {
                if (mVertexesX.firstElement().get().getStartSegment(this) != null)
                    throw new RuntimeException("wrong start vertex");
                if (mVertexesX.lastElement().get().getEndSegment(this) != null) throw new RuntimeException("wrong end vertex");
            }

            int currentMax = -1;
            for (int i = 0; i < mVertexesX.size(); i++) {
                final IVertex v = mVertexesX.get(i);
                if (i == 0 && v.getPixelIndex() != 0) {
                    throw new IllegalStateException("First vertex wrong)");
                }
                if (i == mVertexesX.size() - 1 && v.getPixelIndex() != mPixels.size() - 1 && pFull) {
                    throw new IllegalStateException("Last vertex wrong)");
                }
                if (v.getPixelIndex() <= currentMax) {
                    throw new IllegalStateException("Wrong pixel index order");
                }
                currentMax = v.getPixelIndex();
                if (i != 0 && v.getStartSegment(this) != mSegmentsX.get(i - 1)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
                if (i != mVertexesX.size() - 1 && v.getEndSegment(this) != mSegmentsX.get(i)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
            }
        } catch (final Throwable pT) {
            printVertexs();
            throw pT;
        }
    }

    public void printVertexs() {
        final StringBuilder sb = new StringBuilder()
                .append(String.format("mVertexes.size() = %s, mSegments.size() = %s", mVertexesX.size(), mSegmentsX.size()))
                .append("\nArrray\n");

        for (int i = 0; i < mVertexesX.size(); i++) {
            sb.append(String.format("i = %s, mVertexes.get(i).getVertexIndex() = %s\n", i, mVertexesX.get(i).getVertexIndex()));
        }

        sb.append("\n\nWalking\n");
        IVertex vertex = getStartVertex();
        int index = 0;
        while (vertex != null) {
            sb.append(String.format("index = %s, vertex.getVertexIndex() = %s\n", index, vertex.getVertexIndex()));
            index++;
            vertex = vertex.getEndSegment(this) != null
                    ? vertex.getEndSegment(this).getEndVertex(this)
                    : null;
            ;
        }

        mLogger.severe(sb::toString);
    }



    void setInChain(final PixelMap pPixelMap, final boolean pValue) {
        mPixels.forEach(p -> p.setInChain(pPixelMap, pValue));
    }

    private void setEdge(final PixelMap pPixelMap) {
        mPixels.stream()
                .filter(p -> p != mPixels.firstElement().get())
                .filter(p -> p != mPixels.lastElement().get())
                .forEach(p -> p.setEdge(pPixelMap, false));
        mPixels.stream()
                .filter(pPixel -> pPixel.isNode(pPixelMap))
                .filter(p -> p.countEdgeNeighbours(pPixelMap) < 2 || p.countNodeNeighbours(pPixelMap) == 2)
                .forEach(p -> p.setEdge(pPixelMap, false));
    }

    void setVisited(final PixelMap pPixelMap, final boolean pValue) {
        mPixels.forEach(p -> p.setVisited(pPixelMap, pValue));
    }

    public void delete(final PixelMap pPixelMap) {
        mLogger.fine("delete");
        setInChain(pPixelMap, false);
        setVisited(pPixelMap, false);
        setEdge(pPixelMap);
    }

}

