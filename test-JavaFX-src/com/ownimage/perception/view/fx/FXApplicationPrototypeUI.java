package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.NamedTabs;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.framework.view.javafx.HSplitView;
import com.ownimage.perception.app.AppControlBase;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FXApplicationPrototypeUI extends Application {

	public static void main(final String[] args) {
		FXViewFactory.setAsViewFactory();
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {

		Double c = new Double("C");

		Container other = new Container("other", "other");

		Container container1 = new Container("container1", "container1");
		IntegerControl i1 = new IntegerControl("integer1", "integer1", container1, 1);

		Container container1a = new Container("container1a", "container1a");
		IntegerControl i1a = new IntegerControl("integer1a", "integer1a", container1a, 1);

		PictureType sourceImage = new PictureType("C:\\temp\\test.jpg");
		PictureControl sourcePicture = new PictureControl("source", "source", other, sourceImage);

		HFlowLayout imageLoadTransform = new HFlowLayout(sourcePicture, container1);

		NamedTabs namedTabs = new NamedTabs();
		namedTabs.addTab("other", container1a);
		namedTabs.addTab("z", imageLoadTransform);

		Container container2 = new Container("container2", "container2");
		IntegerControl i2 = new IntegerControl("integer2", "integer2", container2, 2);

		HSplitLayout hsplit = new HSplitLayout(namedTabs, container2);

		// for menu
		BorderPane border = new BorderPane();
		AppControlBase app = new AppControlBase("Test Application");

		// MenuBar menu = createMenu(app.getMenu());
		// menu.prefWidthProperty().bind(primaryStage.widthProperty());
		// border.setTop(menu);

		StackPane root = new StackPane();
		HSplitView hsplitview = (HSplitView) (hsplit.createView());
		root.getChildren().add(hsplitview.getUI());

		Scene scene = new Scene(root, 1000, 800);
		scene.getStylesheets().add("stylesheet.css");

		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
	}

}