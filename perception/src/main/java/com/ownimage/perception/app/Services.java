/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.app;

import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.PegCounterService;
import com.ownimage.perception.render.RenderService;
import com.ownimage.perception.transformSequence.TransformSequence;

import java.util.Optional;
import java.util.logging.Logger;

public class Services {

    public final static Logger mLogger = Framework.getLogger();
    private static final Services mServices;

    private Perception mPerception;
    private Properties mProperties;
    private UndoRedoBuffer mUndoRedoBuffer;
    private RenderService mRenderService;
    private TransformSequence mTransformSequence;
    private PegCounterService mPegCounterService;

    private Services() {
    }

    public Perception getPerception() {
        return mPerception;
    }

    public Properties getProperties() {
        return mProperties;
    }

    public UndoRedoBuffer getUndoRedoBuffer() {
        return mUndoRedoBuffer;
    }

    public RenderService getRenderService() {
        return mRenderService;
    }

    public Optional<TransformSequence> getOptionalTransformSequence() {
        return Optional.ofNullable(mTransformSequence);
    }

    public PegCounterService getPegCounterService() {
        return mPegCounterService;
    }

    public void setTransformSequence(final TransformSequence pTransformSequence) {
        mLogger.info("setTransformSequence()");
        mTransformSequence = pTransformSequence;
    }

    public static Services getServices() {
        return mServices;
    }

    static {
        mServices = new Services();
        mServices.mProperties = new Properties();
        mServices.mUndoRedoBuffer = new UndoRedoBuffer(100);
        mServices.mRenderService = new RenderService();
        mServices.mPerception = new Perception();
        mServices.mPegCounterService = new PegCounterService();
    }
}

