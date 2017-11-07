package com.ownimage.perception.transform.cannyEdge;

import java.awt.Color;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.control.PointControl;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.transform.CannyEdgeTransform;

/**
 * This is an Edge Transform Control Container Dialog
 */
public class GenerateEdgesDialog extends Container {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = GenerateEdgesDialog.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final CannyEdgeTransform mTransform;

	private ICannyEdgeDetector mDetector;
	private ICannyEdgeDetector mPreviewDetector;
	private final PictureControl mPreviewPicture;

	private final PointControl mPreviewControl;
	// private final EdgePointControl mPreviewPosition;

	private final DoubleControl mGaussianKernelRadius;
	private final DoubleControl mLowThreshold;
	private final DoubleControl mHighThreshold;
	private final IntegerControl mGaussianKernelWidth;
	private final BooleanControl mContrastNormalized;

	private final Container mPreviewContainer;
	private final Container mControlContainer;

	// private Thread mPreviewThread = new Thread() {
	// @Override
	// public void run() {
	// updatePreviewControl();
	// }
	// };

	public GenerateEdgesDialog(final CannyEdgeTransform pParent, final String pDisplayName, final String pPropertyName) {
		super(pDisplayName, pPropertyName, pParent);

		mTransform = pParent;

		mPreviewContainer = new Container("Preview Container", "previewContainer", this);
		PictureType picture = new PictureType(mTransform.getProperties().getColorOOBProperty(), 400, 400);
		for (int x = 0; x < picture.getWidth(); x++)
		{
			for (int y = 0; y < picture.getHeight(); y++) {
				picture.setColor(x, y, Color.pink);
			}
		}
		mPreviewPicture = new PictureControl("Preview", "preview", mPreviewContainer, picture);

		mControlContainer = new Container("ControlContainer", "controlContainer", this);
		mPreviewControl = new PointControl("Preview", "preview", mControlContainer, new Point(100.0d, 100.0d));
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

	@Override
	public IView createView() {
		HFlowLayout layout = new HFlowLayout(mPreviewContainer, mControlContainer);
		return layout.createView();
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
	// @Override
	// public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
	// mLogger.fine("CannyEdgeTransform.ETControlContainerDialog::controlChangeEvent " + pControl == null ? "null" :
	// pControl.getDisplayName() + " " + pIsMutating);
	//
	// if (pControl != mPreviewControl.getPictureControl()) {
	// getTransform().graffiti();
	// if (isVisible() && mPreviewDetector != null) {
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
	// updatePreviewControl();
	// }
	// };
	// mPreviewThread.start();
	// }
	// }
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
	// private void generatePreviewPictureFromData(final PixelMap pEdgeData) {
	// final int size = mPreviewControl.getSize();
	//
	// if (mPreviewPicture == null || mPreviewPicture.getWidth() != size || mPreviewPicture.getWidth() != size) {
	// mPreviewPicture = new Picture(size, size);
	// }
	//
	// final Color foreground = getProperties().getEdgePreviewFgColor();
	// final Color background = getProperties().getEdgePreviewBgColor();
	// for (int x = 0; x < size; x++) {
	// for (int y = 0; y < size; y++) {
	// if (pEdgeData.getPixelAt(x, y).isEdge()) {
	// mPreviewPicture.setColor(x, y, foreground);
	// } else {
	// mPreviewPicture.setColor(x, y, background);
	// }
	// }
	// }
	// }
	//
	// private Properties getProperties() {
	// return getTransform().getProperties();
	// }
	//
	// @Override
	// public CannyEdgeTransform getTransform() {
	// return (CannyEdgeTransform) super.getTransform();
	// }
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
	// public void updatePreviewControl() {
	// mLogger.fine("CannyEdgeTransform.ETControlContainerDialog::updatePreviewControl");
	//
	// try {
	// final int size = mPreviewControl.getSize();
	// final int xoff = (int) (getTransform().getWidth() * mPreviewPosition.getX());
	// final int yoff = (int) (getTransform().getHeight() * mPreviewPosition.getY());
	// mPreviewPicture = new Picture(size, size);
	// for (int x = 0; x < size; x++) {
	// // TODO isnt there a different Picture constructor
	// for (int y = 0; y < size; y++) {
	// if (x + xoff < getTransform().getWidth() && y + yoff < getTransform().getHeight()) {
	// mPreviewPicture.setColor(x, y, getTransform().getColorFromPreviousTransform(x + xoff, y + yoff));
	// }
	// }
	// }
	//
	// mPreviewDetector = CannyEdgeDetectorFactory.createInstance(getTransform());
	// mPreviewDetector.setGaussianKernelRadius((float) mGaussianKernelRadius.getDouble());
	// mPreviewDetector.setLowThreshold((float) (mLowThreshold.getDouble() / 100.0d));
	// mPreviewDetector.setHighThreshold((float) (mHighThreshold.getDouble() / 100.0d));
	// mPreviewDetector.setGaussianKernelWidth(mGaussianKernelWidth.getInt());
	// mPreviewDetector.setContrastNormalized(mContrastNormalized.getBoolean());
	//
	// mPreviewDetector.setSourceImage(mPreviewPicture);
	// mPreviewDetector.process(false);
	//
	// if (mPreviewDetector.getKeepRunning()) {
	// // only set the mData if the detector was allowed to finish
	// generatePreviewPictureFromData(mPreviewDetector.getEdgeData());
	// mPreviewControl.getPictureControl().setValue(mPreviewPicture);
	// }
	// } finally {
	// mPreviewDetector.dispose();
	// }
	// }
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
