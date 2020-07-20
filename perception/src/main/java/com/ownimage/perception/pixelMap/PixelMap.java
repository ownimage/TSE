
/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.Counter;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.immutable.Immutable2DArray;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapChainGenerationService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelMapTransformService;
import io.vavr.Tuple2;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
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

}