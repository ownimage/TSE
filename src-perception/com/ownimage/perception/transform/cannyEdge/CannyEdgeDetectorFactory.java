/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */

package com.ownimage.perception.transform.cannyEdge;

import java.util.logging.Logger;

import com.ownimage.framework.util.Version;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.transform.CannyEdgeTransform;

public class CannyEdgeDetectorFactory {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	@SuppressWarnings("unused")
	private final static Logger mLogger = Logger.getLogger(CannyEdgeDetector.class.getName());

	public static final int JAVA_THREADS = 1;
	public static final int OPENCL = 2;

	public static ICannyEdgeDetector createInstance(final CannyEdgeTransform pTransform) {
		if (getProperties().useOpenCL()) {
			System.out.println("####################  OpenCL");
			return new CannyEdgeDetectorOpenCL(pTransform);
		}
		if (getProperties().useJTP()) {
			System.out.println("####################  JTP");
			return new CannyEdgeDetectorJavaThreads(pTransform);
		}
		System.out.println("####################  Normal");
		return new CannyEdgeDetector(pTransform);
	}

	public static ICannyEdgeDetector createInstance(final CannyEdgeTransform pTransform, final int pAllowedTypes) {
		if (getProperties().useOpenCL() && (pAllowedTypes & OPENCL) != 0) {
			System.out.println("####################  OpenCL");
			return new CannyEdgeDetectorOpenCL(pTransform);
		}
		if (getProperties().useJTP() && (pAllowedTypes & JAVA_THREADS) != 0) {
			System.out.println("####################  JTP");
			return new CannyEdgeDetectorJavaThreads(pTransform);
		}

		System.out.println("####################  Normal");
		return new CannyEdgeDetector(pTransform);

	}

	private static Properties getProperties() {
		return Perception.getPerception().getProperties();
	}
}
