package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class StringMetaType implements IMetaType<String> {

	public enum DisplayType {
		NORMAL, LABEL
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
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
