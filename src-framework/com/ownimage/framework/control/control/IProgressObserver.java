package com.ownimage.framework.control.control;

public interface IProgressObserver {

    void setProgress(String pProgressString, int pPercent);

    void started();

    void finished();

}
