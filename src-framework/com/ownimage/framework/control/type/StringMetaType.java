package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class StringMetaType implements IMetaType<String> {

	public enum DisplayType {
		NORMAL, LABEL
	}


    public final static Logger mLogger = Framework.getLogger();

	public final static long serialVersionUID = 1L;;

	private final DisplayType mDisplayType;

	public StringMetaType(final DisplayType pDisplayType) {
		mDisplayType = pDisplayType;
	}

	public DisplayType getDisplayType() {
		return mDisplayType;
	}

	@Override
	public boolean isValid(final String pValue) {
		return true;
	}

}
