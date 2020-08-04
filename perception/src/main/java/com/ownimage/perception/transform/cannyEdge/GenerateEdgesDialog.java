/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Rectangle;
import com.ownimage.framework.queue.ExecuteQueue;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.runWhenDirty.IRunWhenDirty;
import com.ownimage.framework.util.runWhenDirty.RunWhenDirtyFactory;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.ImmutableUIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CropTransform;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.util.logging.Logger;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

/**
 * This is an Edge Transform Control Container Dialog
 */
public class GenerateEdgesDialog extends Container implements IUIEventListener, IControlValidator {

    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private static PixelMapService pixelMapService = context.getBean(PixelMapService.class);

    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private static final int DEFAULT_SIZE = 800;

    private final CannyEdgeTransform mTransform;
    private final PictureControl mPreviewPicture;
    private PictureControl mInputPictureControl;
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

    private final IRunWhenDirty mDetector;

    public GenerateEdgesDialog(
            @NonNull CannyEdgeTransform pParent,
            String pDisplayName,
            String pPropertyName
    ) {
        super(pDisplayName, pPropertyName, Services.getServices()::getUndoRedoBuffer);

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

        mGaussianKernelRadius = new DoubleControl("Kernal Radius", "gaussianKernelRadius", mControlContainer, 5.61d, 0.1001d, 10.0d);
        mLowThreshold = new DoubleControl("Low Threshold", "lowThreshold", mControlContainer, 1.0d, 0.0d, 100.0d);
        mHighThreshold = new DoubleControl("High Threshold", "highThreshold", mControlContainer, 1.0d, 0.0d, 100.0d);
        mGaussianKernelWidth = new IntegerControl("Gaussian Kernal Width", "gausianKernelWidth", mControlContainer, 13, 2, 15, 1);
        mContrastNormalized = new BooleanControl("Contrast Normalized", "contrastNormalized", mControlContainer, false);

        if (mTransform.isInitialized()) {
            updatePreview();
        }

        mDetector = RunWhenDirtyFactory.createOnNewThread(this::runDetector);
    }

    @Override
    public void controlChangeEvent(@NotNull IControl<?,?,?,?> pControl, boolean pIsMutating) {
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
            if (pControl.isOneOf(mPreviewPositionX, mPreviewPositionY, mPreviewSize)) {
                updatePreview();
            }
            if (pControl.isOneOf(mHighThreshold, mLowThreshold, mGaussianKernelRadius,
                    mGaussianKernelWidth, mContrastNormalized)) {
                mDetector.markDirty();
            }
        }
    }

    public ICannyEdgeDetector createCannyEdgeDetector(CannyEdgeDetectorFactory.Type type) {
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

    private void generatePreviewPictureFromData(ImmutablePixelMap pPixelMap) {
        //SplitTimer.split("generatePreviewPictureFromData(final PixelMap pPixelMap) start");
        int size = getSize();

        PictureType preview;
        if (mPreviewPicture == null || mPreviewPicture.getWidth() != size
                || mPreviewPicture.getWidth() != size) {
            preview = new PictureType(size, size);
        } else {
            preview = mPreviewPicture.getValue().createCompatible();
        }

        Color foreground = getProperties().getPixelMapFGColor();
        Color background = getProperties().getPixelMapBGColor();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (pixelMapService.getPixelAt(pPixelMap,x, y).isEdge(pPixelMap)) {
                    preview.setColor(x, y, foreground);
                } else {
                    preview.setColor(x, y, background);
                }
            }
        }
        mPreviewPicture.setValue(preview);
        //SplitTimer.split("generatePreviewPictureFromData(final PixelMap pPixelMap) end");
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
        return new Rectangle(x1, y1, x2, y2);
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
    public void mouseDragEndEvent(ImmutableUIEvent pEvent) {
        mTransform.redrawGrafitti();
        updatePreview();
    }

    @Override
    public void mouseDragEvent(ImmutableUIEvent pEvent) {
        if (pEvent.getDeltaX().isEmpty() || pEvent.getDeltaY().isEmpty()) {
            mLogger.severe(pEvent.toString());
            throw new RuntimeException("this does not look like dragEvent no getDeltaX or getDeltaY");
        }

        mPreviewPositionX.setValue(mDragStart.getX() + pEvent.getDeltaX().get());
        mPreviewPositionY.setValue(mDragStart.getY() + pEvent.getDeltaY().get());
        mTransform.redrawGrafitti();
    }

    @Override
    public void mouseDragStartEvent(ImmutableUIEvent pEvent) {
        mDragStart = new IntegerPoint(mPreviewPositionX.getValue(), mPreviewPositionY.getValue());
    }

    public void showDialog(ActionControl pOk, ActionControl pCancel, IAction pCompleteFunction) {
        var dialogOptions = DialogOptions.builder().withCompleteFunction(pCompleteFunction).build();
        Services.getServices().getPerception().showDialog(this, dialogOptions, getUndoRedoBuffer(), pCancel, pOk);
        updatePreview();
    }

    private PictureType updatePreview() {
        SplitTimer.split("updatePreview() start");
        int size = getSize();
        PictureType inputPicture = new PictureType(size, size);
        mInputPictureControl = new PictureControl("InputPicture", "inputPicture", NullContainer, inputPicture);
        CropTransform crop = new CropTransform(Services.getServices().getPerception(), true);
        crop.setPreviousTransform(getTransform().getPreviousTransform());
        crop.setCrop(getPreviewRectangle());
        Services.getServices().getRenderService().
                getRenderJobBuilder("GenerateEdgesDialog::updatePreview", mInputPictureControl, crop)
                .withCompleteAction(this::runDetector)
                .build()
                .run();
        mLogger.info(() -> "ExecuteQueue depth:" + ExecuteQueue.getInstance().getDepth());
        mLogger.finest("at end");
        return mPreviewPicture.getValue();
    }

    private void runDetector() {
        SplitTimer.split("updatePreview(final PictureType pInputPicture) start");
        ICannyEdgeDetector detector = createCannyEdgeDetector(CannyEdgeDetectorFactory.Type.OPENCL);
        try {
            detector.setSourceImage(mInputPictureControl.getValue());
            detector.process(null);

            if (detector.getKeepRunning()) {
                // only set the mData if the detector was allowed to finish
                generatePreviewPictureFromData(detector.getEdgeData());
                // mPreviewControl.getValue().setValue(mPreviewPicture);
            }
        } finally {
            if (detector != null) {
                detector.dispose();
            }
            SplitTimer.split("updatePreview(final PictureType pInputPicture) end");
        }
    }

    @Override
    public boolean validateControl(Object pControl) {
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
