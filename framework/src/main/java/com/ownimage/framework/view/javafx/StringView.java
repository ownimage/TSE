/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.StringControl;
import com.ownimage.framework.util.Framework;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.logging.Logger;

public class StringView extends ViewBase<StringControl> implements ChangeListener<Number> {


    public final static Logger mLogger = Framework.getLogger();

    private HBox mUI;
    private TextField mText;
    private Label mTitle;

    private SimpleIntegerProperty mWidth;

    private boolean mAllowUpdates = true;

    public StringView(final StringControl pStringControl) {
        super(pStringControl);
        Framework.logEntry(mLogger);

        switch (pStringControl.getMetaType().getDisplayType()) {
            case LABEL:
                createLabel();
                break;
            case NORMAL:
                createNormal();
                break;
            case TITLE:
                createTitle();
                break;
            default:
                createNormal();
                break;
        }

        Framework.logExit(mLogger);
    }

    @Override
    public void changed(final ObservableValue<? extends Number> pObservable, final Number pOldValue, final Number pNewValue) {
        setWidth();
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mControl) {
            runOnFXApplicationThread(() -> {
                switch (mControl.getMetaType().getDisplayType()) {
                    case LABEL:
                        mLabel.setText(mControl.getValue());
                        break;
                    case NORMAL:
                        mText.setText(mControl.getValue());
                        break;
                    case TITLE:
                        mLabel.setText(mControl.getValue());
                        break;
                    default:
                        mText.setText(mControl.getValue());
                        break;
                }
            });
        }
    }

    private void createTitle() {
        setWidth();
        mTitle = new Label(mControl.getValue());
        mTitle.setStyle("-fx-font-weight: bold");
        mTitle.setWrapText(true);
        mTitle.alignmentProperty().setValue(Pos.CENTER);
        mTitle.prefWidthProperty().bind(mWidth);
        mTitle.minWidthProperty().bind(mWidth);
        mTitle.maxWidthProperty().bind(mWidth);

        mUI = new HBox();
        mUI.getChildren().addAll(mTitle);
    }

    private void createLabel() {
        getFactory().controlWidthProperty.addListener(this);
        getFactory().labelWidthProperty.addListener(this);
        setWidth();

        int rightPadding = getFactory().labelRightPaddingProperty.intValue();
        getFactory().labelRightPaddingProperty.addListener(this);

        mLabel = new Label(mControl.getValue());
        mLabel.prefWidthProperty().bind(mWidth);
        mLabel.minWidthProperty().bind(mWidth);
        mLabel.maxWidthProperty().bind(mWidth);

        mLabel.prefWidthProperty().bind(getFactory().controlWidthProperty);
        mLabel.prefWidthProperty().bind(getFactory().controlWidthProperty);
        mLabel.prefWidthProperty().bind(getFactory().controlWidthProperty);

        mLabel.setWrapText(true);

        mUI = new HBox();
        mUI.setAlignment(Pos.CENTER);
        mUI.getChildren().addAll(mLabel);
    }

    private void createNormal() {
        mText = new TextField();
        mText.setText(mControl.getValue());
        mText.textProperty().addListener((observable, oldValue, newValue) -> setControlValue(newValue));
        mText.prefWidthProperty().bind(getFactory().controlWidthProperty);

        mUI = new HBox();
        mUI.setAlignment(Pos.CENTER);
        mUI.getChildren().addAll(mLabel, mText);
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

    private void setControlValue(final String pValue) {
        try {
            if (mAllowUpdates) {
                mControl.setValue(pValue, this, false);
                mAllowUpdates = false;
                mText.setText(mControl.getValue());
                // TODO should use the IView option
            }
        } finally {
            mAllowUpdates = true;
        }
    }

    private void setWidth() {
        int width = getFactory().controlWidthProperty.getValue() + getFactory().labelWidthProperty.getValue();
        if (mWidth == null) {
            mWidth = new SimpleIntegerProperty(width);
        }
        mWidth.set(width);
    }

}
