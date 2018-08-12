/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform;

import java.awt.*;
import java.io.File;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.type.IntegerMetaType;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.render.ITransformResult;

public class ImageLoadTransform extends BaseTransform {


    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();
	private final File mFile;

	private final PictureControl mSourcePicture; // this is the picture from the file
	private PictureType mSharpPicture;
	private final BooleanControl m360 = new BooleanControl("360", "360", getContainer(), false);

	private final BooleanControl mSharpen = new BooleanControl("Sharpen", "sharpen", getContainer(), false);
	private final IntegerControl mPasses = new IntegerControl("Passes", "passes", getContainer(), 0, new IntegerMetaType(0, 1000, 1));
	private final BooleanControl mSmooth = new BooleanControl("Smooth", "smooth", getContainer(), false);
	private final BooleanControl mReshape = new BooleanControl("Reshape", "reshape", getContainer(), false);
	private final int mCurrentPasses = -1;

	public ImageLoadTransform(final Perception pPerception, final File pFile) {
		super("Image Load", "imageLoad");

		Framework.logEntry(mLogger);

		mFile = pFile;
		PictureType sourcePicture = new PictureType(getProperties().getColorOOBProperty(), mFile);
		mSourcePicture = new PictureControl("Source Image", "sourceImage", new Container("x", "x", this), sourcePicture) //
				.setTransient();

		PictureType previewImage = getPreviewImage().getValue().clone();

		double sourceWidth = sourcePicture.getWidth();
		double sourceHeight = sourcePicture.getHeight();

		int previewWidth = previewImage.getWidth();
		int previewHeight = previewImage.getHeight();

		for (int previewX = 0; previewX < previewWidth; previewX++) {
			for (int previewY = 0; previewY < previewHeight; previewY++) {
				double sourceX = (double) previewX / previewWidth;
				double sourceY = (double) previewY / previewHeight;
				Color c = sourcePicture.getColor(sourceX, sourceY);
				previewImage.setColor(previewX, previewY, c);
			}
		}

		getPreviewImage().setValue(previewImage);

		Framework.logExit(mLogger);
	}

	public ImageLoadTransform(final Perception pPerception, final String pFilename) {
		this(pPerception, new File(pFilename));
	}

	@Override
	public void controlChangeEvent(final Object pControl, final boolean pIsMutating) {
		Framework.logEntry(mLogger);
		getPreviewImage().redrawGrafitti();
		Framework.logExit(mLogger);
	}

	@Override
	public ITransform duplicate() {
		throw new RuntimeException("duplicate() should not be called on ImageLoadTransform()");
	}

	@Override
	public int getHeight() {
		return mSourcePicture.getValue().getHeight();
	}

	// @Override
	public int getOversample() {
		return 1;
	}

	// private byte mOffset[][];

	// public String absolutePath(final String pRelative) throws IOException {
	// if (mFile == null) {
	// return pRelative;
	// }
	//
	// String absolute = mFile.getParentFile().getCanonicalPath() + File.separator + pRelative;
	// return absolute;
	//
	// }

	// @Override
	// public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
	// if ((pControl == mPasses || pControl == mSharpen) && mSharpen.getBoolean() && mCurrentPasses != mPasses.getInt()) {
	// // TODO should really kick the thread off in the sharpen method
	// Thread thread = new Thread() {
	// @Override
	// public void run() {
	// sharpen();
	// }
	// };
	// thread.start();
	// mCurrentPasses = mPasses.getInt();
	// }
	//
	// // if ((pControl == mSharpen || pControl == mReshape) && mReshape.getBoolean()) {
	// // reshape(mSharpen.getBoolean() ? mSharpPicture : mSourcePicture);
	// // }
	//
	// if (pControl != mPasses || mSharpen.getBoolean()) {
	// super.controlChangeEvent(pControl, pIsMutating);
	// }
	//
	// }

