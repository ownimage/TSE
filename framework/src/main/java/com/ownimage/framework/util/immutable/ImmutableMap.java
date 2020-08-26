package com.ownimage.framework.util.immutable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ImmutableMap<K, V> extends ImmutableNode<HashMap<K, V>> {

    public ImmutableMap(Map<K, V> map) {
        super(new HashMap<>(map));
    }

    public ImmutableMap() {
        super(new HashMap<>());
    }

    private ImmutableMap(ImmutableMap<K, V> pPrevious, Consumer<HashMap<K, V>> pRedo, Consumer<HashMap<K, V>> pUndo) {
        super(pPrevious, pRedo, pUndo);
    }

    public ImmutableMap<K, V> clear() {
        return new ImmutableMap();
    }

    public ImmutableMap<K, V> put(K pKey, V pValue) {
        synchronized (getSynchronisationObject()) {
            var master = getMaster();
            var currentValue = master.get(pKey);
            Consumer<HashMap<K, V>> redo = m -> m.put(pKey, pValue);
            Consumer<HashMap<K, V>> undo = master.containsKey(pKey)
                    ? m -> m.put(pKey, currentValue)
                    : m -> m.remove(pKey);
            return new ImmutableMap<K, V>(this, redo, undo);
        }
    }

    public V get(K pKey) {
        synchronized (getSynchronisationObject()) {
            var master = getMaster();
            return master.get(pKey);
        }
    }

    public Optional<V> getOptional(K pKey) {
        synchronized (getSynchronisationObject()) {
            var master = getMaster();
            return Optional.ofNullable(master.get(pKey));
        }
    }

    public ImmutableMap<K, V> remove(K pKey) {
        synchronized (getSynchronisationObject()) {
            var master = getMaster();
            if (!master.containsKey(pKey)) {
                return this;
            }
            var currentValue = master.get(pKey);
            Consumer<HashMap<K, V>> redo = m -> m.remove(pKey);
            Consumer<HashMap<K, V>> undo = master.containsKey(pKey)
                    ? m -> m.put(pKey, currentValue)
                    : m -> {
            };
            return new ImmutableMap<K, V>(this, redo, undo);
        }
    }

    public int size() {
        synchronized (getSynchronisationObject()) {
            return getMaster().size();
        }
    }

    public HashMap<K, V> toHashMap() {
        synchronized (getSynchronisationObject()) {
            return new HashMap<K, V>(getMaster());
        }
    }

    public Collection<V> values() {
        synchronized (getSynchronisationObject()) {
            return getMaster().values();
        }
    }

    public ImmutableMap<K, V> update(K key, BiFunction<K, V, V> fn) {
        synchronized (getSynchronisationObject()) {
            var master = getMaster();
            var currentValue = master.get(key);
            var newValue = fn.apply(key, currentValue);
            return put(key, newValue);
        }
    }

}
