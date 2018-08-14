/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */

package com.ownimage.perception.transform.cannyEdge;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.transform.CannyEdgeTransform;

public class CannyEdgeDetectorFactory {

	public enum Type {
		DEFAULT, SINGLE_THREAD, JAVA_THREADS, OPENCL
	}


    @SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();

	public static ICannyEdgeDetector createInstance(final CannyEdgeTransform pTransform) {
		if (getProperties().useOpenCL()) {
            mLogger.info(() -> "####################  OpenCL");
			return new CannyEdgeDetectorOpenCL(pTransform);
		}
		if (getProperties().useJTP()) {
            mLogger.info(() -> "####################  JTP");
			return new CannyEdgeDetectorJavaThreads(pTransform);
		}
        mLogger.info(() -> "####################  Normal");
		return new CannyEdgeDetector(pTransform);
	}

	public static ICannyEdgeDetector createInstance(final CannyEdgeTransform pTransform, final Type pAllowedTypes) {
		if (getProperties().useOpenCL() && (pAllowedTypes == Type.OPENCL || pAllowedTypes == Type.DEFAULT)) {
            mLogger.info(() -> "####################  OpenCL");
			return new CannyEdgeDetectorOpenCL(pTransform);
		}
		if (getProperties().useJTP() && (pAllowedTypes == Type.JAVA_THREADS || pAllowedTypes == Type.DEFAULT)) {
            mLogger.info(() -> "####################  JTP");
			return new CannyEdgeDetectorJavaThreads(pTransform);
		}

        mLogger.info(() -> "####################  Normal");
		return new CannyEdgeDetector(pTransform);

	}

	private static Properties getProperties() {
		return Services.getServices().getProperties();
	}
}
