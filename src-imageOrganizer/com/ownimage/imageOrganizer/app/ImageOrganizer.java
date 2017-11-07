package com.ownimage.imageOrganizer.app;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.app.AppControlBase;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.FileControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.StringControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.type.FileType;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.util.FileFilter;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;

public class ImageOrganizer extends AppControlBase {

	public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
	public final static String mClassname = ImageOrganizer.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private static ImageOrganizer mImageOrganizer;

	private FileControl mMasterDirectory;

	private IntegerControl mSplitInto;

	private ActionControl mSplit;

	private ActionControl mReorgJPG;
	private StringControl mReorgJPGDir;
	private ActionControl mReorgNEF;
	private StringControl mReorgNEFDir;
	private IControlChangeListener mMasterDirectoryChangeListener;

	public ImageOrganizer() {
		super("Image Organizer");
	}

	public static synchronized ImageOrganizer getInstance() {
		if (mImageOrganizer == null) {
			mImageOrganizer = new ImageOrganizer();
		}

		return mImageOrganizer;
	}

	@Override
	protected IView createContentView() {
		Container container = new Container("mainView", "mainView", NullContainer);

		mMasterDirectory = new FileControl("Directory", "directory", container, "", FileType.FileControlType.DIRECTORY);
		mMasterDirectory.addControlChangeListener(mMasterDirectoryChangeListener = (o, b) -> masterDirectoryChangeListener(o, b));
		mSplit = new ActionControl("Split", "split", container, () -> split()).setEnabled(false);
		mSplitInto = new IntegerControl("Split into", "splitInto", container, 3, new IntegerMetaType(1, 10, 1));
		mReorgJPGDir = new StringControl("JPG Dir", "jpgDir", container, "").setEnabled(false);
		mReorgJPG = new ActionControl("Reorg JPG", "reorgJPG", container, () -> reorgJPG()).setEnabled(false);
		mReorgNEFDir = new StringControl("NEF Dir", "nefDir", container, "").setEnabled(false);
		mReorgNEF = new ActionControl("Reorg NEF", "reorgNEF", container, () -> reorgNEF()).setEnabled(false);

		return container.createView();
	}

	@Override
	protected MenuControl createMenuView() {
		Container menuContainer = new Container("MainMenu", "mainMenu", NullContainer);

		MenuControl menu = new MenuControl()
				.addMenu(new MenuControl("File")
						.addAction(new ActionControl("Exit", "exit", menuContainer, () -> fileExit()))
						.lock())
				.lock();

		return menu;
	}

	private void fileExit() {
		System.exit(0);
	}

	private String filename(final String pPadString, final int i, final String pExtension) {
		return pPadString.substring(String.valueOf(i).length()) + String.valueOf(i) + pExtension;
	}

	public void masterDirectoryChangeListener(final Object pSource, final boolean pIsMutating) {
		if (pSource != mMasterDirectory) { throw new IllegalStateException("wrong object specified"); }

		mSplit.setEnabled(true);
		mReorgJPGDir.setValue(mMasterDirectory.getString() + File.separator + "jpg");
		mReorgNEFDir.setValue(mMasterDirectory.getString() + File.separator + "nef");
		mReorgJPG.setEnabled(false);
		mReorgNEF.setEnabled(false);

		String jpgDirName = mMasterDirectory.getString() + File.separator + "jpg";
		System.out.println(jpgDirName);
	}

	private File mkdir(final File pParentDir, final String pDirName) throws Exception {
		File dir = new File(pParentDir, pDirName);
		if (dir.isFile()) {
			throw new Exception("Can not create directory: " + dir.getAbsolutePath() + " is a file.");
		} else if (!dir.isDirectory() && !dir.mkdir()) { throw new Exception("Unable to create directory " + pDirName + " in " + pParentDir.getAbsolutePath()); }
		return dir;
	}

	private void renameFileTypesTo(final File pFromDir, final String pExtension) throws Exception {
		File toDir = new File(pFromDir.getAbsolutePath() + File.separator + pExtension);

		if (toDir.exists()) {
			if (mLogger.isLoggable(Level.INFO)) {
				mLogger.info(toDir.getAbsolutePath() + " already exists");
			}
		}

		toDir.mkdir();

		FileFilter filter;
		filter = new FileFilter(false);
		filter.addExtension(pExtension);

		File[] list;
		list = pFromDir.listFiles(filter);
		for (File f : list) {
			renameTo(toDir, f);
		}
	}

	private void renameTo(final File pToDir, final File pFile) throws Exception {
		if (pFile.isFile() && !pFile.renameTo(new File(pToDir, pFile.getName()))) { throw new Exception("Unable to move file: " + pFile.getAbsolutePath()); }

	}

	private void renameTo(final File pFromDir, final File pToDir, final String pFromName, final String pToName) throws Exception {
		File f = new File(pFromDir, pFromName);
		if (f.isFile() && !f.renameTo(new File(pToDir, pToName))) { throw new Exception("Unable to move file: " + f.getAbsolutePath() + " to " + pToDir.getAbsolutePath() + File.separator + pToName); }
	}

	private void reorgJpg(final File pDirectory) throws Exception {
		File[] dirs;
		dirs = new File[3];
		dirs[0] = mkdir(pDirectory, "0");
		dirs[1] = mkdir(pDirectory, "1");
		dirs[2] = mkdir(pDirectory, "2");

		FileFilter filter;
		filter = new FileFilter(false);
		filter.addExtension("jpg");

		String[] filenames;
		filenames = pDirectory.list(filter);

		Arrays.sort(filenames);

		int i = 0;
		for (String s : filenames) {
			renameTo(pDirectory, dirs[i % 3], s, filename("000", i / 3, ".jpg"));
			i++;
		}
	}

	private void reorgJpg(final String pDirectoryName) throws Exception {
		File directory = new File(pDirectoryName);
		reorgNef(directory);
	}

	private void reorgJPG() {
		System.out.println("reorgJPG");
		try {
			reorgJpg(mMasterDirectory.getFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reorgNef(final File pDirectory) throws Exception {
		File[] dirs;
		dirs = new File[3];
		dirs[0] = mkdir(pDirectory, "0");
		dirs[1] = mkdir(pDirectory, "1");
		dirs[2] = mkdir(pDirectory, "2");

		FileFilter filter;
		filter = new FileFilter(false);
		filter.addExtension("nef");

		String[] filenames;
		filenames = pDirectory.list(filter);

		Arrays.sort(filenames);

		int i = 0;
		for (String s : filenames) {
			renameTo(pDirectory, dirs[i % 3], s, filename("000", i / 3, ".NEF"));
			i++;
		}
	}

	private void reorgNef(final String pDirectoryName) throws Exception {
		File directory = new File(pDirectoryName);
		reorgNef(directory);
	}

	private void reorgNEF() {
		System.out.println("reorgNEF");
	}

	private void split() {
		mReorgJPG.setEnabled(true);
		mReorgNEF.setEnabled(true);
		System.out.println("Split");
	}

	private void splitJpgNef(final File pDirectory) throws Exception {
		renameFileTypesTo(pDirectory, "nef");
		renameFileTypesTo(pDirectory, "jpg");
	}

	/**
	 * Given the name of a directory this will create two sub directories "nef" and "jpg" and move all of the .NEF and .JPG files in
	 * to the appropriate directory.
	 */
	/**
	 * @param pDirectoryName
	 *            the name of the top level directory
	 * @throws Exception
	 */
	private void splitJpgNef(final String pDirectoryName) throws Exception {
		File directory = new File(pDirectoryName);
		splitJpgNef(directory);
	}

}
