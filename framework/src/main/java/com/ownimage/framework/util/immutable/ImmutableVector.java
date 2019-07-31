package com.ownimage.framework.util.immutable;

import lombok.val;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ImmutableVector<E> extends ImmutableNode<Vector<E>> {

    public ImmutableVector() {
        super(new Vector<>());
    }

    private ImmutableVector(Vector<E> mValue) {
        super(mValue);
    }

    private ImmutableVector(ImmutableVector<E> pPrevious, Consumer<Vector<E>> pRedo, Consumer<Vector<E>> pUndo) {
        super(pPrevious, pRedo, pUndo);
    }

    public ImmutableVector<E> add(final E pElement) {
        synchronized (getSynchronisationObject()) {
            Consumer<Vector<E>> redo = m -> m.add(pElement);
            Consumer<Vector<E>> undo = m -> m.remove(pElement);
            return new ImmutableVector<>(this, redo, undo);
        }
    }

    public ImmutableVector<E> add(final int pIndex, final E pElement) {
        synchronized (getSynchronisationObject()) {
            Consumer<Vector<E>> redo = m -> m.add(pIndex, pElement);
            Consumer<Vector<E>> undo = m -> m.remove(pIndex);
            return new ImmutableVector<>(this, redo, undo);
        }
    }

    public ImmutableVector addAll(Collection<E> pAll) {
        synchronized (getSynchronisationObject()) {
            val all = new Vector<>(pAll);
            val size = getMaster().size();
            Consumer<Vector<E>> redo = m -> m.addAll(all);
            Consumer<Vector<E>> undo = m -> m.setSize(size);
            return new ImmutableVector<>(this, redo, undo);
        }
    }

    public ImmutableVector clear() {
        return new ImmutableVector();
    }

    public boolean contains(final E pElement) {
        synchronized (getSynchronisationObject()) {
            return getMaster().contains(pElement);
        }
    }

    public boolean containsAll(final Collection<E> pElements) {
        synchronized (getSynchronisationObject()) {
            return getMaster().containsAll(pElements);
        }
    }

    public E get(final int pIndex) {
        synchronized (getSynchronisationObject()) {
            return getMaster().get(pIndex);
        }
    }

    public ImmutableVector remove(final E pElement) {
        synchronized (getSynchronisationObject()) {
            val index = getMaster().indexOf(pElement);
            if (index == -1) {
                return this;
            }
            Consumer<Vector<E>> redo = m -> m.remove(pElement);
            Consumer<Vector<E>> undo = m -> m.add(index, pElement);
            return new ImmutableVector<>(this, redo, undo);
        }
    }

    public ImmutableVector remove(final int pIndex) {
        synchronized (getSynchronisationObject()) {
            if (0 > pIndex || pIndex >= getMaster().size()) {
                throw new IllegalArgumentException();
            }
            val element = getMaster().get(pIndex);
            Consumer<Vector<E>> redo = m -> m.remove(pIndex);
            Consumer<Vector<E>> undo = m -> m.add(pIndex, element);
            return new ImmutableVector<>(this, redo, undo);
        }
    }

    public Optional<E> firstElement() {
        synchronized (getSynchronisationObject()) {
            if (getMaster().size() == 0) {
                return Optional.empty();
            }
            return Optional.ofNullable(getMaster().firstElement());
        }
    }

    public Optional<E> lastElement() {
        synchronized (getSynchronisationObject()) {
            if (getMaster().size() == 0) {
                return Optional.empty();
            }
            return Optional.ofNullable(getMaster().lastElement());
        }
    }

    /**
     * This will return:
     * EITHER the existing immutable if there are no elements in pAll,
     * OR this immutable if this immutable contains no elements in pAll
     * OR a new immutable containing the result.
     * Because checking is potentially an o(2) operation and might then result in a clone this is potentially
     * an expensive operation.
     *
     * @param pAll a Collection of elements to add
     * @return an immutable representing the result
     */
    public ImmutableVector removeAll(Collection<E> pAll) {
        if (pAll.size() == 0) {
            return this;
        }
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            val change = pAll.stream()
                    .parallel()
                    .anyMatch(master::contains);
            if (!change) {
                return this;
            }
            // cloning technique done as trying to put removed elements back into a vector seems error prone
            val vector = (Vector<E>) master.clone();
            vector.removeAll(pAll);
            return new ImmutableVector<>(vector);
        }
    }

    public void forEach(Consumer<E> pFn) {
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            Vector<E> copy = (Vector<E>) master.clone();
            copy.forEach(pFn);
        }
    }

    public ImmutableVector set(final int pIndex, final E pValue) {
        synchronized (getSynchronisationObject()) {
            val originalValue = getMaster().get(pIndex);
            Consumer<Vector<E>> redo = m -> m.set(pIndex, pValue);
            Consumer<Vector<E>> undo = m -> m.set(pIndex, originalValue);
            return new ImmutableVector<>(this, redo, undo);
        }
    }

    public Stream<E> stream() {
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            Vector<E> copy = (Vector<E>) master.clone();
            return copy.stream();
        }
    }

    public Vector<E> toVector() {
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            return (Vector<E>) master.clone();
        }
    }

    public int size() {
        synchronized (getSynchronisationObject()) {
            return getMaster().size();
        }
    }

    private void readObject(ObjectInputStream pInputStream) throws ClassNotFoundException, IOException {
        Vector<E> master = (Vector<E>) pInputStream.readObject();
        setMasterAndToMaster(master, null);
    }

    private void writeObject(ObjectOutputStream pOutputStream) throws IOException {
        pOutputStream.writeObject(getMaster());
    }

}
