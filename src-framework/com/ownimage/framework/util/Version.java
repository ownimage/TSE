/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;


public class Version {


    private final int mMajor;
	private final int mMinor;
	private final int mRev;
	private String mRevision;
	private final String mDate;

	// Introduced following move to Git
	public Version(final int pMajor, final int pMinor, final int pRev, final String pDate) {
		mMajor = pMajor;
		mMinor = pMinor;
		mRev = pRev;
		mDate = pDate;
	}

	@Deprecated
	// deprecated following move to Git
	public Version(final int pMajor, final int pMinor, final int pRev, final String pRevsion, final String pDate) {
		mMajor = pMajor;
		mMinor = pMinor;
		mRev = pRev;
		mRevision = pRevsion;
		mDate = pDate;
	}

	public String getDate() {
		return mDate;
	}

	public String getFullInfo() {
		if (mRevision != null) { return mMajor + "." + mMinor + "." + mRev + " : " + mRevision + " : " + mDate; }
		return mMajor + "." + mMinor + "." + mRev + " : " + mDate;
	}

	public int getMajor() {
		return mMajor;
	}

	public int getMinor() {
		return mMinor;
	}

	public int getRev() {
		return mRev;
	}

	public String getRevision() {
		return mRevision;
	}

}
