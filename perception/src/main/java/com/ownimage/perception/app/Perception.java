/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.app;

import com.ownimage.framework.app.AppControlBase;
import com.ownimage.framework.app.menu.MenuAction;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.ScrollLayout;
import com.ownimage.framework.control.type.FileType.FileControlType;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.logging.FrameworkException;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.math.RectangleSize;
import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.javafx.DialogView;
import com.ownimage.perception.render.RenderService;
import com.ownimage.perception.transformSequence.TransformSequence;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.NonNull;
import lombok.val;

import java.io.*;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;
import static java.lang.String.format;

public class Perception extends AppControlBase {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private static final int RECENT_FILE_LENGTH = 20;
    private final List<Tuple2<String, List<String>>> TRANSFORM_EXTENSIONS = List.of(
            new Tuple2("Transforms", List.of("*.x", "*.transform")),
            new Tuple2("All", List.of("*.*"))
    );

    private static final String PACKAGE_PREFIX = "com.ownimage";
    private static final String mLoggingPropertiesFilename = new File("logging.properties").getAbsolutePath();

    private final Container mContainer;
    private final FileControl mFileControl;
    private final PictureControl mOutputPreviewControl;
    private final ActionControl mPropertiesSaveDefault = new ActionControl("Save Default", "propertiesSaveDefault", NullContainer, this::propertiesSaveDefault);
    // Logging actions
    private final ActionControl mLoggingSaveDefaultAction = new ActionControl("Save Default", "loggingSaveDefault", NullContainer, this::loggingSaveDefault);
    private final Vector<String> mRecentFiles = new Vector();
    private BorderLayout mBorderLayout;
    private String mFilename;
    // Properties actions
    private final ActionControl mPropertiesSave = new ActionControl("Save", "propertiesSave", NullContainer, this::propertiesSave);
    private final ActionControl mPropertiesSaveAs = new ActionControl("Save As", "propertiesSaveAs", NullContainer, this::propertiesSaveAs);
    private final ActionControl mLoggingSaveAsAction = new ActionControl("Save As", "loggingSaveAs", NullContainer, this::loggingSaveAs);
    private final IControlChangeListener<FileControl> fileOpenHandler = (f, m) -> {
        String filename = f.getString();
        mLogger.info(() -> "filename = " + filename);
        fileOpen(f.getFile());
    };

    Perception() {
        super("Perception");
        Framework.logEntry(mLogger);

        propertiesInit();
        recentFilesInit();

        mContainer = new Container("Container", "container", this::getUndoRedoBuffer);
        mFileControl = new FileControl(
                "File Name",
                "fileName",
                mContainer,
                Paths.get(".").toAbsolutePath().normalize().toString(),
                FileControlType.FILEOPEN,
                null
        );
        mFileControl.addControlChangeListener(fileOpenHandler);
        mLogger.fine(() -> format("mFileControl set to %s", mFileControl.getValue()));

        PictureType preview = new PictureType(getPreviewSize(), getPreviewSize());
        mOutputPreviewControl = new PictureControl("Preview", "preview", mContainer, preview);

        Framework.logExit(mLogger);
    }

    private void recentFilesInit() {
        try (InputStream is = new FileInputStream("recent.properties")) {
            java.util.Properties recentProperties = new java.util.Properties();
            recentProperties.load(is);
            AtomicInteger i = new AtomicInteger();
            String filename = (String) recentProperties.get(Integer.toString(i.getAndIncrement()));
            while (filename != null) {
                mRecentFiles.add(filename);
                filename = (String) recentProperties.get(Integer.toString(i.getAndIncrement()));
            }
        } catch (IOException pE) {
            mLogger.severe("Unable to load recent.properties: " + pE.getMessage());
        }
    }

    private Optional<TransformSequence> getOptionalTransformSequence() {
        return Services.getServices().getOptionalTransformSequence();
    }

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    private TransformSequence getTransformSequence() {
        return Services.getServices().getOptionalTransformSequence().orElseThrow();
    }

