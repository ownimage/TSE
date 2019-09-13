package com.ownimage.framework.util.runWhenDirty;

public interface IRunWhenDirty {

    void markDirty();

    boolean isClean();

    boolean isDirty();
}
