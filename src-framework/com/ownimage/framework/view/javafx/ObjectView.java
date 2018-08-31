/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.type.ObjectMetaType;
import com.ownimage.framework.util.Framework;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

public class ObjectView extends ViewBase<ObjectControl<?>> {


    public final static Logger mLogger = Framework.getLogger();

    private final HBox mUI;
    private ComboBox<Object> mCombobox;
    private boolean mAllowUpdates = true;

    public ObjectView(final ObjectControl pObjectControl) {
        super(pObjectControl);

        ObjectMetaType<?> meta = mControl.getMetaType();
        ObservableList<Object> items = FXCollections.observableArrayList(meta.getAllowedValues());
        FilteredList<Object> filteredItems = new FilteredList<>(items, p -> true);

        mCombobox = new ComboBox<>();
        mCombobox.setEditable(meta.isFilterable());
        mCombobox.getEditor().textProperty().addListener((pObs, pOldValue, pNewValue) -> {
            final TextField editor = mCombobox.getEditor();
            final Object selected = mCombobox.getSelectionModel().getSelectedItem();

            if (meta.isFilterable()) {
                // This needs run on the GUI thread to avoid the error described
                // here: https://bugs.openjdk.java.net/browse/JDK-8081700.
                runOnFXApplicationThread(() -> {
                    mLogger.log(Level.FINEST, "runLater newValue = " + pNewValue);
                    if (selected == null || !selected.equals(editor.getText())) {
                        filteredItems.setPredicate(item -> {
                            String itemString = meta.getString(item);
                            return itemString.toUpperCase().contains(pNewValue.toUpperCase());
                        });
                    }
                });
            }
        });
        mCombobox.setConverter(new StringConverter<Object>() {

            @Override
            public Object fromString(final String pString) {
                mLogger.fine(() -> "fromString pString = " + pString);
                Object value = meta.fromString(pString);
                return value;
            }

            @Override
            public String toString(final Object pObject) {
                mLogger.finest(() -> "toString pObject = " + pObject);
                String string = (pObject != null) ? meta.getString(pObject) : "";
                mLogger.finest(() -> "toString return = " + pObject);
                return string;
            }
        });

        mCombobox.setOnAction((final ActionEvent ev) -> {
            if (mAllowUpdates && mCombobox.getSelectionModel().getSelectedItem() != null) {
                final Object value = mCombobox.getSelectionModel().getSelectedItem();
                mControl.setValue(value, this, false);
                mCombobox.editorProperty().get().setText(mCombobox.getConverter().toString(mCombobox.getValue()));
            } else {
                String typed = mCombobox.getEditor().textProperty().get();
                typed = typed; // for debugging
            }
        });

        mCombobox.setItems(filteredItems);
        mCombobox.getSelectionModel().select(mControl.getValue());
        mCombobox.setPromptText(mCombobox.getConverter().toString(mCombobox.getValue()));
        mCombobox.prefWidthProperty().bind(FXViewFactory.getInstance().controlWidthProperty);

        mUI = new HBox();
        mUI.setAlignment(Pos.CENTER);
        mUI.getChildren().addAll(mLabel, mCombobox);
        mUI.setDisable(!mControl.isEnabled());
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mControl) {
            runOnFXApplicationThread(() -> {
                try {
                    mAllowUpdates = false;
                    mCombobox.getSelectionModel().select(mControl.getValue());
                    mCombobox.setPromptText(mCombobox.getConverter().toString(mCombobox.getValue()));
                } finally {
                    mAllowUpdates = true;
                }
            });
        }
    }

    @Override
    public Pane getUI() {
        return mUI;
    }

}
