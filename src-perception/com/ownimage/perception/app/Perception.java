package com.ownimage.perception.app;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.app.AppControlBase;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.control.layout.HSplitLayout;
import com.ownimage.framework.control.layout.ScrollLayout;
import com.ownimage.framework.control.type.FileType.FileControlType;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.logging.FrameworkException;
import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.perception.render.RenderService;
import com.ownimage.perception.transformSequence.TransformSequence;

public class Perception extends AppControlBase {

	public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
	public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	private static final String PACKAGE_PREFIX = "com.ownimage";
	private static final String mLoggingPropertiesFilename = new File("logging.properties").getAbsolutePath();

	private static Perception mPerception;
	private Properties mProperties;
	private final UndoRedoBuffer mUndoRedoBuffer;

	private final RenderService mRenderService;
	private TransformSequence mTransformSequence;
	private final Container mContainer;

	private final FileControl mFileControl;

	private final PictureControl mPreviewControl;

	private BorderLayout mBorderLayout;

	/** The absolute filename. i.e. full path. */
	private String mFilename;

	private Perception() {
		super("Perception");
		Framework.logEntry(mLogger);

		propertiesInit();
		loggingInit();

		mUndoRedoBuffer = new UndoRedoBuffer(100);
		mRenderService = new RenderService(this);

		mContainer = new Container("Container", "container", this);
		mFileControl = new FileControl("File Name", "fileName", mContainer, "", FileControlType.FILEOPEN);

		int size = getPreviewSize();
		PictureType preview = new PictureType(getProperties().getColorOOBProperty(), size, size);

		mPreviewControl = new PictureControl("Preview", "preview", mContainer, preview);

		Framework.logExit(mLogger);
	}

	public static synchronized Perception getPerception() {
		if (mPerception == null) {
			mPerception = new Perception();
		}
		return mPerception;
	}

	@Override
	protected IView createContentView() {
		Framework.logEntry(mLogger);

		mBorderLayout = new BorderLayout();

		if (mTransformSequence != null) {
			ScrollLayout scrollablePreview = new ScrollLayout(mPreviewControl);
			HSplitLayout hSplitLayout = new HSplitLayout(mTransformSequence.getContent(), scrollablePreview);
			mBorderLayout.setCenter(hSplitLayout);
		}

		Framework.logExit(mLogger);
		return mBorderLayout.createView();
	}

	@Override
	protected MenuControl createMenuView() {
		Framework.logEntry(mLogger);

		Container menuContainer = new Container("MainMenu", "mainMenu", this);

		MenuControl menu = new MenuControl()
				.addMenu(new MenuControl("File")
						.addAction(new ActionControl("Open", "fileOpen", menuContainer, () -> fileOpen()))
						.addAction(new ActionControl("Save", "fileSave", menuContainer, () -> fileSave()))
						.addAction(new ActionControl("Save As", "fileSaveAs", menuContainer, () -> fileSaveAs()))
						.addAction(new ActionControl("Exit", "fileExit", menuContainer, () -> fileExit()))
						.addAction(new ActionControl("Redraw", "redraw", menuContainer, () -> fileRedraw()))
						.lock())

				.addMenu(new MenuControl("Transform")
						.addAction(new ActionControl("Open", "transformOpen", menuContainer, () -> transformOpen()))
						.addAction(new ActionControl("Save", "transformSave", menuContainer, () -> transformSave()))
						.addAction(new ActionControl("Save As", "SaveAs", menuContainer, () -> transformSaveAs()))
						.lock())

				.addMenu(new MenuControl("Properties")
						.addAction(new ActionControl("Edit", "propertiesEdit", menuContainer, () -> propertiesEdit()))
						.addAction(new ActionControl("Open", "propertiesOpen", menuContainer, () -> propertiesOpen()))
						.addAction(new ActionControl("Save", "propertiesSave", menuContainer, () -> propertiesSave()))
						.addAction(new ActionControl("Save As", "propertiesSaveAs", menuContainer, () -> propertiesSaveAs()))
						.addAction(new ActionControl("Save Default", "propertiesSaveDefault", menuContainer, () -> propertiesSaveDefault()))
						.addAction(new ActionControl("Load Default", "propertiesLoadDefault", menuContainer, () -> propertiesOpenSystemDefault()))
						.addAction(new ActionControl("Reset to System Default", "propertiesResetToSystemDefault", menuContainer, () -> propertiesResetToSystemDefault()))
						.lock())

				.addMenu(new MenuControl("Logging")
						.addAction(new ActionControl("Edit", "loggingEdit", menuContainer, () -> loggingEdit()))
						.addAction(new ActionControl("Open Default", "loggingOpenDefault", menuContainer, () -> loggingOpenDefault()))
						.addAction(new ActionControl("Open", "loggingOpen", menuContainer, () -> loggingOpenDefault()))
						.addAction(new ActionControl("Save Default", "loggingSaveDefault", menuContainer, () -> loggingSaveDefault()))
						.addAction(new ActionControl("Save As", "loggingSaveAs", menuContainer, () -> loggingSaveAs()))
						.addAction(new ActionControl("Test", "loggingTest", menuContainer, () -> loggingTest()))
						.lock())

				.lock();

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
		String filename = mFileControl.getString();
		System.out.println("filename = " + filename);
		fileOpen(mFileControl.getFile());

		Framework.logExit(mLogger);
	}

