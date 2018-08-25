/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Interface IType is a representation of a Type. A Type is a wrapper around a raw type that enables it to be used in the
 * persistence, UI and control frameworks.It allows for the value to be set and get using the native raw type R. It allows for the
 * value to be get/set using a String (although for some types, e.g. PictureType this may throw an unsupported operation exception).
 * It also allows for the inclusion of a MetaType which can give other useful information to the UI layer (e.g. min/max values, step
 * sizes, and display types).
 * 
 * Typically the raw type is immutable to ensure that the only way that the value can be changed is through the Control mechanism.
 * For types such as the PictureType this has needed to be done with a locking mechanism rather than creating copies because of the
 * potential size of the underlying data.
 * 
 * When values are persisted the MetaType is NOT persisted with the value, it is assumed that this is set by the program structure
 * and it is only the value that needs to be save or retrieved.
 * 
 * @param <M>
 *            the MetaType for the Type
 * @param <R>
 *            the Raw type for the Type.
 */
public interface IType<M extends IMetaType<R>, R> extends Serializable, Cloneable {


    /**
	 * Clone creates a deep copy of the object.
	 *
	 * @return the the copy of the object.
	 */
	public IType<M, R> clone();

	/**
	 * Gets the meta model for this type.
	 *
	 * @return the meta model
	 */
	public M getMetaModel();

	/**
	 * Gets a string representation of the value. The string is in a form that is used in the persistence mechanism. Calling
	 * a.setString(a.getString()) should leave the value of a unchanged (barring rounding errors).
	 *
	 * @return the string representation of the value
	 */
	public String getString();

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public R getValue();

	/**
	 * Sets the value based on the string.
	 *
	 * @param pValue
	 *            the new string
	 * @see #getString()
	 */
	public void setString(String pValue);

	/**
	 * Sets the raw value. If the values validates against the metamodel, (or if there is not a metamodel) then it will set the
	 * value and return true. If the value does not validate against the metamodel then it will log an INFO to its logger, set the
	 * value to the closet possible value, and return false.
	 * 
	 *
	 * @param pValue
	 *            the new value
	 * @return true, if successful
	 */
	public boolean setValue(R pValue);
}
