/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import com.ownimage.framework.util.Framework;

import java.util.logging.Logger;

public class BooleanType extends TypeBase<IMetaType<Boolean>, Boolean> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    /**
     * Instantiates a new BooleanType
     *
     * @param pValue the value
     */
    public BooleanType(final boolean pValue) {
        super(pValue);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.model.ControlModelBase#duplicate()
     */
    @Override
    public BooleanType clone() {
        // note this is safe as Boolean are immutable
        return new BooleanType(mValue);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.model.IControlModel#setStringValue(java.lang.String)
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
