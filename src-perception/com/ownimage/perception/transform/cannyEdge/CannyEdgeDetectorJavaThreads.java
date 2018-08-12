/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.IPictureSource;

/** note that this has been changed to work with com.ownimage.perception
 * Keith Hart, ownimage, 2013
 */

/**
 * <p>
 * <em>This software is an adaptation of the CanyEdgeDetection algorithm based on the work of Tom Gibara.  Specifically there are several versions of this including, but not limited to:
 * 1) the original algorithm with the input and output images slightly altered
 * 2) a modification to use a Java thread pool
 * 3) a modification to use a OpenCL thread pool
 * 
 * <strong>Please read the notes in this source file for additional information.
 * </strong></em>
 * </p>
 * 
 * <p>
 * This class provides a configurable implementation of the Canny edge detection algorithm. This classic algorithm has a number of
 * shortcomings, but remains an effective tool in many scenarios. <em>This class is designed
 * for single threaded use only.</em>
 * </p>
 * 
 * <p>
 * Sample usage:
 * </p>
 * 
 * <pre>
 * <code>
 * //create the detector
 * CannyEdgeDetectorJavaThreads detector = new CannyEdgeDetectorJavaThreads();
 * //adjust its parameters as desired
 * detector.setLowThreshold(0.5f);
 * detector.setHighThreshold(1f);
 * //apply it to an mData
 * detector.setSourceImage(frame);
 * detector.process();
 * BufferedImage edges = detector.getEdgesImage();
 * </code>
 * </pre>
 * 
 * <p>
 * For a more complete understanding of this edge detector's parameters consult an explanation of the algorithm.
 * </p>
 * 
 * @author Tom Gibara
 * 
 */

public class CannyEdgeDetectorJavaThreads implements ICannyEdgeDetector {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
    private final static Logger mLogger = Framework.getLogger();

	// statics

	private final static float GAUSSIAN_CUT_OFF = 0.005f;
	private final static float MAGNITUDE_SCALE = 100F;
	private final static float MAGNITUDE_LIMIT = 1000F;
	private final static int MAGNITUDE_MAX = (int) (MAGNITUDE_SCALE * MAGNITUDE_LIMIT);

	// fields

	private int height;
	private int width;
	private int picsize;
	private int[][] mData;
	private int[][] mMagnitude;
	private IPictureSource sourceImage;
	private PixelMap edgeData;

	private float gaussianKernelRadius;
	private float lowThreshold;
	private float highThreshold;
	private int gaussianKernelWidth;
	private boolean contrastNormalized;

	private float[][] mXConv;
	private float[][] mYConv;
	private float[][] mXGradient;
	private float[][] mYGradient;

	private final CannyEdgeTransform mTransform;
	// private final ProgressMonitor mProgressMonitor;
	private boolean mShowProgress;
	private boolean mKeepRunning;

	// constructors

	/**
	 * Constructs a new detector with default parameters.
	 */

	CannyEdgeDetectorJavaThreads(final CannyEdgeTransform pTransform) {
		mTransform = pTransform;
		// mProgressMonitor = new ProgressMonitor(pTransform);
		// mProgressMonitor //
		// .addInterval("initArrays", 0) //
		// .addInterval("readLuminance", 4) //
		// .addInterval("normalizeContrast", 10) //
		// .addInterval("computeGradients", 96) //
		// .addInterval("performHysteresis", 97) //
		// .addInterval("thresholdEdges", 98) //
		// .addInterval("writeEdges", 100);

		lowThreshold = 2.5f;
		highThreshold = 7.5f;
		gaussianKernelRadius = 2f;
		gaussianKernelWidth = 16;
		contrastNormalized = false;
	}

	// accessors

