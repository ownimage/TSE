package com.ownimage.perception.view.fx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXApplication extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Hello World!");

		Label label = new Label("Label");

		Button btn1 = new Button();
		btn1.setText("Say 'Hello World'");
		btn1.setOnAction((event) -> buttonPress());
		// btn1.setVisible(true);
		// btn1.setDisable(true);
		// btn1.setMinSize(0, 0);
		// btn1.setMaxSize(0, 0);
		// btn1.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// // btn1.setPrefSize(prefWidth, prefHeight);

		Spinner<Integer> spinner = new Spinner<Integer>(0, 100, 50, 10);
		spinner.setEditable(true);
		spinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(8);

		vbox.getChildren().add(label);
		vbox.getChildren().add(btn1);
		vbox.getChildren().add(spinner);

		StackPane root = new StackPane();
		root.getChildren().add(vbox);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.show();
	}

	private void buttonPress() {
		System.out.println("Hello World!");
	}
}