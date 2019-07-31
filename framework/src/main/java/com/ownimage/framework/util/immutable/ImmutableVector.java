package com.ownimage.framework.util.immutable;

import java.util.Collection;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface ImmutableVector<E> {
    ImmutableVector<E> add(E pElement);

    ImmutableVector<E> add(int pIndex, E pElement);

    ImmutableVector addAll(Collection<E> pAll);

    ImmutableVector clear();

    boolean contains(E pElement);

    boolean containsAll(Collection<E> pElements);

    E get(int pIndex);

    ImmutableVector remove(E pElement);

    ImmutableVector remove(int pIndex);

    Optional<E> firstElement();

    Optional<E> lastElement();

    ImmutableVector removeAll(Collection<E> pAll);

    void forEach(Consumer<E> pFn);

    ImmutableVector set(int pIndex, E pValue);

    Stream<E> stream();

    Vector<E> toVector();

    int size();
}
