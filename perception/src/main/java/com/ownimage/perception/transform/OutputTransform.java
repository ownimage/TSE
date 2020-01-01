/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;

import java.util.logging.Logger;

public class OutputTransform extends BaseTransform {

    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

    private final BooleanControl mUseCustomSize;
    private final IntegerControl mWidth;
    private final IntegerControl mHeight;
    private final ObjectControl<Size> mOutputSize;
    private final IntegerControl mDPI;
    private final IntegerControl mOversample;

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

        private final String mName;
        private final double mHeight;
        private final double mWidth;
        private final Unit mUnit;

        public enum Unit {
            MM, INCH, PIXEL
        }

        Size(final String pName, final double pWidth, final double pHeight, final Unit pUnit) {
            mName = pName;
            mHeight = pHeight;
            mWidth = pWidth;
            mUnit = pUnit;
        }

        public String getName() {
            return mName;
        }

        public int getHeight(final int pDPI) {
            return getSize(mHeight, pDPI);
        }

        public int getWidth(final int pDPI) {
            return getSize(mWidth, pDPI);
        }

        private int getSize(final double pLength, final int pDPI) {
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

        final IntegerMetaType outputMetaModel = new IntegerMetaType(100, 20000, 50);
        final IntegerMetaType dpiMetaModel = new IntegerMetaType(30, 1000, 50);
        final IntegerMetaType oversampleMetaModel = new IntegerMetaType(1, 3, 1);

        new ActionControl("Original Size", "originalSize", getContainer(), this::useOriginalSize);
        new ActionControl("Last Size", "lastSize", getContainer(), this::useLastSize);
        mUseCustomSize = new BooleanControl("Use custom size", "useCustomSize", getContainer(), false);
        mWidth = new IntegerControl("Image out width", "imageOutWidth", getContainer(), 1000, outputMetaModel).setEnabled(false);
        mHeight = new IntegerControl("Image out height", "imageOutHeight", getContainer(), 1000, outputMetaModel).setEnabled(false);
        mOutputSize = new ObjectControl<>("Size", "size", getContainer(), Size.A4Portrait, Size.values());
        mDPI = new IntegerControl("DPI", "dpi", getContainer(), 30, dpiMetaModel);
        mOversample = new IntegerControl("Oversample", "oversample", getContainer(), 1, oversampleMetaModel);
        setValues();
    }

    private void useOriginalSize() {
        setMutating(true);
        mUseCustomSize.setValue(true);
        mWidth.setValue(getFirstTransform().getWidth());
        setMutating(false);
        mHeight.setValue((getFirstTransform().getHeight()));
    }

    private void useLastSize() {
        setMutating(true);
        mUseCustomSize.setValue(true);
        mWidth.setValue(getPreviousTransform().getWidth());
        setMutating(false);
        mHeight.setValue((getPreviousTransform().getHeight()));
    }

    @Override
    public void setValues() {
        if (isNotMutating()) {
            if (!mUseCustomSize.getValue()) {
                try {
                    setMutating(true);
                    final String sizeString = mOutputSize.getString();
                    final int dpi = mDPI.getValue();
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
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (isNotMutating()) {
            super.controlChangeEvent(pControl, pIsMutating);
        }
    }

    private void setUIState() {
        final boolean useCustomSize = mUseCustomSize.getValue();
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
