/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import com.aparapi.Range;
import com.aparapi.internal.kernel.KernelManager;
import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.control.type.IPictureSource;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.transform.CannyEdgeTransform;
import lombok.val;

import java.awt.*;
import java.util.Arrays;
import java.util.logging.Logger;

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

public class CannyEdgeDetectorOpenCL implements ICannyEdgeDetector {

    // statics
    private static PixelMapService pixelMapService =
            com.ownimage.perception.pixelMap.services.Services.getDefaultServices().getPixelMapService();


    protected final static float GAUSSIAN_CUT_OFF = 0.005f;
    protected final static float MAGNITUDE_SCALE = 100F;
    protected final static float MAGNITUDE_LIMIT = 1000F;
    protected final static int MAGNITUDE_MAX = (int) (MAGNITUDE_SCALE * MAGNITUDE_LIMIT);
    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

    // fields
    protected int height;
    protected int width;
    protected int picsize;
    protected int[] data;
    protected int[] magnitude;
    protected IPictureSource sourceImage;

    protected float gaussianKernelRadius;
    protected float lowThreshold;
    protected float highThreshold;
    protected int gaussianKernelWidth;
    protected boolean contrastNormalized;

    protected float[] xConv;
    protected float[] yConv;
    protected float[] xGradient;
    protected float[] yGradient;

    protected int[] mXYStart;

    protected boolean mKeepRunning;
    protected ImmutablePixelMapData mEdgeData;
    private CannyEdgeTransform mTransform;

    // constructors