	private void computeGradients(final float kernelRadius, final int kernelWidth) {

		// generate the gaussian convolution masks
		final float kernel[] = new float[kernelWidth];
		final float diffKernel[] = new float[kernelWidth];
		int kwidth;
		for (kwidth = 0; kwidth < kernelWidth; kwidth++) {
			final float g1 = gaussian(kwidth, kernelRadius);
			if (g1 <= GAUSSIAN_CUT_OFF && kwidth >= 2) {
				break;
			}
			final float g2 = gaussian(kwidth - 0.5f, kernelRadius);
			final float g3 = gaussian(kwidth + 0.5f, kernelRadius);
			kernel[kwidth] = (g1 + g2 + g3) / 3f / (2f * (float) Math.PI * kernelRadius * kernelRadius);
			diffKernel[kwidth] = g3 - g2;
		}

		int initX = kwidth - 1;
		int maxX = width - (kwidth - 1);
		int initY = kwidth - 1;
		int maxY = height - (kwidth - 1);

		// perform convolution in mX and mY directions
		// TODO need to check this against the algorithm
		for (int x = 0; x < width; x++) {
			showProgressBar("computeGradients", 0.1f * (x - initX) / (maxX - initX));
			for (int y = initY; y < maxY; y++) {
				float sumX = getData(x, y) * kernel[0];
				float sumY = sumX;
				int xOffset = 1;
				int yOffset = 1;
				for (; xOffset < kwidth;) {
					sumY += kernel[xOffset] * (getData(x, y - yOffset) + getData(x, y + yOffset));
					sumX += kernel[xOffset] * (getData(x - xOffset, y) + getData(x + xOffset, y));
					yOffset++;
					xOffset++;
				}

				setXConv(x, y, sumX);
				setYConv(x, y, sumY);
			}

		}

		for (int x = 0; x < width; x++) {
			showProgressBar("computeGradients", 0.1f + 0.1f * (x - initX) / (maxX - initX));
			for (int y = initY; y < maxY; y++) {
				float sum = 0f;
				for (int i = 1; i < kwidth; i++) {
					sum += diffKernel[i] * (getYConv(x - i, y) - getYConv(x + i, y));
				}

				setXGradient(x, y, sum);
			}

		}

		for (int x = 0; x < width - 1; x++) {
			showProgressBar("computeGradients", 0.2f + 0.1f * (x - kwidth) / (width - 2 * kwidth));
			for (int y = initY; y < maxY; y++) {
				float sum = 0.0f;
				int yOffset = 1;
				for (int i = 1; i < kwidth; i++) {
					sum += diffKernel[i] * (getXConv(x, y - yOffset) - getXConv(x, y + yOffset));
					yOffset++;
				}

				setYGradient(x, y, sum);
			}

		}

		initX = 0;// kwidth;
		maxX = width - 1;// kwidth;
		initY = kwidth;
		maxY = height - kwidth;
		final Properties properties = Perception.getPerception().getProperties();

		class NonMaximalSupressionThread extends Thread {
			private final int mInitX;
			private final int mMaxX;
			private final int mInitY;
			private final int mMaxY;
			private final int mStepX;

			public NonMaximalSupressionThread(final int pInitX, final int pMaxX, final int pInitY, final int pMaxY, final int pStepX) {
				super();
				mInitX = pInitX;
				mMaxX = pMaxX;
				mInitY = pInitY;
				mMaxY = pMaxY;
				mStepX = pStepX;
			}

			@Override
			public void run() {
				CannyEdgeDetectorJavaThreads.this.nonMaximalSupression(mInitX, mMaxX, mInitY, mMaxY, mStepX);
			}
		}

		if (properties.useJTP()) {
			int threadCount = properties.getRenderThreadPoolSize(); // TODO looks like this could be better
			final Thread threads[] = new Thread[threadCount];

			for (int i = 0; i < threadCount; i++) {
				NonMaximalSupressionThread runnable;
				runnable = new NonMaximalSupressionThread(initX + i, maxX, initY, maxY, threadCount);
				runnable.setName("CannyEdgeDetectorJavaThreads NMS Thread: " + i);
				runnable.start();
				threads[i] = runnable;
			}

			for (int i = 0; i < threadCount; i++) {
				try {
					threads[i].join();
				} catch (final InterruptedException pEx) {
					mLogger.warning("Interruped");
					if (mLogger.isLoggable(Level.FINE)) {
						mLogger.fine(FrameworkLogger.throwableToString(pEx));
					}
				}
			}
		} else {
			nonMaximalSupression(initX, maxX, initY, maxY, 1);
		}

	}

