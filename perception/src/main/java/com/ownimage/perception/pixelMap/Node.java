/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.VertexService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Vector;

// TODO: Auto-generated Javadoc

/**
 * The class Node is where two or more PixelChains can meet.
 */
public class Node extends Pixel {


    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private static PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private static PixelChainService pixelChainService = context.getBean(PixelChainService.class);

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

    Node(int pX, int pY) {
        super(pX, pY);
    }

    /**
     * Adds the pixel chain.
     *
     * @param pPixelChain the pixel chain
     */
    public Node addPixelChain(PixelChain pPixelChain) {
        Node clone = copy();
        clone.mPixelChains.add(pPixelChain);
        return clone;
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
    private PixelChain getPixelChain(int pN) {
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

    private Node copy() {
        Node clone = new Node(this);
        clone.mPixelChains.addAll(mPixelChains);
        return clone;
    }

    public ImmutablePixelMapData mergePixelChains(ImmutablePixelMapData pixelMap) {
        var result = StrongReference.of(pixelMap);
        int count = countPixelChains();
        mLogger.info(() -> String.format("Node::mergePixelChains Node=%s, count=%s", this, count));
        switch (count) {
            case 2:
                PixelChain chain0 = getPixelChain(0);
                PixelChain chain1 = getPixelChain(1);
                if (chain0 != chain1) {// this is to prevent trying to merge a simple loop with itself
                    PixelChain merged = pixelChainService.merge(result.get(), chain0, chain1, this);
                    result.update(r -> pixelMapService.removePixelChain(r, chain0));
                    result.update(r -> pixelMapService.removePixelChain(r, chain1));
                    result.update(r -> pixelMapService.addPixelChain(r, merged));
                }
                break;
            case 3:
                break;
            case 4:
                break;
        }
        return result.get();
    }

    @Override
    public String toString() {
        return "Node(" + getX() + ", " + getY() + ")";
    }
}
