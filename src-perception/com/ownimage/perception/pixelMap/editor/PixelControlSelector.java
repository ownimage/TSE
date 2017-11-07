/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap.editor;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import com.ownimage.perception.math.KMath;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.util.Version;
import com.ownimage.perception.util.logging.PerceptionLogger;

public class PixelControlSelector extends PictureControlSelector {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = PixelControlSelector.class.getName();
	private final static Logger mLogger = Logger.getLogger(mClassname);

	private Pixel mDragStart;
	private Pixel mDragLast;
	private int mMouseX;
	private int mMouseY;

	public PixelControlSelector(final EditPixelMapControlSelectorBase pParent) {
		super(pParent);
	}

	public PixelControlSelector(final EditPixelMapDialog pEditPixelMapDialog) {
		super(pEditPixelMapDialog);
	}

	@Override
	public void delta(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
		System.out.println("delta pDeltaX: " + pDeltaX + "pDeltaY: " + pDeltaY + " pIsMutating:" + pIsMutating);
		final int size = getEditPixelMapDialog().getPreviewPicture().getSize();
		final int zoom = getEditPixelMapDialog().getZoom();
		System.out.println("deltaStart Pixel Selected x=" + (int) (pDeltaX * size / zoom) + " y=" + (int) (pDeltaY * size / zoom));
		mMouseX = (int) (mDragStart.getX() + pDeltaX * size / zoom);
		mMouseY = (int) (mDragStart.getY() + pDeltaY * size / zoom);
		final Pixel pixel = getPixelMap().getPixelAt(mMouseX, mMouseY);

		if (!pixel.equals(mDragLast)) {

			final int deltaX = pixel.getX() - mDragLast.getX();
			final int deltaY = pixel.getY() - mDragLast.getY();
			final int incX = KMath.signum(deltaX);
			final int incY = KMath.signum(deltaY);

			final boolean xbased = Math.abs(deltaX) >= Math.abs(deltaY);

			if (xbased) {
				for (int interpX = mDragLast.getX() + incX; Math.abs(interpX - mDragLast.getX()) <= Math.abs(deltaX); interpX += incX) {
					final int interpY = (int) ((double) mDragLast.getY() + (mDragLast.getX() - interpX) * deltaY / deltaX);
					final Pixel interpPixel = getPixelMap().getPixelAt(interpX, interpY);
					getPixelMap().pixelAction(interpPixel, getEditPixelMapDialog());
				}

			} else { // ybased
				for (int interpY = mDragLast.getY() + incY; Math.abs(interpY - mDragLast.getY()) <= Math.abs(deltaY); interpY += incY) {
					final int interpX = (int) ((double) mDragLast.getX() + (mDragLast.getY() - interpY) * deltaX / deltaY);
					final Pixel interpPixel = getPixelMap().getPixelAt(interpX, interpY);
					getPixelMap().pixelAction(interpPixel, getEditPixelMapDialog());
				}

			}

			// draw new cursor
			getEditPixelMapDialog().grafitiCursor();

		}
		mDragLast = pixel;
		// getPixelMap().pixelAction(pixel, getEditPixelMapDialog().getPixelEditMode());
	}

	@Override
	public void deltaStart(final double pX, final double pY, final Modifier pModifier) {
		System.out.println("deltaStart Pixel Selected x=" + pX + " y=" + pY);
		mDragStart = getPixel(pX, pY);
		mDragLast = mDragStart;
		getPixelMap().pixelAction(mDragStart, getEditPixelMapDialog());
		System.out.println("deltaStart Pixel Selected x=" + mDragStart.getX() + " y=" + mDragStart.getY());
	}

	@Override
	public String getDisplayName() {
		mLogger.entering(mClassname, "getDisplayName");
		final String displayName = "Pixel";
		mLogger.exiting(mClassname, "getDisplayName", displayName);
		return displayName;
	}

	@Override
	public void graffiti(final EPMDGraphicsHelper pGraphics) {
		super.graffiti(pGraphics);
		try {
			final Pixel p = getPixelMap().getPixelAt(mMouseX, mMouseY);
			pGraphics.drawLine(p.getUHVWPoint(), Point.Point11, Color.RED);
		} catch (final Throwable pT) {
		}
	}

	@Override
	public void keyPressed(final int pKeyCode, final Modifier pModifier) {
		mLogger.entering(mClassname, "keyPressed");

		try {
			PerceptionLogger.logParams(mLogger, "pKeyCode, pModifier", pKeyCode, pModifier);

			if (pKeyCode == KeyEvent.VK_Z && pModifier.isControlDown()) {
				mLogger.severe("UNDO");
				actionUndo();
			}

			if (pKeyCode == KeyEvent.VK_Y && pModifier.isControlDown()) {
				mLogger.severe("REDO");
				actionRedo();
			}
		} catch (final Throwable pT) {
			mLogger.severe(PerceptionLogger.stackTraceToString(pT));
		}
	}

	@Override
	public void keyReleased(final int pKeyCode, final Modifier pModifier) {
		mLogger.entering(mClassname, "keyReleased");
		System.out.println("Pixel key released");

		try {
			PerceptionLogger.logParams(mLogger, "pKeyCode, pModifier", pKeyCode, pModifier);

			if (pKeyCode == KeyEvent.VK_ALT) {
				revertControlSelector();
				return;
			}
		} catch (final Throwable pT) {
			mLogger.severe(PerceptionLogger.throwableToString(pT));
		}

		mLogger.exiting(mClassname, "keyReleased");
	}

	@Override
	public boolean setSelected(final double pX, final double pY, final Modifier pModifier) {
		mLogger.entering(mClassname, "setSelected");
		PerceptionLogger.logParams(mLogger, "pX, pY", pX, pY);
		mLogger.fine("setSelected");

		System.out.println("Pixel Selected x=" + pX + " y=" + pY);
		final Pixel pixel = getPixel(pX, pY);
		System.out.println("Pixel Selected x=" + pixel.getX() + " y=" + pixel.getY());

		getPixelMap().pixelAction(pixel, getEditPixelMapDialog());
		graffiti();

		mLogger.exiting(mClassname, "setSelected");
		return true;
	}

}
