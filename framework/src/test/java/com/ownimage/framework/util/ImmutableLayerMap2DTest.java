/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util;

import com.ownimage.framework.logging.FrameworkLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.logging.Level;

public class ImmutableLayerMap2DTest {

    private Byte defaultValue = (byte) 0;

    @BeforeClass
    public static void beforeClass() {
        FrameworkLogger.getInstance().init(null, "TEST.log");
    }

    @Before
    public void before() {
        FrameworkLogger.getInstance().setLevel(Level.OFF);
        FrameworkLogger.getInstance().setLevel(ImmutableLayerMap2D.class.getCanonicalName(), Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
    }

    private void assertLogContains(String pString) {
        Assert.assertTrue(FrameworkLogger.getInstance().getLog().contains(pString));
    }

    private void assertLogDoesNotContain(String pString) {
        Assert.assertFalse(FrameworkLogger.getInstance().getLog().contains(pString));
    }


    @Test
    public void constructor() {
        ImmutableLayerMap2D<Byte> underTest = new ImmutableLayerMap2D<>(10, 10, defaultValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorX() {
        ImmutableLayerMap2D<Byte> underTest = new ImmutableLayerMap2D<>(-10, 10, defaultValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorY() {
        ImmutableLayerMap2D<Byte> underTest = new ImmutableLayerMap2D<>(10, -10, defaultValue);
    }

    @Test
    public void get() {
        // GIVEN
        ImmutableLayerMap2D<Byte>.ImmutableMap2D landscape = new ImmutableLayerMap2D<>(10, 20, defaultValue).getImmutable();
        ImmutableLayerMap2D<Byte>.ImmutableMap2D portrait = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // THEN can get all corner elements of both maps, this is to test the edge cases of the x,y checking
        Assert.assertEquals(defaultValue, landscape.get(0, 0));
        Assert.assertEquals(defaultValue, landscape.get(0, 19));
        Assert.assertEquals(defaultValue, landscape.get(9, 0));
        Assert.assertEquals(defaultValue, landscape.get(9, 19));
        Assert.assertEquals(defaultValue, portrait.get(0, 0));
        Assert.assertEquals(defaultValue, portrait.get(0, 9));
        Assert.assertEquals(defaultValue, portrait.get(19, 0));
        Assert.assertEquals(defaultValue, portrait.get(19, 9));
    }

    @Test(expected = IllegalArgumentException.class)
    // Test out or range x,y values throws exception
    public void getPoorX1() {
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(10, 10, defaultValue).getImmutable();
        underTest.get(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    // Test out or range x,y values throws exception
    public void getPoorX2() {
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(10, 20, defaultValue).getImmutable();
        underTest.get(10, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    // Test out or range x,y values throws exception
    public void getPoorY1() {
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        underTest.get(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    // Test out or range x,y values throws exception
    public void getPoorY2() {
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        underTest.get(0, 10);
    }

    @Test
    public void set_1() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN new value is set
        ImmutableLayerMap2D<Byte>.ImmutableMap2D actual = underTest.set(5, 6, (byte) 7);
        // THEN new layer created, old layer unaffected
        Assert.assertEquals(new Byte((byte) 7), actual.get(5, 6));
        Assert.assertEquals(1, actual.layerSize());
        Assert.assertEquals(defaultValue, underTest.get(5, 6));
    }

    @Test
    public void set_2() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN new value is set that does not modify old value
        ImmutableLayerMap2D<Byte>.ImmutableMap2D actual = underTest.set(5, 6, (byte) 0);
        // THEN new layer not created
        Assert.assertEquals(new Byte((byte) 0), actual.get(5, 6));
        Assert.assertEquals(0, actual.layerSize());
        Assert.assertEquals(1, underTest.getLayerCount());
        Assert.assertEquals(underTest, actual);
    }

    @Test
    public void layerCount_1() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN we add multiple layers and hold onto the layer references
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer2 = underTest.set(5, 6, (byte) 7);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer3 = layer2.set(5, 6, (byte) 6);
        // THEN they are not collapsed
        Assert.assertEquals(3, layer3.getLayerCount());
        assertLogDoesNotContain("MERGE");
    }

    @Test
    public void merge_down() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN we add multiple layers and let the layers get garbage collected and do a read to collapse layers
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer2 = underTest.set(5, 6, (byte) 1);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer3 = layer2.set(5, 7, (byte) 2);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer4 = layer3.set(5, 8, (byte) 3);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer5 = layer4.set(5, 9, (byte) 4);
        //noinspection UnusedAssignment
        layer2 = null;
        //noinspection UnusedAssignment
        layer3 = null;
        System.gc();
        layer4.get(5, 5);
        //noinspection UnusedAssignment
        layer4 = null;
        System.gc();
        FrameworkLogger.getInstance().clearLog();
        layer5.get(5, 5);
        // THEN the final layer has been merged using merge DOWN
        Assert.assertEquals(2, layer5.getLayerCount());
        Assert.assertEquals(4, layer5.layerSize());
        Assert.assertEquals(new Byte((byte) 1), layer5.get(5, 6));
        Assert.assertEquals(new Byte((byte) 2), layer5.get(5, 7));
        Assert.assertEquals(new Byte((byte) 3), layer5.get(5, 8));
        Assert.assertEquals(new Byte((byte) 4), layer5.get(5, 9));
        Assert.assertEquals(new Byte((byte) 0), layer5.get(1, 1));
        assertLogContains("MERGE DOWN");
        assertLogDoesNotContain("MERGE UP");
    }

    @Test
    public void merge_up() {
        // GIVEN a new ImmutableLayerMap2d immutable with layer5 with 3 changes, and layer 2with one change
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer2 = underTest.set(5, 6, (byte) 1);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer3 = layer2.set(5, 7, (byte) 2);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer4 = layer3.set(5, 8, (byte) 3);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer5 = layer4.set(5, 9, (byte) 4);
        //noinspection UnusedAssignment
        layer3 = null;
        //noinspection UnusedAssignment
        layer4 = null;
        System.gc();
        layer5.get(5, 5);
        FrameworkLogger.getInstance().clearLog();
        Assert.assertEquals(1, layer2.layerSize());
        Assert.assertEquals(3, layer5.layerSize());
        // WHEN we remove layer 2 and merge
        //noinspection UnusedAssignment
        layer2 = null;
        System.gc();
        layer5.get(5, 5);
        // THEN the layers have been merged using merge UP
        Assert.assertEquals(2, layer5.getLayerCount());
        Assert.assertEquals(4, layer5.layerSize());
        Assert.assertEquals(new Byte((byte) 1), layer5.get(5, 6));
        Assert.assertEquals(new Byte((byte) 2), layer5.get(5, 7));
        Assert.assertEquals(new Byte((byte) 3), layer5.get(5, 8));
        Assert.assertEquals(new Byte((byte) 4), layer5.get(5, 9));
        Assert.assertEquals(new Byte((byte) 0), layer5.get(1, 1));
        assertLogContains("MERGE UP");
        assertLogDoesNotContain("MERGE DOWN");
    }

    @Test
    public void layerCount_2() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN we add multiple layers and let the layers get garbage collected and do a read to collapse layers
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer2 = underTest.set(5, 6, (byte) 1);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer3 = layer2.set(5, 6, (byte) 2);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer4 = layer3.set(5, 6, (byte) 3);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer5 = layer4.set(5, 6, (byte) 4);
        //noinspection UnusedAssignment
        layer2 = null;
        //noinspection UnusedAssignment
        layer3 = null;
        //noinspection UnusedAssignment
        layer4 = null;
        System.gc();
        layer5.get(5, 5);
        // THEN the layers have been merged using merge UP
        Assert.assertEquals(2, layer5.getLayerCount());
        Assert.assertEquals(1, layer5.layerSize());
        assertLogContains("MERGE UP");
        assertLogDoesNotContain("MERGE DOWN");
    }

    @Test
    public void layerCount_mergeDown_removes_unneeded() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN when we change a value then change it back, remove the reference to the 1st change, and let the layers merge
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer2 = underTest.set(5, 6, (byte) 1);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D layer5 = layer2.set(5, 6, (byte) 0);
        //noinspection UnusedAssignment
        layer2 = null;
        System.gc();
        layer5.get(5, 5);
        // THEN the reset to default value should not be in the resulting layers
        Assert.assertEquals(2, layer5.getLayerCount());
        Assert.assertEquals(0, layer5.layerSize());
        assertLogContains("MERGE UP");
        assertLogDoesNotContain("MERGE DOWN");
    }

    @Test
    public void original_layer_can_be_merged() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN when we change a value then change it back, remove the reference to the 1st change, and let the layers merge
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable1 = underTest.set(5, 6, (byte) 1);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable2 = immutable1.set(5, 6, (byte) 0);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        immutable1 = null;
        System.gc();
        immutable2.get(5, 5);
        // THEN the reset to default value should not be in the resulting layers
        Assert.assertEquals(1, immutable2.getLayerCount());
        Assert.assertEquals(0, immutable2.layerSize());
        assertLogContains("MERGE UP");
        assertLogDoesNotContain("MERGE DOWN");
    }

    @Test
    public void cannot_merge_layer_with_upwards_references() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN when we create 2 immutables from this layer, and let the original immutable reference get garbage collected
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable1 = underTest.set(5, 6, (byte) 2);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable11 = immutable1.set(5, 6, (byte) 3);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable2 = underTest.set(5, 6, (byte) 4);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        immutable1 = null;
        System.gc();
        immutable11.get(5, 5);
        immutable2.get(5, 5);
        // THEN the original layer should NOT be MERGED
        Assert.assertEquals(2, immutable11.getLayerCount());
        Assert.assertEquals(2, immutable2.getLayerCount());
    }

    @Test
    public void cannot_merge_layer_with_upwards_references_will_eventually_merge() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN when we create 2 immutables from this layer, and let the original immutable reference get garbage collected
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable1 = underTest.set(5, 6, (byte) 2);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable11 = immutable1.set(5, 6, (byte) 3);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable2 = underTest.set(5, 6, (byte) 4);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        immutable1 = null;
        System.gc();
        immutable11.get(5, 5);
        immutable2.get(5, 5);
        // THEN the original layer should NOT be MERGED
        Assert.assertEquals(2, immutable11.getLayerCount());
        Assert.assertEquals(2, immutable2.getLayerCount());
        // WHEN the other branches are removed
        //noinspection UnusedAssignment
        immutable2 = null;
        System.gc();
        FrameworkLogger.getInstance().clearLog();
        immutable11.get(5, 5);
        // THEN they should be MERGED
        Assert.assertEquals(1, immutable11.getLayerCount());
        assertLogContains("MERGE UP");
        assertLogDoesNotContain("MERGE DOWN");
    }

    @Test
    public void layers_upstream_in_different_branch_will_merge() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN when we create 2 immutable branches from this layer
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable1 = underTest.set(5, 6, (byte) 2);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable11 = immutable1.set(5, 6, (byte) 3);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable2 = underTest.set(5, 6, (byte) 4);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable21 = immutable2.set(5, 6, (byte) 5);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable211 = immutable21.set(5, 6, (byte) 6);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        immutable1 = null;
        //noinspection UnusedAssignment
        immutable21 = null;
        System.gc();
        immutable11.get(5, 5);
        immutable2.get(5, 5);
        // THEN the original layer should NOT be MERGED
        Assert.assertEquals(2, immutable11.getLayerCount());
        Assert.assertEquals(2, immutable2.getLayerCount());
        // WHEN the other branches are removed
        //noinspection UnusedAssignment
        immutable2 = null;
        System.gc();
        FrameworkLogger.getInstance().clearLog();
        immutable211.set(5, 6, (byte) 7);
        Assert.assertEquals(new Byte((byte) 3), immutable11.get(5, 6));
        // THEN they should NOT be  MERGED as underTest is still being used by immutable211
        Assert.assertEquals(2, immutable11.getLayerCount());
        Assert.assertEquals(3, immutable11.getAllLayerCount());
    }

    @Test
    public void change_mutable_layer_does_not_break_mutability() {
        // GIVEN a new ImmutableLayerMap2d immutable
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue).getImmutable();
        // WHEN when we create 2 immutables from this layer, and let the original immutable reference get garbage collected
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable1 = underTest.set(5, 6, (byte) 2);
        ImmutableLayerMap2D<Byte>.Map2D mutable = immutable1.getMutable();
        mutable.set(5, 6, (byte) 3);
        mutable.set(5, 7, (byte) 0);
        // THEN
        Assert.assertEquals(2, immutable1.getLayerCount());
        Assert.assertEquals(1, immutable1.layerSize());
        Assert.assertEquals(new Byte((byte) 2), immutable1.get(5, 6));
        Assert.assertEquals(3, mutable.layerCount());
        Assert.assertEquals(1, mutable.layerSize());
        Assert.assertEquals(new Byte((byte) 3), mutable.get(5, 6));
    }

