/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util.immutable;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
        final List<String> expectedSecond = Arrays.asList(new String[]{"one", "two", "four", "five"});
        Assert.assertEquals(2, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
        Assert.assertEquals(4, second.size());
        Assert.assertTrue(second.containsAll(expectedSecond));
    }

    @Test
    public void addAll() {
        Collection<String> all = Arrays.asList(new String[]{"two", "three", "four"});
        ImmutableSet<String> underTest = new ImmutableSet<>();
        underTest = underTest.add("one");
        underTest = underTest.add("two");
        ImmutableSet<String> second = underTest;
        underTest = underTest.addAll(all);
        final List<String> expectedUnderTest = Arrays.asList(new String[]{"one", "two", "three", "four"});
        final List<String> expectedSecond = Arrays.asList(new String[]{"one", "two"});
        Assert.assertEquals(4, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
        Assert.assertEquals(2, second.size());
        Assert.assertTrue(second.containsAll(expectedSecond));
    }

    @Test
    public void removeAll01() {
        // if none of the elements to be removed are present then should return itself
        Collection<String> all = Arrays.asList(new String[]{"three", "four"});
        ImmutableSet<String> underTest = new ImmutableSet<>();
        underTest = underTest.add("one");
        underTest = underTest.add("two");
        ImmutableSet<String> result = underTest.removeAll(all);
        final List<String> expecteResult = Arrays.asList(new String[]{"one", "two"});
        Assert.assertEquals(result, underTest);
        Assert.assertTrue(underTest.containsAll(expecteResult));
    }

    @Test
    public void removeAll02() {
        Collection<String> all = Arrays.asList(new String[]{"two", "three", "four"});
        ImmutableSet<String> underTest = new ImmutableSet<>();
        underTest = underTest.add("one");
        underTest = underTest.add("two");
        underTest = underTest.add("three");
        ImmutableSet<String> result = underTest.removeAll(all);
        final List<String> expectedUnderTest = Arrays.asList(new String[]{"one", "two", "three"});
        final List<String> expectedResult = Arrays.asList(new String[]{"one"});
        // test result
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsAll(expectedResult));
        // test rollback
        Assert.assertEquals(3, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
        // test roll forward
        Assert.assertEquals(1, result.size());
        Assert.assertTrue(result.containsAll(expectedResult));
        // test rollback
        Assert.assertEquals(3, underTest.size());
        Assert.assertTrue(underTest.containsAll(expectedUnderTest));
    }

    @Test
    public void forEach() {
        String[] values = "one,two,three,four".split(",");
        Collection<String> all = Arrays.asList(values);
        ImmutableSet<String> underTest = new ImmutableSet<String>().addAll(all);
        HashSet<String> actual = new HashSet<>();
        underTest.forEach(s -> actual.add(s));
        Assert.assertEquals(4, actual.size());
        Assert.assertTrue(actual.containsAll(all));
    }

    @Test
    public void stream() {
        String[] values = "one,two,three,four".split(",");
        Collection<String> all = Arrays.asList(values);
        ImmutableSet<String> underTest = new ImmutableSet<String>().addAll(all);
        HashSet<String> actual = new HashSet<>();
        underTest.stream().forEach(s -> actual.add(s));
        Assert.assertEquals(4, actual.size());
        Assert.assertTrue(actual.containsAll(all));
    }

    @Test
    public void toCollection() {
        String[] values = "one,two,three,four".split(",");
        Collection<String> all = Arrays.asList(values);
        ImmutableSet<String> underTest = new ImmutableSet<String>().addAll(all);
        Collection<String> actual = underTest.toCollection();
        Assert.assertEquals(4, actual.size());
        Assert.assertTrue(actual.containsAll(all));
    }
}
