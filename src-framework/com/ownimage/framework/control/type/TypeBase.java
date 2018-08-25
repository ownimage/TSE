/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public abstract class TypeBase<M extends IMetaType<R>, R> implements IType<M, R> {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	protected R mValue;
	protected M mMetaModel;

	public TypeBase(final R pValue) {
		this(pValue, null);
	}

	public TypeBase(final R pValue, final M pMetaModel) {
		mMetaModel = pMetaModel != null ? pMetaModel : getDefaultMetaModel();
		if (mMetaModel != null && !mMetaModel.isValid(pValue)) {
			throw new IllegalArgumentException("pValue: " + pValue + " is not valid for its MetaType " + mMetaModel);
		}
		setValue(pValue);
	}

	@Override
	public IType<M, R> clone() {
		throw new UnsupportedOperationException("clone()");
	}

	protected M getDefaultMetaModel() {
		return null;
	}

	@Override
	public M getMetaModel() {
		return mMetaModel;
	}

	@Override
	public String getString() {
		return mValue.toString();
	}

	@Override
	public R getValue() {
		return mValue;
	}

	@Override
	public boolean setValue(final R pValue) {
		if (mMetaModel == null || mMetaModel.isValid(pValue)) {
			mValue = pValue;
			return true;
		}
		return false;
	}
}