	@Override
	public void dispose() {
	}

	private void follow(final int x1, final int y1, final int i1, final int threshold) {
		final int x0 = x1 == 0 ? x1 : x1 - 1;
		final int x2 = x1 == width - 1 ? x1 : x1 + 1;
		final int y0 = y1 == 0 ? y1 : y1 - 1;
		final int y2 = y1 == height - 1 ? y1 : y1 + 1;

		setData(x1, y1, getMagnitude(x1, y1));
		for (int x = x0; x <= x2; x++) {
			for (int y = y0; y <= y2; y++) {
				final int i2 = x + y * width;
				if ((y != y1 || x != x1) && getData(x, y) == 0 && getMagnitude(x, y) >= threshold) {
					follow(x, y, i2, threshold);
					return;
				}
			}
		}
	}

	private float gaussian(final float x, final float sigma) {
		return (float) Math.exp(-(x * x) / (2f * sigma * sigma));
	}

	private int getData(final int pX, final int pY) {
		return mData[getX(pX)][pY];
	}

	/**
	 * Obtains an mData containing the edges detected during the last call to the process method. The buffered mData is an opaque
	 * mData of type BufferedImage.TYPE_INT_ARGB in which edge pixels are white and all other pixels are black.
	 * 
	 * @return an mData containing the detected edges, or null if the process method has not yet been called.
	 */

	@Override
	public PixelMap getEdgeData() {
		return edgeData;
	}

	/**
	 * The radius of the Gaussian convolution kernel used to smooth the source mData prior to gradient calculation. The default
	 * value is 16.
	 * 
	 * @return the Gaussian kernel radius in pixels
	 */

	@Override
	public float getGaussianKernelRadius() {
		return gaussianKernelRadius;
	}

	/**
	 * The number of pixels across which the Gaussian kernel is applied. The default value is 16.
	 * 
	 * @return the radius of the convolution operation in pixels
	 */

	@Override
	public int getGaussianKernelWidth() {
		return gaussianKernelWidth;
	}

	/**
	 * The high threshold for hysteresis. The default value is 7.5.
	 * 
	 * @return the high hysteresis threshold
	 */

	@Override
	public float getHighThreshold() {
		return highThreshold;
	}

	@Override
	public boolean getKeepRunning() {
		return mKeepRunning;
	}

	/**
	 * The low threshold for hysteresis. The default value is 2.5.
	 * 
	 * @return the low hysteresis threshold
	 */

	@Override
	public float getLowThreshold() {
		return lowThreshold;
	}

	private int getMagnitude(final int pX, final int pY) {
		return mMagnitude[getX(pX)][pY];
	}

	/**
	 * The mData that provides the luminance data used by this detector to generate edges.
	 * 
	 * @return the source mData, or null
	 */

	@Override
	public IPictureSource getSourceImage() {
		return sourceImage;
	}

	private int getX(final int pX) {
		return pX < 0 ? 0 : pX > width - 1 ? width - 1 : pX;
	}

	// methods

	private float getXConv(final int pX, final int pY) {
		return mXConv[getX(pX)][pY];
	}

	// private utility methods

	private float getXGradient(final int pX, final int pY) {
		return mXGradient[getX(pX)][pY];
	}

	// NOTE: The elements of the method below (specifically the technique for
	// non-maximal suppression and the technique for gradient computation)
	// are derived from an implementation posted in the following forum (with the
	// clear intent of others using the code):
	// http://forum.java.sun.com/thread.jspa?threadID=546211&start=45&tstart=0
	// My code effectively mimics the algorithm exhibited above.
	// Since I don't know the providence of the code that was posted it is a
	// possibility (though I think a very remote one) that this code violates
	// someone's intellectual property rights. If this concerns you feel free to
	// contact me for an alternative, though less efficient, implementation.

