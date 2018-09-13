/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package edu.rit.numeric;

/**
 * Class DomainException is an unchecked runtime exception thrown if a
 * function's argument is outside the allowed set of values for the function.
 * 
 * @author Alan Kaminsky
 * @version 06-Jul-2007
 */
public class DomainException extends NumericRuntimeException {

	// Exported constructors.

	/**
	 * Construct a new domain exception with no detail message and no cause.
	 */
	public DomainException() {
		super();
	}

	/**
	 * Construct a new domain exception with the given detail message and no
	 * cause.
	 * 
	 * @param message
	 *            Detail message.
	 */
    public DomainException(final String message) {
		super(message);
	}

	/**
	 * Construct a new domain exception with the given cause and the default
	 * detail message.
	 * 
	 * @param cause
	 *            Cause.
	 */
    public DomainException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a new domain exception with the given detail message and the
	 * given cause.
	 */
    public DomainException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
