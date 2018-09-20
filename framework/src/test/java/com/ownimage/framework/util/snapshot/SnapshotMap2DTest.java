/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.util.snapshot;

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

public class SnapshotMap2DTest {

    private final Byte defaultValue = (byte) 0;

    @BeforeClass
    public static void beforeClass() {
        FrameworkLogger.getInstance().init(null, "TEST.log");
    }

    @Before
    public void before() {
        FrameworkLogger.getInstance().setLevel(Level.OFF);
        FrameworkLogger.getInstance().setLevel(SnapshotMap2D.class.getCanonicalName(), Level.FINEST);
        FrameworkLogger.getInstance().clearLog();
    }

    private void assertLogContains(final String pString) {
        Assert.assertTrue(FrameworkLogger.getInstance().getLog().contains(pString));
    }

    private void assertLogDoesNotContain(final String pString) {
        Assert.assertFalse(FrameworkLogger.getInstance().getLog().contains(pString));
    }


    @Test
    public void constructor() {
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(10, 10, defaultValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorX() {
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(-10, 10, defaultValue);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorPoorY() {
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(10, -10, defaultValue);
    }

    @Test
    public void get() {
        // GIVEN
        final SnapshotMap2D<Byte> landscape = new SnapshotMap2D<>(10, 20, defaultValue);
        final SnapshotMap2D<Byte> portrait = new SnapshotMap2D<>(20, 10, defaultValue);
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
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(10, 10, defaultValue);
        underTest.get(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    // Test out or range x,y values throws exception
    public void getPoorX2() {
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(10, 20, defaultValue);
        underTest.get(10, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    // Test out or range x,y values throws exception
    public void getPoorY1() {
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        underTest.get(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    // Test out or range x,y values throws exception
    public void getPoorY2() {
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        underTest.get(0, 10);
    }

    @Test
    public void set_1() {
        // GIVEN a new SnapshotMap2D immutable
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN new value is set
        final SnapshotMap2D<Byte> snapshot = underTest.snapshot();
        snapshot.set(5, 6, (byte) 7);
        // THEN new layer created, old layer unaffected
        Assert.assertEquals(new Byte((byte) 7), snapshot.get(5, 6));
        Assert.assertEquals(defaultValue, underTest.get(5, 6));
    }

    @Test
    public void set_2() {
        // GIVEN a new SnapshotMap2D immutable
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN new value is set that does not modify old value
        final SnapshotMap2D<Byte> snapshot = underTest.snapshot();
        snapshot.set(5, 6, (byte) 0);
        // THEN new layer not created
        Assert.assertEquals(new Byte((byte) 0), snapshot.get(5, 6));
    }

    @Test
    public void merge_down() {
        // GIVEN a new SnapshotMap2D immutable
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN we add multiple layers and let the layers get garbage collected and do a read to collapse layers
        SnapshotMap2D<Byte> snapshot2 = underTest.snapshot();
        snapshot2.set(5, 6, (byte) 1);
        SnapshotMap2D<Byte> snapshot3 = snapshot2.snapshot();
        snapshot3.set(5, 7, (byte) 2);
        SnapshotMap2D<Byte> snapshot4 = snapshot3.snapshot();
        snapshot4.set(5, 8, (byte) 3);
        final SnapshotMap2D<Byte> snapshot5 = snapshot4.snapshot();
        snapshot5.set(5, 9, (byte) 4);
        //noinspection UnusedAssignment
        snapshot2 = null;
        //noinspection UnusedAssignment
        snapshot3 = null;
        System.gc();
        snapshot4.get(5, 5);
        //noinspection UnusedAssignment
        snapshot4 = null;
        System.gc();
        FrameworkLogger.getInstance().clearLog();
        snapshot5.get(5, 5);
        // THEN the final layer has been merged using merge DOWN
        Assert.assertEquals(new Byte((byte) 1), snapshot5.get(5, 6));
        Assert.assertEquals(new Byte((byte) 2), snapshot5.get(5, 7));
        Assert.assertEquals(new Byte((byte) 3), snapshot5.get(5, 8));
        Assert.assertEquals(new Byte((byte) 4), snapshot5.get(5, 9));
        Assert.assertEquals(new Byte((byte) 0), snapshot5.get(1, 1));
    }

    @Test
    public void merge_up() {
        // GIVEN a new SnapshotMap2D immutable with snapshot5 with 3 changes, and layer 2with one change
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        SnapshotMap2D<Byte> snapshot2 = underTest.snapshot();
        snapshot2.set(5, 6, (byte) 1);
        SnapshotMap2D<Byte> snapshot3 = snapshot2.snapshot();
        snapshot3.set(5, 7, (byte) 2);
        SnapshotMap2D<Byte> snapshot4 = snapshot3.snapshot();
        snapshot4.set(5, 8, (byte) 3);
        final SnapshotMap2D<Byte> snapshot5 = snapshot4.snapshot();
        snapshot5.set(5, 9, (byte) 4);
        //noinspection UnusedAssignment
        snapshot3 = null;
        //noinspection UnusedAssignment
        snapshot4 = null;
        System.gc();
        snapshot5.get(5, 5);
        FrameworkLogger.getInstance().clearLog();
        // WHEN we remove layer 2 and merge
        //noinspection UnusedAssignment
        snapshot2 = null;
        System.gc();
        snapshot5.get(5, 5);
        // THEN the layers have been merged using merge UP
        Assert.assertEquals(new Byte((byte) 1), snapshot5.get(5, 6));
        Assert.assertEquals(new Byte((byte) 2), snapshot5.get(5, 7));
        Assert.assertEquals(new Byte((byte) 3), snapshot5.get(5, 8));
        Assert.assertEquals(new Byte((byte) 4), snapshot5.get(5, 9));
        Assert.assertEquals(new Byte((byte) 0), snapshot5.get(1, 1));
    }

    @Test
    public void layerCount_2() {
        // GIVEN a new SnapshotMap2D immutable
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN we add multiple layers and let the layers get garbage collected and do a read to collapse layers
        SnapshotMap2D<Byte> snapshot2 = underTest.snapshot();
        snapshot2.set(5, 6, (byte) 1);
        SnapshotMap2D<Byte> snapshot3 = snapshot2.snapshot();
        snapshot3.set(5, 6, (byte) 2);
        SnapshotMap2D<Byte> snapshot4 = snapshot3.snapshot();
        snapshot4.set(5, 6, (byte) 3);
        final SnapshotMap2D<Byte> snapshot5 = snapshot4.snapshot();
        snapshot5.set(5, 6, (byte) 4);
        //noinspection UnusedAssignment
        snapshot2 = null;
        //noinspection UnusedAssignment
        snapshot3 = null;
        //noinspection UnusedAssignment
        snapshot4 = null;
        System.gc();
        snapshot5.get(5, 5);
        // THEN the layers have been merged using merge UP
    }

    @Test
    public void layerCount_mergeDown_removes_unneeded() {
        // GIVEN a new SnapshotMap2D immutable
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN when we change a value then change it back, remove the reference to the 1st change, and let the layers merge
        SnapshotMap2D<Byte> snapshot2 = underTest.snapshot();
        snapshot2.set(5, 6, (byte) 1);
        final SnapshotMap2D<Byte> snapshot5 = snapshot2.snapshot();
        snapshot5.set(5, 6, (byte) 0);
        //noinspection UnusedAssignment
        snapshot2 = null;
        System.gc();
        snapshot5.get(5, 5);
        // THEN the reset to default value should not be in the resulting layers
    }

    @Test
    public void original_layer_can_be_merged() {
        // GIVEN a new SnapshotMap2D immutable
        SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN when we change a value then change it back, remove the reference to the 1st change, and let the layers merge
        SnapshotMap2D<Byte> snapshot1 = underTest.snapshot();
        snapshot1.set(5, 6, (byte) 1);
        final SnapshotMap2D<Byte> snapshot2 = snapshot1.snapshot();
        snapshot2.set(5, 6, (byte) 0);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        snapshot1 = null;
        System.gc();
        snapshot2.get(5, 5);
        // THEN the reset to default value should not be in the resulting layers
    }

    @Test
    public void cannot_merge_layer_with_upwards_references() {
        // GIVEN a new SnapshotMap2D immutable
        SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN when we create 2 immutables from this layer, and let the original immutable reference get garbage collected
        SnapshotMap2D<Byte> snapshot1 = underTest.snapshot();
        snapshot1.set(5, 6, (byte) 2);
        final SnapshotMap2D<Byte> snapshot11 = snapshot1.snapshot();
        snapshot11.set(5, 6, (byte) 3);
        final SnapshotMap2D<Byte> snapshot2 = underTest.snapshot();
        snapshot2.set(5, 6, (byte) 4);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        snapshot1 = null;
        System.gc();
        snapshot11.get(5, 5);
        snapshot2.get(5, 5);
        // THEN the original layer should NOT be MERGED
    }

    @Test
    public void cannot_merge_layer_with_upwards_references_will_eventually_merge() {
        // GIVEN a new SnapshotMap2D immutable
        SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN when we create 2 immutables from this layer, and let the original immutable reference get garbage collected
        SnapshotMap2D<Byte> snapshot1 = underTest.snapshot();
        snapshot1.set(5, 6, (byte) 2);
        final SnapshotMap2D<Byte> snapshot11 = snapshot1.snapshot();
        snapshot11.set(5, 6, (byte) 3);
        SnapshotMap2D<Byte> snapshot2 = underTest.snapshot();
        snapshot2.set(5, 6, (byte) 4);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        snapshot1 = null;
        System.gc();
        snapshot11.get(5, 5);
        snapshot2.get(5, 5);
        // THEN the original layer should NOT be MERGED
        // WHEN the other branches are removed
        //noinspection UnusedAssignment
        snapshot2 = null;
        System.gc();
        FrameworkLogger.getInstance().clearLog();
        snapshot11.get(5, 5);
        // THEN they should be MERGED
    }

    @Test
    public void layers_upstream_in_different_branch_will_merge() {
        // GIVEN a new SnapshotMap2D immutable
        SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN when we create 2 immutable branches from this layer
        SnapshotMap2D<Byte> snapshot1 = underTest.snapshot();
        snapshot1.set(5, 6, (byte) 2);
        final SnapshotMap2D<Byte> snapshot11 = snapshot1.snapshot();
        snapshot11.set(5, 6, (byte) 3);
        SnapshotMap2D<Byte> snapshot2 = underTest.snapshot();
        snapshot2.set(5, 6, (byte) 4);
        SnapshotMap2D<Byte> snapshot3 = snapshot2.snapshot();
        snapshot3.set(5, 6, (byte) 5);
        final SnapshotMap2D<Byte> snapshot31 = snapshot3.snapshot();
        snapshot31.set(5, 6, (byte) 6);
        //noinspection UnusedAssignment
        underTest = null;
        //noinspection UnusedAssignment
        snapshot1 = null;
        //noinspection UnusedAssignment
        snapshot3 = null;
        System.gc();
        snapshot11.get(5, 5);
        snapshot2.get(5, 5);
        // THEN the original layer should NOT be MERGED
        // WHEN the other branches are removed
        //noinspection UnusedAssignment
        snapshot2 = null;
        System.gc();
        FrameworkLogger.getInstance().clearLog();
        snapshot31.set(5, 6, (byte) 7);
        Assert.assertEquals(new Byte((byte) 3), snapshot11.get(5, 6));
        // THEN they should NOT be  MERGED as underTest is still being used by snapshot211
    }

    @Test
    public void change_mutable_layer_does_not_break_mutability() {
        // GIVEN a new SnapshotMap2D immutable
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        // WHEN when we create 2 immutables from this layer, and let the original immutable reference get garbage collected
        final SnapshotMap2D<Byte> snapshot1 = underTest.snapshot();
        snapshot1.set(5, 6, (byte) 2);
        final SnapshotMap2D<Byte> mutable = snapshot1.snapshot();
        mutable.set(5, 6, (byte) 3);
        mutable.set(5, 7, (byte) 0);
        // THEN
        Assert.assertEquals(new Byte((byte) 2), snapshot1.get(5, 6));
        Assert.assertEquals(new Byte((byte) 3), mutable.get(5, 6));
    }

    @Test
    public void add_immutable_to_mutable_layer() {
        // GIVEN a new SnapshotMap2D immutable, with a snapshot1 layer with changes
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        final SnapshotMap2D<Byte> snapshot1 = underTest.snapshot();
        snapshot1.set(5, 5, (byte) 1);
        // WHEN we create a new Immutable layer and make further changes to the snapshot1 layer
        final SnapshotMap2D<Byte> snapShot2 = snapshot1.snapshot();
        snapshot1.set(6, 6, (byte) 2);
        snapshot1.set(7, 7, (byte) 3);
        // THEN
        Assert.assertEquals(new Byte((byte) 1), snapshot1.get(5, 5));
        Assert.assertEquals(new Byte((byte) 2), snapshot1.get(6, 6));
        Assert.assertEquals(new Byte((byte) 3), snapshot1.get(7, 7));

        Assert.assertEquals(new Byte((byte) 1), snapShot2.get(5, 5));
        Assert.assertEquals(new Byte((byte) 0), snapShot2.get(6, 6));
        Assert.assertEquals(new Byte((byte) 0), snapShot2.get(7, 7));
    }

    @Test
    public void immutable_to_mutable() {
        // GIVEN a new SnapshotMap2D snapshot layer with changes
        final SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(20, 10, defaultValue);
        final SnapshotMap2D<Byte> snapshot = underTest.snapshot();
        snapshot.set(5, 5, (byte) 1);
        System.gc();
        // WHEN we create a new mutable layer and make further changes to the mutable layer
        final SnapshotMap2D<Byte> mutable = snapshot.snapshot();
        mutable.set(6, 6, (byte) 2);
        mutable.set(7, 7, (byte) 3);
        // THEN
        Assert.assertEquals(new Byte((byte) 1), snapshot.get(5, 5));
        Assert.assertEquals(new Byte((byte) 0), snapshot.get(6, 6));
        Assert.assertEquals(new Byte((byte) 0), snapshot.get(7, 7));

        Assert.assertEquals(new Byte((byte) 1), snapshot.get(5, 5));
        Assert.assertEquals(new Byte((byte) 2), mutable.get(6, 6));
        Assert.assertEquals(new Byte((byte) 3), mutable.get(7, 7));
    }

    @Test
    public void benchmark() {
        FrameworkLogger.getInstance().setLevel(SnapshotMap2D.class.getCanonicalName(), Level.OFF);
        final int ITERATIONS = 10000;
        final int WIDTH = 1000;
        final int HEIGHT = 1000;
        final DateTimeFormatter sdf = DateTimeFormatter.ofPattern("YYYY MM dd HH:mm:ss SSS");

        //MutableMap2D<Byte> underTest = new MutableMap2D<>(WIDTH, HEIGHT, defaultValue);
        //SnapshotMap2D<Byte>.Map2D underTest = new SnapshotMap2D<>(WIDTH, HEIGHT, defaultValue).getMutable();
        SnapshotMap2D<Byte> underTest = new SnapshotMap2D<>(WIDTH, HEIGHT, defaultValue);

        final Random random = new Random();
        final LocalDateTime start = LocalDateTime.now();

        for (int i = 0; i < ITERATIONS; i++) {
            underTest = underTest.snapshot();
            underTest.set(random.nextInt(WIDTH), random.nextInt(HEIGHT), (byte) random.nextInt(64));
            if (i % 500 == 0) System.out.println("i = " + i);
        }

        final LocalDateTime end = LocalDateTime.now();
        System.out.println("start    " + sdf.format(start));
        System.out.println("end      " + sdf.format(end));
        System.out.println("milli    " + Duration.between(start, end).toMillis());
        System.out.println("milli/op " + (float) Duration.between(start, end).toMillis() / ITERATIONS);
    }
}
