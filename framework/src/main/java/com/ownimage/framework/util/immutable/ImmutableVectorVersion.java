package com.ownimage.framework.util.immutable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ImmutableVectorVersion<E> extends ImmutableNode<Vector<E>> implements IImmutableVector<E> {

    public ImmutableVectorVersion() {
        super(new Vector<>());
    }

    private ImmutableVectorVersion(Vector<E> mValue) {
        super(mValue);
    }

    private ImmutableVectorVersion(ImmutableVectorVersion<E> pPrevious, Consumer<Vector<E>> pRedo, Consumer<Vector<E>> pUndo) {
        super(pPrevious, pRedo, pUndo);
    }

    @Override
    public ImmutableVectorVersion<E> add(E pElement) {
        synchronized (getSynchronisationObject()) {
            Consumer<Vector<E>> redo = m -> m.add(pElement);
            Consumer<Vector<E>> undo = m -> m.remove(pElement);
            return new ImmutableVectorVersion<>(this, redo, undo);
        }
    }

    @Override
    public ImmutableVectorVersion<E> add(int pIndex, E pElement) {
        synchronized (getSynchronisationObject()) {
            Consumer<Vector<E>> redo = m -> m.add(pIndex, pElement);
            Consumer<Vector<E>> undo = m -> m.remove(pIndex);
            return new ImmutableVectorVersion<>(this, redo, undo);
        }
    }

    @Override
    public ImmutableVectorVersion<E> addAll(Collection<E> pAll) {
        synchronized (getSynchronisationObject()) {
            var all = new Vector<>(pAll);
            var size = getMaster().size();
            Consumer<Vector<E>> redo = m -> m.addAll(all);
            Consumer<Vector<E>> undo = m -> m.setSize(size);
            return new ImmutableVectorVersion<>(this, redo, undo);
        }
    }

    @Override
    public ImmutableVectorVersion<E> addAll(IImmutableVector<E> pAll) {
        return addAll(pAll.toVector());
    }

    @Override
    public ImmutableVectorVersion<E> clear() {
        return new ImmutableVectorVersion<E>();
    }

    @Override
    public boolean contains(E pElement) {
        synchronized (getSynchronisationObject()) {
            return getMaster().contains(pElement);
        }
    }

    @Override
    public boolean containsAll(Collection<E> pElements) {
        synchronized (getSynchronisationObject()) {
            return getMaster().containsAll(pElements);
        }
    }

    @Override
    public E get(int pIndex) {
        synchronized (getSynchronisationObject()) {
            return getMaster().get(pIndex);
        }
    }

    @Override
    public ImmutableVectorVersion remove(E pElement) {
        synchronized (getSynchronisationObject()) {
            var index = getMaster().indexOf(pElement);
            if (index == -1) {
                return this;
            }
            Consumer<Vector<E>> redo = m -> m.remove(pElement);
            Consumer<Vector<E>> undo = m -> m.add(index, pElement);
            return new ImmutableVectorVersion<>(this, redo, undo);
        }
    }

    @Override
    public ImmutableVectorVersion remove(int pIndex) {
        synchronized (getSynchronisationObject()) {
            if (0 > pIndex || pIndex >= getMaster().size()) {
                throw new IllegalArgumentException();
            }
            var element = getMaster().get(pIndex);
            Consumer<Vector<E>> redo = m -> m.remove(pIndex);
            Consumer<Vector<E>> undo = m -> m.add(pIndex, element);
            return new ImmutableVectorVersion<>(this, redo, undo);
        }
    }

    @Override
    public Optional<E> firstElement() {
        synchronized (getSynchronisationObject()) {
            if (getMaster().isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(getMaster().firstElement());
        }
    }

    @Override
    public int indexOf(E e) {
        synchronized (getSynchronisationObject()) {
            return getMaster().indexOf(e);
        }
    }

    @Override
    public Optional<E> lastElement() {
        synchronized (getSynchronisationObject()) {
            if (getMaster().isEmpty()) {
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
    @Override
    @SuppressWarnings("unchecked")
    public ImmutableVectorVersion removeAll(Collection<E> pAll) {
        if (pAll.isEmpty()) {
            return this;
        }
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            var change = pAll.stream()
                    .parallel()
                    .anyMatch(master::contains);
            if (!change) {
                return this;
            }
            // cloning technique done as trying to put removed elements back into a vector seems error prone
            var vector = (Vector<E>) master.clone();
            vector.removeAll(pAll);
            return new ImmutableVectorVersion<>(vector);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(Consumer<E> pFn) {
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            Vector<E> copy = (Vector<E>) master.clone();
            copy.forEach(pFn);
        }
    }

    @Override
    public ImmutableVectorVersion set(int pIndex, E pValue) {
        synchronized (getSynchronisationObject()) {
            var originalValue = getMaster().get(pIndex);
            Consumer<Vector<E>> redo = m -> m.set(pIndex, pValue);
            Consumer<Vector<E>> undo = m -> m.set(pIndex, originalValue);
            return new ImmutableVectorVersion<>(this, redo, undo);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<E> stream() {
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            Vector<E> copy = (Vector<E>) master.clone();
            return copy.stream();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector<E> toVector() {
        synchronized (getSynchronisationObject()) {
            Vector<E> master = getMaster();
            return (Vector<E>) master.clone();
        }
    }

    @Override
    public int size() {
        synchronized (getSynchronisationObject()) {
            return getMaster().size();
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream pInputStream) throws ClassNotFoundException, IOException {
        var master = (Vector<E>) pInputStream.readObject();
        setMasterAndToMaster(master, null);
    }

    private void writeObject(ObjectOutputStream pOutputStream) throws IOException {
        pOutputStream.writeObject(getMaster());
    }

}
