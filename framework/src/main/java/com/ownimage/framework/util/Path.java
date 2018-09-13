/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import com.ownimage.framework.math.Point;

import java.util.Vector;

public class Path {


    public enum Type {
        MoveTo, LineTo
    }

    public class Element {
        private final Point mPoint;
        private final Type mType;

        public Element(final Point pPoint, final Type pType) {
            super();
            mPoint = pPoint;
            mType = pType;
        }

        public Point getPoint() {
            return mPoint;
        }

        public double getX() {
            return mPoint.getX();
        }

        public double getY() {
            return mPoint.getY();
        }

        public Type getType() {
            return mType;
        }

    }

    private final Vector<Element> mPath = new Vector<Element>();

    public Path() {
        moveTo(0.0d, 0.0d);
    }

    public Path moveTo(final double pX, final double pY) {
        return moveTo(new Point(pX, pY));
    }

    public Path moveTo(final Point pPoint) {
        mPath.add(new Element(pPoint, Type.MoveTo));
        return this;
    }

    public Path lineTo(final double pX, final double pY) {
        return lineTo(new Point(pX, pY));
    }

    public Path lineTo(final Point pPoint) {
        mPath.add(new Element(pPoint, Type.LineTo));
        return this;
    }

    public Element[] getElements() {
        return mPath.toArray(new Element[mPath.size()]);
    }
}
