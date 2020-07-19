
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
import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapChainGenerationService;
import com.ownimage.perception.pixelMap.services.PixelMapMappingService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelMapTransformService;
import io.vavr.Tuple2;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

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
    private static PixelMapService pixelMapService;
    private static PixelMapChainGenerationService pixelMapChainGenerationService;
    private static PixelMapApproximationService pixelMapApproximationService;
    private static PixelChainService pixelChainService;
    private static PixelMapTransformService pixelMapTransformService;

    static {
        com.ownimage.perception.pixelMap.services.Services defaultServices = com.ownimage.perception.pixelMap.services.Services.getDefaultServices();
        pixelMapService = defaultServices.getPixelMapService();
        pixelMapChainGenerationService = defaultServices.pixelMapChainGenerationService();
        pixelMapApproximationService = defaultServices.getPixelMapApproximationService();
        pixelChainService = defaultServices.getPixelChainService();
        pixelMapTransformService = defaultServices.getPixelMapTransformService();
    }

    public PixelMap(int pWidth, int pHeight, boolean p360, IPixelMapTransformSource pTransformSource) {
        setWidth(pWidth);
        setHeight(pHeight);
        m360 = p360;
        mTransformSource = pTransformSource;
        // mHalfPixel = new Point(0.5d / getHeight(), 0.5d / getWidth());
        mAspectRatio = (double) pWidth / pHeight;
        mData = new ImmutableMap2D<>(pWidth, pHeight, (byte) 0);
        // resetSegmentIndex();
        mUHVWHalfPixel = new Point(0.5d * mAspectRatio / pWidth, 0.5d / pHeight);
        //mAutoTrackChanges = true;
        mSegmentIndex = new Immutable2DArray<>(width(), height(), 20);
        mSegmentCount = 0;
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

    public String toString() {
        return "PixelMap{mVersion=" + mVersion + "}";
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
            Pixel pixel = pixelMapService.getPixelAt(this, i.getX(), i.getY());
            Point centre = pixel.getUHVWMidPoint(this.getHeight());
            if (pSegment.closerThan(this, pPixelChain, centre, getUHVWHalfPixel().length())) {
                val segments = new HashSet();
                pixelMapTransformService.getSegments(this, i.getX(), i.getY())
                        .map(ImmutableSet::toCollection).ifPresent(segments::addAll);
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

    private void reportProgress(IProgressObserver pProgressObserver, String pProgressString, int pPercent) {
        if (pProgressObserver != null) {
            pProgressObserver.setProgress(pProgressString, pPercent);
        }
    }

    public void process07_mergeChains(IProgressObserver pProgressObserver) {
        reportProgress(pProgressObserver, "Merging Chains ...", 0);
        mLogger.info(() -> "number of PixelChains: " + mPixelChains.size());
        nodes().values().forEach(pNode -> pNode.mergePixelChains(this));
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
            setValuesFrom(pixelMapService.pixelChainsClear(this));
            setValuesFrom(pixelMapService.pixelChainsAddAll(this, refined));
        }
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

    // access weakened for testing only
    protected void setData_FOR_TESTING_PURPOSES_ONLY(Pixel pPixel, boolean pState, byte pValue) {
        setValuesFrom(pixelMapService.setData(this, pPixel, pState, pValue));
    }
}