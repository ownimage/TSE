/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IEnabledListener;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.event.UIEvent;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public class DialogView implements IDialogView {

    public final static Logger mLogger = Framework.getLogger();

    private final IViewable mViewable;
    final private IAppControlView.DialogOptions mDialogOptions;
    private UndoRedoBuffer mUndoRedo;
    private final ActionControl[] mButtons;

    public DialogView(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        mViewable = pViewable;
        mDialogOptions = pDialogOptions;
        mUndoRedo = pUndoRedo;
        mButtons = pButtons;
    }

    @Override
    public void showModal() {
        Platform.runLater(() -> {
            Framework.checkStateNotNull(mLogger, mViewable, "pViewable");
            Framework.checkStateNotNull(mLogger, mDialogOptions, "pDialogOptions");

            val buttonMap = new HashMap<ButtonType, ActionControl>();
            val content = (FXView) (mViewable.createView());
            val contentUI = content.getUI();
            val dialog = new Dialog<ActionControl>();

            // the width listener is needed in case the mDialog is showing the UI controls that affect the width of the controls
            // themselves which would mean that the mDialog would need to change size as the controls change value.
            final ChangeListener<? super Number>
                    widthListener = (observable, oldValue, newValue) ->
                    dialog.setWidth(dialog.getWidth() + newValue.doubleValue() - oldValue.doubleValue());

            FXViewFactory.getInstance().controlWidthProperty.addListener(widthListener);
            FXViewFactory.getInstance().labelWidthProperty.addListener(widthListener);

            dialog.setTitle(mViewable.getDisplayName());
            dialog.getDialogPane().setContent(contentUI);
            dialog.getDialogPane().layout();
            dialog.setResultConverter(buttonMap::get);
            dialog.initOwner(AppControlView.getInstance().getPrimaryStage());
            dialog.getDialogPane().getScene().getWindow().sizeToScene();

            val stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(AppControlView.getInstance().getApplicationIcon());
            mDialogOptions.getCompleteFunction().ifPresent(cf -> stage.setOnCloseRequest(x -> cf.performAction()));

            val enabledListeners = new ArrayList<IEnabledListener>(); // prevent garbage collection of listener
            for (val action : mButtons) {
                val button = new ButtonType(action.getDisplayName(), ButtonBar.ButtonData.OK_DONE);
                buttonMap.put(button, action);
                dialog.getDialogPane().getButtonTypes().add(button);
                dialog.getDialogPane().lookupButton(button).setDisable(!action.isEnabled());
                IEnabledListener listener = (c, v) -> dialog.getDialogPane().lookupButton(button).setDisable(!v);
                enabledListeners.add(listener);
                action.addEnabledListener(listener);
            }

            dialog.getDialogPane().setOnKeyPressed(pKE -> {
                if (mUndoRedo != null) {
                    val undo = new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN);
                    val redo = new KeyCodeCombination(KeyCode.Y, KeyCodeCombination.CONTROL_DOWN);

                    mLogger.finest("KeyPressed");
                    if (undo.match(pKE)) {
                        mLogger.finest("Undo");
                        mUndoRedo.undo();

                    } else if (redo.match(pKE)) {
                        mLogger.finest("Redo");
                        mUndoRedo.redo();
                    }
                }
                if (mViewable instanceof IUIEventListener) {
                    val listener = (IUIEventListener) mViewable;
                    val event = UIEvent.createKeyEvent(UIEvent.EventType.KeyPressed, null, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyPressed(event);
                }
            });

            dialog.getDialogPane().setOnKeyReleased(pKE -> {
                if (mViewable instanceof IUIEventListener) {
                    val listener = (IUIEventListener) mViewable;
                    val event = UIEvent.createKeyEvent(UIEvent.EventType.KeyReleased, null, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyReleased(event);
                }
            });

            dialog.getDialogPane().setOnKeyTyped(pKE -> {
                if (mViewable instanceof IUIEventListener) {
                    val listener = (IUIEventListener) mViewable;
                    val event = UIEvent.createKeyEvent(UIEvent.EventType.KeyTyped, null, pKE.getCharacter().toUpperCase(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyTyped(event);
                }
            });

            val dialogResult = dialog.showAndWait();

            new Thread(() -> {
                // this needs to be done here as the complete function might not be specified.
                dialogResult.ifPresent(ActionControl::performAction);

                // the value is passed into the completeFunction only to indicate how the mDialog ended.
                mDialogOptions.getCompleteFunction().ifPresent(IAction::performAction);
            }).start();
        });
    }
}
