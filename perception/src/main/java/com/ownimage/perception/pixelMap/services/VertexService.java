package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.immutable.ImmutableVertex;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.immutable.Vertex;
import com.ownimage.perception.pixelMap.segment.ISegment;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class VertexService {

    public Vertex createVertex(PixelMap pPixelMap, IPixelChain pPixelChain, int pVertexIndex, int pPixelIndex) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelCount()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelCount() + ")");
        }
        val position = pPixelChain.getUHVWPoint(pPixelMap, pPixelIndex);
        return ImmutableVertex.of(pPixelIndex, pVertexIndex, position);
    }

    public Vertex createVertex(IPixelChain pPixelChain, int pVertexIndex, int pPixelIndex, Point pPosition) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelCount()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelCount() + ")");
        }

        return ImmutableVertex.of(pPixelIndex, pVertexIndex, pPosition);
    }

    private Line calcTangent(Point pPoint, Line pStartTangent, Line pEndTangent) {
        Point startTangentPoint = pEndTangent.getPoint(1.0d);
        Point endTangentPoint = pStartTangent.getPoint(1.0d);
        Vector tangentVector = startTangentPoint.minus(endTangentPoint).normalize();

        return new Line(pPoint, pPoint.add(tangentVector));
    }

    /**
     * Calc tangent always generates a tangent line that goes in the direction of start to finish.
     *
     * @param pixelChain the Pixel Chain performing this operation
     * @param pixelMap   the PixelMap performing this operation
     */
    public Line calcTangent(PixelMap pixelMap, IPixelChain pixelChain, Vertex vertex) {
        Line tangent;
        ISegment startSegment = getStartSegment( pixelChain, vertex);
        ISegment endSegment = getEndSegment( pixelChain, vertex);

        if (startSegment == null && endSegment == null) {
            tangent = null;

        } else if (startSegment == null) {
            tangent = endSegment.getStartTangent(pixelMap, pixelChain);
            tangent = tangent.getReverse();

        } else if (endSegment == null) {
            tangent = startSegment.getEndTangent(pixelMap, pixelChain);

        } else {
            return calcTangent(
                    vertex.getPosition(),
                    startSegment.getEndTangent(pixelMap, pixelChain),
                    endSegment.getStartTangent(pixelMap, pixelChain)
            );
        }
        return tangent;
    }

    /**
     * Calculates an approximate tangent line to the PixelChain at this point.  This is done by counting forward and
     * backwards pLength pixels and calculating a vector between these, this vector is added to the UHVW point that
     * represents this Vertex to generate a tangent.
     *
     * @param pPixelMap      the PixelMap performing this operation
     * @param pPixelChainthe PixelChain performing this operation
     * @param pLength        the pixelLength in Pixels to count each way
     * @return the calculated tangent
     */
    public Line calcLocalTangent(PixelMap pixelMap, IPixelChain pixelChain, Vertex vertex, int pLength) {
        val ltStartIndex = KMath.max(vertex.getPixelIndex() - pLength, 0);
        val ltEndIndex = KMath.min(vertex.getPixelIndex() + pLength, pixelChain.getMaxPixelIndex());
        val ltStartPoint = pixelChain.getUHVWPoint(pixelMap, ltStartIndex);
        val ltEndPoint = pixelChain.getUHVWPoint(pixelMap, ltEndIndex);
        val tangentDirection = ltEndPoint.minus(ltStartPoint).normalize();
        var thisPosition = vertex.getPosition();
        return new Line(thisPosition, thisPosition.add(tangentDirection));
    }

    public Pixel getPixel(IPixelChain pixelChain, Vertex vertex) {
        return pixelChain.getPixel(vertex.getPixelIndex());
    }

    public ISegment getStartSegment(IPixelChain pixelChain, Vertex vertex) {
        return pixelChain.getSegment(vertex.getVertexIndex() - 1);
    }

    public ISegment getEndSegment(IPixelChain pixelChain, Vertex vertex) {
        return pixelChain.getSegment(vertex.getVertexIndex());
    }

}
