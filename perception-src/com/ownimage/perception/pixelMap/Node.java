/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.util.Version;

// TODO: Auto-generated Javadoc
/**
 * The class Node is where two or more PixelChains can meet.
 */
public class Node extends Pixel {

	/** The Constant mVersion. */
	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	/** The Constant mClassname. */
	public final static String mClassname = Node.class.getName();

	/** The Constant mLogger. */
	public final static Logger mLogger = Logger.getLogger(mClassname);

	/** The Constant serialVersionUID. */
	public static final long serialVersionUID = 1L;

	/** The m pixel chains. */
	private final Vector<PixelChain> mPixelChains = new Vector<PixelChain>();

	/**
	 * Instantiates a new node.
	 * 
	 * @param pPixel
	 *            the pixel
	 */
	public Node(final Pixel pPixel) {
		super(pPixel);
	}

	/**
	 * Instantiates a new node.
	 * 
	 * @param pPixelMap
	 *            the pixel map
	 * @param pX
	 *            the p x
	 * @param pY
	 *            the p y
	 */
	public Node(final PixelMap pPixelMap, final int pX, final int pY) {
		super(pPixelMap, pX, pY);
	}

	/**
	 * Adds the pixel chain.
	 * 
	 * @param pPixelChain
	 *            the pixel chain
	 */
	public void addPixelChain(final PixelChain pPixelChain) {
		mPixelChains.add(pPixelChain);
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
	 * @param pN
	 *            the p n
	 * @return the pixel chain
	 */
	public PixelChain getPixelChain(final int pN) {
		if (pN > countPixelChains()) { throw new IllegalArgumentException("Cannot return item: " + pN + ".  There are only " + countPixelChains() + " chains."); }

		return mPixelChains.get(pN);
	}

	public List<PixelChain> getPixelChains() {
		return mPixelChains;
	}

	/**
	 * Removes the pixel chain.
	 * 
	 * @param pPixelChain
	 *            the pixel chain
	 */
	public void removePixelChain(final PixelChain pPixelChain) {
		mPixelChains.remove(pPixelChain);
	}

	public void setPixelChainsVisited(final boolean pValue) {
		for (PixelChain pixelChain : mPixelChains) {
			pixelChain.setVisited(pValue);
		}
	}

	@Override
	public String toString() {
		return "Node(" + getX() + ", " + getY() + ")";
	}
}
