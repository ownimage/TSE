/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */

package com.ownimage.perception.pixelMap.editor;

import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.transform.cannyEdge.PreviewPosition;
import com.ownimage.perception.util.Version;
import com.ownimage.perception.util.logging.PerceptionLogger;

public class PictureControlSelector extends EditPixelMapControlSelectorBase {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = PictureControlSelector.class.getName();
	private final static Logger mLogger = Logger.getLogger(mClassname);

	public PictureControlSelector(final EditPixelMapDialog pEditPixelMapDialog) {
		super(pEditPixelMapDialog);
	}

	public PictureControlSelector(EditPixelMapControlSelectorBase pParent) {
		super(pParent);
	}

	protected void actionAddVertex(final double pX, final double pY) {
		mLogger.entering(mClassname, "actionAddVertex");
		PerceptionLogger.logParams(mLogger, "pX, pY", pX, pY);

		final AddPixelChainControlSelector apcs = new AddPixelChainControlSelector(this, pX, pY);

		changeControlSelector(apcs);

		mLogger.exiting(mClassname, "actionAddVertex");
	}

	protected void actionMovePicture(final double pDeltaX, final double pDeltaY, final boolean pIsMutating) {
		mLogger.entering(mClassname, "actionMovePicture");
		PerceptionLogger.logParams(mLogger, "pX, pY", pDeltaX, pDeltaY);

		double dx = -pDeltaX * getWidth();
		double dy = -pDeltaY * getHeight();
		PreviewPosition position = getEditPixelMapDialog().getPreviewPosition();
		position.delta(dx, dy, pIsMutating);

		mLogger.exiting(mClassname, "actionMovePicture");
	}

	@Override
	public void deltaStart(double pX, double pY, Modifier pModifier) {
		getEditPixelMapDialog().getPreviewPosition().saveNormalizedValue();
	}

	protected void actionSelectVertex(final double pX, final double pY) {
		mLogger.entering(mClassname, "actionSelectVertex");
		final Pixel pixel = getPixel(pX, pY);
		sortVisibleVertexes(pixel);

		final VertexControlSelector vcs = new VertexControlSelector(this);
		changeControlSelector(vcs);

		mLogger.exiting(mClassname, "actionSelectVertex");
	}

	protected void actionZoomIn() {
		mLogger.entering(mClassname, "actionZoomIn");
		getEditPixelMapDialog().zoomIn();
		mLogger.exiting(mClassname, "actionZoomOut");
	}

	protected void actionZoomOut() {
		mLogger.entering(mClassname, "actionZoomOut");
		getEditPixelMapDialog().zoomOut();
		mLogger.exiting(mClassname, "actionZoomOut");
	}

	@Override
	public void delta(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
		System.out.println("delta pDeltaX: " + pDeltaX + "pDeltaY: " + pDeltaY + " pIsMutating:" + pIsMutating);
		mLogger.entering(mClassname, "delta");
		PerceptionLogger.logParams(mLogger, "pDeltaX, pDeltaY, pIsMutating", pDeltaX, pDeltaY, pIsMutating);

		actionMovePicture(pDeltaX, pDeltaY, pIsMutating);

		mLogger.exiting(mClassname, "delta");
	}

	@Override
	public String getDisplayName() {
		mLogger.entering(mClassname, "getDisplayName");
		final String displayName = "Picture";
		mLogger.exiting(mClassname, "getDisplayName", displayName);
		return displayName;
	}

	@Override
	public void next(final Modifier pModifier) {
		mLogger.entering(mClassname, "next");

		actionZoomIn();

		mLogger.exiting(mClassname, "next");
	}

	@Override
	public void previous(final Modifier pModifier) {
		mLogger.entering(mClassname, "previous");

		actionZoomOut();

		mLogger.exiting(mClassname, "previous");
	}

	@Override
	public boolean setSelected(final double pX, final double pY, final Modifier pModifier) {
		mLogger.entering(mClassname, "setSelected");
		PerceptionLogger.logParams(mLogger, "pX, pY", pX, pY);
		mLogger.fine("setSelected");

		if (pModifier.isNormal()) {
			actionSelectVertex(pX, pY);
		}

		if (pModifier.isShiftDown()) {
			actionAddVertex(pX, pY);
		}

		mLogger.exiting(mClassname, "setSelected");
		return true;
	}

	@Override
	public void keyReleased(final int pKeyCode, final Modifier pModifier) {
		mLogger.entering(mClassname, "keyTyped");
		System.out.println("Picture key released");

		try {
			PerceptionLogger.logParams(mLogger, "pKeyCode, pModifier", pKeyCode, pModifier);

			if (pKeyCode == KeyEvent.VK_ALT) {
				if (getParent() != null) {
					revertControlSelector();
					return;
				} else {
					PixelControlSelector pcs = new PixelControlSelector(this);
					changeControlSelector(pcs);
					return;
				}
			}
		} catch (Throwable pT) {
			mLogger.severe(PerceptionLogger.throwableToString(pT));
		}

		mLogger.exiting(mClassname, "keyTyped");
	}
}
