package com.ownimage.imageOrganizer.view.javafx.app;

import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.imageOrganizer.app.ImageOrganizer;

public class FXImageOrganizer {

	public static void main(final String[] pArgs) {
		FXViewFactory.setAsViewFactory();
		ImageOrganizer appControl = ImageOrganizer.getInstance();
		appControl.createView();
	}
}
