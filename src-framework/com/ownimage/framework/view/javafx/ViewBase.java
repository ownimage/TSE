package com.ownimage.framework.view.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class ViewBase<C extends IViewable> implements FXView {

    public final static Version mVersion = new Version(2017, 0, 0, "2017/11/07 16:30");
    private final static Logger mLogger = Framework.getLogger();

    protected C mControl;
    protected Label mLabel;

    private boolean mIsMutating = false;

    public ViewBase(final C pControl) {
        mControl = pControl;

        if (pControl instanceof IControl) {
            ((IControl) pControl).addControlChangeListener(this);
            mLabel = new Label(((IControl) pControl).getDisplayName());
            mLabel.prefWidthProperty().bind(FXViewFactory.getInstance().labelWidthProperty);
            mLabel.minWidthProperty().bind(FXViewFactory.getInstance().labelWidthProperty);
            mLabel.maxWidthProperty().bind(FXViewFactory.getInstance().labelWidthProperty);
        }
    }


    protected void runOnFXApplicationThread(Runnable pRunnable) {
        if (Platform.isFxApplicationThread()) pRunnable.run();
        else Platform.runLater(pRunnable);
    }

    public static Image getImage(final String pName) {
        URL url = pName.getClass().getResource(pName);
        try (InputStream stream = url.openStream();) {
            Image image = new Image(stream);
            return image;
        } catch (IOException ioe) {
            mLogger.severe("Unable to getImage for " + pName);
            Framework.logThrowable(mLogger, Level.SEVERE, ioe);
        }
        return null;
    }

    protected void bindWidth(final Region pRegion, final SimpleIntegerProperty pWidthProperty) {
        pRegion.prefWidthProperty().bind(pWidthProperty);
        pRegion.maxWidthProperty().bind(pWidthProperty);
        pRegion.minWidthProperty().bind(pWidthProperty);
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
    }

    public FXViewFactory getFactory() {
        return FXViewFactory.getInstance();
    }

    @Override
    public Node getUI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void redraw() {
    }

    @Override
    public void setEnabled(final boolean pEnabled) {
        runOnFXApplicationThread(() -> getUI().setDisable(!pEnabled));
    }

    @Override
    public void setVisible(final boolean pVisible) {
        runOnFXApplicationThread(() -> {
            getUI().setManaged(pVisible);
            getUI().setVisible(pVisible);
        });
    }

    protected boolean isMutating() {
        return mIsMutating;
    }

    protected void setMutating(final boolean mIsMutating) {
        this.mIsMutating = mIsMutating;
    }
}
