package com.ownimage.perception.view.javafx.app;

import java.io.File;

import javafx.application.Platform;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.Perception;

public class FXPerception extends AppControlView {

	public static void main(final String[] pArgs) {
		try {
			FrameworkLogger.getInstance().init("logging.properies", "log\\Perception.log");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FXViewFactory.setAsViewFactory();

		Perception app = new Perception();
		setAppControl(app);
		launch(pArgs);
		Platform.runLater(() -> app.fileOpen(new File("NY2.jpg")));
	}

}