	/**
	 * File open will open the specified File in the application. It will set the Properties to the System Default, or if the
	 * default.properties file is available from that. Then if in the resulting Properties if Auto Load Transform is set then the
	 * transform for the specified file is opened.
	 *
	 * @param pFile
	 *            the file
	 * @throws IllegalArgumentException
	 *             if pFile is null
	 */
	public void fileOpen(final File pFile) throws IllegalArgumentException {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

		mFilename = pFile.getAbsolutePath();
		mTransformSequence = new TransformSequence(this, pFile);

		propertiesOpenSystemDefault();
		if (mProperties.useDefaultPropertyFile()) {
			propertiesOpenDefault();
		}

		if (mProperties.useAutoLoadTransformFile()) {
			transformOpenDefault();
		}

		ScrollLayout scrollablePreview = new ScrollLayout(mPreviewControl);
		HSplitLayout hSplitLayout = new HSplitLayout(mTransformSequence.getContent(), scrollablePreview);
		mBorderLayout.setCenter(hSplitLayout);

		int size = 800;
		PictureType preview = new PictureType(getProperties().getColorOOBProperty(), size, size);
		mPreviewControl.setValue(preview);
		mRenderService.transform(mPreviewControl, mTransformSequence.getLastTransform(), null);

		Framework.logExit(mLogger);
	}

	private void fileRedraw() {
		mAppControlView.redraw();
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
		fileShowPreview(() -> fileSave(file));

		Framework.logExit(mLogger);
	}

	/**
	 * Saves the result of all the transforms to the file specified. If the file exists then a dialog is shown to get the user to
	 * confirm the file overwrite.
	 *
	 * @param pFile
	 *            the file
	 */
	private void fileSave(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

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

		FileControl outputFile = FileControl.createFileSave("Image Save As", NullContainer, getSaveFilename());
		outputFile.addControlChangeListener((c, m) -> fileSaveUnchecked(outputFile.getFile()));
		// fileSaveUnchecked is ok here as the file chooser save will have the confirm overwrite dialog.

		fileShowPreview(() -> outputFile.showDialog());

		Framework.logExit(mLogger);
	}

	/**
	 * Saves the result of all the transforms to the file. If the file exists it is overwritten.
	 *
	 * @param pFile
	 *            the file
	 */
	private void fileSaveUnchecked(final File pFile) {
		Framework.logEntry(mLogger);

		PictureType output = new PictureType(mProperties.getColorOOBProperty(), "c:\\temp\\ny2.jpg"); // TODO
		PictureControl outputControl = new PictureControl("Preview", "preview", NullContainer, output);
		mRenderService.transform(outputControl, mTransformSequence.getLastTransform(), //
				() -> {
					try {
						outputControl.getValue().save(pFile);
					} catch (Exception e) {
						mLogger.severe("Unable to output file");
					}
				});
		Framework.logExit(mLogger);
	}

	/**
	 * Shows a dialog with a preview of the output of all of the transforms. If the user presses OK then the specified action is
	 * run. If they press Cancel then the dialog closes without action.
	 *
	 * @param pAction
	 *            the action
	 */
	private void fileShowPreview(final IAction pAction) {
		Framework.logEntry(mLogger);

		Container displayContainer = new Container("File Save", "fileSave", this);
		PictureType preview = new PictureType(mProperties.getColorOOBProperty(), 500, 500);
		PictureControl previewControl = new PictureControl("Preview", "preview", displayContainer, preview);
		ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> mLogger.fine("Cancel"));
		ActionControl ok = ActionControl.create("OK", NullContainer, () -> pAction.performAction());
		mRenderService.transform(previewControl, mTransformSequence.getLastTransform(), //
				() -> {
					showDialog(displayContainer, new DialogOptions(), ok, cancel);
				} //
		);

