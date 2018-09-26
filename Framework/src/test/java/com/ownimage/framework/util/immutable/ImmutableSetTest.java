/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util.immutable;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class ImmutableSetTest {

    @Test
    public void constructor() {
        final ImmutableSet<String> underTest = new ImmutableSet<>();
    }

    @Test
    public void redoUndoChanges() {
        ImmutableSet<String> original = new ImmutableSet<>();
        ImmutableSet<String> underTest = original.add("one");
        underTest = underTest.add(new StringBuilder("o").append("ne").toString());
        underTest = underTest.add("two");
        Assert.assertEquals(2, underTest.size());
        Assert.assertEquals(0, original.size());
    }

    @Test
    public void secondImmutable() {
        ImmutableSet<String> underTest = new ImmutableSet<>();
        underTest = underTest.add("one");
        underTest = underTest.add("two");
        ImmutableSet<String> second = underTest;
        underTest = underTest.add("three");
        second = second.add("four");
        second = second.add("five");
        final List<String> expectedUnderTest = Arrays.asList(new String[]{"one", "two", "three"});
        final List<String> expectedSnapshot = Arrays.asList(new String[]{"one", "two", "four", "five"});
        Assert.assertEquals(3, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
        Assert.assertEquals(4, second.size());
        Assert.assertTrue(second.containsAll(expectedSnapshot));
    }

    @Test
    public void removals() {
        ImmutableSet<String> underTest = new ImmutableSet<>();
        underTest = underTest.add("one");
        underTest = underTest.add("two");
        ImmutableSet<String> second = underTest;
        underTest = underTest.remove("one");
        second = second.add("four");
        second = second.add("five");
        underTest = underTest.add("six");
        final List<String> expectedUnderTest = Arrays.asList(new String[]{"two", "six"});
        final List<String> expectedSnapshot = Arrays.asList(new String[]{"one", "two", "four", "five"});
        Assert.assertEquals(2, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
        Assert.assertEquals(4, second.size());
        Assert.assertTrue(second.containsAll(expectedSnapshot));
    }
}
