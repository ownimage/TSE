/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.view.javafx.app;

import java.io.File;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Services;

import javafx.scene.image.Image;

public class FXPerception extends AppControlView {

    public static void main(final String[] pArgs) {
        FrameworkLogger.getInstance().init("logging.properties", "Perception.log");

        FXViewFactory.setAsViewFactory();

        Perception app = Services.getServices().getPerception();
        setAppControl(app);
        launch(pArgs);

        Image applicationIcon = new Image(app.getClass().getResourceAsStream("/icon/tse2.png"));
        getAppControlView().setApplicationIcon(applicationIcon);

        app.fileOpen((new File("NY2.jpg")));
    }

}
