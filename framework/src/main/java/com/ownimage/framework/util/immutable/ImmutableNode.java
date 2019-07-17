package com.ownimage.framework.util.immutable;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class ImmutableNode<M> implements Serializable {

    private M mMaster;
    transient private WeakReference<ImmutableNode<M>> mPrevious;
    private Object mSynchronisation;
    private ImmutableNode<M> mToMaster;
    private Consumer<M> mRedo;
    private Consumer<M> mUndo;

    protected ImmutableNode(M pMaster) {
        setMasterAndToMaster(pMaster, null);
        mPrevious = new WeakReference<>(null);
        mSynchronisation = new Object();
        mUndo = m -> {
        };
        mRedo = m -> {
        };
    }

    protected ImmutableNode(ImmutableNode<M> pPrevious, Consumer<M> pRedo, Consumer<M> pUndo) {
        setMasterAndToMaster(pPrevious.mMaster, null);
        mPrevious = new WeakReference<>(pPrevious);
        pRedo.accept(mMaster);
        mSynchronisation = pPrevious.mSynchronisation;
        mRedo = pRedo;
        mUndo = pUndo;

        pPrevious.setMasterAndToMaster(null, this);
    }

    protected void setMasterAndToMaster(M pMaster, ImmutableNode<M> pToMaster) {
        if (pMaster == null && pToMaster == null) {
            throw new IllegalArgumentException("pMaster and pToMaster must not both be null.");
        }
        if (pMaster != null && pToMaster != null) {
            throw new IllegalArgumentException("pMaster and pToMaster must not both have values.");
        }
        mMaster = pMaster;
        mToMaster = pToMaster;
    }

    protected Object getSynchronisationObject() {
        return mSynchronisation;
    }

    protected M getMaster() {
        if (mMaster != null) return mMaster;
        setMasterAndToMaster(getMaster(this), null);
        return mMaster;
    }

    private M getMaster(final ImmutableNode<M> pToMe) {
        if (pToMe == mToMaster) {
            throw new IllegalArgumentException("pToMe must not equal mToMaster.");
        }
        if (mMaster == null && mToMaster == null) {
            throw new IllegalStateException("mMaster and mToMaster must not both be null.");
        }
        M master = mMaster;
        if (master == null) {
            master = mToMaster.getMaster(this);
        }
        if (pToMe == mPrevious.get()) {
            mUndo.accept(master);
        } else if (mToMaster == mPrevious.get()) {
            mRedo.accept(master);
        }

        setMasterAndToMaster(null, pToMe);
        return master;
    }
}
