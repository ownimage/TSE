/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.util;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;
import java.util.logging.Logger;


public class ImmutableLayerMap2D<T> {

    public static final Logger mLogger = Framework.getLogger();
    private static final int DENSITY = 1000;

    private final int mWidth;
    private final int mHeight;
    private final T mDefaultValue;
    private final Vector<WeakReference<Map2DLayer>> mAllLayers = new Vector<>();

    private class Map2DLayer {

        private HashMap<Long, T> mChanges = new HashMap<>(mWidth * mHeight / DENSITY);
        private Map2DLayer mLowerLayer;
        private WeakReference<Object> mOwner;
        private final Collection<WeakReference<Map2DLayer>> mHigherLayers = new Vector<>();

        private Map2DLayer(Map2DLayer pDown) {
            mLowerLayer = pDown;
            mAllLayers.add(new WeakReference<>(this));
        }

        private T get(Long pKey) {
            synchronized (Map2DLayer.this) {
                T layerValue = mChanges.get(pKey);
                if (layerValue != null) return layerValue;
                if (mLowerLayer != null) return mLowerLayer.get(pKey);
                return mDefaultValue;
            }
        }

        private ImmutableMap2D set(Long key, T pValue) {
            synchronized (Map2DLayer.this) {
                T existingValue = get(key);
                if (Objects.equals(existingValue, pValue)) return getImmutable();
                Map2DLayer layer = new Map2DLayer(this);
                ImmutableMap2D version = new ImmutableMap2D(layer);
                layer.setOwner(version);
                layer.mChanges.put(key, pValue);
                addHigherLayer(layer);
                return version;
            }
        }

        private Map2D getMap2D() {
            Map2DLayer layer = new Map2DLayer(this);
            Map2D map = new Map2D(layer);
            layer.setOwner(map);
            return map;
        }

        private void modify(Long key, T pValue) {
            synchronized (Map2DLayer.this) {
                T lowerValue = mLowerLayer == null ? mDefaultValue : mLowerLayer.get(key);
                if (Objects.equals(pValue, lowerValue))
                    mChanges.remove(key);
                else mChanges.put(key, pValue);
            }
        }

        /**
         * Guaranteed to return a reference to the version.  This might regenerate the reference if it has been garbage collected in the background.
         *
         * @return the version related to this layer.
         */
        public ImmutableMap2D getImmutable() {
            ImmutableMap2D version = (ImmutableMap2D) mOwner.get();
            if (version != null) return version;
            version = new ImmutableMap2D(this);
            mOwner = new WeakReference<>(version);
            return version;
        }

        private void addHigherLayer(Map2DLayer pLayer) {
            if (pLayer == null) return;
            mHigherLayers.add(new WeakReference<>(pLayer));
        }

        private void removeHigherLayer(Map2DLayer pLayer) {
            mHigherLayers.removeIf(l -> l.get() == pLayer);
        }

        int layerSize() {
            return mChanges.size();
        }

        int layerCount() {
            synchronized (Map2DLayer.this) {
                return layerCount(1);
            }
        }

        private int layerCount(int pCount) {
            if (mLowerLayer == null) return pCount;
            return mLowerLayer.layerCount(pCount + 1);
        }

        private void setOwner(Object pOwner) {
            mOwner = new WeakReference<>(pOwner);
        }

        private boolean canMergeUp() {
            pruneHigherLayers();
            return mHigherLayers.size() == 1 && mOwner.get() == null;
        }

        /**
         * Removes expired references to higher up layers.
         */
        private void pruneHigherLayers() {
            mHigherLayers.removeIf(l -> l.get() == null);
        }

        private void compress(boolean pCompressAll) {
            // TODO what option to choose when there is a split above
            // option 1 do not compress ... means longer access time
            // option 2 compress up ... means more space
            // currently I'm going to choose option 1 .. this is defined in the canMergeUp
            if (mLowerLayer != null && mLowerLayer.canMergeUp()) {
                mChanges = merge(mChanges, mLowerLayer.mChanges, mLowerLayer.mLowerLayer);
                if (mLowerLayer.mLowerLayer != null) {
                    mLowerLayer.mLowerLayer.removeHigherLayer(mLowerLayer);
                    mLowerLayer.mLowerLayer.addHigherLayer(this);
                }
                mLowerLayer = mLowerLayer.mLowerLayer;
                compress(pCompressAll);
            }

            if (mLowerLayer != null) {
                mLowerLayer.compress(pCompressAll);
            }

            if (pCompressAll) compressAll();
        }


    }

    public class ImmutableMap2D {

        final Map2DLayer mLayer;

        private ImmutableMap2D(Map2DLayer pLayer) {
            mLayer = pLayer;
        }

        public T get(int pX, int pY) {
            checkXY(pX, pY);
            Long key = generateKey(pX, pY);
            mLayer.compress(true);
            return mLayer.get(key);
        }

        public ImmutableMap2D set(int pX, int pY, T pValue) {
            checkXY(pX, pY);
            Long key = generateKey(pX, pY);
            mLayer.compress(true);
            return mLayer.set(key, pValue);
        }

        int getLayerCount() {
            return mLayer.layerCount();
        }

