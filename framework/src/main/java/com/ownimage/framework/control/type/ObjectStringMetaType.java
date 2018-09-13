/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import com.ownimage.framework.util.Framework;

import java.util.Collection;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc

/**
 * The Class ObjectMetaType is for an ObjectType that is validated against a list of Objects.
 *
 * @param <R> the generic type
 */
public class ObjectStringMetaType extends ObjectMetaType<String> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private boolean mAllowUserDefinedValue = false;

    public ObjectStringMetaType(final Collection<String> pVals) {
        super(pVals);
    }

    public ObjectStringMetaType(final Collection<String> pVals, final boolean pFilterable) {
        super(pVals, pFilterable);
    }

    public ObjectStringMetaType(final Collection<String> pVals, final boolean pFilterable, final boolean pAllowUserDefinedValue) {
        super(pVals, pFilterable);
        mAllowUserDefinedValue = pAllowUserDefinedValue;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String fromString(final String pString) {
        Framework.logEntry(mLogger);
        final String string;

        if (mAllowUserDefinedValue) {
            string = pString;
        } else {
            string = super.fromString(pString);
        }
        return string;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.control.type.IMetaType#isValid(java.lang.Object)
     */
    @Override
    public boolean isValid(final String pValue) {
        Framework.logEntry(mLogger);
        final boolean valid = mAllowUserDefinedValue || mVals.contains(pValue);
        return valid;
    }

    /**
     * Sets the value based on the string representation which matches the one in the mMap.
     *
     * @param pObjectType the object type
     * @param pValue      the value
     */
    @Override
    public void setString(final ObjectType<String> pObjectType, final String pValue) {
        Framework.logEntry(mLogger);

        if (mAllowUserDefinedValue) {
            pObjectType.setValue(pValue);
        } else {
            super.setString(pObjectType, pValue);
        }
    }
}
