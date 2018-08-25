/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import java.awt.*;

import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.IControl;

import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class ColorView extends ViewBase<ColorControl> {

    private final HBox mUI;

    private final ColorPicker mColorPicker;

    public ColorView(final ColorControl pColorControl) {
        super(pColorControl);

        mColorPicker = new ColorPicker();
        mColorPicker.setValue(getFxColor());
        mColorPicker.prefWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);
        mColorPicker.setOnAction((pActionEvent) -> setControlValue());

        mUI = new HBox();
        mUI.getChildren().addAll(mLabel, mColorPicker);
    }

    public final static String toHexString(final Color colour) throws NullPointerException {
        String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
        if (hexColour.length() < 6) {
            hexColour = "000000".substring(0, 6 - hexColour.length()) + hexColour;
        }
        return "#" + hexColour;
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mControl) {
            mColorPicker.setValue(getFxColor());
        }
    }

    private javafx.scene.paint.Color getFxColor() {
        Color awtColor = mControl.getValue();
        double r = awtColor.getRed() / 255.0d;
        double g = awtColor.getGreen() / 255.0d;
        double b = awtColor.getBlue() / 255.0d;
        double a = awtColor.getAlpha() / 255.0d;
        return new javafx.scene.paint.Color(r, g, b, a);
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

    private void setControlValue() {
        javafx.scene.paint.Color c = mColorPicker.getValue();
        Color color = new java.awt.Color((int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
        mControl.setValue(color, this, false);
    }

}
