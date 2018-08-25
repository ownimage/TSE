/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import java.io.Serializable;

import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.segment.ISegment;

/**
 * The Interface IVertex represends a joining point between two segments that approximate part of a PixelChain. The vertex is associated with a Pixel in the PixelChain i.e. it has an integer (x, y)
 * position on the map. However the Pixel that it maps to might be moved slightly in terms of its UHVW double (x, y) position to improve the closeness of the overall approximatio.
 */
public interface IVertex extends Serializable, Comparable<IVertex> {


    public IVertex deepCopy(IVertex pOriginalStartVertex, IVertex pCopyStartVertex);

	public abstract void delete();

	public ISegment getEndSegment();

	public int getIndex();

	public Pixel getPixel();

	public PixelChain getPixelChain();

	public PixelMap getPixelMap();

	public ISegment getStartSegment();

	public Line getTangent();

	public Point getUHVWPoint();

	public int getX();

	public int getY();

	public boolean isDisconnected();

	public boolean isEnd();

	public abstract boolean isFixed();

	public boolean isMiddle();

	public boolean isStart();

	public boolean samePosition(IVertex pVertex);

	public void setEndSegment(ISegment pEndSegment);

	public abstract void setFixed(boolean pFixed);

	public void setIndex(int pIndex);

	public void setPixel(Pixel pPixel);

	public void setStartSegment(ISegment pStartSegment);

	public void setTangent(com.ownimage.perception.math.LineSegment pTangent);

	public abstract IVertex copy();

	public void setSmooth(boolean pSmooth);

	public boolean isSmooth();

}
