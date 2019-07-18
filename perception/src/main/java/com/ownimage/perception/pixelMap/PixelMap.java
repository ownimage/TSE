
/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Counter;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.KColor;
import com.ownimage.framework.util.MyBase64;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.segment.CurveSegment;
import com.ownimage.perception.pixelMap.segment.DoubleCurveSegment;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.transform.CannyEdgeTransform;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;
    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    private int mWidth;
    private int mHeight;
    private int mVersion = 0;
    // TODO should delete the following two values
    private final boolean m360;
    private final IPixelMapTransformSource mTransformSource;
    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     */
    private final double mAspectRatio;
    private final Point mUHVWHalfPixel;
    private ImmutableMap2D<Byte> mData;
    private HashMap<IntegerPoint, Node> mNodes = new HashMap<>();
    private ImmutableSet<PixelChain> mPixelChains = new ImmutableSet<>();
    private LinkedList<Tuple2<PixelChain, ISegment>>[][] mSegmentIndex;
    private Map<ISegment, PixelChain> mSegmentToPixelChainMap = new HashMap<>();
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
        mData = new ImmutableMap2D<>(pWidth, pHeight, (byte) 0);
        // resetSegmentIndex();
        mUHVWHalfPixel = new Point(0.5d * mAspectRatio / pWidth, 0.5d / pHeight);
    }

    public PixelMap(PixelMap pFrom) {
        mVersion = pFrom.mVersion + 1;
        setWidth(pFrom.getWidth());
        setHeight(pFrom.getHeight());
        m360 = pFrom.m360;
        mTransformSource = pFrom.mTransformSource;
        mAutoTrackChanges = pFrom.mAutoTrackChanges;
        mAspectRatio = pFrom.mAspectRatio;
        mSegmentCount = pFrom.mSegmentCount;
        mData = pFrom.mData;
        mNodes = (HashMap<IntegerPoint, Node>) pFrom.mNodes.clone(); // TODO
        mPixelChains = pFrom.mPixelChains;
        mUHVWHalfPixel = pFrom.mUHVWHalfPixel;
        invalidateSegmentIndex();
    }

    public int getPixelChainCount() {
        return mPixelChains.size();
    }

    public PixelMap actionDeletePixelChain(Pixel pPixel, int pCursorSize) {
        PixelMap clone = new PixelMap(this);
        clone.mAutoTrackChanges = false;
        StrongReference<Boolean> changesMade = new StrongReference<>(false);
        final double radius = (double) pCursorSize / getHeight();
        new Range2D(pPixel.getX() - pCursorSize, pPixel.getX() + pCursorSize, pPixel.getY() - pCursorSize, pPixel.getY() + pCursorSize)
                .forEach((x, y) ->
                        clone.getOptionalPixelAt(x, y)
                                .filter(pPixel1 -> pPixel1.isEdge(clone))
                                .filter(p -> pPixel.getUHVWMidPoint(clone).distance(p.getUHVWMidPoint(clone)) < radius)
                                .ifPresent(p -> {
                                    clone.getPixelChains(p).forEach(pc -> {
                                        pc.delete(clone);
                                        pc.getStartNode(clone).ifPresent(n -> clone.mNodes.remove(n));
                                        pc.getEndNode(clone).ifPresent(n -> clone.mNodes.remove(n));
                                        clone.mPixelChains = clone.mPixelChains.remove(pc);
                                        clone.invalidateSegmentIndex();
                                        changesMade.set(true);
                                    });
                                })
                );
        clone.mAutoTrackChanges = true;
        return changesMade.get() ? clone : this;
    }

    public String toString() {
        return "PixelMap{mVersion=" + mVersion + "}";
    }

    public PixelMap actionPixelOff(Pixel pPixel, int pCursorSize) {
        PixelMap clone = new PixelMap(this);
        StrongReference<Boolean> changesMade = new StrongReference<>(false);
        final double radius = (double) pCursorSize / getHeight();
        new Range2D(pPixel.getX() - pCursorSize, pPixel.getX() + pCursorSize, pPixel.getY() - pCursorSize, pPixel.getY() + pCursorSize)
                .forEach((x, y) ->
                        clone.getOptionalPixelAt(x, y)
                                .filter(pPixel1 -> pPixel1.isEdge(clone))
                                .filter(p -> pPixel.getUHVWMidPoint(clone).distance(p.getUHVWMidPoint(clone)) < radius)
                                .ifPresent(p -> {
                                    p.setEdge(clone, false);
                                    changesMade.set(true);
                                })
                );
        return changesMade.get() ? clone : this;
    }

    public PixelMap actionPixelOn(List<Pixel> pPixels) {
        PixelMap clone = new PixelMap(this);
        clone.mAutoTrackChanges = false;
        pPixels.forEach(pixel -> pixel.setEdge(clone, true));
        clone.mAutoTrackChanges = true;
        clone.trackPixelOn(pPixels);
        return clone;
    }

    public PixelMap actionPixelOn(Pixel pPixel) {
        if (pPixel.isEdge(this)) return this; // short circuit return
        PixelMap clone = new PixelMap(this);
        pPixel.setEdge(clone, true);
        return clone;
    }

    public PixelMap actionPixelToggle(Pixel pPixel) {
        PixelMap clone = new PixelMap(this);
        pPixel.setEdge(clone, !pPixel.isEdge(this));
        return clone;
    }

    public PixelMap actionSetPixelChainThickness(final Pixel pPixel, final PixelChain.Thickness pThickness) {
        PixelMap clone = new PixelMap(this);
        clone.getPixelChains(pPixel).forEach(pc -> {
            clone.mPixelChains = clone.mPixelChains.remove(pc);
            clone.mPixelChains = clone.mPixelChains.add(pc.setThickness(pThickness));
            clone.invalidateSegmentIndex();
        });
        return clone;
    }

    private boolean getShowPixels() {
        return mTransformSource.getShowPixels();
    }

    private Color getPixelColor() {
        return mTransformSource.getPixelColor();
    }

    private void addPixelChains(final Collection<PixelChain> pPixelChains) {
        Framework.logEntry(mLogger);
        invalidateSegmentIndex();
        pPixelChains.stream().forEach(pc -> addPixelChain(pc));
        Framework.logExit(mLogger);
    }

    public List<PixelChain> getPixelChains(final Pixel pPixel) {
        Framework.checkParameterNotNull(mLogger, pPixel, "pPixel");
        Framework.logEntry(mLogger);
        final List<PixelChain> pixelChains = mPixelChains.stream()
                .filter(pc -> pc.contains(pPixel)).collect(Collectors.toList());
        Framework.logExit(mLogger);
        return pixelChains;
    }

    protected synchronized void calcSegmentIndex() {
        Framework.logEntry(mLogger);
        if (!isSegmentIndexValid()) {
            indexSegments();
        }
        Framework.logExit(mLogger);
    }

    public void actionEqualizeValues(final EqualizeValues pValues) {
        // TODO do not like this mutable parameter
        StrongReference<Integer> totalLength = new StrongReference<>(0);
        mPixelChains.forEach(chain -> totalLength.set(totalLength.get() + chain.getPixelLength()));
        final Vector<PixelChain> sortedChains = getPixelChainsSortedByLength();
        final int shortThreshold = (int) (totalLength.get() * pValues.getIgnoreFraction());
        final int mediumThreshold = (int) (totalLength.get() * (pValues.getIgnoreFraction() + pValues.getShortFraction()));
        final int longThreshold = (int) (totalLength.get() * (pValues.getIgnoreFraction() + pValues.getShortFraction() +
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

    private PixelChain generateChain(final PixelMap pPixelMap, final Node pStartNode, final Pixel pCurrentPixel, final PixelChain pPixelChain) {
        Framework.logEntry(mLogger);
        if (mLogger.isLoggable(Level.FINEST)) {
            mLogger.finest("pStartNode: " + pStartNode);
            mLogger.finest("pCurrentPixel: " + pCurrentPixel);
            mLogger.finest("pPixelChain: " + pPixelChain);
        }
        Optional<Node> node = getNode(pCurrentPixel);
        if (node.isPresent()) {
            PixelChain copy = pPixelChain.setEndNode(this, node.get());
            Framework.logExit(mLogger);
            return copy;
        }
        PixelChain copy = pPixelChain.add(pCurrentPixel);
        pCurrentPixel.setInChain(this, true);
        pCurrentPixel.setVisited(this, true);
        // try to end quickly at a node to prevent bypassing
        for (final Pixel nodalNeighbour : pCurrentPixel.getNodeNeighbours(this)) {
            // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
            // going back to the staring node.
            // if ((nodalNeighbour.isUnVisitedEdge() || nodalNeighbour.isNode()) && (pChain.count() != 2 ||
            // !nodalNeighbour.isNeighbour(pChain.firstPixel()))) {
            if ((nodalNeighbour.isUnVisitedEdge(this) || nodalNeighbour.isNode(this)) && !(copy.count() == 2 &&
                    nodalNeighbour.samePosition(copy.firstPixel()))) {
                copy = generateChain(pPixelMap, pStartNode, nodalNeighbour, copy);
                Framework.logExit(mLogger);
                return copy;
            }
        }
        // otherwise go to the next pixel normally
        for (final Pixel neighbour : pCurrentPixel.getNeighbours()) {
            // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
            // going back to the staring node.
            // if ((neighbour.isUnVisitedEdge() || neighbour.isNode()) && (pChain.count() != 2 ||
            // !neighbour.isNeighbour(pChain.firstPixel()))) {
            if ((neighbour.isUnVisitedEdge(this) || neighbour.isNode(this)) && !(copy.count() == 2 && copy.getStartNode(pPixelMap).isPresent() && neighbour.samePosition(copy.getStartNode(pPixelMap).get()))) {
                copy = generateChain(pPixelMap, pStartNode, neighbour, copy);
                Framework.logExit(mLogger);
                return copy;
            }
        }
        return copy;
    }

    private Collection<PixelChain> generateChains(final PixelMap pPixelMap, final Node pStartNode) {
        final Vector<PixelChain> chains = new Vector<>();
        pStartNode.setVisited(this, true);
        pStartNode.setInChain(this, true);
        pStartNode.getNeighbours().forEach(neighbour -> {
            if (neighbour.isNode(this) || neighbour.isEdge(this) && !neighbour.isVisited(this)) {
                PixelChain chain = new PixelChain(pStartNode);
                chain = generateChain(pPixelMap, pStartNode, neighbour, chain);
                if (chain.length() > 2) {
                    chains.add(chain);
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

    public double getLineCurvePreference() {
        return mTransformSource.getLineCurvePreference();
    }

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
     * Gets the Node at the PixelPosition if it is a Node either because it is in mNodes or because mData says that it is a Node.
     *
     * @param pIntegerPoint the pixel
     * @return the node
     */
    public Optional<Node> getNode(final IntegerPoint pIntegerPoint) {
        // this is because pIntegerPoint might be a Node or Pixel
        IntegerPoint point = getTrueIntegerPoint(pIntegerPoint);
        Node node = mNodes.get(point);
        if (node != null) {
            return Optional.of(node);
        }
        if (new Pixel(point).isNode(this)) {
            node = new Node(point);
            mNodes.put(point, node);
            return Optional.of(node);
        }
        return Optional.empty();
    }

    private IntegerPoint getTrueIntegerPoint(IntegerPoint pIntegerPoint) {
        // this is because pIntegerPoint might be a Node or Pixel
        return pIntegerPoint.getClass() == IntegerPoint.class ? pIntegerPoint : new IntegerPoint(pIntegerPoint.getX(), pIntegerPoint.getY());
    }

    /*
     * Adds a node at the position specified, will throw a RuntimeException if the node already exists.  @See getNode(IntegerPoint).
     * @param pIntegerPoint the pixel
     */
    private Node nodeAdd(final IntegerPoint pIntegerPoint) {
        Node node = mNodes.get(pIntegerPoint);
        if (node != null) {
            return node;            // throw new RuntimeException(String.format("Trying to add node that already exists, nodesCount=%s", nodeCount()));
        }
        node = new Node(pIntegerPoint);
        mNodes.put(pIntegerPoint, node);
        return node;
    }

    private void nodeRemove(final IntegerPoint pIntegerPoint) {
        Node node = mNodes.get(pIntegerPoint);
        if (node == null) {
            //  throw new RuntimeException("Node to be removed does not already exist");
        }
        mNodes.remove(getTrueIntegerPoint(pIntegerPoint));
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
        return new Pixel(pX, pY);
    }

    public Optional<Pixel> getOptionalPixelAt(final int pX, final int pY) {
        if (0 > pY || pY >= getHeight()) return Optional.empty();
        if (!m360 && (0 > pX || pX >= getWidth())) return Optional.empty();
        final int x = modWidth(pX);
        return Optional.of(new Pixel(x, pY));
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
        final Vector<PixelChain> chains = new Vector<>(mPixelChains.toCollection()); // this will be the sorted collection
        Collections.sort(chains, (pChain1, pChain2) -> pChain1.getPixelLength() - pChain2.getPixelLength());
        return chains;
    }

    private Color transformGetPixelColor(final Point pIn, final Color pColor) {
        Color result = pColor;
        if (getShowPixels()) {
            final Optional<Pixel> pixel = getOptionalPixelAt(pIn);
            if (pixel.isPresent() && pixel.get().isEdge(this)) {
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

    private LinkedList<Tuple2<PixelChain, ISegment>>[][] getSegmentIndex() {
        calcSegmentIndex();
        return mSegmentIndex;
    }

    private Map<ISegment, PixelChain> getSegmentToPixelChainMap() {
        calcSegmentIndex();
        return mSegmentToPixelChainMap;
    }

    private AbstractCollection<Tuple2<PixelChain, ISegment>> getSegments(final int pX, final int pY) {
        Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
        Framework.checkParameterLessThan(mLogger, pX, getWidth(), "pX");
        Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
        Framework.checkParameterLessThan(mLogger, pY, getHeight(), "pY");
        final LinkedList<Tuple2<PixelChain, ISegment>>[][] segmentIndex = getSegmentIndex();
        if (segmentIndex[pX][pY] == null) segmentIndex[pX][pY] = new LinkedList<>();
        return segmentIndex[pX][pY];
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

    IPixelMapTransformSource getTransformSource() {
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

    public int getSegmentCount() {
        return mSegmentCount;
    }

    void index(final PixelChain pPixelChain, final ISegment pSegment) {
        mSegmentCount++;
        // // TODO make assumption that this is 360
        // // mSegmentIndex.add(pLineSegment);
        //
        int minX = (int) Math.floor(pSegment.getMinX(this, pPixelChain) * getWidth() / mAspectRatio) - 1;
        minX = minX < 0 ? 0 : minX;
        minX = minX > getWidth() - 1 ? getWidth() - 1 : minX;
        int maxX = (int) Math.ceil(pSegment.getMaxX(this, pPixelChain) * getWidth() / mAspectRatio) + 1;
        maxX = maxX > getWidth() - 1 ? getWidth() - 1 : maxX;
        int minY = (int) Math.floor(pSegment.getMinY(this, pPixelChain) * getHeight()) - 1;
        minY = minY < 0 ? 0 : minY;
        minY = minY > getHeight() - 1 ? getHeight() - 1 : minY;
        int maxY = (int) Math.ceil(pSegment.getMaxY(this, pPixelChain) * getHeight()) + 1;
        maxY = maxY > getHeight() - 1 ? getHeight() - 1 : maxY;
        new Range2D(minX, maxX, minY, maxY).forEach((x, y) -> {
            final Pixel pixel = getPixelAt(x, y);
            final Point centre = pixel.getUHVWMidPoint(this).add(getUHVWHalfPixel());
            if (pSegment.closerThan(this, pPixelChain, centre, getUHVWHalfPixel().length())) {
                getSegments(x, y).add(new Tuple2<>(pPixelChain, pSegment));
            }
        });
        mSegmentToPixelChainMap.put(pSegment, pPixelChain);
    }

    public Optional<PixelChain> getPixelChainForSegment(final ISegment pSegment) {
        return Optional.ofNullable(getSegmentToPixelChainMap().get(pSegment));
    }

    private boolean isSegmentIndexValid() {
        return mSegmentCount != 0;
    }

    private synchronized void invalidateSegmentIndex() {
        Framework.logEntry(mLogger);
        resetSegmentIndex();
        Framework.logExit(mLogger);
    }

    private boolean isAnyLineCloserThan(final Point pPoint, final double pThinWidth, final double pNormalWidth, final double pThickWidth, final double pMultiplier, final boolean pThickOnly) {
        calcSegmentIndex();
        final double maxThickness = KMath.max(pThinWidth, pNormalWidth, pThickWidth) * pMultiplier;
        final Point uhvw = toUHVW(pPoint);
        // to prevent the expensive closerThanActual being run against the same segment more than once they
        // are condensed into a set.
        final HashSet<Tuple2<PixelChain, ISegment>> candidateSegments = new HashSet<>();
        for (int x = (int) Math.floor((uhvw.getX() - maxThickness) * getWidth() / mAspectRatio) - 1; x <= Math.ceil((uhvw.getX() + maxThickness) * getWidth() / mAspectRatio) + 1; x++) {
            for (int y = (int) (Math.floor((uhvw.getY() - maxThickness) * getHeight())) - 1; y <= Math.ceil((uhvw.getY() + maxThickness) * getHeight()) + 1; y++) {
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
        final StrongReference<Boolean> result = new StrongReference<>(false);
        candidateSegments.stream()
                .filter(tuple -> tuple._2().closerThanActual(this, tuple._1(), mTransformSource, uhvw, pMultiplier))
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

    private void reportProgress(final IProgressObserver pProgressObserver, final String pProgressString, final int pPercent) {
        if (pProgressObserver != null) pProgressObserver.setProgress(pProgressString, pPercent);
    }

    public void actionReapproximate() {
        final double tolerance = getLineTolerance() / getHeight();
        final double lineCurvePreference = getLineCurvePreference();
        Vector<PixelChain> updates = new Vector<>();
        mPixelChains.stream()
                .parallel()
                .forEach(pc -> updates.add(pc.approximate(this, getTransformSource())));
        mPixelChains = mPixelChains.clear().addAll(updates);
    }

    public void actionProcess(final IProgressObserver pProgressObserver) {
        try {
            mAutoTrackChanges = false;
            // // pProgress.showProgressBar();
            process01_reset(pProgressObserver);
            process02_thin(pProgressObserver);
            process03_generateNodes(pProgressObserver);
            process04b_removeBristles(pProgressObserver);  // the side effect of this is to convert Gemini's into Lone Nodes so it is now run first
            process04a_removeLoneNodes(pProgressObserver);
            process05_generateChains(pProgressObserver);
            process05a_findLoops(pProgressObserver);
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
            invalidateSegmentIndex();
            mLogger.info(() -> "invalidateSegmentIndex done");
            printCount();
            calcSegmentIndex();
            validate();
            //
        } catch (final Exception pEx) {
            mLogger.info(() -> "pEx");
            Framework.logThrowable(mLogger, Level.INFO, pEx);
        } finally {
            // pProgress.hideProgressBar();
            mAutoTrackChanges = true;
        }
    }

    private void process05a_findLoops(final IProgressObserver pProgressObserver) {
        forEachPixel(pixel -> {
            if (pixel.isEdge(this) && !pixel.isInChain(this)) {
                setNode(pixel, true);
                getNode(pixel).ifPresent(node -> mPixelChains = mPixelChains.addAll(generateChains(this, node)));
            }
        });
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
            if (pixel.calcIsNode(this)) {
                nodeAdd(pixel);
            }
        });
    }

    void setVisited(final Pixel pPixel, final boolean pValue) {
        setData(pPixel, pValue, VISITED);
    }

    void setInChain(final Pixel pPixel, final boolean pValue) {
        setData(pPixel, pValue, IN_CHAIN);
    }

    void setEdge(@NotNull final Pixel pPixel, final boolean pValue) {
        if (pPixel.isEdge(this) == pValue) return; // ignore no change
        if (pPixel.isNode(this) && !pValue) {
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
                trackPixelOff(pPixel);
            } else { // turning pixel on
                trackPixelOn(pPixel);
            }
        }
    }

    void setEdge(@NotNull final List<Pixel> pPixels, final boolean pValue) {
        pPixels.forEach(pixel -> {
            if (pixel.isEdge(this) == pValue) return; // ignore no change
            if (pixel.isNode(this) && !pValue) {
                setNode(pixel, false);
            }
            setData(pixel, pValue, EDGE);
            calcIsNode(pixel);
            pixel.getNeighbours().forEach(p -> {
                thin(p);
                calcIsNode(p);
            });
            thin(pixel);
        });
        if (mAutoTrackChanges) {
            if (!pValue) { // turning pixel off
                trackPixelOff(pPixels);
            } else { // turning pixel on
                trackPixelOn(pPixels);
            }
        }
    }

    private void trackPixelOn(@NotNull List<Pixel> pPixels) {
        if (pPixels.isEmpty()) return;

        resetInChain();
        resetVisited();

        final var nodes = new HashSet<Node>();
        pPixels.forEach(pixel -> {
            pixel.getNode(this).ifPresent(nodes::add);
            pixel.getNeighbours()
                    .forEach(neighbour -> {
                        getPixelChains(neighbour)
                                .forEach(pc -> {
                                    pc.getStartNode(this).ifPresent(nodes::add);
                                    pc.getEndNode(this).ifPresent(nodes::add);
                                    removePixelChain(pc);
                                });
                        neighbour.getNode(this).ifPresent(nodes::add); // this is the case where is is not in a chain
                    });
        });

        nodes.stream()
                .flatMap(this::generateChainsAndApproximate)
                .map(chain -> chain.indexSegments(this))
                .forEach(this::addPixelChain);

        // if there is a loop then this ensures that it is closed and converted to pixel chain
        pPixels.stream()
                .filter(p -> p.isEdge(this))
                .findFirst()
                .filter(p -> !p.isNode(this))
                .filter(p -> getPixelChains(p).isEmpty())
                .stream()
                .map(p -> setNode(p, true))
                .flatMap(p -> generateChainsAndApproximate(new Node(p)))
                .map(chain -> chain.indexSegments(this))
                .forEach(this::addPixelChain);
    }

    private Stream<PixelChain> generateChainsAndApproximate(Node pNode) {
        final double tolerance = getLineTolerance() / getHeight();
        final double lineCurvePreference = getLineCurvePreference();
        return generateChains(this, pNode)
                .parallelStream()
                .map(pc2 -> pc2.approximate(this, this.getTransformSource()));
    }

    private void trackPixelOn(@NotNull Pixel pPixel) {
        List<Pixel> pixels = Arrays.asList(pPixel);
        trackPixelOn(pixels);
    }

    private void trackPixelOff(@NotNull Pixel pPixel) {
        List<Pixel> pixels = Arrays.asList(pPixel);
        trackPixelOff(pixels);
    }

    private void trackPixelOff(@NotNull List<Pixel> pPixels) {
        pPixels.forEach(pixel -> {
            getPixelChains(pixel).forEach(pc -> {
                mPixelChains = mPixelChains.remove(pc);
                pc.setInChain(this, false);
                pc.setVisited(this, false);
                pc.streamPixels()
                        .filter(pPixel1 -> pPixel1.isNode(this))
                        .forEach(chainPixel -> chainPixel.getNode(this)
                                .ifPresent(node -> {
                                    final List<PixelChain> chains = generateChains(this, node)
                                            .parallelStream()
                                            .map(pc2 -> pc2.approximate(this, this.getTransformSource()))
                                            .collect(Collectors.toList());
                                    addPixelChains(chains);
                                })
                        );
            });
        });
    }

    private Pixel setNode(@NotNull final Pixel pPixel, final boolean pValue) {
        if (pPixel.isNode(this) && !pValue) {
            nodeRemove(pPixel);
        }
        if (!pPixel.isNode(this) && pValue) {
            nodeAdd(pPixel);
        }
        setData(pPixel, pValue, NODE);
        return pPixel;
    }

    boolean calcIsNode(final Pixel pPixel) {
        boolean shouldBeNode = false;
        if (pPixel.isEdge(this)) {
            // here we use transitions to eliminate double counting connected neighbours
            // also note the the number of transitions is twice the number of neighbours
            final int transitionCount = pPixel.countEdgeNeighboursTransitions(this);
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
            if (pixel.isNode(this)) {
                final Node node = getNode(pixel).get();
                if (node.countEdgeNeighbours(this) == 0) {
                    pixel.setEdge(this, false);
                    setNode(pixel, false);
                    pixel.setVisited(this, false);
                }
            }
        });
    }

    private Stream<Node> nodesStream() {
        // note this is to prevent concurrent modification exception
        return ((HashMap) mNodes.clone()).values().stream();
    }

    void process04b_removeBristles(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Removing Bristles ...", 0);
        final Vector<Pixel> toBeRemoved = new Vector<>();
        nodesStream().forEach(node -> node.getNodeNeighbours(this).forEach(other -> {
                    final Set<Pixel> nodeSet = node.allEdgeNeighbours(this);
                    final Set<Pixel> otherSet = other.allEdgeNeighbours(this);
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
        nodesRemoveAll(toBeRemoved);
        toBeRemoved
                .forEach(pixel -> {
                    pixel.setEdge(this, false);
                    pixel.allEdgeNeighbours(this)
                            .forEach(pPixel -> pPixel.calcIsNode(this));
                });
    }

    private void nodesRemoveAll(final Collection<Pixel> pToBeRemoved) {
        pToBeRemoved.forEach(mNodes::remove);
    }

    void process05_generateChains(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Chains ...", 0);
        nodesStream().forEach(node -> mPixelChains = mPixelChains.addAll(generateChains(this, node)));
        forEachPixel(pixel -> {
            if (pixel.isUnVisitedEdge(this)) {
                getNode(pixel).ifPresent(node -> mPixelChains = mPixelChains.addAll(generateChains(this, node)));
            }
        });
        mLogger.info(() -> "Number of chains: " + getPixelChainCount());
    }

    private void process06_straightLinesRefineCorners(final IProgressObserver pProgressObserver, final double pMaxiLineTolerance) {
        reportProgress(pProgressObserver, "Generating Straight Lines ...", 0);
        mLogger.info(() -> "process06_straightLinesRefineCorners " + pMaxiLineTolerance);
        Vector<PixelChain> refined = new Vector<>();
        mPixelChains.forEach(pixelChain -> refined.add(pixelChain.approximate(this, mTransformSource)));
        mPixelChains = mPixelChains.clear().addAll(refined);
        mLogger.info(() -> "approximate - done");
        invalidateSegmentIndex();
    }

    private void process07_mergeChains(final IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Merging Chains ...", 0);
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
        nodesStream().forEach(pNode -> pNode.mergePixelChains(this));
        mSegmentCount = 0;
        invalidateSegmentIndex();
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
    }

    private void process08_refine(final IProgressObserver pProgressObserver) {
        if (mPixelChains.size() > 0) {
            var counter = Counter.createMaxCounter(mPixelChains.size());
            reportProgress(pProgressObserver, "Refining ...", 0);
            Vector<PixelChain> refined = new Vector<>();
            mPixelChains.forEach(pc -> {
                PixelChain refinedPC = pc.approximate(this, getTransformSource());
                refined.add(refinedPC);
                counter.increase();
                reportProgress(pProgressObserver, "Refining ...", counter.getPercentInt());
            });
            mPixelChains = mPixelChains.clear().addAll(refined);
        }
    }

    @Override
    public boolean canRead(final IPersistDB pDB, final String pId) {
        final String pixelString = pDB.read(pId + ".data");
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
                final Collection<PixelChain> pixelChains = (Collection<PixelChain>) ois.readObject();
                mPixelChains = mPixelChains.clear().addAll(pixelChains);
                //TODO this will need to change
                bais = null;
                ois = null;
                objectString = null;
                objectBytes = null;
                mLogger.info("mAllNodes size() = " + nodeCount());
                mLogger.info("mPixelChains size() = " + mPixelChains.size());
                indexSegments();
                mLogger.info("mSegmentCount = " + mSegmentCount);
                calcSegmentIndex();
            }
        } catch (final Exception pEx) {
            mLogger.log(Level.SEVERE, "PixelMap.read()", pEx);
        }
        Framework.logExit(mLogger);
    }

    private int nodeCount() {
        return mNodes.size();
    }

    private void nodesRemoveAll() {
        mNodes.clear();
    }

    private void indexSegments() {
        final Vector<PixelChain> pixelChains = new Vector<>();
        mPixelChains.forEach(pc -> pixelChains.add(pc.indexSegments(this)));
        mPixelChains = mPixelChains.clear().addAll(pixelChains);
    }

    /**
     * The removes a pixelChain from the PixelMap.  It also removes it from the Nodes that it was attached to.
     * This is different from deletePixelChain which can cause the nodes that it was attached to to be merged.
     *
     * @param pPixelChain
     */
    synchronized void removePixelChain(final PixelChain pPixelChain) {
        mPixelChains = mPixelChains.remove(pPixelChain);
        pPixelChain.getStartNode(this).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
        pPixelChain.getEndNode(this).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
        invalidateSegmentIndex();
    }

    synchronized void addPixelChain(final PixelChain pPixelChain) {
        mPixelChains = mPixelChains.add(pPixelChain);
        replaceNode(pPixelChain.getStartNode(this).get().addPixelChain(pPixelChain));
        replaceNode(pPixelChain.getEndNode(this).get().addPixelChain(pPixelChain));
        invalidateSegmentIndex();
    }

    private void replaceNode(Node pNode) {
        mNodes.put(pNode.toIntegerPoint(), pNode);
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

    // access weakened for testing only
    protected void setData_FOR_TESTING_PURPOSES_ONLY(final Pixel pPixel, final boolean pState, final byte pValue) {
        setData(pPixel, pState, pValue);
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

    public void actionSetPixelChainDefaultThickness(final CannyEdgeTransform pTransform) {
        Framework.logEntry(mLogger);
        final int shortLength = pTransform.getShortLineLength();
        final int mediumLength = pTransform.getMediumLineLength();
        final int longLength = pTransform.getLongLineLength();
        Vector<PixelChain> updates = new Vector<>();
        mPixelChains.forEach(chain -> updates.add(chain.setThickness(shortLength, mediumLength, longLength)));
        mPixelChains = mPixelChains.clear().addAll(updates);
        invalidateSegmentIndex();
        Framework.logExit(mLogger);
    }

    void setValue(final int pX, final int pY, final byte pValue) {
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
        if (!pPixel.isEdge(this)) {
            return false;
        }
        boolean canEliminate = false;
        for (final int[] set : eliminate) {
            canEliminate |= pPixel.getNeighbour(set[0]).isEdge(this) && pPixel.getNeighbour(set[1]).isEdge(this) &&
                    !pPixel.getNeighbour(set[2]).isEdge(this);
        }
        if (canEliminate) {
            pPixel.setEdge(this, false);
            nodeRemove(pPixel);
        }
        return canEliminate;
    }

    private Point toUHVW(final Point pIn) {
        return pIn.scaleX(mAspectRatio);
    }

    public void transform(final ITransformResult pRenderResult) {
        // public Color transform(final Point pIn, final Color pColor) {
        final Point pIn = pRenderResult.getPoint();
        Color color = transformGetPixelColor(pIn, pRenderResult.getColor());
        color = transformGetLineColor(pIn, color, false);
        color = getMaxiLineShadowColor(pIn, color);
        pRenderResult.setColor(color);
    }

    private void validate() {
        invalidateSegmentIndex();
        calcSegmentIndex();
        mLogger.info(() -> "Number of chains: " + mPixelChains.size());
        mPixelChains.stream().parallel().forEach(pc -> pc.validate("PixelMap::validate"));
        Set segments = new HashSet<ISegment>();
        for (int x = 0; x < mSegmentIndex.length; x++) {
            for (int y = 0; y < mSegmentIndex[x].length; y++) {
                LinkedList<Tuple2<PixelChain, ISegment>> list = mSegmentIndex[x][y];
                if (list != null) {
                    list.stream().forEach(t -> segments.add(t._2));
                }
            }
        }
        if (mSegmentCount != segments.size() || mSegmentCount != mSegmentToPixelChainMap.keySet().size()) {
            String message = String.format("mSegmentCount mismatch: mSegmentCount=%s, segments.size()=%s, mSegmentToPixelChainMap.keySet().size()=%s", mSegmentCount, segments.size(), mSegmentToPixelChainMap.keySet().size());
            throw new IllegalStateException(message);
        }
    }

    private void printCount() {
        class Counter {
            private final int straight = 0;
            private final int curve = 0;
            private final int doubleCurve = 0;
            private int other = 0;

            private void incOther() {
                other++;
            }

            private void print() {
                mLogger.info(() -> String.format("straight %d, curve %d, doubleCurve %d, other %d\n", straight, curve, doubleCurve, other));
            }
        }
        final Counter counter = new Counter();
        mPixelChains.forEach(pc -> pc.streamSegments().forEach(s -> {
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
        mLogger.info("nodeCount() = " + nodeCount());
        mLogger.info("mPixelChains size() = " + mPixelChains.size());
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        final Collection<PixelChain> pixelChains = mPixelChains.toCollection();
        oos.writeObject(pixelChains);
        oos.close();
        String objectString = MyBase64.compressAndEncode(baos.toByteArray());
        pDB.write(pId + ".objects", objectString);
        objectString = null;
        mLogger.info("mSegmentCount = " + mSegmentCount);
        Framework.logExit(mLogger);
    }

    // Move to a stream
    @Deprecated
    private void forEach(final BiConsumer<Integer, Integer> pFunction) {
        new Range2D(getWidth(), getHeight()).forEach(pFunction);
    }

    // move to a stream
    @Deprecated
    private void forEachPixel(final Consumer<Pixel> pFunction) {
        new Range2D(getWidth(), getHeight()).forEach((x, y) -> pFunction.accept(getPixelAt(x, y)));
    }

    // Move to a stream
    @Deprecated
    public void forEachPixelChain(final Consumer<PixelChain> pFunction) {
        mPixelChains.forEach(pFunction);
    }

    public Stream<PixelChain> streamPixelChains() {
        return mPixelChains.stream();
    }

    public void checkCompatibleSize(@NotNull final PixelMap pPixelMap) {
        Framework.checkParameterNotNull(mLogger, pPixelMap, "pPixelMap");
        if (getWidth() != pPixelMap.getWidth() || getHeight() != pPixelMap.getHeight()) {
            throw new IllegalArgumentException("pPixelMap is different size to this.");
        }
    }
}
