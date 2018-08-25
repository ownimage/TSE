/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view.javafx;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.event.UIEvent;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

public class DialogView implements IDialogView {


    public final static Logger mLogger = Framework.getLogger();

    final private Dialog<ActionControl> mDialog;
    final private IAppControlView.DialogOptions mDialogOptions;
    final private ChangeListener<? super Number> mWidthListener;

    public DialogView(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        Framework.checkParameterNotNull(mLogger, pViewable, "pViewable");
        Framework.checkParameterNotNull(mLogger, pDialogOptions, "pDialogOptions");
        mDialogOptions = pDialogOptions;

        HashMap<ButtonType, ActionControl> buttonMap = new HashMap<>();
        FXView content = (FXView) (pViewable.createView());
        Node contentUI = content.getUI();
        mDialog = new Dialog<>();

        // the width listener is needed in case the mDialog is showing the UI controls that affect the width of the controls
        // themselves which would mean that the mDialog would need to change size as the controls change value.
        mWidthListener = (observable, oldValue, newValue) ->
                mDialog.setWidth(mDialog.getWidth() + newValue.doubleValue() - oldValue.doubleValue());

        FXViewFactory.getInstance().controlWidthProperty.addListener(mWidthListener);
        FXViewFactory.getInstance().labelWidthProperty.addListener(mWidthListener);

        mDialog.setTitle(pViewable.getDisplayName());
        mDialog.getDialogPane().setContent(contentUI);
        mDialog.getDialogPane().layout();
        mDialog.setResultConverter(buttonMap::get);

        for (ActionControl action : pButtons) {
            ButtonType button = new ButtonType(action.getDisplayName(), ButtonBar.ButtonData.OK_DONE);
            buttonMap.put(button, action);
            mDialog.getDialogPane().getButtonTypes().add(button);
        }


        mDialog.getDialogPane().setOnKeyPressed(pKE -> {
            if (pUndoRedo != null) {
                final KeyCodeCombination undo = new KeyCodeCombination(KeyCode.Z, KeyCodeCombination.CONTROL_DOWN);
                final KeyCodeCombination redo = new KeyCodeCombination(KeyCode.Y, KeyCodeCombination.CONTROL_DOWN);

                mLogger.finest("KeyPressed");
                if (undo.match(pKE)) {
                    mLogger.finest("Undo");
                    pUndoRedo.undo();

                } else if (redo.match(pKE)) {
                    mLogger.finest("Redo");
                    pUndoRedo.redo();
                }
            }
            if (pViewable instanceof IUIEventListener) {
                IUIEventListener listener = (IUIEventListener) pViewable;
                UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyPressed, null, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                listener.keyPressed(event);
            }
        });

        mDialog.getDialogPane().setOnKeyReleased(pKE -> {
            if (pViewable instanceof IUIEventListener) {
                IUIEventListener listener = (IUIEventListener) pViewable;
                UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyReleased, null, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                listener.keyReleased(event);
            }
        });

        mDialog.getDialogPane().setOnKeyTyped(pKE -> {
            if (pViewable instanceof IUIEventListener) {
                IUIEventListener listener = (IUIEventListener) pViewable;
                UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyTyped, null, pKE.getCharacter().toUpperCase(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                listener.keyTyped(event);
            }
        });
    }

    @Override
    public void showModal(){
        final Optional<ActionControl> dialogResult = mDialog.showAndWait();

        new Thread(() -> {
            // this needs to be done here as the complete function might not be specified.
            dialogResult.ifPresent(ActionControl::performAction);

            // the value is passed into the completeFunction only to indicate how the mDialog ended.
            mDialogOptions.getCompleteFunction().ifPresent(IAction::performAction);
        }).start();
    }
}
