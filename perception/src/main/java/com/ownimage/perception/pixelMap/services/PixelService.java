package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelConstants;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import org.jetbrains.annotations.NotNull;

public class PixelService {

    private static final IntegerPoint[] mNeighbours = { //
            //
            new IntegerPoint(-1, -1), new IntegerPoint(0, -1), new IntegerPoint(1, -1), //
            new IntegerPoint(-1, 0), new IntegerPoint(0, 0), new IntegerPoint(1, 0), //
            new IntegerPoint(-1, 1), new IntegerPoint(0, 1), new IntegerPoint(1, 1) //
    };

    public IntegerPoint pixelToIntegerPoint(@NotNull Pixel pixel) {
        return new IntegerPoint(pixel.getX(), pixel.getY());
    }

    public boolean isNode(PixelMapData pixelMap, Integer x, Integer y) {
        return isInBounds(pixelMap, x, y) ? (pixelMap.data().get(x, y) & PixelConstants.NODE) != 0 : false;
    }

    public boolean isVisited(PixelMapData pixelMap, IntegerPoint integerPoint) {
        return isVisited(pixelMap, integerPoint.getX(), integerPoint.getY());
    }

    public boolean isVisited(PixelMapData pixelMap, Integer x, Integer y) {
        return isInBounds(pixelMap, x, y) ? (pixelMap.data().get(x, y) & PixelConstants.VISITED) != 0 : false;
    }

    public boolean isNode(PixelMapData pixelMap, IntegerPoint integerPoint) {
        return isNode(pixelMap, integerPoint.getX(), integerPoint.getY());
    }

    public boolean isEdge(PixelMapData pixelMap, int x, int y) {
        return isInBounds(pixelMap, x, y) ? (pixelMap.data().get(x, y) & PixelConstants.EDGE) != 0 : false;
    }

    public boolean isInBounds(@NotNull PixelMapData pixelMap, int x, int y) {
        if (y < 0 || y >= pixelMap.height()) {
            return false;
        }
        if (x < 0 || x >= pixelMap.width()) {
            return false;
        }
        return true;
    }

    public boolean isEdge(PixelMapData pixelMap, IntegerPoint integerPoint) {
        return isEdge(pixelMap, integerPoint.getX(), integerPoint.getY());
    }

    public Pixel getNeighbour(@NotNull Pixel pixel, int pN) {
        return pixel.add(mNeighbours[pN]);
    }
}
