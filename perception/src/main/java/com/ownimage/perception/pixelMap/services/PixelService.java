package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.PixelConstants;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.immutable.XY;
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

    private static final ImmutableIXY[] mNeighbours = { //
            //
            ImmutableIXY.of(-1, -1), ImmutableIXY.of(0, -1), ImmutableIXY.of(1, -1), //
            ImmutableIXY.of(-1, 0), ImmutableIXY.of(0, 0), ImmutableIXY.of(1, 0), //
            ImmutableIXY.of(-1, 1), ImmutableIXY.of(0, 1), ImmutableIXY.of(1, 1) //
    };

    private static final Integer[] mNeighbourOrder = {0, 1, 2, 5, 8, 7, 6, 3};

    public ImmutableIXY pixelToPixelMapGridPosition(@NotNull Pixel pixel) {
        return ImmutableIXY.of(pixel.getX(), pixel.getY());
    }

    public boolean isNode(PixelMap pixelMap, Integer x, Integer y) {
        return isInBounds(pixelMap, x, y) ? (pixelMap.data().get(x, y) & PixelConstants.NODE) != 0 : false;
    }


    public boolean isNode(PixelMap pixelMap, XY integerPoint) {
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

    public boolean isEdge(@NotNull PixelMap pixelMap, @NotNull XY ip) {
        return isEdge(pixelMap, ip.getX(), ip.getY());
    }

    public XY getNeighbour(@NotNull XY pixel, int pN) {
        return pixel.add(mNeighbours[pN]);
    }

    public Vector<ImmutableIXY> getNodeNeighbours(@NotNull PixelMap pixelMap, @NotNull XY pixel) {
        var allNeighbours = new Vector<ImmutableIXY>();
        getNeighbours(pixel)
                .filter(n -> isNode(pixelMap, n))
                .map(XY::of)
                .forEach(allNeighbours::add);
        return allNeighbours;
    }

    public int countNodeNeighbours(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        return getNodeNeighbours(pixelMap, pixel).size();
    }

    public Set<ImmutableIXY> allEdgeNeighbours(@NotNull PixelMap pixelMap, @NotNull XY pixel) {
        var allNeighbours = new HashSet<ImmutableIXY>();
        getNeighbours(pixel)
                .filter(p -> isEdge(pixelMap, p))
                .map(XY::of)
                .forEach(allNeighbours::add);
        return allNeighbours;
    }

    public int countEdgeNeighbours(@NotNull PixelMap pixelMap, @NotNull XY pixel) {
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

    public boolean isNeighbour(@NotNull XY me, @NotNull XY other) {
        // big question is are you a neighbour of yourself - YES
        return Math.max(Math.abs(me.getX() - other.getX()), Math.abs(me.getY() - other.getY())) < 2;
    }


    public Stream<XY> getNeighbours(@NotNull XY point) {
        return Arrays.stream(mNeighbourOrder)
                .map(i -> mNeighbours[i])
                .map(point::add);
    }

}
