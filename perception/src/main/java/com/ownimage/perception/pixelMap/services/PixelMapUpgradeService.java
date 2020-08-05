package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.immutable.CurveSegment;
import com.ownimage.perception.pixelMap.immutable.ImmutableCurveSegment;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;

public class PixelMapUpgradeService {

    public IPixelChain upgradeVertexes(IPixelChain pixelChain) {
        var newVertexs = StrongReference.of(pixelChain.getVertexes().clear());
        pixelChain.getVertexes().stream()
                .map(v -> ImmutableVertex.copyOf(v))
                .forEach(v -> newVertexs.update(vs -> vs.add(v)));
        return pixelChain.changeVertexes(v -> newVertexs.get());
    }

    public IPixelChain upgradeSegments(IPixelChain pixelChain) {
        var newSegments = StrongReference.of(pixelChain.getSegments().clear());
        pixelChain.getSegments().stream()
                .map(s -> {
                    if (s instanceof CurveSegment) {
                        return ImmutableCurveSegment.copyOf((CurveSegment) s);
                    }
                    return s;
                })
                .forEach(s -> newSegments.update(segs -> segs.add(s)));
        return pixelChain.changeSegments(segs -> newSegments.get());
    }
}
