/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import com.ownimage.framework.util.Framework;

import javax.vecmath.Vector2d;
import java.util.logging.Logger;

public class Vector extends Point {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public Vector(final double pX, final double pY) {
        super(pX, pY);
    }

    public Vector(final Point pPoint) {
        super(pPoint.getX(), pPoint.getY());
    }

    public static Vector fromAtoB(final Point pA, final Point pB) {
        return new Vector(pB.getX() - pA.getX(), pB.getY() - pA.getY());
    }

    /**
     * Returns the angle in radians between this vector and the vector parameter; the return value is constrained to the range
     * [0,PI].
     *
     * @param pOther - the other vector
     * @return the angle in radians in the range [0,PI]
     */
    public final double angle(final Vector pOther) {
        final Vector2d me = new Vector2d(getX(), getY());
        final Vector2d other = new Vector2d(pOther.getX(), pOther.getY());
        return me.angle(other);
    }

    public Vector getANormal() {
        return new Vector(getY(), -getX());
    }

    public Vector getNormal() {
        return getANormal().normalize();
    }

    public double gradient() {
        return getY() / getX();
    }

    @Override
    public Vector multiply(final double pScale) {
        return new Vector(getX() * pScale, getY() * pScale);
    }

    /**
     * Normalize.
     *
     * @return the vector
     */
    public Vector normalize() {
        double length = length();
        Vector rv = divide(length).toVector();
        return rv;
    }

    @Override
    public String toString() {
        return "Vector (" + getX() + ", " + getY() + ")";
    }
}
