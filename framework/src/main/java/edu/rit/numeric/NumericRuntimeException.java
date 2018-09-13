/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package edu.rit.numeric;

/**
 * Class NumericRuntimeException is the base class for an unchecked runtime
 * exception thrown by a method in a numerical class.
 * 
 * @author Alan Kaminsky
 * @version 06-Jul-2007
 */
public class NumericRuntimeException extends RuntimeException {

	// Exported constructors.

	/**
	 * Construct a new numeric runtime exception with no detail message and no
	 * cause.
	 */
	public NumericRuntimeException() {
		super();
	}

	/**
	 * Construct a new numeric runtime exception with the given detail message
	 * and no cause.
	 * 
	 * @param message
	 *            Detail message.
	 */
    public NumericRuntimeException(final String message) {
		super(message);
	}

	/**
	 * Construct a new numeric runtime exception with the given cause and the
	 * default detail message.
	 * 
	 * @param cause
	 *            Cause.
	 */
    public NumericRuntimeException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a new numeric runtime exception with the given detail message
	 * and the given cause.
	 */
    public NumericRuntimeException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
