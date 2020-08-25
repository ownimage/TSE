package com.ownimage.framework.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Allows for the counting and collection of values to assist in management of the platform.
 */
public class PegCounterService {

    private HashMap<Object, Long> mMap = new HashMap<>();

    public synchronized void increase(final Object pPeg) {
        mMap.compute(pPeg, (k, v) -> get(k) + 1);
    }

    public synchronized long get(final Object pPeg) {
        return mMap.getOrDefault(pPeg, 0L);
    }

    public synchronized String getString(final Object pPeg) {
        return String.format("%s: %s", pPeg, get(pPeg));
    }

    public synchronized String getString(final Object... pPeg) {
        return Arrays.stream(pPeg)
                .map(this::getString)
                .collect(Collectors.joining("\n"));
    }

    public synchronized void clear(final Object pPeg) {
        mMap.remove(pPeg);
    }

    public synchronized void clear(final Object... pPegs) {
        for (var peg : pPegs) mMap.remove(peg);
    }
}
