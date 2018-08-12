package com.ownimage.perception.render;

import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class TransformResultBatch implements ITransformResultBatch {

    public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    private String mName;

    /**
     * Each element of the X pixel array gives the Y position in the destination image.
     */
    private int[] mXPixel;

    /**
     * Each element of the Y pixel array gives the Y position in the destination image.
     */
    private int[] mYPixel;

    private double[] mX;
    private double[] mY;
    private float[] mR;
    private float[] mG;

    private float[] mB;

    private float[] mA;

    private int mBatchSize;
    private int mMaxBatchSize;

    private IBatchEngine mCurrentOwner;

    private int mXMax;
    private int mYMax;
    private int mXCurrent;
    private int mYCurrent;

    private Date mStartTime;
    private int mPixelsProcessed;
    private final RenderService mRenderService;

    public TransformResultBatch(final RenderService pRenderService, final int pMaxBatchSize) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pRenderService, "pRenderService");
        Framework.logParams(mLogger, "pMaxBatchSize", pMaxBatchSize);
        Framework.checkParameterGreaterThan(mLogger, pMaxBatchSize, 0, "pMaxBatchSize (%d) must be greater than 0");

        mRenderService = pRenderService;
        mMaxBatchSize = pMaxBatchSize;
        mBatchSize = 0;

        createArrays();

        Framework.logExit(mLogger);
    }

    private void createArrays() {
        Framework.logEntry(mLogger);

        mXPixel = new int[mMaxBatchSize];
        mYPixel = new int[mMaxBatchSize];

        mX = new double[mMaxBatchSize];
        mY = new double[mMaxBatchSize];

        mR = new float[mMaxBatchSize];
        mG = new float[mMaxBatchSize];
        mB = new float[mMaxBatchSize];
        mA = new float[mMaxBatchSize];

        Framework.logExit(mLogger);
    }

    @Override
    public float[] getA() {
        return mA;
    }

    @Override
    public float[] getB() {
        return mB;
    }

    @Override
    public int getBatchSize() {
        return mBatchSize;
    }

    @Override
    public float[] getG() {
        return mG;
    }

    @Override
    public int getMaxBatchSize() {
        return mMaxBatchSize;
    }

    @Override
    public float[] getR() {
        return mR;
    }

    @Override
    public ITransformResult getTransformResult(final int pIndex) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pIndex", pIndex);

        if (pIndex >= mMaxBatchSize) {
            mLogger.fine("pIndex = " + pIndex);
            mLogger.fine("mBatchSize = " + mMaxBatchSize);
            throw new IllegalArgumentException("pIndex must be less than mBatchSize");
        }

        Framework.logExit(mLogger);
        return new TransformResult(this, pIndex, mXPixel[pIndex], mYPixel[pIndex], mX[pIndex], mY[pIndex], mR[pIndex], mG[pIndex], mB[pIndex], mA[pIndex]);
    }

    @Override
    public double[] getX() {
        return mX;
    }

    public int getXCurrent() {
        return mXCurrent;
    }

    public int getXMax() {
        return mXMax;
    }

    public int[] getXPixel() {
        return mXPixel;
    }

    @Override
    public double[] getY() {
        return mY;
    }

    public int getYCurrent() {
        return mYCurrent;
    }

    public int getYMax() {
        return mYMax;
    }

    public int[] getYPixel() {
        return mYPixel;
    }

    public boolean hasNext() {
        Framework.logEntry(mLogger);

        if (mXCurrent < 0) {
            throw new IllegalStateException(String.format("mXCurrent (%d) must be greater than 0.", mXCurrent));
        }
        if (mYCurrent < 0) {
            throw new IllegalStateException(String.format("mYCurrent (%d) must be greater than 0.", mYCurrent));
        }

        if (mXCurrent == 0 && mYCurrent == 0) {
            mStartTime = new Date();
            mPixelsProcessed = 0;
        } else {
            mPixelsProcessed += getBatchSize();
        }

        Framework.logValue(mLogger, "mXCurrent", mXCurrent);
        Framework.logValue(mLogger, "mYCurrent", mYCurrent);
        Framework.logValue(mLogger, "mXMax", mXMax);
        Framework.logValue(mLogger, "mYMax", mYMax);

        boolean hasNext = mXCurrent < mXMax && mYCurrent < mYMax;
        if (!hasNext) {
            Calendar cal = Calendar.getInstance();

            cal.setTime(mStartTime);
            long start = cal.getTimeInMillis();

            cal.setTime(new Date());
            long end = cal.getTimeInMillis();

            mLogger.info(() -> String.format("Total batch (%s), batchSise=%d, millisecs=%d", mName, mPixelsProcessed, (end - start)));
        }

        Framework.logExit(mLogger, hasNext);
        return hasNext;
    }

    public void initialize(final PictureType pPicture, final IBatchEngine pOwner, final int pMaxBatchSize) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pMaxBatchSize", pMaxBatchSize);
        Framework.checkParameterNotNull(mLogger, pPicture, "pPicture");
        Framework.checkParameterNotNull(mLogger, pOwner, "pOwner");

        mCurrentOwner = pOwner;
        setMaxBatchSize(pMaxBatchSize);

        mXMax = pPicture.getWidth();
        mYMax = pPicture.getHeight();
        mXCurrent = 0;
        mYCurrent = 0;
        mBatchSize = 0;

        Framework.logValue(mLogger, "mXMax", mXMax);
        Framework.logValue(mLogger, "mYMax", mYMax);
        Framework.logExit(mLogger);
    }

    void moveTo(final IBatchEngine pNewOwner) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pNewOwner, "pNewOwner");

        if (mCurrentOwner.getProcessingLocation() != pNewOwner.getProcessingLocation()) {
            mLogger.fine(String.format("Moving batch from %s to %s", pNewOwner.getProcessingLocation(), mCurrentOwner.getProcessingLocation()));
            mCurrentOwner.get(this);
            pNewOwner.put(this);
            mCurrentOwner = pNewOwner;
        }

        Framework.logExit(mLogger);
    }

    public void next(int pOverSample) {
        Framework.logEntry(mLogger);
        mCurrentOwner.next(this, pOverSample);
        Framework.logExit(mLogger);
    }

    public void render(final PictureType pPicture, int pOverSample) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pPicture, "pPicture");

        moveTo(mRenderService.getBaseBatchEngine());
        float divisor = pOverSample * pOverSample;

        for (int i = 0; i < getBatchSize(); ) {
            int x = mXPixel[i];
            int y = mYPixel[i];
            float r = 0.0f;
            float g = 0.0f;
            float b = 0.0f;
            float a = 0.0f;
            for (int xs = 0; xs < pOverSample; xs++)
                for (int ys = 0; ys < pOverSample; ys++) {
                    r += mR[i];
                    g += mG[i];
                    b += mB[i];
                    a += mA[i];
                    i++;
                }
            Color c = new Color(r / divisor, g / divisor, b / divisor, 1.0f);
            pPicture.setColor(x, y, c);
        }
        Framework.logExit(mLogger);
    }

    public void setBatchSize(final int pSize) {
        mBatchSize = pSize;
    }

    private void setMaxBatchSize(final int pMaxBatchSize) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pMaxBatchSize", pMaxBatchSize);
        Framework.checkParameterGreaterThan(mLogger, pMaxBatchSize, 0, "pBatchSize (%d) must be greater than 0.");

        if (mMaxBatchSize != pMaxBatchSize) {
            mLogger.fine("mMaxBatchSize = " + mMaxBatchSize);
            mLogger.fine("Need to recreate arrays");
            mMaxBatchSize = pMaxBatchSize;
            createArrays();
        }

        Framework.logExit(mLogger);
    }

    public void setXCurrent(final int pXCurrent) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pXCurrent", pXCurrent);
        Framework.checkParameterGreaterThanEqual(mLogger, pXCurrent, 0, "pXCurrent (%d) must be >= 0.");
        Framework.checkParameterLessThan(mLogger, pXCurrent, mXMax, "pXCurrent (%d) must be < mXMax (%d).");

        mXCurrent = pXCurrent;

        Framework.logExit(mLogger);
    }

    /**
     * Sets the current y value. This can be between 0 and mYMax inclusive. It can go as high as mYMax which indicates that there
     * are no more fetches that can be made from the batch.
     *
     * @param pYCurrent the new y current
     */
    public void setYCurrent(final int pYCurrent) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pYCurrent", pYCurrent);
        Framework.checkParameterGreaterThanEqual(mLogger, pYCurrent, 0, "pXCurrent (%d) must be >= 0.");
        Framework.checkParameterLessThanEqual(mLogger, pYCurrent, mYMax, "pXCurrent (%d) must be < mYMax (%d).");

        mYCurrent = pYCurrent;

        Framework.logExit(mLogger);
    }

    public void setName(String pName) {
        mName = pName;
    }

}
