/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.util.Version;

public class FileFilter extends javax.swing.filechooser.FileFilter implements FilenameFilter {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static Logger mLogger = Logger.getLogger(FileFilter.class.getName());

	private Vector<String> mExtensions = new Vector<String>();
	private boolean mAcceptDir;

	public FileFilter() {
		this(true);
	}

	public FileFilter(boolean pAcceptDir) {
		mAcceptDir = pAcceptDir;
	}

	@Override
	public boolean accept(File pFile) {
		if (pFile.isDirectory()) { return mAcceptDir; }

		String ext = getExtension(pFile);
		return mExtensions.contains(ext);
	}

	@Override
	public String getDescription() {
		return mExtensions.toString();
	}

	public void addExtension(String pExt) {
		mExtensions.add(pExt.toLowerCase());
	}

	public void addExtensions(String[] pExtensitons) {
		for (String ext : pExtensitons) {
			addExtension(ext);
		}
	}

	public static String getExtension(File pFile) {
		String ext = null;
		String s = pFile.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	@Override
	public boolean accept(File dir, String name) {
		return accept(new File(dir, name));
	}

}
