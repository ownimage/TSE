/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */

package com.ownimage.perception.pixelMap.editor;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.perception.math.IntegerPoint;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.segment.ISegment;

public class VertexControlSelector extends EditPixelMapControlSelectorBase {

	private class MoveVertexUndo implements com.ownimage.framework.undo.IUndoRedoAction {

		private final IVertex mVertex;
		private final Pixel mFrom;
		private final Pixel mTo;

		public MoveVertexUndo(final IVertex pVertex, final Pixel pFrom, final Pixel pTo) {

			if (pVertex == null) { throw new IllegalArgumentException("pVertex must not be null"); }
			if (pFrom == null) { throw new IllegalArgumentException("pFrom must not be null"); }
			if (pTo == null) { throw new IllegalArgumentException("pTo must not be null"); }

			mVertex = pVertex;
			mFrom = pFrom;
			mTo = pTo;
		}

		@Override
		public String getDescription() {
			return "Move vertex.";
		}

		@Override
		public void redo() {
			mVertex.setPixel(mTo);
		}

		@Override
		public void undo() {
			mVertex.setPixel(mFrom);
		}
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = VertexControlSelector.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);

	public final static long serialVersionUID = 1L;
	private Pixel mVertexStartPixel;

	private int mSelectedSegmentIndex;

	// private PixelChain mUndoPixelChain;
	//
	// private PixelChain mUndoPixelChainData;

	private ICUI mPopupMenu;

	public VertexControlSelector(final PictureControlSelector pParent) {
		super(pParent);
		setSelectedVertex(pParent.getSelectedVertex());
	}

	private void actionDeleteCurrentVertex() {
		mLogger.entering(mClassname, "actionDeleteCurrentVertex");
		final IVertex vertex = getSelectedVertex();
		if (vertex != null) {
			setSelectedVertexNull();
			vertex.getPixelChain().deleteVertex(vertex);
			getPixelMap().indexSegments();
			calcVisibleVertexes();
			graffiti();
		}
		mLogger.exiting(mClassname, "actionDeleteCurrentVertex");
	}

	private void actionEditCurrentSegment() {
		mLogger.entering(mClassname, "actionEditCurrentSegment");
		if (getSelectedVertex() != null && getSelectedSegment() != null) {
			graffiti();
			final SegmentControlSelector scs = new SegmentControlSelector(this);
			changeControlSelector(this, scs);
		}
		mLogger.exiting(mClassname, "actionEditCurrentSegment");
	}

	private void actionMoveCurrentVertex(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
		mLogger.entering(mClassname, "actionMoveCurrentVertex");

		if (getSelectedVertex() != null) {

			try {
				if (mVertexStartPixel == null) {
					mVertexStartPixel = getSelectedVertex().getPixel();
				}

				final int dx = (int) (pDeltaX * getWidth() * getTransformWidth());
				final int dy = (int) (pDeltaY * getHeight() * getTransformHeight());
				final IntegerPoint posn = mVertexStartPixel.add(dx, dy);

				if (!posn.equals(getSelectedVertex().getPixel())) {
					final Pixel pixel = new Pixel(getPixelMap(), posn.getX(), posn.getY());

					if (pModifier.isNormal()) {
						getSelectedVertex().setPixel(pixel);
					}

					if (pModifier.isShiftDown()) {
						for (final IVertex vertex : getVertexesAtSelectedVertexPosition()) {
							vertex.setPixel(pixel);
						}
					}
				}

			} finally {
				if (!pIsMutating) {
					final MoveVertexUndo undo = new MoveVertexUndo(getSelectedVertex(), mVertexStartPixel, getSelectedVertex().getPixel());
					getUndoRedoBuffer().add(undo);
					mVertexStartPixel = null;
				}
			}
		}
		mLogger.exiting(mClassname, "actionMoveCurrentVertex");
	}

	private void actionSelectCurrentVertex(final double pX, final double pY) {
		mLogger.entering(mClassname, "actionSelectCurrentVertex");
		final Pixel pixel = getPixel(pX, pY);
		sortVisibleVertexes(pixel);
		graffiti();
		mLogger.exiting(mClassname, "actionSelectCurrentVertex");
	}

	private void actionSelectNextSegmentAtSelectedVertexPosition() {
		mLogger.entering(mClassname, "actionSelectNextSegmentAtSelectedVertexPosition");
		final List<ISegment> attachedSegments = getSegmentsAtSelectedVertexPosition();
		if (attachedSegments.size() != 0) {
			mLogger.fine("Control next");
			mLogger.fine("attachedSegments.size() = " + attachedSegments.size());
			mLogger.fine("attachedSegments = " + attachedSegments);
			mLogger.fine("mSelectedSegmentIndex = " + mSelectedSegmentIndex);
			mSelectedSegmentIndex = (mSelectedSegmentIndex + 1) % attachedSegments.size();
			setSelectedSegment(attachedSegments.get(mSelectedSegmentIndex));
			mLogger.fine("mSelectedSegmentIndex = " + mSelectedSegmentIndex);
			graffiti();
			mLogger.exiting(mClassname, "actionSelectNextSegmentAtSelectedVertexPosition");
		}
	}

	private void actionSelectNextVertexInPixelChain() {
		mLogger.entering(mClassname, "actionSelectNextVertexInPixelChain");
		try {
			final IVertex next = getSelectedVertex().getEndSegment().getEndVertex();
			sortVisibleVertexes(next.getPixel());
		} catch (final Throwable pT) {
			mLogger.fine("Exception " + pT);
		}
		mLogger.exiting(mClassname, "actionSelectNextVertexInPixelChain");
	}

