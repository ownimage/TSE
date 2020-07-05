package com.ownimage.framework.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StrongReferenceTest {

    @Test
    public void update() {
        // GIVEN
        var string1 = "Hello ";
        var string2 = "world!";
        var expected = string1 + string2;
        var underTest = StrongReference.of(string1);
        // WHEN
        underTest.update(s -> s + string2);
        // THEN
        assertEquals(expected, underTest.get());
    }
}