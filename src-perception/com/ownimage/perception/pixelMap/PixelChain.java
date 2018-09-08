/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.LineSegment;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.segment.*;
import com.ownimage.perception.transform.CannyEdgeTransform;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class PixelChain implements Serializable {

    public enum Thickness {
        None, Thin, Normal, Thick
    }


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 2L;
    transient private PixelMap mPixelMap;
    transient private double mLength;
    private final Vector<Pixel> mPixels = new Vector<>();
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

    // @Deprecated
    // private PixelChain(final PixelMap pPixelMap, final Node pStartNode) {
    // mThickness = Thickness.Normal;
    // mPixelMap = pPixelMap;
    // setStartNode(pStartNode);
    // }

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
        pOtherChain.validate("add");

        if (getPixelMap() != pOtherChain.getPixelMap()) {
            throw new IllegalArgumentException("PixelMap must be the same for both chains");
        }

        if (!lastPixel().equals(pOtherChain.firstPixel())) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + pOtherChain);
        }

        final int offset = mPixels.size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed

        final Vector<Pixel> pixelsDebug = new Vector<>(mPixels);

        mPixels.removeElementAt(mPixels.size() - 1);// need to remove the last pixel as it will be duplicated on the other chain;
        mPixels.addAll(pOtherChain.mPixels);

        IVertex start = mSegments.lastElement().getEndVertex(this);
        start.setFixed(true);

        for (final ISegment segment : pOtherChain.mSegments) {
            final IVertex end = Vertex.createVertex(this, mVertexes.size(), segment.getEndIndex(this) + offset);
            mVertexes.add(end);
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(start, end, this);
            newSegment.attachToVertexes(this, false);
            mSegments.add(newSegment);
            start = end;
        }

        pOtherChain.mSegments.removeAllElements(); // TODO
        getEndNode().removePixelChain(this);
        pOtherChain.getEndNode().removePixelChain(pOtherChain);
        pOtherChain.getStartNode().removePixelChain(pOtherChain);

        mEndNode = pOtherChain.getEndNode(); // not using setEndNode here as this adds the Node pixel to the end of the pixelChain
        getEndNode().addPixelChain(this);
        getPixelMap().removePixelChain(pOtherChain);

        reCalcSegments();
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
        StraightSegment maxLine = null;

        int endIndex = 1;

        while (endIndex < mPixels.size()) {
            for (int index = endIndex; index < mPixels.size(); index++) {
                final IVertex candidateVertex = Vertex.createVertex(this, mVertexes.size(), index);
                final StraightSegment candidateLine = SegmentFactory.createTempStraightSegment(startVertex, candidateVertex, this);

                if (candidateLine.noPixelFurtherThan(this, pTolerance)) {
                    maxIndex = index;
                    maxVertex = candidateVertex;
                    maxLine = candidateLine;
                    continue;
                }
                break;
            }

            mVertexes.add(maxVertex);
            maxLine.attachToVertexes(this, false);
            mSegments.add(maxLine);
            startVertex = maxVertex;
            endIndex = maxIndex + 1;
        }

        reCalcSegments();
        validate("approximate01_straightLines");
        indexSegments();
    }

    void approximate02_refineCorners() {
        // TODO note pTolerance is not used at the moment
        for (final ISegment currentSegment : mSegments) {
            if (currentSegment == mSegments.lastElement()) {
                return;
            }

            if (currentSegment.getEndVertex(this).isFixed(this)) {
                continue;
            }

            final ISegment nextSegment = currentSegment.getEndVertex(this).getEndSegment();
            if (nextSegment == null) {
                throw new IllegalStateException("Only the final segment in a PixelChain can have no EndVertex's EndSegment");
            }

            final int firstLineStartIndex = (currentSegment.getStartVertex(this).getPixelIndex() + currentSegment.getEndVertex(this).getPixelIndex()) / 2;
            final int currentMidpointIndex = currentSegment.getEndVertex(this).getPixelIndex();

            final int secondLineEndIndex = (nextSegment.getStartVertex(this).getPixelIndex() + nextSegment.getEndVertex(this).getPixelIndex()) / 2;

            final IVertex firstLineStartVertex = Vertex.createVertex(this, mVertexes.size(), firstLineStartIndex);
            IVertex midpointVertex = Vertex.createVertex(this, mVertexes.size(), currentMidpointIndex);
            final IVertex secondLineEndVertex = Vertex.createVertex(this, mVertexes.size(), secondLineEndIndex);

            // TODO the check below is needed as some segments may only be one index length so generating a midpoint might generate an invalid segment
            if (firstLineStartIndex < currentMidpointIndex && currentMidpointIndex < secondLineEndIndex) {
                ISegment firstLine = SegmentFactory.createTempStraightSegment(firstLineStartVertex, midpointVertex, this);
                ISegment secondLine = SegmentFactory.createTempStraightSegment(midpointVertex, secondLineEndVertex, this);

                int bestMidpointIndex = currentMidpointIndex;
                double bestError = firstLine.calcError(this) + secondLine.calcError(this);

                for (int candidateIndex = firstLineStartIndex + 1; candidateIndex < secondLineEndIndex; candidateIndex++) {
                    midpointVertex = Vertex.createVertex(this, mVertexes.size(), candidateIndex);

                    firstLine = SegmentFactory.createTempStraightSegment(firstLineStartVertex, midpointVertex, this);
                    secondLine = SegmentFactory.createTempStraightSegment(midpointVertex, secondLineEndVertex, this);

                    final double error = firstLine.calcError(this) + secondLine.calcError(this);

                    if (error < bestError) {
                        bestError = error;
                        bestMidpointIndex = candidateIndex;
                    }
                }

                firstLine.attachToVertexes(this, false);
                secondLine.attachToVertexes(this, false);
            }
        }

        reCalcSegments();
        validate("approximate02_refineCorners");
    }

    private void checkAllVertexesAttached() {
        for (final ISegment segment : mSegments) {
            try {
                if (mLogger.isLoggable(Level.SEVERE)) {
                    if (segment.getStartVertex(this).getEndSegment() != segment) {
                        mLogger.severe("start Vertex not attached");
                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
                    }
                    if (segment.getEndVertex(this).getStartSegment() != segment) {
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
        if (mSegments == null || mSegments.size() == 0) {
            return mVertexes.firstElement();
        }
        return mSegments.firstElement().getStartVertex(this);
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

    void indexSegments() {
        double startPosition = 0.0d;
        for (final ISegment segment : mSegments) {
            getPixelMap().index(this, segment);
            segment.setStartPosition(this, startPosition);
            startPosition += segment.getLength(this);
        }
        setLength(startPosition);
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

    private void reCalcSegments() {
        mSegments.removeAllElements();
        for (IVertex vertex = getStartVertex(); vertex.getEndSegment() != null; vertex = vertex.getEndSegment().getEndVertex(this)) {
            mSegments.add(vertex.getEndSegment());
        }
    }

    void refine() {
        refine01_matchCurves();
        refine03_matchDoubleCurves();
    }

    private void refine01_matchCurves() {

        if (mSegments.size() == 1) {
            return;
        }

        final Vector<ISegment> segments = new Vector<>();

        for (final ISegment currentSegment : mSegments) {
            final IVertex startVertex = currentSegment.getStartVertex(this);
            final IVertex endVertex = currentSegment.getEndVertex(this);

            // get error values from straight line to start the compare
            ISegment bestSegment = currentSegment;
            double lowestError = currentSegment.calcError(this);
            lowestError *= getTransformSource().getLineCurvePreference();

            try {

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
                                final ISegment candidateCurve = SegmentFactory.createTempCurveSegmentTowards(this, currentSegment.getStartVertex(this), currentSegment.getEndVertex(this), p1);
                                final double candidateError = candidateCurve != null ? candidateCurve.calcError(this) : 0.0d;

                                if (isValid(candidateCurve) && candidateError < lowestError) {
                                    lowestError = candidateError;
                                    bestSegment = candidateCurve;
                                }

                            } catch (final Throwable pT) {
                                mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }
                        }
                    } catch (final Throwable pT) {
                        mLogger.log(Level.INFO, "Error: ", pT);
                    } finally {
                        if (bestSegment instanceof LineSegment) {
                            final LineSegment lineSegment = new LineSegment(currentSegment.getStartUHVWPoint(this), currentSegment.getEndUHVWPoint(this));
                            currentSegment.getEndVertex(this).setTangent(lineSegment);
                        }
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
                                final ISegment candidateCurve = SegmentFactory.createTempCurveSegmentTowards(this, currentSegment.getStartVertex(this), currentSegment.getEndVertex(this), p1);
                                final double candidateError = candidateCurve.calcError(this);

                                if (isValid(candidateCurve) && candidateError < lowestError) {
                                    lowestError = candidateError;
                                    bestSegment = candidateCurve;
                                }

                            } catch (final Throwable pT) {
                                mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }
                        }
                    } catch (final Throwable pT) {
                        mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
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
                                    ISegment candidateCurve = SegmentFactory.createTempCurveSegmentTowards(this, currentSegment.getStartVertex(this), currentSegment.getEndVertex(this), p1);
                                    double candidateError = candidateCurve.calcError(this);

                                    if (isValid(candidateCurve) && candidateError < lowestError) {
                                        lowestError = candidateError;
                                        bestSegment = candidateCurve;
                                    }
                                    // }
                                }
                            }
                        } catch (Throwable pT) {
                            mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                        }

                        // method 2 - looking at going through pixels
                        // try {
                        // for (int i = startVertex.getPixelIndex() + 2; i < endVertex.getPixelIndex() - 2; i++) { // first and last pixel will throw an error and are equivalent to the straight line
                        // try {
                        // final Point through = getPixel(i).getUHVWPoint();
                        // final ISegment candidateCurve = SegmentFactory.createTempCurveSegmentThrough(startVertex, endVertex, through);
                        //
                        // final double candidateError = candidateCurve.calcError();
                        //
                        // if (isValid(candidateCurve) && candidateError < lowestError) {
                        // lowestError = candidateError;
                        // bestSegment = candidateCurve;
                        // }
                        // } catch (final Throwable pT) {
                        // mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                        // }
                        // }
                        //
                        // } catch (final Throwable pT) {
                        // mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                        // }

                        // method 3 - intersection of tangents
                        // final Point through = currentSegment.getStartTangent().intersect(currentSegment.getEndTangent());
                        // if (through != null) {
                        // final ISegment candidateCurve = SegmentFactory.createTempCurveSegmentThrough(startVertex, endVertex, through);
                        //
                        // final double candidateError = candidateCurve.calcError();
                        //
                        // if (isValid(candidateCurve) && candidateError < lowestError) {
                        // lowestError = candidateError;
                        // bestSegment = candidateCurve;
                        // }
                        // }

                    } catch (final Throwable pT) {
                        mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                    }
                }
            } finally {
                bestSegment.attachToVertexes(this, false);
                segments.add(bestSegment);
            }
        } // end loop
        mSegments = segments;
        validate("refine01_matchCurves");
    }

    private Line getDCStartTangent(ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex startVertex = pSegment.getStartVertex(this);
        if (startVertex.getStartSegment() instanceof StraightSegment) {
            return startVertex.getStartSegment().getEndTangent(this);
        }

        return startVertex.calcTangent(this);
    }

    private Line getDCEndTangent(ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex endVertex = pSegment.getEndVertex(this);
        if (endVertex.getEndSegment() instanceof StraightSegment) {
            return endVertex.getEndSegment().getStartTangent(this);
        }

        return endVertex.calcTangent(this);
    }

    private void refine03_matchDoubleCurves() {

        if (mSegments.size() == 1) {
            return;
        }

        final Vector<ISegment> segments = new Vector<>();

        for (final ISegment currentSegment : mSegments) {
            final IVertex startVertex = currentSegment.getStartVertex(this);
            final IVertex endVertex = currentSegment.getEndVertex(this);
            final Line startTangent = getDCStartTangent(currentSegment);
            final Line endTangent = getDCEndTangent(currentSegment);

            // get error values from straight line to start the compare
            ISegment bestSegment = currentSegment;
            double lowestError = currentSegment.calcError(this);
            if (currentSegment instanceof StraightSegment) {
                lowestError *= getTransformSource().getLineCurvePreference();
            }

            if (currentSegment instanceof CurveSegment) {
                lowestError = -1.0d;
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

                        ISegment candidateCurve;
                        Point p1;
                        Point p2;

                        for (double sigma = 0.3d; sigma < 0.7d; sigma += 0.1d) {
                            // double sigma = 0.5d;
                            try {
                                // try from start
                                p1 = startLine.getPoint(sigma);
                                p2 = new Line(p1, midVertex.getUHVWPoint(this)).intersect(endLine);
                                if (p2 != null) {
                                    double closestLambda = endLine.closestLambda(p2);

                                    if (closestLambda > 0.1d && closestLambda < 1.2d) {

                                        candidateCurve = SegmentFactory.createTempDoubleCurveSegment(this, startVertex, p1, midVertex, p2, endVertex);

                                        if (candidateCurve != null) {
                                            final double candidateError = candidateCurve.calcError(this);

                                            if (isValid(candidateCurve) && candidateError < lowestError) {
                                                lowestError = candidateError;
                                                bestSegment = candidateCurve;
                                            }
                                        }
                                    }
                                }

                            } catch (final Throwable pT) {
                                mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }

                            // try from end
                            try {
                                p2 = endLine.getPoint(sigma);
                                p1 = new Line(p2, midVertex.getUHVWPoint(this)).intersect(startLine);
                                double closestLambda = startLine.closestLambda(p2);

                                if (p1 != null && 0.1d < closestLambda && closestLambda < 1.2d) { // TODO what are these magic numbers

                                    candidateCurve = SegmentFactory.createTempDoubleCurveSegment(this, startVertex, p1, midVertex, p2, endVertex);

                                    if (candidateCurve != null) {
                                        final double candidateError = candidateCurve.calcError(this);

                                        if (isValid(candidateCurve) && candidateError < lowestError) {
                                            // lowestError = candidateError;
                                            // bestSegment = candidateCurve;
                                        }
                                    }
                                }
                            } catch (final Throwable pT) {
                                mLogger.severe(() -> FrameworkLogger.throwableToString(pT));
                            }
                        }
                    }
                } catch (final Throwable pT) {
                    mLogger.log(Level.INFO, "Error: ", pT);
                } finally {
                    if (bestSegment instanceof DoubleCurveSegment) {
                        // final LineSegment lineSegment = new LineSegment(currentSegment.getStartUHVWPoint(), currentSegment.getEndUHVWPoint());
                        // currentSegment.getEndVertex().setTangent(lineSegment);
                        mLogger.info("DCS m " + ((DoubleCurveSegment) bestSegment).getStartCurve().getEndTangentVector(this));
                        mLogger.info("DCS m " + ((DoubleCurveSegment) bestSegment).getEndCurve().getStartTangentVector(this));

                        mLogger.info("DoubleCurve added");
                    }
                }

            } finally {
                bestSegment.attachToVertexes(this, false);
                segments.add(bestSegment);
            }
        } // end loop
        mSegments = segments;
        validate("refine03_matchDoubleCurves");
    }


    /**
     * Reverses the order of the pixels in the pixel chain. This means reversing the start and end nodes. And all of the segments in the line are also reversed and repaced with new straight line
     * segments.
     */
    private void reverse() {
        // note that this uses direct access to the data members as the public setters have other side effects
        final Node tmp = getStartNode();
        mStartNode = getEndNode();
        mEndNode = tmp;

        Vector<IVertex> vertexes = new Vector<>();
        Collections.reverse(mPixels);

        final Vector<ISegment> stnemges = new Vector<>();
        final int maxPixelIndex = mPixels.size() - 1;

        IVertex start = Vertex.createVertex(this, vertexes.size(), 0);
        vertexes.add(start);
        for (int i = mSegments.size() - 1; i >= 0; i--) {
            final ISegment segment = mSegments.get(i);
            final IVertex end = Vertex.createVertex(this, vertexes.size(), maxPixelIndex - segment.getStartIndex(this));
            vertexes.add(end);
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(start, end, this);
            newSegment.attachToVertexes(this, false);
            stnemges.add(newSegment);
            start = end;
        }
        mSegments = stnemges;
        mVertexes = vertexes;
        // TODO setStartVertex(mSegments.firstElement().getStartVertex(this));
        // TODO mVertexes.firstElement().setIndex(this, 0);
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
            if (getStartVertex().getPixelIndex() != 0) { //
                throw new IllegalStateException("getStartVertex().getPixelIndex() != 0");
            }

            reCalcSegments();
            int nextStartIndex = 0;

            for (final ISegment segment : mSegments) {
                if (segment.getStartIndex(this) != nextStartIndex) { //
                    throw new IllegalStateException("segments not linked properly");
                }
                nextStartIndex = segment.getEndIndex(this);
            }

            if (mSegments.size() != 0 && mSegments.lastElement().getEndIndex(this) != mPixels.size() - 1) { //
                throw new IllegalStateException("last segment not linked properly");
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
                vertex = vertex.getEndSegment() != null
                        ? vertex.getEndSegment().getEndVertex(this)
                        : null;
            }
        } catch (Throwable pT) {
            printVertexs();
            throw pT;
        }
    }

    private void printVertexs() {
        StringBuilder sb = new StringBuilder()
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
            vertex = vertex.getEndSegment() != null
                    ? vertex.getEndSegment().getEndVertex(this)
                    : null;
        }

        mLogger.severe(sb::toString);
    }

    public int getPixelLength() {
        return mPixels.size();
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
                .filter(p -> p.isNode())
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

