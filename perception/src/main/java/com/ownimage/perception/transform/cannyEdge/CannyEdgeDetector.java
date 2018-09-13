/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.util.Arrays;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.control.type.IPictureSource;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.transform.CannyEdgeTransform;

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
 * //apply it to an image
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
 */

public class CannyEdgeDetector implements ICannyEdgeDetector {

    // statics


    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

    private final static float GAUSSIAN_CUT_OFF = 0.005f;
    private final static float MAGNITUDE_SCALE = 100F;
    private final static float MAGNITUDE_LIMIT = 1000F;
    private final static int MAGNITUDE_MAX = (int) (MAGNITUDE_SCALE * MAGNITUDE_LIMIT);

    // fields

    private int height;
    private int width;
    private int picsize;
    private int[] data;
    private int[] magnitude;
    private IPictureSource sourceImage;

    private float gaussianKernelRadius;
    private float lowThreshold;
    private float highThreshold;
    private int gaussianKernelWidth;
    private boolean contrastNormalized;

    private float[] xConv;
    private float[] yConv;
    private float[] xGradient;
    private float[] yGradient;

    private boolean mKeepRunning;
    private PixelMap mEdgeData;
    private CannyEdgeTransform mTransform;

    // constructors

    private CannyEdgeDetector() {
        lowThreshold = 2.5f;
        highThreshold = 7.5f;
        gaussianKernelRadius = 2f;
        gaussianKernelWidth = 16;
        contrastNormalized = false;

        mKeepRunning = true;
    }

    /**
     * Constructs a new detector with default parameters.
     */

    CannyEdgeDetector(final CannyEdgeTransform pTransform) {
        this();
        mTransform = pTransform;
    }

    // accessors

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#getLowThreshold()
     */

