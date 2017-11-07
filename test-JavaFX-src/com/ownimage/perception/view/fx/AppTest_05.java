package com.ownimage.perception.view.fx;

import java.awt.Color;

import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.NamedTabs;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.AppControlBase;

import javafx.application.Application;

public class AppTest_05 extends AppControlBase {

	public static void main(final String[] pArgs) {
		FXViewFactory.setAsViewFactory();
		AppControlBase appControl = new AppTest_05();
		Application app = (AppControlView) appControl.createView();
		app.launch(app.getClass());
	}

	private MenuControl menu21;
	private MenuControl menu22;
	private MenuControl menu23;
	private ActionControl action31;
	private ActionControl action32;

	private ActionControl action33;

	public AppTest_05() {
		super("App_Test_05");
	}

	@Override
	protected IView createContentView() {
		Container container = new Container("x", "x");
		ColorControl colorControl = new ColorControl("x", "x", container, Color.BLUE);
		FileControl fileControl = new FileControl("file control", "fileControl", container, "someFileName", FileControl.FileControlType.FILE);

		NamedTabs namedTabs = new NamedTabs();
		namedTabs.addTab("one", container);
		namedTabs.addTab("two", container);
		namedTabs.setSelectedIndex(1);

		HSplitLayout hsplit = new HSplitLayout(namedTabs, namedTabs);

		return hsplit.createView();
	}

	@Override
	protected MenuControl createMenuView() {
		Container menuContainer = new Container("AppControlBase", "AppControlBase");

		MenuControl menu = new MenuControl()
				.addMenu(new MenuControl("One")
						.addAction(new ActionControl("enable 2 2", "e22", menuContainer, () -> enable22()))
						.addAction(new ActionControl("disable 2 2", "d22", menuContainer, () -> disable22()))
						.addAction(new ActionControl("hide 2 2", "h22", menuContainer, () -> hide22()))
						.addAction(new ActionControl("show 2 2", "s22", menuContainer, () -> show22()))
						.addAction(new ActionControl("enable 3 2", "e32", menuContainer, () -> enable32()))
						.addAction(new ActionControl("disable 3 2", "d32", menuContainer, () -> disable32()))
						.addAction(new ActionControl("hide 3 2", "h32", menuContainer, () -> hide32()))
						.addAction(new ActionControl("show 3 2", "s32", menuContainer, () -> show32()))
						.lock())
				.addMenu(new MenuControl("Two")
						.addMenu(menu21 = new MenuControl("21")
								.addAction(new ActionControl("action 2 1 1", "a211", menuContainer, () -> noAction()))
								.addAction(new ActionControl("action 2 1 2", "a212", menuContainer, () -> noAction()))
								.lock())
						.addMenu(menu22 = new MenuControl("22")
								.addAction(new ActionControl("action 2 2 1", "a221", menuContainer, () -> noAction()))
								.addAction(new ActionControl("action 2 2 2", "a222", menuContainer, () -> noAction()))
								.lock())
						.addMenu(menu23 = new MenuControl("23").lock())
						.lock())
				.addMenu(new MenuControl("Three")
						.addAction(action31 = new ActionControl("action 3 1", "a31", menuContainer, () -> noAction()))
						.addAction(action32 = new ActionControl("action 3 2", "a32", menuContainer, () -> print32()))
						.addAction(action33 = new ActionControl("action 3 3", "a33", menuContainer, () -> noAction()))
						.lock())
				.lock();

		return menu;
	}

	private void disable22() {
		menu22.setEnabled(false);
	}

	private void disable32() {
		action32.setEnabled(false);
	}

	private void enable22() {
		menu22.setEnabled(true);
	}

	private void enable32() {
		action32.setEnabled(true);
	}

	private void hide22() {
		menu22.setVisible(false);
	}

	private void hide32() {
		action32.setVisible(false);
	}

	private void noAction() {
	}

	private void print32() {
		System.out.println("print32");
	}

	private void show22() {
		menu22.setVisible(true);
	}

	private void show32() {
		action32.setVisible(true);
	}
}
