/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.util.Framework;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ViewBase<C extends IViewable> implements FXView {


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


    protected void runOnFXApplicationThread(final Runnable pRunnable) {
        if (Platform.isFxApplicationThread()) pRunnable.run();
        else Platform.runLater(pRunnable);
    }

    public Image getImage(final String pName) {
        final URL url = getClass().getResource(pName);
        try (final InputStream stream = url.openStream()) {
            final Image image = new Image(stream);
            return image;
        } catch (final Exception pEx) {
            mLogger.severe("Unable to getImage for " + pName);
            Framework.logThrowable(mLogger, Level.SEVERE, pEx);
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
        Framework.logEntry(mLogger);
        runOnFXApplicationThread(() -> {
            getUI().setManaged(pVisible);
            getUI().setVisible(pVisible);
        });
        Framework.logExit(Framework.mLogger);
    }

    protected boolean isMutating() {
        return mIsMutating;
    }

    protected boolean isNotMutating() {
        return !mIsMutating;
    }

    protected void setMutating(final boolean mIsMutating) {
        this.mIsMutating = mIsMutating;
    }

    protected void queueApplicationEvent(final IAction pAction) {
        ApplicationEventQueue.getInstance().queueEvent(pAction);
    }
}
