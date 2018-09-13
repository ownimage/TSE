/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Counter;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.ImmutableLayerMap2D;
import com.ownimage.framework.util.KColor;
import com.ownimage.framework.util.MyBase64;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.DoubleCurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.transform.CannyEdgeTransform;

import io.vavr.Tuple2;

/*
 * The Class PixelMap is so that there is an efficient way to manipulate the edges once it has passed through the Canny Edge Detector.  This class and all of its supporting classes work in UHVW units.
 * <br/> Who owns what:
 * <code>
 * <br/> PixelMap
 * <br/> +-- Pixel
 * <br/> +-- Node
 * <br/> +-- PixelChain
 * <br/>     +-- Segment
 * <br/>     +-- Vertex
 * </code>
 */
public class PixelMap implements Serializable, IPersist, PixelConstants {

    private static class AllNodes implements Iterable<Node>, Serializable {
        // note made this static so that when it is serialized there is not the attempt to serialize the parent.

        private final HashMap<Pixel, Node> mNodes = new HashMap<>();

        private Node add(final Pixel pPixel) {
            pPixel.getPixelMap().setData(pPixel, true, NODE);
            final Node node = new Node(pPixel);
            mNodes.put(pPixel, node);
            return node;
        }

        private Node getNode(final Pixel pPixel) {
            return mNodes.get(pPixel);
        }

        @Override
        public Iterator<Node> iterator() {
            return mNodes.values().iterator();
        }

        void remove(final Pixel pPixel) {
            pPixel.getPixelMap().setData(pPixel, false, NODE);
            mNodes.remove(pPixel);
        }

        private void removeAll(final Set<Pixel> pPixels) {
            pPixels.forEach(this::remove);
        }

        Stream<Node> stream() {
            return mNodes.values().stream();
        }

        private void removeAllElements() {
            mNodes.clear();
        }

        int size() {
            return mNodes.size();
        }

    }

    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;
    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    private int mWidth;
    private int mHeight;

    // TODO should delete the following two values
    private final boolean m360;
    private final IPixelMapTransformSource mTransformSource;

    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     */
    private final double mAspectRatio;
    private final Point mUHVWHalfPixel;
    private ImmutableLayerMap2D<Byte>.Map2D mData;
    private final AllNodes mAllNodes = new AllNodes();
    private Vector<PixelChain> mPixelChains = new Vector<>();

    private LinkedList<Tuple2<PixelChain, ISegment>>[][] mSegmentIndex;
    private Map<ISegment, PixelChain> mSegmentToPixelChainMap = new HashMap<>();

    private boolean mSegmentIndexValid = false;
    private int mSegmentCount;

    /**
     * Means that the PixelMap will add/remove/reapproximate PixelChains as nodes are added and removed.
     * This is turned off whilst the bulk processing is running. // TODO should this extend to the conversion of Pixels to Nodes etc.
     */
    private boolean mAutoTrackChanges = false;

    public PixelMap(final int pWidth, final int pHeight, final boolean p360, final IPixelMapTransformSource pTransformSource) {
        setWidth(pWidth);
        setHeight(pHeight);
        m360 = p360;
        mTransformSource = pTransformSource;

        resetSegmentIndex();
        // mHalfPixel = new Point(0.5d / getHeight(), 0.5d / getWidth());
        mAspectRatio = (double) pWidth / pHeight;
        mData = new ImmutableLayerMap2D<>(pWidth, pHeight, (byte) 0).getMutable();
        // resetSegmentIndex();
        mUHVWHalfPixel = new Point(0.5d * mAspectRatio / pWidth, 0.5d / pHeight);
    }

    private boolean getShowPixels() {
        return mTransformSource.getShowPixels();
    }

    private Color getPixelColor() {
        return mTransformSource.getPixelColor();
    }

    private void addPixelChains(final Collection<PixelChain> pPixelChains) {
        Framework.logEntry(mLogger);
        indexSegments();
        mPixelChains.addAll(pPixelChains);
        Framework.logExit(mLogger);
    }

    public List<PixelChain> getPixelChains(final Pixel pPixel) {
        Framework.checkParameterNotNull(mLogger, pPixel, "pPixel");
        Framework.logEntry(mLogger);
        final List<PixelChain> pixelChains = mPixelChains.stream().filter(pc -> pc.contains(pPixel)).collect(Collectors.toList());
        Framework.logExit(mLogger);
        return pixelChains;
    }

    // public void addVertexes(final Vector<IVertex> pVisibleVertexes, final Pixel pOrigin, final Pixel pTopLeft) {
    // Framework.logEntry(mLogger);
    // for (final PixelChain pixelChain : getAllPixelChains()) {
    // pixelChain.checkAllVertexesAttached();
    // pixelChain.addVertexes(pVisibleVertexes, pOrigin, pTopLeft);
    // }
    // Framework.logExit(mLogger);
    // }

//    private AllPixels allPixels() {
//        return new AllPixels();
    //  }


    private synchronized void calcSegmentIndex() {
        if (!mSegmentIndexValid) {
            Framework.logEntry(mLogger);
            resetSegmentIndex();

            Vector<PixelChain> pixelChains = new Vector<>();
            mPixelChains.forEach(pc -> pixelChains.add(pc.indexSegments()));
            mPixelChains = pixelChains;

            //printNodeCounts();
            mSegmentIndexValid = true;
        }
        Framework.logExit(mLogger);
    }

