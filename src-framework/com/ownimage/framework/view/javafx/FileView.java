package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.control.IControl;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class FileView extends ViewBase {

    private final FileControl mFileControl;
    private final HBox mUI;
    private final TextField mTextField;
    private final Button mButton;

    public FileView(final FileControl pFileControl) {
        super(pFileControl);

        mFileControl = pFileControl;
        mFileControl.addControlChangeListener(this);

        mTextField = new TextField(mFileControl.getValue());
        mTextField.setDisable(true);
        mTextField.maxWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);
        mTextField.prefWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);

        mButton = new Button();
        mButton.setOnAction((e) -> performAction());

        HBox box = new HBox();
        box.getChildren().addAll(mTextField, mButton);
        box.prefWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);

        mUI = new HBox();
        mUI.setAlignment(Pos.TOP_LEFT);
        mUI.getChildren().addAll(mLabel, box);
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mFileControl) {
            mTextField.setText(mFileControl.getValue());
        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

    private void performAction() {
        AppControlView.getInstance().showDialog(mFileControl);
    }

}