	// private int delta(final Color pColor1, final Color pColor2) {
	// return Math.abs(pColor1.getRed() - pColor2.getRed()) + Math.abs(pColor1.getGreen() - pColor2.getGreen()) +
	// Math.abs(pColor1.getBlue() - pColor2.getBlue());
	// }
	//
	// public String getAbsoluteFilePath() {
	// return (mFile != null) ? mFile.getAbsolutePath() : null;
	// }
	//
	// @Override
	// public Color getColor(final int pX, final int pY) {
	// return mSourcePicture.getColor(pX, pY);
	// }
	//
	// private Color getColorReshape(final IPictureReadOnly pSourcePicture, final Point pIn) {
	// int x = (int) Math.floor(pIn.getX() * getWidth());
	// int y = (int) Math.floor(pIn.getY() * getHeight());
	//
	// // double xOff = pIn.getX() * getWidth() - RenderEngine;
	// // double yOff = pIn.getY() * getHeight() - y;
	//
	// Color cAvgNW = KColor.average(mSourcePicture.getColor(x, y), mSourcePicture.getColor(x - 1, y), mSourcePicture.getColor(x -
	// 1, y + 1), mSourcePicture.getColor(x, y + 1));
	// Color cAvgNE = KColor.average(mSourcePicture.getColor(x, y), mSourcePicture.getColor(x + 1, y), mSourcePicture.getColor(x +
	// 1, y + 1), mSourcePicture.getColor(x, y + 1));
	// Color cAvgSW = KColor.average(mSourcePicture.getColor(x, y), mSourcePicture.getColor(x - 1, y), mSourcePicture.getColor(x -
	// 1, y - 1), mSourcePicture.getColor(x, y - 1));
	// Color cAvgSE = KColor.average(mSourcePicture.getColor(x, y), mSourcePicture.getColor(x + 1, y), mSourcePicture.getColor(x +
	// 1, y - 1), mSourcePicture.getColor(x, y - 1));
	//
	// int delta = KMath.max(delta(cAvgSW, cAvgNW), delta(cAvgNW, cAvgNE), delta(cAvgNE, cAvgSE), delta(cAvgSE, cAvgSW));
	// int deltaNE = delta(cAvgSW, cAvgNE);
	// int deltaSE = delta(cAvgNW, cAvgSE);
	//
	// if (deltaNE > deltaSE && deltaNE > delta) {
	// return getColorReshapeSE(pSourcePicture, pIn);
	// }
	// if (deltaSE > deltaNE && deltaSE > delta) {
	// return getColorReshapeNE(pSourcePicture, pIn);
	// }
	//
	// return getColorSmooth(pSourcePicture, pIn);
	//
	// }
	//
	// private Color getColorReshapeNE(final IPictureReadOnly pSourcePicture, final Point pIn) {
	// int x = (int) Math.floor(pIn.getX() * getWidth());
	// int y = (int) Math.floor(pIn.getY() * getHeight());
	//
	// double xOff = pIn.getX() * getWidth() - x;
	// double yOff = pIn.getY() * getHeight() - y;
	//
	// Color c0 = pSourcePicture.getColor(x, y);
	// Color cN = pSourcePicture.getColor(x, y + 1);
	// Color cE = pSourcePicture.getColor(x + 1, y);
	// Color cNE = pSourcePicture.getColor(x + 1, y + 1);
	//
	// if (xOff >= yOff) {
	// double delta = xOff - yOff;
	// Color c1 = KColor.fade(c0, cE, delta);
	// Color c2 = KColor.fade(cNE, cE, delta);
	// double maxLength = KMath.SQRT2 * (1.0d - delta);
	// double length = KMath.SQRT2 * yOff;
	// if (length == 0) {
	// return c1;
	// }
	// return KColor.fade(c1, c2, length / maxLength);
	// }
	//
	// double delta = yOff - xOff;
	// Color c1 = KColor.fade(c0, cN, delta);
	// Color c2 = KColor.fade(cNE, cN, delta);
	// double maxLength = KMath.SQRT2 * (1.0d - delta);
	// double length = KMath.SQRT2 * xOff;
	// if (length == 0) {
	// return c1;
	// }
	// return KColor.fade(c1, c2, length / maxLength);
	// }
	//
	// private Color getColorReshapeSE(final IPictureReadOnly pSourcePicture, final Point pIn) {
	// int x = (int) Math.floor(pIn.getX() * getWidth());
	// int y = (int) Math.floor(pIn.getY() * getHeight());
	//
	// double xOff = pIn.getX() * getWidth() - x;
	// double yOff = pIn.getY() * getHeight() - y;
	//
	// Color c0 = pSourcePicture.getColor(x, y);
	// Color cN = pSourcePicture.getColor(x, y + 1);
	// Color cE = pSourcePicture.getColor(x + 1, y);
	// Color cNE = pSourcePicture.getColor(x + 1, y + 1);
	//
	// if (xOff + yOff <= 1) {
	// double sum = xOff + yOff;
	// Color c1 = KColor.fade(c0, cE, sum);
	// Color c2 = KColor.fade(c0, cN, sum);
	// double maxLength = KMath.SQRT2 * sum;
	// double length = Math.sqrt(yOff * yOff + (sum - xOff) * (sum - xOff));
	// if (length == 0) {
	// return c1;
	// }
	// return KColor.fade(c1, c2, length / maxLength);
	// }
	//
	// double sum = xOff + yOff - 1.0d;
	// Color c1 = KColor.fade(cN, cNE, sum);
	// Color c2 = KColor.fade(cE, cNE, sum);
	// double maxLength = KMath.SQRT2 * (1.0 - sum);
	// double length = Math.sqrt((1.0d - yOff) * (1.0d - yOff) + (sum - xOff) * (sum - xOff));
	// if (length == 0) {
	// return c1;
	// }
	// return KColor.fade(c1, c2, length / maxLength);
	// }
	//
	// private Color getColorSmooth(final IPictureReadOnly pSourcePicture, final Point pIn) {
	// int x = (int) Math.floor(pIn.getX() * getWidth());
	// int y = (int) Math.floor(pIn.getY() * getHeight());
	//
	// double xOff = pIn.getX() * getWidth() - x;
	// double yOff = pIn.getY() * getHeight() - y;
	//
	// Color c0 = pSourcePicture.getColor(x, y);
	// Color cN = pSourcePicture.getColor(x, y + 1);
	// Color cE = pSourcePicture.getColor(x + 1, y);
	// Color cNE = pSourcePicture.getColor(x + 1, y + 1);
	//
	// Color x1 = KColor.fade(c0, cN, yOff);
	// Color x2 = KColor.fade(cE, cNE, yOff);
	// return KColor.fade(x1, x2, xOff);
	// }
	//
	// public String getFileName() {
	// return (mFile != null) ? mFile.getName() : null;
	// }

