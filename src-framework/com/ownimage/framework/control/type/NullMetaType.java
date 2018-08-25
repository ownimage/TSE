/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.control.type;

import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;

public class NullMetaType<R> implements IMetaType<R> {


    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 1L;

    @Override
    public boolean isValid(final R pValue) {
        return true;
    }
}