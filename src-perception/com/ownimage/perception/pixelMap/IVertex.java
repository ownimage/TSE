/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.segment.ISegment;

import java.io.Serializable;

/**
 * The Interface IVertex represends a joining point between two segments that approximate part of a PixelChain. The vertex is associated with a Pixel in the PixelChain i.e. it has an integer (x, y)
 * position on the map. However the Pixel that it maps to might be moved slightly in terms of its UHVW double (x, y) position to improve the closeness of the overall approximatio.
 */
public interface IVertex extends Serializable, Comparable<IVertex> {

	ISegment getEndSegment();

	int getIndex();

	ISegment getStartSegment();

	Line calcTangent(PixelChain pPiixelChain);

	Line getTangent();

	Point getUHVWPoint(PixelChain pPixelChain);

	int getX(PixelChain pPixelChain);

	int getY(PixelChain pPixelChain);

	boolean isDisconnected();

	boolean isEnd();

	boolean isFixed(PixelChain pPixelChain);

	boolean isMiddle();

	boolean isStart();

	void setEndSegment(ISegment pEndSegment);

	void setFixed(boolean pFixed);

	void setIndex(PixelChain pPixelChain, int pIndex);

	void setStartSegment(ISegment pStartSegment);

	void setTangent(com.ownimage.perception.math.LineSegment pTangent);


	Pixel getPixel(PixelChain pPixelChain);

}
