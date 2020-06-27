package com.ownimage.framework.util.immutable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ImmutableVectorClone<E> implements IImmutableVector<E>, Serializable {

    private final static long serialVersionUID = -1945581369389290425L;
    
    private Vector<E> mVector = new Vector(); 

    public ImmutableVectorClone<E> clone() {
            var clone = new ImmutableVectorClone<E>();
            clone.mVector = (Vector<E>) mVector.clone();
            return clone;
    }
    
    @Override
    public ImmutableVectorClone<E> add(E pElement) {
        var clone = clone();
        clone.mVector.add(pElement);
        return clone;
    }

    @Override
    public ImmutableVectorClone<E> add(int pIndex, E pElement) {
        var clone = clone();
        clone.mVector.add(pIndex, pElement);
        return clone;
    }

    @Override
    public ImmutableVectorClone<E> addAll(Collection<E> pAll) {
        var clone = clone();
        clone.mVector.addAll(pAll);
        return clone;
    }

    @Override
    public ImmutableVectorClone<E> addAll(IImmutableVector<E> pAll) {
        var clone = clone();
        clone.mVector.addAll(pAll.toVector());
        return clone;
    }

    @Override
    public ImmutableVectorClone<E> clear() {
        return new ImmutableVectorClone<>();
    }

    @Override
    public boolean contains(E pElement) {
        return mVector.contains(pElement);
    }

    @Override
    public boolean containsAll(Collection<E> pElements) {
        return  mVector.containsAll(pElements);
    }

    @Override
    public E get(int pIndex) {
        return mVector.get(pIndex);
    }

    @Override
    public ImmutableVectorClone remove(E pElement) {
        var clone = clone();
        clone.mVector.remove(pElement);
        return clone;
    }

    @Override
    public ImmutableVectorClone remove(int pIndex) {
        if (pIndex < 0 || pIndex >= mVector.size()) {
            throw new IllegalArgumentException();
        }
        var clone = clone();
        clone.mVector.remove(pIndex);
        return clone;
    }

    @Override
    public Optional<E> firstElement() {
        if (mVector.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mVector.firstElement());
    }

    @Override
    public Optional<E> lastElement() {
        if (mVector.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mVector.lastElement());
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
    public ImmutableVectorClone removeAll(Collection<E> pAll) {
        var clone = clone();
        clone.mVector.removeAll(pAll);
        return clone;
    }

    @Override
    public void forEach(Consumer<E> pFn) {
        mVector.forEach(pFn);
    }

    @Override
    public ImmutableVectorClone set(int pIndex, E pValue) {
        if (mVector.get(pIndex) == null && pValue == null
                || ( pValue != null && pValue.equals(mVector.get(pIndex)))) {
            return this;
        }
        var clone = clone();
        clone.mVector.set(pIndex, pValue);
        return clone;
    }

    @Override
    public Stream<E> stream() {
        return mVector.stream();
    }

    @Override
    public Vector<E> toVector() {
        return new Vector<>(mVector);
    }

    @Override
    public int size() {
        return mVector.size();
    }


}