	// @Override
	// public int getHeight() {
	// return mSourcePicture != null ? mSourcePicture.getHeight() : 100;
	// }

	// public IPictureReadOnly getPicture() {
	// return mSourcePicture;
	// }
	//
	// @Override
	// public int getWidth() {
	// return mSourcePicture != null ? mSourcePicture.getWidth() : 100;
	// }

	@Override
	public int getWidth() {
		return mSourcePicture.getValue().getWidth();
	}

	@Override
	public void grafitti(final GrafittiHelper pGraphicsHelper) {
		if (mSharpen.getValue()) {
			pGraphicsHelper.drawLine(0.0f, 0.0f, 1.0f, 1.0f, Color.WHITE, false);
		}
	}

	public boolean isFileLoaded() {
		return mFile != null;
	}

	@Override
	public void setPreviousTransform(final ITransform pPreviousTransform) {
		throw new IllegalStateException("Cannot setPreviousTransform on an ImageLoadTransform");
	}

	@Override
	public void transform(final ITransformResult pRenderResult) {
		double x = pRenderResult.getX();
		double y = pRenderResult.getY();
		Color c = mSourcePicture.getValue().getColor(x, y);
		pRenderResult.setColor(c);
	}

	// public void open(final File pFile) throws PerceptionException {
	// mFile = pFile;
	// setPicture(new Picture(pFile));
	// }
	//
	// @Override
	// public void redrawBeforeImage(final IPicture pBeforePicture, final IProgressObserver pProgressObserver) {
	// for (int x = 0; x < pBeforePicture.getWidth(); x++) {
	// for (int y = 0; y < pBeforePicture.getHeight(); y++) {
	//
	// double px = x / (double) pBeforePicture.getWidth();
	// double py = y / (double) pBeforePicture.getHeight();
	//
	// Point point = new Point(px, py);
	//
	// Color c = mSourcePicture != null ? mSourcePicture.getColor(point) : getProperties().getOutOfBoundsColor();
	// pBeforePicture.setColor(x, y, c);
	// }
	// }
	// }
	//
	// public String relativePath(final String pPath) throws Exception {
	// Path base = (mFile != null && mFile.exists()) ? mFile.getParentFile().getCanonicalFile().toPath() : new File("").toPath();
	// Path path = new File(pPath).getCanonicalFile().toPath();
	// String relative = base.relativize(path).normalize().toString();
	// return relative;
	// }
	//
	// @Override
	// public void setInitialized() {
	// super.setInitialized();
	//
	// if (mSharpen.getBoolean()) {
	// sharpen();
	// }
	// }
	//
	// public void setPicture(final PictureType pPicture) {
	// mSourcePicture.setValue(pPicture);
	// // if (mSourcePicture != null) {
	// // mOffset = new byte[mSourcePicture.getWidth() + 1][mSourcePicture.getHeight() + 1];
	// // // reshape(mSourcePicture);
	// // }
	// }
	//
	// @Override
	// public void setValues() {
	// super.setValues();
	// if (mSourcePicture != null) {
	// mSourcePicture.set360(m360.getBoolean());
	// }
	// if (mSharpPicture != null) {
	// mSharpPicture.set360(m360.getBoolean());
	// }
	// }
	//
	// public synchronized void sharpen() {
	//
	// if (mSharpPicture == null || mSharpPicture.getWidth() != getWidth() || mSharpPicture.getHeight() != getHeight()) {
	// mSharpPicture = new Picture(getWidth(), getHeight());
	// }
	//
	// ISharpen sharpener = SharpenFactory.createInstance(this);
	// sharpener.sharpen(mSourcePicture, mSharpPicture, m360.getBoolean(), mPasses.getInt());
	// }
	//
	// @Override
	// public Color transform(final Point pIn) {
	// if (mSourcePicture == null) {
	// return getProperties().getOutOfBoundsColor();
	// }
	//
	// IPictureReadOnly picture = (mSharpen.getBoolean()) ? mSharpPicture : mSourcePicture;
	// picture = picture == null ? mSourcePicture : picture;
	//
	// if (mReshape.getBoolean()) { //
	// return getColorReshape(picture, pIn);
	// }
	//
	// if (mSmooth.getBoolean()) {
	// return getColorSmooth(picture, pIn);
	// } else {
	// return picture.getColor(pIn);
	// }
	// }
	//
	// @Override
	// public void write(final Properties pProperites, final String pId) throws Exception {
	// super.write(pProperites, pId);
	//
	// if (mFile != null) {
	// pProperites.setProperty(pId + ".filename", mFile.getName());
	// }
	// }

	@Override
	public void updatePreview() {
	}
}
