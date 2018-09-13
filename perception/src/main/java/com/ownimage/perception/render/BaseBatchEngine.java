/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class BaseBatchEngine implements IBatchEngine {


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    @Override
    public void get(final ITransformResultBatch pBatch) {
        // TODO Auto-generated method stub

    }

    @Override
    public BatchLocation getProcessingLocation() {
        return BatchLocation.Java;
    }

    /**
     * Gets the exact x or y position as a fraction of the overall size.  This calculates the position taking into account the oversampling.
     *
     * @param pValue      the x or y pixel value
     * @param pMax        the height or width of the image
     * @param pOverSample how many samples are being taken in this dimention
     * @param pSample     which sample do we want to get the value for
     * @return a double that represents the fractional value of the sample.
     */
    private double getSamplePosition(int pValue, int pMax, int pOverSample, int pSample) {
        double base = (double) pValue / pMax;
        double offset = ((2 * pSample) + 1) / (2.0d * pOverSample * pMax);
        return base + offset;
    }

    @Override
    public void next(final TransformResultBatch pBatch, int pOverSample) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pBatch, "pBatch");

        int index = 0;
        int x = pBatch.getXCurrent();
        int y = pBatch.getYCurrent();

        while ((index + pOverSample * pOverSample) < pBatch.getMaxBatchSize() && y < pBatch.getYMax()) {
            for (int xs = 0; xs < pOverSample; xs++) {
                for (int ys = 0; ys < pOverSample; ys++) {
                    pBatch.getXPixel()[index] = x;
                    pBatch.getYPixel()[index] = y;
                    pBatch.getX()[index] = getSamplePosition(x, pBatch.getXMax(), pOverSample, xs);
                    pBatch.getY()[index] = getSamplePosition(y, pBatch.getYMax(), pOverSample, ys);
                    pBatch.getR()[index] = 0.0f;
                    pBatch.getG()[index] = 0.0f;
                    pBatch.getB()[index] = 0.0f;
                    pBatch.getA()[index] = 0.0f;
                    index++;
                }
            }
            if (++x >= pBatch.getXMax()) {
                x = 0;
                y++;
            }
        }

        pBatch.setXCurrent(x);
        pBatch.setYCurrent(y);
        pBatch.setBatchSize(index);

        Framework.logExit(mLogger);
    }

    @Override
    public void put(final ITransformResultBatch pBatch) {
        // TODO Auto-generated method stub

    }

    @Override
    public void transform(final TransformResultBatch pBatch, final IBatchTransform pTransform) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pBatch, "pBatch");
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");

        pBatch.moveTo(this);
        pTransform.transform(pBatch);

        Framework.logExit(mLogger);
    }

}
