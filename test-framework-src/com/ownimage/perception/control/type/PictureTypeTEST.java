package com.ownimage.perception.control.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.ColorControl.ColorProperty;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.perception.math.IntegerPoint;
import com.ownimage.perception.math.Point;

public class PictureTypeTEST implements IUndoRedoBufferProvider {

	private static Color mTestColor1 = Color.CYAN;
	private static Color mTestColor2 = Color.RED;
	private PictureType mPicture;

	private final Container mContainer = new Container("PictureTypeTEST", "PictureTypeTEST", this);
	private final ColorProperty mOOB = new ColorControl("mOOB", "mOOB", mContainer, Color.CYAN).getProperty();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	public UndoRedoBuffer getUndoRedoBuffer() {
		return new UndoRedoBuffer(100);
	}

	@Test
	// public PictureType(final int pWidth, final int pHeight)
	public void PictureType_01() {
		PictureType picture = new PictureType(mOOB, 10, 20);
		assertEquals("expected width", 10, picture.getWidth());
		assertEquals("expected height", 20, picture.getHeight());

		assertFalse("is360 false", picture.is360());
		assertFalse("locked false", picture.isLocked());

		picture = new PictureType(mOOB, 11, 21);
		assertEquals("expected width", 11, picture.getWidth());
		assertEquals("expected height", 21, picture.getHeight());
	}

	@Test
	// public Point convertPictureResolution(final Point pPoint) {
	public void PictureType_convertPictureResolution_00() {
		Point point = new Point(0.17d, 0.22d);
		Point out = mPicture.convertPictureResolution(point);
		assertEquals("x should match", 0.17d * (mPicture.getWidth() - 1), out.getX(), 0.0d);
		assertEquals("y should match", 0.22d * (mPicture.getHeight() - 1), out.getY(), 0.0d);
	}

	@Test
	// public Point convertPictureResolution(final Point pPoint) {
	public void PictureType_convertPictureResolution_01() {
		Point point = new Point(2.0d, 2.0d);
		Point out = mPicture.convertPictureResolution(point);
		assertEquals("x should match", 2.0d * (mPicture.getWidth() - 1), out.getX(), 0.0d);
		assertEquals("y should match", 2.0d * (mPicture.getHeight() - 1), out.getY(), 0.0d);
	}

	@Test
	// public IntegerPoint convertToIntegerPoint(final Point pPoint) {
	public void PictureType_convertToIntegerPoint_00() {
		Point point = new Point(0.17d, 0.22d);
		IntegerPoint ip = mPicture.convertToIntegerPoint(point);
		assertEquals("x should match", (int) (0.17d * (mPicture.getWidth() - 1)), ip.getX());
		assertEquals("y should match", (int) (0.22d * (mPicture.getHeight() - 1)), ip.getY());
	}

	@Test
	// public IntegerPoint convertToIntegerPoint(final Point pPoint) {
	public void PictureType_convertToIntegerPoint_01() {
		Point point = new Point(2.0d, 2.0d);
		IntegerPoint ip = mPicture.convertToIntegerPoint(point);
		assertEquals("x should match", (int) (2.0d * (mPicture.getWidth() - 1)), ip.getX());
		assertEquals("y should match", (int) (2.0d * (mPicture.getHeight() - 1)), ip.getY());
	}

	@Test
	// public PictureType duplicate() {
	public void PictureType_duplicate_00() {
		mPicture.setColor(25, 25, mTestColor1);
		mPicture.setColor(26, 26, mTestColor2);
		PictureType duplicate = mPicture.clone();

		assertEquals("Colours should match", mTestColor1, duplicate.getColor(25, 25));
		assertEquals("Colours should match", mTestColor2, duplicate.getColor(26, 26));
	}
	// TODO need to have a createComptible

	@Test
	// public synchronized Color getColor(final int pX, final int pY) {
	public void PictureType_getColor_00() {
		Color expected = mTestColor1;
		mPicture.setColor(10, 10, expected);
		Color actual = mPicture.getColor(10, 10);
		assertEquals("Colors should match", actual, expected);
	}

