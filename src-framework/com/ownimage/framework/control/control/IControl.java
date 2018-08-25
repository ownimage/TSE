/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.io.Serializable;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.view.IView;

// TODO: Auto-generated Javadoc
/**
 * The Interface IControl.
 * 
 * @param <C>
 *            the Control class, e.g. DoubleControl
 * @param <T>
 *            the Type class, e.g. DoubleType
 * @param <M>
 *            the Meta Type class of the Control e.g. DoubleMetaType
 * @param <R>
 *            the Raw Type class of the Control e.g. Double
 */
public interface IControl<C, T, M, R> extends IMouseControl, IPersist, IViewable, Serializable, Cloneable, IProperty<R> {


    /** The Constant serialVersionUID. */
	public final static long serialVersionUID = 1L;

	/**
	 * Adds the control change listener.
	 *
	 * @param pListener
	 *            the listener
	 */
	void addControlChangeListener(final IControlChangeListener<?> pListener);

	/**
	 * Adds the control validator.
	 *
	 * @param pValidator
	 *            the validator
	 */
	void addControlValidator(final IControlValidator<?> pValidator);

	/**
	 * Clean.
	 */
	public void clean(); // TODO need to question the visibility of this.

	/**
	 * Clone.
	 *
	 * @return the c
	 */
	public C clone(IContainer pContainer);

	/**
	 * Fire control change event.
	 */
	void fireControlChangeEvent();

	/**
	 * Fire control change event.
	 *
	 * @param pView
	 *            the view
	 * @param pIsMutating
	 *            the is mutating
	 */
	void fireControlChangeEvent(final IView pView, final boolean pIsMutating);

	/**
	 * Fire control validate.
	 *
	 * @return true, if successful
	 */
	boolean fireControlValidate();

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	@Override
	public String getDisplayName();

	/**
	 * Gets the meta type.
	 *
	 * @return the meta type
	 */
	public M getMetaType();

	/**
	 * Gets the property name.
	 *
	 * @return the property name
	 */
	@Override
	public String getPropertyName();

	/**
	 * Gets the string.
	 *
	 * @return the string
	 */
	public String getString();

	/**
	 * Gets the validate value.
	 *
	 * @return the validate value
	 */
	public R getValidateValue();

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	@Override
	public R getValue();

	/**
	 * Checks if is dirty.
	 *
	 * @return true, if is dirty
	 */
	public boolean isDirty();

	/**
	 * Checks if is enabled.
	 *
	 * @return true, if is enabled
	 */
	public boolean isEnabled();

	/**
	 * Checks to see if a control is valid. During the validation of a control this is set true, then by each of the validators
	 * until it becomes false. If after calling all validators it is still true then the value is valid.
	 * 
	 * @return the valid status of the control.
	 */
	public boolean isValid();

	/**
	 * Checks if is visible.
	 *
	 * @return true, if is visible
	 */
	public boolean isVisible();

	/**
	 * Removes the control change listener.
	 *
	 * @param pLIstener
	 *            the l istener
	 */
	void removeControlChangeListener(final IControlChangeListener pLIstener);

	/**
	 * Sets the enabled flag for this object and calls setEnabled on all of the Views of this object.
	 *
	 * @param pValidator
	 *            the validator
	 */
	void removeControlValidator(final IControlValidator pValidator);

	/**
	 * Sets the enabled.
	 *
	 * @param pEnabled
	 *            the new enabled
	 * @return this control
	 */
	public C setEnabled(boolean pEnabled);

	/**
	 * Marks the control as Transient. By default controls are Persistent. Marking them as Transient means that they will not be
	 * persisted using the persistence mechanism. This is a one shot item, once the Control is marked as Transient it can not be
	 * unmarked, it should only be called by the owner of the control.
	 *
	 * @return this control
	 */
	public C setTransient();

	/**
	 * Sets the control valid status. This MUST be synchronised method.
	 *
	 * @param pValid
	 *            the new valid
	 */
	public void setValid(boolean pValid); // TODO need to question the visibility of this

	/**
	 * Marks the object as transient, it will not be persisted to a database using the write method.
	 *
	 * @param pValue
	 *            the value
	 * @return true, if successful
	 * @see #setValue(Object, IView, boolean)
	 */
	public boolean setValue(R pValue);

	/**
	 * Sets the value. Before setting the value it will call validate on each of the validators until one says false. If they all
	 * say true then, if it has one, it will call its container valitator. If that return true also then it will mark the object as
	 * dirty and set the value.
	 *
	 * @param pValue
	 *            the value
	 * @param pSource
	 *            the source view
	 * @param pIsMutating
	 *            the is mutating
	 * @return true, if successful
	 */
	public boolean setValue(R pValue, IView pSource, boolean pIsMutating);

	/**
	 * Sets the visible flag for this obejct and calls setVisible on all of the Views of this object.
	 *
	 * @param pVisible
	 *            the new visible
	 * @return this Control
	 */
	public C setVisible(boolean pVisible);

    public void addVisibileListener(IVisibileListener pListener);

    public void removeVisibleListener(IVisibileListener pListener);

    public void addEnabledListener(IEnabledListener pListener);

    public void removeEnabledListener(IEnabledListener pListener);
}
