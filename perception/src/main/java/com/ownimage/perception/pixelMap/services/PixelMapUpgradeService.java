package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.Segment;
import com.ownimage.perception.pixelMap.immutable.Vertex;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

public class PixelMapUpgradeService {

    private VertexService vertexService;

    @Autowired
    public void setVertexService(VertexService vertexService) {
        this.vertexService = vertexService;
    }

    /**
     * Upgrades a PixelChain to use the immutables Vertex and Segments.
     *
     * @param pixelChain
     * @return an upgraded PixelChain
     */
    public ImmutablePixelChain upgradePixelChain(@NotNull PixelChain pixelChain, int height) {
        var result = upgradeVertexes(pixelChain, height);
        result = upgradeSegments(result);
        return result;
    }

    /**
     * Upgrades a PixelChain from using the old Vertex to the immutables Vertex
     *
     * @param pixelChain
     * @return a new PixelChain with all the vertexes upgraded
     */
    public ImmutablePixelChain upgradeVertexes(@NotNull PixelChain pixelChain, int height) {
        var newVertexs = StrongReference.of(pixelChain.getVertexes().clear());
        pixelChain.getVertexes().stream()
                .map(v -> fixNullPositionVertex(pixelChain, v, height))
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
    public ImmutablePixelChain upgradeSegments(@NotNull PixelChain pixelChain) {
        var newSegments = StrongReference.of(pixelChain.getSegments().clear());
        pixelChain.getSegments().stream()
                .map(Segment::toImmutable)
                .forEach(s -> newSegments.update(segs -> segs.add(s)));
        return pixelChain.changeSegments(segs -> newSegments.get());
    }


    public @NotNull Vertex fixNullPositionVertex(@NotNull PixelChain pixelChain, @NotNull Vertex vertex, int height) {
        if (vertex.getPosition() != null) {
            return vertex;
        }

        var position = vertexService.getPixel(pixelChain, vertex).getUHVWMidPoint(height);
        return vertexService.createVertex(pixelChain, vertex.getVertexIndex(), vertex.getPixelIndex(), position);
    }
}
