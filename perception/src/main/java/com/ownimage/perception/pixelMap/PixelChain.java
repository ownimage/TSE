/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.LineSegment;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.DoubleCurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.transform.CannyEdgeTransform;

import io.vavr.Tuple4;

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


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 2L;
    transient private PixelMap mPixelMap;
    transient private double mLength;
    private Vector<Pixel> mPixels = new Vector<>();
    private Vector<ISegment> mSegments = new Vector<>();
    private Vector<IVertex> mVertexes = new Vector<>();
    private Node mStartNode;
    private Node mEndNode;

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
        mPixelMap = pStartNode.getPixelMap();
        setStartNode(pStartNode);
    }

    Stream<Pixel> streamPixels() {
        return mPixels.stream();
    }

    public boolean add(final Pixel pPixel) {
        if (mPixelMap != pPixel.getPixelMap()) { //
            throw new IllegalArgumentException("Pixel added does not come from the correct PixelMap");
        }

        return mPixels.add(pPixel);
    }

    /**
     * Adds the two pixel chains together. It allocates all of the pixels from the pOtherChain to this, unattaches both chains from the middle node, and adds all of the segments from the second chain
     * to the first (joining at the appropriate vertex in the middle, and using the correct offset for the new vertexes). Note that the new segments that are copied from the pOtherChain are all
     * LineApproximations.
     *
     * @param pOtherChain the other chain
     */
    private void add(final PixelChain pOtherChain) {
        validate("add");
        pOtherChain.validate("add pOtherChain");
        mLogger.fine(() -> String.format("this.mPixels.size() = %s", mPixels.size()));
        mLogger.fine(() -> String.format("pOtherChain.mPixels.size() = %s", pOtherChain.mPixels.size()));
        mLogger.fine(() -> String.format("this.mSegments.size() = %s", mSegments.size()));
        mLogger.fine(() -> String.format("pOtherChain.mSegments.size() = %s", pOtherChain.mSegments.size()));
        if (mLogger.isLoggable(Level.FINE)) {
            mSegments.forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
            mSegments.forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
            pOtherChain.mSegments.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartVertex(pOtherChain).getPixelIndex(), s.getEndVertex(pOtherChain).getPixelIndex())));
            pOtherChain.mSegments.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartIndex(pOtherChain), s.getEndIndex(pOtherChain))));
        }

        validate("add");
        pOtherChain.validate("add");

        if (getPixelMap() != pOtherChain.getPixelMap()) {
            throw new IllegalArgumentException("PixelMap must be the same for both chains");
        }

        if (!lastPixel().equals(pOtherChain.firstPixel())) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + pOtherChain);
        }

        final int offset = mPixels.size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed
        mPixels.removeElementAt(mPixels.size() - 1);// need to remove the last pixel as it will be duplicated on the other chain;
        mPixels.addAll(pOtherChain.mPixels);
        mLogger.fine(() -> String.format("offset = %s", offset));

        for (final ISegment segment : pOtherChain.mSegments) {
            final IVertex end = Vertex.createVertex(this, mVertexes.size(), segment.getEndIndex(pOtherChain) + offset);
            mVertexes.add(end);
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(this, mSegments.size());
            mSegments.add(newSegment);
        }

        pOtherChain.mSegments.removeAllElements(); // TODO
        getEndNode().removePixelChain(this);
        pOtherChain.getEndNode().removePixelChain(pOtherChain);
        pOtherChain.getStartNode().removePixelChain(pOtherChain);

        mEndNode = pOtherChain.getEndNode(); // not using setEndNode here as this adds the Node pixel to the end of the pixelChain
        getEndNode().addPixelChain(this);
        getPixelMap().removePixelChain(pOtherChain);

        mSegments.forEach(s -> mLogger.fine(() -> String.format("out..mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("out.is.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
        validate("add");
    }


    void addToNodes() {
        getStartNode().addPixelChain(this);

        if (mEndNode != null) {
            if (!mEndNode.equals(getStartNode())) {
                mEndNode.addPixelChain(this);
            }
        } else {
            final Pixel end = mPixels.lastElement();
            mEndNode = new Node(end);
            mEndNode.addPixelChain(this);
            if (end.getX() != 0 && end.getY() != 0) {
                mLogger.info(() -> "mEndNode bodge");
                mLogger.info(this::toString);
                //end.printNeighbours(5);
            }
        }
    }


    public void approximate() {
        //mLogger.info(() -> "PixelChain::approximate");
        double tolerance = mPixelMap.getLineTolerance() / mPixelMap.getHeight();
        //mLogger.info(() -> "tolerance " + tolerance);
        mSegments = new Vector<>();
        approximate01_straightLines(tolerance);
        //mLogger.info(() -> "approximate01_straightLines - done");
        approximate02_refineCorners();
        //mLogger.info(() -> "approximate02_refineCorners - done");
        refine();
        //mLogger.info(() -> "refine - done");
        checkAllVertexesAttached();
        //mLogger.info(() -> "checkAllVertexesAttached - done");
        //mLogger.info(() -> "PixelChain::approximate - end");
    }

    void approximate01_straightLines(final double pTolerance) {
        // note that this is version will find the longest line that is close to all pixels.
        // there are cases where a line of length n will be close enough, a line of length n+1 will not be, but there exists an m such that a line of length m is close enough.
        mSegments.removeAllElements();
        mVertexes.removeAllElements();

        if (mPixels.size() <= 1) {
            return;
        }

        IVertex startVertex = Vertex.createVertex(this, 0, 0);
        setStartVertex(startVertex);

        int maxIndex = 0;
        IVertex maxVertex = null;
        StraightSegment maxSegment = null;

        int endIndex = 1;

        while (endIndex < mPixels.size()) {
            int vertexIndex = mVertexes.size();
            mVertexes.add(null);
            int segmentIndex = mSegments.size();
            mSegments.add(null);

            for (int index = endIndex; index < mPixels.size(); index++) {
                final IVertex candidateVertex = Vertex.createVertex(this, vertexIndex, index);
                mVertexes.set(vertexIndex, candidateVertex);
                final StraightSegment candidateSegment = SegmentFactory.createTempStraightSegment(this, segmentIndex);
                mSegments.set(segmentIndex, candidateSegment);

                if (candidateSegment.noPixelFurtherThan(this, pTolerance)) {
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
        indexSegments();
    }

    void approximate02_refineCorners() {
        for (final ISegment segment : mSegments) {
            if (segment == mSegments.lastElement()) return;

            int firstSegmentIndex = segment.getSegmentIndex();
            int secondSegmentIndex = firstSegmentIndex + 1;
            int midVertexIndex = secondSegmentIndex;

            IVertex midVertex = getVertex(midVertexIndex);
            ISegment firstSegment = segment;
            ISegment secondSegment = getSegment(segment.getSegmentIndex() + 1);

            final int minVertexIndex = (segment.getStartVertex(this).getPixelIndex() + segment.getEndVertex(this).getPixelIndex()) / 2;
            final int maxVertexIndex = (secondSegment.getStartVertex(this).getPixelIndex() + secondSegment.getEndVertex(this).getPixelIndex()) / 2;

            double currentError = segment.calcError(this) + secondSegment.calcError(this);
            Tuple4<Double, ISegment, IVertex, ISegment> best = new Tuple4<>(currentError, firstSegment, midVertex, secondSegment);

            // the check below is needed as some segments may only be one index length so generating a midpoint might generate an invalid segment
            if (minVertexIndex < midVertexIndex && midVertexIndex < maxVertexIndex) {
                for (int candidateIndex = minVertexIndex + 1; candidateIndex < maxVertexIndex; candidateIndex++) {
                    midVertex = Vertex.createVertex(this, midVertexIndex, candidateIndex);
                    mVertexes.set(midVertexIndex, midVertex);
                    firstSegment = SegmentFactory.createTempStraightSegment(this, firstSegmentIndex);
                    mSegments.set(firstSegmentIndex, firstSegment);
                    secondSegment = SegmentFactory.createTempStraightSegment(this, secondSegmentIndex);
                    mSegments.set(secondSegmentIndex, secondSegment);

                    currentError = segment.calcError(this) + secondSegment.calcError(this);

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

    public boolean contains(final Pixel pPixel) {
        return mPixels.contains(pPixel);
    }

    public int count() {
        return mPixels.size();
    }

    Pixel firstPixel() {
        return mPixels.firstElement();
    }

    private double getActualCurvedThickness(final IPixelMapTransformSource pLineInfo, final double pFraction) {
        final double c = pLineInfo.getLineEndThickness() * getWidth();
        final double a = c - getWidth();
        final double b = -2.0 * a;
        return a * pFraction * pFraction + b * pFraction + c;
    }

    private double getActualSquareThickness(final IPixelMapTransformSource pLineInfo, final double pFraction) {
        return getWidth();
    }

    private double getActualStraightThickness(final IPixelMapTransformSource pLineInfo, final double pFraction) {
        final double min = pLineInfo.getLineEndThickness() * getWidth();
        final double max = getWidth();
        return min + pFraction * (max - min);
    }

    /**
     * Gets the actual thickness of the line. This allows for the tapering of the line at the ends.
     *
     * @param pPosition the position
     * @return the actual thickness
     */
    public double getActualThickness(final double pPosition) {
        // TODO needs refinement should not really pass the pTolerance in as this can be determined from the PixelChain.
        // TODO this could be improved for performance
        final IPixelMapTransformSource lineInfo = mPixelMap.getTransformSource();
        final double fraction = getActualThicknessEndFraction(lineInfo, pPosition);

        switch (lineInfo.getLineEndShape()) {
            case Curved:
                return getActualCurvedThickness(lineInfo, fraction);
            case Square:
                return getActualSquareThickness(lineInfo, fraction);
        }
        // fall through to straight
        return getActualStraightThickness(lineInfo, fraction);
    }

    /**
     * Gets fraction of the way along the end segment that the pPosition is. 0 would mean at the thinnest end. 1 would mean full thickness.
     *
     * @param pPosition the position
     * @return the actual thickness end fraction
     */
    private double getActualThicknessEndFraction(final IPixelMapTransformSource pInfo, final double pPosition) {

        final double end2 = getLength() - pPosition;
        final double closestEnd = Math.min(pPosition, end2);

        if (pInfo.getLineEndLengthType() == CannyEdgeTransform.LineEndLengthType.Percent) {
            final double closestPercent = 100.0d * closestEnd / getLength();
            return Math.min(closestPercent / pInfo.getLineEndLengthPercent(), 1.0d);
        }

        // type is Pixels
        final double fraction = pInfo.getHeight() * closestEnd / pInfo.getLineEndLengthPixel();
        return Math.min(fraction, 1.0d);

    }

    public Iterable<ISegment> getAllSegments() {
        return mSegments;
    }

    public Node getEndNode() {
        return mEndNode;
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
    public Pixel getPixel(final int pIndex) {
        if (pIndex < 0 || pIndex > length()) {
            throw new IllegalArgumentException("pIndex, currently: " + pIndex + " must be between 0 and the length of mPixels, currently: " + length());
        }

        return mPixels.elementAt(pIndex);
    }

    private PixelMap getPixelMap() {
        return mPixelMap;
    }

    public int getSegmentCount() {
        Framework.logEntry(mLogger);
        final int result = mSegments.size();
        Framework.logExit(mLogger);
        return result;
    }

    public Node getStartNode() {
        return mStartNode;
    }

    private IVertex getStartVertex() {
        return mVertexes.size() != 0
                ? mVertexes.firstElement()
                : null;
    }

    public synchronized Thickness getThickness() {
        if (mThickness == null) {
            mThickness = Thickness.Normal;
        }
        return mThickness;
    }

    private IPixelMapTransformSource getTransformSource() {
        return getPixelMap().getTransformSource();
    }

    /**
     * Gets the UHVW value of the Pixel at the specified position.
     *
     * @param pIndex the index
     * @return the UHVW Point
     */
    public Point getUHVWPoint(final int pIndex) {
        if (pIndex < 0 || pIndex > length()) {
            throw new IllegalArgumentException("pIndex, currently: " + pIndex + " must be between 0 and the length of mPixels, currently: " + length());
        }

        return mPixels.elementAt(pIndex).getUHVWPoint();
    }

    private double getWidth() {
        Framework.logEntry(mLogger);

        double width = 0.0d;
        switch (getThickness()) {
            case Thin:
                width = getPixelMap().getThinWidth();
                break;
            case Normal:
                width = getPixelMap().getNormalWidth();
                break;
            case Thick:
                width = getPixelMap().getThickWidth();
                break;
        }

        Framework.logExit(mLogger);
        return width;

    }

    PixelChain indexSegments() {
        try {
            PixelChain pixelChainClone = (PixelChain) clone();
            double startPosition = 0.0d;
            for (final ISegment segment : pixelChainClone.mSegments) {
                ISegment segmentClone = segment.withStartPosition(pixelChainClone, startPosition);
                getPixelMap().index(pixelChainClone, segmentClone);
                pixelChainClone.mSegments.set(segment.getSegmentIndex(), segmentClone);
                startPosition += segment.getLength(pixelChainClone);
            }
            pixelChainClone.setLength(startPosition);
            return pixelChainClone;
        } catch (CloneNotSupportedException pCNSE) {
            throw new RuntimeException("Clone not supported", pCNSE);
        }
    }

    private boolean isValid(final ISegment pSegment) { // need to maks sure that not only the pixels are close to the line but the line is close to the pixels
        if (pSegment == null) return false;
        if (pSegment.getPixelLength(this) < 4) return true;

        final int startIndexPlus = pSegment.getStartIndex(this) + 1;
        final Point startPointPlus = getPixel(startIndexPlus).getUHVWPoint();
        final double startPlusLambda = pSegment.closestLambda(startPointPlus, this);

        final int endIndexMinus = pSegment.getEndIndex(this) - 1;
        final Point endPointMinus = getPixel(endIndexMinus).getUHVWPoint();
        final double endMinusLambda = pSegment.closestLambda(endPointMinus, this);

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
     * @param pOtherChain the other chain
     * @param pNode       the node
     */
    public void merge(final PixelChain pOtherChain, final Node pNode) {
        if (!(getStartNode() == pNode || getEndNode() == pNode) || !(pOtherChain.getStartNode() == pNode || pOtherChain.getEndNode() == pNode)) {
            throw new IllegalArgumentException("Either this PixelChain: " + this + ", and pOtherChain: " + pOtherChain + ", must share the following node:" + pNode);
        }

        if (getEndNode() != pNode) {
            reverse();
        }

        if (pOtherChain.getStartNode() != pNode) {
            pOtherChain.reverse();
        }

        if (getEndNode() != pNode || pOtherChain.getStartNode() != pNode) {
            throw new RuntimeException("This PixelChain: " + this + " should end on the same node as the other PixelChain: " + pOtherChain + " starts with.");
        }

        add(pOtherChain);
    }

    void refine() {
        refine01_matchCurves();
        refine03_matchDoubleCurves();
    }

    private void refine01_matchCurves() {

        if (mSegments.size() == 1) {
            return;
        }

        for (final ISegment currentSegment : mSegments) {

            // get error values from straight line to start the compare
            ISegment bestSegment = currentSegment;
            double lowestError = currentSegment.calcError(this);
            lowestError *= getTransformSource().getLineCurvePreference();

            if (currentSegment == mSegments.firstElement()) {
                // first segment
                try {
                    // calculate end tangent
                    final Line tangent = currentSegment.getEndVertex(this).calcTangent(this);

                    if (tangent == null) {
                        // this was for test purposes
                        final Line tangent2 = currentSegment.getEndVertex(this).calcTangent(this);
                    }
                    // find closest point between start point and tangent line
                    final Point closest = tangent.closestPoint(currentSegment.getStartUHVWPoint(this));
                    // divide this line (tangentRuler) into the number of pixels in the segment
                    // for each of the points on the division find the lowest error
                    final LineSegment tangentRuler = new LineSegment(closest, currentSegment.getEndUHVWPoint(this));
                    for (int i = 1; i < currentSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                        try {
                            final double lambda = (double) i / currentSegment.getPixelLength(this);
                            final Point p1 = tangentRuler.getPoint(lambda);
                            final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(this, currentSegment.getSegmentIndex(), p1);
                            mSegments.set(currentSegment.getSegmentIndex(), candidateSegment);
                            final double candidateError = candidateSegment != null ? candidateSegment.calcError(this) : 0.0d;

                            if (isValid(candidateSegment) && candidateError < lowestError) {
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
                    mSegments.set(currentSegment.getSegmentIndex(), bestSegment);
                }

            } else if (currentSegment == mSegments.lastElement()) {
                // end segment
                try {
                    // calculate start tangent
                    final Line tangent = currentSegment.getStartVertex(this).calcTangent(this);
                    // find closest point between start point and tangent line
                    if (tangent == null) {
                        final Line tangent2 = currentSegment.getStartVertex(this).calcTangent(this);
                    }
                    final Point closest = tangent.closestPoint(currentSegment.getEndUHVWPoint(this));
                    // divide this line (tangentRuler) into the number of pixels in the segment
                    // for each of the points on the division find the lowest error
                    final Line tangentRuler = new Line(currentSegment.getStartUHVWPoint(this), closest);
                    for (int i = 1; i < currentSegment.getPixelLength(this); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                        try {
                            final double lambda = (double) i / currentSegment.getPixelLength(this);
                            final Point p1 = tangentRuler.getPoint(lambda);
                            final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(this, currentSegment.getSegmentIndex(), p1);
                            if (candidateSegment != null) {
                                final double candidateError = candidateSegment.calcError(this);

                                if (isValid(candidateSegment) && candidateError < lowestError) {
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
                        Line startTangent = currentSegment.getStartVertex(this).calcTangent(this);
                        Line endTangent = currentSegment.getEndVertex(this).calcTangent(this);

                        if (startTangent != null && endTangent != null) {
                            Point p1 = startTangent.intersect(endTangent);
                            if (p1 != null && startTangent.closestLambda(p1) > 0.0d && endTangent.closestLambda(p1) < 0.0d) {
                                Line newStartTangent = new Line(p1, currentSegment.getStartVertex(this).getUHVWPoint(this));
                                Line newEndTangent = new Line(p1, currentSegment.getEndVertex(this).getUHVWPoint(this));
                                p1 = newStartTangent.intersect(newEndTangent);
                                // if (p1 != null && newStartTangent.getAB().dot(startTangent.getAB()) > 0.0d && newEndTangent.getAB().dot(endTangent.getAB()) > 0.0d) {
                                final ISegment candidateSegment = SegmentFactory.createTempCurveSegmentTowards(this, currentSegment.getSegmentIndex(), p1);
                                double candidateError = candidateSegment.calcError(this);

                                if (isValid(candidateSegment) && candidateError < lowestError) {
                                    lowestError = candidateError;
                                    bestSegment = candidateSegment;
                                }
                                // }
                            }
                        }
                    } catch (Throwable pT) {
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

    private Line getDCStartTangent(ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex startVertex = pSegment.getStartVertex(this);
        if (startVertex.getStartSegment(this) instanceof StraightSegment) {
            return startVertex.getStartSegment(this).getEndTangent(this);
        }

        return startVertex.calcTangent(this);
    }

    private Line getDCEndTangent(ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex endVertex = pSegment.getEndVertex(this);
        if (endVertex.getEndSegment(this) instanceof StraightSegment) {
            return endVertex.getEndSegment(this).getStartTangent(this);
        }

        return endVertex.calcTangent(this);
    }


    private PixelChain copy() {
        PixelChain copy = new PixelChain(mStartNode);
        copy.mEndNode = mEndNode;
        copy.mPixels = mPixels;
        copy.mThickness = mThickness;
        return copy;
    }

    private void refine03_matchDoubleCurves() {
        if (mSegments.size() == 1) return;
        for (final ISegment currentSegment : mSegments) {
            if (currentSegment instanceof CurveSegment) continue;

            final IVertex startVertex = currentSegment.getStartVertex(this);
            final IVertex endVertex = currentSegment.getEndVertex(this);
            final Line startTangent = getDCStartTangent(currentSegment);
            final Line endTangent = getDCEndTangent(currentSegment);

            ISegment bestCandidate = currentSegment;

            // get error values from straight line to start the compare
            double lowestError = currentSegment.calcError(this);
            if (currentSegment instanceof StraightSegment) {
                lowestError *= getTransformSource().getLineCurvePreference();
            }

            try {
                try {
                    for (int i = 1; i < currentSegment.getPixelLength(this) - 1; i++) { // first and last pixel will throw an error and are equivalent to the straight line
                        final Vertex midVertex = Vertex.createVertex(this, mVertexes.size(), startVertex.getPixelIndex() + i);

                        Point closestStart = startTangent.closestPoint(midVertex.getUHVWPoint(this));
                        Line startLine = new Line(startVertex.getUHVWPoint(this), closestStart);

                        Point closestEnd = endTangent.closestPoint(midVertex.getUHVWPoint(this));
                        Line endLine = new Line(endVertex.getUHVWPoint(this), closestEnd);
                        // should check that lambdas are the correct sign

                        for (double sigma = 0.3d; sigma < 0.7d; sigma += 0.1d) {
                            try {
                                // try from start
                                Point p1 = startLine.getPoint(sigma);
                                Point p2 = new Line(p1, midVertex.getUHVWPoint(this)).intersect(endLine);
                                if (p2 != null) {
                                    double closestLambda = endLine.closestLambda(p2);

                                    if (closestLambda > 0.1d && closestLambda < 1.2d) {

                                        ISegment candidateCurve = SegmentFactory.createTempDoubleCurveSegment(this, currentSegment.getSegmentIndex(), p1, midVertex, p2);
                                        mSegments.set(currentSegment.getSegmentIndex(), candidateCurve);
                                        if (isValid(candidateCurve)) {
                                            final double candidateError = candidateCurve.calcError(this);
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
                                Point p2 = endLine.getPoint(sigma);
                                Point p1 = new Line(p2, midVertex.getUHVWPoint(this)).intersect(startLine);
                                double closestLambda = startLine.closestLambda(p2);

                                if (p1 != null && 0.1d < closestLambda && closestLambda < 1.2d) { // TODO what are these magic numbers

                                    ISegment candidateCurve = SegmentFactory.createTempDoubleCurveSegment(this, currentSegment.getSegmentIndex(), p1, midVertex, p2);
                                    mSegments.set(currentSegment.getSegmentIndex(), candidateCurve);
                                    if (isValid(candidateCurve)) {
                                        final double candidateError = candidateCurve.calcError(this);
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
     * Reverses the order of the pixels in the pixel chain. This means reversing the start and end nodes. And all of the segments in the line are also reversed and repaced with new straight line
     * segments.
     */
    private void reverse() {
        // note that this uses direct access to the data members as the public setters have other side effects
        mSegments.forEach(s -> mLogger.fine(() -> String.format("reverse this.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("reverse this.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));

        final Node tmp = getStartNode();
        mStartNode = getEndNode();
        mEndNode = tmp;

        final int maxPixelIndex = mPixels.size() - 1;

        Collections.reverse(mPixels);
        Vector<IVertex> vertexes = new Vector<>();
        final Vector<ISegment> stnemges = new Vector<>();

        for (int i = mVertexes.size() - 1; i >= 0; i--) {
            final IVertex vertex = mVertexes.get(i);
            final IVertex v = Vertex.createVertex(this, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
            if (i != mVertexes.size() - 1) {
                final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(this, stnemges.size());
                stnemges.add(newSegment);
            }
        }

        mSegments = stnemges;
        mVertexes = vertexes;
        //setStartVertex(mSegments.firstElement().getStartVertex(this));
        // TODO mVertexes.firstElement().setIndex(this, 0);
        mSegments.forEach(s -> mLogger.fine(() -> String.format("reverse this.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("reverse this.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));

        validate("reverse");
    }

    void setEndNode(final Node pNode) {
        mEndNode = pNode;
        if (pNode != null) {
            add(pNode);
        }

        // need to do a check here to see if we are clobbering over another chain
        // if pixel end-2 is a neighbour of pixel end then pixel end-1 needs to be set as notVisited and removed from the chain
        if (count() >= 3 && pNode.isNeighbour(mPixels.get(count() - 3))) {
            mPixels.remove(count() - 2).setVisited(false);
        }
    }

    private void setLength(final double pLength) {
        mLength = pLength;
    }

    public void setPixelMap(final PixelMap pPixelMap) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pPixelMap", pPixelMap);

        if (pPixelMap == null) {
            throw new IllegalArgumentException("pPixelMap must not be null");
        }
        if (mPixelMap != null) {
            throw new IllegalStateException("Trying to overwrite a non null mPixelMap value");
        }

        mPixelMap = pPixelMap;
        for (final Pixel pixel : mPixels) {
            pixel.setPixelMap(pPixelMap);
        }

        getStartNode().setPixelMap(pPixelMap);

        Framework.logExit(mLogger);
    }

    private void setStartNode(final Node pNode) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pNode", pNode);

        if (mStartNode != null) {
            throw new IllegalStateException("Can not change the start node of a PixelChain once it has been set.  Current mStartNode = " + mStartNode);
        }
        if (pNode == null) {
            throw new IllegalArgumentException("Can not change the start node to a null value");
        }

        mStartNode = pNode;
        add(pNode);

        setStartVertex(Vertex.createVertex(this, mVertexes.size(), 0));

        Framework.logExit(mLogger);
    }

    private void setStartVertex(final IVertex pVertex) {
        if (mVertexes.size() >= 1) mVertexes.remove(0);
        mVertexes.add(0, pVertex);
    }

    void setThickness(final int pThinLength, final int pNormalLength, final int pLongLength) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pNormalLength, pLongLength", pNormalLength, pLongLength);

        if (length() < pThinLength) {
            mThickness = Thickness.None;
        } else if (length() < pNormalLength) {
            mThickness = Thickness.Thin;
        } else if (length() < pLongLength) {
            mThickness = Thickness.Normal;
        } else {
            mThickness = Thickness.Thick;
        }

        Framework.logExit(mLogger);
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

    void validate(String pMethodName) {
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
            for (int i = 0; i < mVertexes.size(); i++) {
                IVertex v = mVertexes.get(i);
                if (i != 0 && v.getStartSegment(this) != mSegments.get(i - 1))
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                if (i != mVertexes.size() - 1 && v.getEndSegment(this) != mSegments.get(i))
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
            }
        } catch (Throwable pT) {
            printVertexs();
            throw pT;
        }
    }

    private void printVertexs() {
        StringBuilder sb = new StringBuilder()
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
        }

        mLogger.severe(sb::toString);
    }

    public int getPixelLength() {
        return mPixels.size();
    }

    ISegment getSegment(int i) {
        if (mSegments.size() <= i || i < 0) return null;
        return mSegments.get(i);
    }

    public IVertex getVertex(int i) {
        if (mVertexes.size() <= i || i < 0) return null;
        return mVertexes.get(i);
    }

    void setInChain(boolean pValue) {
        for (Pixel pixel : mPixels) {
            pixel.setInChain(pValue);
        }
    }

    private void setEdge() {
        mPixels.stream()
                .filter(p -> p != mPixels.firstElement())
                .filter(p -> p != mPixels.lastElement())
                .forEach(p -> p.setEdge(false));
        mPixels.stream()
                .filter(Pixel::isNode)
                .filter(p -> p.countEdgeNeighbours() < 2 || p.countNodeNeighbours() == 2)
                .forEach(p -> p.setEdge(false));
    }

    public void setVisited(boolean pValue) {
        for (Pixel pixel : mPixels) {
            pixel.setVisited(pValue);
        }
    }

    public void delete() {
        getPixelMap().removePixelChain(this);
        setInChain(false);
        setVisited(false);
        setEdge();
    }

}

