/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.view.javafx.app;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Services;
import javafx.scene.image.Image;

import java.io.File;

// need to add the following to VM Options in IntelliJ
// --module-path C:\Users\User\Downloads\openjfx-11.0.2_windows-x64_bin-sdk\javafx-sdk-11.0.2\lib --add-modules=javafx.controls

public class FXPerception extends AppControlView {

    public static void main(final String[] pArgs) {
        FrameworkLogger.getInstance().init("logging.properties", "Perception.log");
        Thread.currentThread().setName("main");
        FXViewFactory.setAsViewFactory();

        final Perception app = Services.getServices().getPerception();
        setAppControl(app);
        launch(pArgs);

        final Image applicationIcon = new Image(app.getClass().getResourceAsStream("/icon/tse2.png"));
        getAppControlView().setApplicationIcon(applicationIcon);

        if (pArgs != null && pArgs.length != 0) {
            String filename = pArgs[0];
            System.out.println(filename);
            app.fileOpen((new File(filename)));
        }

    }

}
