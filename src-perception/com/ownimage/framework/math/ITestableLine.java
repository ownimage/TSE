/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import com.ownimage.framework.util.Framework;

import java.util.logging.Logger;

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
