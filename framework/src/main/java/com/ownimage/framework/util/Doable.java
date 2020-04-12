package com.ownimage.framework.util;

public interface Doable<E extends Throwable> {

    static void convertToRuntimeException(Doable<?> pDoable) {
        try {
            pDoable.doIt();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    void doIt() throws E;

}