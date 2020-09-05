/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.control.ColorControl.ColorProperty;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.ScrollLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.KMath;
import com.ownimage.framework.math.RectangleSize;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.event.ImmutableUIEvent;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.render.IBatchEngine;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.render.ITransformResultBatch;
import lombok.NonNull;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public abstract class BaseTransform implements IGrafitti, ITransform, IControlChangeListener, IUIEventListener {


    private final static Logger mLogger = Framework.getLogger();

    private final String mDisplayName;
    private final String mPropertyName;
    private final Container mContainer;
    private final ProgressControl mProgressControl;
    private final BooleanControl mUseTransform;
    private final PictureControl mInputPreviewImage;

    private ITransform mPreviousTransform;

    private ControlSelector mControlSelector;

    private boolean mIsMutating = false;

    public BaseTransform(String pDisplayName, String pPropertyName) {
        Framework.logEntry(mLogger);

        mDisplayName = pDisplayName;
        mPropertyName = pPropertyName;

        mContainer = new Container(getDisplayName(), getPropertyName(), this::getUndoRedoBuffer);

        mProgressControl = ProgressControl.builder("Progress", "progress", getContainer())
                .withShowLabel(false)
                .build()
                .setVisible(false);

        mUseTransform = new BooleanControl("Use Transform", "use", mContainer, true);

        int previewSize = getProperties().getPreviewSize();
        PictureType preview = new PictureType(previewSize, previewSize);
        Color c = new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
        for (int x = 0; x < previewSize; x++) {
            for (int y = 0; y < previewSize; y++) {
                preview.setColor(x, y, c);
            }
        }
        mInputPreviewImage = new PictureControl("Preview Image", "previewImage", new Container("x", "x", this::getUndoRedoBuffer), preview);
        mInputPreviewImage.setUIListener(this);
        mContainer.addControlChangeListener(this);

        setControlSelector(new ControlSelector(this));
        getPreviewImage().setGrafitti(this);

        Framework.logExit(mLogger);
    }

    @Override
    public void resizeInputPreview(int pPreviewSize) {
        PictureType preview = new PictureType(pPreviewSize);
        mInputPreviewImage.setValue(preview);
    }

    public void addXControl(IMouseControl pControl) {
        mControlSelector.addXControl(pControl);
    }

    public void addXYControl(IMouseControl pControl) {
        mControlSelector.addXYControl(pControl);
    }

    public void addXYControlPair(IMouseControl pControlX, IMouseControl pControlY) {
        mControlSelector.addXYControlPair(pControlX, pControlY);
    }

    public void addYControl(IMouseControl pControl) {
        mControlSelector.addYControl(pControl);
    }

    @Override
    public void controlChangeEvent(IControl pControl, boolean pIsMutating) {
        Framework.logEntry(mLogger);
        if (!isInitialized()) {
            return;
        }

        if (pControl == mProgressControl) {
            return;
        }

        if (!isMouseDragInProgress()) {
            setValues();
            redrawGrafitti();
        }

        if (!pIsMutating) {
            refreshOutputPreview();
        }

        Framework.logExit(mLogger);
    }

    @Override
    public ITransform duplicate() {
        Framework.logEntry(mLogger);

        ITransform t = null;
        try {
            var cl = getClass();
            var cons = cl.getConstructor(Perception.class);
            t = (ITransform) cons.newInstance(getPerception());
        } catch (Exception exception) {
            throw new RuntimeException("Cannot create new instance." + exception);
        }

        Framework.logExit(mLogger, t);
        return t;
    }

    public ColorProperty getColorOOBProperty() {
        return getProperties().getColorOOBProperty();
    }

    protected Container getContainer() {
        return mContainer;
    }

    @Override
    public IViewable<?> getContent() {
        return new ScrollLayout(mInputPreviewImage);
    }

    @Override
    public IContainer getControls() {
        return mContainer;
    }

    private ControlSelector getControlSelector() {
        return mControlSelector;
    }

    @Override
    public final String getDisplayName() {
        return mDisplayName;
    }

    public Color getGrafitiColor1() {
        return getProperties().getColor1();
    }

    public Color getGrafitiColor2() {
        return getProperties().getColor2();
    }

    public Color getGrafitiColor3() {
        return getProperties().getColor3();
    }

    @Override
    public int getHeight() {
        return getPreviousTransform().getHeight();
    }

    public Color getOOBColor() {
        return getProperties().getColorOOB();
    }

    public Perception getPerception() {
        return Services.getServices().getPerception();
    }

    @Override
    public IBatchEngine getPreferredBatchEngine() {
        return getPerception().getRenderService().getBaseBatchEngine();
    }

    protected PictureControl getPreviewImage() {
        return mInputPreviewImage;
    }

    @Override
    public ITransform getPreviousTransform() {
        return mPreviousTransform;
    }

    @Override
    public ITransform getFirstTransform() {
        if (mPreviousTransform == null) {
            return this;
        }
        return mPreviousTransform.getFirstTransform();
    }

    public Properties getProperties() {
        return Services.getServices().getProperties();
    }

    @Override
    public final String getPropertyName() {
        return mPropertyName;
    }

    protected UndoRedoBuffer getUndoRedoBuffer() {
        return Services.getServices().getUndoRedoBuffer();
    }

    @Override
    public boolean getUseTransform() {
        return mUseTransform.getValue();
    }

    protected ProgressControl getProgressControl() {
        return mProgressControl;
    }

    @Override
    public int getWidth() {
        return getPreviousTransform().getWidth();
    }

    @Override
    public RectangleSize getSize() {
        return new RectangleSize(getWidth(), getHeight());
    }

    public boolean isControlSelected(IControl pControl) {
        ControlSelector cs = getControlSelector();
        return cs != null && cs.isControlSelected(pControl);
    }

    @Override
    public boolean isInitialized() {
        return getPreviousTransform() != null;
    }

    public boolean isMouseDragInProgress() {
        return getControlSelector() != null && getControlSelector().isDragging();
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    public double mod1(double pX) {
        return KMath.mod1(pX);
    }

    @Override
    public void mouseClickEvent(ImmutableUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDragEndEvent(pEvent);
        }
    }

    @Override
    public void mouseDoubleClickEvent(ImmutableUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDoubleClickEvent(pEvent);
        }
    }

    @Override
    public void mouseDragEndEvent(ImmutableUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDragEndEvent(pEvent);
        }
    }

    @Override
    public void mouseDragEvent(ImmutableUIEvent pEvent) {
        if (mControlSelector != null) {
            setValues();
            getPreviewImage().drawGrafitti();
            mControlSelector.mouseDragEvent(pEvent);
        }
    }

    @Override
    public void mouseDragStartEvent(ImmutableUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDragStartEvent(pEvent);
        }
    }

    @Override
    public void read(IPersistDB pDB, String pId) {
        mContainer.read(pDB, pId);
    }

    public void redrawGrafitti() {
        getPreviewImage().drawGrafitti();
    }

    public void refreshOutputPreview() {
        Framework.logEntry(mLogger);
        getPerception().refreshOutputPreview();
        Framework.logExit(mLogger);
    }

    @Override
    public void scrollEvent(ImmutableUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.scrollEvent(pEvent);
        }
    }

    public void setControlSelector(ControlSelector pControlSelector) {
        mControlSelector = pControlSelector;
    }

    @Override
    public void setPreviousTransform(ITransform pPreviousTransform) {
        mPreviousTransform = pPreviousTransform;
        setValues();
    }

    public void setUseTransform(boolean pUse) {
        mUseTransform.setValue(pUse);
    }

    @Override
    public void setValues() {
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public void transform(ITransformResult pRenderResult) {
        pRenderResult.setColor(Color.BLUE);
    }

    @Override
    public void transform(@NonNull ITransformResultBatch pBatch) {
        Framework.logEntry(mLogger);

        IntStream.range(0, pBatch.getBatchSize()).parallel()
                .forEach(i -> {
                    ITransformResult rr = pBatch.getTransformResult(i);
                    transform(rr);
                });
//        for (int i = 0; i < pBatch.getBatchSize(); i++) {
//            final ITransformResult rr = pBatch.getTransformResult(i);
//            transform(rr);
//        }

        Framework.logExit(mLogger);
    }

    @Override
    public void refreshInputPreview() {
        Framework.logEntry(mLogger);
        Services.getServices().getRenderService()
                .getRenderJobBuilder("BaseTransform::refreshInputPreview", mInputPreviewImage, mPreviousTransform)
                .build()
                .run();
    }

    @Override
    public void write(IPersistDB pDB, String pId) throws IOException {
        pDB.write(pId + ".name", getPropertyName());
        mContainer.write(pDB, pId);
    }

    @Override
    public void graffiti(GrafittiHelper pGrafittiHelper) {
    }

    protected boolean isMutating() {
        return mIsMutating;
    }

    protected boolean isNotMutating() {
        return !mIsMutating;
    }

    protected void setMutating(boolean pIsMutating) {
        this.mIsMutating = pIsMutating;
    }

    @Override
    public int getOversample() {
        return 1;
    }

}
