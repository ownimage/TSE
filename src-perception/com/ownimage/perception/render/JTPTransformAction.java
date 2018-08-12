package com.ownimage.perception.render;

import java.util.concurrent.RecursiveAction;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class JTPTransformAction extends RecursiveAction {


    public final static Logger mLogger = Framework.getLogger();

	private final IBatchTransform mTransform;
	private final TransformResultBatch mBatch;
	private final int mThreadBatchSize;
	private final int mStart;
	private final int mStop;

	public JTPTransformAction(final IBatchTransform pTransform, final TransformResultBatch pBatch, final int pThreadBatchSize) {
		this(pTransform, pBatch, pThreadBatchSize, 0, pBatch.getBatchSize());
	}

	public JTPTransformAction(final IBatchTransform pTransform, final TransformResultBatch pBatch, final int pThreadBatchSize, final int pStart, final int pStop) {
		mTransform = pTransform;
		mBatch = pBatch;
		mThreadBatchSize = pThreadBatchSize;
		mStart = pStart;
		mStop = pStop;
	}

	@Override
	protected void compute() {

		// if work is above threshold, break tasks up into smaller tasks
		if (mStop - mStart <= mThreadBatchSize) {
			doCompute();
		} else {
			doSplit();
		}
	}

	private void doCompute() {
		for (int i = mStart; i < mStop; i++) {
			ITransformResult result = mBatch.getTransformResult(i);
			mTransform.transform(result);
		}
	}

	private void doSplit() {
		int half = (mStart + mStop) / 2;
		invokeAll(new JTPTransformAction(mTransform, mBatch, mThreadBatchSize, mStart, half), new JTPTransformAction(mTransform,
				mBatch, mThreadBatchSize, half, mStop));
		// invokeAll(new JTPTransformAction(mTransform, mBatch, mThreadBatchSize, mStart, mStart + mThreadBatchSize),
		// new JTPTransformAction(mTransform, mBatch, mThreadBatchSize, mStart + mThreadBatchSize, mStop));
	}
}
