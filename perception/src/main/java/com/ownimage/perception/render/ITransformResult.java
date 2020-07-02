/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import java.awt.*;

import com.ownimage.framework.math.Point;

 public interface ITransformResult {

     float getA();

     float getB();

     Color getColor();

     float getG();

     Point getPoint();

     float getR();

     double getX();

     double getY();

     void setColor(Color pColor);

     void setPoint(Point pPoint);

     void setRGBA(float pR, float pG, float pB, float pA);

     void setX(double pX);

     void setXY(double pX, double pY);

     void setY(double pY);

}