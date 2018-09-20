package com.ownimage.framework.util.snapshot;

import com.ownimage.framework.util.Framework;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ChangeLog<M> implements Node<M> {

    private static final Logger mLogger = Framework.getLogger();

    private WeakReference<Node<M>> mTop;
    private Node<M> mToMaster;

    private final HashMap<Object, Consumer<M>> mRedoLog = new java.util.HashMap<>();
    private final HashMap<Object, Consumer<M>> mUndoLog = new java.util.HashMap<>();

    ChangeLog(Node<M> pTop, Node<M> pToMaster) {
        setTop(pTop);
        setToMaster(pToMaster);
    }

    private M undo(M pMaster) {
        mUndoLog.forEach((k, v) -> v.accept(pMaster));
        return pMaster;
    }

    private M redo(M pMaster) {
        mRedoLog.forEach((k, v) -> v.accept(pMaster));
        return pMaster;
    }

    void addChange(Object pKey, Consumer<M> pRedo, Consumer<M> pUndo) {
        if (mRedoLog.containsKey(pKey)) mRedoLog.replace(pKey, pRedo);
        else mRedoLog.put(pKey, pRedo);
        if (!mUndoLog.containsKey(pKey)) mUndoLog.put(pKey, pUndo);
    }

    @Override
    public M getMaster(final Node pToMe, final Object pSecret) {
        if (pToMe == mToMaster) {
            throw new IllegalArgumentException("pToMe must not equal mToMaster");
        }
        if (pToMe != mTop.get() && mTop.get() != mToMaster) {
            throw new IllegalArgumentException(String.format("Must be traversing to or from mTop," +
                    "\nmTop.get() = %s, \nmToMaster = %s, \npToMe = %s", mTop.get(), mToMaster, pToMe));
        }
        final M masterIn = mToMaster.getMaster(this, pSecret);
        M masterOut = null;
        if (pToMe == mTop.get()) masterOut = redo(masterIn);
        else if (mToMaster == mTop.get()) masterOut = undo(masterIn);
        if (masterOut == null) {
            throw new RuntimeException(String.format("Neither undo nor redo performed, " +
                    "mTop = %s, mMaster.get() = %s, pToMe = %s", mTop.get(), mToMaster, pToMe));
        }
        setToMaster(pToMe);
        return masterOut;
    }

    void setTop(final Node<M> pTop) {
        mTop = new WeakReference<>(pTop);
    }

    public void setToMaster(final Node<M> pNode) {
        mToMaster = pNode;
    }
}
