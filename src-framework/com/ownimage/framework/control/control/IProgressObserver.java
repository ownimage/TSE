/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.control.control;

public interface IProgressObserver {

    void setProgress(String pProgressString, int pPercent);

    void started();

    void finished();

}
