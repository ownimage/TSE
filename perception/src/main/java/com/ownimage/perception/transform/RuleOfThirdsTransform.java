/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.PointControl;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.*;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;
import lombok.NonNull;

import java.awt.*;
import java.util.logging.Logger;

public class RuleOfThirdsTransform extends BaseTransform {

    class ROTPointControl extends PointControl {

        private Point mSourcePoint;
        private Quadrilateral mQuadrilateral;

        public ROTPointControl(final String pDisplayName, final String pPropertyName, final Point pPoint) {
            super(pDisplayName, pPropertyName, RuleOfThirdsTransform.this.getContainer(), pPoint);
        }

        public synchronized Point getSourcePoint() {
            if (mQuadrilateral != mCropQuadrilateral || mSourcePoint == null) {
                // recalculate if quadrilateral has changed or not been caluclated before
                mQuadrilateral = mCropQuadrilateral;
                mSourcePoint = mQuadrilateral.mapToUnitSquare(getValue());
            }
            return mSourcePoint;
        }

        // @Override
        // protected void setValues() {
        // super.setValues();
        // mSourcePoint = null;
        // }
    }


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public static double OneThird = 1.0d / 3.0d;

    public static double TwoThirds = 2.0d / 3.0d;

    private final DoubleControl mGradientRatio = createDoubleControl("Gradient Ratio", "gradientRatio", 0.5d);

    private final ROTPointControl mP = new ROTPointControl("P", "p", new Point(0.3d, 0.3d));
    private final ROTPointControl mQ = new ROTPointControl("Q", "q", new Point(0.7d, 0.3d));
    private final ROTPointControl mR = new ROTPointControl("R", "r", new Point(0.7d, 0.7d));
    private final ROTPointControl mS = new ROTPointControl("S", "s", new Point(0.3d, 0.7d));

    private final DoubleControl mTL = createDoubleControl("Top Left", "topLeft", 0.8d);
    private final DoubleControl mTR = createDoubleControl("Top Right", "topRight", 0.8d);
    private final DoubleControl mBL = createDoubleControl("Bottom Left", "bottomLeft", 0.2d);
    private final DoubleControl mBR = createDoubleControl("Bottom Right", "bottomRight", 0.2d);
    private final DoubleControl mLT = createDoubleControl("Left Top", "letfTop", 0.2d);
    private final DoubleControl mLB = createDoubleControl("Left Bottom", "leftBottom", 0.2d);
    private final DoubleControl mRT = createDoubleControl("Right Top", "rightTop", 0.8d);
    private final DoubleControl mRB = createDoubleControl("Right Bottom", "rightBottom", 0.8d);

    Quadrilateral mCropQuadrilateral;

    public RuleOfThirdsTransform(final Perception pPerception) {
        super("Rule of Thirds", "ruleOfThirds");

        setValues();

        addXYControl(mP);
        addXYControl(mQ);
        addXYControl(mR);
        addXYControl(mS);

        addYControl(mTL);
        addYControl(mTR);
        addYControl(mBL);
        addYControl(mBR);

        addXControl(mLT);
        addXControl(mLB);
        addXControl(mRT);
        addXControl(mRB);
    }

    private DoubleControl createDoubleControl(final String pDisplayName, final String pPropertyName, final double pValue) {
        return new DoubleControl(pDisplayName, pPropertyName, getContainer(), pValue);
    }