	private void actionSelectPreviousSegmentAtSelectedVertexPosition() {
		mLogger.entering(mClassname, "actionSelectPreviousSegmentAtSelectedVertexPosition");
		final List<ISegment> attachedSegments = getSegmentsAtSelectedVertexPosition();
		if (attachedSegments.size() != 0) {
			mLogger.info("Control previous");
			mSelectedSegmentIndex = (mSelectedSegmentIndex + attachedSegments.size() - 1) % attachedSegments.size();
			setSelectedSegment(attachedSegments.get(mSelectedSegmentIndex));
			mLogger.fine("mSelectedSegmentIndex = " + mSelectedSegmentIndex);
			graffiti();
		}
		mLogger.exiting(mClassname, "actionSelectPreviousSegmentAtSelectedVertexPosition");
	}

	private void actionSelectPreviousVertexInPixelChain() {
		mLogger.entering(mClassname, "actionSelectPreviousVertexInPixelChain");
		try {
			final IVertex next = getSelectedVertex().getStartSegment().getStartVertex();
			sortVisibleVertexes(next.getPixel());
		} catch (final Throwable pT) {
			mLogger.log(Level.SEVERE, "Exception " + pT);
		}
		mLogger.exiting(mClassname, "actionSelectPreviousVertexInPixelChain");
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		// TODO Auto-generated method stub
		super.controlChangeEvent(pControl, pIsMutating);
	}

	@Override
	public void delta(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
		mLogger.entering(mClassname, "deltaControl");
		PerceptionLogger.logParams(mLogger, "pDeltaX, pDeltaY, pIsMutating, pModifier", pDeltaX, pDeltaY, pIsMutating, pModifier);

		if (pModifier.isNormal() || pModifier.isShiftDown()) {
			actionMoveCurrentVertex(pDeltaX, pDeltaY, pIsMutating, pModifier);
		}

		mLogger.exiting(mClassname, "deltaControl");
	}

	@Override
	public String getDisplayName() {
		mLogger.entering(mClassname, "getDisplayName");
		final String displayName = "Vertex";
		mLogger.exiting(mClassname, "getDisplayName", displayName);
		return displayName;
	}

	@Override
	public void graffiti(final EPMDGraphicsHelper pGraphics) {
		mLogger.entering(mClassname, "graffiti");
		super.graffiti(pGraphics);

		final IVertex vertex = getSelectedVertex();
		if (vertex != null) {
			pGraphics.graffitiVertexSelected(vertex);

			pGraphics.graffitiSegmentAttached(vertex.getStartSegment());
			pGraphics.graffitiSegmentAttached(vertex.getEndSegment());

			pGraphics.graffitiSegmentSelected(getSelectedSegment());
		}

		mLogger.exiting(mClassname, "graffiti");
	}

	@Override
	public void keyPressed(final int pKeyCode, final Modifier pModifier) {
		mLogger.entering(mClassname, "keyPressed");

		try {
			PerceptionLogger.logParams(mLogger, "pKeyCode, pModifier", pKeyCode, pModifier);

			if (pKeyCode == KeyEvent.VK_DELETE && pModifier.isNormal()) {
				actionDeleteCurrentVertex();
			}

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

		mLogger.exiting(mClassname, "keyPressed");
	}

	@Override
	public void keyReleased(final int pKeyCode, final Modifier pModifier) {
		mLogger.entering(mClassname, "keyReleased");

		try {
			PerceptionLogger.logParams(mLogger, "pKeyCode, pModifier", pKeyCode, pModifier);

			if (pKeyCode == KeyEvent.VK_CONTROL) {
				mLogger.fine("Control key released");
				actionEditCurrentSegment();
			}
		} catch (final Throwable pT) {
			mLogger.severe(mClassname);
			mLogger.severe(pT.getMessage());
			mLogger.severe(PerceptionLogger.throwableToString(pT));
		}

		mLogger.exiting(mClassname, "keyReleased");
	}

	@Override
	public void next(final Modifier pModifier) {
		mLogger.entering(mClassname, "next");
		PerceptionLogger.logParams(mLogger, "pModifier", pModifier);

		if (pModifier.isNormal()) {
			actionSelectNextVertexInPixelChain();
		}

		if (pModifier.isControlDown()) {
			actionSelectNextSegmentAtSelectedVertexPosition();
		}

		mLogger.exiting(mClassname, "next");
	}

	@Override
	public void previous(final Modifier pModifier) {
		mLogger.entering(mClassname, "previous");
		PerceptionLogger.logParams(mLogger, "pModifier", pModifier);

		if (pModifier.isNormal()) {
			actionSelectPreviousVertexInPixelChain();
		}

		if (pModifier.isControlDown()) {
			actionSelectPreviousSegmentAtSelectedVertexPosition();
		}

		mLogger.fine("previous");
		mLogger.exiting(mClassname, "previous");
	}

	@Override
	public boolean setSelected(final double pX, final double pY, final Modifier pModifier) {
		mLogger.entering(mClassname, "setSelected");
		PerceptionLogger.logParams(mLogger, "pX, pY, pModifier", pX, pY, pModifier);

		if (pModifier.isNormal()) {
			actionSelectCurrentVertex(pX, pY);
		}

		if (pModifier.isControlDown()) {
			actionEditCurrentSegment();
		}

		if (pModifier.isAltDown()) {
			revertControlSelector();
		}

		final boolean result = false;
		mLogger.exiting(mClassname, "setSelected", result);
		return result;
	}

}
