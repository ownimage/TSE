/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.math.KMath;

/**
 * The Class DoubleType.
 * 
 * This will always have a valid meta model, if one is not provided at run time (either using the single argument constructor, or
 * providing null as the value for the MetaModel then the getDefaultMetaModel (ZeroToOne) will be used.
 * 
 * The MetaModel can only be set at construction.
 * 
 */
public class DoubleType extends TypeBase<DoubleMetaType, Double> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = DoubleType.class.getName();
    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	/** The Constant MinusHalfToHalf. */
	public static final DoubleMetaType MinusHalfToHalf = new DoubleMetaType(-0.5d, 0.5d);;

	/** The Constant ZeroToHalf. */
	public static final DoubleMetaType ZeroToHalf = new DoubleMetaType(0.0d, 0.5d);

	/** The Constant ZeroToOne. */
	public static final DoubleMetaType ZeroToOne = new DoubleMetaType(0.0d, 1.0d);

	/** The Constant MinusPiToPi. */
	public static final DoubleMetaType MinusPiToPi = new DoubleMetaType(-Math.PI, Math.PI);

	/** The Constant ZeroTo2Pi. */
	public static final DoubleMetaType ZeroTo2Pi = new DoubleMetaType(0.0d, 2.0d * Math.PI);

	/**
	 * Instantiates a new double control with the ZeroToOne MetaType. If the value supplied is not validated by the MetaType then an
	 * IllegalArgumentException is thrown.
	 * 
	 * @param pValue
	 *            the value
	 */
	// TODO is the correct semantics of creating a Type with an out of range value to limit the value rather than trow an exception
	public DoubleType(final double pValue) {
		this(pValue, ZeroToOne);
	}

	/**
	 * Instantiates a new double control with the specified MetaModel. If the value supplied is not validated by the MetaType then
	 * an IllegalArgumentException is thrown.
	 * 
	 * @param pValue
	 *            the value
	 * @param pDoubleMetaModel
	 *            the double meta model
	 */
	public DoubleType(final double pValue, final DoubleMetaType pDoubleMetaModel) {
		super(pValue, pDoubleMetaModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.ControlModelBase#duplicate()
	 */
	@Override
	public DoubleType clone() {
		// note this is safe as Double are immutable
		return new DoubleType(mValue, mMetaModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ownimage.perception.control.model.ControlModelBase#getDefaultMetaModel()
	 */
	@Override
	public DoubleMetaType getDefaultMetaModel() {
		return ZeroToOne;
	}

	@Override
	public DoubleMetaType getMetaModel() {
		return super.getMetaModel();
	}

	public double getNormalizedValue() {
		final double min = getMetaModel().getMin();
		final double max = getMetaModel().getMax();

		final double value = (mValue - min) / (max - min);
		return value;
	}

	public void setNormalizedValue(final double pValue) {
		// TODO what happens when the value is out of range
		final double min = mMetaModel.getMin();
		final double max = mMetaModel.getMax();
		final double value = (pValue < 0.0d) ? 0.0d : (pValue > 1.0d) ? 1.0d : pValue;

		mValue = min + value * (max - min);
	}

	@Override
	public void setString(final String pValue) {
		// TODO this should be replaced with an set value and get rid of the KMath.inbounds
		// TODO need to create the test cases to do this.
		mValue = KMath.inBounds(Double.valueOf(pValue), getMetaModel().getMin(), getMetaModel().getMax());
	}

	@Override
	public boolean setValue(final Double pValue) {
		final double min = getMetaModel().getMin();
		final double max = getMetaModel().getMax();

		if (pValue >= min && pValue <= max) {
			mValue = pValue;
			return true;

		} else if (pValue > max) {
			mValue = max;
			mLogger.info("Value " + pValue + "is less than min " + min + "  in metamodel.");
			return false;

		} else if (pValue < min) {
			mValue = min;
			mLogger.info("Value " + pValue + "is greater than max " + max + "  in metamodel.");
			return false;

		} else {
			throw new IllegalStateException("Error ... MetaModel.getMin() = " + min + ", MetaModel.getMax() = " + max + ".");
		}
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("DoubleType:(");
		buffer.append("value=" + mValue);
		buffer.append(", min=" + mMetaModel.getMin());
		buffer.append(", max=" + mMetaModel.getMax());
		buffer.append(", step=" + mMetaModel.getStep());
		buffer.append(")");
		return buffer.toString();
	}

}
