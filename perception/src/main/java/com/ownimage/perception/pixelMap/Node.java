/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import java.util.Vector;

import com.ownimage.framework.math.IntegerPoint;

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
    private final Vector<PixelChain> mPixelChains = new Vector<>();

    /**
     * Instantiates a new node.
     *
     * @param pPixel the pixel
     */
    Node(final Pixel pPixel) {
        super(pPixel);
    }

    /**
     * Instantiates a new node.
     *
     * @param pPixelMap the pixel map
     * @param pX        the p x
     * @param pY        the p y
     */
    public Node(final PixelMap pPixelMap, final int pX, final int pY) {
        super(pPixelMap, pX, pY);
    }

    /**
     * Instantiates a new node.
     *
     * @param pPixelMap the pixel map
     * @param pPoint        the position on the map
     */
    public Node(final PixelMap pPixelMap, final IntegerPoint pPoint) {
        super(pPixelMap, pPoint.getX(), pPoint.getY());
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
        mPixelChains.remove(pPixelChain);
    }

    void mergePixelChains() {
        switch (countPixelChains()) {
            case 2:
                final PixelChain chain0 = getPixelChain(0);
                final PixelChain chain1 = getPixelChain(1);
                if (chain0 != chain1) {// this is to prevent trying to merge a simple loop with itself
                    chain0.merge(getPixelMap(), chain1, this);
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
