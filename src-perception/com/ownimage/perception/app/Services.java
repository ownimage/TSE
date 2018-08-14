package com.ownimage.perception.app;

import java.util.logging.Logger;

import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.perception.render.RenderService;
import com.ownimage.perception.transformSequence.TransformSequence;

public class Services {

    public final static Logger mLogger = Framework.getLogger();
    private static Services mServices;

    private Perception mPerception;
    private Properties mProperties;
    private UndoRedoBuffer mUndoRedoBuffer;
    private RenderService mRenderService;
    private TransformSequence mTransformSequence;

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

    public TransformSequence getTransformSequence() {
        return mTransformSequence;
    }

    public void setTransformSequence(final TransformSequence pTransformSequence) {
        mLogger.info("setTransformSequence()");
        mTransformSequence = pTransformSequence;
    }

    public static Services getServices() {
        return mServices;
    }

    static {
        Services services = new Services();
        services.mProperties = new Properties();
        services.mUndoRedoBuffer = new UndoRedoBuffer(100);
        mServices = services;
        services.mRenderService = new RenderService();
        services.mPerception = new Perception();
    }
}

