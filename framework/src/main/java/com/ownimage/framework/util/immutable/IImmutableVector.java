package com.ownimage.framework.util.immutable;

import java.util.Collection;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface IImmutableVector<E> {

    IImmutableVector<E> add(E pElement);

    IImmutableVector<E> add(int pIndex, E pElement);

    IImmutableVector<E> addAll(Collection<E> pAll);

    IImmutableVector<E> addAll(IImmutableVector<E> pAll);

    IImmutableVector<E> clear();

    boolean contains(E pElement);

    boolean containsAll(Collection<E> pElements);

    E get(int pIndex);

    IImmutableVector remove(E pElement);

    IImmutableVector remove(int pIndex);

    Optional<E> firstElement();

    int indexOf(E e);

    Optional<E> lastElement();

    IImmutableVector removeAll(Collection<E> pAll);

    void forEach(Consumer<E> pFn);

    IImmutableVector set(int pIndex, E pValue);

    Stream<E> stream();

    Vector<E> toVector();

    int size();
}
