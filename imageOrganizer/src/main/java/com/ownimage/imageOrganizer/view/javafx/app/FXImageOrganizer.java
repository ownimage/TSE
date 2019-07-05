package com.ownimage.imageOrganizer.view.javafx.app;

import com.ownimage.framework.view.javafx.AppControlView;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.imageOrganizer.app.ImageOrganizer;

public class FXImageOrganizer extends AppControlView {

    public static void main(final String[] pArgs) {
        FXViewFactory.setAsViewFactory();
        ImageOrganizer appControl = ImageOrganizer.getInstance();
        setAppControl(new ImageOrganizer());
        launch(pArgs);
    }
}
