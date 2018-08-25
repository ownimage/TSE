/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.perception.pixelMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Path;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.LineSegment;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.DoubleCurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.transform.CannyEdgeTransform;

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
    private final Vector<Pixel> mPixels = new Vector<Pixel>();
    private Vector<ISegment> mSegments = new Vector<ISegment>();
    private Node mStartNode;
    private IVertex mStartVertex;
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

    public void forEachPixel(final Consumer<? super Pixel> pLambda) {
        mPixels.forEach(pLambda);
    }

    public Stream<Pixel> streamPixels() {
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
        validate();
        pOtherChain.validate();

        if (getPixelMap() != pOtherChain.getPixelMap()) {
            throw new IllegalArgumentException("PixelMap must be the same for both chains");
        }

        if (!lastPixel().equals(pOtherChain.firstPixel())) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + pOtherChain);
        }

        final int offset = mPixels.size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed

        final Vector<Pixel> pixelsDebug = new Vector<Pixel>();
        pixelsDebug.addAll(mPixels);
        mPixels.removeElementAt(mPixels.size() - 1);// need to remove the last pixel as it will be duplicated on the other chain;
        mPixels.addAll(pOtherChain.mPixels);

        IVertex start = mSegments.lastElement().getEndVertex();
        start.setFixed(true);

        for (final ISegment segment : pOtherChain.mSegments) {
            final IVertex end = Vertex.createVertex(this, segment.getEndIndex() + offset);
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(start, end);
            newSegment.attachToVertexes(false);
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

        // TODO
        {
            // pOtherChain.mSegments.removeAllElements();
            // this.mSegments.removeAllElements();
            // IVertex start2 = Vertex.createVertex(this, 0);
            // IVertex end = Vertex.createVertex(this, mPixels.size() - 1);
            // LineApproximation newSegment = LineApproximation.createTempLineSegment(start2, end);
            // newSegment.attachToVertexes();
            // this.mSegments.add(newSegment);
        }
        reCalcSegments();
        validate();
    }

    public boolean addAll(final Collection<? extends Pixel> pC) {
        return mPixels.addAll(pC);
    }

    public void addSegment(final ISegment pSegment) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pSegment", pSegment);

        if (pSegment == null) {
            throw new IllegalArgumentException("pSegment must not be null");
        }

        if (mSegments.size() == 0 && pSegment.getStartIndex() != 0) {
            throw new IllegalArgumentException("First segment that is added must have a startIndex of 0");
        }

        if (mSegments.size() != 0 && pSegment.getStartIndex() != mSegments.lastElement().getEndIndex()) {
            mLogger.finer("mSegments = " + mSegments);
            throw new IllegalArgumentException("The start index of the segment must equal the end index of the last segment");
        }

        mSegments.add(pSegment);
        Framework.logExit(mLogger);
    }

    public void addToNodes() {
        mStartNode.addPixelChain(this);

        if (mEndNode != null) {
            if (!mEndNode.equals(mStartNode)) {
                mEndNode.addPixelChain(this);
            }
        } else {
            final Pixel end = mPixels.lastElement();
            mEndNode = new Node(end);
            mEndNode.addPixelChain(this);
            if (end.getX() != 0 && end.getY() != 0) {
                mLogger.info(() -> "mEndNode bodge");
                mLogger.info(() -> this.toString());
                //end.printNeighbours(5);
            }
        }
    }

    private void addVertex(final Vector<IVertex> pVisibleVertexes, final IVertex pVertex, final Pixel pOrigin, final Pixel pTopLeft) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pVisibleVertexes, pVertex, pOrigin, pTopLeft", pVisibleVertexes, pVertex, pOrigin, pTopLeft);

        if (pOrigin.getX() <= pVertex.getX() && pVertex.getX() <= pTopLeft.getX() //
                && pOrigin.getY() <= pVertex.getY() && pVertex.getY() < pTopLeft.getY() //
                ) {
            pVisibleVertexes.add(pVertex);

            if (mSegments.size() != 0 && pVertex.getStartSegment() == null && pVertex.getEndSegment() == null) {
                mLogger.severe("Adding unattached vertex");
            }
            if (mSegments.size() == 0 && pVertex != getStartVertex() && pVertex.getStartSegment() == null && pVertex.getEndSegment() == null) {
                mLogger.severe("Adding unattached vertex that is not the start vertex");
            }
        }
        Framework.logExit(mLogger);
    }

    public void addVertexes(final Vector<IVertex> pVisibleVertexes, final Pixel pOrigin, final Pixel pTopLeft) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pVisibleVertexes, pOrigin, pTopLeft", pVisibleVertexes, pOrigin, pTopLeft);

        // add first vertex, note this works even if there is only a start node
        addVertex(pVisibleVertexes, getStartVertex(), pOrigin, pTopLeft);
        if (getStartVertex() != null && mSegments.size() != 0 && getStartVertex().getEndSegment() != mSegments.firstElement()) {
            mLogger.severe("PixelChain stitching error getStartVertex() not attached properly.");
        }

        for (final ISegment segment : getAllSegments()) {
            addVertex(pVisibleVertexes, segment.getEndVertex(), pOrigin, pTopLeft);
            if (segment.getStartVertex().getEndSegment() != segment) {
                mLogger.severe("PixelChain stitching error, start vertex not attahced.");
            }
            if (segment.getEndVertex().getStartSegment() != segment) {
                mLogger.severe("PixelChain stitching error, end vertex not attached.");
            }
        }

        Framework.logExit(mLogger);
    }

    public void approximate() {
        //mLogger.info(() -> "PixelChain::approximate");
        double tolerance = mPixelMap.getLineTolerance() / mPixelMap.getHeight();
        //mLogger.info(() -> "tolerance " + tolerance);
        mSegments = new Vector<ISegment>();
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

    public void approximate01_straightLines(final double pTolerance) {
        // note that this is version will find the longest line that is close to all pixels.
        // there are cases where a line of length n will be close enough, a line of length n+1 will not be, but there exists an m such that a line of length m is close enough.
        mSegments = new Vector<ISegment>();

        if (mPixels.size() <= 1) {
            return;
        }

        final int startIndex = 0;
        IVertex startVertex = getStartVertex();

        int maxIndex = 0;
        IVertex maxVertex = null;
        StraightSegment maxLine = null;

        int endIndex = 1;

        while (endIndex < mPixels.size()) {
            for (int index = endIndex; index < mPixels.size(); index++) {
                final IVertex candidateVertex = Vertex.createVertex(this, index);
                final StraightSegment candidateLine = SegmentFactory.createTempStraightSegment(startVertex, candidateVertex);

                // if (!startVertex.equals(candidateVertex) && candidateLine.noPixelFurtherThan(pTolerance)) {
                if (candidateLine.noPixelFurtherThan(pTolerance)) {
                    maxIndex = index;
                    maxVertex = candidateVertex;
                    maxLine = candidateLine;
                    continue;
                }

                break;
                // if (candidateVertex.isFixed()) {
                // break;
                // }
            }

            maxLine.attachToVertexes(false);
            mSegments.add(maxLine);
            startVertex = maxVertex;
            endIndex = maxIndex + 1;
        }

        reCalcSegments();
        validate();
    }

    public void approximate02_refineCorners() {
        // TODO note pTolerance is not used at the moment
        for (final ISegment currentSegment : mSegments) {
            if (currentSegment == mSegments.lastElement()) {
                return;
            }

            if (currentSegment.getEndVertex().isFixed()) {
                continue;
            }

            final ISegment nextSegment = currentSegment.getEndVertex().getEndSegment();
            if (nextSegment == null) {
                throw new IllegalStateException("Only the final segment in a PixelChain can have no EndVertex's EndSegment");
            }

            final int firstLineStartIndex = (currentSegment.getStartVertex().getIndex() + currentSegment.getEndVertex().getIndex()) / 2;
            final int currentMidpointIndex = currentSegment.getEndVertex().getIndex();

            final int secondLineEndIndex = (nextSegment.getStartVertex().getIndex() + nextSegment.getEndVertex().getIndex()) / 2;

            final IVertex firstLineStartVertex = createVertex(firstLineStartIndex);
            IVertex midpointVertex = createVertex(currentMidpointIndex);
            final IVertex secondLineEndVertex = createVertex(secondLineEndIndex);

            // TODO the check below is needed as some segments may only be one index length so generating a midpoint might generate an invalid segment
            if (firstLineStartIndex < currentMidpointIndex && currentMidpointIndex < secondLineEndIndex) {
                ISegment firstLine = SegmentFactory.createTempStraightSegment(firstLineStartVertex, midpointVertex);
                ISegment secondLine = SegmentFactory.createTempStraightSegment(midpointVertex, secondLineEndVertex);

                int bestMidpointIndex = currentMidpointIndex;
                double bestError = firstLine.calcError() + secondLine.calcError();

                for (int candidateIndex = firstLineStartIndex + 1; candidateIndex < secondLineEndIndex; candidateIndex++) {
                    midpointVertex = createVertex(candidateIndex);

                    firstLine = SegmentFactory.createTempStraightSegment(firstLineStartVertex, midpointVertex);
                    secondLine = SegmentFactory.createTempStraightSegment(midpointVertex, secondLineEndVertex);

                    final double error = firstLine.calcError() + secondLine.calcError();

                    if (error < bestError) {
                        bestError = error;
                        bestMidpointIndex = candidateIndex;
                    }
                }

                firstLine.attachToVertexes(false);
                secondLine.attachToVertexes(false);
            }
        }

        reCalcSegments();
        validate();
    }

    public void checkAllVertexesAttached() {
        for (final ISegment segment : mSegments) {
            try {
                if (mLogger.isLoggable(Level.SEVERE)) {
                    if (segment.getStartVertex().getEndSegment() != segment) {
                        mLogger.severe("start Vertex not attached");
                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
                    }
                    if (segment.getEndVertex().getStartSegment() != segment) {
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

    public boolean checkVertexIsInChain(final IVertex pVertex) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pVertex", pVertex);

        boolean found = pVertex == getStartVertex();
        for (final ISegment segment : mSegments) {
            if (segment.getStartVertex() == pVertex || segment.getEndVertex() == pVertex) {
                found = true;
                break;
            }
        }

        Framework.logExit(mLogger);
        return found;
    }

    public boolean contains(final Pixel pPixel) {
        return mPixels.contains(pPixel);
    }

    public int count() {
        return mPixels.size();
    }

    private IVertex createVertex(final int pIndex) {
        if (pIndex < 0 || pIndex >= mPixels.size()) {
            throw new IllegalArgumentException("pIndex =(" + pIndex + ") must lie between 0 and the size of the mPixels collection =(" + mPixels.size() + ")");
        }

        return Vertex.createVertex(this, pIndex);
    }

    public PixelChain deepCopy() {
        final PixelChain copy = new PixelChain(getStartNode());
        copy.setEndNode(getEndNode());
        copy.setStartVertex(getStartVertex().deepCopy(null, null));
        copy.reCalcSegments();
        copy.validate();
        return copy;
    }

    public PixelChain[] deleteSegment(final ISegment pSegment) {
        final PixelChain pc1 = shallowCopyTo(pSegment);
        final PixelChain pc2 = shallowCopyFrom(pSegment);

        getPixelMap().removePixelChain(this);

        getPixelMap().addPixelChains(pc1);
        getPixelMap().addPixelChains(pc2);
        return new PixelChain[]{pc1, pc2};

    }

    public void deleteVertex(final IVertex pVertex) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pVertex", pVertex);

        if (pVertex.getPixelChain() != this) {
            throw new IllegalArgumentException("pVertex belongs to PixelChain: " + pVertex.getPixelChain() + ".a\n This is PixelChain: " + this);
        }
        if (!checkVertexIsInChain(pVertex)) {
            throw new IllegalArgumentException("pVertex: " + pVertex + " is not properly connected in this PixelChain: " + this);
        }

        if (pVertex.isStart()) {
            final ISegment endSegment = pVertex.getEndSegment();
            final IVertex endVertex = endSegment.getEndVertex();
            pVertex.setEndSegment(null);
            endVertex.setStartSegment(null);
            setStartVertex(endVertex);
            mSegments.remove(endSegment);

        } else if (pVertex.isMiddle()) {
            final IVertex start = pVertex.getStartSegment().getStartVertex();
            final IVertex end = pVertex.getEndSegment().getEndVertex();
            final ISegment newSegment = SegmentFactory.createTempStraightSegment(start, end);
            newSegment.attachToVertexes(false);

            mSegments.add(newSegment);
            mSegments.remove(pVertex.getStartSegment());
            mSegments.remove(pVertex.getEndSegment());

        } else if (pVertex.isEnd()) {
            final ISegment startSegment = pVertex.getStartSegment();
            pVertex.setStartSegment(null);
            startSegment.getStartVertex().setEndSegment(null);
            mSegments.remove(startSegment);

        } else if (pVertex.isDisconnected()) {
            getPixelMap().removePixelChain(this);
        }

        reCalcSegments();
        validate();
        Framework.logExit(mLogger);
    }

    public Pixel firstPixel() {
        return mPixels.firstElement();
    }

    private double getActualCurvedThickness(final IPixelMapTransformSource pLineInfo, final double pFraction) {
        final double c = pLineInfo.getLineEndThickness() * getWidth();
        final double a = c - getWidth();
        final double b = -2.0 * a;
        final double thickness = a * pFraction * pFraction + b * pFraction + c;
        return thickness;
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

        final double end1 = pPosition;
        final double end2 = getLength() - end1;
        final double closestEnd = Math.min(end1, end2);

        if (pInfo.getLineEndLengthType() == CannyEdgeTransform.LineEndLengthType.Percent) {
            final double closestPercent = 100.0d * closestEnd / getLength();
            final double fraction = Math.min(closestPercent / pInfo.getLineEndLengthPercent(), 1.0d);
            return fraction;
        }

        // type is Pixels
        final double fraction = pInfo.getHeight() * closestEnd / pInfo.getLineEndLengthPixel();
        final double factor = Math.min(fraction, 1.0d);
        return factor;

    }

    public Iterable<ISegment> getAllSegments() {
        return mSegments;
    }

    public Node getEndNode() {
        return mEndNode;
    }

    public ISegment getFirstSegment() {
        return mSegments.firstElement();
    }

    // public boolean allMembersCloseTo(LineApproximation pLine, double pTolerance) {
    // for (Pixel pixel : this) {
    // Point point = pixel.toPoint();
    // if (!pLine.isCloserThan(point, pTolerance)) return false;
    // }
    // return true;
    // }

    public IVertex getJoinVertex() {
        if (mSegments.size() == 0) {
            return getStartVertex();
        }
        return getLastSegment().getEndVertex();
    }

    public ISegment getLastSegment() {
        return mSegments.lastElement();
    }

    public double getLength() {
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

    public PixelMap getPixelMap() {
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

    public IVertex getStartVertex() {
        if (mSegments == null || mSegments.size() == 0) {
            return mStartVertex;
        }
        return mSegments.firstElement().getStartVertex();
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

    // // TODO this method is no longer used
    // public boolean isAnyLineCloserThan(final Point pPoint, final double pTolerance) {
    // for (final ISegment segment : mSegments) {
    // if (segment.closerThan(pPoint, pTolerance)) { return true; }
    // }
    // return false;
    // }

    public double getWidth() {
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

    // TODO can remove the pSelectedVertex from this as this is drawn separately .. The reason for this is that the selected Vertex might be behind a non=selected one.
    //   public void grafittiVertexsAndControlLines(final EPMDGraphicsHelper pEPMD) {
//        Framework.logEntry(mLogger);
//
//        // the vertexes are drawn on afterwards so they appear on top
//        pEPMD.graffitiVertex(getStartVertex());
//        for (final ISegment segment : getAllSegments()) {
//            pEPMD.graffitiVertex(segment.getEndVertex());
//        }
//
//        Framework.logExit(mLogger);
    //   }

    public void indexSegments() {
        double startPosition = 0.0d;
        for (final ISegment segment : mSegments) {
            getPixelMap().index(segment);
            segment.setStartPosition(startPosition);
            startPosition += segment.getLength();
        }
        setLength(startPosition);
    }

    private boolean isValid(final ISegment pSegment) { // need to maks sure that not only the pixels are close to the line but the line is close to the pixels
        if (pSegment == null) return false;
        if (pSegment.getPixelLength() < 4) return true;

        final int startIndexPlus = pSegment.getStartIndex() + 1;
        final Point startPointPlus = getPixel(startIndexPlus).getUHVWPoint();
        final double startPlusLambda = pSegment.closestLambda(startPointPlus);

        final int endIndexMinus = pSegment.getEndIndex() - 1;
        final Point endPointMinus = getPixel(endIndexMinus).getUHVWPoint();
        final double endMinusLambda = pSegment.closestLambda(endPointMinus);

        final boolean valid = startPlusLambda < 0.5d && endMinusLambda > 0.5d;
        return valid;
    }

    public Pixel lastPixel() {
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

    public void reCalcSegments() {
        mSegments.removeAllElements();
        for (IVertex vertex = getStartVertex(); vertex.getEndSegment() != null; vertex = vertex.getEndSegment().getEndVertex()) {
            mSegments.add(vertex.getEndSegment());
        }
    }

    public void refine() {
        refine01_matchCurves();
        refine03_matchDoubleCurves();
    }

    private void refine01_matchCurves() {

        if (mSegments.size() == 1) {
            return;
        }

        final Vector<ISegment> segments = new Vector<ISegment>();

        for (final ISegment currentSegment : mSegments) {
            final IVertex startVertex = currentSegment.getStartVertex();
            final IVertex endVertex = currentSegment.getEndVertex();

            // get error values from straight line to start the compare
            ISegment bestSegment = currentSegment;
            double lowestError = currentSegment.calcError();
            lowestError *= getTransformSource().getLineCurvePreference();

            try {

                if (currentSegment == mSegments.firstElement()) {
                    // first segment
                    try {
                        // calculate end tangent
                        final Line tangent = currentSegment.getEndVertex().getTangent();

                        if (tangent == null) {
                            // this was for test purposes
                            final Line tangent2 = currentSegment.getEndVertex().getTangent();
                        }
                        // find closest point between start point and tangent line
                        final Point closest = tangent.closestPoint(currentSegment.getStartUHVWPoint());
                        // divide this line (tangentRuler) into the number of pixels in the segment
                        // for each of the points on the division find the lowest error
                        final LineSegment tangentRuler = new LineSegment(closest, currentSegment.getEndUHVWPoint());
                        for (int i = 1; i < currentSegment.getPixelLength(); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                            try {
                                final double lambda = (double) i / currentSegment.getPixelLength();
                                final Point p1 = tangentRuler.getPoint(lambda);
                                final ISegment candidateCurve = SegmentFactory.createTempCurveSegmentTowards(currentSegment.getStartVertex(), currentSegment.getEndVertex(), p1);
                                final double candidateError = candidateCurve != null ? candidateCurve.calcError() : 0.0d;

                                if (isValid(candidateCurve) && candidateError < lowestError) {
                                    lowestError = candidateError;
                                    bestSegment = candidateCurve;
                                }

                            } catch (final Throwable pT) {
                                System.err.println(pT);
                            }
                        }
                    } catch (final Throwable pT) {
                        mLogger.log(Level.INFO, "Error: ", pT);
                    } finally {
                        if (bestSegment instanceof LineSegment) {
                            final LineSegment lineSegment = new LineSegment(currentSegment.getStartUHVWPoint(), currentSegment.getEndUHVWPoint());
                            currentSegment.getEndVertex().setTangent(lineSegment);
                        }
                    }

                } else if (currentSegment == mSegments.lastElement()) {
                    // end segment
                    try {
                        // calculate start tangent
                        final Line tangent = currentSegment.getStartVertex().getTangent();
                        // find closest point between start point and tangent line
                        if (tangent == null) {
                            final Line tangent2 = currentSegment.getStartVertex().getTangent();
                        }
                        final Point closest = tangent.closestPoint(currentSegment.getEndUHVWPoint());
                        // divide this line (tangentRuler) into the number of pixels in the segment
                        // for each of the points on the division find the lowest error
                        final Line tangentRuler = new Line(currentSegment.getStartUHVWPoint(), closest);
                        for (int i = 1; i < currentSegment.getPixelLength(); i++) { // first and last pixel will throw an error and are equivalent to the straight line
                            try {
                                final double lambda = (double) i / currentSegment.getPixelLength();
                                final Point p1 = tangentRuler.getPoint(lambda);
                                final ISegment candidateCurve = SegmentFactory.createTempCurveSegmentTowards(currentSegment.getStartVertex(), currentSegment.getEndVertex(), p1);
                                final double candidateError = candidateCurve.calcError();

                                if (isValid(candidateCurve) && candidateError < lowestError) {
                                    lowestError = candidateError;
                                    bestSegment = candidateCurve;
                                }

                            } catch (final Throwable pT) {
                                System.err.println(pT);
                            }
                        }
                    } catch (final Throwable pT) {
                        System.err.println(pT);
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
                            Line startTangent = currentSegment.getStartVertex().getTangent();
                            Line endTangent = currentSegment.getEndVertex().getTangent();

                            if (startTangent != null && endTangent != null) {
                                Point p1 = startTangent.intersect(endTangent);
                                if (p1 != null && startTangent.closestLambda(p1) > 0.0d && endTangent.closestLambda(p1) < 0.0d) {
                                    Line newStartTangent = new Line(p1, currentSegment.getStartVertex().getUHVWPoint());
                                    Line newEndTangent = new Line(p1, currentSegment.getEndVertex().getUHVWPoint());
                                    p1 = newStartTangent.intersect(newEndTangent);
                                    // if (p1 != null && newStartTangent.getAB().dot(startTangent.getAB()) > 0.0d && newEndTangent.getAB().dot(endTangent.getAB()) > 0.0d) {
                                    ISegment candidateCurve = SegmentFactory.createTempCurveSegmentTowards(currentSegment.getStartVertex(), currentSegment.getEndVertex(), p1);
                                    double candidateError = candidateCurve.calcError();

                                    if (isValid(candidateCurve) && candidateError < lowestError) {
                                        lowestError = candidateError;
                                        bestSegment = candidateCurve;
                                    }
                                    // }
                                }
                            }
                        } catch (Throwable pT) {
                            System.err.println(pT);
                        }

                        // method 2 - looking at going through pixels
                        // try {
                        // for (int i = startVertex.getIndex() + 2; i < endVertex.getIndex() - 2; i++) { // first and last pixel will throw an error and are equivalent to the straight line
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
                        // System.err.println(pT);
                        // }
                        // }
                        //
                        // } catch (final Throwable pT) {
                        // System.err.println(pT);
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
                        System.err.println(pT);
                    }
                }
            } finally {
                bestSegment.attachToVertexes(false);
                segments.add(bestSegment);
            }
        } // end loop
        mSegments = segments;
        validate();
    }

    private Line getDCStartTangent(ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex startVertex = pSegment.getStartVertex();
        if (startVertex.getStartSegment() instanceof StraightSegment) {
            return startVertex.getStartSegment().getEndTangent();
        }

        return startVertex.getTangent();
    }

    private Line getDCEndTangent(ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex endVertex = pSegment.getEndVertex();
        if (endVertex.getEndSegment() instanceof StraightSegment) {
            return endVertex.getEndSegment().getStartTangent();
        }

        return endVertex.getTangent();
    }

    private void refine03_matchDoubleCurves() {

        if (mSegments.size() == 1) {
            return;
        }

        final Vector<ISegment> segments = new Vector<ISegment>();

        for (final ISegment currentSegment : mSegments) {
            final IVertex startVertex = currentSegment.getStartVertex();
            final IVertex endVertex = currentSegment.getEndVertex();
            final Line startTangent = getDCStartTangent(currentSegment);
            final Line endTangent = getDCEndTangent(currentSegment);

            // get error values from straight line to start the compare
            ISegment bestSegment = currentSegment;
            double lowestError = currentSegment.calcError();
            if (currentSegment instanceof StraightSegment) {
                lowestError *= getTransformSource().getLineCurvePreference();
            }

            if (currentSegment instanceof CurveSegment) {
                lowestError = -1.0d;
            }

            try {
                try {
                    for (int i = 1; i < currentSegment.getPixelLength() - 1; i++) { // first and last pixel will throw an error and are equivalent to the straight line
                        final Vertex midVertex = Vertex.createVertex(this, startVertex.getIndex() + i);

                        Point closestStart = startTangent.closestPoint(midVertex.getUHVWPoint());
                        Line startLine = new Line(startVertex.getUHVWPoint(), closestStart);

                        Point closestEnd = endTangent.closestPoint(midVertex.getUHVWPoint());
                        Line endLine = new Line(endVertex.getUHVWPoint(), closestEnd);
                        // should check that lambdas are the correct sign

                        ISegment candidateCurve;
                        Point p1;
                        Point p2;

                        for (double sigma = 0.3d; sigma < 0.7d; sigma += 0.1d) {
                            // double sigma = 0.5d;
                            try {
                                // try from start
                                p1 = startLine.getPoint(sigma);
                                p2 = new Line(p1, midVertex.getUHVWPoint()).intersect(endLine);
                                if (p2 != null) {
                                    double closestLambda = endLine.closestLambda(p2);

                                    if (closestLambda > 0.1d && closestLambda < 1.2d) {

                                        candidateCurve = SegmentFactory.createTempDoubleCurveSegment(startVertex, p1, midVertex, p2, endVertex);

                                        if (candidateCurve != null) {
                                            final double candidateError = candidateCurve.calcError();

                                            if (isValid(candidateCurve) && candidateError < lowestError) {
                                                lowestError = candidateError;
                                                bestSegment = candidateCurve;
                                            }
                                        }
                                    }
                                }

                            } catch (final Throwable pT) {
                                System.err.println(pT);
                            }

                            // try from end
                            try {
                                p2 = endLine.getPoint(sigma);
                                p1 = new Line(p2, midVertex.getUHVWPoint()).intersect(startLine);
                                double closestLambda = startLine.closestLambda(p2);

                                if (p1 != null && 0.1d < closestLambda && closestLambda < 1.2d) { // TODO what are these magic numbers

                                    candidateCurve = SegmentFactory.createTempDoubleCurveSegment(startVertex, p1, midVertex, p2, endVertex);

                                    if (candidateCurve != null) {
                                        final double candidateError = candidateCurve.calcError();

                                        if (isValid(candidateCurve) && candidateError < lowestError) {
                                            // lowestError = candidateError;
                                            // bestSegment = candidateCurve;
                                        }
                                    }
                                }
                            } catch (final Throwable pT) {
                                System.err.println(pT);
                            }
                        }
                    }
                } catch (final Throwable pT) {
                    mLogger.log(Level.INFO, "Error: ", pT);
                } finally {
                    if (bestSegment instanceof DoubleCurveSegment) {
                        // final LineSegment lineSegment = new LineSegment(currentSegment.getStartUHVWPoint(), currentSegment.getEndUHVWPoint());
                        // currentSegment.getEndVertex().setTangent(lineSegment);
                        mLogger.info("DCS m " + ((DoubleCurveSegment) bestSegment).getStartCurve().getEndTangentVector());
                        mLogger.info("DCS m " + ((DoubleCurveSegment) bestSegment).getEndCurve().getStartTangentVector());

                        mLogger.info("DoubleCurve added");
                    }
                }

            } finally {
                bestSegment.attachToVertexes(false);
                segments.add(bestSegment);
            }
        } // end loop
        mSegments = segments;
        validate();
    }

//    private void refine03_matchDoubleCurves_original() {
//
//        if (mSegments.size() == 1) {
//            return;
//        }
//
//        final Vector<ISegment> segments = new Vector<ISegment>();
//
//        for (final ISegment currentSegment : mSegments) {
//            final IVertex startVertex = currentSegment.getStartVertex();
//            final IVertex endVertex = currentSegment.getEndVertex();
//            final Line startTangent = startVertex.getTangent();
//            final Line endTangent = endVertex.getTangent();
//
//            // get error values from straight line to start the compare
//            ISegment bestSegment = currentSegment;
//            double lowestError = currentSegment.calcError();
//            if (currentSegment instanceof StraightSegment) {
//                lowestError *= getTransformSource().getLineCurvePreference();
//            }
//
//            try {
//                try {
//
//                    for (int i = 1; i < currentSegment.getPixelLength() - 1; i++) { // first and last pixel will throw an error and are equivalent to the straight line
//                        try {
//                            final Vertex through = Vertex.createVertex(this, startVertex.getIndex() + i);
//                            final ISegment candidateCurve = SegmentFactory.createTempDoubleCurveSegment(startVertex, startTangent, endVertex, endTangent, through);
//                            if (candidateCurve != null) {
//                                final double candidateError = candidateCurve.calcError();
//
//                                if (isValid(candidateCurve) && candidateError < lowestError) {
//                                    lowestError = candidateError;
//                                    bestSegment = candidateCurve;
//                                }
//                            }
//                        } catch (final Throwable pT) {
//                            System.err.println(pT);
//                        }
//                    }
//                } catch (final Throwable pT) {
//                    mLogger.log(Level.INFO, "Error: ", pT);
//                } finally {
//                    if (bestSegment instanceof DoubleCurveSegment) {
//                        // final LineSegment lineSegment = new LineSegment(currentSegment.getStartUHVWPoint(), currentSegment.getEndUHVWPoint());
//                        // currentSegment.getEndVertex().setTangent(lineSegment);
//                        mLogger.info("DoubleCurve added");
//                    }
//                }
//
//            } finally {
//                bestSegment.attachToVertexes(false);
//                segments.add(bestSegment);
//            }
//        } // end loop
//        mSegments = segments;
//        validate();
//    }

    public void restoreFrom(final PixelChain pCopy) {
        // setStartNode(pCopy.getStartNode());
        // setEndNode(pCopy.getEndNode());
        setStartVertex(pCopy.getStartVertex());
        reCalcSegments();
        validate();
        getPixelMap().indexSegments();
    }

    // public void setStartVertex(final Vertex pStartVertex) {
    // Framework.logEntry(mLogger);
    // Framework.logParams(mLogger, "pStartVertex", pStartVertex);
    //
    // mStartVertex = pStartVertex;
    //
    // Framework.logExit(mLogger);
    // }

    /**
     * Reverses the order of the pixels in the pixel chain. This means reversing the start and end nodes. And all of the segments in the line are also reversed and repaced with new straight line
     * segments.
     */
    private void reverse() {
        // note that this uses direct access to the data members as the public setters have other side effects
        final Node tmp = getStartNode();
        mStartNode = getEndNode();
        mEndNode = tmp;
        Collections.reverse(mPixels);

        final Vector<ISegment> stnemges = new Vector<ISegment>();
        final int maxPixelIndex = mPixels.size() - 1;

        IVertex start = Vertex.createVertex(this, 0);
        for (int i = mSegments.size() - 1; i >= 0; i--) {
            final ISegment segment = mSegments.get(i);
            final IVertex end = Vertex.createVertex(this, maxPixelIndex - segment.getStartIndex());
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(start, end);
            newSegment.attachToVertexes(false);
            stnemges.add(newSegment);
            start = end;
        }
        mSegments = stnemges;
        mStartVertex = mSegments.firstElement().getStartVertex();
        mStartVertex.setIndex(0);
        validate();
    }

    public void setEndNode(final Node pNode) {
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

        mStartNode.setPixelMap(pPixelMap);

        for (final ISegment segment : mSegments) {
            segment.getStartPixel().setPixelMap(pPixelMap);
            segment.getEndPixel().setPixelMap(pPixelMap);
        }

        Framework.logExit(mLogger);
    }

    public void setStartNode(final Node pNode) {
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
        mStartVertex = Vertex.createVertex(this, 0);

        Framework.logExit(mLogger);
    }

    private void setStartVertex(final IVertex pVertex) {
        mStartVertex = pVertex;
    }

    public void setThickness(final int pThinLength, final int pNormalLength, final int pLongLength) {
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

    public void setThickness(final Thickness thickness) {
        this.mThickness = thickness;
    }

    private PixelChain shallowCopyFrom(final ISegment pSegment) {
        final PixelChain copy = new PixelChain(new Node(pSegment.getEndPixel()));

        for (int i = pSegment.getEndIndex() + 1; i < mPixels.size() - 1; i++) {
            final Pixel pixel = getPixel(i);
            copy.add(pixel);
        }

        copy.setEndNode(new Node(mPixels.lastElement()));
        final int delta = pSegment.getEndIndex();

        IVertex startVertex = copy.getStartVertex();
        for (int i = mSegments.indexOf(pSegment) + 1; i < mSegments.size(); i++) {
            final ISegment segment = mSegments.elementAt(i);

            final IVertex endVertex = copy.createVertex(segment.getEndIndex() - delta);
            final ISegment copySegment = segment.copy(startVertex, endVertex);
            copySegment.attachToVertexes(false);
            startVertex = endVertex;
            copy.mSegments.add(segment);
        }

        copy.validate();
        return copy;
    }

    private PixelChain shallowCopyTo(final ISegment pSegment) {
        final PixelChain copy = new PixelChain(getStartNode());

        for (int i = 1; i < pSegment.getStartIndex(); i++) {
            final Pixel pixel = getPixel(i);
            copy.add(pixel);
        }

        copy.setEndNode(new Node(pSegment.getStartPixel()));

        IVertex startVertex = copy.getStartVertex();
        for (final ISegment segment : mSegments) {
            if (segment == pSegment) {
                break;
            }

            final IVertex endVertex = copy.createVertex(segment.getEndIndex());
            final ISegment copySegment = segment.copy(startVertex, endVertex);
            copySegment.attachToVertexes(false);
            startVertex = endVertex;
            copy.mSegments.add(segment);
        }

        copy.validate();
        return copy;
    }

    public Path toPath() {
        final Path path = new Path();
        if (getSegmentCount() != 0) {
            path.moveTo(getFirstSegment().getStartPixel().getUHVWPoint());

            for (final ISegment segment : getAllSegments()) {
                segment.addToPath(path);
            }
        }
        return path;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
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

    void validate() {
        if (getStartVertex().getIndex() != 0) { //
            throw new IllegalStateException("getStartVertex().getIndex() != 0");
        }

        reCalcSegments();
        int nextStartIndex = 0;

        for (final ISegment segment : mSegments) {
            if (segment.getStartIndex() != nextStartIndex) { //
                throw new IllegalStateException("segments not linked properly");
            }
            nextStartIndex = segment.getEndIndex();
        }

        if (mSegments.size() != 0 && mSegments.lastElement().getEndIndex() != mPixels.size() - 1) { //
            throw new IllegalStateException("last segment not linked properly");
        }

        if (mSegments.size() == 0) { //
            throw new IllegalStateException("there are no segments");
        }

        checkAllVertexesAttached();
    }

    public int getPixelLength() {
        return mPixels.size();
    }

    public void setInChain(boolean pValue) {
        for (Pixel pixel : mPixels) {
            pixel.setInChain(pValue);
        }
    }

    private void setEdge() {
        mPixels.stream()
                .filter(p -> p != mPixels.firstElement())
                .filter(p -> p != mPixels.lastElement())
                .forEach(p -> {
                    p.setEdge(false);
                });
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
//package com.ownimage.perception.pixelMap;
//
//import com.ownimage.perception.math.Point;
//import com.ownimage.perception.pixelMap.segment.SegmentBase;
//
//public class PixelChain {
//    public PixelChain(Node node) {
//
//    }
//
//    public Pixel getPixel(final int averageIndex) {
//        return null;
//    }
//
//    public void reCalcSegments() {
//
//    }
//
//    public Point getUHVWPoint(final int i) {
//        return null;
//    }
//
//    public void deleteSegment(final SegmentBase segmentBase) {
//    }
//
//    public double getActualThickness(final double pPosition) {
//        return 0;
//    }
//
//    public double length() {
//        return 0;
//    }
//
//    public void deleteVertex(final Vertex vertex) {
//
//    }
//
//    public PixelMap getPixelMap() {
//        return null;
//    }
//
//    public void addToNodes() {
//    }
//}
