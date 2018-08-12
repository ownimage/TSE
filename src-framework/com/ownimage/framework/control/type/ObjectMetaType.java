/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

// TODO: Auto-generated Javadoc
/**
 * The Class ObjectMetaType is for an ObjectType that is validated against a list of Objects.
 *
 * @param <R>
 *            the generic type
 */
public class ObjectMetaType<R> implements IMetaType<R> {


    public final static String mClassname = ObjectMetaType.class.getName();
    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	/** The List of objects that is used to validate the associated ObjectType against. */
	protected final Collection<R> mVals;

	/** This maps the Objects against their string representations based on getString() method.. */
	protected final Map<String, R> mMap = new HashMap<String, R>();

	private final boolean mFilterable;

	public ObjectMetaType(final Collection<R> pVals) {
		this(pVals, false);
	}

	public ObjectMetaType(final Collection<R> pVals, final boolean pFilterable) {
		if (pVals == null) {
			throw new IllegalArgumentException("pVals cannot be null");
		}

		mVals = new Vector<R>(pVals);
		mVals.forEach(v -> mMap.put(getString(v), v));
		mFilterable = pFilterable;
	}

	public R fromString(final String pString) {
		Framework.logEntry(mLogger);
		R value = mMap.get(pString);
		if (value == null) {
			throw new IllegalArgumentException("pValue = " + pString + ", does not map to any known value.");
		}
		return value;
	}

	public Collection<R> getAllowedValues() {
		Framework.logEntry(mLogger);
		Vector<R> values = new Vector<R>(mVals);
		return values;
	}

	/**
	 * Gets the string representation of a value. Note that in the UI the raw type R gets lost and so we need to check it here.
	 *
	 * @param pValue
	 *            the value
	 * @return the string
	 */
	public String getString(final Object pObject) {
		Framework.logEntry(mLogger);
		R rObject = (R) pObject;
		return rObject.toString();
	}

	/**
	 * Checks if is combo box.
	 *
	 * @return true, if is combo box
	 */
	public boolean isFilterable() {
		Framework.logEntry(mLogger);
		return mFilterable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.type.IMetaType#isValid(java.lang.Object)
	 */
	@Override
	public boolean isValid(final R pValue) {
		Framework.logEntry(mLogger);
		boolean valid = mVals.contains(pValue);
		return valid;
	}

	/**
	 * Sets the value based on the string representation which matches the one in the mMap.
	 *
	 * @param pObjectType
	 *            the object type
	 * @param pValue
	 *            the value
	 */
	public void setString(final ObjectType<R> pObjectType, final String pValue) {
		Framework.logEntry(mLogger);
		R value = mMap.get(pValue);
		if (value == null) {
			throw new IllegalArgumentException("pValue = " + pValue + ", does not map to any known value.");
		}

		pObjectType.setValue(value);
	}
}
