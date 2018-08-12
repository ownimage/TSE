/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

/**
 * The Class DoubleMetaModel. This holds information on the min and max values, and also some display information too i.e. whether
 * it should be represented as a SLIDER, or a SPINNER, or BOTH.
 */
public class DoubleMetaType implements IMetaType<Double> {

	/**
	 * The Enum DisplayType.
	 */
	public enum DisplayType {
		SLIDER, SPINNER, BOTH
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = DoubleMetaType.class.getName();
    public final static Logger mLogger = Framework.getLogger();

	public final static long serialVersionUID = 1L;

	/** The minimum value. */
	private final double mMin;

	/** The maximum value. */
	private final double mMax;

	/** The step that a spinner should increase the value by. */
	private final double mStep;

	/** The display component for this, either SPINNER, SLIDER or BOTH. */
	private final DisplayType mDisplayType;

	/**
	 * Instantiates a new double meta model. This will default to type slider. The step will default to (max - min)/100.
	 * 
	 * @param pMin
	 *            the min value
	 * @param pMax
	 *            the max value
	 */
	public DoubleMetaType(final double pMin, final double pMax) {
		this(pMin, pMax, (pMax - pMin) / 100.d, DisplayType.SLIDER);
	}

	/**
	 * Instantiates a new double meta model.
	 * 
	 * @param pMin
	 *            the min value
	 * @param pMax
	 *            the max value
	 * @param pStep
	 *            the step size
	 * @param pType
	 *            the display type
	 */
	public DoubleMetaType(final double pMin, final double pMax, final double pStep, final DisplayType pType) {
		mMin = pMin;
		mMax = pMax;
		mStep = pStep;
		mDisplayType = pType;
	}

	/**
	 * Gets the display type.
	 * 
	 * @return the type
	 */
	public DisplayType getDisplayType() {
		return mDisplayType;
	}

	/**
	 * Gets the max value.
	 * 
	 * @return the max
	 */
	public double getMax() {
		return mMax;
	}

	/**
	 * Gets the min value.
	 * 
	 * @return the min
	 */
	public double getMin() {
		return mMin;
	}

	/**
	 * Gets the step value.
	 * 
	 * @return the step
	 */
	public double getStep() {
		return mStep;
	}

	public Double getValueForNormalizedValue(final double pNormalizedValue) {
		final Double value = getMin() + (getMax() - getMin()) * pNormalizedValue;
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.IMetaModel#isValid(java.lang.Object)
	 */
	@Override
	public boolean isValid(final Double pValue) {
		return getMin() <= pValue && pValue <= getMax();
	}
}
