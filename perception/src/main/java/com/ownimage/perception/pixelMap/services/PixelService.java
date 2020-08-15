package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelConstants;
import com.ownimage.perception.pixelMap.immutable.IXY;
import com.ownimage.perception.pixelMap.immutable.IntegerXY;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.ownimage.perception.pixelMap.PixelConstants.E;
import static com.ownimage.perception.pixelMap.PixelConstants.N;
import static com.ownimage.perception.pixelMap.PixelConstants.NE;
import static com.ownimage.perception.pixelMap.PixelConstants.NW;
import static com.ownimage.perception.pixelMap.PixelConstants.S;
import static com.ownimage.perception.pixelMap.PixelConstants.SE;
import static com.ownimage.perception.pixelMap.PixelConstants.SW;
import static com.ownimage.perception.pixelMap.PixelConstants.W;

@Service
public class PixelService {

    private final static Logger logger = Framework.getLogger();

    private static final IntegerXY[] mNeighbours = { //
            //
            new IntegerXY(-1, -1), new IntegerXY(0, -1), new IntegerXY(1, -1), //
            new IntegerXY(-1, 0), new IntegerXY(0, 0), new IntegerXY(1, 0), //
            new IntegerXY(-1, 1), new IntegerXY(0, 1), new IntegerXY(1, 1) //
    };

    private static final Integer[] mNeighbourOrder = {0, 1, 2, 5, 8, 7, 6, 3};

    public IntegerXY pixelToPixelMapGridPosition(@NotNull Pixel pixel) {
        return new IntegerXY(pixel.getX(), pixel.getY());
    }

    public boolean isNode(PixelMap pixelMap, Integer x, Integer y) {
        return isInBounds(pixelMap, x, y) ? (pixelMap.data().get(x, y) & PixelConstants.NODE) != 0 : false;
    }


    public boolean isNode(PixelMap pixelMap, IXY integerPoint) {
        return isNode(pixelMap, integerPoint.getX(), integerPoint.getY());
    }

    public boolean isEdge(PixelMap pixelMap, int x, int y) {
        return isInBounds(pixelMap, x, y) ? (pixelMap.data().get(x, y) & PixelConstants.EDGE) != 0 : false;
    }

    public boolean isInBounds(@NotNull PixelMap pixelMap, int x, int y) {
        if (y < 0 || y >= pixelMap.height()) {
            return false;
        }
        if (x < 0 || x >= pixelMap.width()) {
            return false;
        }
        return true;
    }

    public boolean isEdge(@NotNull PixelMap pixelMap, @NotNull IXY ip) {
        return isEdge(pixelMap, ip.getX(), ip.getY());
    }

    public IXY getNeighbour(@NotNull IXY pixel, int pN) {
        return pixel.add(mNeighbours[pN]);
    }

    public Vector<IntegerXY> getNodeNeighbours(@NotNull PixelMap pixelMap, @NotNull IXY pixel) {
        var allNeighbours = new Vector<IntegerXY>();
        getNeighbours(pixel)
                .filter(n -> isNode(pixelMap, n))
                .map(IntegerXY::of)
                .forEach(allNeighbours::add);
        return allNeighbours;
    }

    public int countNodeNeighbours(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        return getNodeNeighbours(pixelMap, pixel).size();
    }

    public Set<IntegerXY> allEdgeNeighbours(@NotNull PixelMap pixelMap, @NotNull IXY pixel) {
        var allNeighbours = new HashSet<IntegerXY>();
        getNeighbours(pixel)
                .filter(p -> isEdge(pixelMap, p))
                .map(IntegerXY::of)
                .forEach(allNeighbours::add);
        return allNeighbours;
    }

    public int countEdgeNeighbours(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        return  (int) getNeighbours(pixel)
                .filter(p -> isEdge(pixelMap, p))
                .count();
    }

    public int countEdgeNeighboursTransitions(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        int[] loop = new int[]{NW, N, NE, E, SE, S, SW, W, NW};

        int count = 0;
        boolean currentState = isEdge(pixelMap, getNeighbour(pixel, NW));

        for (int neighbour : loop) {
            if (currentState != isEdge(pixelMap, getNeighbour(pixel, neighbour))) {
                currentState = isEdge(pixelMap, getNeighbour(pixel, neighbour));
                count++;
            }
        }

        return count;
    }

    public boolean isNeighbour(@NotNull IXY me, @NotNull IXY other) {
        // big question is are you a neighbour of yourself - YES
        return Math.max(Math.abs(me.getX() - other.getX()), Math.abs(me.getY() - other.getY())) < 2;
    }


    public Stream<IXY> getNeighbours(@NotNull IXY point) {
        return Arrays.stream(mNeighbourOrder)
                .map(i -> mNeighbours[i])
                .map(point::add);
    }

}
