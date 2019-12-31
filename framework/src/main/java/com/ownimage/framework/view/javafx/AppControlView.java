/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.app.IAppControl;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IAppControlView;
import com.ownimage.framework.view.IView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import lombok.NonNull;

import java.io.File;
import java.util.logging.Logger;

public class AppControlView extends Application implements IAppControlView {


    public final static Logger mLogger = Framework.getLogger();

    private static AppControlView mAppControlView;
    private static IAppControl mAppControl;

    private Stage mPrimaryStage;

    private Scene mScene;
    private Image mApplicationIcon;

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
        return mAppControlView;
    }

    public static IAppControl getAppControl() {
        return mAppControl;
    }

    public static void setAppControl(final IAppControl pAppControl) {
        mAppControl = pAppControl;
    }

    public static AppControlView getAppControlView() {
        return mAppControlView;
    }

    static AppControlView getInstance() {
        return mAppControlView;
    }

    public static void launch(final String... pArgs) {
        final Runnable r = () -> Application.launch(AppControlView.class);
        new Thread(r).start();

        try {
            while (mAppControlView == null) {
                Thread.sleep(100);
            }
        } catch (final InterruptedException e) {

        }
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
            final MenuBar menuBar = ((MenuBarView) mAppControl.getMenu().createView()).getUI();
            final IView content = mAppControl.getContent();
            final BorderPane border = new BorderPane();
            border.setTop(menuBar);
            border.setCenter(((FXView) (content)).getUI());

            final ScrollPane scroll = new ScrollPane(border);
            scroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);

            mScene = new Scene(border, mAppControl.getWidth(), mAppControl.getHeight());
            mScene.widthProperty().addListener((obs, oldVal, newVal) -> mAppControl.setWidth(newVal.intValue()));
            mScene.heightProperty().addListener((obs, oldVal, newVal) -> mAppControl.setHeight(newVal.intValue()));
            menuBar.prefWidthProperty().bind(mScene.widthProperty());
            menuBar.setUseSystemMenuBar(true);

            mPrimaryStage.titleProperty().setValue(mAppControl.getTitle());

            mPrimaryStage.setScene(mScene);
            mPrimaryStage.show();
        });
    }

    public Stage getPrimaryStage() {
        return mPrimaryStage;
    }

    public Scene getScene() {
        return mScene;
    }

    public Image getApplicationIcon() {
        return mApplicationIcon;
    }

    public void setApplicationIcon(final Image pApplicationIcon) {
        mApplicationIcon = pApplicationIcon;
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
    public void showDialog(final IViewable pViewable, final DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        Platform.runLater(() -> showDialogLater(pViewable, pDialogOptions, pUndoRedo, pButtons));
    }

    private void showDialogLater(
            @NonNull final IViewable pViewable,
            @NonNull final DialogOptions pDialogOptions,
            final UndoRedoBuffer pUndoRedo,
            final ActionControl... pButtons
    ) {
        new DialogView(pViewable, pDialogOptions, pUndoRedo, pButtons).showModal();
    }

    public void showDirectoryChooserDialog(final FileControl pFileControl) {
        final DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Open Resource File");

        final File selectedDir = dirChooser.showDialog(mPrimaryStage);
        if (selectedDir != null) {
            pFileControl.setValue(selectedDir.getAbsolutePath());
        }
    }

    public void showFileOpenChooserDialog(final FileControl pFileControl) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(pFileControl.getDisplayName());
        pFileControl.getExtensions().ifPresent(es -> es.forEach(e ->
                fileChooser.getExtensionFilters().add(new ExtensionFilter(e._1, e._2.toJavaList()))
        ));

        if (pFileControl.getFile().isDirectory()) {
            fileChooser.setInitialDirectory(pFileControl.getFile());
        }
        if (pFileControl.getFile().isFile()) {
            fileChooser.setInitialDirectory(pFileControl.getFile().getParentFile());
            fileChooser.setInitialFileName(pFileControl.getFile().getName());
        }
        final File selectedFile = fileChooser.showOpenDialog(mPrimaryStage);
        if (selectedFile != null) {
            pFileControl.setValue(selectedFile.getAbsolutePath());
        }
    }

    public void showFileSaveChooserDialog(@NonNull final FileControl pFileControl) {
        final String originalAbsolutePath = pFileControl.getFile().getAbsolutePath();
        final FileChooser fileChooser = new FileChooser();
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
        final File selectedFile = fileChooser.showSaveDialog(mPrimaryStage);

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
