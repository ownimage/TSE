/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import com.ownimage.framework.math.Point;

import java.awt.*;

public interface ITransformResult {

    public float getA();

    public float getB();

    public Color getColor();

    public float getG();

    public Point getPoint();

    public float getR();

    public double getX();

    public double getY();

    public void setColor(Color pColor);

    public void setPoint(Point pPoint);

    public void setRGBA(float pR, float pG, float pB, float pA);

    public void setX(double pX);

    public void setXY(double pX, double pY);

    public void setY(double pY);

}