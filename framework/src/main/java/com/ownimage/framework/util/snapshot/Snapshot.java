package com.ownimage.framework.util.snapshot;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;

public class Snapshot<M> implements Node<M> {

    private final Object mSharedSyncObject; // TODO
    private M mMaster;
    private WeakReference<ChangeLog<M>> mChangeLog;
    private Node<M> mToMaster;

    /**
     * @param pMaster do not let references to this leak out of the super class constructor.
     */
    protected Snapshot(M pMaster) {
        mMaster = pMaster;
        mSharedSyncObject = new Object();
    }

    protected Snapshot(Snapshot<M> pOther, Link<M> pToMaster) {
        mSharedSyncObject = pOther.mSharedSyncObject;
        final ChangeLog<M> changeLog = new ChangeLog<>(this, pToMaster);
        mToMaster = changeLog;
        setChangeLog(changeLog);
    }

    protected Link<M> createLinkReadyForNewSnapshot() {
        makeMeMaster();
        final ChangeLog<M> newChangeLog = new ChangeLog<>(this, this);
        final Link<M> link = new Link<>(newChangeLog);
        final ChangeLog<M> oldChangeLog = getChangeLog();
        if (oldChangeLog != null) {
            oldChangeLog.setTop(link);
            oldChangeLog.setToMaster(link);
        }
        setChangeLog(newChangeLog);

        return link;
    }

    private void makeMeMaster() {
        if (mMaster != null) return;
        mMaster = mToMaster.getMaster(this, mSharedSyncObject);
        mToMaster = null;
        if (mMaster == null) throw new RuntimeException("mMaster should have been set.");
    }

    protected void addChangeLogEntry(Object pKey, Consumer<M> pRedo, Consumer<M> pUndo) {
        final ChangeLog<M> changeLog = getChangeLog();
        if (changeLog == null) return;
        changeLog.addChange(pKey, pRedo, pUndo);
    }

    private ChangeLog<M> getChangeLog() {
        if (mChangeLog == null) return null;
        return mChangeLog.get();
    }

    private ChangeLog<M> getOrCreateChangeLog() {
        ChangeLog<M> changeLog = getChangeLog();
        if (changeLog == null) {
            changeLog = new ChangeLog<>(this, this);
            setChangeLog(changeLog);
        }
        return changeLog;
    }

    private void setChangeLog(ChangeLog<M> pChangeLog) {
        mChangeLog = new WeakReference<>(pChangeLog);
    }

    protected M getMaster() {
        if (mMaster != null) {
            return mMaster;
        }
        makeMeMaster();
        return mMaster;
    }

    @Override
    public M getMaster(final Node<M> pToMe, final Object pSecret) {
        if (mSharedSyncObject != pSecret) {
            throw new IllegalArgumentException("Wrong shared secret.");
        }
        if (mMaster == pToMe) {
            throw new IllegalArgumentException("pToMe must not equal mMaster.");
        }
        if (mMaster == null) {
            throw new IllegalStateException("You cannot request a master from a Snapshot that does not have a master.");
        }
        mToMaster = pToMe;
        M master = mMaster;
        mMaster = null;
        return master;
    }
}
