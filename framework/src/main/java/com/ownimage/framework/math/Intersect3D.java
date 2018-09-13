/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class Intersect3D {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final Point3D mPoint;
    private final Vector3D mNormal;

    public Intersect3D(final Point3D pPoint, final Vector3D pNormal) {
        mPoint = pPoint;
        mNormal = pNormal;
    }

    public Vector3D getNormal() {
        return mNormal;
    }

    public Point3D getPoint() {
        return mPoint;
    }
}
