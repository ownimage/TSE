/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap.segment;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.util.Path;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.math.CubicEquation;
import com.ownimage.perception.math.CubicEquation.Root;
import com.ownimage.perception.math.KMath;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.QuarticEquation;
import com.ownimage.perception.math.Vector;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.segment.SegmentFactory.SegmentType;

public class CurveSegment extends SegmentBase {

    public final static Version mVersion = new Version(4, 0, 3, "2014/05/30 07:19");
    public final static String mClassname = CurveSegment.class.getName();
    public final static Logger mLogger = Logger.getLogger(mClassname);
    public final static long serialVersionUID = 1L;

    private Point mP1;
    private Point mA;
    private Point mB;

    CurveSegment(final IVertex pStart, final IVertex pEnd, final Point pP1) {
        super(pStart, pEnd);
        mP1 = pP1;
        init();
    }

    @Override
    public void addToPath(final Path pPath) {
        pPath.moveTo(getPointFromLambda(0.0d));
        for (int i = 0; i < getPixelChain().length(); i++) {
            final double t = (double) (i + 1) / getPixelChain().length();
            final Point point = getPointFromLambda(t);
            pPath.lineTo(point);
        }
        // pPath.lineTo(getEndUHVWPoint().add(getPixelMap().getUHVWHalfPixel()));
    }

    @Override
    public boolean closerThan(final Point pPoint) {
        final double lambda = closestLambda(pPoint);
        final double position = getStartPosition() + lambda * getLength();
        final double actualThickness = getActualThickness(position);
        return closerThan(pPoint, actualThickness);
    }

    @Override
    public boolean closerThan(final Point pPoint, final double pTolerance) {
        // TODO is this used any more?
        final double distance = distance(pPoint);
        final boolean b = distance < pTolerance;
        return b;
    }

    // @Override
    // public double closestLambda(final Point pPoint) {
    // final double d0 = getP0().minus(pPoint).length();
    // final double d2 = getP2().minus(pPoint).length();
    // final double lambda = d0 / (d0 + d2);
    //
    // return lambda;
    // }

    @Override
    public double closestLambda(final Point pUVHWPoint) {
        // Note this is closely related to distance
        final Point C = getP0().minus(pUVHWPoint);

        final double a = mA.length2();
        final double b = 2.0d * mA.dot(mB);
        final double c = 2.0d * mA.dot(C) + mB.length2();
        final double d = 2.0d * mB.dot(C);
        final double e = C.length2();

        final QuarticEquation distanceSquared = new QuarticEquation(a, b, c, d, e);
        final CubicEquation differential = distanceSquared.differentiate();

        final Root root = differential.solve();

        final double t1 = KMath.limit01(root.getRoot1());
        final Point p1 = getPointFromLambda(t1);
        final Vector delta1 = p1.minus(pUVHWPoint);
        final double distance1 = delta1.length();
        if (root.getNumberOfRoots() == 1) {
            return t1;
        }

        // I do need the p2 point in this to prevent strange results
        final double t2 = KMath.limit01(root.getRoot2());
        final Point p2 = getPointFromLambda(t2);
        final Vector delta2 = p2.minus(pUVHWPoint);
        final double distance2 = delta2.length();

        final double t3 = KMath.limit01(root.getRoot3());
        final Point p3 = getPointFromLambda(t3);
        final Vector delta3 = p3.minus(pUVHWPoint);
        final double distance3 = delta3.length();

        if (distance1 <= distance2 && distance1 <= distance3) {
            return t1;
        }
        if (distance2 <= distance3) {
            return t2;
        }
        return t3;
    }

    @Override
    public ISegment copy(final IVertex pStartVertex, final IVertex pEndVertex) {
        return new CurveSegment(pStartVertex, pEndVertex, mP1);
    }

    @Override
    public CurveSegment deepCopy(final IVertex pOriginalStartVertex, final IVertex pCopyStartVertex, final IVertex pSegmentStartVertex) {
        final IVertex endVertex = getEndVertex().deepCopy(pOriginalStartVertex, pCopyStartVertex);
        final CurveSegment copy = new CurveSegment(pSegmentStartVertex, endVertex, getP1());
        copy.attachToVertexes(false);
        return copy;
    }

