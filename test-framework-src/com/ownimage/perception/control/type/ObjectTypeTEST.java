package com.ownimage.perception.control.type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.control.type.ObjectMetaType;
import com.ownimage.framework.control.type.ObjectType;

public class ObjectTypeTEST {

	public static class TestObject {
		private final String mId;

		public TestObject(final String pId) {
			mId = pId;
		}

		@Override
		public String toString() {
			return mId;
		}
	}

	public static TestObject mInvalid = new TestObject("invalid");
	public static TestObject mValid = new TestObject("valid");
	public static TestObject mOther = new TestObject("other");
	public static List<TestObject> mValidList;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mValidList = new Vector<TestObject>();
		mValidList.add(mValid);
		mValidList.add(mOther);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	// construct null meta model
	public void ObjectType_01() {
		ObjectType<TestObject> test = new ObjectType<TestObject>(mValid, null);
		assertTrue("error", false);
	}

	@Test
	// construct with meta model
	public void ObjectType_02() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test = new ObjectType<TestObject>(mValid, metaModel);
	}

	@Test(expected = IllegalArgumentException.class)
	// construct with meta model with invalid value
	public void ObjectType_03() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test = new ObjectType<TestObject>(mInvalid, metaModel);
	}

	@Test
	// clone
	public void ObjectType_clone_01() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test1 = new ObjectType<TestObject>(mValid, metaModel);
		ObjectType<TestObject> test2 = test1.clone();
		assertTrue("Meta Model must match", test1.getMetaModel() == test2.getMetaModel());
		assertTrue("Value must match", test1.getValue() == test2.getValue());
	}

	@Test
	// valid
	public void ObjectType_getString_01() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test1 = new ObjectType<TestObject>(mValid, metaModel);
		assertTrue("getString", "valid".equals(test1.getString()));
	}

	@Test
	// valid
	public void ObjectType_getValue_01() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test1 = new ObjectType<TestObject>(mValid, metaModel);
		assertTrue("getValue", mValid == test1.getValue());
	}

	@Test
	// valid
	public void ObjectType_setString_01() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test1 = new ObjectType<TestObject>(mValid, metaModel);
		test1.setString("other");
		assertTrue("getValue", mOther == test1.getValue());
	}

	@Test(expected = IllegalArgumentException.class)
	// invalid
	public void ObjectType_setString_02() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test1 = new ObjectType<TestObject>(mValid, metaModel);
		test1.setString("XXX");
		assertTrue("getValue", mOther == test1.getValue());
	}

	@Test
	// valid
	public void ObjectType_setValue_01() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test1 = new ObjectType<TestObject>(mValid, metaModel);
		test1.setValue(mOther);
		assertTrue("setValue", mOther == test1.getValue());
	}

	@Test
	// valid
	public void ObjectType_setValue_02() {
		ObjectMetaType<TestObject> metaModel = new ObjectMetaType<>(mValidList, true);
		ObjectType<TestObject> test1 = new ObjectType<TestObject>(mValid, metaModel);
		assertFalse("setValue", test1.setValue(mInvalid));
		assertTrue("setValue", mValid == test1.getValue());
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}