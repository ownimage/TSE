package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.segment.ISegment;
import lombok.val;

public class VertexService {

    public static Vertex createVertex(PixelMap pPixelMap, IPixelChain pPixelChain, int pVertexIndex, int pPixelIndex) {
        if (pPixelIndex < 0 || pPixelIndex >= pPixelChain.getPixelCount()) {
            throw new IllegalArgumentException("pIndex =(" + pPixelIndex + ") must lie between 0 and the size of the mPixels collection =(" + pPixelChain.getPixelCount() + ")");
        }
        val position = pPixelChain.getUHVWPoint(pPixelMap, pPixelIndex);
        return new Vertex(pVertexIndex, pPixelIndex, position);
    }

    public static Vertex createVertex(IPixelChain pPixelChain, int pVertexIndex, int pPixelIndex, Point pPosition) {
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
        Line tangent;
        ISegment startSegment = vertex.getStartSegment(context.getPixelChain());
        ISegment endSegment = vertex.getEndSegment(context.getPixelChain());

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
}
