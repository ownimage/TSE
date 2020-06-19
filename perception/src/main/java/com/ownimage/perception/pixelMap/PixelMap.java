
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
import com.ownimage.framework.util.PegCounter;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.Immutable2DArray;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.transform.CannyEdgeTransform;
import io.vavr.Tuple2;
import lombok.NonNull;
import lombok.val;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
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
    private final static Logger mLogger = Framework.getLogger();
    private final static long serialVersionUID = 1L;
    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    // TODO should delete the following two values
    private final boolean m360;
    private final IPixelMapTransformSource mTransformSource;
    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     */
    private final double mAspectRatio;
    private final Point mUHVWHalfPixel;
    private int mWidth;
    private int mHeight;
    private int mVersion = 0;
    private ImmutableMap2D<Byte> mData;
    private HashMap<IntegerPoint, Node> mNodes = new HashMap<>();
    private ImmutableSet<PixelChain> mPixelChains = new ImmutableSet<>();
    private Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> mSegmentIndex;
    private int mSegmentCount;
    /**
     * Means that the PixelMap will add/remove/reapproximate PixelChains as nodes are added and removed.
     * This is turned off whilst the bulk processing is running. // TODO should this extend to the conversion of Pixels to Nodes etc.
     */
    private boolean mAutoTrackChanges = false;

    public PixelMap(int pWidth, int pHeight, boolean p360, IPixelMapTransformSource pTransformSource) {
        setWidth(pWidth);
        setHeight(pHeight);
        m360 = p360;
        mTransformSource = pTransformSource;
        clearSegmentIndex();
        // mHalfPixel = new Point(0.5d / getHeight(), 0.5d / getWidth());
        mAspectRatio = (double) pWidth / pHeight;
        mData = new ImmutableMap2D<>(pWidth, pHeight, (byte) 0);
        // resetSegmentIndex();
        mUHVWHalfPixel = new Point(0.5d * mAspectRatio / pWidth, 0.5d / pHeight);
    }

    private PixelMap(PixelMap pFrom) {
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
        mSegmentIndex = pFrom.mSegmentIndex;
        mUHVWHalfPixel = pFrom.mUHVWHalfPixel;
    }

    public ImmutableSet<PixelChain> getPixelChains() {
        return mPixelChains;
    }

    private void pixelChainsAddAll(Collection<PixelChain> pAll) {
        pAll.forEach(this::pixelChainsAdd);
    }

    private void pixelChainsAdd(PixelChain pChain) {
        val chain = pChain.indexSegments(this, true);
        mPixelChains = mPixelChains.add(chain);
    }

    private void pixelChainsRemove(PixelChain pChain) {
        mPixelChains = mPixelChains.remove(pChain);
        pChain.indexSegments(this, false);
    }

    private void pixelChainsClear() {
        mPixelChains = mPixelChains.clear();
        mSegmentIndex = mSegmentIndex.clear();
    }

    public int getPixelChainCount() {
        return mPixelChains.size();
    }

    public PixelMap actionDeletePixelChain(Collection<Pixel> pPixels) {
        PixelMap clone = new PixelMap(this);
        clone.mAutoTrackChanges = false;
        val changesMade = new StrongReference<>(false);
        pPixels.stream()
                .filter(p -> p.isEdge(clone))
                .forEach(p -> clone.getPixelChains(p).forEach(pc -> {
                    pc.delete(clone);
                    pc.getStartNode(clone).ifPresent(n -> clone.mNodes.remove(n));
                    pc.getEndNode(clone).ifPresent(n -> clone.mNodes.remove(n));
                    clone.pixelChainsRemove(pc);
                    changesMade.set(true);
                }));
        clone.mAutoTrackChanges = true;
        return changesMade.get() ? clone : this;
    }

    public PixelMap actionSetPixelChainThickness(
            @NonNull Collection<Pixel> pPixels,
            @NonNull Function<PixelChain, Thickness> pMap
    ) {
        PixelMap clone = new PixelMap(this);
        val changesMade = new StrongReference<>(false);
        pPixels.stream()
                .filter(p -> p.isEdge(clone))
                .flatMap(p -> this.getPixelChains(p).stream())
                .distinct()
                .forEach(pc -> {
                    var currentThickness = pc.getThickness();
                    var newThickness = pMap.apply(pc);
                    if (newThickness != currentThickness) {
                        clone.pixelChainsRemove(pc);
                        clone.pixelChainsAdd(pc.setThickness(newThickness));
                        changesMade.set(true);
                    }
                });
        return changesMade.get() ? clone : this;
    }

    public PixelMap actionSetPixelChainThicknessReset(
            @NonNull Collection<Pixel> pPixels,
            int shortLength,
            int mediumLength,
            int longLength
    ) {
        PixelMap clone = new PixelMap(this);
        val changesMade = new StrongReference<>(false);
        pPixels.stream()
                .filter(p -> p.isEdge(clone))
                .flatMap(p -> this.getPixelChains(p).stream())
                .distinct()
                .forEach(pc -> {
                    var update = pc.setThickness(shortLength, mediumLength, longLength);
                    if (update != pc) {
                        clone.pixelChainsRemove(pc);
                        clone.pixelChainsAdd(update);
                        changesMade.set(true);
                    }
                });
        return changesMade.get() ? clone : this;
    }

    public PixelMap actionPixelChainDeleteAllButThis(@NonNull Pixel pPixel) {
        val pixelChains = getPixelChains(pPixel);
        if (getPixelChains(pPixel).isEmpty() || pixelChains.size() != 1) {
            return this;
        }

        PixelMap clone = new PixelMap(this);
        clone.pixelChainsClear();
        clone.pixelChainsAddAll(pixelChains);
        //copy.indexSegments();
        return clone;
    }


    public PixelMap actionPixelChainApproximateCurvesOnly(Pixel pPixel) {
        if (getPixelChains(pPixel).isEmpty()) {
            return this;
        }
        double tolerance = getTransformSource().getLineTolerance() / getTransformSource().getHeight();
        double lineCurvePreference = getTransformSource().getLineCurvePreference();
        PixelMap clone = new PixelMap(this);
        clone.getPixelChains(pPixel).forEach(pc -> {
            clone.removePixelChain(pc);
            val pc2 = pc.approximateCurvesOnly(this, tolerance, lineCurvePreference);
            clone.addPixelChain(pc2);
        });
        //copy.indexSegments();
        return clone;
    }

    public String toString() {
        return "PixelMap{mVersion=" + mVersion + "}";
    }

    public PixelMap actionPixelOff(Pixel pPixel, int pCursorSize) {
        PixelMap clone = new PixelMap(this);
        StrongReference<Boolean> changesMade = new StrongReference<>(false);
        double radius = (double) pCursorSize / getHeight();
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

    public PixelMap actionPixelOn(Collection<Pixel> pPixels) {
        PixelMap clone = new PixelMap(this);
        clone.mAutoTrackChanges = false;
        pPixels.forEach(pixel -> pixel.setEdge(clone, true));
        clone.mAutoTrackChanges = true;
        clone.trackPixelOn(pPixels);
        return clone;
    }

    public PixelMap actionPixelOn(Pixel pPixel) {
        if (pPixel.isEdge(this)) {
            return this; // short circuit return
        }
        PixelMap clone = new PixelMap(this);
        pPixel.setEdge(clone, true);
        return clone;
    }

    public PixelMap actionPixelToggle(Pixel pPixel) {
        PixelMap clone = new PixelMap(this);
        pPixel.setEdge(clone, !pPixel.isEdge(this));
        return clone;
    }

    private boolean getShowPixels() {
        return mTransformSource.getShowPixels();
    }

    private Color getPixelColor() {
        return mTransformSource.getPixelColor();
    }

    private void addPixelChains(Collection<PixelChain> pPixelChains) {
        Framework.logEntry(mLogger);
        pPixelChains.forEach(this::addPixelChain);
        Framework.logExit(mLogger);
    }

    public List<PixelChain> getPixelChains(@NonNull Pixel pPixel) {
        Framework.logEntry(mLogger);
        List<PixelChain> pixelChains = mPixelChains.stream()
                .filter(pc -> pc.contains(pPixel)).collect(Collectors.toList());
        Framework.logExit(mLogger);
        return pixelChains;
    }

    public void actionEqualizeValues(EqualizeValues pValues) {
        if (mPixelChains.size() == 0) {
            return;
        }
        // TODO do not like this mutable parameter
        StrongReference<Integer> totalLength = new StrongReference<>(0);
        mPixelChains.forEach(chain -> totalLength.set(totalLength.get() + chain.getPixelCount()));
        Vector<PixelChain> sortedChains = getPixelChainsSortedByLength();
        int shortThreshold = (int) (totalLength.get() * pValues.getIgnoreFraction());
        int mediumThreshold = (int) (totalLength.get() * (pValues.getIgnoreFraction() + pValues.getShortFraction()));
        int longThreshold = (int) (totalLength.get() * (pValues.getIgnoreFraction() + pValues.getShortFraction() +
                pValues.getMediumFraction()));
        Integer shortLength = null;
        Integer mediumLength = null;
        Integer longLength = null;
        int currentLength = 0;
        for (PixelChain chain : sortedChains) {
            currentLength += chain.getPixelCount();
            if (shortLength == null && currentLength > shortThreshold) {
                shortLength = chain.getPixelCount();
            }
            if (mediumLength == null && currentLength > mediumThreshold) {
                mediumLength = chain.getPixelCount();
            }
            if (longLength == null && currentLength > longThreshold) {
                longLength = chain.getPixelCount();
                break;
            }
        }
        pValues.setReturnValues(shortLength, mediumLength, longLength);
    }

    private PixelChain generateChain(PixelMap pPixelMap, Node pStartNode, Pixel pCurrentPixel, PixelChain pPixelChain) {
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
        PixelChain copy = pPixelChain.add(pPixelMap, pCurrentPixel);
        pCurrentPixel.setInChain(this, true);
        pCurrentPixel.setVisited(this, true);
        // try to end quickly at a node to prevent bypassing
        for (Pixel nodalNeighbour : pCurrentPixel.getNodeNeighbours(this)) {
            // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
            // going back to the staring node.
            // if ((nodalNeighbour.isUnVisitedEdge() || nodalNeighbour.isNode()) && (pChain.count() != 2 ||
            // !nodalNeighbour.isNeighbour(pChain.firstPixel()))) {
            if ((nodalNeighbour.isUnVisitedEdge(this) || nodalNeighbour.isNode(this)) && !(copy.getPixelCount() == 2 &&
                    nodalNeighbour.samePosition(copy.firstPixel()))) {
                copy = generateChain(pPixelMap, pStartNode, nodalNeighbour, copy);
                Framework.logExit(mLogger);
                return copy;
            }
        }
        // otherwise go to the next pixel normally
        for (Pixel neighbour : pCurrentPixel.getNeighbours()) {
            // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
            // going back to the staring node.
            // if ((neighbour.isUnVisitedEdge() || neighbour.isNode()) && (pChain.count() != 2 ||
            // !neighbour.isNeighbour(pChain.firstPixel()))) {
            if ((neighbour.isUnVisitedEdge(this) || neighbour.isNode(this)) && !(copy.getPixelCount() == 2 && copy.getStartNode(pPixelMap).isPresent() && neighbour.samePosition(copy.getStartNode(pPixelMap).get()))) {
                copy = generateChain(pPixelMap, pStartNode, neighbour, copy);
                Framework.logExit(mLogger);
                return copy;
            }
        }
        return copy;
    }

    private Collection<PixelChain> generateChains(PixelMap pPixelMap, Node pStartNode) {
        Vector<PixelChain> chains = new Vector<>();
        pStartNode.setVisited(this, true);
        pStartNode.setInChain(this, true);
        pStartNode.getNeighbours().forEach(neighbour -> {
            if (neighbour.isNode(this) || neighbour.isEdge(this) && !neighbour.isVisited(this)) {
                PixelChain chain = new PixelChain(pPixelMap, pStartNode);
                chain = generateChain(pPixelMap, pStartNode, neighbour, chain);
                if (chain.length() > 2) {
                    chains.add(chain);
                }
            }
        });
        return chains;
    }

    boolean getData(Pixel pPixel, byte pValue) {
        if (0 <= pPixel.getY() && pPixel.getY() < getHeight()) {
            int x = modWidth(pPixel.getX());
            return (getValue(x, pPixel.getY()) & pValue) != 0;
        } else {
            return false;
        }
    }

    public int getHeight() {
        return mHeight;
    }

    private void setHeight(int pHeight) {
        mHeight = pHeight;
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

    private Color transformGetLineColor(Point pIn, Color pColor, boolean pThickOnly) {
        return getShowLines() ?
                transformGetLineColor(pIn, pColor, getMaxiLineColor(), getLineOpacity(), 1.0d, pThickOnly) :
                pColor;
    }

    private Color transformGetLineColor(Point pIn, Color pColorIn, Color pLineColor, double pOpacity,
                                        double pThicknessMultiplier, boolean pThickOnly) {
        double shortThickness = getMediumLineThickness() * pThicknessMultiplier / 1000d;
        double normalThickness = getShortLineThickness() * pThicknessMultiplier / 1000d;
        double longThickness = getLongLineThickness() * pThicknessMultiplier / 1000d;
        if (isAnyLineCloserThan(pIn, shortThickness, normalThickness, longThickness, pThicknessMultiplier, pThickOnly)) {
            return KColor.fade(pColorIn, pLineColor, pOpacity);
        }
        return pColorIn;
    }

    private Color getMaxiLineShadowColor(Point pIn, Color pColor) {
        if (getShowShadow()) {
            double x = pIn.getX() - getShadowXOffset() / 1000d;
            x = x < 0 ? 0 : x > getWidth() - 1 ? x - getWidth() : x;
            double y = pIn.getY() - getShadowYOffset() / 1000d;
            y = y < 0 ? 0 : y > getHeight() - 1 ? y - (getHeight() - 1) : y;
            Point uhvw = new Point(x, y);
            return transformGetLineColor(uhvw, pColor, getShadowColor(), getShadowOpacity(), getShadowThickness(), true);
        }
        return pColor;
    }

    private double getMediumLineThickness() {
        return mTransformSource.getMediumLineThickness();
    }

    /**
     * Gets the Node at the PixelPosition if it is a Node either because it is in mNodes or because mData says that it is a Node.
     *
     * @param pIntegerPoint the pixel
     * @return the node
     */
    public Optional<Node> getNode(IntegerPoint pIntegerPoint) {
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
    private Node nodeAdd(IntegerPoint pIntegerPoint) {
        Node node = mNodes.get(pIntegerPoint);
        if (node != null) {
            return node;            // throw new RuntimeException(String.format("Trying to add node that already exists, nodesCount=%s", nodeCount()));
        }
        node = new Node(pIntegerPoint);
        mNodes.put(pIntegerPoint, node);
        return node;
    }

    private void nodeRemove(IntegerPoint pIntegerPoint) {
        Node node = mNodes.get(pIntegerPoint);
        if (node == null) {
            //  throw new RuntimeException("Node to be removed does not already exist");
        }
        mNodes.remove(getTrueIntegerPoint(pIntegerPoint));
    }

    public double getNormalWidth() {
        return getMediumLineThickness() / 1000d;
    }

    private Optional<Pixel> getOptionalPixelAt(double pX, double pY) {
        return Optional.ofNullable(getPixelAt(pX, pY));
    }

    private Pixel getPixelAt(double pX, double pY) {
        Framework.logEntry(mLogger);
        // Framework.logParams(mLogger, "pX, pY", pX, pY);
        int x = (int) (pX * getWidth());
        int y = (int) (pY * getHeight());
        y = y == getHeight() ? getHeight() - 1 : y;
        x = modWidth(x);
        Pixel pixel = getPixelAt(x, y);
        Framework.logExit(mLogger);
        return pixel;
    }

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    public Pixel getPixelAt(int pX, int pY) {
        return new Pixel(pX, pY);
    }

    public Optional<Pixel> getOptionalPixelAt(int pX, int pY) {
        if (0 > pY || pY >= getHeight()) {
            return Optional.empty();
        }
        if (!m360 && (0 > pX || pX >= getWidth())) {
            return Optional.empty();
        }
        int x = modWidth(pX);
        return Optional.of(new Pixel(x, pY));
    }

    private Optional<Pixel> getOptionalPixelAt(Point pPoint) {
        return getOptionalPixelAt(pPoint.getX(), pPoint.getY());
    }

    public Optional<Pixel> getOptionalPixelAt(IntegerPoint pPoint) {
        return getOptionalPixelAt(pPoint.getX(), pPoint.getY());
    }

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    public Pixel getPixelAt(Point pPoint) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pPoint", pPoint);
        Pixel pixel = getPixelAt(pPoint.getX(), pPoint.getY());
        Framework.logExit(mLogger);
        return pixel;
    }

    private Vector<PixelChain> getPixelChainsSortedByLength() {
        Vector<PixelChain> chains = new Vector<>(mPixelChains.toCollection()); // this will be the sorted collection
        chains.sort(Comparator.comparingInt(IPixelChain::getPixelCount));
        return chains;
    }

    private Color transformGetPixelColor(Point pIn, Color pColor) {
        Color result = pColor;
        if (getShowPixels()) {
            Optional<Pixel> pixel = getOptionalPixelAt(pIn);
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

    private Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> getSegmentIndex() {
        return mSegmentIndex;
    }

    private Optional<ImmutableSet<Tuple2<PixelChain, ISegment>>> getSegments(int pX, int pY) {
        Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
        Framework.checkParameterLessThan(mLogger, pX, getWidth(), "pX");
        Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
        Framework.checkParameterLessThan(mLogger, pY, getHeight(), "pY");
        val segmentIndex = getSegmentIndex().get(pX, pY);
        return Optional.ofNullable(segmentIndex);
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

    private double getShortLineThickness() {
        return mTransformSource.getShortLineThickness();
    }

    private boolean getShowShadow() {
        return mTransformSource.getShowShadow();
    }

    private IPixelMapTransformSource getTransformSource() {
        return mTransformSource;
    }

    private Point getUHVWHalfPixel() {
        return mUHVWHalfPixel;
    }

    byte getValue(int pX, int pY) {
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

    private void setWidth(int pWidth) {
        mWidth = pWidth;
    }

    public int getSegmentCount() {
        return mSegmentCount;
    }

    void index(PixelChain pPixelChain, ISegment pSegment, boolean pAdd) {
        mSegmentCount++;
        // // TODO make assumption that this is 360
        // // mSegmentIndex.add(pLineSegment);
        //
        int minX = (int) Math.floor(pSegment.getMinX(this, pPixelChain) * getWidth() / mAspectRatio) - 1;
        minX = Math.max(minX, 0);
        minX = Math.min(minX, getWidth() - 1);
        int maxX = (int) Math.ceil(pSegment.getMaxX(this, pPixelChain) * getWidth() / mAspectRatio) + 1;
        maxX = Math.min(maxX, getWidth() - 1);
        int minY = (int) Math.floor(pSegment.getMinY(this, pPixelChain) * getHeight()) - 1;
        minY = Math.max(minY, 0);
        minY = Math.min(minY, getHeight() - 1);
        int maxY = (int) Math.ceil(pSegment.getMaxY(this, pPixelChain) * getHeight()) + 1;
        maxY = Math.min(maxY, getHeight() - 1);

        new Range2D(minX, maxX, minY, maxY).stream().forEach(i -> {
            Pixel pixel = getPixelAt(i.getX(), i.getY());
            Point centre = pixel.getUHVWMidPoint(this);
            if (pSegment.closerThan(this, pPixelChain, centre, getUHVWHalfPixel().length())) {
                val segments = new HashSet();
                getSegments(i.getX(), i.getY()).map(ImmutableSet::toCollection).ifPresent(segments::addAll);
                if (pAdd) {
                    segments.add(new Tuple2<>(pPixelChain, pSegment));
                } else {
                    segments.remove(new Tuple2<>(pPixelChain, pSegment));
                }
                mSegmentIndex = mSegmentIndex.set(i.getX(), i.getY(), new ImmutableSet<Tuple2<PixelChain, ISegment>>().addAll(segments));
            }
        });
    }

    private boolean isAnyLineCloserThan(Point pPoint, double pThinWidth, double pNormalWidth, double pThickWidth, double pMultiplier, boolean pThickOnly) {
        double maxThickness = KMath.max(pThinWidth, pNormalWidth, pThickWidth) * pMultiplier;
        Point uhvw = toUHVW(pPoint);
        // to prevent the expensive closerThanActual being run against the same segment more than once they
        // are condensed into a set.
        var candidateSegments = new HashSet<Tuple2<PixelChain, ISegment>>();
        for (int x = (int) Math.floor((uhvw.getX() - maxThickness) * getWidth() / mAspectRatio) - 1; x <= Math.ceil((uhvw.getX() + maxThickness) * getWidth() / mAspectRatio) + 1; x++) {
            for (int y = (int) (Math.floor((uhvw.getY() - maxThickness) * getHeight())) - 1; y <= Math.ceil((uhvw.getY() + maxThickness) * getHeight()) + 1; y++) {
                if (0 <= x && x < getWidth() && 0 <= y && y < getHeight()) {
                    getSegments(x, y).ifPresent(set -> set.stream()
                            .filter(tuple -> tuple._1().getThickness() != IPixelChain.Thickness.None)
                            .filter(tuple -> !pThickOnly || tuple._1().getThickness() == IPixelChain.Thickness.Thick)
                            .forEach(candidateSegments::add));
                }
            }
        }
        StrongReference<Boolean> result = new StrongReference<>(false);
        candidateSegments.stream()
                .filter(tuple -> tuple._2().closerThanActual(this, tuple._1(), mTransformSource, uhvw, pMultiplier))
                .findAny()
                .ifPresent(tuple -> result.set(true));
        return result.get();
    }

    @Override
    public boolean isPersistent() {
        // TODO Auto-generated method stub
        return false;
    }

    private int modWidth(int pX) {
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

    private void reportProgress(IProgressObserver pProgressObserver, String pProgressString, int pPercent) {
        if (pProgressObserver != null) {
            pProgressObserver.setProgress(pProgressString, pPercent);
        }
    }

    public PixelMap actionReapproximate() {
        SplitTimer.split("PixelMap actionReapproximate() start");
        PixelMap clone = new PixelMap(this);
        Vector<PixelChain> updates = new Vector<>();
        val tolerance = getTransformSource().getLineTolerance() / getTransformSource().getHeight();
        val lineCurvePreference = getTransformSource().getLineCurvePreference();
        clone.mPixelChains.stream()
                .parallel()
                .map(pc -> pc.approximate(this, tolerance))
                .map(pc -> pc.refine(this, tolerance, lineCurvePreference))
                //.map(pc -> pc.indexSegments(this, true))
                .forEach(updates::add);
        clone.pixelChainsClear();
        clone.pixelChainsAddAll(updates);
        SplitTimer.split("PixelMap actionReapproximate() end");
        return clone;
    }

    public PixelMap actionRerefine() {
        PixelMap clone = new PixelMap(this);
        Vector<PixelChain> updates = new Vector<>();
        val tolerance = getTransformSource().getLineTolerance() / getTransformSource().getHeight();
        val lineCurvePreference = getTransformSource().getLineCurvePreference();
        mPixelChains.stream()
                .parallel()
                .map(pc -> pc.refine(this, tolerance, lineCurvePreference))
                //.map(pc -> pc.indexSegments(this, true))
                .forEach(updates::add);
        clone.pixelChainsClear();
        clone.pixelChainsAddAll(updates);
        return clone;
    }

    public void actionProcess(IProgressObserver pProgressObserver) {
        try {
            SplitTimer.split("PixelMap actionProcess() start");
            mAutoTrackChanges = false;
            // // pProgress.showProgressBar();
            process01_reset(pProgressObserver);
            process02_thin(pProgressObserver);
            process03_generateNodes(pProgressObserver);
            process04b_removeBristles(pProgressObserver);  // the side effect of this is to convert Gemini's into Lone Nodes so it is now run first
            process04a_removeLoneNodes(pProgressObserver);
            process05_generateChains(pProgressObserver);
            process05a_findLoops(pProgressObserver);
            var pegs = new Object[]{
                    IPixelChain.PegCounters.RefineCornersAttempted,
                    IPixelChain.PegCounters.RefineCornersSuccessful
            };
            getPegCounter().clear(pegs);
            process06_straightLinesRefineCorners(pProgressObserver, mTransformSource.getLineTolerance() / mTransformSource.getHeight());
            mLogger.info(getPegCounter().getString(pegs));
            //validate();
            mLogger.info(() -> "validate done");
            process07_mergeChains(pProgressObserver);
            mLogger.info(() -> "process07_mergeChains done");
            //validate();
            mLogger.info(() -> "validate done");
            pegs = new Object[]{
                    IPixelChain.PegCounters.StartSegmentStraightToCurveAttempted,
                    IPixelChain.PegCounters.StartSegmentStraightToCurveSuccessful,
                    IPixelChain.PegCounters.MidSegmentEatForwardAttempted,
                    IPixelChain.PegCounters.MidSegmentEatForwardSuccessful,
                    IPixelChain.PegCounters.refine01FirstSegmentAttempted,
                    IPixelChain.PegCounters.refine01FirstSegmentSuccessful
            };
            getPegCounter().clear(pegs);
            process08_refine(pProgressObserver);
            mLogger.info(getPegCounter().getString(pegs));
            mLogger.info(() -> "process08_refine done");
            //validate();
            mLogger.info(() -> "validate done");
            // // reapproximate(null, mTransformSource.getLineTolerance());
            //validate();
            mLogger.info(() -> "validate done");
            //process04a_removeLoneNodes();
            validate();
            //
        } catch (Exception pEx) {
            mLogger.info(() -> "pEx");
            Framework.logThrowable(mLogger, Level.INFO, pEx);
        } finally {
            // pProgress.hideProgressBar();
            SplitTimer.split("PixelMap actionProcess() end");
            mAutoTrackChanges = true;
        }
    }

    private void process05a_findLoops(IProgressObserver pProgressObserver) {
        forEachPixel(pixel -> {
            if (pixel.isEdge(this) && !pixel.isInChain(this)) {
                setNode(pixel, true);
                getNode(pixel).ifPresent(node -> pixelChainsAddAll(generateChains(this, node)));
            }
        });
    }

    //
    // resets everything but the isEdgeData
    void process01_reset(IProgressObserver pProgressObserver) {
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
    void process02_thin(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Thinning ...", 0);
        forEachPixel(this::thin);
    }

    void process03_generateNodes(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Nodes ...", 0);
        forEachPixel(pixel -> {
            if (pixel.calcIsNode(this)) {
                nodeAdd(pixel);
            }
        });
    }

    void setVisited(Pixel pPixel, boolean pValue) {
        setData(pPixel, pValue, VISITED);
    }

    void setInChain(Pixel pPixel, boolean pValue) {
        setData(pPixel, pValue, IN_CHAIN);
    }

    void setEdge(@NonNull Pixel pPixel, boolean pValue) {
        if (pPixel.isEdge(this) == pValue) {
            return; // ignore no change
        }
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
            if (pValue) { // turning pixel on
                trackPixelOn(pPixel);
            } else { // turning pixel off
                trackPixelOff(pPixel);
            }
        }
    }

    private void trackPixelOn(@NonNull Collection<Pixel> pPixels) {
        if (pPixels.isEmpty()) {
            return;
        }

        resetInChain();
        resetVisited();

        var nodes = new HashSet<Node>();
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
                .forEach(this::addPixelChain);
    }

    private Stream<PixelChain> generateChainsAndApproximate(Node pNode) {
        double tolerance = getLineTolerance() / getHeight();
        double lineCurvePreference = getLineCurvePreference();
        return generateChains(this, pNode)
                .parallelStream()
                .map(pc -> pc.approximate(this, tolerance))
                .map(pc -> pc.approximateCurvesOnly(this, tolerance, lineCurvePreference));
    }

    private void trackPixelOn(@NonNull Pixel pPixel) {
        List<Pixel> pixels = Collections.singletonList(pPixel);
        trackPixelOn(pixels);
    }

    private void trackPixelOff(@NonNull Pixel pPixel) {
        List<Pixel> pixels = Collections.singletonList(pPixel);
        trackPixelOff(pixels);
    }

    private void trackPixelOff(@NonNull List<Pixel> pPixels) {
        double tolerance = getLineTolerance() / getHeight();
        double lineCurvePreference = getLineCurvePreference();
        pPixels.forEach(pixel -> getPixelChains(pixel).forEach(pc -> {
            pixelChainsRemove(pc);
            pc.setInChain(this, false);
            pc.setVisited(this, false);
            pc.streamPixels()
                    .filter(pPixel1 -> pPixel1.isNode(this))
                    .forEach(chainPixel -> chainPixel.getNode(this)
                            .ifPresent(node -> {
                                List<PixelChain> chains = generateChains(this, node)
                                        .parallelStream()
                                        .map(pc2 -> pc2.approximate(this, tolerance))
                                        .collect(Collectors.toList());
                                addPixelChains(chains);
                            })
                    );
        }));
    }

    private Pixel setNode(@NonNull Pixel pPixel, boolean pValue) {
        if (pPixel.isNode(this) && !pValue) {
            nodeRemove(pPixel);
        }
        if (!pPixel.isNode(this) && pValue) {
            nodeAdd(pPixel);
        }
        setData(pPixel, pValue, NODE);
        return pPixel;
    }

    boolean calcIsNode(Pixel pPixel) {
        boolean shouldBeNode = false;
        if (pPixel.isEdge(this)) {
            // here we use transitions to eliminate double counting connected neighbours
            // also note the the number of transitions is twice the number of neighbours
            int transitionCount = pPixel.countEdgeNeighboursTransitions(this);
            if (transitionCount != 4) {
                shouldBeNode = true;
            }
        }
        setNode(pPixel, shouldBeNode);
        return shouldBeNode;
    }

    void process04a_removeLoneNodes(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Removing Lone Nodes ...", 0);
        forEachPixel(pixel -> {
            if (pixel.isNode(this)) {
                Node node = getNode(pixel).get();
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

    void process04b_removeBristles(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Removing Bristles ...", 0);
        Vector<Pixel> toBeRemoved = new Vector<>();
        nodesStream().forEach(node -> node.getNodeNeighbours(this).forEach(other -> {
                    Set<Pixel> nodeSet = node.allEdgeNeighbours(this);
                    Set<Pixel> otherSet = other.allEdgeNeighbours(this);
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

    private void nodesRemoveAll(Collection<Pixel> pToBeRemoved) {
        pToBeRemoved.forEach(mNodes::remove);
    }

    void process05_generateChains(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Chains ...", 0);
        nodesStream().forEach(node -> pixelChainsAddAll(generateChains(this, node)));
        forEachPixel(pixel -> {
            if (pixel.isUnVisitedEdge(this)) {
                getNode(pixel).ifPresent(node -> pixelChainsAddAll(generateChains(this, node)));
            }
        });
        mLogger.info(() -> "Number of chains: " + getPixelChainCount());
    }

    private void process06_straightLinesRefineCorners(
            IProgressObserver pProgressObserver,
            double pMaxiLineTolerance
    ) {
        double tolerance = getLineTolerance() / getHeight();
        double lineCurvePreference = getLineCurvePreference();
        reportProgress(pProgressObserver, "Generating Straight Lines ...", 0);
        mLogger.info(() -> "process06_straightLinesRefineCorners " + pMaxiLineTolerance);
        Vector<PixelChain> refined = new Vector<>();
        mPixelChains.forEach(pixelChain -> refined.add(pixelChain.approximate(this, tolerance)));
        pixelChainsClear();
        pixelChainsAddAll(refined);
        mLogger.info("approximate - done");
    }

    private void process07_mergeChains(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Merging Chains ...", 0);
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
        nodesStream().forEach(pNode -> pNode.mergePixelChains(this));
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
    }

    private void process08_refine(IProgressObserver pProgressObserver) {
        if (mPixelChains.size() > 0) {
            var counter = Counter.createMaxCounter(mPixelChains.size());
            reportProgress(pProgressObserver, "Refining ...", 0);
            Vector<PixelChain> refined = new Vector<>();
            mPixelChains.stream().parallel().forEach(pc -> {
                //PixelChain refinedPC = pc.refine(this, getTransformSource());
                val tolerance = getTransformSource().getLineTolerance() / getTransformSource().getHeight();
                val lineCurvePreference = getTransformSource().getLineCurvePreference();
                PixelChain refinedPC = pc.approximateCurvesOnly(this, tolerance, lineCurvePreference);
                refined.add(refinedPC);
                counter.increase();
                reportProgress(pProgressObserver, "Refining ...", counter.getPercentInt());
            });
            pixelChainsClear();
            pixelChainsAddAll(refined);
        }
    }

    @Override
    public boolean canRead(IPersistDB pDB, String pId) {
        String pixelString = pDB.read(pId + ".data");
        return pixelString != null && !pixelString.isEmpty();
    }

    private PegCounter getPegCounter() {
        return Services.getServices().getPegCounter();
    }

    @Override
    public void read(IPersistDB pDB, String pId) {
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
                    byte[] buff = new byte[getHeight()];
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
                Collection<PixelChain> pixelChains = (Collection<PixelChain>) ois.readObject();
                pixelChainsClear();
                pixelChainsAddAll(pixelChains);
                //TODO this will need to change
                bais = null;
                ois = null;
                objectString = null;
                objectBytes = null;
                mLogger.info("mAllNodes size() = " + nodeCount());
                mLogger.info("mPixelChains size() = " + mPixelChains.size());
                mLogger.info("mSegmentCount = " + mSegmentCount);
            }
        } catch (Exception pEx) {
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

    public synchronized void indexSegments() {
        var pixelChains = new ArrayList<PixelChain>();
        mPixelChains.stream().forEach(pc -> pixelChains.add(pc.indexSegments(this, true)));
        pixelChainsClear();
        pixelChainsAddAll(pixelChains);
        val count = new AtomicInteger();
        pixelChains.stream().parallel()
                .flatMap(PixelChain::streamSegments)
                .filter(s -> s instanceof StraightSegment)
                .forEach(s -> count.incrementAndGet());
        mLogger.info(() -> "Number of straight segments = " + count.get());
    }

    /**
     * The removes a pixelChain from the PixelMap.  It also removes it from the Nodes that it was attached to.
     * This is different from deletePixelChain which can cause the nodes that it was attached to to be merged.
     *
     * @param pPixelChain
     */
    synchronized void removePixelChain(PixelChain pPixelChain) {
        pixelChainsRemove(pPixelChain);
        pPixelChain.getStartNode(this).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
        pPixelChain.getEndNode(this).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
    }

    synchronized void addPixelChain(PixelChain pPixelChain) {
        pixelChainsAdd(pPixelChain);
        replaceNode(pPixelChain.getStartNode(this).get().addPixelChain(pPixelChain));
        replaceNode(pPixelChain.getEndNode(this).get().addPixelChain(pPixelChain));
    }

    private void replaceNode(Node pNode) {
        mNodes.put(pNode.toIntegerPoint(), pNode);
    }

    private void resetInChain() {
        mData = mData.forEach(v -> (byte) (v & (ALL ^ IN_CHAIN)));
    }

    private void resetNode() {
        mData = mData.forEach(v -> (byte) (v & (ALL ^ NODE)));
    }

    private void clearSegmentIndex() {
        mSegmentIndex = new Immutable2DArray<>(mWidth, mHeight, 20);
        mSegmentCount = 0;
    }

    private void resetVisited() {
        mData = mData.forEach(v -> (byte) (v & (ALL ^ VISITED)));
    }

    // access weakened for testing only
    protected void setData_FOR_TESTING_PURPOSES_ONLY(Pixel pPixel, boolean pState, byte pValue) {
        setData(pPixel, pState, pValue);
    }

    private void setData(Pixel pPixel, boolean pState, byte pValue) {
        if (0 <= pPixel.getY() && pPixel.getY() < getHeight()) {
            int x = modWidth(pPixel.getX());
            byte newValue = (byte) (getValue(x, pPixel.getY()) & (ALL ^ pValue));
            if (pState) {
                newValue |= pValue;
            }
            setValue(x, pPixel.getY(), newValue);
        }
    }

    public void actionSetPixelChainDefaultThickness(CannyEdgeTransform pTransform) {
        Framework.logEntry(mLogger);
        int shortLength = pTransform.getShortLineLength();
        int mediumLength = pTransform.getMediumLineLength();
        int longLength = pTransform.getLongLineLength();
        Vector<PixelChain> updates = new Vector<>();
        mPixelChains.forEach(chain -> updates.add(chain.setThickness(shortLength, mediumLength, longLength)));
        pixelChainsClear();
        pixelChainsAddAll(updates);
        Framework.logExit(mLogger);
    }

    void setValue(int pX, int pY, byte pValue) {
        mData = mData.set(pX, pY, pValue);
    }

    /**
     * Thin checks whether a Pixel should be removed in order to make the absolute single Pixel wide lines that are needed. If the
     * Pixel should not be an edge this method 1) does a setEdge(false) on the Pixel, and 2) returns true. Otherwise it returns
     * false.
     *
     * @param pPixel the pixel
     * @return true, if the Pixel was thinned.
     */
    private boolean thin(Pixel pPixel) {
        if (!pPixel.isEdge(this)) {
            return false;
        }
        boolean canEliminate = false;
        for (int[] set : eliminate) {
            canEliminate |= pPixel.getNeighbour(set[0]).isEdge(this) && pPixel.getNeighbour(set[1]).isEdge(this) &&
                    !pPixel.getNeighbour(set[2]).isEdge(this);
        }
        if (canEliminate) {
            pPixel.setEdge(this, false);
            nodeRemove(pPixel);
        }
        return canEliminate;
    }

    private Point toUHVW(Point pIn) {
        return pIn.scaleX(mAspectRatio);
    }

    public void transform(ITransformResult pRenderResult) {
        Point pIn = pRenderResult.getPoint();
        Color color = transformGetPixelColor(pIn, pRenderResult.getColor());
        color = transformGetLineColor(pIn, color, false);
        color = getMaxiLineShadowColor(pIn, color);
        pRenderResult.setColor(color);
    }

    private void validate() {
        mLogger.info(() -> "Number of chains: " + mPixelChains.size());
//        mPixelChains.stream().parallel().forEach(pc -> pc.validate(pPixelMap, true, "PixelMap::validate"));
        Set segments = new HashSet<ISegment>();
        for (int x = 0; x < mWidth; x++) {
            for (int y = 0; y < mHeight; y++) {
                var list = mSegmentIndex.get(x, y);
                if (list != null) {
                    list.stream().forEach(t -> segments.add(t._2));
                }
            }
        }
        if (mSegmentCount != segments.size()) {
            String message = String.format("mSegmentCount mismatch: mSegmentCount=%s, segments.size()=%s", mSegmentCount, segments.size());
            throw new IllegalStateException(message);
        }
    }

    @Override
    public void write(IPersistDB pDB, String pId) throws IOException {
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
                byte[] buff = new byte[getHeight()];
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
        Collection<PixelChain> pixelChains = mPixelChains.toCollection();
        oos.writeObject(pixelChains);
        oos.close();
        String objectString = MyBase64.compressAndEncode(baos.toByteArray());
        pDB.write(pId + ".objects", objectString);
        objectString = null;
        mLogger.info("mSegmentCount = " + mSegmentCount);
        Framework.logExit(mLogger);
    }

    /**
     * @deprecated TODO: explain
     */ // move to a stream
    @Deprecated
    private void forEachPixel(Consumer<Pixel> pFunction) {
        new Range2D(getWidth(), getHeight()).forEach((x, y) -> pFunction.accept(getPixelAt(x, y)));
    }

    /**
     * @deprecated TODO: explain
     */ // Move to a stream
    @Deprecated
    public void forEachPixelChain(Consumer<PixelChain> pFunction) {
        mPixelChains.forEach(pFunction);
    }

    public Stream<PixelChain> streamPixelChains() {
        return mPixelChains.stream();
    }

    public void checkCompatibleSize(@NonNull PixelMap pPixelMap) {
        if (getWidth() != pPixelMap.getWidth() || getHeight() != pPixelMap.getHeight()) {
            throw new IllegalArgumentException("pPixelMap is different size to this.");
        }
    }

    public int getDataSize() {
        return mData.getSize();
    }

}