    public void equalizeValues(final EqualizeValues pValues) {
        // TODO do not like this mutable parameter

        int totalLength = 0;
        for (final PixelChain chain : mPixelChains) {
            totalLength += chain.getPixelLength();
        }

        final Vector<PixelChain> sortedChains = getPixelChainsSortedByLength();

        final int shortThreshold = (int) (totalLength * pValues.getIgnoreFraction());
        final int mediumThreshold = (int) (totalLength * (pValues.getIgnoreFraction() + pValues.getShortFraction()));
        final int longThreshold = (int) (totalLength * (pValues.getIgnoreFraction() + pValues.getShortFraction() +
                pValues.getMediumFraction()));

        Integer shortLength = null;
        Integer mediumLength = null;
        Integer longLength = null;

        int currentLength = 0;
        for (final PixelChain chain : sortedChains) {
            currentLength += chain.getPixelLength();

            if (shortLength == null && currentLength > shortThreshold) {
                shortLength = chain.getPixelLength();
            }

            if (mediumLength == null && currentLength > mediumThreshold) {
                mediumLength = chain.getPixelLength();
            }

            if (longLength == null && currentLength > longThreshold) {
                longLength = chain.getPixelLength();
                break;
            }
        }

        pValues.setReturnValues(shortLength, mediumLength, longLength);
    }

    private void generateChain(final Node pStartNode, final Pixel pCurrentPixel, final PixelChain pChain) {
        try {
            Framework.logEntry(mLogger);
            if (mLogger.isLoggable(Level.FINEST)) {
                mLogger.finest("pStartNode: " + pStartNode);
                mLogger.finest("pCurrentPixel: " + pCurrentPixel);
                mLogger.finest("pChain: " + pChain);
            }

            if (pCurrentPixel.isNode()) {
                pChain.setEndNode(getNode(pCurrentPixel));
                Framework.logExit(mLogger);
                return;
            }

            pChain.add(pCurrentPixel);
            pCurrentPixel.setInChain(true);
            pCurrentPixel.setVisited(true);

            // try to end quickly at a node to prevent bypassing
            for (final Pixel nodalNeighbour : pCurrentPixel.getNodeNeighbours()) {
                // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
                // going back to the staring node.
                // if ((nodalNeighbour.isUnVisitedEdge() || nodalNeighbour.isNode()) && (pChain.count() != 2 ||
                // !nodalNeighbour.isNeighbour(pChain.firstPixel()))) {
                if ((nodalNeighbour.isUnVisitedEdge() || nodalNeighbour.isNode()) && !(pChain.count() == 2 &&
                        nodalNeighbour.equals(pChain.firstPixel()))) {
                    generateChain(pStartNode, nodalNeighbour, pChain);
                    Framework.logExit(mLogger);
                    return;
                }
            }

            // otherwise go to the next pixel normally
            for (final Pixel neighbour : pCurrentPixel.getNeighbours()) {
                // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
                // going back to the staring node.
                // if ((neighbour.isUnVisitedEdge() || neighbour.isNode()) && (pChain.count() != 2 ||
                // !neighbour.isNeighbour(pChain.firstPixel()))) {
                if ((neighbour.isUnVisitedEdge() || neighbour.isNode()) && !(pChain.count() == 2 && neighbour.equals(pChain.getStartNode()))) {
                    generateChain(pStartNode, neighbour, pChain);
                    Framework.logExit(mLogger);
                    return;
                }
            }

        } catch (final Throwable pT) {
            mLogger.log(Level.SEVERE, "Exception thrown", pT);
            mLogger.severe("pStartNode: " + pStartNode);
            mLogger.severe("pCurrentPixel: " + pCurrentPixel);
            mLogger.severe("pChain: " + pChain);
        }
    }

    private Collection<PixelChain> generateChains(final Node pStartNode) {
        final Vector<PixelChain> chains = new Vector<>();
        pStartNode.setVisited(true);
        pStartNode.setInChain(true);
        pStartNode.getNeighbours().forEach(neighbour -> {
            if (neighbour.isNode() || neighbour.isEdge() && !neighbour.isVisited()) {
                final PixelChain chain = new PixelChain(pStartNode);
                generateChain(pStartNode, neighbour, chain);
                if (chain.length() > 2) {
                    chains.add(chain);
                    chain.addToNodes();
                }
            }
        });
        return chains;
    }

    boolean getData(final Pixel pPixel, final byte pValue) {
        if (0 <= pPixel.getY() && pPixel.getY() < getHeight()) {
            final int x = modWidth(pPixel.getX());
            return (getValue(x, pPixel.getY()) & pValue) != 0;
        } else {
            return false;
        }
    }

    public int getHeight() {
        return mHeight;
    }

    private double getLineOpacity() {
        return mTransformSource.getLineOpacity();
    }

    public double getLineTolerance() {
        return mTransformSource.getLineTolerance();
    }

    // public int getLongLineLength() {
    // return mTransformSource.getLongLineLength();
    // }

    private double getLongLineThickness() {
        return mTransformSource.getLongLineThickness();
    }


    private Color getMaxiLineColor() {
        return mTransformSource.getLineColor();
    }


    private boolean getShowLines() {
        return mTransformSource.getShowLines();
    }

    private Color transformGetLineColor(final Point pIn, final Color pColor, final boolean pThickOnly) {
        return getShowLines() ?
                transformGetLineColor(pIn, pColor, getMaxiLineColor(), getLineOpacity(), 1.0d, pThickOnly) :
                pColor;
    }

    private Color transformGetLineColor(final Point pIn, final Color pColorIn, final Color pLineColor, final double pOpacity, final
    double pThicknessMultiplier, final boolean pThickOnly) {
        final double shortThickness = getMediumLineThickness() * pThicknessMultiplier / 1000d;
        final double normalThickness = getShortLineThickness() * pThicknessMultiplier / 1000d;
        final double longThickness = getLongLineThickness() * pThicknessMultiplier / 1000d;
        if (isAnyLineCloserThan(pIn, shortThickness, normalThickness, longThickness, pThicknessMultiplier, pThickOnly)) {
            return KColor.fade(pColorIn, pLineColor, pOpacity);
        }
        return pColorIn;
    }

