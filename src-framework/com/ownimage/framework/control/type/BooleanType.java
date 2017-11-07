/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.util.Version;

public class BooleanType extends TypeBase<IMetaType<Boolean>, Boolean> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	public final static String mClassname = ControlBase.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	/**
	 * Instantiates a new BooleanType
	 * 
	 * @param pValue
	 *            the value
	 */
	public BooleanType(final boolean pValue) {
		super(pValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.ControlModelBase#duplicate()
	 */
	@Override
	public BooleanType clone() {
		// note this is safe as Boolean are immutable
		return new BooleanType(mValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.IControlModel#setStringValue(java.lang.String)
	 */
	@Override
	public void setString(final String pValue) {
		mValue = Boolean.parseBoolean(pValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("BooleanType(");
		buffer.append("value=" + mValue);
		buffer.append(")");
		return buffer.toString();
	}

}
