/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.perception.pixelMap.segment.ISegment;

import java.io.Serializable;

/**
 * The Interface IVertex represents a joining point between two segments that approximate part of a PixelChain.
 * The vertex is associated with a Pixel in the PixelChain i.e. it has an integer (x, y)
 * position on the map. However the Pixel that it maps to might be moved slightly in terms of its UHVW double (x, y)
 * position to improve the closeness of the overall approximatio.
 */
public interface IVertex extends Serializable, Comparable<IVertex> {

    ISegment getEndSegment(IPixelChain pPixelChain);

    int getPixelIndex();

    ISegment getStartSegment(IPixelChain pPixelChain);

    int getVertexIndex();

    boolean isPositionSpecified();

    Line calcTangent(IPixelChain pPixelChain, final PixelMap pPixelMap);

    Point getUHVWPoint(final PixelMap pPixelMap, IPixelChain pPixelChain);

    Pixel getPixel(IPixelChain pPixelChain);

    IVertex withPosition(IPixelChain pPixelChain, Point pPosition);
}
