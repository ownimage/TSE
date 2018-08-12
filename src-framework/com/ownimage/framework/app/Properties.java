package com.ownimage.framework.app;

import java.awt.*;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.type.DoubleMetaType;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;

public class Properties {


    private final static Logger mLogger = Framework.getLogger();

	private final Container mColorContainer = new Container("Colors", "color", () -> {
		return new UndoRedoBuffer(10);
	});
	private final DoubleControl mFontSize = new DoubleControl("Font Size", "fontSize", mColorContainer, 10.0, new DoubleMetaType(0.0d, 100.0d));
	private final ColorControl mColor1 = new ColorControl("Color 1", "color1", mColorContainer, Color.RED);
	private final ColorControl mColor2 = new ColorControl("Color 2", "color2", mColorContainer, Color.ORANGE);
	private final ColorControl mColor3 = new ColorControl("Color 3", "color3", mColorContainer, Color.GREEN);
	private final ColorControl mColorOOB = new ColorControl("Out of Bounds Color", "colorOOB", mColorContainer, Color.PINK);

	public Color getColor1() {
		return mColor1.getValue();
	}

	public Color getColor2() {
		return mColor2.getValue();
	}

	public Color getColor3() {
		return mColor3.getValue();
	}

	public double getFontSize() {
		return mFontSize.getValue();
	}

	public double getJpgQuality() {
		return 1.0d;
	}

	public Color getOutOfBoundsColor() {
		return mColorOOB.getValue();
	}

	public int getPreviewSize() {
		return 500;
	}

}
