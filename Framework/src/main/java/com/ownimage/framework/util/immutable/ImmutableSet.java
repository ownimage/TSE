package com.ownimage.framework.util.immutable;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

public class ImmutableSet<E> extends ImmutableNode<HashSet<E>> {

    public ImmutableSet() {
        super(new HashSet<E>());
    }

    private ImmutableSet(ImmutableSet pPrevious, Consumer<HashSet<E>> pRedo, Consumer<HashSet<E>> pUndo) {
        super(pPrevious, pRedo, pUndo);
    }

    public ImmutableSet<E> add(final E pElement) {
        synchronized (getSynchronisationObject()) {
            if (getMaster().contains(pElement)) {
                return this;
            }
            Consumer<HashSet<E>> redo = m -> m.add(pElement);
            Consumer<HashSet<E>> undo = m -> m.remove(pElement);
            return new ImmutableSet(this, redo, undo);
        }
    }

    public ImmutableSet remove(final E pElement) {
        synchronized (getSynchronisationObject()) {
            if (!getMaster().contains(pElement)) {
                return this;
            }
            Consumer<HashSet<E>> redo = m -> m.remove(pElement);
            Consumer<HashSet<E>> undo = m -> m.add(pElement);
            return new ImmutableSet(this, redo, undo);
        }
    }

    public ImmutableSet clear() {
        return new ImmutableSet();
    }

    public int size() {
        synchronized (getSynchronisationObject()) {
            return getMaster().size();
        }
    }

    public boolean containsAll(final Collection<E> pElements) {
        synchronized (getSynchronisationObject()) {
            return getMaster().containsAll(pElements);
        }
    }

    public boolean contains(final E pElement) {
        synchronized (getSynchronisationObject()) {
            return getMaster().contains(pElement);
        }
    }
}
