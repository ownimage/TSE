package com.ownimage.perception.pixelMap.immutable;

import com.ownimage.framework.math.CubicEquation;
import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.QuarticEquation;
import com.ownimage.framework.math.Vector;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.segment.ISegmentGrafittiHelper;
import io.vavr.Tuple2;
import org.immutables.value.Value;

import java.awt.*;
import java.io.Serializable;

@Value.Immutable
public interface CurveSegment extends Segment, Serializable, Cloneable {

    @Override
    @Value.Parameter(order = 1)
    int getSegmentIndex();

    @Override
    @Value.Parameter(order = 2)
    double getStartPosition();

    @Value.Parameter(order = 3)
    Point getA();

    @Value.Parameter(order = 4)
    Point getB();

    @Value.Parameter(order = 5)
    Point getP1();

    @Override
    default boolean closerThanActual(PixelMap pPixelMap, IPixelChain pPixelChain, IPixelMapTransformSource pTransformSource, Point pPoint, double pMultiplier) {
        double lambda = closestLambda(pPixelMap, pPixelChain, pPoint);
        double position = getStartPosition() + lambda * getLength(pPixelMap, pPixelChain);
        double actualThickness = getActualThickness(pTransformSource, pPixelChain, position) * pMultiplier;
        return closerThan(pPixelMap, pPixelChain, pPoint, actualThickness);
    }

    @Override
    default boolean closerThan(PixelMap pPixelMap, IPixelChain pPixelChain, Point pPoint, double pTolerance) {
        double distance = distance(pPixelMap, pPixelChain, pPoint);
        return distance < pTolerance;
    }

    @Override
    default double distance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint) {
        return closestLambdaAndDistance(pPixelMap, pPixelChain, pUVHWPoint)._2;
    }

    @Override
    default double closestLambda(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint) {
        return closestLambdaAndDistance(pPixelMap, pPixelChain, pUVHWPoint)._1;
    }

    default Tuple2<Double, Double> closestLambdaAndDistance(PixelMap pPixelMap, IPixelChain pPixelChain, Point pUVHWPoint) {
        // Note this is closely related to distance
        Point C = getP0(pPixelMap, pPixelChain).minus(pUVHWPoint);

        double a = getA().length2();
        double b = 2.0d * getA().dot(getB());
        double c = 2.0d * getA().dot(C) + getB().length2();
        double d = 2.0d * getB().dot(C);
        double e = C.length2();

        QuarticEquation distanceSquared = new QuarticEquation(a, b, c, d, e);
        CubicEquation differential = distanceSquared.differentiate();

        CubicEquation.Root root = differential.solve();

        double t1 = KMath.limit01(root.getRoot1());
        Point p1 = getPointFromLambda(pPixelMap, pPixelChain, t1);
        Vector delta1 = p1.minus(pUVHWPoint);
        double distance1 = delta1.length();
        if (root.getNumberOfRoots() == 1) {
            return new Tuple2<>(t1, distance1);
        }

        // I do need the p2 point in this to prevent strange results
        double t2 = KMath.limit01(root.getRoot2());
        Point p2 = getPointFromLambda(pPixelMap, pPixelChain, t2);
        Vector delta2 = p2.minus(pUVHWPoint);
        double distance2 = delta2.length();

        double t3 = KMath.limit01(root.getRoot3());
        Point p3 = getPointFromLambda(pPixelMap, pPixelChain, t3);
        Vector delta3 = p3.minus(pUVHWPoint);
        double distance3 = delta3.length();

        if (distance1 <= distance2 && distance1 <= distance3) {
            return new Tuple2<>(t1, distance1);
        }
        if (distance2 <= distance3) {
            return new Tuple2<>(t2, distance2);
        }
        return new Tuple2<>(t3, distance3);
    }


    @Override
    default Vector getEndTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getP2P1(pPixelMap, pPixelChain).normalize();
    }

    @Override
    default double getLength(PixelMap pPixelMap, IPixelChain pPixelChain) {
        // TODO needs improvement
        return getP0P1(pPixelMap, pPixelChain).length() + getP2P1(pPixelMap, pPixelChain).length();
    }

    @Override
    default double getMaxX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return KMath.max(getStartUHVWPoint(pPixelMap, pPixelChain).getX(), getEndUHVWPoint(pPixelMap, pPixelChain).getX(), getP1().getX());
    }

    @Override
    default double getMaxY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return KMath.max(getStartUHVWPoint(pPixelMap, pPixelChain).getY(), getEndUHVWPoint(pPixelMap, pPixelChain).getY(), getP1().getY());
    }

    @Override
    default double getMinX(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return KMath.min(getStartUHVWPoint(pPixelMap, pPixelChain).getX(), getEndUHVWPoint(pPixelMap, pPixelChain).getX(), getP1().getX());
    }

    @Override
    default double getMinY(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return KMath.min(getStartUHVWPoint(pPixelMap, pPixelChain).getY(), getEndUHVWPoint(pPixelMap, pPixelChain).getY(), getP1().getY());
    }

    default Point getP0(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getStartUHVWPoint(pPixelMap, pPixelChain);
    }

    /**
     * Gets the Vector from P0 to P1.
     *
     * @param pPixelMap   the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the Vector
     */
    default Vector getP0P1(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getP1().minus(getP0(pPixelMap, pPixelChain));
    }


    default Point getP2(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getEndUHVWPoint(pPixelMap, pPixelChain);
    }

    /**
     * Gets the Vector from P2 to P1
     *
     * @param pPixelMap   the PixelMap performing the this operation
     * @param pPixelChain the Pixel Chain performing this operation
     * @return the Vector
     */
    default Vector getP2P1(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getP2(pPixelMap, pPixelChain).minus(getP1());
    }

    @Override
    default Point getPointFromLambda(PixelMap pPixelMap, IPixelChain pPixelChain, double pT) {
        return getP0(pPixelMap, pPixelChain).multiply((1.0d - pT) * (1.0d - pT)) //
                .add(getP1().multiply(2.0d * (1.0d - pT) * pT)) //
                .add(getP2(pPixelMap, pPixelChain).multiply(pT * pT));
    }

    @Override
    default Vector getStartTangentVector(PixelMap pPixelMap, IPixelChain pPixelChain) {
        return getP0P1(pPixelMap, pPixelChain).minus().normalize();
    }

    @Override
    default void graffiti(
            PixelMap pPixelMap,
            IPixelChain pPixelChain,
            ISegmentGrafittiHelper pGraphics
    ) {
        var c = Color.WHITE;
        pGraphics.graffitiLine(getP0(pPixelMap, pPixelChain), getP1(), c);
        pGraphics.graffitiLine(getP1(), getP2(pPixelMap, pPixelChain), c);
        pGraphics.graffitiLine(getP0(pPixelMap, pPixelChain), getP2(pPixelMap, pPixelChain), Color.RED);
        //super.graffiti(pPixelMap, pPixelChain, pGraphics);
        pGraphics.graffitiControlPoint(getP1());
    }

}
