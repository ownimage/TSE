/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.app;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.BooleanControl.BooleanProperty;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.ColorControl.ColorProperty;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.IntegerControl.IntegerProperty;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.NamedTabs;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.logging.FrameworkException;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.ImageQuality;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Properties implements IViewable, IUndoRedoBufferProvider, IPersist, IControlChangeListener {


    private final static Logger mLogger = Framework.getLogger();

    private final Container mContainer = new Container("Properties", "properties", getUndoRedoBuffer());

    // defaults
    private final BooleanControl mUseDefaultPropertyFile = new BooleanControl("Use Default Property Properties File", "useDefaultPropertyFile", mContainer, true);
    private final BooleanControl mUseDefaultLoggingFile = new BooleanControl("Use Default Logging Properties File", "useDefaultLoggingFile", mContainer, true);
    private final BooleanControl mAutoLoadTransformFile = new BooleanControl("Auto Load Transform File", "autoLoadTransformFile", mContainer, true);

    // sizes
    private final IntegerControl mPreviewSize = new IntegerControl("Preview size", "previewSize", mContainer, 800, 500, 1600, 100);
    private final IntegerControl mSavePreviewSize = new IntegerControl("Save size", "savePreviewSize", mContainer, 500, 200, 1600, 100);

    // cannyEdgeTransform
    public final IntegerMetaType CETEPMDPreviewSizeModel = new IntegerMetaType(100, 1000, 50);
    public final IntegerMetaType CETEPMDZoomModel = new IntegerMetaType(1, 20, 2);
    private final IntegerControl mCETEPMDPreviewSize = new IntegerControl("Preview size", "previewSize", mContainer, 800, CETEPMDPreviewSizeModel);
    private final IntegerControl mCETEPMDZoom = new IntegerControl("Save size", "savePreviewSize", mContainer, 2, CETEPMDZoomModel);
    private final ColorControl mCETEPMDEdgeColor = new ColorControl("Edge Color", "edgeColor", mContainer, Color.GREEN);
    private final ColorControl mCETEPMDNodeColor = new ColorControl("Node Color", "nodeColor", mContainer, Color.RED);
    private final ColorControl mCETEPMDWorkingColor = new ColorControl("Working Color", "workingColor", mContainer, Color.LIGHT_GRAY);

    // colors
    private final ColorControl mColor1 = new ColorControl("Color 1", "color1", mContainer, Color.RED);
    private final ColorControl mColor2 = new ColorControl("Color 2", "color2", mContainer, Color.ORANGE);
    private final ColorControl mColor3 = new ColorControl("Color 3", "color3", mContainer, Color.GREEN);
    private final ColorControl mColorOOB = new ColorControl("Color OOB", "colorOOB", mContainer, Color.PINK);
    private final ColorControl mPixelMapBGColor = new ColorControl("PixelMap background", "pixelMapBG", mContainer, Color.WHITE);
    private final ColorControl mPixelMapFGColor = new ColorControl("PixelMap foreground", "pixelMapFG", mContainer, Color.BLACK);

    // render
    private final BooleanControl mUseJTP = new BooleanControl("Use JTP", "useJTP", mContainer, true);
    private final IntegerControl mRenderBatchSize = new IntegerControl("Batch size", "batchSize", mContainer, 1000, 1, 1000000, 100000);
    private final IntegerControl mRenderThreadPoolSize = new IntegerControl("Thread pool size", "threadPoolSize", mContainer, 8, 1, 32, 1);
    private final IntegerControl mRenderJTPBatchSize = new IntegerControl("JTP batch size", "JTPBatchSize", mContainer, 100, 1, 100000, 1000);
    private final BooleanControl mUseOpenCL = new BooleanControl("Use OpenCL", "useOpenCL", mContainer, true);

    // output
    private final DoubleControl mJPGQuality = new DoubleControl("JPG Quality", "jpgQuality", mContainer, 1.0);


    public Properties() {
        Framework.logEntry(mLogger);
        mContainer.addControlChangeListener(this);
        Framework.logExit(mLogger);
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mUseJTP) {
            setEnabled(mUseJTP.getValue(), mRenderBatchSize, mRenderJTPBatchSize, mRenderThreadPoolSize);
        }

    }

    @Override
    public IView createView() {
        final NamedTabs view = new NamedTabs("Properties", "properties");

        final VFlowLayout defaults = new VFlowLayout(mUseDefaultPropertyFile, mUseDefaultLoggingFile, mAutoLoadTransformFile);
        final VFlowLayout sizes = new VFlowLayout(mPreviewSize, mSavePreviewSize);
        final VFlowLayout colors = new VFlowLayout(mColor1, mColor2, mColor3, mColorOOB, mPixelMapBGColor, mPixelMapFGColor);

        final NamedTabs transformsTab = new NamedTabs("Transforms", "transforms");
        transformsTab.addTab("CannyEdge", new VFlowLayout(
                mCETEPMDPreviewSize, mCETEPMDZoom, mCETEPMDEdgeColor, mCETEPMDNodeColor, mCETEPMDWorkingColor));
        final VFlowLayout render = new VFlowLayout(
                mUseJTP, mRenderBatchSize, mRenderThreadPoolSize, mRenderJTPBatchSize, mUseOpenCL);
        final VFlowLayout output = new VFlowLayout(mJPGQuality);

        view.addTab("Defaults", defaults);
        view.addTab("Sizes", sizes);
        view.addTab("Colors", colors);
        view.addTab("Transforms", transformsTab);
        view.addTab("Render Engine", render);
        view.addTab("Output", output);
        view.addTab(ViewFactory.getInstance().getViewFactoryPropertiesViewable());

        return view.createView();
    }

    public Color getColor1() {
        return mColor1.getValue();
    }

    public ColorProperty getColor1Property() {
        return mColor1.getProperty();
    }

    public Color getColor2() {
        return mColor2.getValue();
    }

    public ColorProperty getColor2Property() {
        return mColor2.getProperty();
    }

    public Color getColor3() {
        return mColor3.getValue();
    }

    public ColorProperty getColor3Property() {
        return mColor3.getProperty();
    }

    public Color getColorOOB() {
        return mColorOOB.getValue();
    }

    public ColorProperty getColorOOBProperty() {
        return mColorOOB.getProperty();
    }

    @Override
    public String getDisplayName() {
        return "Properties";
    }

    public double getJpgQuality() {
        return mJPGQuality.getValue();
    }

    public Color getPixelMapBGColor() {
        return mPixelMapBGColor.getValue();
    }

    public ColorProperty getPixelMapBGColorProperty() {
        return mPixelMapBGColor.getProperty();
    }

    public Color getPixelMapFGColor() {
        return mPixelMapFGColor.getValue();
    }

    public ColorProperty getPixelMapFGColorProperty() {
        return mPixelMapFGColor.getProperty();
    }

    public int getPreviewSize() {
        return mPreviewSize.getValue();
    }

    public int getSavePreviewSize() {
        return mSavePreviewSize.getValue();
    }

    @Override
    public String getPropertyName() {
        return "properties";
    }

    public int getRenderBatchSize() {
        return mRenderBatchSize.getValue();
    }

    public IntegerProperty getRenderBatchSizeProperty() {
        return mRenderBatchSize.getProperty();
    }

    public int getRenderJTPBatchSize() {
        return mRenderJTPBatchSize.getValue();
    }

    public IntegerProperty getRenderJTPBatchSizeProperty() {
        return mRenderJTPBatchSize.getProperty();
    }

    public int getRenderThreadPoolSize() {
        return mRenderThreadPoolSize.getValue();
    }

    public IntegerProperty getRenderThreadPoolSizeProperty() {
        return mRenderThreadPoolSize.getProperty();
    }

    @Override
    public UndoRedoBuffer getUndoRedoBuffer() {
        return ViewFactory.getInstance().getPropertiesUndoRedoBuffer();
    }

    public boolean useOpenCL() {
        return mUseOpenCL.getValue();
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void read(final IPersistDB pDB, final String pId) {
        mContainer.read(pDB, pId);
        ViewFactory.getInstance().getViewFactoryPropertiesViewable().read(pDB, pId);
    }

    private void setEnabled(final boolean pEnabled, final IControl... pControls) {
        Arrays.stream(pControls).forEach(c -> c.setEnabled(pEnabled));
    }

    public boolean useAutoLoadTransformFile() {
        return mAutoLoadTransformFile.getValue();
    }

    public BooleanProperty useAutoLoadTransformFileProperty() {
        return mAutoLoadTransformFile.getProperty();
    }

    public boolean useDefaultLoggingFile() {
        return mUseDefaultLoggingFile.getValue();
    }

    public BooleanProperty useDefaultLoggingFileProperty() {
        return mUseDefaultLoggingFile.getProperty();
    }

    public boolean useDefaultPropertyFile() {
        return mUseDefaultPropertyFile.getValue();
    }

    public BooleanProperty useDefaultPropertyFileProperty() {
        return mUseDefaultPropertyFile.getProperty();
    }

    public Boolean useJTP() {
        return mUseJTP.getValue();
    }

    public BooleanProperty useJTPProperty() {
        return mUseJTP.getProperty();
    }

    @Override
    public void write(final IPersistDB pDB, final String pId) throws IOException {
        mContainer.write(pDB, pId);
        ViewFactory.getInstance().getViewFactoryPropertiesViewable().write(pDB, pId);
    }

    public void write(final File pFile, final String pComment) {
        try (final FileOutputStream fos = new FileOutputStream(pFile)) {
            final PersistDB db = new PersistDB();
            write(db, "");
            db.store(fos, pComment);

        } catch (final Throwable pT) {
            throw new FrameworkException(this, Level.SEVERE, "Unable to write properties", pT);
        }
    }

    public void reset() {
        try {
            final PersistDB db = new PersistDB();
            new Properties().write(db, "");
            read(db, "");
        } catch (final Throwable pT) {
            throw new FrameworkException(this, Level.SEVERE, "Unable to reset properties", pT);
        }
    }

    public ImageQuality getImageQuality() {
        return new ImageQuality((float) Services.getServices().getProperties().getJpgQuality());
    }

    public int getCETEPMDPreviewSize() {
        return mCETEPMDPreviewSize.getValue();
    }

    public int getCETEPMDZoom() {
        return mCETEPMDZoom.getValue();
    }

    public Color getCETEPMDEdgeColor() {
        return mCETEPMDEdgeColor.getValue();
    }

    public Color getCETEPMDNodeColor() {
        return mCETEPMDNodeColor.getValue();
    }

    public Color getCETEPMDWorkingColor() {
        return mCETEPMDWorkingColor.getValue();
    }
}
