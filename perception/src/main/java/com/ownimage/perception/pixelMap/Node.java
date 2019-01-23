/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;

import java.util.Vector;

// TODO: Auto-generated Javadoc

/**
 * The class Node is where two or more PixelChains can meet.
 */
public class Node extends Pixel {


    /**
     * The Constant serialVersionUID.
     */
    public static final long serialVersionUID = 1L;

    /**
     * The m pixel chains.
     */
    private Vector<PixelChain> mPixelChains = new Vector<>();

    /**
     * Instantiates a new node.
     *
     * @param pIntegerPoint the pixel
     */
    Node(final IntegerPoint pIntegerPoint) {
        super(pIntegerPoint);
    }

    /**
     * Adds the pixel chain.
     *
     * @param pPixelChain the pixel chain
     */
    void addPixelChain(final PixelChain pPixelChain) {
        mPixelChains.add(pPixelChain);
    }

    /**
     * Count pixel chains.
     *
     * @return the int
     */
    private int countPixelChains() {
        return mPixelChains.size();
    }

    /**
     * Gets the pixel chain.
     *
     * @param pN the p n
     * @return the pixel chain
     */
    private PixelChain getPixelChain(final int pN) {
        if (pN > countPixelChains()) {
            throw new IllegalArgumentException("Cannot return item: " + pN + ". There are only " +
                                                       countPixelChains() + " chains.");
        }

        return mPixelChains.get(pN);
    }
    //
    // public List<PixelChain> getPixelChains() {
    // return mPixelChains;
    // }

    /**
     * Removes the pixel chain.
     *
     * @param pPixelChain the pixel chain
     */
    void removePixelChain(final PixelChain pPixelChain) {
        mLogger.info(() -> String.format("Node::removePixelChain count=%s", mPixelChains.size()));
        mPixelChains = new Vector<>(mPixelChains); // TODO better immutable or clone
        mPixelChains.remove(pPixelChain);
        mLogger.info(() -> String.format("Node::removePixelChain count=%s", mPixelChains.size()));
    }

    void mergePixelChains(final PixelMap pPixelMap) {
        int count = countPixelChains();
        mLogger.info(() -> String.format("Node::mergePixelChains Node=%s, count=%s", this, count));
        switch (count) {
            case 2:
                final PixelChain chain0 = getPixelChain(0);
                final PixelChain chain1 = getPixelChain(1);
                if (chain0 != chain1) {// this is to prevent trying to merge a simple loop with itself
                    chain0.merge(pPixelMap, chain1, this);
                }
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }

    @Override
    public String toString() {
        return "Node(" + getX() + ", " + getY() + ")";
    }
}
