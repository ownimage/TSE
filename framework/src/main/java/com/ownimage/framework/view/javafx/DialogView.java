/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.event.UIEvent;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

public class DialogView implements IDialogView {


    public final static Logger mLogger = Framework.getLogger();

    private final IViewable mViewable;
    final private IAppControlView.DialogOptions mDialogOptions;
    UndoRedoBuffer mUndoRedo;
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
            Framework.checkParameterNotNull(mLogger, mViewable, "pViewable");
            Framework.checkParameterNotNull(mLogger, mDialogOptions, "pDialogOptions");

            final HashMap<ButtonType, ActionControl> buttonMap = new HashMap<>();
            final FXView content = (FXView) (mViewable.createView());
            final Node contentUI = content.getUI();
            final Dialog<ActionControl> mDialog = new Dialog<>();

            // the width listener is needed in case the mDialog is showing the UI controls that affect the width of the controls
            // themselves which would mean that the mDialog would need to change size as the controls change value.
            final ChangeListener<? super Number>
                    widthListener = (observable, oldValue, newValue) ->
                    mDialog.setWidth(mDialog.getWidth() + newValue.doubleValue() - oldValue.doubleValue());

            FXViewFactory.getInstance().controlWidthProperty.addListener(widthListener);
            FXViewFactory.getInstance().labelWidthProperty.addListener(widthListener);

            mDialog.setTitle(mViewable.getDisplayName());
            mDialog.getDialogPane().setContent(contentUI);
            mDialog.getDialogPane().layout();
            mDialog.setResultConverter(buttonMap::get);
            mDialog.initOwner(AppControlView.getInstance().getPrimaryStage());

            //set the icon
            final Stage stage = (Stage) mDialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(AppControlView.getInstance().getApplicationIcon());
            mDialogOptions.getCompleteFunction().ifPresent(cf -> stage.setOnCloseRequest(x -> cf.performAction()));

            for (final ActionControl action : mButtons) {
                final ButtonType button = new ButtonType(action.getDisplayName(), ButtonBar.ButtonData.OK_DONE);
                buttonMap.put(button, action);
                mDialog.getDialogPane().getButtonTypes().add(button);
            }


            mDialog.getDialogPane().setOnKeyPressed(pKE -> {
                if (mUndoRedo != null) {
                    final KeyCodeCombination undo = new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN);
                    final KeyCodeCombination redo = new KeyCodeCombination(KeyCode.Y, KeyCodeCombination.CONTROL_DOWN);

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
                    final IUIEventListener listener = (IUIEventListener) mViewable;
                    final UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyPressed, null, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyPressed(event);
                }
            });

            mDialog.getDialogPane().setOnKeyReleased(pKE -> {
                if (mViewable instanceof IUIEventListener) {
                    final IUIEventListener listener = (IUIEventListener) mViewable;
                    final UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyReleased, null, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyReleased(event);
                }
            });

            mDialog.getDialogPane().setOnKeyTyped(pKE -> {
                if (mViewable instanceof IUIEventListener) {
                    final IUIEventListener listener = (IUIEventListener) mViewable;
                    final UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyTyped, null, pKE.getCharacter().toUpperCase(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyTyped(event);
                }
            });

            final Optional<ActionControl> dialogResult = mDialog.showAndWait();

            new Thread(() -> {
                System.out.println("############ Dialog complete");
                dialogResult.ifPresent(ActionControl::performAction);
                mDialogOptions.getCompleteFunction().ifPresent(IAction::performAction);
            }).start();
        });
    }
}
