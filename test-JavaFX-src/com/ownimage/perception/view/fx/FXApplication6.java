package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.control.DoubleControl;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXApplication6 extends Application {

	static final Integer SIZE = 5;

	boolean mVisible = true;

	private DoubleControl[] controls = new DoubleControl[SIZE];

	public static void main(String[] args) {
		launch(args);
	}

	private Button createButton(int pId) {
		Button button = new Button();
		button.setText("Say 'Hello World " + pId);
		button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		return button;
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Hello World!");

		VBox vbox = new VBox();
		for (int i = 0; i < SIZE; i++) {
			String name = "Double Control " + i;
			DoubleControl control = new DoubleControl(name, name, i / 10.0d);
			controls[i] = control;
			vbox.getChildren().add(new TestDoubleView(control).getUI());
			vbox.getChildren().add(new TestDoubleView(control).getUI());
		}

		Button printButton = new Button("Print values");
		printButton.setOnAction((e) -> printValues());
		vbox.getChildren().add(printButton);

		StackPane root = new StackPane();
		root.getChildren().add(vbox);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	private void printValues() {
		for (int i = 0; i < SIZE; i++) {
			DoubleControl control = controls[i];
			System.out.println(control.getDisplayName() + " = " + control.getDouble());
		}
	}

}