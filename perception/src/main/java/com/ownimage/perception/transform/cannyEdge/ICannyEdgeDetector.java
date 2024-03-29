/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import com.ownimage.framework.control.control.IProgressObserver;
import com.ownimage.framework.control.type.IPictureSource;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;

public interface ICannyEdgeDetector {


    void dispose();

    /**
     * Obtains an mData containing the edges detected during the last call to the process method. The buffered mData is an opaque
     * mData of type BufferedImage.TYPE_INT_ARGB in which edge pixels are white and all other pixels are black.
     *
     * @return an mData containing the detected edges, or null if the process method has not yet been called.
     */

    ImmutablePixelMap getEdgeData();

    /**
     * The radius of the Gaussian convolution kernel used to smooth the source image prior to gradient calculation. The default
     * value is 16.
     *
     * @return the Gaussian kernel radius in pixels
     */

    float getGaussianKernelRadius();

    /**
     * The number of pixels across which the Gaussian kernel is applied. The default value is 16.
     *
     * @return the radius of the convolution operation in pixels
     */

    int getGaussianKernelWidth();

    /**
     * The high threshold for hysteresis. The default value is 7.5.
     *
     * @return the high hysteresis threshold
     */

    float getHighThreshold();

    boolean getKeepRunning();

    /**
     * The low threshold for hysteresis. The default value is 2.5.
     *
     * @return the low hysteresis threshold
     */

    float getLowThreshold();

    /**
     * The mData that provides the luminance data used by this detector to generate edges.
     *
     * @return the source mData, or null
     */

    IPictureSource getSourceImage();

    /**
     * Whether the luminance data extracted from the source image is normalized by linearizing its histogram prior to edge
     * extraction. The default value is false.
     *
     * @return whether the contrast is normalized
     */

    boolean isContrastNormalized();

    void process(IProgressObserver pProgressObserver);

    /**
     * Sets whether the contrast is normalized
     *
     * @param contrastNormalized true if the contrast should be normalized, false otherwise
     */

    void setContrastNormalized(boolean contrastNormalized);

    /**
     * Sets the edges mData. Calling this method will not change the operation of the edge detector in any way. It is intended to
     * provide a means by which the memory referenced by the detector object may be reduced.
     *
     * @param edgeData expected (though not required) to be null
     */

    void setEdgeData(ImmutablePixelMap edgeData);

    /**
     * Sets the radius of the Gaussian convolution kernel used to smooth the source image prior to gradient calculation.
     */

    void setGaussianKernelRadius(float gaussianKernelRadius);

    /**
     * The number of pixels across which the Gaussian kernel is applied. This implementation will reduce the radius if the
     * contribution of pixel values is deemed negligable, so this is actually a maximum radius.
     *
     * @param gaussianKernelWidth a radius for the convolution operation in pixels, at least 2.
     */

    void setGaussianKernelWidth(int gaussianKernelWidth);

    /**
     * Sets the high threshold for hysteresis. Suitable values for this parameter must be determined experimentally for each
     * application. It is nonsensical (though not prohibited) for this value to be less than the low threshold value.
     *
     * @param threshold a high hysteresis threshold
     */

    void setHighThreshold(float threshold);

    void setKeepRunning(boolean pKeepRunning);

    /**
     * Sets the low threshold for hysteresis. Suitable values for this parameter must be determined experimentally for each
     * application. It is nonsensical (though not prohibited) for this value to exceed the high threshold value.
     *
     * @param threshold a low hysteresis threshold
     */

    void setLowThreshold(float threshold);

    /**
     * Specifies the mData that will provide the luminance data in which edges will be detected. A source mData must be set before
     * the process method is called.
     *
     * @param pImage a source of luminance data
     */

    void setSourceImage(IPictureSource pImage);

}