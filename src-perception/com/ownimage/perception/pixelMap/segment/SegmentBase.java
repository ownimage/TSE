/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap.segment;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Intersect3D;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Point3D;
import com.ownimage.perception.math.Vector3D;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;

public abstract class SegmentBase implements ISegment {



    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final PixelChain mPixelChain;
    private final IVertex mStart;
    private final IVertex mEnd;
    private double mStartPosition;

    private Point mAveragePoint;

    public SegmentBase(final IVertex pStart, final IVertex pEnd) {

        if (pStart == null) {
            throw new IllegalArgumentException("pStart must not be null.");
        }

        if (pEnd == null) {
            throw new IllegalArgumentException("pEnd must not be null.");
        }

        if (pStart.getPixelChain() != pEnd.getPixelChain()) {
            throw new IllegalArgumentException("start and end Vertex must belong to the same pixelChain.");
        }

        if (pStart.getIndex() >= pEnd.getIndex()) {
            throw new IllegalArgumentException("start index =(" + pStart.getIndex() + ")must be less than end index =(" + pEnd.getIndex() + ").");
        }

        mPixelChain = pStart.getPixelChain();
        mStart = pStart;
        mEnd = pEnd;
    }

    @Override
    public void attachToVertexes(final boolean pReCalcSegments) {
        mStart.setEndSegment(this);
        mEnd.setStartSegment(this);
        if (pReCalcSegments) {
            getPixelChain().reCalcSegments();
        }
    }

    private void calcAveragePoint() {
        Point sum = Point.Point00;
        for (int i = getStartIndex(); i <= getEndIndex(); i++) {
            sum = sum.add(mPixelChain.getUHVWPoint(i));
        }
        final double cnt = getEndIndex() + 1 - getStartIndex();
        mAveragePoint = sum.divide(cnt);
    }

    @Override
    public double calcError() {
        double error = 0.0d;
        for (int i = getStartIndex(); i <= getEndIndex(); i++) {
            final Point uhvw = mPixelChain.getUHVWPoint(i);
            final double distance = distance(uhvw);

            error += distance * distance;
        }

        if (error < 0.0d) {
            System.err.println("-ve error");
        }

        return error;
    }

    @Override
    public boolean closerThan(final Point pPoint, final double pTolerance) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Point closestPoint(final Point pUhvw) {
        final double lambda = closestLambda(pUhvw);
        final Point point = getPointFromLambda(lambda);
        return point;
    }

    @Override
    public void delete() {
        getPixelChain().deleteSegment(this);
    }

    public abstract double distance(final Point pUVHWPoint);

    public double getActualThickness(final double pPosition) {
        final double actualThickness = getPixelChain().getActualThickness(pPosition);
        return actualThickness;
    }

    @Override
    public synchronized Point getAveragePoint() {
        if (mAveragePoint == null) {
            calcAveragePoint();
        }

        return mAveragePoint;
    }

    @Override
    public Point getControlPoint() {
        Framework.logEntry(mLogger);
        Framework.logExit(mLogger);
        return null;
    }

    @Override
    public int getEndIndex() {
        return mEnd.getIndex();
    }

    @Override
    public Pixel getEndPixel() {
        return mEnd.getPixel();
    }

    @Override
    public Line getEndTangent() {
        return new Line(getEndUHVWPoint(), getEndUHVWPoint().add(getEndTangentVector()));
    }

    @Override
    public Point getEndUHVWPoint() {
        return mEnd.getUHVWPoint();
    }

    @Override
    public IVertex getEndVertex() {
        return mEnd;
    }

    @Override
    public double getMaxX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMaxY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMinX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getMinY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PixelChain getPixelChain() {
        return mStart.getPixelChain();
    }

    @Override
    public int getPixelLength() {
        int length;

        if (getStartIndex() == 0) {
            length = 1 + getEndIndex();
        } else {
            length = getEndIndex() - getStartIndex();
        }

        return length;
    }

    @Override
    public PixelMap getPixelMap() {
        return getStartVertex().getPixelMap();
    }

    @Override
    public int getStartIndex() {
        return mStart.getIndex();
    }

    @Override
    public Pixel getStartPixel() {
        return mStart.getPixel();
    }

    @Override
    public double getStartPosition() {
        return mStartPosition;
    }

    @Override
    public Line getStartTangent() {
        return new Line(getStartUHVWPoint(), getStartUHVWPoint().add(getStartTangentVector()));
    }

    @Override
    public Point getStartUHVWPoint() {
        return mStart.getUHVWPoint();
    }

    @Override
    public IVertex getStartVertex() {
        return mStart;
    }

    @Override
    public void graffiti(final ISegmentGrafittiHelper pGraphics) {
    }

    @Override
    public Intersect3D intersect3D(final Point pUHVW) {
        final double lambda = closestLambda(pUHVW);
        final Point closest = getPointFromLambda(lambda);
        final double distance = closest.minus(pUHVW).length();

        final double position = getStartPosition() + lambda * getLength();
        final double actualThickness = getActualThickness(position);

        if (actualThickness < distance) {
            return null;
        }

        final double height = Math.sqrt(actualThickness * actualThickness - distance * distance);
        final Point3D iPoint = new Point3D(pUHVW, height);
        final Point3D closet3D = new Point3D(closest);
        final Vector3D iVector = closet3D.to(iPoint).normalize();
        return new Intersect3D(iPoint, iVector);
    }

    @Override
    public double length() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void nextControlPoint() {
    }

    @Override
    public boolean noPixelFurtherThan(final double pDistance) {
        for (int i = getStartIndex(); i <= getEndIndex(); i++) {
            final Point uhvw = mPixelChain.getUHVWPoint(i);
            if (distance(uhvw) > pDistance) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean noPixelFurtherThanReverse(final double pDistance) {
        for (int i = getEndIndex(); i <= getStartIndex(); i--) {
            final Point uhvw = mPixelChain.getUHVWPoint(i);
            if (distance(uhvw) > pDistance) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void previousControlPoint() {
    }

    @Override
    public void setControlPoint(final Point pPoint) {
        Framework.logEntry(mLogger);
        Framework.logExit(mLogger);
    }

    @Override
    public void setStartPosition(final double pStartPosition) {
        mStartPosition = pStartPosition;
    }

    @Override
    public String toString() {
        return "SegmentBase[" + mStart + "," + mEnd + "]";
    }

    @Override
    public void vertexChange(final IVertex pVertex) {
        // final Date start = new Date();
        getPixelMap().indexSegments();
        // final long time = new Date().getTime() - start.getTime();
        // mLogger.severe("Vertex time change " + time / 1000.0);
    }
}