    @Override
    protected IView createContentView() {
        mBorderLayout = new BorderLayout();
        getOptionalTransformSequence().ifPresent(ts -> updateView());
        return mBorderLayout.createView();
    }

    private MenuControl createFileMenu() {
        val fileMenuBulider = new MenuControl.Builder().setDisplayName("File")
                .addAction(new MenuAction("Open", this::fileOpen))
                .addAction(new MenuAction("Save", this::fileSave))
                .addAction(new MenuAction("Save As", this::fileSaveAs))
                .addAction(new MenuAction("Exit", this::fileExit));

        if (!mRecentFiles.isEmpty()) {
            val recent = new MenuControl.Builder().setDisplayName("Recent");
            mRecentFiles.forEach(s -> recent.addAction(new MenuAction(s, () -> fileOpen(s))));
            fileMenuBulider.addMenu(recent.build());
        }
        fileMenuBulider.addAction(new MenuAction("Redraw", this::fileRedraw));
        return fileMenuBulider.build();
    }

    @Override
    protected MenuControl createMenuView() {
        Framework.logEntry(mLogger);

        MenuControl menu = new MenuControl.Builder()
                .addMenu(createFileMenu())
                .addMenu(new MenuControl.Builder().setDisplayName("Transform")
                        .addAction(new MenuAction("Open", this::transformOpen))
                        .addAction(new MenuAction("Save", this::transformSave))
                        .addAction(new MenuAction("Save As", this::transformSaveAs))
                        .build())

                .addMenu(new MenuControl.Builder().setDisplayName("Properties")
                        .addAction(new MenuAction("Edit", this::propertiesEdit))
                        .addAction(new MenuAction("Open", this::propertiesOpen))
                        .addAction(mPropertiesSave)
                        .addAction(mPropertiesSaveAs)
                        .addAction(mPropertiesSaveDefault)
                        .addAction(new MenuAction("Load Default", this::propertiesOpenSystemDefault))
                        .addAction(new MenuAction("Reset to System Default", this::propertiesResetToSystemDefault))
                        .build())

                .addMenu(new MenuControl.Builder().setDisplayName("Logging")
                        .addAction(new MenuAction("Edit", this::loggingEdit))
                        .addAction(new MenuAction("Open Default", this::loggingOpenDefault))
                        .addAction(new MenuAction("Open", this::loggingOpenDefault))
                        .addAction(mLoggingSaveDefaultAction)
                        .addAction(mLoggingSaveAsAction)
                        .addAction(new MenuAction("Test", this::loggingTest))
                        .build())
                .build();

        Framework.logExit(mLogger);
        return menu;
    }

    private void fileExit() {
        Framework.logEntry(mLogger);

        mAppControlView.exit();

        Framework.logExit(mLogger);
    }

    private void fileOpen() {
        Framework.logEntry(mLogger);
        mFileControl.showDialog();
        Framework.logExit(mLogger);
    }

    public void fileOpen(int pIndex) {
        if (pIndex < 0 || mRecentFiles == null || pIndex >= mRecentFiles.size()) {
            mLogger.info(format("open(%s) but there are only %s recent files.", pIndex, mRecentFiles.size()));
            return;
        }
        fileOpen(mRecentFiles.get(pIndex));
    }

    private void recentFilesAdd(String pFilename) {
        mRecentFiles.remove(pFilename);
        mRecentFiles.add(0, pFilename);
        while (mRecentFiles.size() > RECENT_FILE_LENGTH) {
            mRecentFiles.remove(RECENT_FILE_LENGTH);
        }
        Try.of(this::writeRecentFiles).onFailure(f -> mLogger.severe("Unable to write recent.properties file"));
        menuRegenerate();
    }

    private Void writeRecentFiles() throws IOException {
        java.util.Properties recent = new java.util.Properties();
        AtomicInteger i = new AtomicInteger();
        mRecentFiles.forEach(f -> recent.put(Integer.toString(i.getAndIncrement()), f));
        Writer writer = new FileWriter("recent.properties");
        recent.store(writer, "Recent Files");
        writer.close();
        return null;
    }

