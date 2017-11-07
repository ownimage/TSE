package com.ownimage.perception.view.fx;

import java.awt.Color;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.view.javafx.FXView;
import com.ownimage.framework.view.javafx.FXViewFactory;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXApplication7 extends Application {

	static final Integer SIZE = 5;

	boolean mVisible = true;
	boolean mEnabled = true;

	private final DoubleControl[] mDoubleControls = new DoubleControl[SIZE];
	private final IntegerControl[] mIntegerControls = new IntegerControl[SIZE];
	private final BooleanControl[] mBooleanControls = new BooleanControl[SIZE];

	private ActionControl mHideShowButton;

	public static void main(final String[] args) {
		FXViewFactory.setAsViewFactory();
		launch(args);
	}

	private void enableToggle() {
		System.out.println("enableToggle");
		mEnabled = !mEnabled;
		mIntegerControls[2].setEnabled(mEnabled);
		mDoubleControls[2].setEnabled(mEnabled);
		mBooleanControls[2].setEnabled(mEnabled);
		mHideShowButton.setEnabled(mEnabled);
	}

	private void hideShow() {
		System.out.println("hideShow");
		mVisible = !mVisible;
		mIntegerControls[2].setVisible(mVisible);
		mDoubleControls[2].setVisible(mVisible);
		mBooleanControls[2].setVisible(mVisible);
		mHideShowButton.setVisible(mVisible);
	}

	private void printValues() {
		for (int i = 0; i < SIZE; i++) {
			{
				DoubleControl control = mDoubleControls[i];
				System.out.println(control.getDisplayName() + " = " + control.getValue());
			}

			{
				IntegerControl control = mIntegerControls[i];
				System.out.println(control.getDisplayName() + " = " + control.getValue());
			}
		}
	}

	@Override
	public void start(final Stage primaryStage) {
		Container container = new Container("x", "x");
		primaryStage.setTitle("Hello World!");
		VBox vbox = new VBox();

		Button printButton = new Button("Print values");
		printButton.setOnAction((e) -> printValues());
		vbox.getChildren().add(printButton);

		Button enablToggleButton = new Button("Enable Toggle");
		enablToggleButton.setOnAction((e) -> enableToggle());
		vbox.getChildren().add(enablToggleButton);

		Button hideShowButton = new Button("Hide/Show");
		hideShowButton.setOnAction((e) -> hideShow());
		vbox.getChildren().add(hideShowButton);

		ColorControl colorControl = new ColorControl("x", "x", container, Color.ORANGE);

		mHideShowButton = new ActionControl("hideshow", "hideshow", container, () -> hideShow());
		// vbox.getChildren().add(((FXView) (mHideShowButton.createView())).getUI());

		// vbox.getChildren().add(((FXView) (colorControl.createView())).getUI());
		// vbox.getChildren().add(((FXView) (colorControl.createView())).getUI());

		for (int i = 0; i < SIZE; i++) {
			{
				String name = "DoubleControl" + i;
				DoubleControl control = new DoubleControl(name, name, container, i / 10.0d);
				mDoubleControls[i] = control;
				// vbox.getChildren().add(((FXView) (control.createView())).getUI());
				// vbox.getChildren().add(((FXView) (control.createView())).getUI());
			}

			{
				String name = "IntegerControl" + i;
				IntegerControl control = new IntegerControl(name, name, container, i);
				mIntegerControls[i] = control;

				// vbox.getChildren().add(((FXView) (control.createView())).getUI());
				// vbox.getChildren().add(((FXView) (control.createView())).getUI());
			}

			{
				String name = "BooleanControl" + i;
				BooleanControl control = new BooleanControl(name, name, container, ((i & 1) == 1));
				mBooleanControls[i] = control;

				// vbox.getChildren().add(((FXView) (control.createView())).getUI());
				// vbox.getChildren().add(((FXView) (control.createView())).getUI());
			}
		}

		vbox.getChildren().add(((FXView) (container.createView())).getUI());

		StackPane root = new StackPane();
		root.getChildren().add(vbox);
		primaryStage.setScene(new Scene(root, 300, 250));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

}