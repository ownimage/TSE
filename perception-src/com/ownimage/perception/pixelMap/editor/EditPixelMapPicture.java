/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */
package com.ownimage.perception.pixelMap.editor;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.perception.control.combo.PointControl;
import com.ownimage.perception.control.controller.IControlPrimative;
import com.ownimage.perception.control.group.IControlContainer;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.transform.GraphicsHelper;
import com.ownimage.perception.transform.IGrafitti;
import com.ownimage.perception.ui.client.ICUI;
import com.ownimage.perception.ui.client.IGrafittiPictureUI;
import com.ownimage.perception.ui.factory.GUIFactory;
import com.ownimage.perception.ui.server.IGrafittiPictureUISource;
import com.ownimage.perception.util.IPicture;
import com.ownimage.perception.util.Version;

public class EditPixelMapPicture implements IGrafittiPictureUISource, IGrafitti {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = EditPixelMapPicture.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private boolean mVisible = true;
	private boolean mEnabled = true;
	private IGrafittiPictureUI mUI;

	private final EditPixelMapDialog mPixelMapDialog;

	public EditPixelMapDialog getPixelMapDialog() {
		return mPixelMapDialog;
	}

	public EditPixelMapPicture(final EditPixelMapDialog pPixelMapDialog) {
		mPixelMapDialog = pPixelMapDialog;
	}

	public IGrafittiPictureUI createUI() {
		if (mUI == null) {
			mUI = GUIFactory.getInstance().createUI(this);
		}
		return mUI;
	}

	@Override
	public IControlContainer getControlContainer() {
		return getPixelMapDialog();
	}

	@Override
	public List<IGrafitti> getGrafitti() {
		final Vector<IGrafitti> graffitis = new Vector<IGrafitti>();
		graffitis.add(getPixelMapDialog());
		graffitis.add(this);
		return graffitis;
	}

	@Override
	public IControlPrimative<IPicture> getPictureControl() {
		return getPixelMapDialog().getPicture();
	}

	@Override
	public ICUI getPopupMenu() {
		return null;
	}

	@Override
	public PointControl getStartControl() {
		final Point startPoint = getPixelMapDialog().getPreviewPosition().getPoint().scaleX(getPixelMapDialog().getAspectRatio());
		final PointControl startControl = new PointControl(startPoint);
		return startControl;
	}

	@Override
	public IControlPrimative<Double> getWidthControl() {
		return getPixelMapDialog().getWidthControl();
	}

	@Override
	public boolean isCenter() {
		return false;
	}

	@Override
	public boolean isCorner() {
		return true;
	}

	@Override
	public boolean isPadded() {
		return false;
	}

	@Override
	public void setVisible(boolean pVisible) {
		mVisible = pVisible;
	}

	@Override
	public void setEnabled(boolean pEnabled) {
		mEnabled = pEnabled;
	}

	@Override
	public boolean isVisible() {
		return mVisible;
	}

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	@Override
	public void graffiti(GraphicsHelper pGraphics) {
		// this is to implement the cursor grafitti
		getPixelMapDialog().grafittiCursor(pGraphics);

	}

	private IGrafittiPictureUI getUI() {
		return mUI;
	}

	public void grafittiCursor() {
		getUI().grafitti(1);
	}
}
