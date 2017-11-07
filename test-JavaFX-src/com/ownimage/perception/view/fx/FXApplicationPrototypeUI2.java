package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.view.javafx.ContainerView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.AppControlBase;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FXApplicationPrototypeUI2 extends Application {

	EventHandler<MouseEvent> mousehandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(final MouseEvent mouseEvent) {
			System.out.println("hi");
		}
	};

	public static void main(final String[] args) {
		FXViewFactory.setAsViewFactory();
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		Container other = new Container("other", "other");

		// Image Transform
		Container itControls = new Container("container1", "container1");
		IntegerControl i1 = new IntegerControl("itcontrols", "itcontrols", itControls, 1);

		ContainerView itView = (ContainerView) itControls.createView();

		// Rotate Transform
		Container rControls = new Container("container1a", "container1a");
		IntegerControl i1a = new IntegerControl("rControls", "rControls", rControls, 1);

		ContainerView rView = (ContainerView) rControls.createView();

		// PictureType sourceImage = new PictureType("C:\\temp\\test.jpg");
		// PictureControl sourcePicture = new PictureControl("source", "source", other, sourceImage);

		// HFlow imageLoadTransform = new HFlow(sourcePicture, container1);

		TitledPane t1 = new TitledPane("T1", itView.getUI());
		t1.expandedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue, final Boolean newValue) {
				System.out.println("change " + newValue);
			}
		});
		TitledPane t2 = new TitledPane("T2", rView.getUI());
		Accordion accordion = new Accordion();
		accordion.getPanes().addAll(t1, t2);
		t1.setOnMouseClicked(mousehandler);
		t2.setOnMouseClicked(mousehandler);

		// NamedTabs namedTabs = new NamedTabs();
		// namedTabs.addTab("other", container1a);
		// namedTabs.addTab("z", imageLoadTransform);
		//
		// Container container2 = new Container("container2", "container2");
		// IntegerControl i2 = new IntegerControl("integer2", "integer2", container2, 2);
		//
		// HSplit hsplit = new HSplit(namedTabs, container2);

		// for menu
		AppControlBase app = new AppControlBase("Test Application");

		// MenuBar menu = createMenu(app.getMenu());
		// menu.prefWidthProperty().bind(primaryStage.widthProperty());

		ScrollPane scroll = new ScrollPane(accordion);
		scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

		BorderPane border = new BorderPane();
		border.setLeft(scroll);

		StackPane root = new StackPane();
		// HSplitView hsplitview = (HSplitView) (hsplit.createView());
		root.getChildren().add(border);

		Scene scene = new Scene(root, 1000, 800);
		scene.getStylesheets().add("stylesheet.css");

		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();
	}

}