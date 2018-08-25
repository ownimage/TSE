/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

public class IntegerType extends TypeBase<IntegerMetaType, Integer> {

	public static IntegerMetaType ZeroToOneHundredStepFive = new IntegerMetaType(0, 100, 5);

	/**
	 * Instantiates a new IntegerType. If the value supplied is not validated by the MetaType then an IllegalArgumentException is
	 * thrown.
	 * 
	 * @param pValue
	 *            the value
	 */
	public IntegerType(final Integer pValue) {
		super(pValue, null);
	}

	/**
	 * Instantiates a new IntegerType. If the value supplied is not validated by the MetaType then an IllegalArgumentException is
	 * thrown.
	 * 
	 * @param pValue
	 *            the value
	 */
	public IntegerType(final Integer pValue, final IntegerMetaType pMetaModel) {
		super(pValue, pMetaModel);
	}

	@Override
	public IntegerType clone() {
		return new IntegerType(getValue(), getMetaModel());
	}

	@Override
	public IntegerMetaType getDefaultMetaModel() {
		return ZeroToOneHundredStepFive;
	}

	@Override
	public IntegerMetaType getMetaModel() {
		return super.getMetaModel();
	}

	// could be used in UI context
	public double getNormalizedValue() {
		final double min = getMetaModel().getMin();
		final double max = getMetaModel().getMax();

		final double value = (mValue - min) / (max - min);
		return value;
	}

	public void setNormalizedValue(final double pValue) {
		Integer value = getMetaModel().getValueForNormalizedValue(pValue);
		setValue(value);
	}

	@Override
	public void setString(final String pValue) {
		// mValue = KMath.inBounds(Integer.valueOf(pValue), getMetaModel().getMin(), getMetaModel().getMin());
		// TODO why does above not work
		setValue(Integer.valueOf(pValue));
	}

	@Override
	public boolean setValue(final Integer pValue) {
		final int min = getMetaModel().getMin();
		final int max = getMetaModel().getMax();

		if (pValue >= min && pValue <= max) {
			super.setValue(pValue);
			return true;

		} else if (pValue > max) {
			super.setValue(max);
			mLogger.info("Value " + pValue + " is greater than max " + max + "  in metamodel.");
			return false;

		} else if (pValue < min) {
			super.setValue(min);
			mLogger.info("Value " + pValue + " is less than min " + min + "  in metamodel.");
			return false;

		} else {
			throw new IllegalStateException("Error ... MetaModel.getMin() = " + min + ", MetaModel.getMax() = " + max + ".");
		}
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("IntegerType:(");
		buffer.append("value=" + mValue);
		buffer.append(", min=" + mMetaModel.getMin());
		buffer.append(", max=" + mMetaModel.getMax());
		buffer.append(", step=" + mMetaModel.getStep());
		buffer.append(")");
		return buffer.toString();
	}
}
