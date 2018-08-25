/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import java.awt.*;

import com.ownimage.framework.view.IGrafittiImp;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

public class GrafittiImp implements IGrafittiImp {

    private final GraphicsContext mGraphicsContext;
    private final double mHeight;
    private final double mWidth;

    public GrafittiImp(final GraphicsContext pGraphicsContext, final double pWidth, final double pHeight) {
        mGraphicsContext = pGraphicsContext;
        mWidth = pWidth;
        mHeight = pHeight;
    }

    private javafx.scene.paint.Color convert(final Color pColor) {
        double r = pColor.getRed() / 255.0d;
        double g = pColor.getGreen() / 255.0d;
        double b = pColor.getBlue() / 255.0d;
        double a = pColor.getAlpha() / 255.0d;
        return new javafx.scene.paint.Color(r, g, b, a);
    }

    @Override
    public void drawCircle(final double pX, final double pY, final double pRadius, final Color pColor, final boolean pDashed) {
        double x = (pX - pRadius) * mWidth;
        double y = (1.0d - pY - pRadius) * mHeight;
        double w = 2.0d * pRadius * mWidth;
        double h = 2.0d * pRadius * mHeight;
        setDashed(pDashed);
        mGraphicsContext.setStroke(convert(pColor));
        mGraphicsContext.strokeOval(x, y, w, h);
    }

    @Override
    public void clearRectangle(final double pX1, final double pY1, final double pX2, final double pY2) {
        double x = pX1 * mWidth;
        double w = (pX2 - pX1) * mWidth;
        double y = (1.0 - pY2) * mHeight;
        double h = (pY2 - pY1) * mHeight;
        mGraphicsContext.clearRect(x, y, w, h);
    }

    @Override
    public void drawFilledRectangle(final double pX1, final double pY1, final double pX2, final double pY2, final Color pColor) {
        double x = pX1 * mWidth;
        double w = (pX2 - pX1) * mWidth;
        double y = (1.0 - pY2) * mHeight;
        double h = (pY2 - pY1) * mHeight;
        mGraphicsContext.setFill(convert(pColor));
        mGraphicsContext.fillRect(x, y, w, h);
    }

    @Override
    public void drawLine(final double pX1, final double pY1, final double pX2, final double pY2, final Color pColor, final boolean pDashed) {
        double x1 = pX1 * mWidth;
        double x2 = pX2 * mWidth;
        double y1 = (1.0 - pY1) * mHeight;
        double y2 = (1.0 - pY2) * mHeight;
        setDashed(pDashed);
        mGraphicsContext.setStroke(convert(pColor));
        mGraphicsContext.strokeLine(x1, y1, x2, y2);
    }

    @Override
    public void drawString(final String pLabel, final double pX, final double pY) {
        double x = pX * mWidth;
        double y = (1.0 - pY) * mHeight;
        FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(mGraphicsContext.getFont());
        double w = fm.computeStringWidth(pLabel);
        double h = fm.getAscent() + fm.getDescent(); // fm.getLineHeight() gets the leading as well
        double d = fm.getMaxDescent();

        double xpadding = 3;
        double ypadding = 1;

        mGraphicsContext.setFill(convert(Color.WHITE));
        mGraphicsContext.fillRect(x, y - (h + 2 * ypadding), w + 2 * xpadding, h + 2 * ypadding);

        mGraphicsContext.setFill(convert(Color.BLACK));
        mGraphicsContext.fillText(pLabel, x + xpadding, y - d - ypadding);

    }

    private void setDashed(final boolean pDashed) {
        if (pDashed) {
            mGraphicsContext.setLineDashes(10.0d, 10.0d);
        } else {
            mGraphicsContext.setLineDashes(null);
        }

    }

    @Override
    public void setFontSize(final double pFontSize) {
        FontMetrics fm = Toolkit.getToolkit().getFontLoader().getFontMetrics(mGraphicsContext.getFont());
        double existingHieght = fm.getAscent() + fm.getDescent();
        double newSize = mGraphicsContext.getFont().getSize() * pFontSize / existingHieght;
        Font font = new Font(newSize);
        mGraphicsContext.setFont(font);
    }

    @Override
    public void setPenWidth(final double pPenWidth) {
        double width = pPenWidth * mWidth;
        mGraphicsContext.setLineWidth(width);
    }

}
