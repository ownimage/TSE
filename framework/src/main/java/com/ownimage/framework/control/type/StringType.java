/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import com.ownimage.framework.control.type.StringMetaType.DisplayType;
import com.ownimage.framework.util.Framework;

import java.util.logging.Logger;

public class StringType extends TypeBase<StringMetaType, String> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    public static StringMetaType NORMAL = new StringMetaType((DisplayType.NORMAL));
    public static StringMetaType LABEL = new StringMetaType((DisplayType.LABEL));
    public static StringMetaType TITLE = new StringMetaType((DisplayType.TITLE));

    /**
     * Instantiates a new SringType
     *
     * @param pValue the value
     */
    public StringType(final String pValue) {
        super(pValue, StringType.NORMAL);
    }

    public StringType(final String pValue, final StringMetaType pMetaType) {
        super(pValue, pMetaType);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.model.ControlModelBase#duplicate()
     */
    @Override
    public StringType clone() {
        return new StringType(mValue, getMetaModel());
    }

    @Override
    public void setString(final String pValue) {
        mValue = pValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append("FileType(")
                .append("value=").append(mValue)
                .append(")")
                .toString();
    }

}
