package com.ownimage.framework.view.javafx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.ownimage.framework.control.control.ActionControl;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;

public class ActionView extends ViewBase<ActionControl> {

	private final HBox mUI;;
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
			URL url = getClass().getResource(mControl.getImageName());
			InputStream stream = url.openStream();
			Image image = new Image(stream);
			mButton.setGraphic(new javafx.scene.image.ImageView(image));

		} catch (IOException pEx) {
			System.out.println(pEx.getMessage());
		}

		Tooltip tooltip = new Tooltip(mControl.getDisplayName());
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
		getUI().setDisable(!pEnabled);
		mButton.setDisable(!pEnabled);
	}

}
