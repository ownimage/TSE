package com.ownimage.perception.view.javafx.app;

import java.io.File;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.Perception;

import javafx.application.Platform;
import javafx.scene.image.Image;

public class FXPerception extends AppControlView {

	public static void main(final String[] pArgs) {
		FrameworkLogger.getInstance().init("logging.properies", "log\\Perception.log");

		FXViewFactory.setAsViewFactory();

		Perception app = new Perception();
		setAppControl(app);
		launch(pArgs);

		Image applicationIcon = new Image(app.getClass().getResourceAsStream("/Perception.png"));
		getAppControlView().setApplicationIcon(applicationIcon);

		Platform.runLater(() -> app.fileOpen(new File("NY2.jpg")));
	}

}