    public void fileOpen(@NonNull String pFilename) {
        File file = new File(pFilename);
        fileOpen(file);
    }

    /**
     * File open will open the specified File in the application. It will set the Properties to the System Default, or if the
     * default.properties file is available from that. Then if in the resulting Properties if Auto Load Transform is set then the
     * transform for the specified file is opened.
     *
     * @param pFile the file
     * @throws IllegalArgumentException if pFile is null
     */
    public void fileOpen(@NonNull File pFile) throws IllegalArgumentException {
        Framework.logEntry(mLogger);

        mFilename = pFile.getAbsolutePath();
        recentFilesAdd(mFilename);

        propertiesOpenSystemDefault();
        if (getProperties().useDefaultPropertyFile()) {
            propertiesOpenDefault();
        }

        Services.getServices().setTransformSequence(new TransformSequence(this, pFile));
        if (getProperties().useAutoLoadTransformFile()) {
            transformOpenDefault();
        }

        updateView();

        if (!getProperties().useAutoLoadTransformFile()) {
            // if the transform file has been loaded then the output will have already been updated
            refreshOutputPreview();
        }

        Framework.logExit(mLogger);
    }

    private void updateView() {
        getOptionalTransformSequence().ifPresent(ts -> mBorderLayout.setLeft(
                new HFlowLayout(
                        ts.updateView(),
                        new ScrollLayout(mOutputPreviewControl)
                )
        ));
    }

    private void fileRedraw() {
        refreshPreviews();
        mAppControlView.redraw();
    }

    /**
     * This will cause the application to recreate all of the images and redraw itseslf
     */
    private void refreshPreviews() {
        refreshOutputPreview();
        getTransformSequence().resizeInputPreviews(getPreviewSize());
    }

    /**
     * Saves the result of all the transforms to the default output file. A dialog showing a preview of the output image is shown
     * first. If the user presses ok on that then if the file exists then a subsequent dialog is shown to get the user to confirm
     * the file overwrite.
     */
    private void fileSave() {
        Framework.logEntry(mLogger);

        String filename = getSaveFilename();
        File file = new File(filename);
        fileSaveShowPreview(() -> fileSave(file));

        Framework.logExit(mLogger);
    }

    /**
     * Saves the result of all the transforms to the file specified. If the file exists then a dialog is shown to get the user to
     * confirm the file overwrite.
     *
     * @param pFile the file
     */
    private void fileSave(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        fileExistsCheck(pFile, "File Save", () -> fileSaveUnchecked(pFile), () -> mLogger.log(Level.FINE, "Cancel pressed"));

        Framework.logExit(mLogger);

    }

    /**
     * Saves the result of all the transforms to a file chosen by the user. A dialog showing a preview of the output image is shown
     * first. If the user presses ok on that the file chooser dialog is shown. Once a file is selected if the file exists then a
     * subsequent dialog is shown to get the user to confirm the file overwrite.
     */
    private void fileSaveAs() {
        Framework.logEntry(mLogger);

        fileSaveShowPreview(() -> {
            FileControl outputFile = FileControl.createFileSave("Image Save As", NullContainer, getSaveFilename());
            outputFile.addControlChangeListener((c, m) -> new Thread(() -> fileSaveUnchecked(outputFile.getFile())).start());
            // fileSaveUnchecked is ok here as the file chooser save will have the confirm overwrite dialog.);
            outputFile.showDialog();
        });

        Framework.logExit(mLogger);
    }

