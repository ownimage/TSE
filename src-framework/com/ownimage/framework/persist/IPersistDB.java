/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.persist;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public interface IPersistDB {


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
