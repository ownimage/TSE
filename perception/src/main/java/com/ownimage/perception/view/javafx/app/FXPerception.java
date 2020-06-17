/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.view.javafx.app;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Services;
import javafx.scene.image.Image;

import java.io.File;
import java.util.logging.Logger;

import static java.lang.String.format;

// need to add the following to VM Options in IntelliJ
// --module-path C:\Users\User\Downloads\openjfx-11.0.2_windows-x64_bin-sdk\javafx-sdk-11.0.2\lib --add-modules=javafx.controls

public class FXPerception extends AppControlView {

    private final static Logger mLogger = Framework.getLogger();

    public static void main(String[] pArgs) {
        FrameworkLogger.getInstance().init("logging.properties", "Perception.log");
        Thread.currentThread().setName("main");
        FXViewFactory.setAsViewFactory();

        Perception app = Services.getServices().getPerception();
        app.setWidth(2000);
        app.setHeight(900);
        setAppControl(app);
        launch(pArgs);

        Image applicationIcon = new Image(app.getClass().getResourceAsStream("/icon/tse2.png"));
        getAppControlView().setApplicationIcon(applicationIcon);

        if (pArgs == null || pArgs.length == 0) {
            mLogger.info("Opening last file.");
            app.fileOpen(0);
        }
        if (pArgs != null && pArgs.length == 1) {
            try {
                int index = Integer.parseInt(pArgs[0]);
                mLogger.info(format("Opening file n = %s.", index));
                app.fileOpen(index);

            } catch (Exception pEx) {
                String filename = pArgs[0];
                mLogger.info(format("Opening filename = %s.", filename));
                app.fileOpen((new File(filename)));
            }
        }

    }

}
