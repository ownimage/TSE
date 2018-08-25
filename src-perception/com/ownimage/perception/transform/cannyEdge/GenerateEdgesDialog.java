/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.perception.transform.cannyEdge;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.awt.*;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.queue.ExecuteQueue;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.math.IntegerPoint;
import com.ownimage.perception.math.Rectangle;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CropTransform;

/**
 * This is an Edge Transform Control Container Dialog
 */
public class GenerateEdgesDialog extends Container implements IUIEventListener, IControlValidator {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private static int DEFAULT_SIZE = 200;

    private final CannyEdgeTransform mTransform;
    private final PictureControl mPreviewPicture;
    private final IntegerControl mPreviewSize;

    private final IntegerControl mPreviewPositionX;
    private final IntegerControl mPreviewPositionY;

    private final DoubleControl mGaussianKernelRadius;
    private final DoubleControl mLowThreshold;
    private final DoubleControl mHighThreshold;

    private final IntegerControl mGaussianKernelWidth;
    private final BooleanControl mContrastNormalized;
    private final Container mPreviewContainer;
    private final Container mControlContainer;

    private IntegerPoint mDragStart = IntegerPoint.IntegerPoint00;

    public GenerateEdgesDialog(final CannyEdgeTransform pParent, final String pDisplayName,
                               final String pPropertyName) {
        super(pDisplayName, pPropertyName, Services.getServices()::getUndoRedoBuffer);
        Framework.checkParameterNotNull(mLogger, pParent, "pParent");

        mTransform = pParent;

        mPreviewContainer = new Container("Preview Container", "previewContainer", this, this);
        mPreviewSize = new IntegerControl("Preview Size", "previewSize", mPreviewContainer, getDefaultSize(), 100, 1000, 50);
        mPreviewSize.addControlChangeListener(this);
        mPreviewPicture = new PictureControl("Preview", "preview", mPreviewContainer, new PictureType(getDefaultSize(), getDefaultSize()));
        mPreviewPicture.setUIListener(this);

        mControlContainer = new Container("ControlContainer", "controlContainer", this, this);
        mControlContainer.addControlChangeListener(this);
        mControlContainer.addControlValidator(this);

        mPreviewPositionX = new IntegerControl("Preview Position X", "previewPositionX", mControlContainer, 0, 0, mTransform.getWidth(), 10).setEnabled(false);
        mPreviewPositionY = new IntegerControl("Preview Position Y", "previewPositionY", mControlContainer, 0, 0, mTransform.getHeight(), 10).setEnabled(false);

        mGaussianKernelRadius = new DoubleControl("Kernal Radius", "gaussianKernelRadius", mControlContainer, 0.2d, 0.1001d, 10.0d);
        mLowThreshold = new DoubleControl("Low Threshold", "lowThreshold", mControlContainer, 1.0d, 0.0d, 100.0d);
        mHighThreshold = new DoubleControl("High Threshold", "highThreshold", mControlContainer, 1.0d, 0.0d, 100.0d);
        mGaussianKernelWidth = new IntegerControl("Gaussian Kernal Width", "gausianKernelWidth", mControlContainer, 2, 2, 15, 1);
        mContrastNormalized = new BooleanControl("Contrast Normalized", "contrastNormalized", mControlContainer, false);

        if (mTransform.isInitialized()) {
            updatePreview();
        }
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        mLogger.fine(String.format("CannyEdgeTransform.ETControlContainerDialog::controlChangeEvent(%s, %s)"
                , pControl == null ? "null" : pControl.getDisplayName()
                , pIsMutating
        ));

        if (pControl == mPreviewPositionX || pControl == mPreviewPositionY || pControl == mPreviewPicture) {
            // ignore these preview position updates are handled my the mouse drag event
            return;
        }
        if (getTransform().isInitialized()) {
            if (pControl == mPreviewSize) {
                getTransform().redrawGrafitti();
            }
            mPreviewPicture.setValue(updatePreview());
        }
    }

    public ICannyEdgeDetector createCannyEdgeDetector(final CannyEdgeDetectorFactory.Type type) {
        ICannyEdgeDetector detector = CannyEdgeDetectorFactory.createInstance(getTransform(), type);
        detector.setGaussianKernelRadius(mGaussianKernelRadius.getValue().floatValue());
        detector.setLowThreshold(mLowThreshold.getValue().floatValue() / 100.0f);
        detector.setHighThreshold(mHighThreshold.getValue().floatValue() / 100.0f);
        detector.setGaussianKernelWidth(mGaussianKernelWidth.getValue());
        detector.setContrastNormalized(mContrastNormalized.getValue());
        return detector;
    }

    @Override
    public IView createView() {
        HFlowLayout hflow = new HFlowLayout(mPreviewContainer, mControlContainer);
        IView view = ViewFactory.getInstance().createView(hflow);
        addView(view);
        return view;
    }

