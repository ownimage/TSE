package com.ownimage.perception.transform.cannyEdge;

import java.awt.Color;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.control.PointControl;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.render.ITransformResult;
import com.ownimage.perception.render.SingleTransformResult;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.ITransform;

/**
 * This is an Edge Transform Control Container Dialog
 */
public class GenerateEdgesDialog extends Container {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = GenerateEdgesDialog.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final CannyEdgeTransform mTransform;

	private final PictureControl mPreviewPicture;
	private final IntegerControl mPreviewSize;

	private final PointControl mPreviewPosition;
	// private final EdgePointControl mPreviewPosition;

	private final DoubleControl mGaussianKernelRadius;
	private final DoubleControl mLowThreshold;
	private final DoubleControl mHighThreshold;
	private final IntegerControl mGaussianKernelWidth;
	private final BooleanControl mContrastNormalized;

	private final Container mPreviewContainer;
	private final Container mControlContainer;

	private final Thread mPreviewThread = new Thread() {
		@Override
		public void run() {
			updatePreview();
		}
	};

	public GenerateEdgesDialog(final CannyEdgeTransform pParent, final String pDisplayName, final String pPropertyName) {
		super(pDisplayName, pPropertyName, pParent);

		mTransform = pParent;

		mPreviewContainer = new Container("Preview Container", "previewContainer", this, this);

		mPreviewPicture = new PictureControl("Preview", "preview", mPreviewContainer, updatePreview());
		mPreviewSize = new IntegerControl("Preview Size", "previewSize", mPreviewContainer, 100, 100, 1000, 50);
		mPreviewSize.addControlChangeListener(this);

		mControlContainer = new Container("ControlContainer", "controlContainer", this, this);
		mPreviewPosition = new PointControl("Preview", "preview", mControlContainer, new Point(100.0d, 100.0d));
		// mPreviewPosition = addControl(new EdgePointControl(pParent, mPreviewControl, null, "GeneratePreview",
		// "generatePreview"));
		mGaussianKernelRadius = new DoubleControl("Kernal Radius", "gaussianKernelRadius", mControlContainer, 0.2d, 0.1001d, 10.0d);
		mLowThreshold = new DoubleControl("Low Threshold", "lowThreshold", mControlContainer, 1.0d, 0.0d, 100.0d);
		mHighThreshold = new DoubleControl("High Threshold", "highThreshold", mControlContainer, 1.0d, 0.0d, 100.0d);
		mGaussianKernelWidth = new IntegerControl("Gaussian Kernal Width", "gausianKernelWidth", mControlContainer, 2, 2, 15, 1);
		mContrastNormalized = new BooleanControl("Contrast Normalized", "contrastNormalized", mControlContainer, false);

		// getControlSelector().addMouseXYControl(mPreviewPosition, mPreviewPosition);
		// TODO mPreviewPosition.addMouseControls(pParent); // TODO

		// TODO I think we can remove this later
		// mPreviewPosition.addControlEventListener(this);
	}

	// @Override
	// public void actionCancel() {
	// try {
	// setMutating(true);
	// super.actionCancel();
	// getTransform().graffiti();
	// updatePreviewControl();
	// } finally {
	// setMutating(false);
	// }
	// }
	//
	// @Override
	// public void actionOK() {
	// getTransform().resetHeightWidth();
	//
	// class Execute extends Job {
	// public Execute() {
	// super(mClassname + "::actionOK", false);
	// }
	//
	// @Override
	// public void doJob() {
	// try {
	// getTransform().setEnabled(false);
	// generateEdgeImage();
	// getTransform().postProcess();
	// getTransform().renderPreviewImages(true);
	// } finally {
	// getTransform().setEnabled(true);
	// }
	// }
	// }
	// getTransform().run(new Execute());
	// }
	//
	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		mLogger.fine("CannyEdgeTransform.ETControlContainerDialog::controlChangeEvent " + pControl == null ? "null" : pControl.getDisplayName() + " " + pIsMutating);
		// ICannyEdgeDetector mPreviewDetector = CannyEdgeDetectorFactory.createInstance(getTransform()); // TODO should I have a
		// set
		// of parameters rather than
		// a CETransform