    @Test
    public void add_immutable_to_mutable_layer() {
        // GIVEN a new ImmutableLayerMap2d immutable, with a mutable layer with changes
        ImmutableLayerMap2D<Byte> underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue);
        ImmutableLayerMap2D<Byte>.Map2D mutable = underTest.getMutable();
        mutable.set(5, 5, (byte) 1);
        // WHEN we create a new Immutable layer and make further changes to the mutable layer
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable = mutable.getImmutable();
        mutable.set(6, 6, (byte) 2);
        mutable.set(7, 7, (byte) 3);
        // THEN
        Assert.assertEquals(2, mutable.layerCount());
        Assert.assertEquals(2, mutable.layerSize());
        Assert.assertEquals(new Byte((byte) 1), mutable.get(5, 5));
        Assert.assertEquals(new Byte((byte) 2), mutable.get(6, 6));
        Assert.assertEquals(new Byte((byte) 3), mutable.get(7, 7));

        Assert.assertEquals(1, immutable.getLayerCount());
        Assert.assertEquals(1, immutable.layerSize());
        Assert.assertEquals(new Byte((byte) 1), immutable.get(5, 5));
        Assert.assertEquals(new Byte((byte) 0), immutable.get(6, 6));
        Assert.assertEquals(new Byte((byte) 0), immutable.get(7, 7));
    }

    @Test
    public void immutable_to_mutable() {
        // GIVEN a new ImmutableLayerMap2d immutable layer with changes
        ImmutableLayerMap2D<Byte> underTest = new ImmutableLayerMap2D<>(20, 10, defaultValue);
        ImmutableLayerMap2D<Byte>.ImmutableMap2D immutable =
                underTest.getImmutable().set(5, 5, (byte) 1);
        System.gc();
        // WHEN we create a new mutable layer and make further changes to the mutable layer
        ImmutableLayerMap2D<Byte>.Map2D mutable = immutable.getMutable();
        mutable.set(6, 6, (byte) 2);
        mutable.set(7, 7, (byte) 3);
        // THEN
        Assert.assertEquals(1, immutable.getLayerCount());
        Assert.assertEquals(1, immutable.layerSize());
        Assert.assertEquals(new Byte((byte) 1), immutable.get(5, 5));
        Assert.assertEquals(new Byte((byte) 0), immutable.get(6, 6));
        Assert.assertEquals(new Byte((byte) 0), immutable.get(7, 7));

        Assert.assertEquals(2, mutable.layerCount());
        Assert.assertEquals(2, mutable.layerSize());
        Assert.assertEquals(new Byte((byte) 1), immutable.get(5, 5));
        Assert.assertEquals(new Byte((byte) 2), mutable.get(6, 6));
        Assert.assertEquals(new Byte((byte) 3), mutable.get(7, 7));
    }

    @Test
    public void benchmark() {
        FrameworkLogger.getInstance().setLevel(ImmutableLayerMap2D.class.getCanonicalName(), Level.OFF);
        int ITERATIONS = 100;
        int WIDTH = 1000;
        int HEIGHT = 1000;
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern("YYYY MM dd HH:mm:ss SSS");

        //MutableMap2D<Byte> underTest = new MutableMap2D<>(WIDTH, HEIGHT, defaultValue);
        //ImmutableLayerMap2D<Byte>.Map2D underTest = new ImmutableLayerMap2D<>(WIDTH, HEIGHT, defaultValue).getMutable();
        ImmutableLayerMap2D<Byte>.ImmutableMap2D underTest = new ImmutableLayerMap2D<>(WIDTH, HEIGHT, defaultValue).getImmutable();

        Random random = new Random();
        LocalDateTime start = LocalDateTime.now();

        for (int i = 0; i < ITERATIONS; i++) {
            underTest = underTest.set(random.nextInt(WIDTH), random.nextInt(HEIGHT), (byte) random.nextInt(64));
            if (i % 500 == 0) System.out.println("i = " + i);
        }

        LocalDateTime end = LocalDateTime.now();
        System.out.println("start    " + sdf.format(start));
        System.out.println("end      " + sdf.format(end));
        System.out.println("milli    " + Duration.between(start, end).toMillis());
        System.out.println("milli/op " + (float) Duration.between(start, end).toMillis() / ITERATIONS);
    }
}
