package com.ownimage.framework.util.runWhenDirty;

public class RunWhenDirtyFactory {

    public static IRunWhenDirty create(Runnable pAction) {
        return new RunWhenDirty(pAction, false);
    }

    public static IRunWhenDirty createOnNewThread(Runnable pAction) {
        return new RunWhenDirty(pAction, true);
    }
}
