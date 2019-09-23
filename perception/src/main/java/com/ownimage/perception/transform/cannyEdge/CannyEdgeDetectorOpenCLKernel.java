package com.ownimage.perception.transform.cannyEdge;

import com.aparapi.Kernel;

/**
 * This has been extracted from CannyEdgeDetectorOpenCL
 *
 * @author Tom Gibara adapted by ownimage
 */

public class CannyEdgeDetectorOpenCLKernel extends Kernel {

    protected int[] magnitude;
    protected float[] xGradient;
    protected float[] yGradient;
    protected int[] mXYStart;

    public CannyEdgeDetectorOpenCLKernel(int[] pMagnitude, float[] pXGradient, float[] pYGradient, int[] pXYStart) {
        magnitude = pMagnitude;
        xGradient = pXGradient;
        yGradient = pYGradient;
        mXYStart = pXYStart;
    }


    public int[] getMagnitude() {
        return magnitude;
    }

    public float[] getxGradient() {
        return xGradient;
    }

    public float[] getyGradient() {
        return yGradient;
    }

    @Override
    public void run() {
        int i = getGlobalId(0);
        int j = getGlobalId(1);
        int x = i + mXYStart[0];
        int y = j * mXYStart[2] + mXYStart[1];
        computeGradientsNonMaximalSuppression(x, y, mXYStart[2]);
    }

    public void computeGradientsNonMaximalSuppression(final int x, final int y, final int width) {
        final int index = x + y;
        final int indexN = index - width;
        final int indexS = index + width;
        final int indexW = index - 1;
        final int indexE = index + 1;
        final int indexNW = indexN - 1;
        final int indexNE = indexN + 1;
        final int indexSW = indexS - 1;
        final int indexSE = indexS + 1;

        final float xGrad = xGradient[index];
        final float yGrad = yGradient[index];
        final float gradMag = hypot(xGrad, yGrad);

        // perform non-maximal supression
        final float nMag = hypot(xGradient[indexN], yGradient[indexN]);
        final float sMag = hypot(xGradient[indexS], yGradient[indexS]);
        final float wMag = hypot(xGradient[indexW], yGradient[indexW]);
        final float eMag = hypot(xGradient[indexE], yGradient[indexE]);
        final float neMag = hypot(xGradient[indexNE], yGradient[indexNE]);
        final float seMag = hypot(xGradient[indexSE], yGradient[indexSE]);
        final float swMag = hypot(xGradient[indexSW], yGradient[indexSW]);
        final float nwMag = hypot(xGradient[indexNW], yGradient[indexNW]);
        final float tmp;
        /*
         * An explanation of what's happening here, for those who want to understand the source: This performs the
         * "non-maximal supression" phase of the Canny edge detection in which we need to compare the gradient magnitude to that in
         * the direction of the gradient; only if the value is a local maximum do we consider the point as an edge candidate.
         *
         * We need to break the comparison into a number of different cases depending on the gradient direction so that the
         * appropriate values can be used. To avoid computing the gradient direction, we use two simple comparisons: first we check
         * that the partial derivatives have the same sign (1) and then we check which is larger (2). As a consequence, we have
         * reduced the problem to one of four identical cases that each test the central gradient magnitude against the values at
         * two points with 'identical support'; what this means is that the geometry required to accurately interpolate the
         * magnitude of gradient function at those points has an identical geometry (upto right-angled-rotation/reflection).
         *
         * When comparing the central gradient to the two interpolated values, we avoid performing any divisions by multiplying both
         * sides of each inequality by the greater of the two partial derivatives. The common comparand is stored in a temporary
         * variable (3) and reused in the mirror case (4).
         */
        boolean b = false;
        if (xGrad * yGrad <= 0) {
            if (abs(xGrad) >= abs(yGrad)) /* (2) */ {
                tmp = abs(xGrad * gradMag);
                b = tmp >= abs(yGrad * neMag - (xGrad + yGrad) * eMag) /* (3) */
                        && tmp > abs(yGrad * swMag - (xGrad + yGrad) * wMag); /* (4) */
            } else {
                tmp = abs(yGrad * gradMag);
                b = tmp >= abs(xGrad * neMag - (yGrad + xGrad) * nMag) /* (3) */
                        && tmp > abs(xGrad * swMag - (yGrad + xGrad) * sMag); /* (4) */
            }
        } else {
            if (abs(xGrad) >= abs(yGrad)) {/* (2) */
                tmp = abs(xGrad * gradMag);
                b = tmp >= abs(yGrad * seMag + (xGrad - yGrad) * eMag) /* (3) */
                        && tmp > abs(yGrad * nwMag + (xGrad - yGrad) * wMag); /* (4) */
            } else {
                tmp = abs(yGrad * gradMag);
                b = tmp >= abs(xGrad * seMag + (yGrad - xGrad) * sMag) /* (3) */
                        && tmp > abs(xGrad * nwMag + (yGrad - xGrad) * nMag); /* (4) */
            }
        }
        if (b) {
            magnitude[index] = gradMag >= CannyEdgeDetectorOpenCL.MAGNITUDE_LIMIT
                    ? CannyEdgeDetectorOpenCL.MAGNITUDE_MAX
                    : (int) (CannyEdgeDetectorOpenCL.MAGNITUDE_SCALE * gradMag);
            // NOTE: The orientation of the edge is not employed by this
            // implementation. It is a simple matter to compute it at
            // this point as: Math.atan2(yGrad, xGrad);
        } else {
            magnitude[index] = 0;
        }
    }

}
