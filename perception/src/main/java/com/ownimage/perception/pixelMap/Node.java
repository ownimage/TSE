/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Vector;
import java.util.stream.Stream;

// TODO: Auto-generated Javadoc

/**
 * The class Node is where two or more PixelChains can meet.
 */
public class Node extends Pixel {


    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

//    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
//    private static PixelMapService pixelMapService = context.getBean(PixelMapService.class);
//    private static PixelChainService pixelChainService = context.getBean(PixelChainService.class);

    /**
     * The m pixel chains.
     */
    private final Vector<PixelChain> mPixelChains = new Vector<>();

    /**
     * Instantiates a new node.
     *
     * @param pIntegerPoint the pixel
     */
    public Node(IntegerPoint pIntegerPoint) {
        super(pIntegerPoint);
    }

    public Node(int pX, int pY) {
        super(pX, pY);
    }

    /**
     * Adds the pixel chain.
     *
     * @param pPixelChain the pixel chain
     */
    public Node addPixelChain(PixelChain pPixelChain) {
        if (mPixelChains.contains(pPixelChain)) {
            return this;
        }

        Node clone = copy();
        clone.mPixelChains.add(pPixelChain);
        return clone;
    }

    public Stream<PixelChain> streamPixelChains() {
        return mPixelChains.stream();
    }

    /**
     * Count pixel chains.
     *
     * @return the int
     */
    public int countPixelChains() {
        return mPixelChains.size();
    }

    /**
     * Gets the pixel chain.
     *
     * @param pN the p n
     * @return the pixel chain
     */
    public PixelChain getPixelChain(int pN) {
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
    public Node removePixelChain(PixelChain pPixelChain) {
        Node clone = copy();
        clone.mPixelChains.remove(pPixelChain);
        return clone;
    }

    public boolean containsPixelChain(PixelChain pPixelChain) {
        return mPixelChains.contains(pPixelChain);
    }

    private Node copy() {
        Node clone = new Node(this);
        clone.mPixelChains.addAll(mPixelChains);
        return clone;
    }

    @Override
    public String toString() {
        return "Node(" + getX() + ", " + getY() + ")";
    }
}
