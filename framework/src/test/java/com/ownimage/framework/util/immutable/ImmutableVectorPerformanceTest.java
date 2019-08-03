package com.ownimage.framework.util.immutable;

import com.ownimage.framework.util.SplitTimer;
import com.ownimage.framework.util.StrongReference;

public class ImmutableVectorPerformanceTest {

    public static void main(String[] pArgs) {

        new ImmutableVectorVersion<String>();
        new ImmutableVectorClone<String>();

        var timer = new SplitTimer();
        System.out.println(test(new ImmutableVectorClone<String>()));
        System.out.println(String.format("Clone : %s ms", timer.split()));
        System.out.println(test(new ImmutableVectorClone<String>()));
        System.out.println(String.format("Clone : %s ms", timer.split()));
        System.out.println(test(new ImmutableVectorClone<String>()));
        System.out.println(String.format("Clone : %s ms", timer.split()));

        System.out.println(test(new ImmutableVectorVersion<String>()));
        System.out.println(String.format("Version : %s ms", timer.split()));
        System.out.println(test(new ImmutableVectorVersion<String>()));
        System.out.println(String.format("Version : %s ms", timer.split()));
        System.out.println(test(new ImmutableVectorVersion<String>()));
        System.out.println(String.format("Version : %s ms", timer.split()));
    }

    private static int test(IImmutableVector<String> pUnderTest) {
        int total = 0;
        for (int j = 0; j < 100000; j++) {
            StrongReference<IImmutableVector<String>> t = new StrongReference<>(pUnderTest);
            for (int i = 0; i < 10; i++) {
                t.set(t.get().add("new string"));
            }
            total += t.get().size();
        }
        return total;
    }
}
