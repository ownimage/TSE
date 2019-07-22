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
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.DoubleCurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.transform.CannyEdgeTransform;
import io.vavr.Tuple4;

import java.io.Serializable;
import java.util.Collections;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class PixelChain implements Serializable, Cloneable {

    public enum Thickness {
        None, Thin, Normal, Thick
    }

    public enum PegCounters {
        StartSegmentStraightToCurveAttempted,
        StartSegmentStraightToCurveSuccessful

    }

    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 2L;
    transient private double mLength;
    private Vector<Pixel> mPixels = new Vector<>();
    private Vector<ISegment> mSegments = new Vector<>();
    private Vector<IVertex> mVertexes = new Vector<>();

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

        mThickness = Thickness.Normal;
        setStartNode(pStartNode);
    }

    Stream<Pixel> streamPixels() {
        return mPixels.stream();
    }

    private PegCounter getPegCounter() {
        return Services.getServices().getPegCounter();
    }

    private PixelChain deepCopy() {
        try {
            PixelChain copy = (PixelChain) super.clone();
            copy.mLength = mLength;
            copy.mThickness = mThickness;
            copy.mPixels = (Vector<Pixel>) mPixels.clone();
            copy.mSegments = (Vector<ISegment>) mSegments.clone();
            copy.mVertexes = (Vector<IVertex>) mVertexes.clone();
            return copy;
        } catch (CloneNotSupportedException pE) {
            throw new RuntimeException("CloneNotSupportedException", pE);
        }
    }

    public PixelChain add(final Pixel pPixel) {
        PixelChain clone = deepCopy();
        clone.mPixels.add(pPixel);
        return clone;
    }

    /**
     * Adds the two pixel chains together. It allocates all of the pixels from the pOtherChain to this, unattaches both chains from the middle node, and adds all of the segments from the second chain
     * to the first (joining at the appropriate vertex in the middle, and using the correct offset for the new vertexes). Note that the new segments that are copied from the pOtherChain are all
     * LineApproximations.
     *
     * @param pPixelMap   the pixelMap
     * @param pOtherChain the other chain
     */
    private PixelChain merge(final PixelMap pPixelMap, final PixelChain pOtherChain) {
        PixelChain clone = deepCopy();

        pOtherChain.validate("add pOtherChain");
        mLogger.fine(() -> String.format("this.mPixels.size() = %s", clone.mPixels.size()));
        mLogger.fine(() -> String.format("pOtherChain.mPixels.size() = %s", pOtherChain.mPixels.size()));
        mLogger.fine(() -> String.format("this.mSegments.size() = %s", clone.mSegments.size()));
        mLogger.fine(() -> String.format("pOtherChain.mSegments.size() = %s", pOtherChain.mSegments.size()));
        if (mLogger.isLoggable(Level.FINE)) {
            clone.mSegments.forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
            clone.mSegments.forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
            pOtherChain.mSegments.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartVertex(pOtherChain).getPixelIndex(), s.getEndVertex(pOtherChain).getPixelIndex())));
            pOtherChain.mSegments.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartIndex(pOtherChain), s.getEndIndex(pOtherChain))));
        }

        clone.validate("merge");
        pOtherChain.validate("merge");

        if (!clone.lastPixel().equals(pOtherChain.firstPixel())) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + pOtherChain);
        }

        final int offset = clone.mPixels.size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed
        clone.mPixels.removeElementAt(clone.mPixels.size() - 1);// need to remove the last pixel as it will be duplicated on the other chain;
        clone.mPixels.addAll(pOtherChain.mPixels);
        mLogger.fine(() -> String.format("offset = %s", offset));

        for (final ISegment segment : pOtherChain.mSegments) {
            final IVertex end = Vertex.createVertex(clone, clone.mVertexes.size(), segment.getEndIndex(pOtherChain) + offset);
            clone.mVertexes.add(end);
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, clone, clone.mSegments.size());
            clone.mSegments.add(newSegment);
        }

        mLogger.fine(() -> String.format("clone.mPixels.size() = %s", clone.mPixels.size()));
        mLogger.fine(() -> String.format("clone.mSegments.size() = %s", clone.mSegments.size()));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("out.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("out.is.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
        clone.validate("add");
        return clone;
    }

    PixelChain approximate(final PixelMap pPixelMap, final IPixelMapTransformSource pTransformSource) {
        var tolerance = pTransformSource.getLineTolerance() / pTransformSource.getHeight();
        var copy = deepCopy();
        copy.approximate01_straightLines(pPixelMap, tolerance);
        copy.approximate02_refineCorners(pPixelMap);
        copy.checkAllVertexesAttached();
        return copy;
    }

    void approximate01_straightLines(final PixelMap pPixelMap, final double pTolerance) {
        // note that this is version will find the longest line that is close to all pixels.
        // there are cases where a line of length n will be close enough, a line of length n+1 will not be, but there exists an m such that a line of length m is close enough.
        mSegments.removeAllElements();
        mVertexes.removeAllElements();

        if (mPixels.size() <= 1) {
            return;
        }

        final IVertex startVertex = Vertex.createVertex(this, 0, 0);
        setStartVertex(startVertex);

        int maxIndex = 0;
        IVertex maxVertex = null;
        StraightSegment maxSegment = null;

        int endIndex = 1;

        while (endIndex < mPixels.size()) {
            final int vertexIndex = mVertexes.size();
            mVertexes.add(null);
            final int segmentIndex = mSegments.size();
            mSegments.add(null);

            for (int index = endIndex; index < mPixels.size(); index++) {
                final IVertex candidateVertex = Vertex.createVertex(this, vertexIndex, index);
                mVertexes.set(vertexIndex, candidateVertex);
                final StraightSegment candidateSegment = SegmentFactory.createTempStraightSegment(pPixelMap, this, segmentIndex);
                mSegments.set(segmentIndex, candidateSegment);

                if (candidateSegment.noPixelFurtherThan(pPixelMap, this, pTolerance)) {
                    maxIndex = index;
                    maxVertex = candidateVertex;
                    maxSegment = candidateSegment;
                    continue;
                }
                break;
            }

            mVertexes.set(vertexIndex, maxVertex);
            mSegments.set(segmentIndex, maxSegment);
            endIndex = maxIndex + 1;
        }

        validate("approximate01_straightLines");
    }

    private void approximate02_refineCorners(final PixelMap pPixelMap) {
        for (final ISegment segment : mSegments) {
            if (segment == mSegments.lastElement()) return;

            final int firstSegmentIndex = segment.getSegmentIndex();
            final int secondSegmentIndex = firstSegmentIndex + 1;
            final int midVertexIndex = secondSegmentIndex;

            IVertex midVertex = getVertex(midVertexIndex);
            ISegment firstSegment = segment;
            ISegment secondSegment = getSegment(segment.getSegmentIndex() + 1);

            final int minVertexIndex = (segment.getStartVertex(this).getPixelIndex() + segment.getEndVertex(this).getPixelIndex()) / 2;
            final int maxVertexIndex = (secondSegment.getStartVertex(this).getPixelIndex() + secondSegment.getEndVertex(this).getPixelIndex()) / 2;

            double currentError = segment.calcError(pPixelMap, this) + secondSegment.calcError(pPixelMap, this);
            Tuple4<Double, ISegment, IVertex, ISegment> best = new Tuple4<>(currentError, firstSegment, midVertex, secondSegment);

            // the check below is needed as some segments may only be one index length so generating a midpoint might generate an invalid segment
            if (minVertexIndex < midVertexIndex && midVertexIndex < maxVertexIndex) {
                for (int candidateIndex = minVertexIndex + 1; candidateIndex < maxVertexIndex; candidateIndex++) {
                    midVertex = Vertex.createVertex(this, midVertexIndex, candidateIndex);
                    mVertexes.set(midVertexIndex, midVertex);
                    firstSegment = SegmentFactory.createTempStraightSegment(pPixelMap, this, firstSegmentIndex);
                    mSegments.set(firstSegmentIndex, firstSegment);
                    secondSegment = SegmentFactory.createTempStraightSegment(pPixelMap, this, secondSegmentIndex);
                    mSegments.set(secondSegmentIndex, secondSegment);

                    currentError = segment.calcError(pPixelMap, this) + secondSegment.calcError(pPixelMap, this);

                    if (currentError < best._1) {
                        best = new Tuple4<>(currentError, firstSegment, midVertex, secondSegment);
                    }
                }
                mVertexes.set(midVertexIndex, best._3);
                mSegments.set(firstSegmentIndex, best._2);
                mSegments.set(secondSegmentIndex, best._4);
            }
        }

        validate("approximate02_refineCorners");
    }

    private void checkAllVertexesAttached() {
        for (final ISegment segment : mSegments) {
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
        }
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

    public Stream<Pixel> stream() {
        return mPixels.stream();
    }

    int count() {
        return mPixels.size();
    }

    Pixel firstPixel() {
        return mPixels.firstElement();
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
    public double getActualThickness(final IPixelMapTransformSource pTransformSource, final double pPosition) {
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

    public Stream<ISegment> streamSegments() {
        return mSegments.stream();
    }

    Optional<Node> getEndNode(PixelMap pPixelMap) {
        return pPixelMap.getNode(mPixels.lastElement());
    }

    private ISegment getFirstSegment() {
        return mSegments.firstElement();
    }

    private ISegment getLastSegment() {
        return mSegments.lastElement();
    }

    private double getLength() {
        return mLength;
    }

    /**
     * Gets the Pixel at the specified position.
     *
     * @param pIndex the index
     * @return the Pixel
     */
    Pixel getPixel(final int pIndex) {
        if (pIndex < 0 || pIndex > length()) {
            throw new IllegalArgumentException("pIndex, currently: " + pIndex + " must be between 0 and the length of mPixels, currently: " + length());
        }

        return mPixels.elementAt(pIndex);
    }

    public int getSegmentCount() {
        Framework.logEntry(mLogger);
        final int result = mSegments.size();
        Framework.logExit(mLogger);
        return result;
    }

    Optional<Node> getStartNode(final PixelMap pPixelMap) {
        return pPixelMap.getNode(mPixels.firstElement());
    }

    private IVertex getStartVertex() {
        return mVertexes.size() != 0
                ? mVertexes.firstElement()
                : null;
    }

    synchronized Thickness getThickness() {
        if (mThickness == null) {
            mThickness = Thickness.Normal;
        }
        return mThickness;
    }

    /**
     * Gets the UHVW value of the Pixel at the specified position.
     *
     * @param pIndex    the index
     * @param pPixelMap
     * @return the UHVW Point
     */
    public Point getUHVWPoint(final int pIndex, final PixelMap pPixelMap) {
        if (pIndex < 0 || pIndex > length()) {
            throw new IllegalArgumentException("pIndex, currently: " + pIndex + " must be between 0 and the length of mPixels, currently: " + length());
        }

        return mPixels.elementAt(pIndex).getUHVWMidPoint(pPixelMap);
    }

    public double getWidth(final IPixelMapTransformSource pIPMTS) {
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

    PixelChain indexSegments(final PixelMap pPixelMap) {
        final PixelChain copy = deepCopy();
        double startPosition = 0.0d;
        for (final ISegment segment : copy.mSegments) {
            final ISegment segmentClone = segment.withStartPosition(pPixelMap, copy, startPosition);
            pPixelMap.index(copy, segmentClone);
            copy.mSegments.set(segment.getSegmentIndex(), segmentClone);
            startPosition += segment.getLength(pPixelMap, copy);
        }
        copy.setLength(startPosition);
        return copy;
    }

    private boolean isValid(final PixelMap pPixelMap, final ISegment pSegment) { // need to maks sure that not only the pixels are close to the line but the line is close to the pixels
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

    private Pixel lastPixel() {
        return mPixels.lastElement();
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
        mThickness = getPixelLength() > pOtherChain.getPixelLength() ? mThickness : pOtherChain.mThickness;
        return merge(pPixelMap, pOtherChain);
    }

    public PixelChain refine(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {
        var copy = deepCopy();
        copy.refine01_matchCurves(pPixelMap, pSource);
        copy.refine02_matchDoubleCurves(pSource, pPixelMap);
        copy.refine03_matchCurves(pPixelMap, pSource);

        if (copy.containsStraightSegment()) {
            PixelChain reversed = copy.reverse(pPixelMap);
            reversed.refine01_matchCurves(pPixelMap, pSource);
            reversed.refine02_matchDoubleCurves(pSource, pPixelMap);
            reversed.refine03_matchCurves(pPixelMap, pSource);
            if (reversed.countStraightSegments() < copy.countStraightSegments()) {
                return reversed;
            }
        }

        return copy;
    }

    private boolean containsStraightSegment() {
        return mSegments.stream()
                .filter(s -> (s instanceof StraightSegment))
                .findFirst()
                .isPresent();
    }

    private int countStraightSegments() {
        var count = Counter.createCounter();
        mSegments.stream()
                .forEach(s -> count.increase());
        return count.getCount();
    }

    private void refine03_matchCurves(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {

        if (mSegments.size() == 1) {
            return;
        }

        for (final ISegment currentSegment : mSegments) {
            if (currentSegment == mSegments.firstElement()) {
                refine03FirstSegment(pPixelMap, pSource, currentSegment);
            } else if (currentSegment == mSegments.lastElement()) {
                //refineEndSegment(pPixelMap, pSource, currentSegment);
            } else {
                //refineMidSegment(pPixelMap, pSource, currentSegment);
            }
        }
        validate("refine03_matchCurves");
    }

    private void refine01_matchCurves(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {

        if (mSegments.size() == 1) {
            return;
        }

        for (final ISegment currentSegment : mSegments) {
            if (currentSegment == mSegments.firstElement()) {
                refine01FirstSegment(pPixelMap, pSource, currentSegment);
            } else if (currentSegment == mSegments.lastElement()) {
                refine01EndSegment(pPixelMap, pSource, currentSegment);
            } else {
                refine01MidSegment(pPixelMap, pSource, currentSegment);
            }
        }
        validate("refine01_matchCurves");
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
            mSegments.set(pCurrentSegment.getSegmentIndex(), bestSegment);
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
            mSegments.set(pCurrentSegment.getSegmentIndex(), bestSegment);
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
                    mSegments.set(pCurrentSegment.getSegmentIndex(), candidateSegment);
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
            mSegments.set(pCurrentSegment.getSegmentIndex(), bestSegment);
        }
    }

    private void refine03FirstSegment(
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
            getPegCounter().increase(PegCounters.StartSegmentStraightToCurveAttempted);
            var lowestError = pCurrentSegment.calcError(pPixelMap, this) * 1000 * pSource.getLineCurvePreference();
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
                mVertexes.set(originalEndVertex.getVertexIndex(), originalEndVertex);
                var lambda = (double) i / nextSegmentPixelLength;
                var controlPointStart = originalNextSegment.getPointFromLambda(pPixelMap, this, lambda);
                var candidateVertex = Vertex.createVertex(this, originalEndVertex.getVertexIndex(), originalEndVertex.getPixelIndex() + i, controlPointStart);
                mVertexes.set(candidateVertex.getVertexIndex(), candidateVertex);
                var controlPoints = new Line(controlPointEnd, controlPointStart).stream(100).collect(Collectors.toList());
                for (var controlPoint : controlPoints) {
                    var candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, pCurrentSegment.getSegmentIndex(), controlPoint);
                    if (candidateSegment != null) {
                        mSegments.set(pCurrentSegment.getSegmentIndex(), candidateSegment);
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
                getPegCounter().increase(PegCounters.StartSegmentStraightToCurveSuccessful);
                mVertexes.set(bestCandidateVertex.getVertexIndex(), bestCandidateVertex);
                mSegments.set(bestCandidateSegment.getSegmentIndex(), bestCandidateSegment);
                System.out.println("Pixel for curve: " + bestCandidateVertex.getPixel(this));
            }
        }
    }

    private void refine01_matchCurves(
            final PixelMap pPixelMap,
            final IPixelMapTransformSource pSource,
            final boolean pInitialRun
    ) {

        if (mSegments.size() == 1) {
            return;
        }

        for (final ISegment currentSegment : mSegments) {

            // get error values from straight line to start the compare
            var bestSegment = currentSegment;
            var bestStartVertex = currentSegment.getStartVertex(this);
            var originalEndVertex = currentSegment.getEndVertex(this);
            var bestEndVertex = originalEndVertex;
            var lowestError = pSource.getLineCurvePreference() *
                    SegmentFactory.createTempStraightSegment(pPixelMap, this, currentSegment.getSegmentIndex())
                            .calcError(pPixelMap, this);

            if (getThickness() != Thickness.Thick) {
                lowestError *= 2; // TODO just a thought
            }

            if (currentSegment == mSegments.firstElement()) {
                // first segment
                try {

                    var nextSegment = currentSegment.getNextSegment(this);
                    var nextSegmentIsLine = nextSegment instanceof StraightSegment;

                    for (int j = 0; j < nextSegment.getPixelLength(this); j++) { // TODO neeed to set loop up so that it only loops when next line is straight
                        if (nextSegmentIsLine) {
                            if (j != 0) {
                                var lambda = j / (2.0d * nextSegment.getPixelLength(this));
                                var position = nextSegment.getPointFromLambda(pPixelMap, this, lambda);
                                var candidateEndVertex = bestEndVertex.withPosition(this, position);
                                mVertexes.set(candidateEndVertex.getVertexIndex(), candidateEndVertex);
                            }
                        } else if (j != 0) {
                            break; // TODO need to set loop up better
                        }

                        var tangent = calcEndTangent(pPixelMap, currentSegment, pInitialRun);
                        // find closest point between start point and tangent line
                        final Point closest = tangent.closestPoint(currentSegment.getStartUHVWPoint(pPixelMap, this));
                        // divide this line (tangentRuler) into the number of pixels in the segment
                        // for each of the points on the division find the lowest error
                        final LineSegment tangentRuler = new LineSegment(closest, currentSegment.getEndUHVWPoint(pPixelMap, this));
                        for (int i = 1; i < currentSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                            try {
                                final double lambda = (double) i / currentSegment.getPixelLength(this);
                                final Point p1 = tangentRuler.getPoint(lambda);
                                final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, currentSegment.getSegmentIndex(), p1);
                                mSegments.set(currentSegment.getSegmentIndex(), candidateSegment);
                                final double candidateError = candidateSegment != null ? candidateSegment.calcError(pPixelMap, this) : 0.0d;

                                if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                                    lowestError = candidateError;
                                    bestSegment = candidateSegment;
                                    bestEndVertex = candidateSegment.getEndVertex(this);
                                }

                            } catch (final Throwable pT) {
                                mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }
                        }
                    }
                } catch (final Throwable pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                } finally {
                    if (bestEndVertex != originalEndVertex) {
                        System.out.println("############################# " + originalEndVertex + " -> " + bestEndVertex);
                    }
                    mSegments.set(currentSegment.getSegmentIndex(), bestSegment);
                    mVertexes.set(bestEndVertex.getVertexIndex(), bestEndVertex);
                }

            } else if (currentSegment == mSegments.lastElement()) {
                // end segment
                var previousSegment = currentSegment.getPreviousSegment(this);
                var previousSegmentIsLine = previousSegment instanceof StraightSegment;
                try {
                    for (int j = 0; j < previousSegment.getPixelLength(this); j++) { // TODO neeed to set loop up so that it only loops when next line is straight
                        if (previousSegmentIsLine) {
                            var lambda = 1.0d - j / (2.0d * previousSegment.getPixelLength(this)); // TODO this is not needed for the j = 0 case
                            var position = previousSegment.getPointFromLambda(pPixelMap, this, lambda);
                            var candidateStartVertex = bestStartVertex.withPosition(this, position);
                            mVertexes.set(candidateStartVertex.getVertexIndex(), candidateStartVertex);
                        } else if (j != 0) {
                            break; // TODO need to set loop up better
                        }
                        var tangent = calcStartTangent(pPixelMap, currentSegment, pInitialRun);
                        // find closest point between start point and tangent line
                        final Point closest = tangent.closestPoint(currentSegment.getEndUHVWPoint(pPixelMap, this));
                        // divide this line (tangentRuler) into the number of pixels in the segment
                        // for each of the points on the division find the lowest error
                        final Line tangentRuler = new Line(currentSegment.getStartUHVWPoint(pPixelMap, this), closest);
                        for (int i = 1; i < currentSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                            try {
                                final double lambda = (double) i / currentSegment.getPixelLength(this);
                                final Point p1 = tangentRuler.getPoint(lambda);
                                final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, currentSegment.getSegmentIndex(), p1);
                                if (candidateSegment != null) {
                                    final double candidateError = candidateSegment.calcError(pPixelMap, this);

                                    if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                                        lowestError = candidateError;
                                        bestSegment = candidateSegment;
                                        bestStartVertex = candidateSegment.getStartVertex(this);
                                    }
                                }
                            } catch (final Throwable pT) {
                                mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }
                        }
                    }
                } catch (final Throwable pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                } finally {
                    mSegments.set(currentSegment.getSegmentIndex(), bestSegment);
                }

            } else {
                // mid segment
                // get tangent at start and end
                // calculate intersection
                // what if they are parallel ? -- ignore as the initial estimate is not good enough
                // see if it is closer than the line
                try {
                    // // method 1 - looking at blending the gradients
                    try {
                        var startTangent = calcStartTangent(pPixelMap, currentSegment, pInitialRun);
                        var endTangent = calcEndTangent(pPixelMap, currentSegment, pInitialRun);

                        if (startTangent != null && endTangent != null) {
                            Point p1 = startTangent.intersect(endTangent);
                            if (p1 != null && startTangent.closestLambda(p1) > 0.0d && endTangent.closestLambda(p1) < 0.0d) {
                                final Line newStartTangent = new Line(p1, currentSegment.getStartVertex(this).getUHVWPoint(pPixelMap, this));
                                final Line newEndTangent = new Line(p1, currentSegment.getEndVertex(this).getUHVWPoint(pPixelMap, this));
                                p1 = newStartTangent.intersect(newEndTangent);
                                // if (p1 != null && newStartTangent.getAB().dot(startTangent.getAB()) > 0.0d && newEndTangent.getAB().dot(endTangent.getAB()) > 0.0d) {
                                if (p1 != null) {
                                    final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(pPixelMap, this, currentSegment.getSegmentIndex(), p1);
                                    final double candidateError = candidateSegment.calcError(pPixelMap, this);

                                    if (isValid(pPixelMap, candidateSegment) && candidateError < lowestError) {
                                        bestSegment = candidateSegment;
                                    }
                                }
                                // }
                            }
                        }
                    } catch (final Throwable pT) {
                        mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                    } finally {
                        mSegments.set(currentSegment.getSegmentIndex(), bestSegment);
                    }

                } catch (final Throwable pT) {
                    mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                }
            }

        } // end loop
        validate("refine01_matchCurves");
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

    private void refine02_matchDoubleCurves(final IPixelMapTransformSource pSource, final PixelMap pPixelMap) {
        if (mSegments.size() == 1) return;
        for (final ISegment currentSegment : mSegments) {
            if (currentSegment instanceof CurveSegment) continue;

            final IVertex startVertex = currentSegment.getStartVertex(this);
            final IVertex endVertex = currentSegment.getEndVertex(this);
            final Line startTangent = getDCStartTangent(pPixelMap, currentSegment);
            final Line endTangent = getDCEndTangent(pPixelMap, currentSegment);

            ISegment bestCandidate = currentSegment;

            // get error values from straight line to start the compare
            double lowestError = currentSegment.calcError(pPixelMap, this);
            if (currentSegment instanceof StraightSegment) {
                lowestError *= pSource.getLineCurvePreference();
            }

            try {
                try {
                    for (int i = 1; i < currentSegment.getPixelLength(this) - 1; i++) { // first and last pixel will throw an error and are equivalent to the straight line
                        final Vertex midVertex = Vertex.createVertex(this, mVertexes.size(), startVertex.getPixelIndex() + i);

                        final Point closestStart = startTangent.closestPoint(midVertex.getUHVWPoint(pPixelMap, this));
                        final Line startLine = new Line(startVertex.getUHVWPoint(pPixelMap, this), closestStart);

                        final Point closestEnd = endTangent.closestPoint(midVertex.getUHVWPoint(pPixelMap, this));
                        final Line endLine = new Line(endVertex.getUHVWPoint(pPixelMap, this), closestEnd);
                        // should check that lambdas are the correct sign

                        for (double sigma = 0.3d; sigma < 0.7d; sigma += 0.1d) {
                            try {
                                // try from start
                                final Point p1 = startLine.getPoint(sigma);
                                final Point p2 = new Line(p1, midVertex.getUHVWPoint(pPixelMap, this)).intersect(endLine);
                                if (p2 != null) {
                                    final double closestLambda = endLine.closestLambda(p2);

                                    if (closestLambda > 0.1d && closestLambda < 1.2d) {

                                        final ISegment candidateCurve = SegmentFactory.createTempDoubleCurveSegment(pPixelMap, this, currentSegment.getSegmentIndex(), p1, midVertex, p2);
                                        mSegments.set(currentSegment.getSegmentIndex(), candidateCurve);
                                        if (isValid(pPixelMap, candidateCurve)) {
                                            final double candidateError = candidateCurve.calcError(pPixelMap, this);
                                            if (candidateError < lowestError) {
                                                bestCandidate = candidateCurve;
                                            }
                                        }
                                    }
                                }

                            } catch (final Throwable pT) {
                                //mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }

                            // try from end
                            try {
                                final Point p2 = endLine.getPoint(sigma);
                                final Point p1 = new Line(p2, midVertex.getUHVWPoint(pPixelMap, this)).intersect(startLine);
                                final double closestLambda = startLine.closestLambda(p2);

                                if (p1 != null && 0.1d < closestLambda && closestLambda < 1.2d) { // TODO what are these magic numbers

                                    final ISegment candidateCurve = SegmentFactory.createTempDoubleCurveSegment(pPixelMap, this, currentSegment.getSegmentIndex(), p1, midVertex, p2);
                                    mSegments.set(currentSegment.getSegmentIndex(), candidateCurve);
                                    if (isValid(pPixelMap, candidateCurve)) {
                                        final double candidateError = candidateCurve.calcError(pPixelMap, this);
                                        if (candidateError < lowestError) {
                                            bestCandidate = candidateCurve;
                                        }
                                    }
                                }
                            } catch (final Throwable pT) {
                                //mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }
                        }
                    }
                } catch (final Throwable pT) {
                    mLogger.severe(FrameworkLogger.throwableToString(pT));
                }
            } finally {
                if (bestCandidate instanceof DoubleCurveSegment) mLogger.fine("DoubleCurve added");
                mSegments.set(currentSegment.getSegmentIndex(), bestCandidate);
            }
        } // end loop

        validate("refine03_matchDoubleCurves");
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
        validate("reverse");
        PixelChain clone = deepCopy();

        // reverse pixels
        Collections.reverse(clone.mPixels);

        // reverse vertexes
        final int maxPixelIndex = clone.mPixels.size() - 1;
        final Vector<IVertex> vertexes = new Vector<>();
        for (int i = clone.mVertexes.size() - 1; i >= 0; i--) {
            final IVertex vertex = clone.mVertexes.get(i);
            final IVertex v = Vertex.createVertex(clone, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
        }
        clone.mVertexes = vertexes;

        // reverse segments
        final Vector<ISegment> segments = new Vector<>();
        for (int i = clone.mVertexes.size() - 1; i >= 0; i--) {
            if (i != mVertexes.size() - 1) {
                final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, clone, segments.size());
                segments.add(newSegment);
            }
        }
        clone.mSegments = segments;

        clone.validate("reverse");
        return clone;
    }

    PixelChain setEndNode(final PixelMap pPixelMap, final Node pNode) {
        Framework.checkParameterNotNull(mLogger, pNode, "pNode");

        PixelChain copy = this.deepCopy();
        copy.mPixels.add(pNode);

        // need to do a check here to see if we are clobbering over another chain
        // if pixel end-2 is a neighbour of pixel end then pixel end-1 needs to be set as notVisited and removed from the chain
        if (copy.count() >= 3 && pNode.isNeighbour(copy.mPixels.get(copy.count() - 3))) {
            copy.mPixels.remove(copy.count() - 2).setVisited(pPixelMap, false);
        }
        return copy;
    }

    private void setLength(final double pLength) {
        mLength = pLength;
    }

    private void setStartNode(final Node pNode) {
        // this is a mutating method and should only be called from the constructor
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pNode", pNode);

        if (mPixels.size() != 0) {
            throw new IllegalStateException("Can not change the start node of a PixelChain once it has been set.  Current mStartNode = " + mPixels.firstElement());
        }
        if (pNode == null) {
            throw new IllegalArgumentException("Can not change the start node to a null value");
        }

        mPixels.add(pNode);

        setStartVertex(Vertex.createVertex(this, mVertexes.size(), 0));

        Framework.logExit(mLogger);
    }

    private void setStartVertex(final IVertex pVertex) {
        if (mVertexes.size() >= 1) mVertexes.remove(0);
        mVertexes.add(0, pVertex);
    }

    PixelChain setThickness(final int pThinLength, final int pNormalLength, final int pLongLength) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pNormalLength, pLongLength", pNormalLength, pLongLength);

        Thickness newThickness;
        if (length() < pThinLength) {
            newThickness = Thickness.None;
        } else if (length() < pNormalLength) {
            newThickness = Thickness.Thin;
        } else if (length() < pLongLength) {
            newThickness = Thickness.Normal;
        } else {
            newThickness = Thickness.Thick;
        }

        PixelChain result;
        if (newThickness == mThickness) result = this;
        else {
            result = deepCopy();
            result.mThickness = newThickness;
        }
        return result;
    }

    public PixelChain setThickness(final Thickness pThickness) {
        Framework.logEntry(mLogger);
        if (pThickness == mThickness) return this;
        PixelChain clone = deepCopy();
        clone.mThickness = pThickness;
        return clone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");

        boolean firstElement = true;
        for (final Pixel p : mPixels) {
            if (!firstElement) {
                sb.append(", ");
            }
            firstElement = false;
            sb.append(p);
        }

        sb.append(" ]\n");

        return sb.toString();
    }

    void validate(final String pMethodName) {
        try {
            if (getStartVertex().getPixelIndex() != 0) {
                throw new IllegalStateException("getStartVertex().getPixelIndex() != 0");
            }

            if ((mVertexes.size() == 0 && mSegments.size() != 0)) {
                throw new IllegalStateException(String.format("mVertexes.size() = %s && mSegments.size() = %s", mVertexes.size(), mSegments.size()));
            }

            if (mVertexes.size() != 0 && mSegments.size() + 1 != mVertexes.size()) {
                throw new IllegalStateException(String.format("mVertexes.size() = %s && mSegments.size() = %s", mVertexes.size(), mSegments.size()));
            }

            int nextStartIndex = 0;

            for (final ISegment segment : mSegments) {
                if (segment.getStartIndex(this) != nextStartIndex) { //
                    throw new IllegalStateException("segments not linked properly");
                }
                nextStartIndex = segment.getEndIndex(this);
            }

            if (mSegments.size() != 0 && mSegments.lastElement().getEndIndex(this) != mPixels.size() - 1) { //
                throw new IllegalStateException(String.format("last segment not linked properly, %s, %s, %s", mSegments.size(), mSegments.lastElement().getEndIndex(this), mPixels.size() - 1));
            }

            if (mSegments.size() == 0) { //
                throw new IllegalStateException("there are no segments");
            }

            checkAllVertexesAttached();

            IVertex vertex = getStartVertex();
            int index = 0;
            while (vertex != null) {
                if (mVertexes.get(vertex.getVertexIndex()) != vertex) {
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

            if (mVertexes.size() != 0) {
                if (mVertexes.firstElement().getStartSegment(this) != null)
                    throw new RuntimeException("wrong start vertex");
                if (mVertexes.lastElement().getEndSegment(this) != null) throw new RuntimeException("wrong end vertex");
            }

            int currentMax = -1;
            for (int i = 0; i < mVertexes.size(); i++) {
                final IVertex v = mVertexes.get(i);
                if (i == 0 && v.getPixelIndex() != 0) throw new IllegalStateException("First vertex wrong)");
                if (i == mVertexes.size() - 1 && v.getPixelIndex() != mPixels.size() - 1)
                    throw new IllegalStateException("Last vertex wrong)");
                if (v.getPixelIndex() <= currentMax) throw new IllegalStateException("Wrong pixel index order");
                currentMax = v.getPixelIndex();
                if (i != 0 && v.getStartSegment(this) != mSegments.get(i - 1))
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                if (i != mVertexes.size() - 1 && v.getEndSegment(this) != mSegments.get(i))
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
            }
        } catch (final Throwable pT) {
            printVertexs();
            throw pT;
        }
    }

    public void printVertexs() {
        final StringBuilder sb = new StringBuilder()
                .append(String.format("mVertexes.size() = %s, mSegments.size() = %s", mVertexes.size(), mSegments.size()))
                .append("\nArrray\n");

        for (int i = 0; i < mVertexes.size(); i++) {
            sb.append(String.format("i = %s, mVertexes.get(i).getVertexIndex() = %s\n", i, mVertexes.get(i).getVertexIndex()));
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

    int getPixelLength() {
        return mPixels.size();
    }

    ISegment getSegment(final int i) {
        if (mSegments.size() <= i || i < 0) return null;
        return mSegments.get(i);
    }

    public IVertex getVertex(final int i) {
        if (mVertexes.size() <= i || i < 0) return null;
        return mVertexes.get(i);
    }

    void setInChain(final PixelMap pPixelMap, final boolean pValue) {
        for (final Pixel pixel : mPixels) {
            pixel.setInChain(pPixelMap, pValue);
        }
    }

    private void setEdge(final PixelMap pPixelMap) {
        mPixels.stream()
                .filter(p -> p != mPixels.firstElement())
                .filter(p -> p != mPixels.lastElement())
                .forEach(p -> p.setEdge(pPixelMap, false));
        mPixels.stream()
                .filter(pPixel -> pPixel.isNode(pPixelMap))
                .filter(p -> p.countEdgeNeighbours(pPixelMap) < 2 || p.countNodeNeighbours(pPixelMap) == 2)
                .forEach(p -> p.setEdge(pPixelMap, false));
    }

    void setVisited(final PixelMap pPixelMap, final boolean pValue) {
        for (final Pixel pixel : mPixels) {
            pixel.setVisited(pPixelMap, pValue);
        }
    }

    public void delete(final PixelMap pPixelMap) {
        mLogger.fine("delete");
        setInChain(pPixelMap, false);
        setVisited(pPixelMap, false);
        setEdge(pPixelMap);
    }

}

