package com.ownimage.perception.view.fx;

import java.awt.Color;
import java.util.Iterator;

import com.ownimage.framework.app.menu.IMenuItem;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.NamedTabs;
import com.ownimage.framework.view.javafx.FXView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.AppControlBase;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXApplication9 extends Application {

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

	private MenuBar createMenu(final MenuControl pMenuBar) {
		if (!pMenuBar.isMenuBar()) {
			throw new IllegalArgumentException("pMenuBar must be a Menu with the isMenuBar set.");
		}

		if (!pMenuBar.isLocked()) {
			throw new IllegalArgumentException("pMenuBar must locked.");
		}

		MenuBar menuBar = new MenuBar();

		Iterator<IMenuItem> menuIterator = pMenuBar.getChildIterator();
		while (menuIterator.hasNext()) {
			MenuControl menuControl = (MenuControl) menuIterator.next();
			menuBar.getMenus().add(createSubMenu(menuControl));
			System.out.println("Menu created: " + menuControl.getDisplayName());
		}

		return menuBar;
	}

	private Menu createSubMenu(final MenuControl pMenu) {
		Menu menu = new Menu(pMenu.getDisplayName());

		Iterator<IMenuItem> childIterator = pMenu.getChildIterator();
		while (childIterator.hasNext()) {
			IMenuItem child = childIterator.next();

			if (child instanceof ActionControl) {
				ActionControl actionControl = (ActionControl) child;
				MenuItem actionItem = new MenuItem(actionControl.getDisplayName());
				actionItem.setOnAction(e -> actionControl.performAction());
				menu.getItems().add(actionItem);
				System.out.println("Menu created: " + actionControl.getDisplayName());

			} else if (child instanceof MenuControl) {
				MenuControl menuControl = (MenuControl) child;
				System.out.println("Menu created: " + menuControl.getDisplayName());
				menu.getItems().add(createSubMenu(menuControl));
			}
		}

		return menu;
	}

	private void editSubMenuActionMethod() {
		System.out.println("editSubMenuActionMethod");
	}

	private void enableToggle() {
		System.out.println("enableToggle");
		mEnabled = !mEnabled;
		mIntegerControls[2].setEnabled(mEnabled);
		mDoubleControls[2].setEnabled(mEnabled);
		mBooleanControls[2].setEnabled(mEnabled);
		mHideShowButton.setEnabled(mEnabled);
	}

	private void fileMenuActionMethod() {
		System.out.println("fileMenuActionMethod");
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

		Container menuContainer = new Container("x", "x");

		ActionControl fileMenuAction = new ActionControl("fileMenuAction", "fileMenuAction", menuContainer, () -> fileMenuActionMethod());
		MenuControl fileMenu = new MenuControl("File");
		fileMenu.addAction(fileMenuAction);

		ActionControl editSubMenuAction = new ActionControl("editSubMenuAction", "editSubMenuActiion", menuContainer, () -> editSubMenuActionMethod());
		MenuControl editSubMenu = new MenuControl("Edit Sub-Menu");
		editSubMenu.addAction(editSubMenuAction);
		editSubMenu.lock();

		MenuControl editMenu = new MenuControl("Edit");
		editMenu.addMenu(editSubMenu);

		fileMenu.lock();
		editMenu.lock();

		MenuControl menuBar = new MenuControl();
		menuBar.addMenu(fileMenu);
		menuBar.addMenu(editMenu);
		menuBar.lock();

		Container container = new Container("x", "x");
		Container container2 = new Container("x", "x");
		primaryStage.setTitle("Hello World!");
		VBox vbox = new VBox();

		// Button printButton = new Button("Print values");
		// printButton.setOnAction((e) -> printValues());
		// vbox.getChildren().add(printButton);
		//
		// Button enablToggleButton = new Button("Enable Toggle");
		// enablToggleButton.setOnAction((e) -> enableToggle());
		// vbox.getChildren().add(enablToggleButton);
		//
		// Button hideShowButton = new Button("Hide/Show");
		// hideShowButton.setOnAction((e) -> hideShow());
		// vbox.getChildren().add(hideShowButton);

		ColorControl colorControl = new ColorControl("x", "x", container, Color.ORANGE);

		NamedTabs namedTabs = new NamedTabs();
		namedTabs.addTab("one", container);
		namedTabs.addTab("two", container2);
		namedTabs.setSelectedIndex(1);

		// HFlow hflow = new HFlow(namedTabs, namedTabs);
		HSplitLayout hsplit = new HSplitLayout(namedTabs, namedTabs);

		// vbox.getChildren().add(((FXView) (namedTabs.createView())).getUI());
		vbox.getChildren().add(((FXView) (hsplit.createView())).getUI());

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

		// for menu
		BorderPane border = new BorderPane();
		AppControlBase app = new AppControlBase("Test Application");

		MenuBar menu = createMenu(app.getMenu());
		menu.prefWidthProperty().bind(primaryStage.widthProperty());
		border.setTop(menu);

		StackPane root = new StackPane();
		root.getChildren().add(vbox);
		primaryStage.setScene(new Scene(border, 300, 250));
		primaryStage.sizeToScene();
		primaryStage.show();
	}

}