    private void computeGradients(final float kernelRadius, final int kernelWidth) {

        // generate the gaussian convolution masks
        float kernel[] = new float[kernelWidth];
        float diffKernel[] = new float[kernelWidth];
        int kwidth;
        for (kwidth = 0; kwidth < kernelWidth; kwidth++) {
            float g1 = gaussian(kwidth, kernelRadius);
            if (g1 <= GAUSSIAN_CUT_OFF && kwidth >= 2) {
                break;
            }
            float g2 = gaussian(kwidth - 0.5f, kernelRadius);
            float g3 = gaussian(kwidth + 0.5f, kernelRadius);
            kernel[kwidth] = (g1 + g2 + g3) / 3f / (2f * (float) Math.PI * kernelRadius * kernelRadius);
            diffKernel[kwidth] = g3 - g2;
        }

        int initX = kwidth - 1;
        int maxX = width - (kwidth - 1);
        int initY = width * (kwidth - 1);
        int maxY = width * (height - (kwidth - 1));

        // perform convolution in x and y directions
        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y += width) {
                int index = x + y;
                float sumX = data[index] * kernel[0];
                float sumY = sumX;
                int xOffset = 1;
                int yOffset = width;
                for (; xOffset < kwidth; ) {
                    sumY += kernel[xOffset] * (data[index - yOffset] + data[index + yOffset]);
                    sumX += kernel[xOffset] * (data[index - xOffset] + data[index + xOffset]);
                    yOffset += width;
                    xOffset++;
                }

                yConv[index] = sumY;
                xConv[index] = sumX;
            }

        }

        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y += width) {
                float sum = 0f;
                int index = x + y;
                for (int i = 1; i < kwidth; i++) {
                    sum += diffKernel[i] * (yConv[index - i] - yConv[index + i]);
                }

                xGradient[index] = sum;
            }

        }

        for (int x = kwidth; x < width - kwidth; x++) {
            for (int y = initY; y < maxY; y += width) {
                float sum = 0.0f;
                int index = x + y;
                int yOffset = width;
                for (int i = 1; i < kwidth; i++) {
                    sum += diffKernel[i] * (xConv[index - yOffset] - xConv[index + yOffset]);
                    yOffset += width;
                }

                yGradient[index] = sum;
            }

        }

        initX = kwidth;
        maxX = width - kwidth;
        initY = width * kwidth;
        maxY = width * (height - kwidth);
        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y += width) {
                int index = x + y;
                int indexN = index - width;
                int indexS = index + width;
                int indexW = index - 1;
                int indexE = index + 1;
                int indexNW = indexN - 1;
                int indexNE = indexN + 1;
                int indexSW = indexS - 1;
                int indexSE = indexS + 1;

                float xGrad = xGradient[index];
                float yGrad = yGradient[index];
                float gradMag = hypot(xGrad, yGrad);

                // perform non-maximal supression
                float nMag = hypot(xGradient[indexN], yGradient[indexN]);
                float sMag = hypot(xGradient[indexS], yGradient[indexS]);
                float wMag = hypot(xGradient[indexW], yGradient[indexW]);
                float eMag = hypot(xGradient[indexE], yGradient[indexE]);
                float neMag = hypot(xGradient[indexNE], yGradient[indexNE]);
                float seMag = hypot(xGradient[indexSE], yGradient[indexSE]);
                float swMag = hypot(xGradient[indexSW], yGradient[indexSW]);
                float nwMag = hypot(xGradient[indexNW], yGradient[indexNW]);
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
                    magnitude[index] = gradMag >= MAGNITUDE_LIMIT ? MAGNITUDE_MAX : (int) (MAGNITUDE_SCALE * gradMag);
                    // NOTE: The orientation of the edge is not employed by this
                    // implementation. It is a simple matter to compute it at
                    // this point as: Math.atan2(yGrad, xGrad);
                } else {
                    magnitude[index] = 0;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setLowThreshold(float)
     */

    @Override
    public void dispose() {
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#getHighThreshold()
     */

    private void follow(final int x1, final int y1, final int i1, final int threshold) {
        int x0 = x1 == 0 ? x1 : x1 - 1;
        int x2 = x1 == width - 1 ? x1 : x1 + 1;
        int y0 = y1 == 0 ? y1 : y1 - 1;
        int y2 = y1 == height - 1 ? y1 : y1 + 1;

        data[i1] = magnitude[i1];
        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                int i2 = x + y * width;
                if ((y != y1 || x != x1) && data[i2] == 0 && magnitude[i2] >= threshold) {
                    follow(x, y, i2, threshold);
                    return;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setHighThreshold(float)
     */

    private float gaussian(final float x, final float sigma) {
        return (float) Math.exp(-(x * x) / (2f * sigma * sigma));
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#getGaussianKernelWidth()
     */

    @Override
    public PixelMap getEdgeData() {
        return mEdgeData;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setGaussianKernelWidth(int)
     */

    @Override
    public float getGaussianKernelRadius() {
        return gaussianKernelRadius;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#getGaussianKernelRadius()
     */

    @Override
    public int getGaussianKernelWidth() {
        return gaussianKernelWidth;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setGaussianKernelRadius(float)
     */

    @Override
    public float getHighThreshold() {
        return highThreshold;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#isContrastNormalized()
     */

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#getKeepRunning()
     */
    @Override
    public boolean getKeepRunning() {
        return mKeepRunning;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setContrastNormalized(boolean)
     */

    @Override
    public float getLowThreshold() {
        return lowThreshold;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#getSourceImage()
     */

    @Override
    public IPictureSource getSourceImage() {
        return sourceImage;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setSourceImage(com.ownimage.framework.util.IPictureReadOnly)
     */

    // NOTE: It is quite feasible to replace the implementation of this method
    // with one which only loosely approximates the hypot function. I've tested
    // simple approximations such as Math.abs(x) + Math.abs(y) and they work fine.
    private float hypot(final float x, final float y) {
        return (float) Math.hypot(x, y);
    }

    // methods

    private void initArrays() {
        if (data == null || picsize != data.length) {
            data = new int[picsize];
            magnitude = new int[picsize];

            xConv = new float[picsize];
            yConv = new float[picsize];
            xGradient = new float[picsize];
            yGradient = new float[picsize];
        }
    }

    // private utility methods

    @Override
    public boolean isContrastNormalized() {
        return contrastNormalized;
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

    private int luminance(final float r, final float g, final float b) {
        return Math.round(0.299f * r + 0.587f * g + 0.114f * b);
    }

    private void normalizeContrast() {
        int[] histogram = new int[256];
        for (int i = 0; i < data.length; i++) {
            histogram[data[i]]++;
        }
        int[] remap = new int[256];
        int sum = 0;
        int j = 0;
        for (int i = 0; i < histogram.length; i++) {
            sum += histogram[i];
            int target = sum * 255 / picsize;
            for (int k = j + 1; k <= target; k++) {
                remap[k] = i;
            }
            j = target;
        }

        for (int i = 0; i < data.length; i++) {
            data[i] = remap[data[i]];
        }
    }

    private void performHysteresis(final int low, final int high) {
        // NOTE: this implementation reuses the data array to store both
        // luminance data from the image, and edge intensity from the processing.
        // This is done for memory efficiency, other implementations may wish
        // to separate these functions.
        Arrays.fill(data, 0);

        int offset = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (data[offset] == 0 && magnitude[offset] >= high) {
                    follow(x, y, offset, low);
                }
                offset++;
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#getEdgeData()
     */

    public void process() {
        // StopWatch stopWatch = new StopWatch(mLogger);

        width = sourceImage.getWidth();
        height = sourceImage.getHeight();
        picsize = width * height;
        initArrays();
        // stopWatch.logLapTime(Level.INFO, "initArrays");

        readLuminance();
        // stopWatch.logLapTime(Level.INFO, "readLuminance");

        if (contrastNormalized) {
            normalizeContrast();
        }
        // stopWatch.logLapTime(Level.INFO, "normalizeContrast");

        computeGradients(gaussianKernelRadius, gaussianKernelWidth);
        // stopWatch.logLapTime(Level.INFO, "computeGradients");

        int low = Math.round(lowThreshold * MAGNITUDE_SCALE);
        int high = Math.round(highThreshold * MAGNITUDE_SCALE);
        performHysteresis(low, high);
        // stopWatch.logLapTime(Level.INFO, "performHysteresis");

        thresholdEdges();
        // stopWatch.logLapTime(Level.INFO, "thresholdEdges");

        writeEdges(data);
        // stopWatch.logLapTime(Level.INFO, "writeEdges");
        // stopWatch.logElapsedTime(Level.INFO, "Process");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setEdgeData(com.ownimage.perception.pixelMap.PixelMap)
     */

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#process(boolean)
     */
    @Override
    public void process(final IProgressObserver pProgressObserver) {
        process();
    }

    private void readLuminance() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                Color c = sourceImage.getColor(x, y).orElseGet(() -> Services.getServices().getProperties().getColorOOB());
                int r = c.getRed();
                int g = c.getGreen();
                int b = c.getBlue();
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
        // byte[] pixels = (byte[]) sourceImage.getData().getDataElements(0, 0, width, height, null);
        // int offset = 0;
        // for (int i = 0; i < picsize; i++) {
        // int b = pixels[offset++] & 0xff;
        // int g = pixels[offset++] & 0xff;
        // int r = pixels[offset++] & 0xff;
        // data[i] = luminance(r, g, b);
        // }
        // } else {
        // throw new IllegalArgumentException("Unsupported image type: " + type);
        // }
    }

    @Override
    public void setContrastNormalized(final boolean contrastNormalized) {
        this.contrastNormalized = contrastNormalized;
    }

    private void setData(final int pX, final int pY, final int pValue) {
        int index = pX + pY * width;
        data[index] = pValue;
    }

    @Override
    public void setEdgeData(final PixelMap edgeData) {
        mEdgeData = edgeData;
    }

    @Override
    public void setGaussianKernelRadius(final float gaussianKernelRadius) {
        if (gaussianKernelRadius < 0.1f) {
            throw new IllegalArgumentException();
        }
        this.gaussianKernelRadius = gaussianKernelRadius;
    }

    @Override
    public void setGaussianKernelWidth(final int gaussianKernelWidth) {
        if (gaussianKernelWidth < 2) {
            throw new IllegalArgumentException();
        }
        this.gaussianKernelWidth = gaussianKernelWidth;
    }

    @Override
    public void setHighThreshold(final float threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException();
        }
        highThreshold = threshold;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.perception.transform.cannyEdge.ICannyEdgeDetector#setKeepRunning(boolean)
     */
    @Override
    public void setKeepRunning(final boolean pKeepRunning) {
        mKeepRunning = pKeepRunning;
    }

    @Override
    public void setLowThreshold(final float threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException();
        }
        lowThreshold = threshold;
    }

    @Override
    public void setSourceImage(final IPictureSource image) {
        sourceImage = image;
    }

    private void thresholdEdges() {
        for (int i = 0; i < picsize; i++) {
            data[i] = data[i] > 0 ? -1 : 0xff000000;
        }
    }

    private void writeEdges(final int pixels[]) {
        if (mEdgeData == null || mEdgeData.getWidth() != width || mEdgeData.getHeight() != height) {
            mEdgeData = new PixelMap(width, height, true, mTransform); // TODO needs to come from m360 value
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x + y * width;
                boolean edge = pixels[index] == -1;
                mEdgeData.getPixelAt(x, y).setEdge(edge);
            }
        }

    }
}