    // private Color getMaxiLineColor3D(final Point pIn, final Color pColorIn, final Color pLineColor, final double pOpacity) {
    // final double shortThickness = getMediumLineThickness() / 1000d;
    // final double normalThickness = getShortLineThickness() / 1000d;
    // final double longThickness = getLongLineThickness() / 1000d;
    // final Intersect3D intersect = getMaxIntersect3d(pIn, shortThickness, normalThickness, longThickness);
    //
    // if (intersect != null) {
    // final Point3D light = new Point3D(-10.0d, 0.0d, 10.0d);
    // final Point3D point = intersect.getPoint();
    // final Vector3D normal = intersect.getNormal();
    //
    // final Vector3D vectorToLight = point.to(light).normalize();
    // final double lightDotNormal = vectorToLight.dot(normal);
    //
    // final Point3D eye = new Point3D(0.5d, 0.5d, 200.0d);
    // final Vector3D vectorToEye = point.to(eye).normalize();
    // // final double eyeDotNormal = vectorToEye.dot(normal);
    //
    // if (lightDotNormal > 0.0d) { // need to avoid highlights on hidden surfaces
    //
    // final Vector3D reflectedDirection = normal.scale(2.0d * lightDotNormal).minus(vectorToLight);
    // final double cos = reflectedDirection.normalize().dot(vectorToEye);
    // double highlight = Math.pow(cos, 4.0d);
    //
    // final Range range = Range.ZeroToOne;
    // highlight = range.getBoundedValue(highlight);
    // if (highlight < 0.0d || highlight > 1.0d) {
    // final int i = 1;
    // }
    // final Color lineColor = KColor.fade(pLineColor, Color.WHITE, highlight);
    // return KColor.fade(pColorIn, lineColor, pOpacity);
    // // return Color.RED;
    // }
    // return KColor.fade(pColorIn, pLineColor, pOpacity);
    // }
    //
    // return pColorIn;
    // }
    //
    private Color getMaxiLineShadowColor(final Point pIn, final Color pColor) {
        if (getShowShadow()) {
            double x = pIn.getX() - getShadowXOffset() / 1000d;
            x = x < 0 ? 0 : x > getWidth() - 1 ? x - getWidth() : x;

            double y = pIn.getY() - getShadowYOffset() / 1000d;
            y = y < 0 ? 0 : y > getHeight() - 1 ? y - (getHeight() - 1) : y;

            final Point uhvw = new Point(x, y);
            return transformGetLineColor(uhvw, pColor, getShadowColor(), getShadowOpacity(), getShadowThickness(), true);
        }
        return pColor;
    }

    //
    // private Intersect3D getMaxIntersect3d(final Point pPoint, final double pThinWidth, final double pNormalWidth, final double
    // pThickWidth) {
    // calcSegmentIndex();
    //
    // final double maxWidth = KMath.max(pThinWidth, pNormalWidth, pThickWidth);
    // final Point uhvw = toUHVW(pPoint);
    // Intersect3D maxIntersect = null;
    //
    // for (int x = (int) Math.floor((uhvw.getX() - maxWidth) * getWidth() / mAspectRatio) - 1; x <= Math.ceil((uhvw.getX() +
    // maxWidth) * getWidth() / mAspectRatio) + 1; x++) {
    // for (int y = (int) (Math.floor(uhvw.getY() * getHeight()) - maxWidth) - 1; y <= Math.ceil(uhvw.getY() * getHeight() +
    // maxWidth) + 1; y++) {
    // if (0 <= x && x < getWidth() && 0 <= y && y < getHeight()) {
    // for (final ISegment segment : getSegments(x, y)) {
    // if (segment.getPixelChains().length() < getShortLineLength()) {
    // break;
    // }
    // if (segment.getPixelLength() > getShortLineLength()) {
    // final Intersect3D intersect = segment.intersect3D(uhvw);
    // if (maxIntersect == null || intersect != null && intersect.getPoint().getZ() > maxIntersect.getPoint().getZ()) {
    // maxIntersect = intersect;
    // }
    // }
    // }
    // }
    // }
    // }
    // return maxIntersect;
    // }
    //
    // public int getMediumLineLength() {
    // return mTransformSource.getMediumLineLength();
    // }
    //
    private double getMediumLineThickness() {
        return mTransformSource.getMediumLineThickness();
    }


    /**
     * Gets the Node at the PixelPosition, BEWARE that mAllNodes is not made persistent so this is NOT valid after a transform
     * load,
     * only during the generate phase
     *
     * @param pPixel the pixel
     * @return the node
     */
    private Node getNode(final Pixel pPixel) {
        Node node = mAllNodes.getNode(pPixel);
        if (node == null) {
            node = new Node(pPixel);
        }
        return node;
    }

    public double getNormalWidth() {
        return getMediumLineThickness() / 1000d;
    }

    private Optional<Pixel> getOptionalPixelAt(final double pX, final double pY) {
        Framework.logEntry(mLogger);
        // Framework.logParams(mLogger, "pX, pY", pX, pY);

        int x = (int) (pX * getWidth());
        int y = (int) (pY * getHeight());
        y = y == getHeight() ? getHeight() - 1 : y;
        x = modWidth(x);
        final Pixel pixel = getPixelAt(x, y);

        Framework.logExit(mLogger);
        return Optional.ofNullable(pixel);
    }

    @Deprecated
    public Pixel getPixelAt(final double pX, final double pY) {
        Framework.logEntry(mLogger);
        // Framework.logParams(mLogger, "pX, pY", pX, pY);

        int x = (int) (pX * getWidth());
        int y = (int) (pY * getHeight());
        y = y == getHeight() ? getHeight() - 1 : y;
        x = modWidth(x);
        final Pixel pixel = getPixelAt(x, y);

        Framework.logExit(mLogger);
        return pixel;
    }

    @Deprecated
    public Pixel getPixelAt(final int pX, final int pY) {
        return new Pixel(this, pX, pY);
    }

