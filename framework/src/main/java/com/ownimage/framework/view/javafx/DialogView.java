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
import com.ownimage.framework.util.StrongReference;
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
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

import static com.ownimage.framework.util.Doable.convertToRuntimeException;

public class DialogView implements IDialogView {

    public final static Logger mLogger = Framework.getLogger();

    private final IViewable mViewable;
    private final IAppControlView.DialogOptions mDialogOptions;
    private final ActionControl[] mButtons;
    private UndoRedoBuffer mUndoRedo;

    @Getter
    @Setter
    private Dialog mDialog;

    public DialogView(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        mViewable = pViewable;
        mDialogOptions = pDialogOptions;
        mUndoRedo = pUndoRedo;
        mButtons = pButtons;
    }

    public DialogView(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final ActionControl... pButtons) {
        mViewable = pViewable;
        mDialogOptions = pDialogOptions;
        mButtons = pButtons;
    }

    @Override
    public void showModal(final UndoRedoBuffer pUndoRedo) {
        mUndoRedo = pUndoRedo;
        showModal();
    }

    @Override
    public void showModal() {
        val isFxApplicationThread = Platform.isFxApplicationThread();
        val lock = new Object();
        val dialogResult = new StrongReference<Optional<IAction>>(null);

        Runnable runDialog = () -> {
            Framework.checkStateNotNull(mLogger, mViewable, "pViewable");
            Framework.checkStateNotNull(mLogger, mDialogOptions, "pDialogOptions");

            val buttonMap = new HashMap<ButtonType, IAction>();
            val content = (FXView) (mViewable.createView());
            val contentUI = content.getUI();
            val dialog = new Dialog<IAction>();
            setDialog(dialog);

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

            val stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(AppControlView.getInstance().getApplicationIcon());
            stage.setOnCloseRequest(x -> {
            }); // without this the dialog does not close when pressing X

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
                    val event = UIEvent.createKeyEvent(UIEvent.EventType.KeyPressed, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyPressed(event);
                }
            });

            dialog.getDialogPane().setOnKeyReleased(pKE -> {
                if (mViewable instanceof IUIEventListener) {
                    val listener = (IUIEventListener) mViewable;
                    val event = UIEvent.createKeyEvent(UIEvent.EventType.KeyReleased, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyReleased(event);
                }
            });

            dialog.getDialogPane().setOnKeyTyped(pKE -> {
                if (mViewable instanceof IUIEventListener) {
                    val listener = (IUIEventListener) mViewable;
                    val event = UIEvent.createKeyEvent(UIEvent.EventType.KeyTyped, pKE.getCharacter().toUpperCase(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                    listener.keyTyped(event);
                }
            });

            dialogResult.set(dialog.showAndWait());
            setDialog(null);

            if (!isFxApplicationThread) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };

        if (isFxApplicationThread) {
            runDialog.run(); // yes run on FX Application Thread
        } else {
            Platform.runLater(runDialog);
            synchronized (lock) {
                convertToRuntimeException(lock::wait);
            }
        }
        dialogResult.get().or(() -> mDialogOptions.getCompleteFunction()).ifPresent(IAction::performAction);
    }

    @Override
    public void setEnabled(boolean pEnabled) {
        if (getDialog() != null) {
            getDialog().getDialogPane().getContent().setDisable(!pEnabled);
            getDialog().getDialogPane().getButtonTypes()
                    .forEach(bt -> getDialog().getDialogPane().lookupButton(bt).setDisable(!pEnabled));
        }
    }

}
