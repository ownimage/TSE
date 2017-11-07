package com.ownimage.perception.render;

public interface ITransformResultBatch {

	public float[] getA();

	public float[] getB();

	public int getBatchSize();

	public float[] getG();

	public int getMaxBatchSize();

	public float[] getR();

	public ITransformResult getTransformResult(int pIndex);

	public double[] getX();

	public double[] getY();

}