		Framework.logExit(mLogger);
	}

	public String getFilename() {
		return mFilename;
	}

	public String getFilenameStem() {
		Framework.logEntry(mLogger);

		if (mFilename == null) { return "UNKONWN"; }

		int i = mFilename.lastIndexOf('.');
		if (i <= 0) { return "UNKONWN"; }

		String stem = mFilename.substring(0, i);

		Framework.logExit(mLogger, stem);
		return stem;
	}

	@Override
	public int getHeight() {
		return 900;
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
		return 800;
	}

	@Override
	public synchronized Properties getProperties() {
		if (mProperties == null) {
			mProperties = new Properties();
		}
		return mProperties;
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
		return mRenderService;
	}

	private String getSaveFilename() {
		Framework.logEntry(mLogger);
		String filename = getFilenameStem() + "-transform.jpg";
		Framework.logExit(mLogger, filename);
		return filename;
	}

	private String getTransformFilename() {
		Framework.logEntry(mLogger);

		String filename = getFilenameStem() + ".transform";

		Framework.logExit(mLogger, filename);
		return filename;
	}

	@Override
	public UndoRedoBuffer getUndoRedoBuffer() {
		return mUndoRedoBuffer;
	}

	@Override
	public int getWidth() {
		return 1800;
	}

	private void loggingEdit() {
		Framework.logEntry(mLogger);

		FrameworkLogger.getInstance().showEditDialog(mAppControlView, PACKAGE_PREFIX);

		Framework.logExit(mLogger);
	}

	private void loggingInit() {
		Framework.logEntry(mLogger);

		if (mProperties.useDefaultLoggingFile()) {
			loggingOpenDefault();
		}

		Framework.logExit(mLogger);
	}

	private void loggingOpen() {
		Framework.logEntry(mLogger);

		FileControl fileControl = FileControl.createFileOpen("Open Logging settings", NullContainer, mLoggingPropertiesFilename);
		fileControl.showDialog();
		File file = fileControl.getFile();

		try (FileInputStream fstream = new FileInputStream(file);) {
			PersistDB props = new PersistDB();
			props.load(fstream);
			FrameworkLogger.getInstance().read(props, "");
		} catch (Exception pEx) {
			throw new FrameworkException(this, Level.SEVERE, "Cannot Open Logging settings from: " + file.getAbsolutePath(), pEx);
		}

		Framework.logExit(mLogger);
	}

	private void loggingOpenDefault() {
		Framework.logEntry(mLogger);

		String filename = getLoggingDefaultFilename();
		try (FileInputStream fstream = new FileInputStream(filename);) {
			PersistDB props = new PersistDB();
			props.load(fstream);
			FrameworkLogger.getInstance().read(props, "");
		} catch (Exception pEx) {
			mLogger.log(Level.SEVERE, "Cannot Open Default Logging settings from: " + filename);
			mLogger.log(Level.SEVERE, "Cannot Open Default Logging settings from: " + filename, pEx);
		}

		Framework.logExit(mLogger);
	}

	private void loggingSave(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

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

	private void loggingSaveUnchecked(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

		try (FileOutputStream fstream = new FileOutputStream(pFile);) {
			PersistDB props = new PersistDB();
			FrameworkLogger.getInstance().write(props, "");
			props.store(fstream, "Perception Logging");
		} catch (Exception pEx) {
			throw new FrameworkException(this, Level.SEVERE, "Cannot save Properties to: " + pFile.getAbsolutePath(), pEx);
		}

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

		mProperties.getUndoRedoBuffer().resetAndDestroyAllBuffers();

		PersistDB db = new PersistDB();
		mProperties.write(db, "");

		ActionControl ok = ActionControl.create("OK", NullContainer, () -> mLogger.fine("OK"));
		ActionControl cancel = ActionControl.create("Cancel", NullContainer, () -> mProperties.read(db, ""));

		showDialog(mProperties, new DialogOptions(), mProperties.getUndoRedoBuffer(), cancel, ok);

		Framework.logExit(mLogger);
	}

	private void propertiesInit() {
		Framework.logEntry(mLogger);

		try {
			propertiesOpenSystemDefault();
			if (!mProperties.useDefaultPropertyFile()) {
				propertiesSetSystemDefault();
			}

		} catch (Exception pT) {
			propertiesSetSystemDefault();
			// throw new FrameworkException(this, Level.SEVERE, "Cannot run initProperties", pT);
		}

		Framework.logExit(mLogger);
	}

	private void propertiesOpen() {
		Framework.logEntry(mLogger);

		FileControl propertyFile = FileControl.createFileOpen("Properties Open", NullContainer, getPropertyFilename());
		propertyFile.addControlChangeListener((c, m) -> propertiesOpen(propertyFile.getFile()));
		propertyFile.showDialog();

		Framework.logExit(mLogger);
	}

	private void propertiesOpen(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

		try (FileInputStream fos = new FileInputStream(pFile)) {
			PersistDB db = new PersistDB();
			db.load(fos);
			getProperties().read(db, "");
		} catch (Throwable pT) {
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

		PersistDB db = new PersistDB();
		Properties newProps = new Properties();
		newProps.write(db, "properties");
		mProperties.read(db, "properties");

		Framework.logExit(mLogger);
	}

	private void propertiesSave() {
		Framework.logEntry(mLogger);

		File file = new File(getPropertyFilename());
		propertiesSave(file);

		Framework.logExit(mLogger);
	}

	private void propertiesSave(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

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

	private void propertiesSaveUnchecked(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

		try (FileOutputStream fos = new FileOutputStream(pFile)) {
			PersistDB db = new PersistDB();
			mProperties.write(db, "");
			db.store(fos, "Perception Properties");

		} catch (Throwable pT) {
			throw new FrameworkException(this, Level.SEVERE, "Error", pT);
		}

		Framework.logExit(mLogger);
	}

	private synchronized void propertiesSetSystemDefault() {
		Framework.logEntry(mLogger);

		mProperties = new Properties();

		Framework.logExit(mLogger);
	}

	public void refreshPreview() {
		Framework.logEntry(mLogger);

		mRenderService.transform(mPreviewControl, mTransformSequence.getLastTransform(), null);

		Framework.logExit(mLogger);
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

		FileControl transformFile = FileControl.createFileOpen("Transform Open", NullContainer, getTransformFilename());
		transformFile.addControlChangeListener((c, m) -> transformOpen(transformFile.getFile()));
		transformFile.showDialog();

		Framework.logExit(mLogger);
	}

	private void transformOpen(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

		try (FileInputStream fos = new FileInputStream(pFile)) {
			PersistDB db = new PersistDB();
			db.load(fos);
			mTransformSequence.read(db, "transform");
		} catch (Throwable pT) {
			mLogger.log(Level.SEVERE, "Error", pT);
		}

		Framework.logExit(mLogger);
	}

	private synchronized void transformOpenDefault() {
		Framework.logEntry(mLogger);

		File file = new File(getTransformFilename());
		transformOpen(file);

		Framework.logExit(mLogger);
	}

	private void transformSave() {
		Framework.logEntry(mLogger);

		File file = new File(getTransformFilename());
		transformSave(file);

		Framework.logExit(mLogger);
	}

	private void transformSave(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");

		fileExistsCheck(pFile, "Transform File", () -> transformSaveUnchecked(pFile), () -> mLogger.log(Level.FINE, "Cancel pressed"));

		Framework.logExit(mLogger);
	}

	private void transformSaveAs() {
		Framework.logEntry(mLogger);

		FileControl transformFile = FileControl.createFileSave("Transform Save As", NullContainer, getTransformFilename());
		transformFile.addControlChangeListener((c, m) -> transformSaveUnchecked(((FileControl) c).getFile()));
		transformFile.showDialog();

		Framework.logExit(mLogger);
	}

	private void transformSaveUnchecked(final File pFile) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pFile, "pFile");
		Framework.logParams(mLogger, "pFile.getAbsolutePath()", pFile.getAbsolutePath());

		try (FileOutputStream fos = new FileOutputStream(pFile)) {
			PersistDB db = new PersistDB();
			mTransformSequence.write(db, "transform");
			db.store(fos, "Perception Transform");

		} catch (Throwable pT) {
			throw new FrameworkException(this, Level.SEVERE, "Error", pT);
		}

		Framework.logExit(mLogger);
	}

}
