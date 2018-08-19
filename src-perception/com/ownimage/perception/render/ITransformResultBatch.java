package com.ownimage.perception.render;

public interface ITransformResultBatch {

    float[] getA();

    float[] getB();

    int getBatchSize();

    float[] getG();

    int getMaxBatchSize();

    float[] getR();

    ITransformResult getTransformResult(int pIndex);

    double[] getX();

    double[] getY();

    int getPercentComplete();
}
