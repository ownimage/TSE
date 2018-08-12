package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class NullMetaType<R> implements IMetaType<R> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = ControlBase.class.getName();
	public final static Logger mLogger = Framework.getLogger();

	public final static long serialVersionUID = 1L;

	@Override
	public boolean isValid(final R pValue) {
		return true;
	}
}