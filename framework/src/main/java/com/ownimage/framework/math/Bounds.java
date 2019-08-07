package com.ownimage.framework.math;

import lombok.NonNull;

import java.util.Objects;

public class Bounds {
    private final IntegerPoint mLowerLeft;
    private final IntegerPoint mUpperRight;

    public Bounds() {
        mLowerLeft = null;
        mUpperRight = null;
    }

    public Bounds(int pXMin, int pYMin, int pXMax, int pYMax) {
        mLowerLeft = new IntegerPoint(pXMin, pYMin);
        mUpperRight = new IntegerPoint(pXMax, pYMax);
        checkValidConstruction();
    }

    public Bounds(IntegerPoint pLowerLeft, IntegerPoint pUpperRight) {
        mLowerLeft = pLowerLeft;
        mUpperRight = pUpperRight;
        checkValidConstruction();
    }

    public Bounds(IntegerPoint pPoint) {
        mLowerLeft = pPoint;
        mUpperRight = pPoint.add(1, 1);
        checkValidConstruction();
    }

    private void checkValidConstruction() {
        if (mLowerLeft.getX() > mUpperRight.getX() || mLowerLeft.getY() > mUpperRight.getY()) {
            throw new IllegalArgumentException("Invalid arguments passed to constructor " + this);
        }
    }

    private void checkValid() {
        if (!testValid()) {
            throw new IllegalStateException("No bounds yet defined");
        }
    }

    private boolean testValid() {
        return mLowerLeft != null && mUpperRight != null;
    }

    public int getXMin() {
        checkValid();
        return mLowerLeft.getX();
    }

    public int getYMin() {
        checkValid();
        return mLowerLeft.getY();
    }

    public int getXMax() {
        checkValid();
        return mUpperRight.getX();
    }

    public int getYMax() {
        checkValid();
        return mUpperRight.getY();
    }

    public IntegerPoint getLowerLeft() {
        checkValid();
        return mLowerLeft;
    }

    public IntegerPoint getUpperRight() {
        checkValid();
        return mUpperRight;
    }

    /**
     * Tests whether a point is inside the Bounds.  NOTE the upperRight IntergerPoint of the Bounds is NOT considered
     * to be inside, whilst the lowerLeft IS inside.
     *
     * @param pPoint
     * @return whether the point is inside the bounds
     */
    public boolean contains(@NonNull IntegerPoint pPoint) {
        if (!testValid()) {
            return false;
        }
        return mLowerLeft.getX() <= pPoint.getX()
                && mLowerLeft.getY() <= pPoint.getY()
                && mUpperRight.getX() > pPoint.getX()
                && mUpperRight.getY() > pPoint.getY();
    }

    /**
     * Returns the smallest Bounds that contains the current Bounds and the new point.  If the point is in the current
     * Bounds then this is returned.
     *
     * @param pPoint
     * @return Bounds object
     */
    public Bounds getBounds(@NonNull IntegerPoint pPoint) {
        if (!testValid()) {
            return new Bounds(pPoint);
        }
        if (contains(pPoint)) {
            return this;
        }
        return new Bounds(Math.min(pPoint.getX(), getXMin()), Math.min(pPoint.getY(), getYMin()),
                Math.max(pPoint.getX() + 1, getXMax()), Math.max(pPoint.getY() + 1, getYMax()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bounds bounds = (Bounds) o;
        return Objects.equals(mLowerLeft, bounds.mLowerLeft) &&
                Objects.equals(mUpperRight, bounds.mUpperRight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mLowerLeft, mUpperRight);
    }

    @Override
    public String toString() {
        if (testValid()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Bounds[mLowerLeft=");
            sb.append(mLowerLeft.toString());
            sb.append(", mUpperRight=");
            sb.append(mUpperRight.toString());
            sb.append("]");
            return sb.toString();
        }
        return "Bounds[]";
    }

    public int getWidth() {
        checkValid();
        return mUpperRight.getX() - mLowerLeft.getX();
    }

    public int getHeight() {
        checkValid();
        return mUpperRight.getY() - mLowerLeft.getY();
    }
}
