/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.util.Vector;

import com.ownimage.perception.math.Point;

public class Path {


    public enum Type {
        MoveTo, LineTo
    }

    public class Element {
        private Point mPoint;
        private Type mType;

        public Element(Point pPoint, Type pType) {
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

    private Vector<Element> mPath = new Vector<Element>();

    public Path() {
        moveTo(0.0d, 0.0d);
    }

    public Path moveTo(double pX, double pY) {
        return moveTo(new Point(pX, pY));
    }

    public Path moveTo(Point pPoint) {
        mPath.add(new Element(pPoint, Type.MoveTo));
        return this;
    }

    public Path lineTo(double pX, double pY) {
        return lineTo(new Point(pX, pY));
    }

    public Path lineTo(Point pPoint) {
        mPath.add(new Element(pPoint, Type.LineTo));
        return this;
    }

    public Element[] getElements() {
        return mPath.toArray(new Element[mPath.size()]);
    }
}
