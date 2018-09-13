/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;

public class OutputTransform extends BaseTransform {

    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

    private BooleanControl mUseCustomSize;
    private IntegerControl mWidth;
    private IntegerControl mHeight;
    private ObjectControl<Size> mOutputSize;
    private IntegerControl mDPI;
    private IntegerControl mOversample;

    private enum Size {
        A4Portrait("A4-Portrait", 210.0d, 297.0d, Unit.MM),
        A4Landscape("A4-Landscape", 297.0d, 210.0d, Unit.MM),
        A3Portrait("A3-Portrait", 297.0d, 420.0d, Unit.MM),
        A3Landscape("A3-Landscape", 420.0d, 297.0d, Unit.MM),
        A4Square("A4-Square", 210.0d, 210.0d, Unit.MM),
        A3Square("A3-Square", 297.0d, 297.0d, Unit.MM),
        HD("HD", 1920, 1080, Unit.PIXEL),
        QHD("2560x1440", 2560, 1440, Unit.PIXEL),
        Full4K("3980x2160", 3980, 2160, Unit.PIXEL),
        Imperial20x16Landscape("20\"x16\"L", 6000, 4800, Unit.PIXEL),
        Imperial20x16Portrait("20\"x16\"P", 4800, 6000, Unit.PIXEL),
        Metric1000x500mm("1000mmx500mm", 1000, 500, Unit.MM);

        private String mName;
        private double mHeight;
        private double mWidth;
        private Unit mUnit;

        public enum Unit {
            MM, INCH, PIXEL
        }

        Size(String pName, double pWidth, double pHeight, Unit pUnit) {
            mName = pName;
            mHeight = pHeight;
            mWidth = pWidth;
            mUnit = pUnit;
        }

        public String getName() {
            return mName;
        }

        public int getHeight(int pDPI) {
            return getSize(mHeight, pDPI);
        }

        public int getWidth(int pDPI) {
            return getSize(mWidth, pDPI);
        }

        private int getSize(double pLength, int pDPI) {
            switch (mUnit) {
                case PIXEL:
                    return (int) pLength;
                case INCH:
                    return (int) pLength * pDPI;
                case MM:
                    return (int) (pDPI * pLength / 25.4d);
                default:
                    return 0;
            }
        }

        @Override
        public String toString() {
            return mName;
        }
    }

    public OutputTransform(final Perception pPerception) {
        super("Output", "output");

        IntegerMetaType outputMetaModel = new IntegerMetaType(100, 20000, 50);
        IntegerMetaType dpiMetaModel = new IntegerMetaType(30, 1000, 50);
        IntegerMetaType oversampleMetaModel = new IntegerMetaType(1, 3, 1);

        mUseCustomSize = new BooleanControl("Use custom size", "useCustomSize", getContainer(), false);
        mWidth = new IntegerControl("Image out width", "imageOutWidth", getContainer(), 1000, outputMetaModel).setEnabled(false);
        mHeight = new IntegerControl("Image out height", "imageOutHeight", getContainer(), 1000, outputMetaModel).setEnabled(false);
        mOutputSize = new ObjectControl<>("Size", "size", getContainer(), Size.A4Portrait, Size.values());
        mDPI = new IntegerControl("DPI", "dpi", getContainer(), 30, dpiMetaModel);
        mOversample = new IntegerControl("Oversample", "oversample", getContainer(), 1, oversampleMetaModel);
        setValues();
    }

    @Override
    public void setValues() {
        if (isNotMutating()) {
            if (!mUseCustomSize.getValue()) {
                try {
                    setMutating(true);
                    String sizeString = mOutputSize.getString();
                    int dpi = mDPI.getValue();
                    mHeight.setValue(mOutputSize.getValue().getHeight(dpi));
                    mWidth.setValue(mOutputSize.getValue().getWidth(dpi));
                } finally {
                    setMutating(false);
                }
            }
        }
        setUIState();
    }

    @Override
    public void controlChangeEvent(final Object pControl, final boolean pIsMutating) {
        if (isNotMutating()) {
            super.controlChangeEvent(pControl, pIsMutating);
        }
    }

    private void setUIState() {
        boolean useCustomSize = mUseCustomSize.getValue();
        mHeight.setEnabled(useCustomSize);
        mWidth.setEnabled(useCustomSize);
        mOutputSize.setEnabled(!useCustomSize);
        mDPI.setEnabled(!useCustomSize);
    }

    @Override
    public int getWidth() {
        return getUseTransform() ? mWidth.getValue() : getPreviousTransform().getWidth();
    }

    @Override
    public int getHeight() {
        return getUseTransform() ? mHeight.getValue() : getPreviousTransform().getHeight();
    }

    @Override
    public int getOversample() {
        return getUseTransform() ? mOversample.getValue() : getPreviousTransform().getOversample();
    }

    @Override
    public void transform(final ITransformResult pRenderResult) {
    }
}
