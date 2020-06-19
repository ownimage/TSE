package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.segment.ISegment;
import lombok.val;

public class VertexService {

    public Vertex createVertex(PixelMap pPixelMap, IPixelChain pPixelChain, int pVertexIndex, int pPixelIndex) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelCount()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelCount() + ")");
        }
        val position = pPixelChain.getUHVWPoint(pPixelMap, pPixelIndex);
        return new Vertex(pVertexIndex, pPixelIndex, position);
    }

    public Vertex createVertex(IPixelChain pPixelChain, int pVertexIndex, int pPixelIndex, Point pPosition) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelCount()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelCount() + ")");
        }

        return new Vertex(pVertexIndex, pPixelIndex, pPosition);
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
     * @param context.getPixelChain() the Pixel Chain performing this operation
     * @param context.getPixelMap()   the PixelMap performing this operation
     */
    public Line calcTangent(Services services, PixelChainContext context, IVertex vertex) {
        var vertexServices = services.getVertexService();
        Line tangent;
        ISegment startSegment = vertexServices.getStartSegment(services,context,vertex);
        ISegment endSegment = vertexServices.getEndSegment(services,context,vertex);

        if (startSegment == null && endSegment == null) {
            tangent = null;

        } else if (startSegment == null) {
            tangent = endSegment.getStartTangent(context.getPixelMap(), context.getPixelChain());
            tangent = tangent.getReverse();

        } else if (endSegment == null) {
            tangent = startSegment.getEndTangent(context.getPixelMap(), context.getPixelChain());

        } else {
            return calcTangent(
                    vertex.getUHVWPoint(context.getPixelMap(), context.getPixelChain()),
                    startSegment.getEndTangent(context.getPixelMap(), context.getPixelChain()),
                    endSegment.getStartTangent(context.getPixelMap(), context.getPixelChain())
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
     * @param pLength        the length in Pixels to count each way
     * @return the calculated tangent
     */
    public Line calcLocalTangent(Services services, PixelChainContext context, IVertex vertex, int pLength) {
        var pixelChain = context.getPixelChain();
        var pixelMap = context.getPixelMap();

        val ltStartIndex = KMath.max(vertex.getPixelIndex() - pLength, 0);
        val ltEndIndex = KMath.min(vertex.getPixelIndex() + pLength, pixelChain.getMaxPixelIndex());
        val ltStartPoint = pixelChain.getUHVWPoint(pixelMap, ltStartIndex);
        val ltEndPoint = pixelChain.getUHVWPoint(pixelMap, ltEndIndex);
        val tangentDirection = ltEndPoint.minus(ltStartPoint).normalize();
        var thisPosition = vertex.getUHVWPoint(pixelMap, pixelChain);
        return new Line(thisPosition, thisPosition.add(tangentDirection));
    }

    public Pixel getPixel(Services services, PixelChainContext context, IVertex vertex) {
        return context.getPixelChain().getPixel(vertex.getPixelIndex());
    }

    public ISegment getStartSegment(Services services, PixelChainContext context, IVertex vertex) {
        return context.getPixelChain().getSegment(vertex.getVertexIndex() - 1);
    }

    public ISegment getEndSegment(Services services, PixelChainContext context, IVertex vertex) {
        return context.getPixelChain().getSegment(vertex.getVertexIndex());
    }

}
