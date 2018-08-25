/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.math.Point;

public class PointType extends TypeBase<IMetaType<Point>, Point> {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	/**
	 * Instantiates a new PointType
	 * 
	 * @param pValue
	 *            the value
	 */
	public PointType(final Point pValue) {
		super(pValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.ControlModelBase#duplicate()
	 */
	@Override
	public PointType clone() {
		// note this is safe as Boolean are immutable
		return new PointType(mValue);
	}

	@Override
	public String getString() {
		return mValue.getX() + "," + mValue.getY();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.IControlModel#setStringValue(java.lang.String)
	 */
	@Override
	public void setString(final String pValue) {
		String[] xy = pValue.split(",");
		double x = Double.parseDouble(xy[0]);
		double y = Double.parseDouble(xy[1]);
		mValue = new Point(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("PointType(");
		buffer.append("x=" + mValue.getX());
		buffer.append(",y=" + mValue.getY());
		buffer.append(")");
		return buffer.toString();
	}

}
