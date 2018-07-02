package com.ownimage.perception.render;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class BaseBatchEngine implements IBatchEngine {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
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

	@Override
	public void next(final TransformResultBatch pBatch) {
		Framework.logEntry(mLogger);
		Framework.checkParameterNotNull(mLogger, pBatch, "pBatch");

		int index = 0;
		int x = pBatch.getXCurrent();
		int y = pBatch.getYCurrent();

		while (index < pBatch.getMaxBatchSize() && y < pBatch.getYMax()) {
			pBatch.getXPixel()[index] = x;
			pBatch.getYPixel()[index] = y;
			pBatch.getX()[index] = (double) x / pBatch.getXMax();
			pBatch.getY()[index] = (double) y / pBatch.getYMax();
			pBatch.getR()[index] = 0.0f;
			pBatch.getG()[index] = 0.0f;
			pBatch.getB()[index] = 0.0f;
			pBatch.getA()[index] = 0.0f;

			if (++x >= pBatch.getXMax()) {
				x = 0;
				y++;
			}

			index++;
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