    private CannyEdgeDetectorOpenCL() {
        mLogger.info(() -> "Kernel created");

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

    CannyEdgeDetectorOpenCL(final CannyEdgeTransform pTransform) {
        this();
        mTransform = pTransform;
    }

    private void computeGradients(final float kernelRadius, final int kernelWidth) {

        // generate the gaussian convolution masks
        final float[] kernel = new float[kernelWidth];
        final float[] diffKernel = new float[kernelWidth];
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
        int initY = width * (kwidth - 1);
        int maxY = width * (height - (kwidth - 1));

        // perform convolution in x and y directions
        for (int x = initX; x < maxX; x++) {
            for (int y = initY; y < maxY; y += width) {
                final int index = x + y;
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
                final int index = x + y;
                for (int i = 1; i < kwidth; i++) {
                    sum += diffKernel[i] * (yConv[index - i] - yConv[index + i]);
                }

                xGradient[index] = sum;
            }

        }

        for (int x = kwidth; x < width - kwidth; x++) {
            for (int y = initY; y < maxY; y += width) {
                float sum = 0.0f;
                final int index = x + y;
                int yOffset = width;
                for (int i = 1; i < kwidth; i++) {
                    sum += diffKernel[i] * (xConv[index - yOffset] - xConv[index + yOffset]);
                    yOffset += width;
                }

                yGradient[index] = sum;
            }
        }

        initX = kwidth;
        maxX = width - 2 * kwidth;
        initY = width * kwidth;
        maxY = height - 2 * kwidth;
        mXYStart = new int[]{initX, initY, width};

        val range = Range.create2D(maxX, maxY);
        val openCLKernel = new CannyEdgeDetectorOpenCLKernel(magnitude, xGradient, yGradient, mXYStart);
        openCLKernel.put(mXYStart);
        openCLKernel.execute(range);
        magnitude = openCLKernel.getMagnitude();
        xGradient = openCLKernel.getxGradient();
        yGradient = openCLKernel.getyGradient();
        openCLKernel.dispose();

        mLogger.info(() -> {
            val sb = new StringBuilder();
            KernelManager.instance().reportDeviceUsage(sb, true);
            return sb.toString();
        });
    }

    private void follow(final int x1, final int y1, final int i1, final int threshold) {
        final int x0 = x1 == 0 ? x1 : x1 - 1;
        final int x2 = x1 == width - 1 ? x1 : x1 + 1;
        final int y0 = y1 == 0 ? y1 : y1 - 1;
        final int y2 = y1 == height - 1 ? y1 : y1 + 1;

        data[i1] = magnitude[i1];
        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                final int i2 = x + y * width;
                if ((y != y1 || x != x1) && data[i2] == 0 && magnitude[i2] >= threshold) {
                    follow(x, y, i2, threshold);
                    return;
                }
            }
        }
    }

    public float gaussian(final float x, final float sigma) {
        return (float) Math.exp(-(x * x) / (2f * sigma * sigma));
    }

    @Override
    public void dispose() {

    }

    /**
     * Obtains an mData containing the edges detected during the last call to the process method. The buffered mData is an opaque
     * mData of type BufferedImage.TYPE_INT_ARGB in which edge pixels are white and all other pixels are black.
     *
     * @return an mData containing the detected edges, or null if the process method has not yet been called.
     */

    @Override
    public ImmutablePixelMapData getEdgeData() {
        return mEdgeData;
    }

    /**
     * Sets the edges mData. Calling this method will not change the operation of the edge detector in any way. It is intended to
     * provide a means by which the memory referenced by the detector object may be reduced.
     *
     * @param edgeData expected (though not required) to be null
     */

    @Override
    public void setEdgeData(final ImmutablePixelMapData edgeData) {
        mEdgeData = edgeData;
    }

    /**
     * The radius of the Gaussian convolution kernel used to smooth the source image prior to gradient calculation. The default
     * value is 16.
     *
     * @return the Gaussian kernel radius in pixels
     */

    @Override
    public float getGaussianKernelRadius() {
        return gaussianKernelRadius;
    }

    /**
     * Sets the radius of the Gaussian convolution kernel used to smooth the source image prior to gradient calculation.
     *
     * @return a Gaussian kernel radius in pixels, must exceed 0.1f.
     */

    @Override
    public void setGaussianKernelRadius(final float gaussianKernelRadius) {
        if (gaussianKernelRadius < 0.1f) {
            throw new IllegalArgumentException();
        }
        this.gaussianKernelRadius = gaussianKernelRadius;
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
     * The number of pixels across which the Gaussian kernel is applied. This implementation will reduce the radius if the
     * contribution of pixel values is deemed negligable, so this is actually a maximum radius.
     *
     * @param gaussianKernelWidth a radius for the convolution operation in pixels, at least 2.
     */

    @Override
    public void setGaussianKernelWidth(final int gaussianKernelWidth) {
        if (gaussianKernelWidth < 2) {
            throw new IllegalArgumentException();
        }
        this.gaussianKernelWidth = gaussianKernelWidth;
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

    /**
     * Sets the high threshold for hysteresis. Suitable values for this parameter must be determined experimentally for each
     * application. It is nonsensical (though not prohibited) for this value to be less than the low threshold value.
     *
     * @param threshold a high hysteresis threshold
     */

    @Override
    public void setHighThreshold(final float threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException();
        }
        highThreshold = threshold;
    }

    // methods

    @Override
    public boolean getKeepRunning() {
        return mKeepRunning;
    }

    // private utility methods

    @Override
    public void setKeepRunning(final boolean pKeepRunning) {
        mKeepRunning = pKeepRunning;
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

    /**
     * The low threshold for hysteresis. The default value is 2.5.
     *
     * @return the low hysteresis threshold
     */

    @Override
    public float getLowThreshold() {
        return lowThreshold;
    }

    /**
     * Sets the low threshold for hysteresis. Suitable values for this parameter must be determined experimentally for each
     * application. It is nonsensical (though not prohibited) for this value to exceed the high threshold value.
     *
     * @param threshold a low hysteresis threshold
     */

    @Override
    public void setLowThreshold(final float threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException();
        }
        lowThreshold = threshold;
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

    /**
     * Specifies the mData that will provide the luminance data in which edges will be detected. A source mData must be set before
     * the process method is called.
     *
     * @param image a source of luminance data
     */
    @Override
    public void setSourceImage(final IPictureSource image) {
        sourceImage = image;
    }

    // NOTE: It is quite feasible to replace the implementation of this method
    // with one which only loosely approximates the hypot function. I've tested
    // simple approximations such as Math.abs(x) + Math.abs(y) and they work fine.
//        public float hypot ( final float x, final float y){
//            return sqrt(x * x + y * y);
//        }

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

    /**
     * Whether the luminance data extracted from the source image is normalized by linearizing its histogram prior to edge
     * extraction. The default value is false.
     *
     * @return whether the contrast is normalized
     */

    @Override
    public boolean isContrastNormalized() {
        return contrastNormalized;
    }

    /**
     * Sets whether the contrast is normalized
     *
     * @param contrastNormalized true if the contrast should be normalized, false otherwise
     */

    @Override
    public void setContrastNormalized(final boolean contrastNormalized) {
        this.contrastNormalized = contrastNormalized;
    }

    private int luminance(final float r, final float g, final float b) {
        return Math.round(0.299f * r + 0.587f * g + 0.114f * b);
    }

    private void normalizeContrast() {
        final int[] histogram = new int[256];
        for (int i = 0; i < data.length; i++) {
            histogram[data[i]]++;
        }
        final int[] remap = new int[256];
        int sum = 0;
        int j = 0;
        for (int i = 0; i < histogram.length; i++) {
            sum += histogram[i];
            final int target = sum * 255 / picsize;
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

    public synchronized void process() {
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

        final int low = Math.round(lowThreshold * MAGNITUDE_SCALE);
        final int high = Math.round(highThreshold * MAGNITUDE_SCALE);
        performHysteresis(low, high);
        // stopWatch.logLapTime(Level.INFO, "performHysteresis");

        thresholdEdges();
        // stopWatch.logLapTime(Level.INFO, "thresholdEdges");

        writeEdges(data);
        // stopWatch.logLapTime(Level.INFO, "writeEdges");
        // stopWatch.logElapsedTime(Level.INFO, "Process");
    }

    @Override
    public void process(final IProgressObserver pProgressObserver) {
        process();
    }

    private void readLuminance() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                final Color c = sourceImage.getColor(x, y).orElseGet(() -> Services.getServices().getProperties().getColorOOB());
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

    private void setData(final int pX, final int pY, final int pValue) {
        final int index = pX + pY * width;
        data[index] = pValue;
    }

    private void thresholdEdges() {
        for (int i = 0; i < picsize; i++) {
            data[i] = data[i] > 0 ? -1 : 0xff000000;
        }
    }

    private void writeEdges(final int pixels[]) {
        if (mEdgeData == null || mEdgeData.width() != width || mEdgeData.height() != height) {
            mEdgeData = ImmutablePixelMapData.builder().width(width).height(height).is360(true).build();
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int index = x + y * width;
                final boolean col = pixels[index] == -1;
                var pixel = pixelMapService.getPixelAt(mEdgeData, x, y);
                mEdgeData = pixelMapService.setEdge(mEdgeData, mTransform, pixel, col);
            }
        }

    }
}
