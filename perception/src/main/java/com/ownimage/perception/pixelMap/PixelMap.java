
/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Counter;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.PegCounter;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.Immutable2DArray;
import com.ownimage.framework.util.immutable.ImmutableMap;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelService;
import io.vavr.Tuple2;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
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
public class PixelMap implements Serializable, PixelConstants, com.ownimage.perception.pixelMap.immutable.PixelMapData {
    private final static Logger mLogger = Framework.getLogger();
    private final static long serialVersionUID = 1L;
    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    private static PixelMapService pixelMapService;
    private static PixelChainService pixelChainService;
    private static PixelService pixelService;

    static {
        com.ownimage.perception.pixelMap.services.Services defaultServices = com.ownimage.perception.pixelMap.services.Services.getDefaultServices();
        pixelMapService = defaultServices.getPixelMapService();
        pixelChainService = defaultServices.getPixelChainService();
        pixelService = defaultServices.getPixelService();
    }

    @Getter
    private final boolean m360;
    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     */
    private final double mAspectRatio;
    private final Point mUHVWHalfPixel;
    private IPixelMapTransformSource mTransformSource;
    private int mWidth;
    private int mHeight;
    private int mVersion = 0;
    @Getter
    private ImmutableMap2D<Byte> mData;
    private HashMap<IntegerPoint, Node> mNodes = new HashMap<>();
    @Getter
    private ImmutableSet<PixelChain> mPixelChains = new ImmutableSet<>();
    @Getter
    private Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> mSegmentIndex;

    private int mSegmentCount;

