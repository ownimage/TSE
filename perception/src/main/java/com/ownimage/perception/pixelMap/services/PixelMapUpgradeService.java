package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.Node;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.Segment;
import com.ownimage.perception.pixelMap.immutable.Vertex;
import com.ownimage.perception.pixelMap.immutable.XY;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

public class PixelMapUpgradeService {

    private VertexService vertexService;

    @Autowired
    public void setVertexService(VertexService vertexService) {
        this.vertexService = vertexService;
    }

    public @NotNull ImmutablePixelMap ensureAllPixelChainsMappedToNodes(@NotNull ImmutablePixelMap pixelMap) {
        var nodes = StrongReference.of(pixelMap.nodes());
        pixelMap.pixelChains().stream()
                .forEach(pc -> {
                    Consumer<ImmutableIXY> updateNodes = key ->
                            nodes.update(n -> n.update(key, (k, v) -> (v != null ? v : Node.ofIXY(k)).addPixelChain(pc)));
                    pc.pixels().firstElement().map(XY::of).ifPresent(updateNodes);
                    pc.pixels().lastElement().map(XY::of).ifPresent(updateNodes);
                });
        return pixelMap.withNodes(nodes.get());
    }

    /**
     * Upgrades a PixelChain to use the immutables Pixels, Vertex and Segments.
     *
     * @param pixelChain the pixelChain
     * @return an upgraded PixelChain
     */
    public ImmutablePixelChain upgradePixelChain(@NotNull PixelChain pixelChain, int height) {
        var result = upgradePixels(pixelChain, height);
        result = upgradeVertexes(result, height);
        result = upgradeSegments(result);
        return result;
    }

    /**
     * Upgrades a PixelChain from using the old IntergerPoint based Pixels and nodes to the immutables Pixel
     *
     * @param pixelChain the pixelChain
     * @return a new PixelChain with all the vertexes upgraded
     */
    public ImmutablePixelChain upgradePixels(@NotNull PixelChain pixelChain, int height) {
        var newPixels = StrongReference.of(pixelChain.pixels().clear());
        for (int i = 0; i < pixelChain.pixels().size(); i++) {
            var oldPixel = (Object) pixelChain.pixels().get(i);
            // the line belows allows for the conversion of old and new formats of the pixel
            var ip = oldPixel instanceof XY ? XY.of((XY) oldPixel) : XY.of((IntegerPoint) oldPixel);
            var newPixel = Pixel.of(ip.getX(), ip.getY(), height);
            newPixels.update(np -> np.add(newPixel));
        }
        return pixelChain.changePixels(pixels -> newPixels.get());
    }

    /**
     * Upgrades a PixelChain from using the old Vertex to the immutables Vertex
     *
     * @param pixelChain the pixelChain
     * @return a new PixelChain with all the vertexes upgraded
     */
    public ImmutablePixelChain upgradeVertexes(@NotNull PixelChain pixelChain, int height) {
        var newVertexs = StrongReference.of(pixelChain.vertexes().clear());
        pixelChain.vertexes().stream()
                .map(v -> fixNullPositionVertex(pixelChain, v, height))
                .map(ImmutableVertex::copyOf)
                .forEach(v -> newVertexs.update(vs -> vs.add(v)));
        return pixelChain.changeVertexes(v -> newVertexs.get());
    }

    /**
     * Upgrades a PixelChain from using the old StraightSegment and CurveSegment to the immutables versions
     *
     * @param pixelChain the pixelChain
     * @return a new PixelChain with all the segmetns upgraded
     */
    public ImmutablePixelChain upgradeSegments(@NotNull PixelChain pixelChain) {
        var newSegments = StrongReference.of(pixelChain.segments().clear());
        pixelChain.segments().stream()
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
