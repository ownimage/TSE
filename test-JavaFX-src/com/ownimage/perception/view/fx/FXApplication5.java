package com.ownimage.perception.view.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FXApplication5 extends Application {

	static final Integer SIZE = 10;

	boolean mVisible = true;

	private Button[] mBtn = new Button[SIZE];
	private Label[] mLabel = new Label[SIZE];

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

		GridPane gridpane = new GridPane();
		gridpane.getColumnConstraints().add(new ColumnConstraints(100)); // column 1 is 100 wide
		gridpane.getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 200 wide
		for (int i = 0; i < SIZE; i++) {
			final Integer id = i;
			mLabel[i] = new Label("Label " + i);
			gridpane.add(mLabel[i], 0, i);

			mBtn[i] = createButton(i);
			mBtn[i].setOnAction((event) -> buttonPress(id));
			gridpane.add(mBtn[i], 1, i);
		}

		StackPane root = new StackPane();
		root.getChildren().add(gridpane);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	private void buttonPress(int pI) {
		mVisible = !mVisible;
		System.out.println(pI + "mVisisble = " + mVisible);
		mLabel[pI].setVisible(mVisible);
		mBtn[pI].setVisible(mVisible);
	}
}