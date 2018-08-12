package com.ownimage.perception.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public interface ITestableLine {


    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	public boolean closerThan(Point pPoint, double pTolerance);

	public double getMaxX();

	public double getMaxY();

	public double getMinX();

	public double getMinY();

	public double length();
}
