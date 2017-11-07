package com.ownimage.perception.view.fx;

import java.util.Iterator;

import com.ownimage.framework.app.menu.IMenuItem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/*from ww w.  j  ava  2s . co  m*/
public class MenuTest extends Application {

	public static void main(final String[] args) {
		launch(args);
	}

	private MenuBar createMenu(final com.ownimage.framework.app.menu.MenuControl pMenuBar) {
		if (!pMenuBar.isMenuBar()) {
			throw new IllegalArgumentException("pMenuBar must be a Menu with the isMenuBar set.");
		}
		;

		if (!pMenuBar.isLocked()) {
			throw new IllegalArgumentException("pMenuBar must locked.");
		}
		;

		MenuBar menuBar = new MenuBar();

		Iterator<IMenuItem> menuIterator = pMenuBar.getChildIterator();
		while (menuIterator.hasNext()) {
			com.ownimage.framework.app.menu.MenuControl menu = (com.ownimage.framework.app.menu.MenuControl) menuIterator.next();
			MenuItem menuItem = new MenuItem(menu.getDisplayName());
			System.out.println("Menu created: " + menu.getDisplayName());
		}

		return menuBar;
	}

	@Override
	public void start(final Stage primaryStage) {
		BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 300, 250, Color.WHITE);

		MenuBar menuBar = new MenuBar();
		menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
		root.setTop(menuBar);

		// File menu - new, save, exit
		Menu fileMenu = new Menu("File");
		MenuItem newMenuItem = new MenuItem("New");
		MenuItem saveMenuItem = new MenuItem("Save");
		MenuItem exitMenuItem = new MenuItem("Exit");
		exitMenuItem.setOnAction(actionEvent -> Platform.exit());

		fileMenu.getItems().addAll(newMenuItem, saveMenuItem,
				new SeparatorMenuItem(), exitMenuItem);

		Menu webMenu = new Menu("Web");
		CheckMenuItem htmlMenuItem = new CheckMenuItem("HTML");
		htmlMenuItem.setSelected(true);
		webMenu.getItems().add(htmlMenuItem);

		CheckMenuItem cssMenuItem = new CheckMenuItem("CSS");
		cssMenuItem.setSelected(true);
		webMenu.getItems().add(cssMenuItem);

		Menu sqlMenu = new Menu("SQL");
		ToggleGroup tGroup = new ToggleGroup();
		RadioMenuItem mysqlItem = new RadioMenuItem("MySQL");
		mysqlItem.setToggleGroup(tGroup);

		RadioMenuItem oracleItem = new RadioMenuItem("Oracle");
		oracleItem.setToggleGroup(tGroup);
		oracleItem.setSelected(true);

		sqlMenu.getItems().addAll(mysqlItem, oracleItem,
				new SeparatorMenuItem());

		Menu tutorialManeu = new Menu("Tutorial");
		tutorialManeu.getItems().addAll(
				new CheckMenuItem("Java"),
				new CheckMenuItem("JavaFX"),
				new CheckMenuItem("Swing"));

		sqlMenu.getItems().add(tutorialManeu);

		menuBar.getMenus().addAll(fileMenu, webMenu, sqlMenu);

		primaryStage.setScene(scene);
		primaryStage.show();
	}

}