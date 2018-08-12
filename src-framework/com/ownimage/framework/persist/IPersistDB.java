/* This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */
package com.ownimage.framework.persist;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public interface IPersistDB {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = ControlBase.class.getName();
	public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	/**
	 * Reads the value of the property. If it is not found then null is returned.
	 *
	 * @param pId
	 *            the id of the property
	 * @return the value
	 */
	public String read(String pId);

	public void write(String pId, String pValue);
}