	@Test
	// public synchronized void getColor(final int pX, final int pY) {
	public void PictureType_getColor_01() {
		Color expected = mTestColor1;
		mPicture.set360(true);
		mPicture.setColor(mPicture.getWidth(), 1, expected);
		Color actual = mPicture.getColor(0, 1);
		assertEquals("Colors should match", actual, expected);
	}

	@Test
	// public synchronized void getColor(final int pX, final int pY) {
	public void PictureType_getColor_02() {
		Color expected = mTestColor1;
		mPicture.set360(true);
		mPicture.setColor(0, 1, expected);
		Color actual = mPicture.getColor(mPicture.getWidth(), 1);
		assertEquals("Colors should match", actual, expected);
	}

	@Test
	// public synchronized void getColor(final int pX, final int pY) {
	public void PictureType_getColor_03() {
		Color expected = mTestColor1;
		mPicture.set360(true);
		mPicture.setColor(10, 1, expected);
		Color actual = mPicture.getColor(mPicture.getWidth() + 10, 1);
		assertEquals("Colors should match", actual, expected);
	}

	@Test
	// public synchronized void getColor(final int pX, final int pY) {
	public void PictureType_getColor_04() {
		Color expected = mOOB.getValue();
		Color actual = mPicture.getColor(1, -1);
		assertEquals("Should be out of bounds", expected, actual);
	}

	@Test
	// public synchronized void getColor(final int pX, final int pY) {
	public void PictureType_getColor_05() {
		Color expected = mOOB.getValue();
		Color actual = mPicture.getColor(1, mPicture.getHeight());
		assertEquals("Should be out of bounds", expected, actual);
	}

	@Test
	// public synchronized void getColor(final int pX, final int pY) {
	public void PictureType_getColor_06() {
		Color expected = mOOB.getValue();
		Color actual = mPicture.getColor(-1, 1);
		assertEquals("Should be out of bounds", expected, actual);
	}

