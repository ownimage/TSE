/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view;

import java.awt.Color;

import com.ownimage.framework.util.Version;

public interface IGrafittiImp {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	public void drawCircle(double pX, double pY, double pRadius, Color pColor, boolean pDashed);

    void clearRectangle(double pX1, double pY1, double pX2, double pY2);

    public void drawFilledRectangle(double pX1, double pY1, double pX2, double pY2, Color pColor);

	public void drawLine(double pX1, double pY1, double pX2, double pY2, Color pColor, boolean pDashed);

	public void drawString(String pLabel, double pX, double pY);

	public void setFontSize(double pFontSize);

	public void setPenWidth(double pPenWidth);

}