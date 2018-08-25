/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.awt.*;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.util.KColor;

public class ColorType extends TypeBase<IMetaType<Color>, Color> {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	/**
	 * Instantiates a new BooleanType
	 * 
	 * @param pValue
	 *            the value
	 */
	public ColorType(final Color pValue) {
		super(pValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.ControlModelBase#duplicate()
	 */
	@Override
	public ColorType clone() {
		// note this is safe as Boolean are immutable
		return new ColorType(mValue);
	}

	@Override
	public String getString() {
		return String.valueOf(getValue().getRGB());
	}

    public String getHex() {
        return KColor.toHex(getValue());
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.IControlModel#setStringValue(java.lang.String)
	 */
	@Override
	public void setString(final String pValue) {
		mValue = new Color(Integer.parseInt(pValue), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("ColorType(");
		buffer.append("value=[" + mValue.getRed() + "," + mValue.getGreen() + "," + mValue.getBlue() + "]");
		buffer.append(")");
		return buffer.toString();
	}

}
