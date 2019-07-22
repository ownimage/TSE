package com.ownimage.framework.util;

import java.util.HashMap;

/**
 * Allows for the counting and collection of values to assist in management of the platform.
 */
public class PegCounter {

    private HashMap<Object, Long> mMap = new HashMap<>();

    public void increase(final Object pPeg) {
        mMap.compute(pPeg, (k, v) -> get(k) + 1);
    }

    public long get(final Object pPeg) {
        return mMap.getOrDefault(pPeg, 0L);
    }

    public void clear(final Object pPeg) {
        mMap.remove(pPeg);
    }
}
