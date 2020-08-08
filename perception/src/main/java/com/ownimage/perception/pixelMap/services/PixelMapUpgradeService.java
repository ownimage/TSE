package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.immutable.IPixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.Segment;

public class PixelMapUpgradeService {

    /**
     * Upgrades a PixelChain to use the immutables Vertex and Segments.
     *
     * @param pixelChain
     * @return an upgraded PixelChain
     */
    public PixelChain upgradePixelChain(IPixelChain pixelChain) {
        var result = upgradeVertexes(pixelChain);
        result = upgradeSegments(result);
        return result;
    }

    /**
     * Upgrades a PixelChain from using the old Vertex to the immutables Vertex
     *
     * @param pixelChain
     * @return a new PixelChain with all the vertexes upgraded
     */
    public PixelChain upgradeVertexes(IPixelChain pixelChain) {
        var newVertexs = StrongReference.of(pixelChain.getVertexes().clear());
        pixelChain.getVertexes().stream()
                .map(ImmutableVertex::copyOf)
                .forEach(v -> newVertexs.update(vs -> vs.add(v)));
        return pixelChain.changeVertexes(v -> newVertexs.get());
    }

    /**
     * Upgrades a PixelChain from using the old StraightSegment and CurveSegment to the immutables versions
     *
     * @param pixelChain
     * @return a new PixelChain with all the segmetns upgraded
     */
    public PixelChain upgradeSegments(IPixelChain pixelChain) {
        var newSegments = StrongReference.of(pixelChain.getSegments().clear());
        pixelChain.getSegments().stream()
                .map(Segment::toImmutable)
                .forEach(s -> newSegments.update(segs -> segs.add(s)));
        return pixelChain.changeSegments(segs -> newSegments.get());
    }
}
