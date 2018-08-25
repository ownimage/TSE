/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.render;

public interface IBatchTransform {

    public String getDisplayName();

    /**
     * Gets the preferred batch engine.
     *
     * @return the preferred batch engine
     */
    public IBatchEngine getPreferredBatchEngine();

    public IBatchTransform getPreviousTransform();

    public boolean getUseTransform();

    public void transform(ITransformResult pRenderResult);

    public void transform(ITransformResultBatch pBatch);

}