		if (pControl == mPreviewSize) {
			System.out.println("Preview Size changed to " + mPreviewSize.getValue());
			mPreviewPicture.setValue(updatePreview());
		} else if (pControl != mPreviewPicture) {
			// getTransform().graffiti();
			// if (mPreviewPicture.isVisible() && mPreviewDetector != null) {
			// mPreviewDetector.setKeepRunning(false);
			// try {
			// if (mPreviewThread != null && mPreviewThread.isAlive()) {
			// mPreviewThread.join();
			// }
			// } catch (final InterruptedException e) {
			// // Do nothing
			// }
			//
			// mPreviewThread = new Thread() {
			// @Override
			// public void run() {
			// updatePreview();
			// }
			// };
			// mPreviewThread.start();
			// }
		}
	}

	// @Override
	// public IView createView() {
	// HFlowLayout layout = new HFlowLayout(mPreviewContainer, mControlContainer);
	// return layout.createView();
	// }
	//
	// public void generateEdgeImage() {
	// try {
	// mLogger.fine("CannyEdgeDetector::generateEdgeImage");
	//
	// mDetector = CannyEdgeDetectorFactory.createInstance(getTransform());
	// mDetector.setContrastNormalized(mContrastNormalized.getBoolean());
	// mDetector.setGaussianKernelRadius((float) mGaussianKernelRadius.getDouble());
	// mDetector.setGaussianKernelWidth(mGaussianKernelWidth.getInt());
	// mDetector.setHighThreshold((float) (mHighThreshold.getDouble() / 100.0d));
	// mDetector.setLowThreshold((float) (mLowThreshold.getDouble() / 100.0d));
	// mDetector.setEdgeData(mTransform.getPixelMap());
	//
	// mDetector.setSourceImage(getTransform().getPreviousTransform());
	// mDetector.process(true);
	//
	// } finally {
	// mTransform.setPixelMap(mDetector.getEdgeData());
	// mDetector.dispose();
	// getTransform().showProgressBar("Done", 100);
	// getTransform().hideProgressBar();
	// }
	// }
	//
	private void generatePreviewPictureFromData(final PixelMap pEdgeData) {
		System.out.println("generatePreviewPictureFromData");
		final int size = mPreviewSize.getValue();

		PictureType preview;
		if (mPreviewPicture == null || mPreviewPicture.getWidth() != size || mPreviewPicture.getWidth() != size) {
			preview = new PictureType(getProperties().getColorOOBProperty(), size, size);
		} else {
			preview = mPreviewPicture.getValue().createCompatible();
		}

		final Color foreground = getProperties().getColor1();
		final Color background = getProperties().getColor2();
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

	private Properties getProperties() {
		return Perception.getPerception().getProperties();
	}

	private int getSize() {
		return (mPreviewSize != null) ? mPreviewSize.getValue() : 100;
	}

	public CannyEdgeTransform getTransform() {
		return mTransform;
	}

	//
	// public void graffitiTransform(final GraphicsHelper pGraphics) {
	// mPreviewPosition.graffiti(pGraphics);
	// }
	//
	// @Override
	// public void showDialog() {
	// try {
	// getTransform().setEnabled(false);
	// updatePreviewControl();
	// super.showDialog();
	// } finally {
	// getTransform().setEnabled(true);
	// }
	// }
	//
	public PictureType updatePreview() {
		mLogger.finest("CannyEdgeTransform.ETControlContainerDialog::updatePreviewControl");

		int size = getSize();
		PictureType outputPicture = new PictureType(mTransform.getColorOOBProperty(), size, size);
		for (int x = 0; x < outputPicture.getWidth(); x++) {
			for (int y = 0; y < outputPicture.getHeight(); y++) {
				outputPicture.setColor(x, y, Color.BLUE);
			}
		}
		if (!getTransform().isInitialized()) { return outputPicture; }
		mLogger.finest("past initialized");

		ICannyEdgeDetector detector = null;
		PictureType inputPicture;

		try {

			final int xoff = 0;// (int) (getTransform().getWidth() * mPreviewPosition.getX());
			final int yoff = 0;// (int) (getTransform().getHeight() * mPreviewPosition.getY());
			inputPicture = new PictureType(mTransform.getColorOOBProperty(), size, size);
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					ITransformResult tr = new SingleTransformResult((double) x / size, (double) y / size);
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
	//
	// @Override
	// public boolean validate(final IControlPrimative<?> pControl) {
	// if (!getTransform().isInitialized()) { return true; }
	//
	// boolean valid = true;
	//
	// if (pControl == mPreviewPosition.getXControl()) {
	// final double xSize = (double) mPreviewControl.getSize() / getTransform().getWidth();
	// valid &= mPreviewPosition.getXControl().getValidateDouble() + xSize <= 1.0d;
	// }
	//
	// if (pControl == mPreviewPosition.getYControl()) {
	// final double ySize = (double) mPreviewControl.getSize() / getTransform().getHeight();
	// valid &= mPreviewPosition.getYControl().getValidateDouble() + ySize <= 1.0d;
	// }
	//
	// return valid && super.validate(pControl);
	// }

}
