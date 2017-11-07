/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */

package com.ownimage.perception.pixelMap.editor;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.undo.IUndoRedoBuffer;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.ControlSelector;

public class EditPixelMapControlSelectorBase extends ControlSelector implements IControlChangeListener {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = EditPixelMapControlSelectorBase.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final EditPixelMapDialog mEditPixelMapDialog;
	private final EditPixelMapControlSelectorBase mParent;

	public EditPixelMapControlSelectorBase(final EditPixelMapControlSelectorBase pParent) {
		mParent = pParent;
		mEditPixelMapDialog = mParent.getEditPixelMapDialog();
	}

	public EditPixelMapControlSelectorBase(final EditPixelMapDialog pEditPixelMapDialog) {
		mParent = null;
		mEditPixelMapDialog = pEditPixelMapDialog;
	}

	protected void actionRedo() {
		mLogger.entering(mClassname, "actionRedo");

		getUndoRedoBuffer().redo();
		setSelectedVertexNull();
		graffiti();

		mLogger.exiting(mClassname, "actionRedo");
	}

	protected void actionUndo() {
		mLogger.entering(mClassname, "actionUndo");

		getUndoRedoBuffer().undo();
		setSelectedVertexNull();
		graffiti();

		mLogger.exiting(mClassname, "actionUndo");
	}

	public void addPixelChain(final PixelChain pPixelChain) {
		mEditPixelMapDialog.addPixelChain(pPixelChain);
	}

	public void calcVisibleVertexes() {
		mEditPixelMapDialog.calcVisibleVertexes();
	}

	public void changeControlSelector(final EditPixelMapControlSelectorBase pTo) {
		mEditPixelMapDialog.changeControlSelector(this, pTo);
		graffiti();
	}

	public void changeControlSelector(final EditPixelMapControlSelectorBase pFrom, final EditPixelMapControlSelectorBase pTo) {
		mEditPixelMapDialog.changeControlSelector(pFrom, pTo);
		graffiti();
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		// TODO Auto-generated method stub
	}

	@Override
	public void delta(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
		if (pModifier.isNormal()) {
			deltaNormal(pDeltaX, pDeltaY, pIsMutating, pModifier);
		}
		if (pModifier.isControlDown()) {
			deltaControl(pDeltaX, pDeltaY, pIsMutating, pModifier);
		}
		if (pModifier.isShiftDown()) {
			deltaShift(pDeltaX, pDeltaY, pIsMutating, pModifier);
		}
		if (pModifier.isAltDown()) {
			deltaAlt(pDeltaX, pDeltaY, pIsMutating, pModifier);
		}
	}

	public void deltaAlt(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
	}

	public void deltaControl(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
	}

	public void deltaNormal(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
	}

	public void deltaShift(final double pDeltaX, final double pDeltaY, final boolean pIsMutating, final Modifier pModifier) {
	}

	public double getAspectRatio() {
		return mEditPixelMapDialog.getAspectRatio();
	}

	public ControlSelector getControlSelector() {
		return getTransform().getControlSelector();
	}

	public EditPixelMapDialog getEditPixelMapDialog() {
		return mEditPixelMapDialog;
	}

	public double getHeight() {
		return mEditPixelMapDialog.getHeight();
	}

	public EditPixelMapControlSelectorBase getParent() {
		return mParent;
	}

	public Pixel getPixel(final double pX, final double pY) {
		mLogger.entering(mClassname, "getPixel");
		final Pixel pixel = mEditPixelMapDialog.getPixel(pX, pY);
		mLogger.log(Level.FINER, "pixel = " + pixel);
		mLogger.exiting(mClassname, "getPixel", pixel);
		return pixel;
	}

	public PixelMap getPixelMap() {
		return mEditPixelMapDialog.getPixelMap();
	}

	public List<ISegment> getSegmentsAtSelectedVertexPosition() {
		return mEditPixelMapDialog.getSegmentsAtSelectedVertexPosition();
	}

	public PixelChain getSelectedPixelChain() {
		return mEditPixelMapDialog.getSelectedPixelChain();
	}

