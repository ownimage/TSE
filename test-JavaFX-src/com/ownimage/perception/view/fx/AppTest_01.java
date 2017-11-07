package com.ownimage.perception.view.fx;

import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.AppControlBase;

import javafx.application.Application;

public class AppTest_01 {

	public static void main(final String[] pArgs) {
		FXViewFactory.setAsViewFactory();
		AppControlBase appControl = new AppControlBase("App_Test_01");
		Application app = (AppControlView) appControl.createView();
		app.launch(app.getClass());

	}

}
