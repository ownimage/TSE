package com.ownimage.perception.view.fx;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.AppControlBase;

import javafx.application.Application;

public class AppTest_Border extends AppControlBase {

	Container container = new Container("x", "x");

	ActionControl actionControlTop = new ActionControl("Top", "top", container, () -> actionTop());
	ActionControl actionControlTop2 = new ActionControl("Top2", "top2", container, () -> actionTop2());
	ActionControl actionControlBottom = new ActionControl("Bottom", "bottom", container, () -> actionBottom());
	ActionControl actionControlLeft = new ActionControl("Left", "left", container, () -> actionLeft());
	ActionControl actionControlRight = new ActionControl("Right", "right", container, () -> actionRight());
	ActionControl actionControlCenter = new ActionControl("Center", "center", container, () -> actionCenter());

	BorderLayout border = new BorderLayout();

	public AppTest_Border() {
		super("App_Test_05");
	}

	public static void main(final String[] pArgs) {
		FXViewFactory.setAsViewFactory();
		AppControlBase appControl = new AppTest_Border();
		Application app = (AppControlView) appControl.createView();
		Application.launch(app.getClass());
	}

	private Object actionBottom() {
		// TODO Auto-generated method stub
		return null;
	}

	private Object actionCenter() {
		// TODO Auto-generated method stub
		return null;
	}

	private void actionLeft() {
		border.setLeft(actionControlRight);
	}

	private Object actionRight() {
		// TODO Auto-generated method stub
		return null;
	}

	private void actionTop() {
		border.setTop(actionControlTop2);
	}

	private void actionTop2() {
		border.setTop(null);
	}

	@Override
	protected IView createContentView() {

		border.setTop(actionControlTop);
		border.setBottom(actionControlBottom);
		border.setLeft(actionControlLeft);
		border.setRight(actionControlRight);
		border.setCenter(actionControlCenter);

		return border.createView();
	}
}
