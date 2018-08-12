/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.io.File;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class FileType extends StringType {

	public enum FileControlType {
		FILEOPEN, FILESAVE, DIRECTORY
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = FileType.class.getName();
	public final static Logger mLogger = Framework.getLogger();

	public final static long serialVersionUID = 1L;;

	private final FileControlType mFileControlType;

	/**
	 * Instantiates a new BooleanType
	 * 
	 * @param pValue
	 *            the value
	 */
	public FileType(final String pFilename, final FileControlType pFileControlType) {
		super(pFilename);
		mFileControlType = pFileControlType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.ControlModelBase#duplicate()
	 */
	@Override
	public FileType clone() {
		return new FileType(mValue, mFileControlType);
	}

	public FileControlType getFileControlType() {
		return mFileControlType;
	}

	@Override
	public void setString(final String pValue) {
		File file = new File(pValue);

		if (mFileControlType == FileControlType.DIRECTORY && !file.isDirectory()) {
			throw new IllegalArgumentException("The value supplied <" + pValue + "> is not a directory.");
		}

		if (mFileControlType != FileControlType.DIRECTORY && file.isDirectory()) {
			throw new IllegalArgumentException("The value supplied <" + pValue + "> is a directory and it should not be.");
		}

		if (mFileControlType != FileControlType.DIRECTORY && !file.getParentFile().exists()) {
			throw new IllegalArgumentException("The value supplied <" + pValue + "> is specifices a parent directory that does not exist.");
		}

		mValue = pValue;

	}

	@Override
	public boolean setValue(final String pValue) {
		try {
			setString(pValue);
			return true;
		} catch (Throwable pT) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("FileType(");
		buffer.append("value=" + mValue);
		buffer.append(")");
		return buffer.toString();
	}

}
