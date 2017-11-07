package com.ownimage.perception.view.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXApplication3 extends Application {

	static final Integer SIZE = 10;

	private boolean mVisible = true;

	private Stage mStage;

	private Button[] mBtn = new Button[SIZE];
	private Label[] mLabel = new Label[SIZE];

	private VBox mVBox;

	private BorderPane mBorder[] = new BorderPane[SIZE];

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
	public void start(Stage pStage) {
		mStage = pStage;
		mStage.setTitle("Hello World!");

		mVBox = new VBox();

		for (int i = 0; i < SIZE; i++) {
			mBorder[i] = new BorderPane();
			final Integer id = i;
			mLabel[i] = new Label("Label " + i);
			mBorder[i].setTop(mLabel[i]);

			mBtn[i] = createButton(i);
			mBtn[i].setOnAction((event) -> buttonPress(id));
			mBorder[i].setBottom(mBtn[i]);

			mVBox.getChildren().add(mBorder[i]);
		}

		StackPane root = new StackPane();
		root.getChildren().add(mVBox);
		mStage.setScene(new Scene(root, 500, 800));
		mStage.sizeToScene();
		mStage.show();
	}

	private void buttonPress(int pI) {
		mVisible = !mVisible;
		System.out.println(pI + " mVisisble2 = " + mVisible);
		// mLabel[3].setVisible(mVisible);
		// mBtn[3].setVisible(mVisible);
		// mBorder[3].setVisible(mVisible);
		mBorder[3].setManaged(mVisible);

		mVBox.layout();
		mStage.sizeToScene();
	}
}