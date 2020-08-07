package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.Segment;

public class PixelMapUpgradeService {

    public IPixelChain upgradeVertexes(IPixelChain pixelChain) {
        var newVertexs = StrongReference.of(pixelChain.getVertexes().clear());
        pixelChain.getVertexes().stream()
                .map(ImmutableVertex::copyOf)
                .forEach(v -> newVertexs.update(vs -> vs.add(v)));
        return pixelChain.changeVertexes(v -> newVertexs.get());
    }

    public IPixelChain upgradeSegments(IPixelChain pixelChain) {
        var newSegments = StrongReference.of(pixelChain.getSegments().clear());
        pixelChain.getSegments().stream()
                .map(Segment::toImmutable)
                .forEach(s -> newSegments.update(segs -> segs.add(s)));
        return pixelChain.changeSegments(segs -> newSegments.get());
    }
}
