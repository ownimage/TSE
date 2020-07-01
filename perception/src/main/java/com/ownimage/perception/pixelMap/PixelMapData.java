package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.immutable.Immutable2DArray;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.segment.ISegment;
import io.vavr.Tuple2;
import org.immutables.value.Value;

import java.util.HashMap;

@Value.Immutable
public class PixelMapData {

    private int width;

    private int height;

    private boolean is360;

    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     */
    private double aspectRatio;

    private Point halfPixel;

    private int version = 0;

    private ImmutableMap2D<Byte> data;

    private HashMap<IntegerPoint, Node> nodes;

    private ImmutableSet<PixelChain> pixelChains;

    private Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> segmentIndex;

    private int segmentCount;

    private boolean autoTrackChanges;

    public PixelMapData() {
        this.width = 0;
        this.height = 0;
        this.is360 = false;
        this.halfPixel = new Point(0, 0);
        this.aspectRatio = (double) width / height;
        this.data = new ImmutableMap2D<>(width, height, (byte) 0);
        this.segmentIndex = new Immutable2DArray<>(width, height, 20);
        this.nodes = new HashMap<>();
        this.pixelChains = new ImmutableSet<>();
        this.autoTrackChanges = false;
    }

    public PixelMapData(int width, int height, boolean is360) {
        this();
        this.width = width;
        this.height = height;
        this.is360 = is360;
    }

}
