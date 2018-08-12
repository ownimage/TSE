/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.control.grafitti;

public class GraphicsHelper {


    //
	// public static final Object TAB_SELECTED = new Object();
	//
	// private final com.ownimage.perception.ui.graphics.IGraphics mGraphics;
	// private final ControlSelector mControlSelector;
	// private final Vector<Object> mOtherInfo = new Vector<Object>();
	//
	// public GraphicsHelper(final ControlSelector pControlSelector, final IGraphics pGraphics) {
	// mControlSelector = pControlSelector;
	// mGraphics = pGraphics;
	// }
	//
	// public GraphicsHelper(final GraphicsHelper pGraphicsHelper) {
	// mControlSelector = pGraphicsHelper.mControlSelector;
	// mGraphics = pGraphicsHelper.mGraphics;
	// }
	//
	// public GraphicsHelper addOtherInfo(final Object pOtherInfo) {
	// mOtherInfo.add(pOtherInfo);
	// return this;
	// }
	//
	// public boolean containsOtherInfo(final Object pOtherInfo) {
	// return mOtherInfo.contains(pOtherInfo);
	// }
	//
	// public void drawCircle(final double pX, final double pY, final double pRadius, final Color pColor) {
	// drawCircle(pX, pY, pRadius, pColor, false);
	// }
	//
	// public void drawCircle(final double pX, final double pY, final double pRadius, final Color pColor, final boolean pDashed) {
	// mGraphics.drawCircle(pX, pY, pRadius, pColor, pDashed);
	// }
	//
	// public void drawCircle(final Point pCentre, final double pRadius, final Color pColor) {
	// drawCircle(pCentre.getX(), pCentre.getY(), pRadius, pColor, false);
	// }
	//
	// public void drawCircle(final Point pCentre, final double pRadius, final Color pColor, final boolean pIsSelected) {
	// drawCircle(pCentre.getX(), pCentre.getY(), pRadius, pColor, pIsSelected);
	// }
	//
	// public void drawCircleWithLabel(final double pX, final double pY, final double pRadius, final String pLabel, final Color
	// pColor) {
	// drawCircle(pX, pY, pRadius, pColor, false);
	// drawString(pLabel, pX, pY - pRadius);
	// }
	//
	// public void drawCircleWithLabel(final double pX, final double pY, final double pRadius, final String pLabel, final Color
	// pColor, final boolean pIsSelected) {
	// drawCircle(pX, pY, pRadius, pColor, pIsSelected);
	// drawString(pLabel, pX, pY - pRadius);
	// }
	//
	// public void drawCircleWithLabel(final Point pCentre, final double pRadius, final String pLabel, final Color pColor) {
	// drawCircleWithLabel(pCentre.getX(), pCentre.getY(), pRadius, pLabel, pColor, false);
	// }
	//
	// public void drawCircleWithLabel(final Point pCentre, final double pRadius, final String pLabel, final Color pColor, final
	// boolean pIsSelected) {
	// drawCircleWithLabel(pCentre.getX(), pCentre.getY(), pRadius, pLabel, pColor, pIsSelected);
	// }
	//
	// public void drawCrossHairs(final Point pPoint, final Color pColor, final double pSize) {
	// drawHorizontalLine(pPoint.getY(), pPoint.getX() - pSize / 2.0d, pPoint.getX() + pSize / 2.0d, pColor);
	// drawVerticalLine(pPoint.getX(), pPoint.getY() - pSize / 2.0d, pPoint.getY() + pSize / 2.0d, pColor);
	// }
	//
	// public void drawCrossHairs(final Point pPoint, final double mSize) {
	// drawCrossHairs(pPoint, getGrafitiColor1(), mSize);
	// }
	//
	// public void drawFilledRectangle(final double pX1, final double pY1, final double pX2, final double pY2, final Color pColor) {
	// mGraphics.drawFilledRectangle(pX1, pY1, pX2, pY2, pColor);
	// }
	//
	// public final void drawHorizontalLine(final double pY, final Color pC) {
	// drawHorizontalLine(pY, 0.0d, 1.0d, pC, false);
	// }
	//
	// public final void drawHorizontalLine(final double pY, final Color pC, final boolean pDashed) {
	// drawHorizontalLine(pY, 0.0d, 1.0d, pC, pDashed);
	// }
	//
	// public final void drawHorizontalLine(final double pY, final double pX1, final double pX2, final Color pC) {
	// drawHorizontalLine(pY, pX1, pX2, pC, false);
	// }
	//
	// public final void drawHorizontalLine(final double pY, final double pX1, final double pX2, final Color pColor, final boolean
	// pDashed) {
	// drawLine(pX1, pY, pX2, pY, pColor, pDashed);
	// }
	//
	// public final void drawHorizontalLine(final IControlPrimative<?> pControl, final Color pC) {
	// drawHorizontalLine(pControl.getNormalizedValue(), 0.0d, 1.0d, pC, mControlSelector.isControlSelected(pControl));
	// }
	//
	// public void drawHorizontalWithLabel(final IControlPrimative<Double> pControl, final Color pColor) {
	// drawHorizontalLine(pControl.getNormalizedValue(), pColor, mControlSelector.isControlSelected(pControl));
	// drawString(pControl, 0.0d, pControl.getNormalizedValue());
	// }
	//
	// public void drawLine(final double pX1, final double pY1, final double pX2, final double pY2, final Color pColor, final
	// boolean pDashed) {
	// mGraphics.drawLine(pX1, pY1, pX2, pY2, pColor, pDashed);
	// }
	//
	// public void drawLine(final LineSegment pLineSegment, final Color pColor) {
	// drawLine(pLineSegment.getA().getX(), pLineSegment.getA().getY(), pLineSegment.getB().getX(), pLineSegment.getB().getY(),
	// pColor, false);
	// }
	//
	// public void drawLine(final LineSegment pLineSegment, final Color pColor, final boolean pDashed) {
	// drawLine(pLineSegment.getA().getX(), pLineSegment.getA().getY(), pLineSegment.getB().getX(), pLineSegment.getB().getY(),
	// pColor, pDashed);
	// }
	//
	// public void drawLine(final Point pA, final Point pB, final Color pColor) {
	// drawLine(pA.getX(), pA.getY(), pB.getX(), pB.getY(), pColor, false);
	// }
	//
	// public void drawLine(final Point pA, final Point pB, final Color pColor, final boolean pDashed) {
	// drawLine(pA.getX(), pA.getY(), pB.getX(), pB.getY(), pColor, pDashed);
	// }
	//
	// public void drawLine(final PointControl pA, final PointControl pB, final Color pColor) {
	// drawLine(pA.getPoint(), pB.getPoint(), pColor);
	// }
	//
	// public void drawLine(final PointControl pA, final PointControl pB, final Color pColor, final boolean pDashed) {
	// drawLine(pA.getPoint(), pB.getPoint(), pColor, pDashed);
	// }
	//
	// public void drawPath(final Path pPath, final Color pColor) {
	// drawPath(pPath, pColor, false);
	// }
	//
	// public void drawPath(final Path pPath, final Color pColor, final boolean pDashed) {
	// double x1 = 0.0d;
	// double y1 = 0.0d;
	// double x2 = 0.0d;
	// double y2 = 0.0d;
	//
	// for (final Path.Element element : pPath.getElements()) {
	// x1 = x2;
	// y1 = y2;
	// x2 = element.getX();
	// y2 = element.getY();
	//
	// if (element.getType() == Path.Type.LineTo) {
	// drawLine(x1, y1, x2, y2, pColor, pDashed);
	// }
	// }
	//
	// }
	//
	// public void drawPoint(final Point pPoint, final Color pColor) {
	// // TODO need to make square size a property
	// // Color color = Color.CYAN;
	// // pGraphics.drawSquare(getPoint(), 0.02d, getDisplayName(), color);
	//
	// drawLine(pPoint.getX(), pPoint.getY() - 0.02d, pPoint.getX(), pPoint.getY() + 0.02d, pColor, false);
	// drawLine(pPoint.getX() - 0.02d, pPoint.getY(), pPoint.getX() + 0.02d, pPoint.getY(), pColor, false);
	// }
	//
	// public void drawRectangle(final double pX1, final double pY1, final double pX2, final double pY2, final Color pColor) {
	// drawRectangle(pX1, pY1, pX2, pY2, pColor, false);
	// }
	//
	// public void drawRectangle(final double pX1, final double pY1, final double pX2, final double pY2, final Color pColor, final
	// boolean pDashed) {
	// drawLine(pX1, pY1, pX1, pY2, pColor, pDashed);
	// drawLine(pX2, pY1, pX2, pY2, pColor, pDashed);
	// drawLine(pX1, pY1, pX2, pY1, pColor, pDashed);
	// drawLine(pX1, pY2, pX2, pY2, pColor, pDashed);
	// }
	//
	// public void drawRectangleWithLabel(final String pText, final double pX1, final double pY1, final double pX2, final double
	// pY2, final Color pColor) {
	// drawRectangleWithLabel(pText, pX1, pY1, pX2, pY2, pColor, false);
	// }
	//
	// public void drawRectangleWithLabel(final String pText, final double pX1, final double pY1, final double pX2, final double
	// pY2, final Color pColor, final boolean pDashed) {
	// drawRectangle(pX1, pY1, pX2, pY2, pColor, pDashed);
	// drawString(pText, pX1, pY1);
	// }
	//
	// public final void drawSlider(final double pValue, final Orientation pOrientation, final double pPosn, final Color pColor,
	// final boolean pDashed) {
	// if (pOrientation == Orientation.Horizontal) {
	// drawHorizontalLine(pPosn, 0.0d, 1.0d, pColor, pDashed);
	// drawFilledRectangle(pValue - 0.01d, pPosn - 0.02d, pValue + 0.02d, pPosn + 0.04d, pColor);
	// } else {
	// drawVerticalLine(pPosn, 0.0d, 1.0d, pColor, pDashed);
	// drawFilledRectangle(pPosn - 0.02d, pValue - 0.01d, pPosn + 0.04d, pValue + 0.02d, pColor);
	// }
	// }
	//
	// public final void drawSlider(final IControlPrimative<?> pControl, final Orientation pOrientation, final double pPosn, final
	// Color pColor) {
	// drawSlider(pControl.getNormalizedValue(), pOrientation, pPosn, pColor, mControlSelector.isControlSelected(pControl));
	// }
	//
	// public final void drawSlider(final IControlPrimative<?> pControl, final Orientation pOrientation, final double pPosn, final
	// Color pColor, final boolean pDashed) {
	// drawSlider(pControl.getNormalizedValue(), pOrientation, pPosn, pColor, pDashed);
	// }
	//
	// public final void drawSquare(final double pX, final double pY, final double pSize, final Color pColor) {
	// drawSquare(pX, pY, pSize, null, pColor);
	// }
	//
	// public final void drawSquare(final double pX, final double pY, final double pSize, final String pLabel, final Color pColor) {
	// final double halfSize = pSize / 2.0d;
	// drawFilledRectangle(pX - halfSize, pY - halfSize, pX + halfSize, pY + halfSize, pColor);
	// if (pLabel != null) {
	// drawString(pLabel, pX - halfSize, pY - halfSize);
	// }
	// }
	//
	// public final void drawSquare(final Point pPoint, final double pSize, final String pLabel, final Color pColor) {
	// drawSquare(pPoint.getX(), pPoint.getY(), pSize, pLabel, pColor);
	// }
	//
	// public void drawString(final IControlPrimative<?> pControl, final double pX, final double pY) {
	// drawString(pControl.getDisplayName(), pX, pY);
	// }
	//
	// public void drawString(final IControlPrimative<?> pControl, final Point pPoint) {
	// drawString(pControl.getDisplayName(), pPoint.getX(), pPoint.getY());
	// }
	//
	// public void drawString(final String pText, final double pX, final double pY) {
	// mGraphics.drawString(pText, pX, pY);
	// }
	//
	// public void drawString(final String pText, final int pX, final int pY) {
	// mGraphics.drawString(pText, pX, pY);
	// }
	//
	// public void drawString(final String pText, final Point pPoint) {
	// drawString(pText, pPoint.getX(), pPoint.getY());
	// }
	//
	// public final void drawVerticalLine(final double pX, final Color pColor) {
	// drawVerticalLine(pX, 0.0d, 1.0d, pColor, false);
	// }
	//
	// public final void drawVerticalLine(final double pX, final Color pColor, final boolean pDashed) {
	// drawVerticalLine(pX, 0.0d, 1.0d, pColor, pDashed);
	// }
	//
	// public final void drawVerticalLine(final double pX, final double pY1, final double pY2, final Color pColor) {
	// drawVerticalLine(pX, pY1, pY2, pColor, false);
	// }
	//
	// public final void drawVerticalLine(final double pX, final double pY1, final double pY2, final Color pColor, final boolean
	// pDashed) {
	// drawLine(pX, pY1, pX, pY2, pColor, pDashed);
	// }
	//
	// public final void drawVerticalLine(final IControlPrimative<?> pControl, final Color pColor) {
	// drawVerticalLine(pControl.getNormalizedValue(), 0.0d, 1.0d, pColor, mControlSelector.isControlSelected(pControl));
	// }
	//
	// public void drawVerticalLineWithLabel(final IControlPrimative<Double> pControl, final Color pColor) {
	// drawVerticalLine(pControl.getNormalizedValue(), pColor, mControlSelector.isControlSelected(pControl));
	// drawString(pControl, pControl.getNormalizedValue(), 0.0d);
	// }
	//
	// public final Color getGrafitiColor1() {
	// return Perception.getProperties().getGrafitiColor1();
	// }
	//
	// public final Color getGrafitiColor2() {
	// return Perception.getProperties().getGrafitiColor2();
	// }
	//
	// public final Color getGrafitiColor3() {
	// return Perception.getProperties().getGrafitiColor3();
	// }
	//
	// public Object getOtherInfo() {
	// return mOtherInfo;
	// }
	//
	// public double getPenWidth() {
	// return mGraphics.getPenWidth();
	// }
	//
	// public GraphicsHelper removeOtherInfo(final Object pOtherInfo) {
	// mOtherInfo.remove(pOtherInfo);
	// return this;
	// }
	//
	// public void setPenWidth(final double pPenWidth) {
	// mGraphics.setPenWidth(pPenWidth);
	//
	// }
	//
	// public void translate(final double pX, final double pY) {
	// mGraphics.translate(pX, pY);
	// }
	//
	// public void translate(final Point pTranslate) {
	// translate(pTranslate.getX(), pTranslate.getY());
	// }
}
