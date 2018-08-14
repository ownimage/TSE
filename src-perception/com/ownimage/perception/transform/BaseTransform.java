/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl.ColorProperty;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IGrafitti;
import com.ownimage.framework.control.control.IMouseControl;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.ScrollLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.math.KMath;
import com.ownimage.perception.math.RectangleSize;
import com.ownimage.perception.render.IBatchEngine;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.render.ITransformResultBatch;

public abstract class BaseTransform implements IGrafitti, ITransform, IControlChangeListener, IUIEventListener, IUndoRedoBufferProvider {


    private final static Logger mLogger = Framework.getLogger();

    private final String mDisplayName;
    private final String mPropertyName;
    private final Container mContainer;
    private final BooleanControl mUseTransform;
    private final PictureControl mPreviewImage;

    private ITransform mPreviousTransform;
    private ControlSelector mControlSelector;

    private boolean mIsMutating = false;
    private boolean mIsInitialized = false;

    public BaseTransform(final String pDisplayName, final String pPropertyName) {
        Framework.logEntry(mLogger);

        mDisplayName = pDisplayName;
        mPropertyName = pPropertyName;

        mContainer = new Container(getDisplayName(), getPropertyName(), getPerception());
        mUseTransform = new BooleanControl("Use Transform", "use", mContainer, true);

        int previewSize = getProperties().getPreviewSize();
        ColorProperty oob = getProperties().getColorOOBProperty();
        PictureType preview = new PictureType(oob, previewSize, previewSize);
        Color c = new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random()));
        for (int x = 0; x < previewSize; x++) {
            for (int y = 0; y < previewSize; y++) {
                preview.setColor(x, y, c);
            }
        }
        mPreviewImage = new PictureControl("Preview Image", "previewImage", new Container("x", "x", getPerception()), preview);
        mPreviewImage.setUIListener(this);
        mContainer.addControlChangeListener(this);

        setControlSelector(new ControlSelector(this));
        getPreviewImage().setGrafitti(this);

        Framework.logExit(mLogger);
    }

    public void addXControl(final IMouseControl pControl) {
        mControlSelector.addXControl(pControl);
    }

    public void addXYControl(final IMouseControl pControl) {
        mControlSelector.addXYControl(pControl);
    }

    public void addXYControlPair(final IMouseControl pControlX, final IMouseControl pControlY) {
        mControlSelector.addXYControlPair(pControlX, pControlY);
    }

    public void addYControl(final IMouseControl pControl) {
        mControlSelector.addYControl(pControl);
    }

    @Override
    public void controlChangeEvent(final Object pControl, final boolean pIsMutating) {
        Framework.logEntry(mLogger);
        if (!isInitialized()) {
            return;
        }

        if (!isMouseDragInProgress()) {
            setValues();
            redrawGrafitti();
            refreshPreview();
        }

        Framework.logExit(mLogger);
    }

    @Override
    public ITransform duplicate() {
        Framework.logEntry(mLogger);

        ITransform t = null;
        try {
            Class cl = getClass();
            Constructor<?> cons = cl.getConstructor(Perception.class);
            t = (ITransform) cons.newInstance(getPerception());
        } catch (Throwable pT) {
            throw new RuntimeException("Cannot create new instance." + pT);
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
        return new ScrollLayout(mPreviewImage);
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
        return mPreviewImage;
    }

    @Override
    public ITransform getPreviousTransform() {
        return mPreviousTransform;
    }

    public Properties getProperties() {
        return getPerception().getProperties();
    }

    @Override
    public final String getPropertyName() {
        return mPropertyName;
    }

    @Override
    public UndoRedoBuffer getUndoRedoBuffer() {
        return getPerception().getUndoRedoBuffer();
    }

    @Override
    public boolean getUseTransform() {
        return mUseTransform.getValue();
    }

    @Override
    public int getWidth() {
        return getPreviousTransform().getWidth();
    }

    @Override
    public RectangleSize getSize() {
        return new RectangleSize(getWidth(), getHeight());
    }

    public boolean isControlSelected(final IControl pControl) {
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

    public double mod1(final double pX) {
        return KMath.mod1(pX);
    }

    @Override
    public void mouseClickEvent(final IUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDragEndEvent(pEvent);
        }
    }

    @Override
    public void mouseDoubleClickEvent(final IUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDoubleClickEvent(pEvent);
        }
    }

    @Override
    public void mouseDragEndEvent(final IUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDragEndEvent(pEvent);
        }
    }

    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
        if (mControlSelector != null) {
            setValues();
            getPreviewImage().redrawGrafitti();
            mControlSelector.mouseDragEvent(pEvent);
        }
    }

    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.mouseDragStartEvent(pEvent);
        }
    }

    @Override
    public void read(final IPersistDB pDB, final String pId) {
        mContainer.read(pDB, pId);
    }

    public void redrawGrafitti() {
        getPreviewImage().redrawGrafitti();
    }

    public void refreshPreview() {
        Framework.logEntry(mLogger);
        getPerception().refreshPreview();
        Framework.logExit(mLogger);
    }

    @Override
    public void scrollEvent(final IUIEvent pEvent) {
        if (mControlSelector != null) {
            mControlSelector.scrollEvent(pEvent);
        }
    }

    public void setControlSelector(final ControlSelector pControlSelector) {
        mControlSelector = pControlSelector;
    }

    @Override
    public void setPreviousTransform(final ITransform pPreviousTransform) {
        mPreviousTransform = pPreviousTransform;
    }

    public void setUseTransform(final boolean pUse) {
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
    public void transform(final ITransformResult pRenderResult) {
        pRenderResult.setColor(Color.BLUE);
    }

    @Override
    public void transform(final ITransformResultBatch pBatch) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pBatch, "pBatch");

        for (int i = 0; i < pBatch.getBatchSize(); i++) {
            ITransformResult rr = pBatch.getTransformResult(i);
            transform(rr);
        }

        Framework.logExit(mLogger);
    }

    @Override
    public void updatePreview() {
        Framework.logEntry(mLogger);
        getPerception().getRenderService().transform(mPreviewImage, mPreviousTransform, null);
        Framework.logExit(mLogger);
    }

    @Override
    public void write(final IPersistDB pDB, final String pId) throws IOException {
        pDB.write(pId + ".name", getPropertyName());
        mContainer.write(pDB, pId);
    }

    @Override
    public void grafitti(final GrafittiHelper pGrafittiHelper) {
    }

    protected boolean isMutating() {
        return mIsMutating;
    }

    protected boolean isNotMutating() {
        return !mIsMutating;
    }

    protected void setMutating(final boolean pIsMutating) {
        this.mIsMutating = pIsMutating;
    }

    @Override
    public int getOversample() {
        return 1;
    }

}
