/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

public class ActionView extends ViewBase<ActionControl> {

    public final static Logger mLogger = Framework.getLogger();

    private final HBox mUI;

    private final Button mButton;

    public ActionView(final ActionControl pActionControl) {
        super(pActionControl);

        mUI = new HBox();
        mUI.setAlignment(Pos.TOP_LEFT);

        mButton = new Button();
        mButton.setOnAction((e) -> performAction());
        mButton.setDisable(!pActionControl.isEnabled());

        if (mControl.isFullSize()) {
            createButton();

        } else if (mControl.hasImage()) {
            createImageButton();

        } else {
            createSmallButton();
        }

    }

    private void createButton() {
        mLabel = new Label();
        mButton.setText(mControl.getDisplayName());
        mButton.prefWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);

        mUI.getChildren().addAll(mLabel, mButton);
    }

    private void createImageButton() {
        mButton.prefWidthProperty().bind(FXViewFactory.getInstance().smallButtonWidthProperty);

        try {
            final URL url = getClass().getResource(mControl.getImageName());
            final InputStream stream = url.openStream();
            final Image image = new Image(stream);
            mButton.setGraphic(new javafx.scene.image.ImageView(image));

        } catch (final Exception pEx) {
            mLogger.severe(pEx.getMessage() + "");
            mLogger.severe(FrameworkLogger.throwableToString(pEx));
        }

        final Tooltip tooltip = new Tooltip(mControl.getDisplayName());
        Tooltip.install(mButton, tooltip);

        mUI.getChildren().addAll(mButton);
    }

    private void createSmallButton() {
        mButton.prefWidthProperty().bind(FXViewFactory.getInstance().smallButtonWidthProperty);
        mButton.setText(mControl.getDisplayName());

        mUI.getChildren().addAll(mButton);
    }

    @Override
    public Node getUI() {
        return mUI;
    }

    private void performAction() {
        mControl.performAction();
    }

    @Override
    public void setEnabled(final boolean pEnabled) {
        runOnFXApplicationThread(() -> {
            getUI().setDisable(!pEnabled);
            mButton.setDisable(!pEnabled);
        });
    }

}
