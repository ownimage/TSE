/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.app;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.lang.ref.WeakReference;

public class Test {

    //DoubleProperty view = new SimpleDoubleProperty(1.0);
    WeakReference<DoubleProperty> viewRef;
    DoubleProperty prop = new SimpleDoubleProperty(2.0);


    public static void main(final String[] args) throws InterruptedException {
        final Test test = new Test();
        test(test);
    }

    public static void test(final Test test) throws InterruptedException {
        test.print();
        //test.view = null;
        System.gc();
        Thread.sleep(5000);
        test.print();
    }

    public Test() {
        viewRef = new WeakReference<>(new SimpleDoubleProperty(1.0));
        viewRef.get().bind(prop);
    }

    public void print() {
        printRef(viewRef);
        prop.setValue(3.0);
        printRef(viewRef);
    }

    public void printRef(final WeakReference<DoubleProperty> r) {
        final DoubleProperty v = r.get();
        if (v == null) System.out.println("WeakReference null");
        else System.out.println(v.get());
    }
}