    /**
     * Saves the result of all the transforms to the file. If the file exists it is overwritten.
     *
     * @param pFile the file
     */
    private void fileSaveUnchecked(File pFile) {
        mLogger.info(() -> "fileSaveUnchecked");
        getOptionalTransformSequence().ifPresent(ts -> {
            val lastTransform = ts.getLastTransform();

            val ok = new ActionControl("OK", "ok", NullContainer, () -> {
            }).setEnabled(false);
            val progress = ProgressControl.builder("Progress", "progress", NullContainer)
                    .withCompleteAction(() -> ok.setEnabled(true))
                    .build();
            showDialog(progress, DialogOptions.NONE, ok);

            Framework.logEntry(mLogger);
            new Thread(() -> {
                try {
                    PictureType testSave = new PictureType(100, 100);
                    testSave.getValue().save(pFile, getProperties().getImageQuality()); // no point generating a large file if we cant save it

                    PictureType output = new PictureType(lastTransform.getWidth(), lastTransform.getHeight());
                    getRenderService()
                            .getRenderJobBuilder("Perception::fileSaveUnchecked", output, lastTransform)
                            .withControlObject(output)
                            .withCompleteAction(() -> {
                                try {
                                    output.getValue().save(pFile, getProperties().getImageQuality());
                                    mLogger.severe("Done");
                                } catch (Exception e) {
                                    mLogger.severe("Unable to output file");
                                }
                            })
                            .withProgressObserver(progress)
                            .withOverSample(lastTransform.getOversample())
                            .build()
                            .run();
                } catch (Exception pT) {
                    Framework.logThrowable(mLogger, Level.SEVERE, pT);
                    mLogger.info(() -> "Unable to save file");

                }
            }).start();
        });
        Framework.logExit(mLogger);
    }

    private int getSavePreviewSize() {
        return getProperties().getSavePreviewSize();
    }

    /**
     * Shows a dialog with a preview of the output of all of the transforms. If the user presses OK then the specified action is
     * run. If they press Cancel then the dialog closes without action.
     *
     * @param pAction the action
     */
    private void fileSaveShowPreview(IAction pAction) {
        Framework.logEntry(mLogger);

        Container displayContainer = new Container("File Save", "fileSave", this::getUndoRedoBuffer);
        StrongReference<PictureType> preview = new StrongReference<>();
        getResizedPictureTypeIfNeeded(getSavePreviewSize(), null).ifPresent(preview::set);
        ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> mLogger.fine("Cancel"));
        ActionControl ok = ActionControl.create("OK", NullContainer, pAction);
        getRenderService().
                getRenderJobBuilder("Perception::fileSaveShowPreview", preview.get(), getTransformSequence().getLastTransform())
                .withControlObject(preview.get())
                .withCompleteAction(() -> {
                    PictureControl previewControl = new PictureControl("Preview", "preview", displayContainer, preview.get());
                    showDialog(displayContainer, DialogOptions.NONE, ok, cancel);
                })
                .build()
                .run();