    private void generatePreviewPictureFromData(final PixelMap pEdgeData) {
        SplitTimer.split("generatePreviewPictureFromData(final PixelMap pEdgeData) start");
        final int size = getSize();

        PictureType preview;
        if (mPreviewPicture == null || mPreviewPicture.getWidth() != size
                || mPreviewPicture.getWidth() != size) {
            preview = new PictureType(size, size);
        } else {
            preview = mPreviewPicture.getValue().createCompatible();
        }

        final Color foreground = getProperties().getPixelMapFGColor();
        final Color background = getProperties().getPixelMapBGColor();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (pEdgeData.getPixelAt(x, y).isEdge()) {
                    preview.setColor(x, y, foreground);
                } else {
                    preview.setColor(x, y, background);
                }
            }
        }
        mPreviewPicture.setValue(preview);
        SplitTimer.split("generatePreviewPictureFromData(final PixelMap pEdgeData) end");
    }

    private int getDefaultSize() {
        return DEFAULT_SIZE;
    }

    private int getPreviewPositionX() {
        return mPreviewPositionX.getValue();
    }

    private int getPreviewPositionY() {
        return mPreviewPositionY.getValue();
    }

    public Rectangle getPreviewRectangle() {
        double x1 = (double) getPreviewPositionX() / getTransform().getWidth();
        double y1 = (double) getPreviewPositionY() / getTransform().getHeight();
        double x2 = (double) (getPreviewPositionX() + getSize()) / getTransform().getWidth();
        double y2 = (double) (getPreviewPositionY() + getSize()) / getTransform().getHeight();
        Rectangle r = new Rectangle(x1, y1, x2, y2);
        return r;
    }

    private Properties getProperties() {
        return Services.getServices().getProperties();
    }

    private int getSize() {
        return (mPreviewSize != null) ? mPreviewSize.getValue() : getDefaultSize();
    }

    private CannyEdgeTransform getTransform() {
        return mTransform;
    }

    @Override
    public void mouseDragEndEvent(final IUIEvent pEvent) {
        mTransform.redrawGrafitti();
        updatePreview();
    }

    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
        mPreviewPositionX.setValue(mDragStart.getX() + pEvent.getDeltaX());
        mPreviewPositionY.setValue(mDragStart.getY() + pEvent.getDeltaY());
        mTransform.redrawGrafitti();
    }

    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        mDragStart = new IntegerPoint(mPreviewPositionX.getValue(), mPreviewPositionY.getValue());
    }

    public void showDialog(final ActionControl pOk, final ActionControl pCancel) {
        Services.getServices().getPerception().showDialog(this, DialogOptions.NONE, getUndoRedoBuffer(), pCancel, pOk);
        updatePreview();
    }

    private PictureType updatePreview() {
        SplitTimer.split("updatePreview() start");
        int size = getSize();
        PictureType inputPicture = new PictureType(size, size);
        PictureControl inputPictureControl = new PictureControl("InputPicture", "inputPicture", NullContainer, inputPicture);
        CropTransform crop = new CropTransform(Services.getServices().getPerception(), true);
        crop.setPreviousTransform(getTransform().getPreviousTransform());
        crop.setCrop(getPreviewRectangle());
        Services.getServices().getPerception().getRenderService().transform(inputPictureControl, crop, () -> updatePreview(inputPictureControl.getValue()), null);
        mLogger.info(() -> "ExecuteQueue depth:" + ExecuteQueue.getInstance().getDepth());

        mLogger.finest("at end");
        return mPreviewPicture.getValue();
    }

    private void updatePreview(final PictureType pInputPicture) {
        SplitTimer.split("updatePreview(final PictureType pInputPicture) start");
        ICannyEdgeDetector detector = createCannyEdgeDetector(CannyEdgeDetectorFactory.Type.JAVA_THREADS);
        try {
            detector.setSourceImage(pInputPicture);
            detector.process(false);

            if (detector.getKeepRunning()) {
                // only set the mData if the detector was allowed to finish
                generatePreviewPictureFromData(detector.getEdgeData());
                // mPreviewControl.getValue().setValue(mPreviewPicture);
            }
        } finally {
            if (detector != null) {
                detector.dispose();
            }
        }
    }

    @Override
    public boolean validateControl(final Object pControl) {
        boolean rv = true;
        if (pControl == mPreviewPositionX) {
            rv = mPreviewPositionX.getValidateValue() + getSize() < getTransform().getWidth();
            rv |= mPreviewPositionX.getValidateValue() < mPreviewPositionX.getValue();
        } else if (pControl == mPreviewPositionY) {
            rv = mPreviewPositionY.getValidateValue() + getSize() < getTransform().getHeight();
            rv |= mPreviewPositionY.getValidateValue() < mPreviewPositionY.getValue();
        }
        return rv;
    }
}
