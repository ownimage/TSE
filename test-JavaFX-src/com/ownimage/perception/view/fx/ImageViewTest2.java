package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.view.javafx.ActionView;
import com.ownimage.framework.view.javafx.ContainerView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.framework.view.javafx.PictureView;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ImageViewTest2 extends Application {

	private Node mPictureView;

	private PictureControl mPictureControl;
	private DoubleControl mDC;
	private IControlChangeListener<DoubleControl> x;

	public static void main(final String[] args) {
		FXViewFactory.setAsViewFactory();
		Application.launch(args);
	}

	private void action() {
		System.out.println("action");
		mPictureControl.redrawGrafitti();
	}

	private void grafitti(final GrafittiHelper pGH) {
		// pGH.drawLine(0.0, 0.0, 1.0, 1.0, java.awt.Color.GREEN, false);
		pGH.drawString("Hello cruely World", 0.15d, mDC.getValue());
		pGH.setFontSize(100);
		// pGH.drawCircle(0.2, 0.2, 0.2, java.awt.Color.BLACK, false);
		// pGH.drawFilledRectangle(0.8, 0.7, 0.88, 0.76, java.awt.Color.PINK);
	}

	private void newFn(final DoubleControl pO, final boolean pB) {
		action();
	}

	@Override
	public void start(final Stage stage) {
		// load the image
		Container container = new Container("ImageView", "imageView");
		PictureType picture = new PictureType("c:\\temp\\ny600.jpg");
		mPictureControl = new PictureControl("x", "x", container, picture);
		mPictureControl.setGrafitti((pGraphicsHelper) -> grafitti(pGraphicsHelper));
		mPictureView = ((PictureView) mPictureControl.createView()).getUI();
		Node pictureView = ((PictureView) mPictureControl.createView()).getUI();

		ActionControl action = new ActionControl("Action", "Action", container, () -> action());
		ActionView actionView = (ActionView) action.createView();

		mDC = new DoubleControl("Double Control", "dc", container, 0.5);
		// mDC.addControlChangeListener(this);
		x = (o, b) -> newFn(o, b); // needed to prevent lambda being garbage collected
		mDC.addControlChangeListener(x);

		Group root = new Group();
		Scene scene = new Scene(root);
		scene.setFill(Color.BLACK);
		HBox box = new HBox();
		box.getChildren().add(((ContainerView) (container.createView())).getUI());
		// box.getChildren().add(mPictureView);
		// box.getChildren().add(pictureView);
		// box.getChildren().add(actionView.getUI());

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(box);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

		root.getChildren().add(scrollPane);

		stage.setTitle("ImageView");
		stage.setWidth(415);
		stage.setHeight(200);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}
}
