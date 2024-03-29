package com.ownimage.perception.view.fx;

import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.AppControlBase;

import javafx.application.Application;

public class AppTest_02 extends AppControlBase {

	private MenuControl menu21;
	private MenuControl menu22;
	private MenuControl menu23;
	private ActionControl action31;
	private ActionControl action32;
	private ActionControl action33;

	public AppTest_02() {
		super("App_Test_02");
	}

	public static void main(final String[] pArgs) {
		FXViewFactory.setAsViewFactory();
		AppControlBase appControl = new AppTest_02();
		Application app = (AppControlView) appControl.createView();
		app.launch(app.getClass());
	}

	@Override
	protected MenuControl createMenuView() {
		Container menuContainer = new Container("AppControlBase", "AppControlBase");

		MenuControl oneSubMenu = new MenuControl("One") //
				.addAction(new ActionControl("enable 2 2", "e22", menuContainer, () -> enable22())) //
				.addAction(new ActionControl("disable 2 2", "d22", menuContainer, () -> disable22())) //
				.addAction(new ActionControl("hide 2 2", "h22", menuContainer, () -> hide22())) //
				.addAction(new ActionControl("show 2 2", "s22", menuContainer, () -> show22())) //
				.addAction(new ActionControl("enable 3 2", "e32", menuContainer, () -> enable32())) //
				.addAction(new ActionControl("disable 3 2", "d32", menuContainer, () -> disable32())) //
				.addAction(new ActionControl("hide 3 2", "h32", menuContainer, () -> hide32())) //
				.addAction(new ActionControl("show 3 2", "s32", menuContainer, () -> show32()));

		MenuControl twoSubMenu = new MenuControl("Two") //
				.addMenu(menu21 = new MenuControl("21").lock()) //
				.addMenu(menu22 = new MenuControl("22").lock()) //
				.addMenu(menu23 = new MenuControl("23").lock());

		MenuControl threeSubMenu = new MenuControl("Three") //
				.addAction(action31 = new ActionControl("action 3 1", "a31", menuContainer, () -> noAction())) //
				.addAction(action32 = new ActionControl("action 3 2", "a32", menuContainer, () -> noAction())) //
				.addAction(action33 = new ActionControl("action 3 3", "a33", menuContainer, () -> noAction()));

		MenuControl menu = new MenuControl() //
				.addMenu(oneSubMenu.lock()) //
				.addMenu(twoSubMenu.lock()) //
				.addMenu(threeSubMenu.lock()) //
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

	private void show22() {
		menu22.setVisible(true);
	}

	private void show32() {
		action32.setVisible(true);
	}
}
