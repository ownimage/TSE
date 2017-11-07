package com.ownimage.framework.util;

public class Id {

	private final String mDescription;

	public Id(final String pDescription) {

		if (pDescription == null) {
			throw new IllegalArgumentException("pDescription must not be null.");
		}

		if (pDescription.length() == 0) {
			throw new IllegalArgumentException("pDescription must not be zero length.");
		}

		mDescription = pDescription;
	}

	public Object getDescription() {
		return mDescription;
	}

}
