package com.ownimage.framework.util.immutable;

import lombok.val;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Stack;
import java.util.UUID;
import java.util.function.Consumer;

public class ImmutableNode<M> implements Serializable {

    private M mMaster;
    transient final private WeakReference<ImmutableNode<M>> mPrevious;
    private final UUID mSynchronisation;
    private ImmutableNode<M> mToMaster;
    private final transient Consumer<M> mRedo;
    private final transient Consumer<M> mUndo;


    protected ImmutableNode(M pMaster) {
        mSynchronisation = UUID.randomUUID();
        setMasterAndToMaster(pMaster, null);
        mPrevious = new WeakReference<>(null);
        mUndo = m -> {
        };
        mRedo = m -> {
        };
    }

    protected ImmutableNode(ImmutableNode<M> pPrevious, Consumer<M> pRedo, Consumer<M> pUndo) {
        mSynchronisation = pPrevious.mSynchronisation;
        synchronized (getSynchronisationObject()) {
            setMasterAndToMaster(pPrevious.getMaster(), null);
            mPrevious = new WeakReference<>(pPrevious);
            pRedo.accept(mMaster);
            mRedo = pRedo;
            mUndo = pUndo;
            pPrevious.setMasterAndToMaster(null, this);
        }
    }

    protected void setMasterAndToMaster(M pMaster, ImmutableNode<M> pToMaster) {
        synchronized (getSynchronisationObject()) {
            if (pMaster == null && pToMaster == null) {
                throw new IllegalArgumentException("pMaster and pToMaster must not both be null.");
            }
            if (pMaster != null && pToMaster != null) {
                throw new IllegalArgumentException("pMaster and pToMaster must not both have values.");
            }
            mMaster = pMaster;
            mToMaster = pToMaster;
        }
    }

    protected Object getSynchronisationObject() {
        return mSynchronisation;
    }

    protected M getMaster() {
        synchronized (getSynchronisationObject()) {
            if (mMaster != null) return mMaster;
            setMasterAndToMaster(getMaster(this), null);
            return mMaster;
        }
    }

    private M getMaster(final ImmutableNode<M> pToMe) {
        synchronized (getSynchronisationObject()) {
            if (pToMe == mToMaster) {
                throw new IllegalArgumentException("pToMe must not equal mToMaster.");
            }
            if (mMaster == null && mToMaster == null) {
                throw new IllegalStateException("mMaster and mToMaster must not both be null.");
            }
            M master = mMaster;
            if (master == null) {
                val stack = new Stack<ImmutableNode<M>>();
                stack.push(this);
                while (stack.peek().mMaster == null) {
                    stack.push(stack.peek().mToMaster);
                }
                master = stack.peek().mMaster;
                while (!stack.empty()) {
                    val current = stack.pop();
                    if (current == this) {
                        if (current.mToMaster == current.mPrevious.get()) {
                            current.mRedo.accept(master);
                        }
                        current.setMasterAndToMaster(master, null);
                    } else {
                        val to = stack.peek();
                        if (to == current.mPrevious.get()) {
                            current.mUndo.accept(master);
                        } else if (current.mToMaster == current.mPrevious.get()) {
                            current.mRedo.accept(master);
                        }
                        current.setMasterAndToMaster(null, to);
                    }
                }
            }
            return master;
        }
    }
}