    @Override
    public double distance(final Point pUVHWPoint) {
        // note this is closely related to closestLambda
        final Point C = getP0().minus(pUVHWPoint);

        final double a = mA.length2();
        final double b = 2.0d * mA.dot(mB);
        final double c = 2.0d * mA.dot(C) + mB.length2();
        final double d = 2.0d * mB.dot(C);
        final double e = C.length2();

        final QuarticEquation distanceSquared = new QuarticEquation(a, b, c, d, e);
        final CubicEquation differential = distanceSquared.differentiate();

        final Root root = differential.solve();

        final double t1 = KMath.limit01(root.getRoot1());
        final Point p1 = getPointFromLambda(t1);
        final Vector delta1 = p1.minus(pUVHWPoint);
        final double distance1 = delta1.length();
        if (root.getNumberOfRoots() == 1) {
            return distance1;
        }

        // I do need the p2 point in this to prevent strange results
        final double t2 = KMath.limit01(root.getRoot2());
        final Point p2 = getPointFromLambda(t2);
        final Vector delta2 = p2.minus(pUVHWPoint);
        final double distance2 = delta2.length();

        final double t3 = KMath.limit01(root.getRoot3());
        final Point p3 = getPointFromLambda(t3);
        final Vector delta3 = p3.minus(pUVHWPoint);
        final double distance3 = delta3.length();

        return KMath.min(distance1, distance2, distance3);
    }

    public Point getA() {
        return mA;
    }

    @Override
    public Point getControlPoint() {
        mLogger.entering(mClassname, "getControlPoint");
        mLogger.exiting(mClassname, "getControlPoint");
        return getP1();
    }

    @Override
    public Vector getEndTangentVector() {
        return getP2P1().normalize();
    }

    @Override
    public double getLength() {
        // TODO needs improvement
        return getP0P1().length() + getP2P1().length();
    }

    @Override
    public double getMaxX() {
        return KMath.max(getStartUHVWPoint().getX(), getEndUHVWPoint().getX(), getP1().getX());
    }

    @Override
    public double getMaxY() {
        return KMath.max(getStartUHVWPoint().getY(), getEndUHVWPoint().getY(), getP1().getY());
    }

    @Override
    public double getMinX() {
        return KMath.min(getStartUHVWPoint().getX(), getEndUHVWPoint().getX(), getP1().getX());
    }

    @Override
    public double getMinY() {
        return KMath.min(getStartUHVWPoint().getY(), getEndUHVWPoint().getY(), getP1().getY());
    }

    public Point getP0() {
        return getStartUHVWPoint();
    }

    /**
     * Gets the Vector from P0 to P1.
     *
     * @return the Vector
     */
    public Vector getP0P1() {
        return getP1().minus(getP0());
    }

    public Point getP1() {
        return mP1;
    }

    public Point getP2() {
        return getEndUHVWPoint();
    }

    /**
     * Gets the Vector from P2 to P1
     *
     * @return the Vector
     */
    public Vector getP2P1() {
        return getP2().minus(getP1());
    }

    @Override
    public Point getPointFromLambda(final double pT) {
        final Point p = getP0().multiply((1.0d - pT) * (1.0d - pT)) //
                .add(getP1().multiply(2.0d * (1.0d - pT) * pT)) //
                .add(getP2().multiply(pT * pT));
        return p;
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.Curve;
    }

    @Override
    public Vector getStartTangentVector() {
        return getP0P1().minus().normalize();
    }

    @Override
    public void graffiti(final ISegmentGrafittiHelper pGraphics) {
        pGraphics.graffiitControlLine(getStartUHVWPoint(), getEndUHVWPoint());
        pGraphics.graffiitControlLine(getP0(), getP1());
        pGraphics.graffiitControlLine(getP1(), getP2());
        pGraphics.graffiitControlLine(getP2(), getP0());
        pGraphics.graffitiControlPoint(getP1());
    }

    private void init() {
//        final Point p0 = getP0();
//        final Point p1 = getP1();
//        final Point p1x2 = p1.multiply(2.0d);
//        final Point p2 = getP2();
//        final Point p0ap2 = p0.add(p2);
//        mA= p0ap2.minus(p1x2);
        mA = getP0().add(getP2()).minus(getP1().multiply(2.0d));
        mB = getP1().minus(getP0()).multiply(2.0d);
    }

    @Override
    public void setControlPoint(final Point pPoint) {
        mLogger.entering(mClassname, "setControlPoint");
        setP1(pPoint);
        mLogger.exiting(mClassname, "setControlPoint");
    }

    public void setP1(final Point pP1) {
        mP1 = pP1;
        init();
    }

    @Override
    public void vertexChange(final IVertex pVertex) {
        init();
        super.vertexChange(pVertex);
    }

    @Override
    public String toString() {
        return "CurveSegment[" + super.toString() + "]";
    }
}
