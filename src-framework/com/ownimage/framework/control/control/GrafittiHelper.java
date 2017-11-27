package com.ownimage.framework.control.control;

import java.awt.Color;

import com.ownimage.framework.view.IGrafittiImp;
import com.ownimage.perception.control.grafitti.Path;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Rectangle;

public class GrafittiHelper {

	public enum Orientation {
		Horizontal, Vertical
	}

	private final IGrafittiImp mGrafittiImp;

	public GrafittiHelper(final IGrafittiImp pGrafittiImp) {
		mGrafittiImp = pGrafittiImp;
	}

	public void drawCircle(final double pX, final double pY, final double pRadius,
			final Color pColor, final boolean pDashed) {
		mGrafittiImp.drawCircle(pX, pY, pRadius, pColor, pDashed);
	}

	public void drawFilledRectangle(final double pX1, final double pY1, final double pX2,
			final double pY2, final Color pColor) {
		mGrafittiImp.drawFilledRectangle(pX1, pY1, pX2, pY2, pColor);
	}

	public void drawFilledRectangle(final Point pBottomLeft, final Point pTopRight,
			final Color pColor) {
		Rectangle r = new Rectangle(pBottomLeft, pTopRight);
		drawFilledRectangle(r, pColor);
	}

	public void drawFilledRectangle(final Rectangle pR, final Color pColor) {
		drawFilledRectangle(pR.getX1(), pR.getY1(), pR.getX2(), pR.getY2(), pColor);
	}

	public void drawHorizontalLine(final double pY, final Color pColor) {
		drawHorizontalLine(pY, pColor, false);
	}

	public void drawHorizontalLine(final double pY, final Color pColor, final boolean pDashed) {
		mGrafittiImp.drawLine(0.0d, pY, 1.0d, pY, pColor, pDashed);
	}

	public void drawLine(final double pX1, final double pY1, final double pX2, final double pY2,
			final Color pColor, final boolean pDashed) {
		mGrafittiImp.drawLine(pX1, pY1, pX2, pY2, pColor, pDashed);
	}

	public void drawLine(final Point pPoint1, final Point pPoint2, final Color pColor) {
		drawLine(pPoint1, pPoint2, pColor, false);

	}

	public void drawLine(final Point pPoint1, final Point pPoint2, final Color pColor,
			final boolean pDashed) {
		drawLine(pPoint1.getX(), pPoint1.getY(), pPoint2.getX(), pPoint2.getY(), pColor, pDashed);
	}

	public void drawPath(final Path pPath, final Color pValue) {
		drawPath(pPath, pValue, false);
	}

	public void drawPath(final Path pPath, final Color pColor, final boolean pDashed) {
		double x1 = 0.0d;
		double y1 = 0.0d;
		double x2 = 0.0d;
		double y2 = 0.0d;

		for (final Path.Element element : pPath.getElements()) {
			x1 = x2;
			y1 = y2;
			x2 = element.getX();
			y2 = element.getY();

			if (element.getType() == Path.Type.LineTo) {
				drawLine(x1, y1, x2, y2, pColor, pDashed);
			}
		}
	}

	public void drawRectangle(final double pX1, final double pY1, final double pX2,
			final double pY2, final Color pColor) {
		drawRectangle(pX1, pY1, pX2, pY2, pColor, false);
	}

	public void drawRectangle(final double pX1, final double pY1, final double pX2,
			final double pY2, final Color pColor, final boolean pDashed) {
		mGrafittiImp.drawLine(pX1, pY1, pX2, pY1, pColor, pDashed);
		mGrafittiImp.drawLine(pX2, pY1, pX2, pY2, pColor, pDashed);
		mGrafittiImp.drawLine(pX2, pY2, pX1, pY2, pColor, pDashed);
		mGrafittiImp.drawLine(pX1, pY2, pX1, pY1, pColor, pDashed);
	}

	public void drawRectangle(final Point pBottomLeft, final Point pTopRight, final Color pColor) {
		drawRectangle(pBottomLeft, pTopRight, pColor, false);
	}

	public void drawRectangle(final Point pBottomLeft, final Point pTopRight, final Color pColor,
			final boolean pDashed) {
		Rectangle r = new Rectangle(pBottomLeft, pTopRight);
		drawRectangle(r, pColor, pDashed);
	}

	public void drawRectangle(final Rectangle pR, final Color pColor) {
		drawRectangle(pR.getX1(), pR.getY1(), pR.getX2(), pR.getY2(), pColor, false);
	}

	public void drawRectangle(final Rectangle pR, final Color pColor, final boolean pDashed) {
		drawRectangle(pR.getX1(), pR.getY1(), pR.getX2(), pR.getY2(), pColor, pDashed);
	}

	public void drawSlider(final double pValue, final Orientation pOrientation, final double pPosn,
			final Color pColor,
			final boolean pDashed) {
		double width = 0.02d;
		double height = 0.01d;
		if (pOrientation == Orientation.Horizontal) {
			drawHorizontalLine(pPosn, pColor, pDashed);
			drawFilledRectangle(pValue - height, pPosn - width, pValue + height, pPosn + width,
					pColor);
		} else {
			drawVerticalLine(pPosn, pColor, pDashed);
			drawFilledRectangle(pPosn - width, pValue - height, pPosn + width, pValue + height,
					pColor);
		}
	}

	public final void drawSquare(final double pX, final double pY, final double pSize,
			final String pLabel, final Color pColor) {
		final double halfSize = pSize / 2.0d;
		if (pLabel != null) {
			drawString(pLabel, pX - halfSize, pY - halfSize);
		}
		drawFilledRectangle(pX - halfSize, pY - halfSize, pX + halfSize, pY + halfSize, pColor);
	}

	public void drawString(final IControl pControl, final double pX, final double pY) {
		mGrafittiImp.drawString(pControl.getDisplayName(), pX, pY);
	}

	public void drawString(final IControl pControl, final Point pPoint) {
		drawString(pControl, pPoint.getX(), pPoint.getY());

	}

	public void drawString(final String pLabel, final double pX, final double pY) {
		mGrafittiImp.drawString(pLabel, pX, pY);
	}

	public void drawVerticalLine(final double pX, final Color pColor) {
		drawVerticalLine(pX, pColor, false);
	}

	public void drawVerticalLine(final double pX, final Color pColor, final boolean pDashed) {
		mGrafittiImp.drawLine(pX, 0.0d, pX, 1.0d, pColor, pDashed);
	}

	public void drawVerticalLineWithLabel(final DoubleControl pControl, final Color pColor) {
		drawVerticalLineWithLabel(pControl, pColor, false);
	}

	public void drawVerticalLineWithLabel(final DoubleControl pControl, final Color pColor,
			final boolean pDashed) {
		double x = pControl.getNormalizedValue();
		drawVerticalLine(x, pColor, pDashed);
		drawString(pControl, x, 0);
	}

	public void setFontSize(final double pFontSize) {
		mGrafittiImp.setFontSize(pFontSize);
	}

	public void setPenWidth(final double pPenWidth) {
		mGrafittiImp.setPenWidth(pPenWidth);
	}

}
