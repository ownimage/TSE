package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelConstants;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final IntegerPoint[] mNeighbours = { //
            //
            new IntegerPoint(-1, -1), new IntegerPoint(0, -1), new IntegerPoint(1, -1), //
            new IntegerPoint(-1, 0), new IntegerPoint(0, 0), new IntegerPoint(1, 0), //
            new IntegerPoint(-1, 1), new IntegerPoint(0, 1), new IntegerPoint(1, 1) //
    };

    private static final Integer[] mNeighbourOrder = {0, 1, 2, 5, 8, 7, 6, 3};

    public IntegerPoint pixelToIntegerPoint(@NotNull Pixel pixel) {
        return new IntegerPoint(pixel.getX(), pixel.getY());
    }

    public boolean isNode(PixelMap pixelMap, Integer x, Integer y) {
        return isInBounds(pixelMap, x, y) ? (pixelMap.data().get(x, y) & PixelConstants.NODE) != 0 : false;
    }


    public boolean isNode(PixelMap pixelMap, IntegerPoint integerPoint) {
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

    public boolean isEdge(PixelMap pixelMap, IntegerPoint integerPoint) {
        var ip = integerPoint.getClass() == IntegerPoint.class
                ? integerPoint
                : new IntegerPoint(integerPoint.getX(), integerPoint.getY());
        return isEdge(pixelMap, ip.getX(), ip.getY());
    }

    public Pixel getNeighbour(@NotNull IntegerPoint pixel, int pN) {
        return new Pixel(pixel.add(mNeighbours[pN]));
    }

    public Vector<Pixel> getNodeNeighbours(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        Framework.logEntry(logger);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Pixel = " + this);
        }

        Vector<Pixel> allNeighbours = new Vector<>();
        pixel.getNeighbours().
            filter(n -> isNode(pixelMap, n.toIntegerPoint()))
            .forEach(allNeighbours::add);

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Returning " + allNeighbours);
        }
        Framework.logExit(logger);

        return allNeighbours;
    }

    public int countNodeNeighbours(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        return getNodeNeighbours(pixelMap, pixel).size();
    }

    public Set<Pixel> allEdgeNeighbours(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        HashSet<Pixel> allNeighbours = new HashSet<>();
        pixel.getNeighbours().
                filter(p -> isEdge(pixelMap, p))
                .forEach(allNeighbours::add);
        return allNeighbours;
    }

    public int countEdgeNeighbours(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        return  (int) pixel.getNeighbours()
                .filter(p -> isEdge(pixelMap, p))
                .count();
    }

    public int countEdgeNeighboursTransitions(@NotNull PixelMap pixelMap, @NotNull Pixel pixel) {
        int[] loop = new int[]{NW, N, NE, E, SE, S, SW, W, NW};

        int count = 0;
        boolean currentState = isEdge(pixelMap, pixel.getNeighbour(NW));

        for (int neighbour : loop) {
            if (currentState != isEdge(pixelMap, pixel.getNeighbour(neighbour))) {
                currentState = isEdge(pixelMap, pixel.getNeighbour(neighbour));
                count++;
            }
        }

        return count;
    }

    public boolean isNeighbour(@NotNull IntegerPoint me, @NotNull IntegerPoint other) {
        // big question is are you a neighbour of yourself - YES
        return Math.max(Math.abs(me.getX() - other.getX()), Math.abs(me.getY() - other.getY())) < 2;
    }


}
