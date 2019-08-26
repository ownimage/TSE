package com.ownimage.framework.util.immutable;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImmutableMapTest {

    @Test
    public void clear_01() {
        // GIVEN
        val key = 1;
        val value = "one";
        val underTest = new ImmutableMap<Integer, String>().put(key, value);
        assertEquals(underTest.get(key), value);
        // WHEN
        val actual = underTest.clear();
        // THEN
        assertEquals(underTest.get(key), value);
        assertEquals(actual.get(key), null);
        assertEquals(underTest.get(key), value);
        assertEquals(actual.get(key), null);
    }

    @Test
    public void put_01() {
        // GIVEN
        val key = 1;
        val value1 = "one";
        val value2 = "two";
        val underTest = new ImmutableMap<Integer, String>().put(key, value1);
        assertEquals(underTest.get(key), value1);
        // WHEN
        val actual = underTest.put(key, value2);
        // THEN
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), value2);
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), value2);
    }

    @Test
    public void put_02() {
        // GIVEN
        var underTest = new ImmutableMap<Integer, String>();
        underTest = underTest.put(1, "one");
        underTest = underTest.put(2, "two");
        assertEquals(underTest.get(1), "one");
        assertEquals(underTest.get(2), "two");
        // WHEN
        val actual = underTest.put(1, "ONE");
        // THEN
        assertEquals(underTest.get(1), "one");
        assertEquals(underTest.get(2), "two");

        assertEquals(actual.get(1), "ONE");
        assertEquals(actual.get(2), "two");

        assertEquals(underTest.get(1), "one");
        assertEquals(underTest.get(2), "two");

        assertEquals(actual.get(1), "ONE");
        assertEquals(actual.get(2), "two");
    }

    @Test
    public void remove_01() {
        // GIVEN
        val key = 1;
        val value1 = "one";
        val underTest = new ImmutableMap<Integer, String>().put(key, value1);
        assertEquals(underTest.get(key), value1);
        // WHEN
        val actual = underTest.remove(key);
        // THEN
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), null);
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), null);
    }

    @Test
    public void remove_02() {
        // GIVEN
        val key = 1;
        val key2 = 2;
        val value1 = "one";
        val underTest = new ImmutableMap<Integer, String>().put(key, value1);
        assertEquals(underTest.get(key), value1);
        // WHEN
        val actual = underTest.remove(key2);
        // THEN
        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), "one");
        assertEquals(underTest.get(key2), null);
        assertEquals(actual.get(key2), null);

        assertEquals(underTest.get(key), value1);
        assertEquals(actual.get(key), "one");
        assertEquals(underTest.get(key2), null);
        assertEquals(actual.get(key2), null);
    }

    @Test
    public void size_01() {
        // GIVEN
        val underTest = new ImmutableMap<Integer, String>();
        // WHEN
        val actual = underTest.size();
        // THEN
        assertEquals(actual, 0);
    }

    @Test
    public void size_02() {
        // GIVEN
        val underTest = new ImmutableMap<Integer, String>()
                .put(1, "one");
        // WHEN
        val actual = underTest.size();
        // THEN
        assertEquals(actual, 1);
    }

    @Test
    public void size_03() {
        // GIVEN
        val underTest = new ImmutableMap<Integer, String>()
                .put(1, "one")
                .put(1, "two");
        // WHEN
        val actual = underTest.size();
        // THEN
        assertEquals(actual, 1);
    }

    @Test
    public void size_04() {
        // GIVEN
        val underTest = new ImmutableMap<Integer, String>()
                .put(1, "one")
                .put(2, "two");
        // WHEN
        val actual = underTest.size();
        // THEN
        assertEquals(actual, 2);
    }

    @Test
    public void size_05() {
        // GIVEN
        val underTest = new ImmutableMap<Integer, String>()
                .put(1, "one")
                .put(2, "two")
                .clear();
        // WHEN
        val actual = underTest.size();
        // THEN
        assertEquals(actual, 0);
    }
}