        int getAllLayerCount() {
            return ImmutableLayerMap2D.this.getAllLayerCount();
        }

        int layerSize() {
            return mLayer.layerSize();
        }

        public Map2D getMutable() {
            mLayer.compress(true);
            return mLayer.getMap2D();
        }
    }

    public class Map2D {
        Map2DLayer mLayer;

        private Map2D(Map2DLayer pLayer) {
            mLayer = pLayer;
        }

        public T get(int pX, int pY) {
            checkXY(pX, pY);
            Long key = generateKey(pX, pY);
            return mLayer.get(key);
        }

        public Map2D set(int pX, int pY, T pValue) {
            checkXY(pX, pY);
            Long key = generateKey(pX, pY);
            mLayer.modify(key, pValue);
            return this;
        }

        public ImmutableMap2D getImmutable() {
            Map2DLayer existingLayer = mLayer;

            Map2DLayer upperLayer = new Map2DLayer(mLayer);
            upperLayer.setOwner(this);
            mLayer = upperLayer;

            ImmutableMap2D immutable = new ImmutableMap2D(existingLayer);
            existingLayer.setOwner(immutable);

            return immutable;
        }

        int layerCount() {
            return mLayer.layerCount();
        }

        int layerSize() {
            return mLayer.layerSize();
        }


    }

    public ImmutableLayerMap2D(int pX, int pY, T pDefaultValue) {
        Framework.checkParameterGreaterThan(mLogger, pX, 0, "pX");
        Framework.checkParameterGreaterThan(mLogger, pY, 0, "pY");

        mWidth = pX;
        mHeight = pY;
        mDefaultValue = pDefaultValue;
    }

    public ImmutableMap2D getImmutable() {
        Map2DLayer layer = new Map2DLayer(null);
        ImmutableMap2D immutable = new ImmutableMap2D(layer);
        layer.setOwner(immutable);
        return immutable;
    }

    public Map2D getMutable() {
        Map2DLayer layer = new Map2DLayer(null);
        Map2D map = new Map2D(layer);
        layer.setOwner(map);
        return map;
    }

    private void checkXY(int pX, int pY) {
        Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
        Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
        Framework.checkParameterLessThan(mLogger, pX, mWidth, "pX");
        Framework.checkParameterLessThan(mLogger, pY, mHeight, "pY");
    }

    private HashMap<Long, T> merge(HashMap<Long, T> pChanges, HashMap<Long, T> pLowerChanges, Map2DLayer pBeyondLower) {
        return (pChanges.size() >= pLowerChanges.size())
                ? mergeUp(pChanges, pLowerChanges, pBeyondLower)
                : mergeDown(pChanges, pLowerChanges, pBeyondLower);
    }

    /**
     * This merges the changes from the lower set of changes into the upper set of changes.
     * This deliberately has side effects on the supplied pChanges for efficiency reasons.
     *
     * @param pChanges      the changes in the upper layer
     * @param pLowerChanges the changes in the lower layer
     * @param pBeyondLower  the layer below which is needed to check for redundant changes
     * @return the pChanges object
     */
    private HashMap<Long, T> mergeUp(HashMap<Long, T> pChanges, HashMap<Long, T> pLowerChanges, Map2DLayer pBeyondLower) {
        mLogger.finest("MERGE UP");
        pLowerChanges.forEach((k, v) -> {
            T layerValue = pChanges.get(k);
            T beyondLowerValue = pBeyondLower == null ? mDefaultValue : pBeyondLower.get(k);
            if (Objects.equals(layerValue, beyondLowerValue)) pChanges.remove(k);
            else pChanges.putIfAbsent(k, v);

        });
        return pChanges;
    }

    /**
     * This merges the changes from the set of changes into the lower set of changes.
     * This deliberately has side effects on the supplied pLowerChanges for efficiency reasons.
     *
     * @param pChanges      the changes in the upper layer
     * @param pLowerChanges the changes in the lower layer
     * @param pBeyondLower  the layer below which is needed to check for redundant changes
     * @return the pLowerChanges object
     */
    private HashMap<Long, T> mergeDown(HashMap<Long, T> pChanges, HashMap<Long, T> pLowerChanges, Map2DLayer pBeyondLower) {
        mLogger.finest("MERGE DOWN");
        pChanges.forEach((k, v) -> {
            T lowerValue = pLowerChanges.get(k);
            T beyondLowerValue = pBeyondLower == null ? mDefaultValue : pBeyondLower.get(k);
            if (!Objects.equals(v, beyondLowerValue)) pLowerChanges.put(k, v);
            else if (lowerValue != null) pLowerChanges.remove(k);
        });
        return pLowerChanges;
    }

    private int getAllLayerCount() {
        return mAllLayers.size();
    }

    private void compressAll() {
        mAllLayers.removeIf(weak -> {
            Map2DLayer layer = weak.get();
            return layer == null || layer.mOwner.get() == null;
        });
        mAllLayers.forEach(weak -> {
            Map2DLayer layer = weak.get();
            if (layer != null) layer.compress(false);
        });
    }

    private Long generateKey(int pX, int pY) {
        return (long) pX * mWidth + pY;
    }
}

