/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */

package com.ownimage.perception.transform.cannyEdge;

import java.util.logging.Logger;

import com.ownimage.perception.Perception;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.util.Version;

public class CannyEdgeDetectorFactory {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	@SuppressWarnings("unused")
	private final static Logger mLogger = Logger.getLogger(CannyEdgeDetector.class.getName());

	public static ICannyEdgeDetector createInstance(CannyEdgeTransform pTransform) {

		if (Perception.getInstanceProperties().useOpenCL()) {
			return new CannyEdgeDetectorOpenCL(pTransform);
		}

		if (Perception.getInstanceProperties().useThreads()) {
			return new CannyEdgeDetectorJavaThreads(pTransform);
		}

		return new CannyEdgeDetector(pTransform);

	}
}
