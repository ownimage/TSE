package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.perception.pixelMap.PixelConstants;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;

public class PixelService {

    public boolean isNode(PixelMapData pixelMap, Integer x, Integer y) {
        return (pixelMap.data().get(x, y) & PixelConstants.NODE) != 0;
    }

    public boolean isNode(PixelMapData pixelMap, IntegerPoint integerPoint) {
        return isNode(pixelMap, integerPoint.getX(), integerPoint.getY());
    }

    public boolean isEdge(PixelMapData pixelMap, Integer x, Integer y) {
        return (pixelMap.data().get(x, y) & PixelConstants.EDGE) != 0;
    }

    public boolean isEdge(PixelMapData pixelMap, IntegerPoint integerPoint) {
        return isEdge(pixelMap, integerPoint.getX(), integerPoint.getY());
    }
}
