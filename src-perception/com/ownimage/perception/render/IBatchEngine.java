package com.ownimage.perception.render;

public interface IBatchEngine {

	public enum BatchLocation {
		Java, OpenCL
	}

	public void get(ITransformResultBatch pBatch);

	public BatchLocation getProcessingLocation();

    public void next(final TransformResultBatch pBatch, int pOverSample);

	public void put(ITransformResultBatch pBatch);

	public void transform(TransformResultBatch pBatch, IBatchTransform pTransform);

}