	public ISegment getSelectedSegment() {
		return mEditPixelMapDialog.getSelectedSegment();
	}

	public IVertex getSelectedVertex() {
		return mEditPixelMapDialog.getSelectedVertex();
	}

	public CannyEdgeTransform getTransform() {
		mLogger.entering(mClassname, "getTransform");
		final CannyEdgeTransform transform = getEditPixelMapDialog().getTransform();
		mLogger.exiting(mClassname, "getTransform", transform);
		return transform;
	}

	public int getTransformHeight() {
		mLogger.entering(mClassname, "getTransformHeight");
		final int height = getTransform().getHeight();
		mLogger.exiting(mClassname, "getTransformHeight", height);
		return height;
	}

	public int getTransformWidth() {
		mLogger.entering(mClassname, "getTransformWidth");
		final int width = getTransform().getWidth();
		mLogger.exiting(mClassname, "getTransformWidth", width);
		return width;
	}

	public IUndoRedoBuffer getUndoRedoBuffer() {
		return mEditPixelMapDialog.getUndoRedoBuffer();
	}

	public Vector<IVertex> getVertexesAtSelectedVertexPosition() {
		return mEditPixelMapDialog.getVertexesAtSelectedVertexPosition();
	}

	public Vector<IVertex> getVisibleVertexes() {
		return mEditPixelMapDialog.getVisibleVertexes();
	}

	public double getWidth() {
		return mEditPixelMapDialog.getWidth();
	}

	public void graffiti() {
		mEditPixelMapDialog.graffiti();
	}

	public void graffiti(final EPMDGraphicsHelper pGraphics) {
	}

	public void graffitiCursor() {
		// TODO Auto-generated method stub

	}

	public void graffitiName(final EPMDGraphicsHelper pGraphicsHelper) {
		if (getDisplayName() != null) {
			pGraphicsHelper.drawString(getDisplayName(), 0, 18);
		}
	}

	public EditPixelMapDialog mEditPixelMapDialog() {
		return mEditPixelMapDialog;
	}

	public void revertControlSelector() {
		changeControlSelector(this, getParent());
		setPopupMenu(null);
		graffiti();
	}

	@Override
	public void saveValues(final Modifier pModifier) {
		mLogger.entering(mClassname, "saveValues");

		final ControlSelector controlSelector = getControlSelector();
		// can not call controlSelector.saveValues as this will just delegate back here.
		if (controlSelector.getXControl() != null) {
			controlSelector.getXControl().saveNormalizedValue();
		}

		if (controlSelector.getYControl() != null) {
			controlSelector.getYControl().saveNormalizedValue();
		}

		mLogger.exiting(mClassname, "saveValues");
	}

	@Override
	public void setDisplayName(final String pDisplayName) {
		mLogger.entering(mClassname, "setDisplayName");
		PerceptionLogger.logParams(mLogger, "pDisplayName", pDisplayName);
		mLogger.exiting(mClassname, "setDisplayName");
		throw new UnsupportedOperationException("Can not setDisplayName for a " + mClassname);
	}

	public void setPopupMenu(final KPopupMenu pPopupMenu) {
		Perception.getInstance().getApplicationUI().setPopupMenu(pPopupMenu);
	}

	public void setSelectedPixelChain(final PixelChain pSelectedPixelChain) {
		mEditPixelMapDialog.setSelectedPixelChain(pSelectedPixelChain);
	}

	public void setSelectedSegment(final ISegment pSegment) {
		mEditPixelMapDialog.setSelectedSegment(pSegment);
	}

	public void setSelectedSegmentNull() {
		mEditPixelMapDialog.setSelectedSegmentNull();
	}

	public void setSelectedVertex(final IVertex pVertex) {
		mEditPixelMapDialog.setSelectedVertex(pVertex);
	}

	public void setSelectedVertexNull() {
		mEditPixelMapDialog.setSelectedVertexNull();
	}

	public Vector<IVertex> sortVisibleVertexes(final Pixel pPixel) {
		return mEditPixelMapDialog.sortVisibleVertexes(pPixel);
	}

	@Override
	public boolean validate(final IControlPrimative<?> pControl) {
		return true;
	}
}
