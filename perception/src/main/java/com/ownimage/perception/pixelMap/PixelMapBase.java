package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.util.immutable.Immutable2DArray;
import com.ownimage.framework.util.immutable.ImmutableMap;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import io.vavr.Tuple2;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class PixelMapBase implements PixelMapData {

    @Getter
    protected boolean m360;
    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     */
    protected double mAspectRatio;
    protected Point mUHVWHalfPixel;
    public  IPixelMapTransformSource mTransformSource;
    protected int mWidth;
    protected int mHeight;
    protected int mVersion = 0;
    @Getter
    protected ImmutableMap2D<Byte> mData;
    protected ImmutableMap<IntegerPoint, Node> mNodes = new ImmutableMap<>();
    @Getter
    protected ImmutableSet<PixelChain> mPixelChains = new ImmutableSet<>();
    @Getter
    protected Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> mSegmentIndex;

    protected int mSegmentCount;

    /**
     * Means that the PixelMap will add/remove/reapproximate PixelChains as nodes are added and removed.
     * This is turned off whilst the bulk processing is running. // TODO should this extend to the conversion of Pixels to Nodes etc.
     */
    @Getter
    // privacy changed to ease migration
    protected boolean mAutoTrackChanges = false;

    @Override
    public ImmutableMap<IntegerPoint, Node> nodes() {
        return mNodes;
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

    public ImmutablePixelMapData withAutoTrackChanges(boolean autoTrackChanges) {
        return ImmutablePixelMapData.copyOf(this).withAutoTrackChanges(autoTrackChanges);
    }

    @Override
    public ImmutableMap2D<Byte> data() {
        return mData;
    }

    public ImmutablePixelMapData withNodes(ImmutableMap<IntegerPoint, Node> nodes) {
        return ImmutablePixelMapData.copyOf(this).withNodes(nodes);
    }

    public ImmutablePixelMapData withPixelChains(ImmutableSet<PixelChain> pixelChains) {
        return ImmutablePixelMapData.copyOf(this).withPixelChains(pixelChains);
    }

    public ImmutablePixelMapData withSegmentIndex(Immutable2DArray<ImmutableSet<Tuple2<PixelChain, ISegment>>> segmentIndex) {
        return ImmutablePixelMapData.copyOf(this).withSegmentIndex(segmentIndex);
    }

    public ImmutablePixelMapData withSegmentCount(int segmentCount) {
        return ImmutablePixelMapData.copyOf(this).withSegmentCount(segmentCount);
    }

    @Override
    public int width() {
        return mWidth;
    }

    @Override
    public int height() {
        return mHeight;
    }

    public ImmutablePixelMapData withData(@NotNull ImmutableMap2D<Byte> data) {
        if (data.width() != width() || data.height() != height()) {
            var msg = String.format("PixelMap wxh = %sx%s, data wxh = %sx%s",
                    width(), height(), data.width(), data.height());
            throw new IllegalArgumentException(msg);
        }

        return ImmutablePixelMapData.copyOf(this).withData(data);

    }

    @Override
    public int segmentCount() {
        return mSegmentCount;
    }


    public int getHeight() {
        return mHeight;
    }


    protected void setHeight(int pHeight) {
        mHeight = pHeight;
    }

    protected double getLineTolerance() {
        return mTransformSource.getLineTolerance();
    }

    public double getLineCurvePreference() {
        return mTransformSource.getLineCurvePreference();
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
}
