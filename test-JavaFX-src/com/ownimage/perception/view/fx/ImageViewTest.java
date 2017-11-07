package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.view.javafx.ActionView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.framework.view.javafx.PictureView;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ImageViewTest extends Application {

	public static void main(final String[] args) {
		FXViewFactory.setAsViewFactory();
		Application.launch(args);
	}

	private PictureView mPictureView;

	private void action() {
		System.out.println("action");
		mPictureView.grafitti();
	}

	@Override
	public void start(final Stage stage) {
		// load the image
		Image image = new Image("file://localhost/c|test1.jpg");

		mPictureView = new PictureView(image);
		// // simple displays ImageView the image as is
		// ImageView iv1 = new ImageView();
		// iv1.setImage(image);

		Container container = new Container("x", "x");
		ActionControl action = new ActionControl("Action", "Action", container, () -> action());
		ActionView actionView = (ActionView) action.createView();
		Group root = new Group();
		Scene scene = new Scene(root);
		scene.setFill(Color.BLACK);
		HBox box = new HBox();
		box.getChildren().add(mPictureView.getUI());
		box.getChildren().add(actionView.getUI());
		root.getChildren().add(box);

		stage.setTitle("ImageView");
		stage.setWidth(415);
		stage.setHeight(200);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
}
