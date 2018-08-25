/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.app;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public void controlChangeEvent(final Object pControl, final boolean pIsMutating) {
        if (pControl == mUseJTP) {
            setEnabled(mUseJTP.getValue(), mRenderBatchSize, mRenderJTPBatchSize, mRenderThreadPoolSize);
        }

    }

    @Override
    public IView createView() {
        NamedTabs view = new NamedTabs("Properties", "properties");

        VFlowLayout defaults = new VFlowLayout(mUseDefaultPropertyFile, mUseDefaultLoggingFile, mAutoLoadTransformFile);
        VFlowLayout sizes = new VFlowLayout(mPreviewSize, mSavePreviewSize);
        VFlowLayout colors = new VFlowLayout(mColor1, mColor2, mColor3, mColorOOB, mPixelMapBGColor, mPixelMapFGColor);
        VFlowLayout render = new VFlowLayout(mUseJTP, mRenderBatchSize, mRenderThreadPoolSize, mRenderJTPBatchSize, mUseOpenCL);
        VFlowLayout output = new VFlowLayout(mJPGQuality);

        view.addTab("Defaults", defaults);
        view.addTab("Sizes", sizes);
        view.addTab("Colors", colors);
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
        try (FileOutputStream fos = new FileOutputStream(pFile)) {
            PersistDB db = new PersistDB();
            write(db, "");
            db.store(fos, pComment);

        } catch (Throwable pT) {
            throw new FrameworkException(this, Level.SEVERE, "Unable to write properties", pT);
        }
    }

    public void reset() {
        try {
            PersistDB db = new PersistDB();
            new Properties().write(db, "");
            read(db, "");
        } catch (Throwable pT) {
            throw new FrameworkException(this, Level.SEVERE, "Unable to reset properties", pT);
        }
    }

    public ImageQuality getImageQuality() {
        return new ImageQuality((float) Services.getServices().getProperties().getJpgQuality());
    }
}