    @Override
    public void graffiti(final GrafittiHelper pGrafittiHelper) {
        final boolean p = isControlSelected(mP);
        final boolean q = isControlSelected(mQ);
        final boolean r = isControlSelected(mR);
        final boolean s = isControlSelected(mS);

        pGrafittiHelper.drawLine(mP.getValue(), mQ.getValue(), getGrafitiColor1(), p || q);
        pGrafittiHelper.drawLine(mP.getValue(), mP.getValue().bottom(), getGrafitiColor1(), p);
        pGrafittiHelper.drawLine(mP.getValue(), mP.getValue().left(), getGrafitiColor1(), p);

        pGrafittiHelper.drawLine(mQ.getValue(), mR.getValue(), getGrafitiColor1(), q || r);
        pGrafittiHelper.drawLine(mQ.getValue(), mQ.getValue().right(), getGrafitiColor1(), q);
        pGrafittiHelper.drawLine(mQ.getValue(), mQ.getValue().bottom(), getGrafitiColor1(), q);

        pGrafittiHelper.drawLine(mR.getValue(), mS.getValue(), getGrafitiColor1(), r || s);
        pGrafittiHelper.drawLine(mR.getValue(), mR.getValue().top(), getGrafitiColor1(), r);
        pGrafittiHelper.drawLine(mR.getValue(), mR.getValue().right(), getGrafitiColor1(), r);

        pGrafittiHelper.drawLine(mS.getValue(), mP.getValue(), getGrafitiColor1(), s || p);
        pGrafittiHelper.drawLine(mS.getValue(), mS.getValue().top(), getGrafitiColor1(), s);
        pGrafittiHelper.drawLine(mS.getValue(), mS.getValue().left(), getGrafitiColor1(), s);

        Color color = null;

        color = isControlSelected(mTL) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(0.0d, mTL.getValue(), 0.02d, mTL.getDisplayName(), color);
        color = isControlSelected(mTR) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(1.0d, mTR.getValue(), 0.02d, mTR.getDisplayName(), color);
        pGrafittiHelper.drawLine(0.0d, mTL.getValue(), 1.0d, mTR.getValue(), getGrafitiColor1(), false);

        color = isControlSelected(mBL) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(0.0d, mBL.getValue(), 0.02d, mBL.getDisplayName(), color);
        color = isControlSelected(mBR) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(1.0d, mBR.getValue(), 0.02d, mBR.getDisplayName(), color);
        pGrafittiHelper.drawLine(0.0d, mBL.getValue(), 1.0d, mBR.getValue(), getGrafitiColor1(), false);

        color = isControlSelected(mLT) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(mLT.getValue(), 1.0d, 0.02d, mLT.getDisplayName(), color);
        color = isControlSelected(mLB) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(mLB.getValue(), 0.0d, 0.02d, mLB.getDisplayName(), color);
        pGrafittiHelper.drawLine(mLB.getValue(), 0.0d, mLT.getValue(), 1.0d, getGrafitiColor1(), false);

        color = isControlSelected(mRT) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(mRT.getValue(), 1.0d, 0.02d, mRT.getDisplayName(), color);
        color = isControlSelected(mRB) ? getGrafitiColor1() : getGrafitiColor2();
        pGrafittiHelper.drawSquare(mRB.getValue(), 0.0d, 0.02d, mRB.getDisplayName(), color);
        pGrafittiHelper.drawLine(mRB.getValue(), 0.0d, mRT.getValue(), 1.0d, getGrafitiColor1(), false);
    }

    @Override
    public void setValues() {

        final Line top = new Line(new Point(0.0d, mTL.getValue()), new Point(1.0d, mTR.getValue()));
        final Line bottom = new Line(new Point(0.0d, mBL.getValue()), new Point(1.0d, mBR.getValue()));
        final Line left = new Line(new Point(mLB.getValue(), 0.0d), new Point(mLT.getValue(), 1.0d));
        final Line right = new Line(new Point(mRB.getValue(), 0.0d), new Point(mRT.getValue(), 1.0d));

        final Point cropP = left.intersect(bottom);
        final Point cropQ = right.intersect(bottom);
        final Point cropR = right.intersect(top);
        final Point cropS = left.intersect(top);
        mCropQuadrilateral = new Quadrilateral(cropP, cropQ, cropR, cropS);
    }

    @Override
    public void transform(@NonNull final ITransformResult pRenderResult) {
        Framework.logEntry(mLogger);

        final Point point = pRenderResult.getPoint();

        // Map mX
        double xLowerThird = 0.0d;
        double xUpperThird = 0.0d;

        if (point.getY() > TwoThirds) {
            xLowerThird = mS.getSourcePoint().getX();
            xUpperThird = mR.getSourcePoint().getX();
        } else if (point.getY() < OneThird) {
            xLowerThird = mP.getSourcePoint().getX();
            xUpperThird = mQ.getSourcePoint().getX();
        } else {
            xLowerThird = new Range(mP.getSourcePoint().getX(), mS.getSourcePoint().getX()).getValue(point.getY(), new Range(OneThird, TwoThirds));
            xUpperThird = new Range(mQ.getSourcePoint().getX(), mR.getSourcePoint().getX()).getValue(point.getY(), new Range(OneThird, TwoThirds));
        }

        // Map mY
        double yLowerThird = 0.0d;
        double yUpperThird = 0.0d;

        if (point.getX() > TwoThirds) {
            yLowerThird = mQ.getSourcePoint().getY();
            yUpperThird = mR.getSourcePoint().getY();
        } else if (point.getX() < OneThird) {
            yLowerThird = mP.getSourcePoint().getY();
            yUpperThird = mS.getSourcePoint().getY();
        } else {
            yLowerThird = new Range(mP.getSourcePoint().getY(), mQ.getSourcePoint().getY()).getValue(point.getX(), new Range(OneThird, TwoThirds));
            yUpperThird = new Range(mS.getSourcePoint().getY(), mR.getSourcePoint().getY()).getValue(point.getX(), new Range(OneThird, TwoThirds));
        }

        final SSpline xSpline = new SSpline(new Point(OneThird, xLowerThird), new Point(TwoThirds, xUpperThird), mGradientRatio.getValue());
        final SSpline ySpline = new SSpline(new Point(OneThird, yLowerThird), new Point(TwoThirds, yUpperThird), mGradientRatio.getValue());

        final double x = xSpline.evaluate(point.getX());
        final double y = ySpline.evaluate(point.getY());

        final Point pOut = new Point(x, y);
        final Point crop = mCropQuadrilateral.mapFromUnitSquare(pOut);

        pRenderResult.setPoint(crop);
        Framework.logExit(mLogger);
    }

}
