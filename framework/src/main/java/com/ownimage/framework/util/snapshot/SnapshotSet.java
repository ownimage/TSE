/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util.snapshot;

import com.ownimage.framework.util.Framework;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class SnapshotSet<T> extends Snapshot<Set<T>> {

    public static final Logger mLogger = Framework.getLogger();

    public SnapshotSet() {
        super(new HashSet<>());
    }

    private SnapshotSet(final SnapshotSet<T> pOther, Link<Set<T>> link) {
        super(pOther, link);
    }

    public SnapshotSet<T> snapshot() {
        synchronized (mSharedSyncObject) {
            final Link<Set<T>> link = createLinkReadyForNewSnapshot();
            return new SnapshotSet<>(this, link);
        }
    }

    public boolean add(final T pValue) {
        synchronized (mSharedSyncObject) {
            final boolean added = getMaster().add(pValue);
            if (added) {
                addChangeLogEntry(pValue, m -> m.add(pValue), m -> m.remove(pValue));
            }
            return added;
        }
    }

    public void remove(final T pValue) {
        synchronized (mSharedSyncObject) {
            final boolean removed = getMaster().remove(pValue);
            if (removed) {
                addChangeLogEntry(pValue, m -> m.remove(pValue), m -> m.add(pValue));
            }
        }
    }

    public int size() {
        synchronized (mSharedSyncObject) {
            return getMaster().size();
        }
    }

    public Stream<T> stream() {
        synchronized (mSharedSyncObject) {
            return getMaster().stream();
        }
    }

    public boolean contains(T pValue) {
        synchronized (mSharedSyncObject) {
            return getMaster().contains(pValue);
        }
    }

    public boolean containsAll(Collection<T> pValues) {
        synchronized (mSharedSyncObject) {
            return getMaster().containsAll(pValues);
        }
    }


}

