/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view.javafx;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.app.IAppControl;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IEnabledListener;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.IUndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.UIEvent;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class AppControlView extends Application implements IAppControlView {


    public final static Logger mLogger = Framework.getLogger();

    private static AppControlView mAppControlView;
    private static IAppControl mAppControl;

    private Stage mPrimaryStage;

    private Scene mScene;

    public AppControlView() { // need public constructor for the app to work
        if (mAppControlView != null) {
            throw new IllegalStateException("Can only create one instance of AppControlView.");
        }
        if (mAppControl == null) {
            throw new IllegalStateException("mAppControl must be set before the instance can be created.");
        }

        mAppControlView = this;
        mAppControl.setView(mAppControlView);
    }

    public synchronized static IAppControlView createAppControlView(final IAppControl pAppControl) {
        // if (mAppControlView != null || mAppControl != null) { throw new
        // IllegalStateException("Cannot call createAppControlView can only be called once."); }
        //
        // mAppControl = pAppControl;
        // // Application.launch(AppControlView.class);
        //
        // // note that the following block is needed as Application.launch does not return until the application is closed.
        // // Application.launch(AppControlView.class);
        // launch();
        //
        // // Runnable r = () -> Application.launch(AppControlView.class);
        // // new Thread(r).start();
        // //
        // // try {
        // // while (mAppControlView == null) {
        // // Thread.sleep(100);
        // // }
        // // } catch (InterruptedException e) {
        // //
        // // }
        // //
        return mAppControlView;
    }

    public static IAppControl getAppControl() {
        return mAppControl;
    }

    public static AppControlView getAppControlView() {
        return mAppControlView;
    }

    static AppControlView getInstance() {
        return mAppControlView;
    }

    public static void launch(final String... pArgs) {
        Runnable r = () -> Application.launch(AppControlView.class);
        new Thread(r).start();

        try {
            while (mAppControlView == null) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {

        }
    }

    public static void setAppControl(final IAppControl pAppControl) {
        mAppControl = pAppControl;
    }

    @Override
    public void exit() {
        Platform.exit();
    }

    public FXViewFactory getFactory() {
        return FXViewFactory.getInstance();
    }

    @Override
    public void redraw() {
        Platform.runLater(() -> {
            MenuBar menuBar = ((MenuBarView) mAppControl.getMenu().createView()).getUI();

            IView content = mAppControl.getContent();
            BorderPane border = new BorderPane();
            border.setTop(menuBar);
            border.setCenter(((FXView) (content)).getUI());

            ScrollPane scroll = new ScrollPane(border);
            scroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

            mScene = new Scene(border, mAppControl.getWidth(), mAppControl.getHeight());
            menuBar.prefWidthProperty().bind(mScene.widthProperty());
            //
            mPrimaryStage.setTitle(mAppControl.getTitle());
            // mPrimaryStage.setScene(mScene);
            // mPrimaryStage.sizeToScene();
            // mPrimaryStage.show();
            // MenuBar menuBar = new MenuBar();
            // Menu menu = new Menu("File");
            // MenuItem quit = new MenuItem("Quit");
            // quit.setOnAction(e -> Platform.exit());
            // menu.getItems().add(quit);
            // menuBar.getMenus().add(menu);
            menuBar.setUseSystemMenuBar(true);

            BorderPane root = new BorderPane();
            // root.setTop(menuBar);
            // Scene scene = new Scene(root, 600, 600);
            mPrimaryStage.setScene(mScene);
            mPrimaryStage.show();
        });
    }

    public void setApplicationIcon(final Image pApplicationIcon) {
        mPrimaryStage.getIcons().add(pApplicationIcon);
    }

    public void showDialog(final FileControl pFileControl) {
        Platform.runLater(() -> {
            switch (pFileControl.getFileControlType()) {
                case FILEOPEN:
                    showFileOpenChooserDialog(pFileControl);
                    break;
                case DIRECTORY:
                    showDirectoryChooserDialog(pFileControl);
                    break;
                case FILESAVE:
                    showFileSaveChooserDialog(pFileControl);
                    break;
            }
        });
    }

    @Override
    public void showDialog(final IViewable pViewable, final DialogOptions pDialogOptions, final ActionControl... pButtons) {
        showDialog(pViewable, pDialogOptions, null, pButtons);
    }

    @Override
    public void showDialog(final IViewable pViewable, final DialogOptions pDialogOptions, final IUndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        Platform.runLater(() -> showDialogLater(pViewable, pDialogOptions, pUndoRedo, pButtons));
    }

    private void showDialogLater(final IViewable pViewable, final DialogOptions pDialogOptions, final IUndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        Framework.checkParameterNotNull(mLogger, pViewable, "pViewable");
        Framework.checkParameterNotNull(mLogger, pDialogOptions, "pDialogOptions");

        HashMap<ButtonType, ActionControl> buttonMap = new HashMap<>();

        FXView content = (FXView) (pViewable.createView());
        Node contentUI = content.getUI();

        Dialog<ActionControl> dialog = new Dialog<>();
        dialog.setTitle(pViewable.getDisplayName());
        dialog.getDialogPane().setContent(contentUI);
        dialog.getDialogPane().layout();
        dialog.setResultConverter(buttonMap::get);

        // the width listener is needed in case the mDialog is showing the UI controls that affect the width of the controls
        // themselves which would mean that the mDialog would need to change size as the controls change value.
        ChangeListener<? super Number> widthListener = (observable, oldValue, newValue) -> dialog.setWidth(dialog.getWidth() + newValue.doubleValue() - oldValue.doubleValue());
        FXViewFactory.getInstance().controlWidthProperty.addListener(widthListener);
        FXViewFactory.getInstance().labelWidthProperty.addListener(widthListener);

        List<IEnabledListener> listeners = new Vector<>(); // these are only collected to prevent garbage collection
        for (ActionControl action : pButtons) {
            ButtonType button = new ButtonType(action.getDisplayName(), ButtonData.OK_DONE);
            buttonMap.put(button, action);
            dialog.getDialogPane().getButtonTypes().add(button);
            dialog.getDialogPane().lookupButton(button).setDisable(!action.isEnabled());
            IEnabledListener listener = (c, e) -> dialog.getDialogPane().lookupButton(button).setDisable(!e);
            listeners.add(listener);
            action.addEnabledListener(listener);
        }

        dialog.getDialogPane().setOnKeyPressed(pKE -> {
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

        dialog.getDialogPane().setOnKeyReleased(pKE -> {
            if (pViewable instanceof IUIEventListener) {
                IUIEventListener listener = (IUIEventListener) pViewable;
                UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyReleased, null, pKE.getCode().getName(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                listener.keyReleased(event);
            }
        });

        dialog.getDialogPane().setOnKeyTyped(pKE -> {
            if (pViewable instanceof IUIEventListener) {
                IUIEventListener listener = (IUIEventListener) pViewable;
                UIEvent event = UIEvent.createKeyEvent(UIEvent.EventType.KeyTyped, null, pKE.getCharacter().toUpperCase(), pKE.isControlDown(), pKE.isAltDown(), pKE.isShiftDown());
                listener.keyTyped(event);
            }
        });

        final Optional<ActionControl> dialogResult = dialog.showAndWait();

        new Thread(() -> {
            // this needs to be done here as the complete function might not be specified.
            dialogResult.ifPresent(ActionControl::performAction);

            // the value is passed into the completeFunction only to indicate how the mDialog ended.
            pDialogOptions.getCompleteFunction().ifPresent(IAction::performAction);
        }).start();
    }

    public void showDirectoryChooserDialog(final FileControl pFileControl) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Open Resource File");

        File selectedDir = dirChooser.showDialog(mPrimaryStage);
        if (selectedDir != null) {
            pFileControl.setValue(selectedDir.getAbsolutePath());
        }
    }

    public void showFileOpenChooserDialog(final FileControl pFileControl) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(pFileControl.getDisplayName());
        fileChooser.getExtensionFilters().addAll(
                // new ExtensionFilter("Text Files", "*.txt"),
                // new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                // new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                new ExtensionFilter("All Files", "*.*"));

        if (pFileControl.getFile().isDirectory()) {
            fileChooser.setInitialDirectory(pFileControl.getFile());
        }
        if (pFileControl.getFile().isFile()) {
            fileChooser.setInitialDirectory(pFileControl.getFile().getParentFile());
            fileChooser.setInitialFileName(pFileControl.getFile().getName());
        }
        File selectedFile = fileChooser.showOpenDialog(mPrimaryStage);
        if (selectedFile != null) {
            pFileControl.setValue(selectedFile.getAbsolutePath());
        }
    }

    public void showFileSaveChooserDialog(final FileControl pFileControl) {
        Framework.checkParameterNotNull(mLogger, pFileControl, "pFileControl");

        String originalAbsolutePath = pFileControl.getFile().getAbsolutePath();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(pFileControl.getDisplayName());
        fileChooser.getExtensionFilters().addAll(
                // new ExtensionFilter("Text Files", "*.txt"),
                // new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                // new ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                new ExtensionFilter("All Files", "*.*"));

        if (pFileControl.getFile().isDirectory()) {
            fileChooser.setInitialDirectory(pFileControl.getFile());
        }
        if (pFileControl.getFile().isDirectory()) {
            fileChooser.setInitialDirectory(pFileControl.getFile().getParentFile());
            fileChooser.setInitialFileName(pFileControl.getFile().getName());
        }
        File selectedFile = fileChooser.showSaveDialog(mPrimaryStage);

        if (selectedFile != null) {
            if (!selectedFile.getAbsolutePath().equals(originalAbsolutePath)) {
                mLogger.info(() -> "selected file changed to: " + selectedFile.getAbsolutePath());
                pFileControl.setValue(selectedFile.getAbsolutePath());
            } else {
                mLogger.info(() -> "selected file unchanged: " + selectedFile.getAbsolutePath());
                pFileControl.fireControlChangeEvent();
            }
        }
    }

    @Override
    public void start(final Stage pPrimaryStage) {
        mPrimaryStage = pPrimaryStage;
        redraw();
    }

}
