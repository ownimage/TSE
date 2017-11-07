/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */

package com.ownimage.perception.pixelMap.editor;

import java.awt.event.KeyEvent;
import java.util.logging.Logger;

import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.util.Version;
import com.ownimage.perception.util.logging.PerceptionLogger;

public class SegmentControlSelector extends EditPixelMapControlSelectorBase {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = SegmentControlSelector.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private Point mSelectedControlPointStartPosition;

	public SegmentControlSelector(final VertexControlSelector pParent) {
		super(pParent);
		setSelectedSegment(pParent.getSelectedSegment());
	}

	private void actionControlPointMove(final double pDeltaX, final double pDeltaY, final boolean pIsMutating) {
		mLogger.entering(mClassname, "actionMoveSelectedControlPoint");
		try {
			if (mSelectedControlPointStartPosition == null) {
				mSelectedControlPointStartPosition = getSelectedControlPoint();
			}

			if (mSelectedControlPointStartPosition != null) {
				mLogger.fine("moving control point");
				final double dx = pDeltaX * getWidth() * getAspectRatio();
				final double dy = pDeltaY * getHeight();
				final Point delta = new Point(dx, dy);
				final Point newPosition = mSelectedControlPointStartPosition.add(delta);
				setSelectedControlPoint(newPosition);
			}
		} finally {
			if (!pIsMutating) {
				mSelectedControlPointStartPosition = null;
			}
		}
		mLogger.exiting(mClassname, "actionMoveSelectedControlPoint");
	}

	private void actionControlPointNext() {
		if (getSelectedSegment() != null) {
			getSelectedSegment().nextControlPoint();
		}
		graffiti();
	}

	private void actionControlPointPrevious() {
		if (getSelectedSegment() != null) {
			getSelectedSegment().previousControlPoint();
		}
		graffiti();
	}

	private void actionSegmentDelete() {
		final ISegment segment = getSelectedSegment();
		if (segment != null) {
			segment.delete();
			setSelectedSegmentNull();
			graffiti();
		}

		// this is done to recalculate the attached vertexes
		setSelectedVertexNull();
	}

	@Override
	public void delta(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
		mLogger.entering(mClassname, "delta");
		PerceptionLogger.logParams(mLogger, "pDeltaX, pDeltaY, pIsMutating, pModifier", pDeltaX, pDeltaY, pIsMutating, pModifier);

		actionControlPointMove(pDeltaX, pDeltaY, pIsMutating);
		mLogger.exiting(mClassname, "delta");
	}

	@Override
	public String getDisplayName() {
		mLogger.entering(mClassname, "getDisplayName");
		final String displayName = "Segment";
		mLogger.exiting(mClassname, "getDisplayName", displayName);
		return displayName;
	}

	public Point getSelectedControlPoint() {
		mLogger.entering(mClassname, "getSelectedControlPoint");
		Point point = null;
		if (getSelectedSegment() != null) {
			point = getSelectedSegment().getControlPoint();
		}
		mLogger.exiting(mClassname, "getSelectedControlPoint", point);
		return point;
	}

	@Override
	public void graffiti(final EPMDGraphicsHelper pGraphics) {
		mLogger.entering(mClassname, "graffiti");
		super.graffiti(pGraphics);

		pGraphics.graffitiSegmentSelected(getSelectedSegment());
		final ISegment segment = getSelectedSegment();
		if (segment != null) {
			segment.graffiti(pGraphics);
		}

		mLogger.exiting(mClassname, "graffiti");
	}

	@Override
	public void keyPressed(final int pKeyCode, final Modifier pModifier) {
		mLogger.entering(mClassname, "keyTyped");

		try {
			PerceptionLogger.logParams(mLogger, "pKeyCode, pModifier", pKeyCode, pModifier);

			if (pKeyCode == KeyEvent.VK_DELETE) {
				actionSegmentDelete();
				return;
			}

		} catch (Throwable pT) {
			mLogger.severe(PerceptionLogger.throwableToString(pT));
		}

		mLogger.exiting(mClassname, "keyTyped");
	}

	@Override
	public void next(final Modifier pModifier) {
		actionControlPointNext();
	}

	@Override
	public void previous(final Modifier pModifier) {
		actionControlPointPrevious();
	}

	@Override
	public boolean setSelected(final double pX, final double pY, final Modifier pModifier) {
		mLogger.entering(mClassname, "setSelected");
		PerceptionLogger.logParams(mLogger, "pX, pY, pModifier", pX, pY, pModifier);
		final boolean result = false;

		if (pModifier.isAltDown()) {
			mLogger.fine("setSelected isAltDown");
			setSelectedSegmentNull();
			revertControlSelector();
		}

		mLogger.exiting(mClassname, "setSelected", result);
		return result;
	}

	public void setSelectedControlPoint(final Point pPoint) {
		mLogger.entering(mClassname, "setSelectedControlPoint");
		if (getSelectedSegment() != null) {
			getSelectedSegment().setControlPoint(pPoint);
		}
		mLogger.exiting(mClassname, "setSelectedControlPoint");
	}
}
