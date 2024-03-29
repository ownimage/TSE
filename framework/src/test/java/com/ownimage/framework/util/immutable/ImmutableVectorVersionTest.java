package com.ownimage.framework.util.immutable;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class ImmutableVectorVersionTest {

    private final String s1 = "one";
    private final String s2 = "two";
    private final String s3 = "three";
    private final String s4 = "four";
    private final String s5 = "five";

    private void assertEquals(final List<String> pExpected, final ImmutableVectorVersion<String> pActual) {
        Assert.assertEquals(pExpected.size(), pActual.size());
        IntStream.range(0, pActual.size()).forEach(i -> Assert.assertEquals(pExpected.get(i), pActual.get(i)));
    }

    @Test
    public void constructor_01() {
        new ImmutableVectorVersion<String>();
    }

    @Test
    public void add_01() {
        // GIVEN WHEN
        val underTest1 = new ImmutableVectorVersion<String>();
        val underTest2 = underTest1.add(s1);
        val underTest3 = underTest2.add(s2);
        val underTest4 = underTest3.add(s3);
        // THEN done in this order to test rollback and forward
        assertEquals(List.of(s1, s2, s3), underTest4);
        assertEquals(List.of(s1), underTest2);
        assertEquals(List.of(s1, s2), underTest3);
        assertEquals(List.of(), underTest1);
    }

    @Test
    public void addAt_01() {
        // GIVEN WHEN
        val underTest1 = new ImmutableVectorVersion<String>();
        val underTest2 = underTest1.add(s1);
        val underTest3 = underTest2.add(s2);
        val underTest4 = underTest3.add(s3);
        val underTest5 = underTest4.add(1, s4);
        // THEN
        assertEquals(List.of(s1, s4, s2, s3), underTest5);
        assertEquals(List.of(), underTest1);
        assertEquals(List.of(s1), underTest2);
        assertEquals(List.of(s1, s2), underTest3);
        assertEquals(List.of(s1, s4, s2, s3), underTest5);
    }

    @Test
    public void addAll_01() {
        // GIVEN
        val s2Tos4 = List.of(s2, s3, s4);
        // WHEN
        val underTest1 = new ImmutableVectorVersion<String>();
        val underTest2 = underTest1.add(s1);
        val underTest3 = underTest2.addAll(s2Tos4);
        val underTest4 = underTest3.add(s5);
        // THEN
        assertEquals(List.of(s1, s2, s3, s4, s5), underTest4);
        assertEquals(List.of(), underTest1);
        assertEquals(List.of(s1), underTest2);
        assertEquals(List.of(s1, s2, s3, s4), underTest3);
        assertEquals(List.of(s1, s2, s3, s4, s5), underTest4);
    }

    @Test
    public void clear_01() {
        // GIVEN WHEN
        val underTest1 = new ImmutableVectorVersion<String>();
        val underTest2 = underTest1.add(s1);
        val underTest3 = underTest2.add(s2);
        val underTest4 = underTest3.add(s3);
        val underTest5 = underTest4.clear();
        // THEN
        assertEquals(List.of(), underTest5);
        assertEquals(List.of(s1, s2, s3), underTest4);
        assertEquals(List.of(s1), underTest2);
        assertEquals(List.of(s1, s2), underTest3);
        assertEquals(List.of(), underTest1);
        assertEquals(List.of(), underTest5);
    }

    @Test
    public void contains_01() {
        // GIVEN
        val s1Tos4 = List.of(s1, s2, s3, s4);
        // WHEN
        val underTest = new ImmutableVectorVersion<String>()
                .addAll(s1Tos4);
        // THEN
        Assert.assertTrue(underTest.contains(s1));
        Assert.assertTrue(underTest.contains(s2));
        Assert.assertTrue(underTest.contains(s3));
        Assert.assertTrue(underTest.contains(s4));
        Assert.assertFalse(underTest.contains(s5));
    }

    @Test
    public void containsAll_01() {
        // GIVEN
        val s1Tos4 = List.of(s1, s2, s3, s4);
        // WHEN
        val underTest = new ImmutableVectorVersion<String>().addAll(s1Tos4);
        // THEN
        Assert.assertTrue(underTest.containsAll(s1Tos4));
        Assert.assertTrue(underTest.containsAll(List.of(s1)));
        Assert.assertTrue(underTest.containsAll(List.of(s2)));
        Assert.assertTrue(underTest.containsAll(List.of(s3)));
        Assert.assertTrue(underTest.containsAll(List.of(s4)));
        Assert.assertFalse(underTest.containsAll(List.of(s5)));
        Assert.assertFalse(underTest.containsAll(List.of(s1, s5)));
    }

    @Test
    public void remove_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.remove(s1);
        // THEN
        assertEquals(List.of(s2, s3, s1, s4), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s2, s3, s1, s4), underTest2);
    }

    @Test
    public void remove_02() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.remove(0);
        // THEN
        assertEquals(List.of(s2, s3, s1, s4), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s2, s3, s1, s4), underTest2);
    }

    @Test
    public void remove_03() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.remove(2);
        // THEN
        assertEquals(List.of(s1, s2, s1, s4), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s1, s2, s1, s4), underTest2);
    }

    @Test
    public void remove_04() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.remove(4);
        // THEN
        assertEquals(List.of(s1, s2, s3, s1), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s1, s2, s3, s1), underTest2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void remove_05() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        underTest1.remove(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void remove_06() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        underTest1.remove(-1);
    }

    @Test
    public void firstElement_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val actual = underTest1.firstElement().get();
        // THEN
        Assert.assertEquals(s1, actual);
    }

    @Test
    public void firstElement_02() {
        // GIVEN
        val underTest1 = new ImmutableVectorVersion<String>();
        // WHEN
        val actual = underTest1.firstElement();
        // THEN
        Assert.assertEquals(Optional.empty(), actual);
    }

    @Test
    public void lastElement_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s1, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val actual = underTest1.lastElement().get();
        // THEN
        Assert.assertEquals(s4, actual);
    }

    @Test
    public void lastElement_02() {
        // GIVEN
        val underTest1 = new ImmutableVectorVersion<String>();
        // WHEN
        val actual = underTest1.lastElement();
        // THEN
        Assert.assertEquals(Optional.empty(), actual);
    }

    @Test
    public void removeAll_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s2, s3);
        ImmutableVectorVersion<String> underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        assertEquals(List.of(s1, s4), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s1, s4), underTest2);
    }

    @Test
    public void removeAll_02() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s2, s3, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        assertEquals(List.of(s1), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s1), underTest2);
    }

    @Test
    public void removeAll_03() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s1, s2, s3);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        assertEquals(List.of(s4), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s4), underTest2);
    }

    @Test
    public void removeAll_04() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s1, s2, s3, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        assertEquals(List.of(), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(), underTest2);
    }

    @Test
    public void removeAll_05() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = Collections.<String> emptyList();
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        Assert.assertSame(underTest1, underTest2);
    }

    @Test
    public void removeAll_06() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s5);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        Assert.assertSame(underTest1, underTest2);
    }

    @Test
    public void removeAll_07() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s2, s5);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        assertEquals(List.of(s1, s3, s4), underTest2);
        assertEquals(all, underTest1);
        assertEquals(List.of(s1, s3, s4), underTest2);
    }

    @Test
    public void forEach_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        val sb = new StringBuilder();
        // WHEN
        underTest1.forEach(s -> sb.append(s).append(" "));
        // THEN
        Assert.assertEquals("one two three four ", sb.toString());
    }

    @Test
    public void forEach_02() throws InterruptedException {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s2, s5);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        val underTest2 = underTest1.removeAll(remove);
        val failures = new Boolean[]{false};
        val thread1 = new Thread(() ->
            IntStream.range(0, 10000).forEach(i -> {
                val sb = new StringBuilder();
                underTest1.forEach(s -> sb.append(s).append(" "));
                if (!"one two three four ".equals(sb.toString())) {
                    failures[0] = true;
                }
            })
        );
        val thread2 = new Thread(() ->
            IntStream.range(0, 10000).forEach(i -> {
                val sb = new StringBuilder();
                underTest2.forEach(s -> sb.append(s).append(" "));
                if (!"one three four ".equals(sb.toString())) {
                    failures[0] = true;
                }
            })
        );
        // WHEN
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        // THEN
        Assert.assertFalse(failures[0]);
    }

    @Test
    public void set_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val expected = List.of(s1, s2, s5, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val underTest2 = underTest1.set(2, s5);
        // THEN
        assertEquals(expected, underTest2);
        assertEquals(all, underTest1);
        assertEquals(expected, underTest2);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void set_02() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        underTest1.set(10, s5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void set_03() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        underTest1.set(-1, s5);
    }

    @Test
    public void stream_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        val sb = new StringBuilder();
        // WHEN
        underTest1.stream().forEach(s -> sb.append(s).append(" "));
        // THEN
        Assert.assertEquals("one two three four ", sb.toString());
    }

    @Test
    public void stream_02() throws InterruptedException {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s2, s5);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        val underTest2 = underTest1.removeAll(remove);
        val failures = new Boolean[]{false};
        val thread1 = new Thread(() ->
            IntStream.range(0, 10000).forEach(i -> {
                val sb = new StringBuilder();
                underTest1.stream().forEach(s -> sb.append(s).append(" "));
                if (!"one two three four ".equals(sb.toString())) {
                    failures[0] = true;
                }
            })
        );
        val thread2 = new Thread(() ->
            IntStream.range(0, 10000).forEach(i -> {
                val sb = new StringBuilder();
                underTest2.stream().forEach(s -> sb.append(s).append(" "));
                if (!"one three four ".equals(sb.toString())) {
                    failures[0] = true;
                }
            })
        );
        // WHEN
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        // THEN
        Assert.assertFalse(failures[0]);
    }

    @Test
    public void vector_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val expected = new String[]{s1, s2, s3, s4};
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        // WHEN
        val actual = underTest1.toVector();
        // THEN
        assertEquals(all, underTest1);
        Assert.assertArrayEquals(expected, actual.toArray());
        Assert.assertEquals(s3, actual.get(2));
        // AND WHEN the returned Vector is modified
        actual.set(2, s5);
        // THEN there is no change to the immutable
        assertEquals(all, underTest1);
        Assert.assertEquals(s5, actual.get(2));
    }

    @Test
    public void size_01() {
        // GIVEN
        val all = List.of(s1, s2, s3, s4);
        val remove = List.of(s1, s2, s3);
        val underTest1 = new ImmutableVectorVersion<String>().addAll(all);
        val underTest2 = underTest1.removeAll(remove);
        // THEN
        Assert.assertEquals(4, underTest1.size());
        Assert.assertEquals(1, underTest2.size());
        Assert.assertEquals(4, underTest1.size());
        Assert.assertEquals(1, underTest2.size());
    }

    @Test
    public void rollback() {
        // GIVEN
        val initial = new ImmutableVectorVersion<String>();
        val firstVersion = initial.add("Hello");
        // WHEN
        val otherVersion = initial.add("World");
        // THEN
        Assert.assertEquals(0, initial.size());
        assertEquals(List.of("Hello"), firstVersion);
        assertEquals(List.of("World"), otherVersion);
    }

    @Test
    public void indexOf_01() {
        // GIVEN
        var all = List.of(s1, s2, s3, s4);
        var underTest = new ImmutableVectorClone<String>().addAll(all);
        // WHEN
        var actual = underTest.indexOf(s5);
        // THEN
        Assert.assertEquals(-1, actual);
    }

    @Test
    public void indexOf_02() {
        // GIVEN
        var all = List.of(s1, s2, s3, s4);
        var underTest = new ImmutableVectorClone<String>().addAll(all);
        var three = new StringBuilder("thr").append("ee").toString();
        // WHEN
        var actual = underTest.indexOf(three);
        // THEN
        Assert.assertEquals(2, actual);
        Assert.assertFalse(s3 == three);
    }

}