        Framework.logExit(mLogger);
    }

    public String getFilename() {
        return mFilename;
    }

    private String getFilenameStem() {
        Framework.logEntry(mLogger);

        if (mFilename == null) {
            return "UNKNOWN";
        }

        int i = mFilename.lastIndexOf('.');
        if (i <= 0) {
            return "UNKNOWN";
        }

        String stem = mFilename.substring(0, i);

        Framework.logExit(mLogger, stem);
        return stem;
    }

    private String getLoggingDefaultFilename() {
        return mLoggingPropertiesFilename;
    }

    private String getLoggingFilename() {
        Framework.logEntry(mLogger);

        String logingFilename;
        if (mFilename != null) {
            logingFilename = new File(mFilename).getParent() + File.separator + "logging.properties";
        } else {
            logingFilename = getLoggingDefaultFilename();
        }
        Framework.logExit(mLogger, logingFilename);
        return logingFilename;
    }

    private int getPreviewSize() {
        return getProperties().getPreviewSize();
    }

    public synchronized Properties getProperties() {
        return Services.getServices().getProperties();
    }

    private String getPropertyFilename() {
        Framework.logEntry(mLogger);

        String filename = getFilenameStem() + ".properties";

        Framework.logExit(mLogger, filename);
        return filename;
    }

    private String getPropertySystemDefaultFilename() {
        return "default.properties";
    }

    public RenderService getRenderService() {
        return Services.getServices().getRenderService();
    }

    private String getSaveFilename() {
        Framework.logEntry(mLogger);
        String filename = getFilenameStem() + "-transform.jpg";
        Framework.logExit(mLogger, filename);
        return filename;
    }

    private String getFullTransformFilename() {
        Framework.logEntry(mLogger);
        String filename = getFilenameStem() + ".transform";
        Framework.logExit(mLogger, filename);
        return filename;
    }

    private String getTransformFilename() {
        return new File(getFullTransformFilename()).getName();
    }

    private void propertiesInit() {
        Framework.logEntry(mLogger);

        try {
            propertiesOpenSystemDefault();
            if (!getProperties().useDefaultPropertyFile()) {
                propertiesSetSystemDefault();
            }

        } catch (Exception pT) {
            propertiesSetSystemDefault();
            // throw new FrameworkException(this, Level.SEVERE, "Cannot run initProperties", pT);
        }

        Framework.logExit(mLogger);
    }

    private UndoRedoBuffer getUndoRedoBuffer() {
        return Services.getServices().getUndoRedoBuffer();
    }

    private void loggingEdit() {
        Framework.logEntry(mLogger);

        FrameworkLogger.getInstance().showEditDialog(mAppControlView, PACKAGE_PREFIX, mLoggingSaveAsAction, mLoggingSaveDefaultAction);

        Framework.logExit(mLogger);
    }

    private void loggingOpen() {
        Framework.logEntry(mLogger);
        FileControl fileControl = FileControl.createFileOpen("Open Logging settings", NullContainer, mLoggingPropertiesFilename);
        fileControl.addControlChangeListener((c, m) -> FrameworkLogger.getInstance().read(fileControl.getValue()));
        FrameworkLogger.getInstance().read(fileControl.getValue());
        Framework.logExit(mLogger);
    }

    private void loggingOpenDefault() {
        Framework.logEntry(mLogger);
        FrameworkLogger.getInstance().read(getLoggingDefaultFilename());
        Framework.logExit(mLogger);
    }

    private void loggingSave(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        fileExistsCheck(pFile, "Log Properties File", () -> loggingSaveUnchecked(pFile), () -> mLogger.log(Level.FINE, "Cancel pressed"));

        Framework.logExit(mLogger);
    }

    private void loggingSaveAs() {
        Framework.logEntry(mLogger);

        FileControl fileControl = FileControl.createFileSave("Logging Save As", NullContainer, getLoggingFilename());
        fileControl.showDialog();
        File file = fileControl.getFile();
        loggingSaveUnchecked(file);

        Framework.logExit(mLogger);
    }

    private void loggingSaveDefault() {
        Framework.logEntry(mLogger);

        File file = new File(getLoggingDefaultFilename());
        loggingSave(file);

        Framework.logExit(mLogger);
    }

    private void loggingSaveUnchecked(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        FrameworkLogger.getInstance().write(pFile, "Perception Logger");

        Framework.logExit(mLogger);
    }

    private void loggingTest() {
        Framework.logEntry(mLogger);

        mLogger.log(Level.SEVERE, "Severe");
        mLogger.log(Level.WARNING, "Warning");
        mLogger.log(Level.INFO, "Info");
        mLogger.log(Level.FINE, "Fine");
        mLogger.log(Level.FINER, "Finer");
        mLogger.log(Level.FINEST, "Finest");

        Framework.logExit(mLogger);
    }

    private void propertiesEdit() {
        Framework.logEntry(mLogger);
        int previousPreviewSize = getPreviewSize();

        UndoRedoBuffer undoRedoBuffer = getProperties().getUndoRedoBuffer();
        Id id = undoRedoBuffer.startSavepoint("Edit");

        IAction success = () -> {
            undoRedoBuffer.endSavepoint(id);
            if (previousPreviewSize != getPreviewSize()) {
                this.refreshPreviews();
            }
        };
        ActionControl ok = ActionControl.create("OK", NullContainer, success);
        ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> {
            undoRedoBuffer.endSavepoint(id);
            undoRedoBuffer.undo();
        });

        new DialogView(getProperties()
                , DialogOptions.builder().withCompleteFunction(() -> mLogger.info("Dialog Closed")).build()
                , mPropertiesSave.doBefore(success)
                , mPropertiesSaveAs.doBefore(success)
                , mPropertiesSaveDefault.doBefore(success)
                , ok
        ).showModal();

        Framework.logExit(mLogger);
    }


    private void propertiesOpen() {
        Framework.logEntry(mLogger);

        FileControl propertyFile = FileControl.createFileOpen("Properties Open", NullContainer, getPropertyFilename());
        propertyFile.addControlChangeListener((c, m) -> propertiesOpen(propertyFile.getFile()));
        propertyFile.showDialog();

        Framework.logExit(mLogger);
    }

    private void propertiesOpen(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        try (FileInputStream fos = new FileInputStream(pFile)) {
            PersistDB db = new PersistDB();
            db.load(fos);
            getProperties().read(db, "");
        } catch (Exception pT) {
            mLogger.log(Level.SEVERE, "Error", pT);
        }

        Framework.logExit(mLogger);
    }

    private synchronized void propertiesOpenDefault() {
        Framework.logEntry(mLogger);

        File file = new File(getPropertyFilename());
        propertiesOpen(file);

        Framework.logExit(mLogger);
    }

    private synchronized void propertiesOpenSystemDefault() {
        Framework.logEntry(mLogger);

        File file = new File(getPropertySystemDefaultFilename());
        propertiesOpen(file);

        Framework.logExit(mLogger);
    }

    private void propertiesResetToSystemDefault() {
        Framework.logEntry(mLogger);

        try {
            PersistDB db = new PersistDB();
            Properties newProps = new Properties();
            newProps.write(db, "properties");
            getProperties().read(db, "properties");
        } catch (IOException pIOE) {
            // TODO defautl
        }
        Framework.logExit(mLogger);
    }

    private void propertiesSave() {
        Framework.logEntry(mLogger);

        File file = new File(getPropertyFilename());
        propertiesSave(file);

        Framework.logExit(mLogger);
    }

    private void propertiesSave(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        fileExistsCheck(pFile, "Default Properties", () -> propertiesSaveUnchecked(pFile), () -> mLogger.log(Level.FINE, "Cancel pressed"));

        Framework.logExit(mLogger);
    }

    private void propertiesSaveAs() {
        Framework.logEntry(mLogger);

        FileControl propertyFile = FileControl.createFileSave("Properties Save As", NullContainer, getPropertyFilename());
        propertyFile.addControlChangeListener((c, m) -> propertiesSaveUnchecked(propertyFile.getFile()));
        propertyFile.showDialog();

        Framework.logExit(mLogger);
    }

    private void propertiesSaveDefault() {
        Framework.logEntry(mLogger);

        File file = new File(getPropertySystemDefaultFilename());
        propertiesSave(file);

        Framework.logExit(mLogger);
    }

    private void propertiesSaveUnchecked(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        getProperties().write(pFile, "Perception Properties");

        Framework.logExit(mLogger);
    }

    private synchronized void propertiesSetSystemDefault() {
        Framework.logEntry(mLogger);

        Services.getServices().getProperties().reset();

        Framework.logExit(mLogger);
    }

    public void refreshOutputPreview() {
        Framework.logEntry(mLogger);

        resizePreviewControlIfNeeded();
        getRenderService()
                .getRenderJobBuilder("Perception::refreshOutputPreview", mOutputPreviewControl, getTransformSequence().getLastTransform())
                .build()
                .run();

        Framework.logExit(mLogger);
    }

    /**
     * Creates a blank picture of the correct ratio based on the getHeight()/getWidth of the last transform, with a max height and width from getPreviewSize().
     */
    private void resizePreviewControlIfNeeded() {
        int size = getPreviewSize();
        if (getTransformSequence() == null || getTransformSequence().getLastTransform() == null) {
            mOutputPreviewControl.setValue(new PictureType(size));
        } else {
            getResizedPictureTypeIfNeeded(size, mOutputPreviewControl.getValue()).ifPresent(mOutputPreviewControl::setValue);
        }
    }

    /**
     * Gets a PictureType of max square size pNewSize but in the aspect ratio needed for the TransformSequence IF NEEDED.
     * If the pCurrent is supplied and is the same as the size that would have been returned then
     * this returns Optional.empty().
     *
     * @param pNewSize the size of the square that the new PictureType needs to be constrained by
     * @param pCurrent if supplied specifies the size of the current PictureType
     * @return Optional representing the correctly sized PictureType or Optional.empty if the picture size was OK.
     */
    private Optional<PictureType> getResizedPictureTypeIfNeeded(int pNewSize, PictureType pCurrent) {
        RectangleSize requiredRatio = getTransformSequence().getLastTransform().getSize();
        RectangleSize requiredSize = requiredRatio.scaleToSquare(pNewSize);
        if (pCurrent == null || !requiredSize.equals(pCurrent.getSize())) {
            return Optional.of(new PictureType(requiredSize));
        }
        return Optional.empty();
    }

    @Override
    protected void setSize() {
        Framework.logEntry(mLogger);

        setX(200);
        setY(200);
        setWidth(600);
        setHeight(600);

        Framework.logExit(mLogger);
    }

    private void transformOpen() {
        Framework.logEntry(mLogger);

        FileControl transformFile = FileControl.createFileOpen(
                "Transform Open",
                NullContainer,
                getFullTransformFilename(),
                TRANSFORM_EXTENSIONS.prepend(new Tuple2("default", List.of(getTransformFilename())))
        );
        transformFile.addControlChangeListener((c, m) -> transformOpen(transformFile.getFile()));
        transformFile.showDialog();

        Framework.logExit(mLogger);
    }

    private void transformOpen(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        try {
            getTransformSequence().read(pFile);
        } catch (Exception pT) {
            mLogger.log(Level.SEVERE, "Error", pT);
        }

        Framework.logExit(mLogger);
    }

    private synchronized void transformOpenDefault() {
        Framework.logEntry(mLogger);

        File file = new File(getFullTransformFilename());
        transformOpen(file);

        Framework.logExit(mLogger);
    }

    private void transformSave() {
        Framework.logEntry(mLogger);

        File file = new File(getFullTransformFilename());
        transformSave(file);

        Framework.logExit(mLogger);
    }

    private void transformSave(@NonNull File pFile) {
        Framework.logEntry(mLogger);

        fileExistsCheck(pFile, "Transform File", () -> transformSaveUnchecked(pFile), () -> mLogger.log(Level.FINE, "Cancel pressed"));

        Framework.logExit(mLogger);
    }

    private void transformSaveAs() {
        Framework.logEntry(mLogger);

        FileControl transformFile = FileControl.createFileSave("Transform Save As", NullContainer, getFullTransformFilename());
        transformFile.addControlChangeListener((c, m) -> transformSaveUnchecked(((FileControl) c).getFile()));
        transformFile.showDialog();

        Framework.logExit(mLogger);
    }

    private void transformSaveUnchecked(@NonNull File pFile) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pFile.getAbsolutePath()", pFile.getAbsolutePath());

        try (FileOutputStream fos = new FileOutputStream(pFile)) {
            PersistDB db = new PersistDB();
            getTransformSequence().write(db, "transform");
            db.store(fos, "Perception Transform");

        } catch (Exception pT) {
            throw new FrameworkException(this, Level.SEVERE, "Error", pT);
        }

        Framework.logExit(mLogger);
    }

}