    /**
     * Means that the PixelMap will add/remove/reapproximate PixelChains as nodes are added and removed.
     * This is turned off whilst the bulk processing is running. // TODO should this extend to the conversion of Pixels to Nodes etc.
     */
    @Getter
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
        //mAutoTrackChanges = true;
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
        mSegmentIndex = pFrom.mSegmentIndex;
        mUHVWHalfPixel = pFrom.mUHVWHalfPixel;
    }

    @Deprecated
    public ImmutableMap<IntegerPoint, Node> getImmutableNodeMap() {
        return new ImmutableMap(mNodes);
    }

    @Override
    public int segmentCount() {
        return mSegmentCount;
    }

    public ImmutableSet<PixelChain> getPixelChains() {
        return mPixelChains;
    }

    // TODO MUTATOR CHANGED ACCESS
    // Moved tp service
    public void pixelChainsAddAll(Collection<PixelChain> pAll) {
        pAll.forEach(this::pixelChainsAdd);
    }

    // moved to Service
    private void pixelChainsAdd(PixelChain pChain) {
        val chain = pixelChainService.indexSegments(this, pChain, true);
        mPixelChains = mPixelChains.add(chain);
    }

    private void pixelChainsRemove(PixelChain pChain) {
        mPixelChains = mPixelChains.remove(pChain);
        pixelChainService.indexSegments(this, pChain, false);
    }

    // TODO MUTATOR CHANGED ACCESS
    public void pixelChainsClear() {
        mPixelChains = mPixelChains.clear();
        mSegmentIndex = mSegmentIndex.clear();
    }

    public int getPixelChainCount() {
        return mPixelChains.size();
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
                        clone.pixelChainsAdd(pixelChainService.withThickness(pc, newThickness));
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
            val pc2 = pixelChainService.approximateCurvesOnly(this, pc, tolerance, lineCurvePreference);
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
                                .filter(p -> pPixel.getUHVWMidPoint(clone.getHeight()).distance(p.getUHVWMidPoint(clone.getHeight())) < radius)
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

    @Deprecated // already migrated to PixelMapService
    public List<PixelChain> getPixelChains(@NonNull Pixel pPixel) {
        Framework.logEntry(mLogger);
        List<PixelChain> pixelChains = mPixelChains.stream()
                .filter(pc -> pixelChainService.contains(pc, pPixel))
                .collect(Collectors.toList());
        Framework.logExit(mLogger);
        return pixelChains;
    }



    private PixelChain generateChain(PixelMap pPixelMap, Node pStartNode, Pixel pCurrentPixel, PixelChain pPixelChain) {
        try {
            Framework.logEntry(mLogger);
            if (mLogger.isLoggable(Level.FINEST)) {
                mLogger.finest("pStartNode: " + pStartNode);
                mLogger.finest("pCurrentPixel: " + pCurrentPixel);
                mLogger.finest("pPixelChain: " + pPixelChain);
            }
            Optional<Node> node = getNode(pCurrentPixel);
            if (node.isPresent()) {
                PixelChain copy = pixelChainService.setEndNode(this, pPixelChain, node.get());
                Framework.logExit(mLogger);
                return copy;
            }

            if (pPixelChain.getPixels().lastElement().orElseThrow() == pCurrentPixel) {
                mLogger.severe("SHOULD NOT BE ADDING THE SAME PIXEL LASTPIXEL");
            }

            if (pPixelChain.getPixels().contains(pCurrentPixel)) {
                mLogger.severe("SHOULD NOT BE ADDING A PIXEL THAT IT ALREADY CONTAINS");
            }

            PixelChain copy = pixelChainService.add(pPixelChain, pCurrentPixel);
            pCurrentPixel.setInChain(this, true);
            pCurrentPixel.setVisited(this, true);
            // try to end quickly at a node to prevent bypassing
            for (Pixel nodalNeighbour : pCurrentPixel.getNodeNeighbours(this)) {
                // !neighbour.isNeighbour(pChain.firstElement() means you can only go back to a node if you are not IMMEDIATELY
                // going back to the staring node.
                // if ((nodalNeighbour.isUnVisitedEdge() || nodalNeighbour.isNode()) && (pChain.count() != 2 ||
                // !nodalNeighbour.isNeighbour(pChain.firstPixel()))) {
                if ((nodalNeighbour.isUnVisitedEdge(this) || nodalNeighbour.isNode(this)) && !(copy.getPixelCount() == 2 &&
                        nodalNeighbour.samePosition(pixelChainService.firstPixel(copy)))) {
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
                if ((neighbour.isUnVisitedEdge(this) || neighbour.isNode(this))
                        && !(copy.getPixelCount() == 2 && pixelChainService.getStartNode(pPixelMap, copy).isPresent()
                        && neighbour.samePosition(pixelChainService.getStartNode(pPixelMap, copy).get()))) {
                    copy = generateChain(pPixelMap, pStartNode, neighbour, copy);
                    Framework.logExit(mLogger);
                    return copy;
                }
            }
            return copy;
        } catch (StackOverflowError soe) {
            mLogger.severe("Stack Overflow Error");
            throw new RuntimeException("oops");
        }
    }

    private Collection<PixelChain> generateChains(PixelMap pPixelMap, Node pStartNode) {
        Vector<PixelChain> chains = new Vector<>();
        pStartNode.setVisited(this, true);
        pStartNode.setInChain(this, true);
        pStartNode.getNeighbours().forEach(neighbour -> {
            if (neighbour.isNode(this) || neighbour.isEdge(this) && !neighbour.isVisited(this)) {
                PixelChain chain = new PixelChain(pPixelMap, pStartNode);
                chain = generateChain(pPixelMap, pStartNode, neighbour, chain);
                if (pixelChainService.pixelLength(chain) > 2) {
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

    @Deprecated // already moved to PixelMapService
    private Optional<Pixel> getOptionalPixelAt(double pX, double pY) {
        return Optional.ofNullable(getPixelAt(pX, pY));
    }

    @Deprecated // already moved to PixelMapService
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

    @Deprecated // already moved to PixelMapService
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

    @Deprecated // already moved to PixelMapService
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




    public Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> getSegmentIndex() {
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

    public byte getValue(int pX, int pY) {
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

    public void index(PixelChain pPixelChain, ISegment pSegment, boolean pAdd) {
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
            Point centre = pixel.getUHVWMidPoint(this.getHeight());
            if (pSegment.closerThan(this, pPixelChain, centre, getUHVWHalfPixel().length())) {
                val segments = new HashSet();
                getSegments(i.getX(), i.getY()).map(ImmutableSet::toCollection).ifPresent(segments::addAll);
                if (pAdd) {
                    segments.add(new Tuple2<>(pPixelChain, pSegment));
                } else {
                    segments.remove(new Tuple2<>(pPixelChain, pSegment));
                    System.out.println("########################### PixelMap  remove " + i);
                }
                mSegmentIndex = mSegmentIndex.set(i.getX(), i.getY(), new ImmutableSet<Tuple2<PixelChain, ISegment>>().addAll(segments));
            }
        });
    }


    @Deprecated // already moved to PixelMapService
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
                .map(pc -> pixelChainService.approximate(this, pc, tolerance))
                .map(pc -> pixelChainService.refine(this, pc, tolerance, lineCurvePreference))
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
                .map(pc -> pixelChainService.refine(this, pc, tolerance, lineCurvePreference))
                //.map(pc -> pc.indexSegments(this, true))
                .forEach(updates::add);
        clone.pixelChainsClear();
        clone.pixelChainsAddAll(updates);
        return clone;
    }

    public PixelMap actionProcess(IProgressObserver pProgressObserver) {
        try {
            SplitTimer.split("PixelMap actionProcess() start");
            mAutoTrackChanges = false;
            // // pProgress.showProgressBar();
            process01_reset(pProgressObserver);
            mLogger.info("############## reset done");
            process02_thin(pProgressObserver);
            mLogger.info("############## thin done");
            process03_generateNodes(pProgressObserver);
            mLogger.info("############## generateNodes done");

            process04b_removeBristles(pProgressObserver);  // the side effect of this is to convert Gemini's into Lone Nodes so it is now run first
            mLogger.info("############## removeBristles done");

            process04a_removeLoneNodes(pProgressObserver);
            mLogger.info("############## removeLoneNodes done");

            process05_generateChains(pProgressObserver);
            mLogger.info("############## generateChains done");

            process05a_findLoops(pProgressObserver);
            mLogger.info("############## findLoops done");

            var pegs = new Object[]{
                    IPixelChain.PegCounters.RefineCornersAttempted,
                    IPixelChain.PegCounters.RefineCornersSuccessful
            };
            getPegCounter().clear(pegs);
            process06_straightLinesRefineCorners(pProgressObserver, mTransformSource.getLineTolerance() / mTransformSource.getHeight());
            mLogger.info("############## straightLinesRefineCorners done");

            mLogger.info(getPegCounter().getString(pegs));
            //validate();
            mLogger.info("############## validate done");
            process07_mergeChains(pProgressObserver);
            mLogger.info("############## process07_mergeChains done");
            //validate();
            mLogger.info("############## validate done");
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
            mLogger.info("############## process08_refine done");
            //validate();
            mLogger.info("############## validate done");
            // // reapproximate(null, mTransformSource.getLineTolerance());
            //validate();
            mLogger.info("############## validate done");
            //process04a_removeLoneNodes();
            indexSegments();
            mLogger.info("############## indesSegments done");
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
        return this;
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
                                    pixelChainService.getStartNode(this, pc).ifPresent(nodes::add);
                                    pixelChainService.getEndNode(this, pc).ifPresent(nodes::add);
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
                .map(pc -> pixelChainService.approximate(this, pc, tolerance))
                .map(pc -> pixelChainService.approximateCurvesOnly(this, pc, tolerance, lineCurvePreference));
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
            pc.getPixels().stream().forEach(p -> {
                this.setInChain(p, false);
                this.setVisited(p, false);
            });
            pc.streamPixels()
                    .filter(pPixel1 -> pPixel1.isNode(this))
                    .forEach(chainPixel -> chainPixel.getNode(this)
                            .ifPresent(node -> {
                                List<PixelChain> chains = generateChains(this, node)
                                        .parallelStream()
                                        .map(pc2 -> pixelChainService.approximate(this, pc2, tolerance))
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
        mPixelChains.forEach(pixelChain -> refined.add(pixelChainService.approximate(this, pixelChain, tolerance)));
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
                PixelChain refinedPC = pixelChainService.approximateCurvesOnly(this, pc, tolerance, lineCurvePreference);
                refined.add(refinedPC);
                counter.increase();
                reportProgress(pProgressObserver, "Refining ...", counter.getPercentInt());
            });
            pixelChainsClear();
            pixelChainsAddAll(refined);
        }
    }

    private PegCounter getPegCounter() {
        return Services.getServices().getPegCounter();
    }


    public int nodeCount() {
        return mNodes.size();
    }

    private void nodesRemoveAll() {
        mNodes.clear();
    }

    public synchronized void indexSegments() {
//        var pixelChains = new ArrayList<PixelChain>();
//        mPixelChains.stream().forEach(pc -> pixelChains.add(pixelChainService.indexSegments(this, pc, true)));
//        pixelChainsClear();
//        pixelChainsAddAll(pixelChains);
//        val count = new AtomicInteger();
//        pixelChains.stream().parallel()
//                .flatMap(PixelChain::streamSegments)
//                .filter(s -> s instanceof StraightSegment)
//                .forEach(s -> count.incrementAndGet());
//        mLogger.info(() -> "Number of straight segments = " + count.get());
        mPixelChains.forEach(pc -> pc.getSegments().forEach(seg -> index(pc, seg, true)));
    }

    /**
     * The removes a pixelChain from the PixelMap.  It also removes it from the Nodes that it was attached to.
     * This is different from deletePixelChain which can cause the nodes that it was attached to to be merged.
     *
     * @param pPixelChain
     */
    synchronized void removePixelChain(PixelChain pPixelChain) {
        pixelChainsRemove(pPixelChain);
        pixelChainService.getStartNode(this, pPixelChain).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
        pixelChainService.getEndNode(this, pPixelChain).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
    }

    synchronized void addPixelChain(PixelChain pPixelChain) {
        pixelChainsAdd(pPixelChain);
        replaceNode(pixelChainService.getStartNode(this, pPixelChain).get().addPixelChain(pPixelChain));
        replaceNode(pixelChainService.getEndNode(this, pPixelChain).get().addPixelChain(pPixelChain));
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


    public int getDataSize() {
        return mData.getSize();
    }

    public PixelMap withData(@NotNull ImmutableMap2D<Byte> data) {
        if (data.width() != width() || data.height() != height()) {
            var msg = String.format("PixelMap wxh = %sx%s, data wxh = %sx%s",
                    width(), height(), data.width(), data.height());
            throw new IllegalArgumentException(msg);
        }

        var clone = new PixelMap(this);
        clone.mData = data;
        return clone;
    }

    @Override
    public int width() {
        return mWidth;
    }

    @Override
    public int height() {
        return mHeight;
    }

    public PixelMap withNodes(HashMap<IntegerPoint, Node> nodes) {
        var clone = new PixelMap(this);
        clone.mNodes = nodes;
        return clone;
    }

    public PixelMap withNodes(ImmutableMap<IntegerPoint, Node> nodes) {
        var clone = new PixelMap(this);
        clone.mNodes = nodes.toHashMap();
        return clone;
    }

    public PixelMap withPixelChains(Collection<PixelChain> pixelChains) {
        var clone = new PixelMap(this);
        clone.mPixelChains = new ImmutableSet<PixelChain>().addAll(pixelChains);
        return clone;
    }

    public PixelMap withSegmentIndex(Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> segmentIndex) {
        var clone = new PixelMap(this);
        clone.mSegmentIndex = segmentIndex;
        return clone;
    }

    public PixelMap withSegmentCount(int segmentCount) {
        var clone = new PixelMap(this);
        clone.mSegmentCount = segmentCount;
        return clone;
    }

    @Override
    public ImmutableMap<IntegerPoint, Node> nodes() {
        mLogger.severe("############################################################");
        mLogger.severe("PixelMap.nodes() ... this is a very poorly performing method");
        mLogger.severe("############################################################");
        return new ImmutableMap(mNodes);
    }

    @Override
    public ImmutableSet<PixelChain> pixelChains() {
        return mPixelChains;
    }

    @Override
    public Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> segmentIndex() {
        return mSegmentIndex;
    }

    @Override
    public boolean autoTrackChanges() {
        return mAutoTrackChanges;
    }

    public PixelMap withAutoTrackChanges(boolean autoTrackChanges) {
        var clone = new PixelMap(this);
        clone.mAutoTrackChanges = autoTrackChanges;
        return clone;
    }

    public PixelMap withTransformSource(IPixelMapTransformSource source) {
        var clone = new PixelMap(this);
        clone.mTransformSource = source;
        return clone;
    }

    @Override
    public ImmutableMap2D<Byte> data() {
        return mData;
    }
}
