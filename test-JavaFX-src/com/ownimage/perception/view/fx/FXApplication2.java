package com.ownimage.perception.view.fx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;

public class FXApplication2 extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Hello World!");

		Label label1 = new Label("Label 1");
		Label label2 = new Label("Label 2");

		Button btn1 = new Button();
		btn1.setText("Say 'Hello World'");
		btn1.setOnAction((event) -> buttonPress());
		// btn1.setVisible(true);
		// btn1.setDisable(true);
		// btn1.setMinSize(0, 0);
		// btn1.setMaxSize(0, 0);
		// btn1.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// btn1.setPrefSize(prefWidth, prefHeight);

		Spinner<Integer> spinner = new Spinner<Integer>(0, 100, 50, 10);
		spinner.setEditable(true);
		spinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		GroupLayoutPane root = new GroupLayoutPane();
		root.setPadding(new Insets(5));

		root.getChildren().add(label1);
		root.getChildren().add(label2);
		root.getChildren().add(btn1);
		root.getChildren().add(spinner);

		root.setHorizontalGroup( //
				root.createSequentialGroup()
						.addGroup( //
								root.createParallelGroup() //
										.addNode(label1) //
										.addNode(label2) //
		) //
						.addGap(10) //
						.addGroup( //
								root.createParallelGroup() //
										.addNode(btn1) //
										.addNode(spinner) //
		) //
		);

		root.setVerticalGroup( //
				root.createSequentialGroup()
						.addGroup( //
								root.createParallelGroup() //
										.addNode(label1) //
										.addNode(btn1) //
		) //
						.addGap(10) //
						.addGroup( //
								root.createParallelGroup() //
										.addNode(label2) //
										.addNode(spinner) //
		) //
		);

		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.sizeToScene();
		primaryStage.show();

	}

	private void buttonPress() {
		System.out.println("Hello World!");
	}
}