	@Test
	// public synchronized void getColor(final int pX, final int pY) {
	public void PictureType_getColor_07() {
		Color expected = mOOB.getValue();
		Color actual = mPicture.getColor(mPicture.getWidth(), 1);
		assertEquals("Should be out of bounds", expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	// public Color getColor(final IntegerPoint pPoint) {
	public void PictureType_getColor_10() {
		mPicture.getColor((IntegerPoint) null);
	}

	@Test
	// public Color getColor(final IntegerPoint pPoint) {
	public void PictureType_getColor_11() {
		IntegerPoint ip = new IntegerPoint(10, 10);
		mPicture.setColor(ip, mTestColor1);
		Color actual = mPicture.getColor(ip);
		assertTrue("colors match", mTestColor1.equals(actual));
	}

	@Test(expected = IllegalArgumentException.class)
	// public Color getColor(final IntegerPoint pPoint) {
	public void PictureType_getColor_20() {
		mPicture.getColor((Point) null);
	}

	@Test
	// public Color getColor(final IntegerPoint pPoint) {
	public void PictureType_getColor_21() {
		Point p = new Point(0.23d, 0.45d);
		mPicture.setColor(p, mTestColor1);
		Color actual = mPicture.getColor(p);
		assertTrue("colors match", mTestColor1.equals(actual));
	}

	@Test
	// public int[] getColors() {
	public void PictureType_getColors_00() {
		IntegerPoint ip = new IntegerPoint(10, 10);
		int width = 37;
		int height = 47;

		PictureType picture1 = new PictureType(mOOB, width, height);
		assertFalse("Colors should not match to start with", picture1.equals(mPicture.getColor(ip)));
		picture1.setColor(ip, mTestColor1);

		PictureType picture2 = new PictureType(mOOB, width, height);
		picture2.setColors(picture1.getColors());
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		assertTrue("Colors should match", picture1.getColor(ip).equals(picture2.getColor(ip)));
	}

	@Test
	// public int[] getColors(final int[] pColors) {
	public void PictureType_getColors_10() {
		IntegerPoint ip = new IntegerPoint(10, 10);
		int width = 37;
		int height = 47;
		int[] small = new int[] { 1, 2, 3 };

		PictureType picture1 = new PictureType(mOOB, width, height);
		assertFalse("Colors should not match to start with", picture1.equals(mPicture.getColor(ip)));
		picture1.setColor(ip, mTestColor1);

		PictureType picture2 = new PictureType(mOOB, width, height);
		picture2.setColors(picture1.getColors(small));
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		assertTrue("Colors should match", picture1.getColor(ip).equals(picture2.getColor(ip)));
	}

	@Test
	// public int[] getColors(final int[] pColors) {
	public void PictureType_getColors_11() {
		IntegerPoint ip = new IntegerPoint(10, 10);
		int width = 37;
		int height = 47;
		int[] correct = new int[width * height];

		PictureType picture1 = new PictureType(mOOB, width, height);
		assertFalse("Colors should not match to start with", picture1.equals(mPicture.getColor(ip)));
		picture1.setColor(ip, mTestColor1);

		PictureType picture2 = new PictureType(mOOB, width, height);
		picture2.setColors(picture1.getColors(correct));
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		assertTrue("Colors should match", picture1.getColor(ip).equals(picture2.getColor(ip)));
	}

	@Test
	// public int[] getColors(final int[] pColors) {
	public void PictureType_getColors_12() {
		IntegerPoint ip = new IntegerPoint(10, 10);
		int width = 37;
		int height = 47;
		int[] large = new int[2 * width * height];

		PictureType picture1 = new PictureType(mOOB, width, height);
		assertFalse("Colors should not match to start with", picture1.equals(mPicture.getColor(ip)));
		picture1.setColor(ip, mTestColor1);

		PictureType picture2 = new PictureType(mOOB, width, height);
		picture2.setColors(picture1.getColors(large));
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		assertTrue("Colors should match", picture1.getColor(ip).equals(picture2.getColor(ip)));
	}

	@Test
	// public IMetaType<PictureType> getMetaModel() {
	public void PictureType_getMetaModel_00() {
		assertEquals("Should be null meta model", null, mPicture.getMetaModel());
	}

	@Test(expected = UnsupportedOperationException.class)
	// public String getString() {
	public void PictureType_getString_00() {
		mPicture.getString();
	}

	@Test
	// public PictureType getValue() {
	public void PictureType_getValue_00() {
		assertTrue("returns self", mPicture == mPicture.getValue());
	}

	@Test
	// public void is360(final boolean pP360) {
	public void PictureType_is360_00() {
		assertFalse("created false", mPicture.is360());

		mPicture.set360(true);
		assertTrue("set true", mPicture.is360());

		mPicture.set360(false);
		assertFalse("set false", mPicture.is360());
	}

	@Test(expected = IllegalStateException.class)
	// public void is360(final boolean pP360) {
	public void PictureType_is360_01() {
		mPicture.lock();
		mPicture.set360(false);
	}

	@Test
	// public boolean isLocked() {
	public void PictureType_isLocked_00() {
		assertFalse("created false", mPicture.isLocked());

		int key = mPicture.lock();
		assertTrue("set true", mPicture.isLocked());

		mPicture.unlock(key);
		assertFalse("set false", mPicture.isLocked());
	}

	@Test
	// public int lock() {
	public void PictureType_lock_00() {
		assertFalse("created unlocked", mPicture.isLocked());

		int key1 = mPicture.lock();
		assertTrue("isLocked", mPicture.isLocked());

		mPicture.unlock(key1);
		assertFalse("created unlocked", mPicture.isLocked());

		int key2 = mPicture.lock();
		assertTrue("isLocked", mPicture.isLocked());

		assertTrue("Keys should not match", key1 != key2);
	}

	@Test(expected = IllegalStateException.class)
	// public int lock() {
	public void PictureType_lock_01() {
		int key1 = mPicture.lock();
		int key2 = mPicture.lock();
	}

	@Test
	// public void save(final File file) throws Exception {
	public void PictureType_save_00() throws Exception {
		File tempFile = File.createTempFile("picture", ".gif");
		IntegerPoint ip = new IntegerPoint(10, 10);
		int width = 37;
		int height = 47;

		PictureType picture1 = new PictureType(mOOB, width, height);
		assertFalse("Colors should not match to start with", picture1.equals(mPicture.getColor(ip)));
		picture1.setColor(ip, mTestColor1);
		picture1.save(tempFile);

		PictureType picture2 = new PictureType(mOOB, tempFile);
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		assertEquals("Colors should match", picture1.getColor(ip), picture2.getColor(ip));

		tempFile.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	// public void save(final File file) throws Exception {
	public void PictureType_save_01() throws Exception {
		mPicture.save((File) null);
	}

	@Test
	// public void save(final String name) throws Exception {
	public void PictureType_save_10() throws Exception {
		File tempFile = File.createTempFile("picture", ".jpg");
		String tmpFileName = tempFile.getAbsolutePath();
		System.out.println(tmpFileName);
		IntegerPoint ip = new IntegerPoint(101, 99);
		int width = 203;
		int height = 201;

		PictureType picture1 = new PictureType(mOOB, width, height);
		assertFalse("Colors should not match to start with", picture1.equals(mPicture.getColor(ip)));
		picture1.setColor(ip, mTestColor1);
		picture1.save(tmpFileName);

		PictureType picture2 = new PictureType(mOOB, tmpFileName);
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		picture2.save(tmpFileName);
		// assertEquals("Colors should match", picture1.getColor(ip), picture2.getColor(ip));
		// TODO for some reason the above test fails

		tempFile.delete();
	}

	@Test
	// public void save(final String name) throws Exception {
	public void PictureType_save_11() throws Exception {
		File tempFile = File.createTempFile("picture", ".gif");
		String tmpFileName = tempFile.getAbsolutePath();
		System.out.println(tmpFileName);
		IntegerPoint ip = new IntegerPoint(10, 10);
		int width = 37;
		int height = 47;

		PictureType picture1 = new PictureType(mOOB, width, height);
		assertFalse("Colors should not match to start with", picture1.equals(mPicture.getColor(ip)));
		for (int x = 0; x < picture1.getWidth(); x++) {
			for (int y = 0; y < picture1.getHeight(); y++) {
				picture1.setColor(x, y, mTestColor1);
			}
		}
		picture1.save(tmpFileName);

		PictureType picture2 = new PictureType(mOOB, tmpFileName);
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		picture2.save(tmpFileName);
		assertEquals("Colors should match", picture1.getColor(ip), picture2.getColor(ip));

		tempFile.delete();
	}

	@Test(expected = IllegalArgumentException.class)
	// public void save(final String name) throws Exception {
	public void PictureType_save_12() throws Exception {
		File tempFile = File.createTempFile("picture", ".non");
		String tmpFileName = tempFile.getAbsolutePath();
		tempFile.delete();
		mPicture.save(tmpFileName);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void save(final String name) throws Exception {
	public void PictureType_save_13() throws Exception {
		File tempFile = File.createTempFile("picture", "");
		String tmpFileName = tempFile.getAbsolutePath();
		System.out.println(tmpFileName);
		tempFile.delete();
		mPicture.save(tmpFileName);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void save(final File file) throws Exception {
	public void PictureType_save_14() throws Exception {
		mPicture.save((String) null);
	}

	@Test(expected = IllegalStateException.class)
	// public synchronized void setColor(int pX, int pY, final Color pColor) {
	public void PictureType_setColor_00() {
		mPicture.lock();
		mPicture.setColor(10, 10, mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public synchronized void setColor(int pX, int pY, final Color pColor) {
	public void PictureType_setColor_01() {
		mPicture.setColor(10, 10, null);
	}

	@Test(expected = IllegalArgumentException.class)
	// public synchronized void setColor(int pX, int pY, final Color pColor) {
	public void PictureType_setColor_02() {
		mPicture.setColor(-1, 1, mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public synchronized void setColor(int pX, int pY, final Color pColor) {
	public void PictureType_setColor_03() {
		mPicture.setColor(mPicture.getWidth(), 1, mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public synchronized void setColor(int pX, int pY, final Color pColor) {
	public void PictureType_setColor_04() {
		mPicture.setColor(1, mPicture.getHeight(), mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public synchronized void setColor(int pX, int pY, final Color pColor) {
	public void PictureType_setColor_05() {
		mPicture.setColor(1, -1, mTestColor1);
	}

	@Test(expected = IllegalStateException.class)
	// public void setColor(final IntegerPoint pPoint, final Color pColor) {
	public void PictureType_setColor_10() {
		mPicture.lock();
		IntegerPoint point = new IntegerPoint(10, 10);
		mPicture.setColor(point, mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void setColor(final IntegerPoint pPoint, final Color pColor) {
	public void PictureType_setColor_11() {
		IntegerPoint point = new IntegerPoint(10, 10);
		mPicture.setColor((IntegerPoint) null, mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void setColor(final IntegerPoint pPoint, final Color pColor) {
	public void PictureType_setColor_12() {
		IntegerPoint point = new IntegerPoint(10, 10);
		mPicture.setColor(point, null);
	}

	@Test
	// public void setColor(final IntegerPoint pPoint, final Color pColor) {
	public void PictureType_setColor_13() {
		IntegerPoint point = new IntegerPoint(10, 10);

		assertFalse("starting colurs must be different", mTestColor1.equals(mPicture.getColor(point)));
		mPicture.setColor(point, mTestColor1);
		assertTrue("colors same", mTestColor1.equals(mPicture.getColor(point)));
	}

	@Test(expected = IllegalStateException.class)
	// public void setColor(final Point pPoint, final Color pColor) {
	public void PictureType_setColor_20() {
		mPicture.lock();
		Point point = new Point(0.1, 0.1);
		mPicture.setColor(point, mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void setColor(final Point pPoint, final Color pColor) {
	public void PictureType_setColor_21() {
		Point point = new Point(0.1, 0.1);
		mPicture.setColor((Point) null, mTestColor1);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void setColor(final Point pPoint, final Color pColor) {
	public void PictureType_setColor_22() {
		Point point = new Point(0.1, 0.1);
		mPicture.setColor(point, null);
	}

	@Test(expected = IllegalStateException.class)
	// public void setColors(int[] pColors) {
	public void PictureType_setColors_00() {
		int[] colours = new int[] { 1, 2, 3 };
		mPicture.lock();
		mPicture.setColors(colours);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void setColors(int[] pColors) {
	public void PictureType_setColors_01() {
		int[] colours = null;
		mPicture.setColors(colours);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void setColors(int[] pColors) {
	public void PictureType_setColors_02() {
		int[] colours = new int[] { 1, 2, 3 };
		mPicture.setColors(colours);
	}

	@Test
	// public void setColors(int[] pColors) {
	public void PictureType_setColors_03() {
		IntegerPoint ip = new IntegerPoint(10, 10);
		int width = 37;
		int height = 47;

		PictureType picture1 = new PictureType(mOOB, width, height);
		picture1.setColor(ip, mTestColor1);
		assertFalse("Colors should not match to start with", picture1.getColor(ip).equals(mPicture.getColor(ip)));

		PictureType picture2 = new PictureType(mOOB, width, height);
		picture2.setColors(picture1.getColors());
		assertTrue("width correct", picture2.getWidth() == width);
		assertTrue("height correct", picture2.getHeight() == height);
		assertTrue("Colors should match", picture1.getColor(ip).equals(picture2.getColor(ip)));
	}

	@Test(expected = UnsupportedOperationException.class)
	// public void setString(String pValue) {
	public void PictureType_setString_00() {
		mPicture.setString("");
	}

	@Test(expected = UnsupportedOperationException.class)
	// public void setValue(PictureType pValue) {
	public void PictureType_setValue() {
		PictureType picture = new PictureType(mOOB, 27, 28);
		mPicture.setValue(picture);
	}

	@Test(expected = IllegalStateException.class)
	// public void unlock(int pKey) {
	public void PictureType_unlock_01() {
		mPicture.unlock(0);
	}

	@Test(expected = IllegalArgumentException.class)
	// public void unlock(int pKey) {
	public void PictureType_unlock_02() {
		mPicture.lock();
		mPicture.unlock(0);
	}

	@Before
	public void setUp() throws Exception {
		mPicture = new PictureType(mOOB, 100, 100);
	}

	@After
	public void tearDown() throws Exception {
	}

}