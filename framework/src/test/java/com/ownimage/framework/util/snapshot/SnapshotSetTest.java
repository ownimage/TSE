/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util.snapshot;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SnapshotSetTest {

    @Test
    public void constructor() {
        final SnapshotSet<String> underTest = new SnapshotSet<>();
    }

    @Test
    public void oneSnapshot() {
        final SnapshotSet<String> underTest = new SnapshotSet<>();
        underTest.add("one");
        underTest.add(new StringBuilder("o").append("ne").toString());
        underTest.add("two");
        Assert.assertEquals(2, underTest.size());
    }

    @Test
    public void secondSnapshot() {
        final SnapshotSet<String> underTest = new SnapshotSet<>();
        underTest.add("one");
        underTest.add("two");
        final SnapshotSet<String> snapshot = underTest.snapshot();
        underTest.add("three");
        snapshot.add("four");
        snapshot.add("five");
        final List<String> expectedUnderTest = Arrays.asList(new String[]{"one", "two", "three"});
        final List<String> expectedSnapshot = Arrays.asList(new String[]{"one", "two", "four", "five"});
        Assert.assertEquals(3, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
        Assert.assertEquals(4, snapshot.size());
        Assert.assertTrue(snapshot.containsAll(expectedSnapshot));
    }

    @Test
    public void removals() {
        final SnapshotSet<String> underTest = new SnapshotSet<>();
        underTest.add("one");
        underTest.add("two");
        final SnapshotSet<String> snapshot = underTest.snapshot();
        underTest.remove("one");
        snapshot.add("four");
        snapshot.add("five");
        underTest.add("six");
        final List<String> expectedUnderTest = Arrays.asList(new String[]{"two", "six"});
        final List<String> expectedSnapshot = Arrays.asList(new String[]{"one", "two", "four", "five"});
        Assert.assertEquals(2, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
        Assert.assertEquals(4, snapshot.size());
        Assert.assertTrue(snapshot.containsAll(expectedSnapshot));
    }
}
