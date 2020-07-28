package com.ownimage.framework.util.immutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImmutableSet<E> extends ImmutableNode<HashSet<E>> {

    public ImmutableSet() {
        super(new HashSet<>());
    }

    private ImmutableSet(ImmutableSet<E> pPrevious, Consumer<HashSet<E>> pRedo, Consumer<HashSet<E>> pUndo) {
        super(pPrevious, pRedo, pUndo);
    }

    public ImmutableSet<E> add(final E pElement) {
        synchronized (getSynchronisationObject()) {
            if (getMaster().contains(pElement)) {
                return this;
            }
            Consumer<HashSet<E>> redo = m -> m.add(pElement);
            Consumer<HashSet<E>> undo = m -> m.remove(pElement);
            return new ImmutableSet<E>(this, redo, undo);
        }
    }

    public ImmutableSet<E> remove(final E pElement) {
        synchronized (getSynchronisationObject()) {
            if (!getMaster().contains(pElement)) {
                return this;
            }
            Consumer<HashSet<E>> redo = m -> m.remove(pElement);
            Consumer<HashSet<E>> undo = m -> m.add(pElement);
            return new ImmutableSet<E>(this, redo, undo);
        }
    }

    public ImmutableSet<E> addAll(Collection<E> pAll) {
        synchronized (getSynchronisationObject()) {
            HashSet<E> master = getMaster();
            HashSet<E> all = new HashSet<>(pAll);
            all.removeAll(master);
            Consumer<HashSet<E>> redo = m -> m.addAll(all);
            Consumer<HashSet<E>> undo = m -> m.removeAll(all);
            return new ImmutableSet<E>(this, redo, undo);
        }
    }

    public ImmutableSet<E> removeAll(Collection<E> pAll) {
        synchronized (getSynchronisationObject()) {
            HashSet<E> master = getMaster();
            ArrayList<E> remove = pAll.stream().filter(e -> master.contains(e)).collect(Collectors.toCollection(ArrayList::new));
            if (remove.size() == 0) {
                return this;
            }
            Consumer<HashSet<E>> redo = m -> m.removeAll(remove);
            Consumer<HashSet<E>> undo = m -> m.addAll(remove);
            return new ImmutableSet<E>(this, redo, undo);
        }
    }

    public void forEach(Consumer<E> pFn) {
        synchronized (getSynchronisationObject()) {
            HashSet<E> master = getMaster();
            master.forEach(pFn);
        }
    }

    public Stream<E> stream() {
        synchronized (getSynchronisationObject()) {
            HashSet<E> master = getMaster();
            HashSet<E> copy = (HashSet<E>) master.clone();
            return copy.stream();
        }
    }

    public Collection<E> toCollection() {
        synchronized (getSynchronisationObject()) {
            HashSet<E> master = getMaster();
            HashSet<E> copy = (HashSet<E>) master.clone();
            return copy;
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
