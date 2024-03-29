/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.PointControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Rectangle;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;
import lombok.NonNull;
import lombok.val;

import java.util.logging.Logger;

public class CropTransform extends BaseTransform implements IControlValidator {


    private final static Logger mLogger = Framework.getLogger();

    /**
     * The Isolated value indicates that this is used independently of the main transform stack. Specifically this means that when
     * the transforms control values change then the super controlChangeEvent will NOT be called.
     */
    private final boolean mIndependent;

    private double mBottom;
    private double mTop;
    private double mLeft;
    private double mRight;

    private final DoubleControl mBottomControl;
    private final DoubleControl mTopControl;
    private final DoubleControl mLeftControl;
    private final DoubleControl mRightControl;
    private final PointControl mMoveControl;

    public CropTransform(final Perception pPerception) {
        this(pPerception, false);
    }

    public CropTransform(final Perception pPerception, final boolean pIndependent) {
        super("Crop", "crop");

        mIndependent = pIndependent;

        mBottomControl = new DoubleControl("Bottom", "bottom", getContainer(), 0.1d);
        mTopControl = new DoubleControl("Top", "top", getContainer(), 0.9d);
        mLeftControl = new DoubleControl("Left", "left", getContainer(), 0.1d);
        mRightControl = new DoubleControl("Right", "right", getContainer(), 0.9d);
        mMoveControl = new PointControl("Move", "move", NullContainer.NullContainer,
                new Point(mLeftControl.getValue(), mBottomControl.getValue()));
        setValues();

        mBottomControl.addControlValidator(this);
        mTopControl.addControlValidator(this);
        mLeftControl.addControlValidator(this);
        mRightControl.addControlValidator(this);


        addYControl(mTopControl);
        addXControl(mLeftControl);
        addXControl(mRightControl);
        addYControl(mBottomControl);
        addXYControl(mMoveControl);
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (isNotMutating()) {
            if (isIndependent()) { // supress the update to the base component which updates the main preview
                setValues();
                redrawGrafitti();
            } else {
                super.controlChangeEvent(pControl, pIsMutating);
            }
        }
    }

    private boolean isIndependent() {
        return mIndependent;
    }

    @Override
    public void graffiti(final GrafittiHelper pGrafittiHelper) {
        val allSelected = isControlSelected(mMoveControl);
        pGrafittiHelper.drawHorizontalLine(mTop, getGrafitiColor1(), allSelected || isControlSelected(mTopControl));
        pGrafittiHelper.drawHorizontalLine(mBottom, getGrafitiColor1(), allSelected || isControlSelected(mBottomControl));
        pGrafittiHelper.drawVerticalLine(mLeft, getGrafitiColor1(), allSelected || isControlSelected(mLeftControl));
        pGrafittiHelper.drawVerticalLine(mRight, getGrafitiColor1(), allSelected || isControlSelected(mRightControl));
    }

    public void setCrop(final double pLeft, final double pBottom, final double pRight, final double pTop) {
        try {
            setMutating(true);
            mLeftControl.setValue(pLeft);
            mBottomControl.setValue(pBottom);
            mRightControl.setValue(pRight);
            mTopControl.setValue(pTop);
            setValues();
        } finally {
            setMutating(false);
        }
    }

    public void setCrop(final Rectangle pBounds) {
        setCrop(pBounds.getLeft(), pBounds.getBottom(), pBounds.getRight(), pBounds.getTop());
    }

    @Override
    public void setValues() {
        setMutating(true);
        try {
            if (isControlSelected(mMoveControl)) {
                double w = mRight - mLeft;
                double h = mTop - mBottom;
                double x = Math.min(mMoveControl.getValue().getX(), 1.0d - w);
                double y = Math.min(mMoveControl.getValue().getY(), 1.0d - h);
                mBottom = KMath.limit01(y);
                mTop = KMath.limit01(y + h);
                mLeft = KMath.limit01(x);
                mRight = KMath.limit01(x + w);
                mBottomControl.setValue(mBottom);
                mTopControl.setValue(mTop);
                mLeftControl.setValue(mLeft);
                mRightControl.setValue(mRight);
                mMoveControl.setValue(new Point(mLeftControl.getValue(), mBottomControl.getValue()));
            } else {
                mBottom = KMath.limit01(mBottomControl.getValue());
                mTop = KMath.limit01(mTopControl.getValue());
                mLeft = KMath.limit01(mLeftControl.getValue());
                mRight = KMath.limit01(mRightControl.getValue());
                mMoveControl.setValue(new Point(mLeftControl.getValue(), mBottomControl.getValue()));
            }
        } finally {
            setMutating(false);
        }
    }

    @Override
    public void transform(@NonNull final ITransformResult pRenderResult) {
        Framework.logEntry(mLogger);

        final double x = pRenderResult.getX();
        final double y = pRenderResult.getY();

        final Point point = new Point( //
                mLeft + x * (mRight - mLeft), //
                mBottom + y * (mTop - mBottom) //
        );

        pRenderResult.setPoint(point);

        Framework.logExit(mLogger);
    }

    @Override
    public boolean validateControl(final Object pControl) {
        final boolean rv = mBottomControl.getValidateValue() < mTopControl.getValidateValue() //
                && mLeftControl.getValidateValue() < mRightControl.getValidateValue();
        return rv;
    }

}
