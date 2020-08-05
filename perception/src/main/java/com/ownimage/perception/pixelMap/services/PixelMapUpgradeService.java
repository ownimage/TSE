package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;

public class PixelMapUpgradeService {

    public IPixelChain upgradeVertex(IPixelChain pixelChain) {
        var newVertexs = StrongReference.of(pixelChain.getVertexes().clear());
        pixelChain.getVertexes().stream()
                .map(v -> ImmutableVertex.of(v.getVertexIndex(), v.getPixelIndex(), v.getPosition()))
        .forEach(v -> newVertexs.update(vs -> vs.add(v)));
        return pixelChain.changeVertexes(v -> newVertexs.get());
    }
}
