
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
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapChainGenerationService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import io.vavr.Tuple2;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
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
public class PixelMap extends PixelMapBase implements Serializable, PixelConstants, PixelMapData {
    private final static Logger mLogger = Framework.getLogger();
    private final static long serialVersionUID = 1L;
    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    private static PixelMapService pixelMapService;
    private static PixelMapChainGenerationService pixelMapChainGenerationService;
    private static PixelChainService pixelChainService;

    static {
        com.ownimage.perception.pixelMap.services.Services defaultServices = com.ownimage.perception.pixelMap.services.Services.getDefaultServices();
        pixelMapService = defaultServices.getPixelMapService();
        pixelMapChainGenerationService = defaultServices.pixelMapChainGenerationService();
        pixelChainService = defaultServices.getPixelChainService();
    }

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
        mNodes = pFrom.mNodes;
        mPixelChains = pFrom.mPixelChains;
        mSegmentIndex = pFrom.mSegmentIndex;
        mUHVWHalfPixel = pFrom.mUHVWHalfPixel;
    }

    public PixelMap(
            @NotNull PixelMapData from,
            IPixelMapTransformSource transformSource) {
        mVersion = 0;
        setWidth(from.width());
        setHeight(from.height());
        m360 = from.is360();
        mTransformSource = transformSource;
        mAutoTrackChanges = from.autoTrackChanges();
        mAspectRatio = (double) mWidth / mHeight;
        mSegmentCount = from.segmentCount();
        mData = from.data();
        mNodes = from.nodes();
        mPixelChains = from.pixelChains();
        mSegmentIndex = from.segmentIndex();
        mUHVWHalfPixel = new Point(0.5d * mAspectRatio / mWidth, 0.5d / mHeight);
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

    // TODO MUTATOR CHANGED ACCESS
    public void pixelChainsClear() {
        mPixelChains = mPixelChains.clear();
        mSegmentIndex = mSegmentIndex.clear();
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

    private void addPixelChains(Collection<PixelChain> pPixelChains) {
        Framework.logEntry(mLogger);
        pPixelChains.forEach(this::addPixelChain);
        Framework.logExit(mLogger);
    }



    boolean getData(Pixel pPixel, byte pValue) {
        if (0 <= pPixel.getY() && pPixel.getY() < getHeight()) {
            int x = modWidth(pPixel.getX());
            return (getValue(x, pPixel.getY()) & pValue) != 0;
        } else {
            return false;
        }
    }



    private void setHeight(int pHeight) {
        mHeight = pHeight;
    }

    public double getLineTolerance() {
        return mTransformSource.getLineTolerance();
    }

    public double getLineCurvePreference() {
        return mTransformSource.getLineCurvePreference();
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
            mNodes = mNodes.put(point, node);
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
        mNodes = mNodes.put(pIntegerPoint, node);
        return node;
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



    public void process05a_findLoops(IProgressObserver pProgressObserver) {
        forEachPixel(pixel -> {
            if (pixel.isEdge(this) && !pixel.isInChain(this)) {
                setNode(pixel, true);
                getNode(pixel).ifPresent(node -> {
                    var result = pixelMapChainGenerationService.generateChains(this, node);
                    setValuesFrom(result._1);
                    pixelChainsAddAll(result._2);
                });
            }
        });
    }

    //
    // resets everything but the isEdgeData
    public void process01_reset(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Resetting ...", 0);
        var reset = pixelMapService.resetVisited(this);
        reset = pixelMapService.resetInChain(reset);
        reset = pixelMapService.resetNode(reset);
        setValuesFrom(reset);
    }

    // chains need to have been thinned
    // TODO need to work out how to have a progress bar
    public void process02_thin(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Thinning ...", 0);
        forEachPixel(this::thin);
    }

    public void process03_generateNodes(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Nodes ...", 0);
        forEachPixel(pixel -> {
            var calsIsNodeResult = pixelMapService.calcIsNode(this, pixel);
            setValuesFrom(calsIsNodeResult._1);
            if (calsIsNodeResult._2) {
                setValuesFrom(pixelMapService.nodeAdd(calsIsNodeResult._1, pixel));
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
        setValuesFrom(pixelMapService.calcIsNode(this, pPixel)._1);
        pPixel.getNeighbours().forEach(p -> {
            thin(p);
            setValuesFrom(pixelMapService.calcIsNode(this, p)._1);
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

    public void trackPixelOn(@NonNull Collection<Pixel> pPixels) {
        if (pPixels.isEmpty()) {
            return;
        }

        var reset = pixelMapService.resetInChain(this);
        reset = pixelMapService.resetVisited(reset);
        setValuesFrom(reset);

        var nodes = new HashSet<Node>();
        pPixels.forEach(pixel -> {
            pixel.getNode(this).ifPresent(nodes::add);
            pixel.getNeighbours()
                    .forEach(neighbour -> {
                        pixelMapService.getPixelChains(this, neighbour)
                                .forEach(pc -> {
                                    pixelChainService.getStartNode(this, pc).ifPresent(nodes::add);
                                    pixelChainService.getEndNode(this, pc).ifPresent(nodes::add);
                                    removePixelChain(pc);
                                });
                        neighbour.getNode(this).ifPresent(nodes::add); // this is the case where is is not in a chain
                    });
        });

        nodes.stream()
                .map(n -> pixelMapService.generateChainsAndApproximate(this, this.getTransformSource(), n))
                .peek(r -> setValuesFrom(r._1))
                .flatMap(r -> r._2)
                .forEach(this::addPixelChain);

        // if there is a loop then this ensures that it is closed and converted to pixel chain
        pPixels.stream()
                .filter(p -> p.isEdge(this))
                .findFirst()
                .filter(p -> !p.isNode(this))
                .filter(p -> pixelMapService.getPixelChains(this, p).isEmpty())
                .stream()
                .map(p -> setNode(p, true))
                .map(p -> pixelMapService.generateChainsAndApproximate(this, this.getTransformSource(), new Node(p)))
                .peek(r -> setValuesFrom(r._1))
                .flatMap(r -> r._2)
                .forEach(this::addPixelChain);
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
        pPixels.forEach(pixel -> pixelMapService.getPixelChains(this, pixel).forEach(pc -> {
            setValuesFrom(pixelMapService.pixelChainRemove(this, pc));
            pc.getPixels().stream().forEach(p -> {
                this.setInChain(p, false);
                this.setVisited(p, false);
            });
            pc.streamPixels()
                    .filter(pPixel1 -> pPixel1.isNode(this))
                    .forEach(chainPixel -> chainPixel.getNode(this)
                            .ifPresent(node -> {
                                var generatedChains = pixelMapChainGenerationService.generateChains(this, node);
                                setValuesFrom(generatedChains._1);
                                var chains = generatedChains._2
                                        .parallelStream()
                                        .map(pc2 -> pixelChainService.approximate(this, pc2, tolerance))
                                        .collect(Collectors.toList());
                                addPixelChains(chains);
                            })
                    );
        }));
    }

    public void setValuesFrom(ImmutablePixelMapData other) {
        if (m360 != other.is360() || mWidth != other.width() || mHeight != other.height()) {
            throw new IllegalStateException("Incompatible PixelMaps");
        }
        mData = other.data();
        mNodes = other.nodes();
        mPixelChains = other.pixelChains();
        mSegmentIndex = other.segmentIndex();
        mSegmentCount = other.segmentCount();
        mAutoTrackChanges = other.autoTrackChanges();
    }

    private Pixel setNode(@NonNull Pixel pPixel, boolean pValue) {
        if (pPixel.isNode(this) && !pValue) {
            setValuesFrom(pixelMapService.nodeRemove(this, pPixel));
        }
        if (!pPixel.isNode(this) && pValue) {
            nodeAdd(pPixel);
        }
        setData(pPixel, pValue, NODE);
        return pPixel;
    }

    public void process04a_removeLoneNodes(IProgressObserver pProgressObserver) {
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
        return mNodes.values().stream();
    }

    public void process04b_removeBristles(IProgressObserver pProgressObserver) {
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
                            .forEach(pPixel -> setValuesFrom(pixelMapService.calcIsNode(this, pPixel)._1));
                });
    }

    private void nodesRemoveAll(Collection<Pixel> pToBeRemoved) {
        var result = StrongReference.of(mNodes);
        pToBeRemoved.forEach(p -> result.update(r -> r.remove(p.toIntegerPoint())));
    }

    public void process05_generateChains(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Generating Chains ...", 0);
        nodesStream().forEach(node -> {
            val gc = pixelMapChainGenerationService.generateChains(this, node);
            setValuesFrom(gc._1);
            pixelChainsAddAll(gc._2);
        });
        forEachPixel(pixel -> {
            if (pixel.isUnVisitedEdge(this)) {
                getNode(pixel).ifPresent(node -> {
                    var gc = pixelMapChainGenerationService.generateChains(this, node);
                    setValuesFrom(gc._1);
                    pixelChainsAddAll(gc._2);
                });
            }
        });
        mLogger.info(() -> "Number of chains: " + pixelChains().size());
    }

    public void process06_straightLinesRefineCorners(
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

    public void process07_mergeChains(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Merging Chains ...", 0);
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
        nodesStream().forEach(pNode -> pNode.mergePixelChains(this));
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
    }

    public void process08_refine(IProgressObserver pProgressObserver) {
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

    public PegCounter getPegCounter() {
        return Services.getServices().getPegCounter();
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
        setValuesFrom(pixelMapService.pixelChainRemove(this, pPixelChain));
        pixelChainService.getStartNode(this, pPixelChain).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
        pixelChainService.getEndNode(this, pPixelChain).ifPresent(n -> replaceNode(n.removePixelChain(pPixelChain)));
    }

    synchronized void addPixelChain(PixelChain pPixelChain) {
        pixelChainsAdd(pPixelChain);
        replaceNode(pixelChainService.getStartNode(this, pPixelChain).get().addPixelChain(pPixelChain));
        replaceNode(pixelChainService.getEndNode(this, pPixelChain).get().addPixelChain(pPixelChain));
    }

    private void replaceNode(Node pNode) {
        mNodes = mNodes.put(pNode.toIntegerPoint(), pNode);
    }

    private void clearSegmentIndex() {
        mSegmentIndex = new Immutable2DArray<>(mWidth, mHeight, 20);
        mSegmentCount = 0;
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
            canEliminate |= pPixel.getNeighbour(set[0]).isEdge(this)
                    && pPixel.getNeighbour(set[1]).isEdge(this)
                    && !pPixel.getNeighbour(set[2]).isEdge(this);
        }
        if (canEliminate) {
            pPixel.setEdge(this, false);
            setValuesFrom(pixelMapService.nodeRemove(this, pPixel));
        }
        return canEliminate;
    }

// --Commented out by Inspection START (06/07/2020 12:58):
//    private Point toUHVW(Point pIn) {
//        return pIn.scaleX(mAspectRatio);
//    }
// --Commented out by Inspection STOP (06/07/2020 12:58)


    public void validate() {
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
//        if (mSegmentCount != segments.size()) {
//            String message = String.format("mSegmentCount mismatch: mSegmentCount=%s, segments.size()=%s", mSegmentCount, segments.size());
//            throw new IllegalStateException(message);
//        }
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






// --Commented out by Inspection START (06/07/2020 12:58):
//    public PixelMap withNodes(HashMap<IntegerPoint, Node> nodes) {
//        var clone = new PixelMap(this);
//        clone.mNodes = nodes;
//        return clone;
//    }
// --Commented out by Inspection STOP (06/07/2020 12:58)




}
