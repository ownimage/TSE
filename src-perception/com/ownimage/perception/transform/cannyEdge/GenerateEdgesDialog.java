package com.ownimage.perception.transform.cannyEdge;

import java.awt.Color;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.math.IntegerPoint;
import com.ownimage.perception.math.Rectangle;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.render.SingleTransformResult;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.ITransform;

/**
 * This is an Edge Transform Control Container Dialog
 */
public class GenerateEdgesDialog extends Container implements IUIEventListener, IControlValidator {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

	public final static String mClassname = GenerateEdgesDialog.class.getName();

	public final static Logger mLogger = Logger.getLogger(mClassname);

	public final static long serialVersionUID = 1L;
	private final CannyEdgeTransform mTransform;
	private final PictureControl mPreviewPicture;
	private final IntegerControl mPreviewSize;

	private final IntegerControl mPreviewPositionX;
	private final IntegerControl mPreviewPositionY;

	private final DoubleControl mGaussianKernelRadius;
	private final DoubleControl mLowThreshold;
	private final DoubleControl mHighThreshold;

	private final IntegerControl mGaussianKernelWidth;
	private final BooleanControl mContrastNormalized;
	private final Container mPreviewContainer;
	private final Container mControlContainer;
	private final Thread mPreviewThread = new Thread(() -> updatePreview());

	private IntegerPoint mDragStart = IntegerPoint.IntegerPoint00;

	public GenerateEdgesDialog(final CannyEdgeTransform pParent, final String pDisplayName,
			final String pPropertyName) {
		super(pDisplayName, pPropertyName, pParent);

		mTransform = pParent;

		mPreviewContainer = new Container("Preview Container", "previewContainer", this, this);
		int size = 100;
		mPreviewSize = new IntegerControl("Preview Size", "previewSize", mPreviewContainer, size,
				100, 1000, 50);
		mPreviewSize.addControlChangeListener(this);
		mPreviewPicture = new PictureControl("Preview", "preview", mPreviewContainer,
				new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(),
						size, size));
		mPreviewPicture.setUIListener(this);

		mControlContainer = new Container("ControlContainer", "controlContainer", this, this);
		mControlContainer.addControlChangeListener(this);
		mControlContainer.addControlValidator(this);

		mPreviewPositionX = new IntegerControl("Preview Position X", "previewPositionX",
				mControlContainer, 0, 0, mTransform.getWidth(), 10).setEnabled(false);
		mPreviewPositionY = new IntegerControl("Preview Position Y", "previewPositionY",
				mControlContainer, 0, 0, mTransform.getHeight(), 10).setEnabled(false);

		mGaussianKernelRadius = new DoubleControl("Kernal Radius", "gaussianKernelRadius",
				mControlContainer, 0.2d, 0.1001d, 10.0d);
		mLowThreshold = new DoubleControl("Low Threshold", "lowThreshold", mControlContainer, 1.0d,
				0.0d, 100.0d);
		mHighThreshold = new DoubleControl("High Threshold", "highThreshold", mControlContainer,
				1.0d, 0.0d, 100.0d);
		mGaussianKernelWidth = new IntegerControl("Gaussian Kernal Width", "gausianKernelWidth",
				mControlContainer, 2, 2, 15, 1);
		mContrastNormalized = new BooleanControl("Contrast Normalized", "contrastNormalized",
				mControlContainer, false);

		updatePreview();
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		mLogger.fine("CannyEdgeTransform.ETControlContainerDialog::controlChangeEvent "
				+ pControl == null ? "null" : pControl.getDisplayName() + " " + pIsMutating);