	private float getYConv(final int pX, final int pY) {
		return mYConv[getX(pX)][pY];
	}

	private float getYGradient(final int pX, final int pY) {
		return mYGradient[getX(pX)][pY];
	}

	// NOTE: It is quite feasible to replace the implementation of this method
	// with one which only loosely approximates the hypot function. I've tested
	// simple approximations such as Math.abs(mX) + Math.abs(mY) and they work fine.
	private float hypot(final float x, final float y) {
		return (float) Math.hypot(x, y);
	}

	private void initArrays() {
		if (mData == null || mData.length != width || mData[0].length != height) {
			mData = new int[width][height];
			mMagnitude = new int[width][height];

			mXConv = new float[width][height];
			mYConv = new float[width][height];
			mXGradient = new float[width][height];
			mYGradient = new float[width][height];
		}
	}

	/**
	 * Whether the luminance data extracted from the source mData is normalized by linearizing its histogram prior to edge
	 * extraction. The default value is false.
	 * 
	 * @return whether the contrast is normalized
	 */

	@Override
	public boolean isContrastNormalized() {
		return contrastNormalized;
	}

	private int luminance(final float r, final float g, final float b) {
		return Math.round(0.299f * r + 0.587f * g + 0.114f * b);
	}

	private void nonMaximalSupression(final int pInitX, final int pMaxX, final int pInitY, final int pMaxY, final int pStepX) {
		for (int y = pInitY; y < pMaxY; y++) {
			showProgressBar("computeGradients", 0.3f + 0.7f * (y - pInitY) / (pMaxY - pInitY));
			for (int x = pInitX; x < pMaxX; x += pStepX) {

				final float xGrad = getXGradient(x, y);
				final float yGrad = getYGradient(x, y);
				final float gradMag = hypot(xGrad, yGrad);

				// perform non-maximal supression
				final float nMag = hypot(getXGradient(x, y - 1), getYGradient(x, y - 1));
				final float sMag = hypot(getXGradient(x, y + 1), getYGradient(x, y + 1));
				final float wMag = hypot(getXGradient(x - 1, y), getYGradient(x - 1, y));
				final float eMag = hypot(getXGradient(x + 1, y), getYGradient(x + 1, y));
				final float neMag = hypot(getXGradient(x + 1, y - 1), getYGradient(x + 1, y - 1));
				final float seMag = hypot(getXGradient(x + 1, y + 1), getYGradient(x + 1, y + 1));
				final float swMag = hypot(getXGradient(x - 1, y + 1), getYGradient(x - 1, y + 1));
				final float nwMag = hypot(getXGradient(x - 1, y - 1), getYGradient(x - 1, y - 1));
				float tmp;
				/*
				 * An explanation of what's happening here, for those who want to understand the source: This performs the
				 * "non-maximal supression" phase of the Canny edge detection in which we need to compare the gradient magnitude to
				 * that in the direction of the gradient; only if the value is a local maximum do we consider the point as an edge
				 * candidate.
				 * 
				 * We need to break the comparison into a number of different cases depending on the gradient direction so that the
				 * appropriate values can be used. To avoid computing the gradient direction, we use two simple comparisons: first
				 * we check that the partial derivatives have the same sign (1) and then we check which is larger (2). As a
				 * consequence, we have reduced the problem to one of four identical cases that each test the central gradient
				 * magnitude against the values at two points with 'identical support'; what this means is that the geometry
				 * required to accurately interpolate the magnitude of gradient function at those points has an identical geometry
				 * (upto right-angled-rotation/reflection).
				 * 
				 * When comparing the central gradient to the two interpolated values, we avoid performing any divisions by
				 * multiplying both sides of each inequality by the greater of the two partial derivatives. The common comparand is
				 * stored in a temporary variable (3) and reused in the mirror case (4).
				 */
				if (xGrad * yGrad <= 0 /* (1) */
						? Math.abs(xGrad) >= Math.abs(yGrad) /* (2) */
								? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * neMag - (xGrad + yGrad) * eMag) /* (3) */
										&& tmp > Math.abs(yGrad * swMag - (xGrad + yGrad) * wMag) /* (4) */
								: (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * neMag - (yGrad + xGrad) * nMag) /* (3) */
										&& tmp > Math.abs(xGrad * swMag - (yGrad + xGrad) * sMag) /* (4) */
						: Math.abs(xGrad) >= Math.abs(yGrad) /* (2) */
								? (tmp = Math.abs(xGrad * gradMag)) >= Math.abs(yGrad * seMag + (xGrad - yGrad) * eMag) /* (3) */
										&& tmp > Math.abs(yGrad * nwMag + (xGrad - yGrad) * wMag) /* (4) */
								: (tmp = Math.abs(yGrad * gradMag)) >= Math.abs(xGrad * seMag + (yGrad - xGrad) * sMag) /* (3) */
										&& tmp > Math.abs(xGrad * nwMag + (yGrad - xGrad) * nMag) /* (4) */
				) {
					setMagnitude(x, y, gradMag >= MAGNITUDE_LIMIT ? MAGNITUDE_MAX : (int) (MAGNITUDE_SCALE * gradMag));
					// NOTE: The orientation of the edge is not employed by this
					// implementation. It is a simple matter to compute it at
					// this point as: Math.atan2(yGrad, xGrad);
				} else {
					setMagnitude(x, y, 0);
				}
			}
		}
	}

	private void normalizeContrast() {
		final long[] histogram = new long[256];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				histogram[getData(x, y)]++;
			}
		}

		final long[] remap = new long[256];
		long sum = 0;
		long j = 0;
		for (int i = 0; i < histogram.length; i++) {
			sum += histogram[i];
			final long target = sum * 255 / picsize;
			for (long k = j + 1; k <= target; k++) {
				remap[(int) k] = i;
			}
			j = target;
		}

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				setData(x, y, (int) remap[getData(x, y)]);
			}
		}
	}

	private void performHysteresis(final int low, final int high) {
		// NOTE: this implementation reuses the data array to store both
		// luminance data from the mData, and edge intensity from the processing.
		// This is done for memory efficiency, other implementations may wish
		// to separate these functions.
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				setData(x, y, 0);
			}
		}
		;

		int offset = 0;
		for (int y = 0; y < height; y++) {
			showProgressBar("performHysteresis", (float) y / height);
			for (int x = 0; x < width; x++) {

				if (getData(x, y) == 0 && getMagnitude(x, y) >= high) {
					follow(x, y, offset, low);
				}
				offset++;
			}
		}
	}

	@Override
	public void process(final boolean pShowProgress) {
		// final StopWatch stopWatch = new StopWatch(mLogger);
		setShowProgress(pShowProgress);
		setKeepRunning(true);

		try {
			// if (pShowProgress) {
			// mTransform.showProgressBar();
			// }

			width = sourceImage.getWidth();
			height = sourceImage.getHeight();
			picsize = width * height;
			initArrays();
			// stopWatch.logLapTime(Level.INFO, "initArrays");

			if (getKeepRunning()) {
				readLuminance();
			}
			// stopWatch.logLapTime(Level.INFO, "readLuminance");

			if (getKeepRunning()) {
				if (contrastNormalized) {
					normalizeContrast();
				}
			}
			// stopWatch.logLapTime(Level.INFO, "normalizeContrast");

			if (getKeepRunning()) {
				computeGradients(gaussianKernelRadius, gaussianKernelWidth);
			}
			// stopWatch.logLapTime(Level.INFO, "computeGradients");

			if (getKeepRunning()) {
				final int low = Math.round(lowThreshold * MAGNITUDE_SCALE);
				final int high = Math.round(highThreshold * MAGNITUDE_SCALE);
				performHysteresis(low, high);
			}
			// stopWatch.logLapTime(Level.INFO, "performHysteresis");

			if (getKeepRunning()) {
				thresholdEdges();
			}
			// stopWatch.logLapTime(Level.INFO, "thresholdEdges");

			if (getKeepRunning()) {
				writeEdges(mData);
			}
			// stopWatch.logLapTime(Level.INFO, "writeEdges");
			// stopWatch.logElapsedTime(Level.INFO, "Process");

			// mProgressMonitor.log();
		} finally {
			// if (pShowProgress) {
			// mTransform.hideProgressBar();
			// }
		}
	}

	private void readLuminance() {
		for (int x = 0; x < width; x++) {
			if (!showProgressBar("readLuminance", (float) x / width)) { return; }
			for (int y = 0; y < height; y++) {

				final Color c = sourceImage.getColor(x, y);
				final int r = c.getRed();
				final int g = c.getGreen();
				final int b = c.getBlue();
				setData(x, y, luminance(r, g, b));
			}
		}

		// int type = sourceImage.getType();
		// if (type == BufferedImage.TYPE_INT_RGB || type == BufferedImage.TYPE_INT_ARGB) {
		// int[] pixels = (int[]) sourceImage.getData().getDataElements(0, 0, width, height, null);
		// for (int i = 0; i < picsize; i++) {
		// int p = pixels[i];
		// int r = (p & 0xff0000) >> 16;
		// int g = (p & 0xff00) >> 8;
		// int b = p & 0xff;
		// data[i] = luminance(r, g, b);
		// }
		// } else if (type == BufferedImage.TYPE_BYTE_GRAY) {
		// byte[] pixels = (byte[]) sourceImage.getData().getDataElements(0, 0, width, height, null);
		// for (int i = 0; i < picsize; i++) {
		// data[i] = (pixels[i] & 0xff);
		// }
		// } else if (type == BufferedImage.TYPE_USHORT_GRAY) {
		// short[] pixels = (short[]) sourceImage.getData().getDataElements(0, 0, width, height, null);
		// for (int i = 0; i < picsize; i++) {
		// data[i] = (pixels[i] & 0xffff) / 256;
		// }
		// } else if (type == BufferedImage.TYPE_3BYTE_BGR) {
		// // byte[] pixels = (byte[]) sourceImage.getData().getDataElements(0, 0, width, height, null);
		// // int offset = 0;
		// // for (int i = 0; i < picsize; i++) {
		// // int b = pixels[offset++] & 0xff;
		// // int g = pixels[offset++] & 0xff;
		// // int r = pixels[offset++] & 0xff;
		// // data[i] = luminance(r, g, b);
		//
		// // 20121204 Changed ownimage
		// // changed to use a less memory intensive algo to prevent out of memory errors
		// for (int mX = 0; mX < width; mX++)
		// for (int mY = 0; mY < height; mY++) {
		// int offset = mX + mY * width;
		// Color c = new Color(sourceImage.getRGB(mX, mY));
		// data[offset] = luminance(c.getRed(), c.getGreen(), c.getBlue());
		// }
		//
		// } else {
		// throw new IllegalArgumentException("Unsupported mData type: " + type);
		// }
	}

	/**
	 * Sets whether the contrast is normalized
	 * 
	 * @param contrastNormalized
	 *            true if the contrast should be normalized, false otherwise
	 */

	@Override
	public void setContrastNormalized(final boolean contrastNormalized) {
		this.contrastNormalized = contrastNormalized;
	}

	private void setData(final int pX, final int pY, final int pValue) {
		mData[pX][pY] = pValue;
	}

	/**
	 * Sets the edges mData. Calling this method will not change the operation of the edge detector in any way. It is intended to
	 * provide a means by which the memory referenced by the detector object may be reduced.
	 * 
	 * @param edgeData
	 *            expected (though not required) to be null
	 */

	@Override
	public void setEdgeData(final PixelMap edgeData) {
		this.edgeData = edgeData;
	}

	/**
	 * Sets the radius of the Gaussian convolution kernel used to smooth the source mData prior to gradient calculation.
	 * 
	 * @return a Gaussian kernel radius in pixels, must exceed 0.1f.
	 */

	@Override
	public void setGaussianKernelRadius(final float gaussianKernelRadius) {
		if (gaussianKernelRadius < 0.1f) { throw new IllegalArgumentException(); }
		this.gaussianKernelRadius = gaussianKernelRadius;
	}

	/**
	 * The number of pixels across which the Gaussian kernel is applied. This implementation will reduce the radius if the
	 * contribution of pixel values is deemed negligable, so this is actually a maximum radius.
	 * 
	 * @param gaussianKernelWidth
	 *            a radius for the convolution operation in pixels, at least 2.
	 */

	@Override
	public void setGaussianKernelWidth(final int gaussianKernelWidth) {
		if (gaussianKernelWidth < 2) { throw new IllegalArgumentException(); }
		this.gaussianKernelWidth = gaussianKernelWidth;
	}

	/**
	 * Sets the high threshold for hysteresis. Suitable values for this parameter must be determined experimentally for each
	 * application. It is nonsensical (though not prohibited) for this value to be less than the low threshold value.
	 * 
	 * @param threshold
	 *            a high hysteresis threshold
	 */

	@Override
	public void setHighThreshold(final float threshold) {
		if (threshold < 0) { throw new IllegalArgumentException(); }
		highThreshold = threshold;
	}

	@Override
	public void setKeepRunning(final boolean pKeepRunning) {
		mKeepRunning = pKeepRunning;
	}

	/**
	 * Sets the low threshold for hysteresis. Suitable values for this parameter must be determined experimentally for each
	 * application. It is nonsensical (though not prohibited) for this value to exceed the high threshold value.
	 * 
	 * @param threshold
	 *            a low hysteresis threshold
	 */

	@Override
	public void setLowThreshold(final float threshold) {
		if (threshold < 0) { throw new IllegalArgumentException(); }
		lowThreshold = threshold;
	}

	private void setMagnitude(final int pX, final int pY, final int pValue) {
		mMagnitude[pX][pY] = pValue;
	}

	private void setShowProgress(final boolean pShowProgress) {
		mShowProgress = pShowProgress;
	}

	/**
	 * Specifies the mData that will provide the luminance data in which edges will be detected. A source mData must be set before
	 * the process method is called.
	 * 
	 * @param mData
	 *            a source of luminance data
	 */

	@Override
	public void setSourceImage(final IPictureSource image) {
		sourceImage = image;
	}

	private void setXConv(final int pX, final int pY, final float pValue) {
		mXConv[pX][pY] = pValue;
	}

	private void setXGradient(final int pX, final int pY, final float pValue) {
		mXGradient[pX][pY] = pValue;
	}

	private void setYConv(final int pX, final int pY, final float pValue) {
		mYConv[pX][pY] = pValue;
	}

	private void setYGradient(final int pX, final int pY, final float pValue) {
		mYGradient[pX][pY] = pValue;
	}

	private boolean showProgress() {
		return mShowProgress;
	}

	/**
	 * Show progress bar.
	 * 
	 * @param pSection
	 *            the section
	 * @param pFraction
	 *            the fraction
	 * @return true, if ok to continue, false means stop processing thread ASAP
	 */
	private boolean showProgressBar(final String pSection, final float pFraction) {
		// if (showProgress() && mProgressMonitor != null) {
		// mProgressMonitor.setProgressBar(pSection, pFraction);
		// }
		return mKeepRunning;
	}

	private void thresholdEdges() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				setData(x, y, getData(x, y) > 0 ? -1 : 0xff000000);
			}
		}
	}

	private void writeEdges(final int pixels[][]) {
		// NOTE: There is currently no mechanism for obtaining the edge data
		// in any other format other than an INT_ARGB type BufferedImage.
		// This may be easily remedied by providing alternative accessors.
		edgeData = new PixelMap(width, height, true, mTransform); // TODO needs to come from m360 value

		for (int x = 0; x < width; x++) {
			if (!showProgressBar("writeEdges", (float) x / width)) { return; }
			for (int y = 0; y < height; y++) {

				final boolean col = pixels[x][y] == -1;
				// Color c = new Color(col);
				edgeData.getPixelAt(x, y).setEdge(col);
			}
		}

	}

}