/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.io.File;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.FileType;
import com.ownimage.framework.control.type.FileType.FileControlType;
import com.ownimage.framework.control.type.StringMetaType;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class FileControl extends ControlBase<FileControl, FileType, StringMetaType, String, IView> {

	public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
	public final static String mClassname = ControlBase.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	public File mFile;

	public FileControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final String pValue, final FileControlType pFileControlType) {
		super(pDisplayName, pPropertyName, pContainer, new FileType(pValue, pFileControlType));
	}

	public static FileControl createFileOpen(final String pDisplayName, final IContainer pContainer, final String pFilename) {
		return new FileControl(pDisplayName, "fileName", pContainer, pFilename, FileControlType.FILEOPEN);
	}

	public static FileControl createFileSave(final String pDisplayName, final IContainer pContainer, final String pFilename) {
		return new FileControl(pDisplayName, "fileName", pContainer, pFilename, FileControlType.FILESAVE);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public IView createView() { // TODO can we push this into the base class
		IView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	public synchronized File getFile() {
		if (mFile == null) {
			mFile = new File(getValue());
		}
		return mFile;
	}

	public FileControlType getFileControlType() {
		return mValue.getFileControlType();
	}

	@Override
	public boolean setValue(final String pValue) {
		mFile = null;
		return super.setValue(pValue);
	}

	@Override
	public boolean setValue(final String pValue, final IView pView, final boolean pIsMutating) {
		mFile = null;
		return super.setValue(pValue, pView, pIsMutating);
	}

	public void showDialog() {
		ViewFactory.getInstance().showDialog(this);

	}

}