		if (pControl == mPreviewPositionX || pControl == mPreviewPositionY
				|| pControl == mPreviewPicture) { return;
		// ignore these preview position updates are handled my the mouse drag event
		}
		if (pControl == mPreviewSize) {
			getTransform().redrawGrafitti();
		}
		mPreviewPicture.setValue(updatePreview());
	}

	@Override
	public IView createView() {
		HFlowLayout hflow = new HFlowLayout(mPreviewContainer, mControlContainer);
		IView view = ViewFactory.getInstance().createView(hflow);
		addView(view);
		return view;
	}

	private void generatePreviewPictureFromData(final PixelMap pEdgeData) {
		System.out.println("generatePreviewPictureFromData");
		final int size = mPreviewSize.getValue();

		PictureType preview;
		if (mPreviewPicture == null || mPreviewPicture.getWidth() != size
				|| mPreviewPicture.getWidth() != size) {
			preview = new PictureType(getProperties().getColorOOBProperty(), size, size);
		} else {
			preview = mPreviewPicture.getValue().createCompatible();
		}

		final Color foreground = getProperties().getPixelMapFGColor();
		final Color background = getProperties().getPixelMapBGColor();
		for (int x = 0; x < size; x++) {
			for (int y = 0; y < size; y++) {
				if (pEdgeData.getPixelAt(x, y).isEdge()) {
					preview.setColor(x, y, foreground);
				} else {
					preview.setColor(x, y, background);
				}
			}
		}
		mPreviewPicture.setValue(preview);
	}

	public int getPreviewPositionX() {
		return mPreviewPositionX.getValue();
	}

	public int getPreviewPositionY() {
		return mPreviewPositionY.getValue();
	}

	public Rectangle getPreviewRectangle() {
		double x1 = (double) getPreviewPositionX() / getTransform().getWidth();
		double y1 = (double) getPreviewPositionY() / getTransform().getHeight();
		double x2 = (double) (getPreviewPositionX() + getSize()) / getTransform().getWidth();
		double y2 = (double) (getPreviewPositionY() + getSize()) / getTransform().getHeight();
		Rectangle r = new Rectangle(x1, y1, x2, y2);
		return r;
	}

	private Properties getProperties() {
		return Perception.getPerception().getProperties();
	}

	public int getSize() {
		return (mPreviewSize != null) ? mPreviewSize.getValue() : 100;
	}

	public CannyEdgeTransform getTransform() {
		return mTransform;
	}

	@Override
	public void mouseDragEndEvent(final IUIEvent pEvent) {
		mTransform.redrawGrafitti();
		updatePreview();
	}

	@Override
	public void mouseDragEvent(final IUIEvent pEvent) {
		mPreviewPositionX.setValue(mDragStart.getX() + pEvent.getDeltaX());
		mPreviewPositionY.setValue(mDragStart.getY() + pEvent.getDeltaY());
		mTransform.redrawGrafitti();
	}

	@Override
	public void mouseDragStartEvent(final IUIEvent pEvent) {
		mDragStart = new IntegerPoint(mPreviewPositionX.getValue(), mPreviewPositionY.getValue());
	}

	public void showDialog(final ActionControl pOk, final ActionControl pCancel) {
		Perception.getPerception().showDialog(this, new DialogOptions(), getUndoRedoBuffer(),
				pCancel, pOk);
		updatePreview();
	}

	public PictureType updatePreview() {
		mLogger.finest("CannyEdgeTransform.ETControlContainerDialog::updatePreviewControl");

		int size = getSize();
		// PictureType outputPicture = new PictureType(mTransform.getColorOOBProperty(), size, size);
		// for (int x = 0; x < outputPicture.getWidth(); x++) {
		// for (int y = 0; y < outputPicture.getHeight(); y++) {
		// outputPicture.setColor(x, y, Color.BLUE);
		// }
		// }
		// if (!getTransform().isInitialized()) { return outputPicture; }
		// mLogger.finest("past initialized");

		ICannyEdgeDetector detector = null;
		PictureType inputPicture;

		try {

			final int xoff = 0;
			final int yoff = 0;
			final int width = mTransform.getWidth();
			final int height = mTransform.getHeight();
			inputPicture = new PictureType(mTransform.getColorOOBProperty(), size, size);
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					ITransformResult tr = new SingleTransformResult(
							((double) (x + mPreviewPositionX.getValue())) / width,
							((double) (y + mPreviewPositionY.getValue())) / height);
					ITransform transform = getTransform().getPreviousTransform();
					while (transform != null) {
						transform.transform(tr);
						transform = transform.getPreviousTransform();
					}
					inputPicture.setColor(x, y, tr.getColor());
				}
			}
			mLogger.finest("input picture generated");

			// TODO CannyEdgeDetectorFactory.createInstance(getTransform());
			detector = new CannyEdgeDetectorJavaThreads(getTransform());
			detector.setGaussianKernelRadius(mGaussianKernelRadius.getValue().floatValue());
			detector.setLowThreshold(mLowThreshold.getValue().floatValue() / 100.0f);
			detector.setHighThreshold(mHighThreshold.getValue().floatValue() / 100.0f);
			detector.setGaussianKernelWidth(mGaussianKernelWidth.getValue());
			detector.setContrastNormalized(mContrastNormalized.getValue());

			detector.setSourceImage(inputPicture);
			detector.process(false);

			if (detector.getKeepRunning()) {
				// only set the mData if the detector was allowed to finish
				generatePreviewPictureFromData(detector.getEdgeData());
				// mPreviewControl.getValue().setValue(mPreviewPicture);
			}
		} finally {
			if (detector != null) {
				detector.dispose();
			}
		}

		mLogger.finest("at end");
		return mPreviewPicture.getValue();
	}

	@Override
	public boolean validateControl(final Object pControl) {
		boolean rv = true;
		if (pControl == mPreviewPositionX) {
			rv = mPreviewPositionX.getValidateValue() + getSize() < getTransform().getWidth();
			rv |= mPreviewPositionX.getValidateValue() < mPreviewPositionX.getValue();
		} else if (pControl == mPreviewPositionY) {
			rv = mPreviewPositionY.getValidateValue() + getSize() < getTransform().getHeight();
			rv |= mPreviewPositionY.getValidateValue() < mPreviewPositionY.getValue();
		}
		return rv;
	}
}
