package com.ownimage.perception.view.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class FXApplication4 extends Application {

	boolean mVisible = true;

	private Button mBtn1;
	private Button mBtn2;
	private Button mBtn3;
	private Button mBtn4;
	private Button mBtn5;
	private Button mBtn6;
	private Button mBtn7;

	private Spinner<Integer> mSpinner;

	private Label mLabel1;
	private Label mLabel2;
	private Label mLabel3;
	private Label mLabel4;
	private Label mLabel5;
	private Label mLabel6;
	private Label mLabel7;
	private Label mLabel8;

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

		mBtn1 = createButton(1);
		mBtn1.setOnAction((event) -> buttonPress());

		mBtn2 = createButton(2);
		mBtn3 = createButton(3);
		mBtn4 = createButton(4);
		mBtn5 = createButton(5);
		mBtn6 = createButton(6);
		mBtn7 = createButton(7);

		mSpinner = new Spinner<Integer>(0, 100, 50, 10);
		mSpinner.setEditable(true);
		mSpinner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		mLabel1 = new Label("Label 1");
		mLabel2 = new Label("Label 2");
		mLabel3 = new Label("Label 3");
		mLabel4 = new Label("Label 4");
		mLabel5 = new Label("Label 5");
		mLabel6 = new Label("Label 6");
		mLabel7 = new Label("Label 7");
		mLabel8 = new Label("Label 8");

		GridPane gridpane = new GridPane();
		// gridpane.setGridLinesVisible(true);
		gridpane.getColumnConstraints().add(new ColumnConstraints(100)); // column 1 is 100 wide
		gridpane.getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 200 wide

		gridpane.add(mLabel1, 0, 0);
		gridpane.add(mLabel2, 0, 1);
		gridpane.add(mLabel3, 0, 2);
		gridpane.add(mLabel4, 0, 3);
		gridpane.add(mLabel5, 0, 4);
		gridpane.add(mLabel6, 0, 5);
		gridpane.add(mLabel7, 0, 6);
		gridpane.add(mLabel8, 0, 7);

		gridpane.add(mBtn1, 1, 0);
		gridpane.add(mSpinner, 1, 1);
		gridpane.add(mBtn2, 1, 2);
		gridpane.add(mBtn3, 1, 3);
		gridpane.add(mBtn4, 1, 4);
		gridpane.add(mBtn5, 1, 5);
		gridpane.add(mBtn6, 1, 6);
		gridpane.add(mBtn7, 1, 7);

		StackPane root = new StackPane();
		root.getChildren().add(gridpane);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

	private void buttonPress() {
		mVisible = !mVisible;
		System.out.println("mVisisble = " + mVisible);
		mLabel5.setVisible(mVisible);
		mBtn4.setVisible(mVisible);
	}
}