    public Optional<Pixel> getOptionalPixelAt(final int pX, final int pY) {
        if (0 > pY || pY >= getHeight()) return Optional.empty();
        if (!m360 && (0 > pX || pX >= getWidth())) return Optional.empty();
        int x = modWidth(pX);
        return Optional.of(new Pixel(this, x, pY));
    }

    private Optional<Pixel> getOptionalPixelAt(final Point pPoint) {
        return getOptionalPixelAt(pPoint.getX(), pPoint.getY());
    }

    @Deprecated
    public Pixel getPixelAt(final Point pPoint) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pPoint", pPoint);

        final Pixel pixel = getPixelAt(pPoint.getX(), pPoint.getY());

        Framework.logExit(mLogger);
        return pixel;
    }


    private Vector<PixelChain> getPixelChainsSortedByLength() {
        final Vector<PixelChain> chains = new Vector<>(mPixelChains); // this will be the sorted collection
        Collections.sort(chains, (pChain1, pChain2) -> pChain1.getPixelLength() - pChain2.getPixelLength());
        return chains;
    }

    private Color transformGetPixelColor(final Point pIn, final Color pColor) {
        Color result = pColor;
        if (getShowPixels()) {
            Optional<Pixel> pixel = getOptionalPixelAt(pIn);
            if (pixel.isPresent() && pixel.get().isEdge()) {
                result = getPixelColor();
            }
        }
        return result;
    }

    @Override
    public String getPropertyName() {
        // TODO Auto-generated method stub
        return null;
    }

    private AbstractCollection<Tuple2<PixelChain, ISegment>> getSegments(final int pX, final int pY) {

        if (pX < 0 || pX >= getWidth()) {
            throw new IllegalArgumentException("pX out of range. pX = " + pX + ". It needs to be greater than 0 and less than" + getWidth());
        }

        if (pY < 0 || pY >= getHeight()) {
            throw new IllegalArgumentException("pY out of range. pY = " + pY + ". It needs to be greater than 0 and less than" + getHeight());
        }

        if (mSegmentIndex[pX][pY] == null) { //
            mSegmentIndex[pX][pY] = new LinkedList<>();
        }
        return mSegmentIndex[pX][pY];
    }

    private Color getShadowColor() {
        return mTransformSource.getShadowColor();
    }

    private double getShadowOpacity() {
        return mTransformSource.getShadowOpacity();
    }

    private double getShadowThickness() {
        return mTransformSource.getShadowThickness();
    }

    private double getShadowXOffset() {
        return mTransformSource.getShadowXOffset();
    }

    private double getShadowYOffset() {
        return mTransformSource.getShadowYOffset();
    }

    // private Color getShortLineColor(final Point pIn, final Color pColor) {
    // // if (mShowShortLines.getBoolean()) {
    // // Pixel pixelIn = getEdgeData().getPixelAt(pIn);
    // // Point pictureResolutionPoint = getEdgeData().toUHVW(pIn);
    // //
    // // int[][] segments = { //
    // // //
    // // { 1, 4 }, { 3, 4 }, { 5, 4 }, { 7, 4 }, // plus
    // // { 0, 4 }, { 2, 4 }, { 6, 4 }, { 8, 4 },// cross
    // // { 1, 3 }, { 3, 7 }, { 7, 5 }, { 5, 1 }, // diamond
    // // { 0, 1 }, { 1, 2 }, { 2, 5 }, { 5, 8 }, { 8, 7 }, { 7, 6 }, { 6, 3 }, { 3, 0 } // big square
    // // };
    // //
    // // boolean[] pixel = new boolean[9];
    // // for (int i = 0; i < 9; i++) {
    // // pixel[i] = pixelIn.getNeighbour(i).isEdge();// && !pixelIn.getNeighbour(i).isInChain();
    // // }
    // //
    // // for (int i = 0; i < segments.length; i++) {
    // // if (pixel[segments[i][0]] && pixel[segments[i][1]]) {
    // // LineApproximation line = new LineApproximation(pixelIn.getNeighbour(segments[i][0]).getUHVWPoint(), //
    // // pixelIn.getNeighbour(segments[i][1]).getUHVWPoint());
    // // if (line.isCloserThan(pictureResolutionPoint, mShortLineThickness.getValue())) {
    // // return mShortLineColor.getValue();
    // // }
    // // }
    // // }
    // // }
    // //
    // return pColor;
    // }

    private double getShortLineThickness() {
        return mTransformSource.getShortLineThickness();
    }

    // public boolean getShowPixels() {
    // return mTransformSource.getShowPixels();
    // }

    private boolean getShowShadow() {
        return mTransformSource.getShowShadow();
    }

    public double getThickWidth() {
        return getLongLineThickness() / 1000d;
    }

    public double getThinWidth() {
        return getShortLineThickness() / 1000d;
    }

    public IPixelMapTransformSource getTransformSource() {
        return mTransformSource;
    }

    private Point getUHVWHalfPixel() {
        return mUHVWHalfPixel;
    }

    private byte getValue(final int pX, final int pY) {
        // TODO change these to Framework checks
        if (pX < 0) {
            throw new IllegalArgumentException("pX must be > 0.");
        }

        if (pX > getWidth() - 1) {
            throw new IllegalArgumentException("pX must be less than getWidth() -1. pX = " + pX + ", getWidth() = " + getWidth());
        }

        if (pY < 0) {
            throw new IllegalArgumentException("pY must be > 0.");
        }

        if (pY > getHeight() - 1) {
            throw new IllegalArgumentException("pX must be less than getHeight() -1. pX = " + pX + ", getHeight() = " + getHeight());
        }

        return mData.get(pX, pY);
    }

    public int getWidth() {
        return mWidth;
    }

    void index(final PixelChain pPixelChain, final ISegment pSegment) {
        mSegmentCount++;
        // // TODO make assumption that this is 360
        // // mSegmentIndex.add(pLineSegment);
        //
        int minX = (int) Math.floor(pSegment.getMinX(pPixelChain) * getWidth() / mAspectRatio) - 1;
        minX = minX < 0 ? 0 : minX;
        minX = minX > getWidth() - 1 ? getWidth() - 1 : minX;

        int maxX = (int) Math.ceil(pSegment.getMaxX(pPixelChain) * getWidth() / mAspectRatio) + 1;
        maxX = maxX > getWidth() - 1 ? getWidth() - 1 : maxX;

        int minY = (int) Math.floor(pSegment.getMinY(pPixelChain) * getHeight()) - 1;
        minY = minY < 0 ? 0 : minY;
        minY = minY > getHeight() - 1 ? getHeight() - 1 : minY;

        int maxY = (int) Math.ceil(pSegment.getMaxY(pPixelChain) * getHeight()) + 1;
        maxY = maxY > getHeight() - 1 ? getHeight() - 1 : maxY;

        new Range2D(minX, maxX, minY, maxY).forEach((x, y) -> {
            final Pixel pixel = getPixelAt(x, y);
            final Point centre = pixel.getUHVWPoint().add(getUHVWHalfPixel());
            if (pSegment.closerThan(pPixelChain, centre, getUHVWHalfPixel().length())) {
                getSegments(x, y).add(new Tuple2<PixelChain, ISegment>(pPixelChain, pSegment));
            }
        });

        mSegmentToPixelChainMap.put(pSegment, pPixelChain);

    }

    public Optional<PixelChain> getPixelChainForSegment(ISegment pSegment) {
        return Optional.ofNullable(mSegmentToPixelChainMap.get(pSegment));
    }

    private synchronized void indexSegments() {
        Framework.logEntry(mLogger);
        mSegmentIndexValid = false;
        Framework.logExit(mLogger);
    }

    private boolean isAnyLineCloserThan(final Point pPoint, final double pThinWidth, final double pNormalWidth, final double pThickWidth, double pMultiplier, final boolean pThickOnly) {
        calcSegmentIndex();

        final double maxWidth = KMath.max(pThinWidth, pNormalWidth, pThickWidth) * pMultiplier;
        final Point uhvw = toUHVW(pPoint);

        // to prevent the expensive closerThanActual being run against the same segment more than once they
        // are condensed into a set.
        HashSet<Tuple2<PixelChain, ISegment>> candidateSegments = new HashSet<>();

        for (int x = (int) Math.floor((uhvw.getX() - maxWidth) * getWidth() / mAspectRatio) - 1; x <= Math.ceil((uhvw.getX() + maxWidth) * getWidth() / mAspectRatio) + 1; x++) {
            for (int y = (int) (Math.floor(uhvw.getY() * getHeight()) - maxWidth) - 1; y <= Math.ceil(uhvw.getY() * getHeight() + maxWidth) + 1; y++) {
                if (0 <= x && x < getWidth() && 0 <= y && y < getHeight()) {
                    for (final Tuple2<PixelChain, ISegment> tuple : getSegments(x, y)) {
                        if (tuple._1().getThickness() == PixelChain.Thickness.None) {
                            break;
                        }
                        if (pThickOnly && tuple._1().getThickness() != PixelChain.Thickness.Thick) {
                            break;
                        }
                        candidateSegments.add(tuple);
                    }
                }
            }
        }

        StrongReference<Boolean> result = new StrongReference<>(false);
        candidateSegments.stream()
                .filter(tuple -> tuple._2().closerThanActual(tuple._1(), uhvw, pMultiplier))
                .findFirst()
                .ifPresent(tuple -> result.set(true));
        return result.get();
    }

    @Override
    public boolean isPersistent() {
        // TODO Auto-generated method stub
        return false;
    }

    private int modWidth(final int pX) {
        if (0 <= pX && pX < mWidth) {
            return pX;
        }
        if (m360) {
            if (pX < 0) {
                return modWidth(pX + mWidth);
            }
            return modWidth(pX - mWidth);
        } else {
            if (pX < 0) {
                return 0;
            }
            return mWidth - 1;
        }
    }

    public Point pixelToPoint(final Pixel pPixel) {
        return new Point((double) pPixel.getX() / (double) getWidth(), (double) pPixel.getY() / (double) getHeight());
    }

    private void reportProgress(final IProgressObserver pProgressObserver, String pProgressString, int pPercent) {
        if (pProgressObserver != null) pProgressObserver.setProgress(pProgressString, pPercent);
    }

    public void reapproximateAllChains() {
        mPixelChains.stream().parallel().forEach(PixelChain::approximate);
    }

    public void process(final IProgressObserver pProgressObserver) {
        try {
            mAutoTrackChanges = false;
            // // pProgress.showProgressBar();
            process01_reset(pProgressObserver);
            process02_thin(pProgressObserver);
            process03_generateNodes(pProgressObserver);
            process04b_removeBristles(pProgressObserver);  // the side effect of this is to convert Gemini's into Lone Nodes so it is now run first
            process04a_removeLoneNodes(pProgressObserver);
            process05_generateChains(pProgressObserver);
            process06_straightLinesRefineCorners(pProgressObserver, mTransformSource.getLineTolerance() / mTransformSource.getHeight());
            validate();
            mLogger.info(() -> "validate done");
            process07_mergeChains(pProgressObserver);
            mLogger.info(() -> "process07_mergeChains done");
            validate();
            mLogger.info(() -> "validate done");
            process08_refine(pProgressObserver);
            mLogger.info(() -> "process08_refine done");
            validate();
            mLogger.info(() -> "validate done");
            // // reapproximate(null, mTransformSource.getLineTolerance());
            validate();
            mLogger.info(() -> "validate done");
            //process04a_removeLoneNodes();
            indexSegments();
            mLogger.info(() -> "indexSegments done");
            printCount();
            //
        } catch (final Exception pEx) {
            mLogger.info(() -> "pEx");
            pEx.printStackTrace(System.err);
        } finally {
            // pProgress.hideProgressBar();
            mAutoTrackChanges = true;
        }
    }

    //
    // resets everything but the isEdgeData
    void process01_reset(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Resetting ...", 0);
        resetVisited();
        resetInChain();
        resetNode();

        // mAllNodes.removeAllElements();
        // mPixelChains.removeAllElements();
        // resetSegmentIndex();
    }

    // chains need to have been thinned
    // TODO need to work out how to have a progress bar
    void process02_thin(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Thinning ...", 0);
        forEachPixel(this::thin);
    }

    void process03_generateNodes(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Nodes ...", 0);
        forEachPixel(pixel -> {
            if (pixel.calcIsNode()) {
                mAllNodes.add(pixel);
            }
        });
    }

    public void setVisited(final Pixel pPixel, final boolean pValue) {
        setData(pPixel, pValue, VISITED);
    }

    public void setInChain(final Pixel pPixel, final boolean pValue) {
        setData(pPixel, pValue, IN_CHAIN);
    }


    public void setEdge(final Pixel pPixel, final boolean pValue) {
        if (pPixel.isEdge() == pValue) return; // ignore no change

        if (pPixel.isNode() && !pValue) {
            setNode(pPixel, false);
        }

        setData(pPixel, pValue, EDGE);
        calcIsNode(pPixel);
        pPixel.getNeighbours().forEach(p -> {
            thin(p);
            calcIsNode(p);
        });
        thin(pPixel);

        if (mAutoTrackChanges) {
            if (!pValue) { // turning pixel off
                getPixelChains(pPixel).forEach(pc -> {
                    mPixelChains.remove(pc);
                    pc.setInChain(false);
                    pc.setVisited(false);
                    pc.streamPixels()
                            .filter(Pixel::isNode)//pixel -> pixel.isNode())
                            .forEach(pixel -> pixel.getNode()
                                    .ifPresent(node -> {
                                        final Collection<PixelChain> chains = generateChains(node);
                                        addPixelChains(chains);
                                        chains.parallelStream()
                                                .forEach(PixelChain::approximate);
                                    })

                            );
                });
            } else { // turning pixel on
                Set<Node> nodes = new HashSet<>();
                resetInChain();
                resetVisited();
                pPixel.getNode().ifPresent(nodes::add);
                pPixel.getNeighbours()
                        .forEach(pixel -> {
                            getPixelChains(pixel)
                                    .forEach(pc -> {
                                        nodes.add(pc.getStartNode());
                                        nodes.add(pc.getEndNode());
                                        removePixelChain(pc);
                                    });
                            pixel.getNode().ifPresent(nodes::add); // this is the case where is is not in a chain
                        });
                nodes.stream().filter(Node::isNode).forEach(n -> {
                    final Collection<PixelChain> chains = generateChains(n);
                    addPixelChains(chains);
                    chains.parallelStream()
                            .forEach(PixelChain::approximate);
                });
            }
        }
    }

    private void setNode(final Pixel pPixel, final boolean pValue) {
        if (pPixel.isNode() && !pValue) {
            mAllNodes.remove(pPixel);
        }
        if (!pPixel.isNode() && pValue) {
            mAllNodes.add(pPixel);
        }
        setData(pPixel, pValue, NODE);
    }

    public boolean calcIsNode(Pixel pPixel) {
        boolean shouldBeNode = false;
        if (pPixel.isEdge()) {
            // here we use transitions to eliminate double counting connected neighbours
            // also note the the number of transitions is twice the number of neighbours
            final int transitionCount = pPixel.countEdgeNeighboursTransitions();
            if (transitionCount != 4) {
                shouldBeNode = true;
            }
        }

        setNode(pPixel, shouldBeNode);
        return shouldBeNode;
    }


    void process04a_removeLoneNodes(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Removing Lone Nodes ...", 0);
        forEachPixel(pixel -> {
            if (pixel.isNode()) {
                final Node node = getNode(pixel);
                if (node.countEdgeNeighbours() == 0) {
                    pixel.setEdge(false);
                    setNode(pixel, false);
                    pixel.setVisited(false);
                }
            }
        });

    }

    void process04b_removeBristles(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Removing Bristles ...", 0);
        final HashSet<Pixel> toBeRemoved = new HashSet<>();

        mAllNodes.forEach(node -> node.getNodeNeighbours().forEach(other -> {
                              final Set<Pixel> nodeSet = node.allEdgeNeighbours();
                              final Set<Pixel> otherSet = other.allEdgeNeighbours();

                              nodeSet.remove(other);
                              nodeSet.removeAll(otherSet);

                              otherSet.remove(node);
                              otherSet.removeAll(nodeSet);

                              if (nodeSet.isEmpty() && !toBeRemoved.contains(other)) {
                                  // TODO should be a better check here to see whether it is better to remove the other node
                                  toBeRemoved.add(node);
                              }
                          })
        );

        mAllNodes.removeAll(toBeRemoved);
        toBeRemoved
                .forEach(pixel -> {
                    pixel.setEdge(false);
                    pixel.allEdgeNeighbours()
                            .forEach(Pixel::calcIsNode);
                });
    }

    Vector<PixelChain> process05_generateChains(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Chains ...", 0);
        try {

            mAllNodes.forEach(node -> mPixelChains.addAll(generateChains(node)));

            // this captures all the simple loops (i.e. connected sets of pixels with no nodes).
//            for (final Pixel pixel : allPixels()) { // COMMENTED OUT AS REPLICATED BELOW WITH FOR EACH PIXEL
//                if (pixel.isUnVisitedEdge()) {
//                    final Node node = mAllNodes.add(pixel);
//                    mPixelChains.addAll(generateChains(node));
//                }
//            }
            forEachPixel(pixel -> {
                if (pixel.isUnVisitedEdge()) {
                    final Node node = mAllNodes.add(pixel);
                    mPixelChains.addAll(generateChains(node));
                }
            });

            //mLogger.info(() -> "Number of chains: " + mPixelChains.size());
        } finally {
//            if (pProgress != null) {
//                pProgress.hideProgressBar();
//            }
        }
        return mPixelChains;
    }

    private void process06_straightLinesRefineCorners(final IProgressObserver pProgressObserver, final double pMaxiLineTolerance) {
        reportProgress(pProgressObserver, "Generating Straight Lines ...", 0);
        mLogger.info(() -> "process06_straightLinesRefineCorners " + pMaxiLineTolerance);

        mPixelChains.forEach(pixelChain -> {
            pixelChain.approximate01_straightLines(pMaxiLineTolerance);
            pixelChain.approximate02_refineCorners();
        });
        mLogger.info(() -> "process06_straightLinesRefineCorners - done");
        indexSegments();
    }

    private void process07_mergeChains(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Merging Chains ...", 0);
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
        //for (final Node node : getAllNodes()) {
        mAllNodes.stream().forEach(Node::mergePixelChains);
        mSegmentCount = 0;

        indexSegments();
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
    }

    private void process08_refine(final IProgressObserver pProgressObserver) {
        Counter counter = new Counter(mPixelChains.size());
        reportProgress(pProgressObserver, "Refining ...", 0);
        mPixelChains.forEach(pc -> {
            pc.approximate();
            counter.increase();
            reportProgress(pProgressObserver, "Refining ...", counter.getPercentInt());
        });
    }


    @Override
    public boolean canRead(final IPersistDB pDB, final String pId) {
        String pixelString = pDB.read(pId + ".data");
        return pixelString != null && pixelString.length() != 0;
    }

    @Override
    public void read(final IPersistDB pDB, final String pId) {
        // TODO the width and height should come from the PixelMap ... or it should thrown an error if they are different
        // note that write/read does not preserve the mAllNodes values
        Framework.logEntry(mLogger);
        mAutoTrackChanges = true;
        try {
            // pixel data
            {
                String pixelString = pDB.read(pId + ".data");
                byte[] pixelBytes = MyBase64.decodeAndDecompress(pixelString);

                ByteArrayInputStream bais = new ByteArrayInputStream(pixelBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                for (int x = 0; x < getWidth(); x++) {
                    final byte[] buff = new byte[getHeight()];
                    int cnt = 0;
                    while ((cnt += ois.read(buff, cnt, getHeight() - cnt)) < getHeight()) {
                    }
                    for (int y = 0; y < mHeight; y++) {
                        mData = mData.set(x, y, buff[y]);
                    }
                }

                bais = null;
                ois = null;
                pixelString = null;
                pixelBytes = null;

                int cnt = 0;
                for (int x = 0; x < getWidth(); x++) {
                    for (int y = 0; y < getHeight(); y++) {
                        if (getValue(x, y) != 0) {
                            cnt++;
                        }
                    }
                }
                mLogger.info("mData cnt = " + cnt);
            }

            // mPixelChains
            {
                String objectString = pDB.read(pId + ".objects");
                byte[] objectBytes = MyBase64.decodeAndDecompress(objectString);

                ByteArrayInputStream bais = new ByteArrayInputStream(objectBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);

                final Vector<PixelChain> pixelChains = (Vector<PixelChain>) ois.readObject();
                mPixelChains.removeAllElements();
                mPixelChains.addAll(pixelChains);
                mPixelChains.parallelStream().forEach(pc -> pc.setPixelMap(this));

                mAllNodes.removeAllElements();
                forEachPixel(p -> {
                    if (p.isNode()) mAllNodes.add(p);
                });

                bais = null;
                ois = null;
                objectString = null;
                objectBytes = null;

                mLogger.info("mAllNodes size() = " + mAllNodes.size());
                mLogger.info("mPixelChains size() = " + mPixelChains.size());

                for (final PixelChain chain : mPixelChains) {
                    chain.indexSegments();
                }

                mLogger.info("mSegmentCount = " + mSegmentCount);
                calcSegmentIndex();

            }

        } catch (final Exception pEx) {
            mLogger.log(Level.SEVERE, "PixelMap.read()", pEx);
        }
        Framework.logExit(mLogger);
    }

    synchronized void removePixelChain(final PixelChain pPixelChain) {
        mPixelChains.remove(pPixelChain);
        indexSegments();
    }

    private void resetInChain() {
        forEach((x, y) -> {
            final byte newValue = (byte) (getValue(x, y) & (ALL ^ IN_CHAIN));
            setValue(x, y, newValue);
        });
    }

    private void resetNode() {
        forEach((x, y) -> {
            final byte newValue = (byte) (getValue(x, y) & (ALL ^ NODE));
            setValue(x, y, newValue);
        });
    }


    private void resetSegmentIndex() {
        mSegmentIndex = new LinkedList[getWidth()][getHeight()];
        mSegmentToPixelChainMap = new HashMap<>();
        mSegmentCount = 0;
    }

    private void resetVisited() {
        forEach((x, y) -> {
            final byte newValue = (byte) (getValue(x, y) & (ALL ^ VISITED));
            setValue(x, y, newValue);
        });
    }

    private void setData(final Pixel pPixel, final boolean pState, final byte pValue) {
        if (0 <= pPixel.getY() && pPixel.getY() < getHeight()) {
            final int x = modWidth(pPixel.getX());
            byte newValue = (byte) (getValue(x, pPixel.getY()) & (ALL ^ pValue));
            if (pState) {
                newValue |= pValue;
            }
            setValue(x, pPixel.getY(), newValue);
        }
    }

    private void setHeight(final int pHeight) {
        mHeight = pHeight;
    }

    public void setPixelChainDefaultThickness(final CannyEdgeTransform pTransform) {
        Framework.logEntry(mLogger);
        final int shortLength = pTransform.getShortLineLength();
        final int mediumLength = pTransform.getMediumLineLength();
        final int longLength = pTransform.getLongLineLength();

        mPixelChains.forEach(chain -> chain.setThickness(shortLength, mediumLength, longLength));
        Framework.logExit(mLogger);
    }

    private void setValue(final int pX, final int pY, final byte pValue) {
        // all the parameter bounds checking is doing in the saveValueNoUndo.
        // mLogger.info(() -> "setValue: " + pX + ", " + pY + ", " + pValue);

        final byte before = getValue(pX, pY);
        if (before != pValue) {
            Services.getServices().getUndoRedoBuffer()
                    .add("Pixel::setValue",
                         () -> setValueNoUndo(pX, pY, before),
                         () -> setValueNoUndo(pX, pY, pValue)
                    );
        }
        setValueNoUndo(pX, pY, pValue);
    }

    /**
     * Sets the value of a pixel in the PixelMap BUT does not generate the Undo/Redo information. This is specifically intended to
     * be called from the UndoRedo mechanism. All normal usage should be to the setValue(...) method.
     *
     * @param pX     the x
     * @param pY     the y
     * @param pValue the value
     */
    void setValueNoUndo(final int pX, final int pY, final byte pValue) {
        // TODO check these value using Framework
        if (pX < 0) {
            throw new IllegalArgumentException("pX must be > 0.");
        }

        if (pX > getWidth() - 1) {
            throw new IllegalArgumentException("pX must be less than getWidth() -1. pX = " + pX + ", getWidth() = " + getWidth());
        }

        if (pY < 0) {
            throw new IllegalArgumentException("pY must be > 0.");
        }

        if (pY > getHeight() - 1) {
            throw new IllegalArgumentException("pX must be less than getHeight() -1. pX = " + pX + ", getHeight() = " + getHeight());
        }

        mData = mData.set(pX, pY, pValue);
    }

    private void setWidth(final int pWidth) {
        mWidth = pWidth;
    }

    /**
     * Thin checks whether a Pixel should be removed in order to make the absolute single Pixel wide lines that are needed. If the
     * Pixel should not be an edge this method 1) does a setEdge(false) on the Pixel, and 2) returns true. Otherwise it returns
     * false.
     *
     * @param pPixel the pixel
     * @return true, if the Pixel was thinned.
     */
    private boolean thin(final Pixel pPixel) {
        if (!pPixel.isEdge()) {
            return false;
        }

        boolean canEliminate = false;
        for (final int[] set : eliminate) {
            canEliminate |= pPixel.getNeighbour(set[0]).isEdge() && pPixel.getNeighbour(set[1]).isEdge() &&
                    !pPixel.getNeighbour(set[2]).isEdge();
        }

        if (canEliminate) {
            pPixel.setEdge(false);
        }
        return canEliminate;
    }


    private Point toUHVW(final Point pIn) {
        return pIn.scaleX(mAspectRatio).minus(getUHVWHalfPixel());  // TODO this should line up with the Pixel get UHVW point
    }

    public void transform(final ITransformResult pRenderResult) {
        // public Color transform(final Point pIn, final Color pColor) {
        Point pIn = pRenderResult.getPoint();
        Color color = transformGetPixelColor(pIn, pRenderResult.getColor());
        color = transformGetLineColor(pIn, color, false);
        color = getMaxiLineShadowColor(pIn, color);

        pRenderResult.setColor(color);
    }

    private void validate() {
        mLogger.info(() -> "Number of chains: " + mPixelChains.size());
        mPixelChains.stream().parallel().forEach(pc -> pc.validate("PixelMap::validate"));
    }


    private void printCount() {
        class Counter {
            private int straight = 0;
            int curve = 0;
            private int doubleCurve = 0;
            int other = 0;

            private void incOther() {
                other++;
            }

            void print() {
                mLogger.info(() -> String.format("straight %d, curve %d, doubleCurve %d, other %d\n", straight, curve, doubleCurve, other));
            }
        }

        final Counter counter = new Counter();

        mPixelChains.forEach(pc -> pc.getAllSegments().forEach(s -> {
                                 if (s instanceof StraightSegment) {
                                 } else if (s instanceof CurveSegment) {
                                 } else if (s instanceof DoubleCurveSegment) {
                                 } else counter.incOther();
                             })
        );

        counter.print();
    }

    @Override
    public void write(final IPersistDB pDB, final String pId) throws IOException {
        // note that write/read does not preserve the mAllNodes values
        Framework.logEntry(mLogger);
        // from http://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;

        // mData
        {
            mLogger.finest("About to write mData");
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            for (int x = 0; x < getWidth(); x++) {
                final byte[] buff = new byte[getHeight()];
                for (int y = 0; y < mHeight; y++) {
                    buff[y] = mData.get(x, y);
                }
                oos.write(buff);
            }
            oos.close();

            String pixelString = MyBase64.compressAndEncode(baos.toByteArray());
            pDB.write(pId + ".data", pixelString);
            pixelString = null;
        }

        // mAllNodes & mPixelChains
        mLogger.info("mAllNodes size() = " + mAllNodes.size());
        mLogger.info("mPixelChains size() = " + mPixelChains.size());
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(mPixelChains);
        oos.close();

        String objectString = MyBase64.compressAndEncode(baos.toByteArray());
        pDB.write(pId + ".objects", objectString);
        objectString = null;
        mLogger.info("mSegmentCount = " + mSegmentCount);

        Framework.logExit(mLogger);
    }

    private void forEach(BiConsumer<Integer, Integer> pFunction) {
        new Range2D(getWidth(), getHeight()).forEach(pFunction);
    }

    private void forEachPixel(Consumer<Pixel> pFunction) {
        new Range2D(getWidth(), getHeight()).forEach((x, y) -> pFunction.accept(getPixelAt(x, y)));
    }

    public void forEachPixelChain(Consumer<PixelChain> pFunction) {
        mPixelChains.forEach(pFunction);
    }

}
