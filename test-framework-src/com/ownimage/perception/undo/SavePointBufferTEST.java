package com.ownimage.perception.undo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ownimage.framework.logging.FrameworkLogger;
import com.ownimage.framework.undo.IUndoRedoAction;
import com.ownimage.framework.undo.SavePointBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;

public class SavePointBufferTEST {


	public final static Logger mLogger = Framework.getLogger();

	private SavePointBuffer mSavePointBuffer;
	private UndoRedoActionSequenceASSISTANT[] mUndoRedoActions;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private void reset() {
		UndoRedoActionSequenceASSISTANT.resetGlobalSequence();

		for (int i = 0; i < 5; i++) {
			mUndoRedoActions[i].reset();
		}
	}

	// normal
	@Test
	public void SavePointBuffer_00() {
		SavePointBuffer b1 = new SavePointBuffer("test1", new Id("x"));
		assertEquals("equals", "test1", b1.getDescription());

		SavePointBuffer b2 = new SavePointBuffer("test2", new Id("x"));
		assertEquals("equals", "test2", b2.getDescription());
	}

	// checks that once the buffer is locked nothing more can be added
	@Test
	public void SavePointBuffer_add_0_00() throws Exception {
		IUndoRedoAction action = new UndoRedoActionASSISTANT("test");
		FrameworkLogger.getInstance().init("logging.properties", "junit.log");
		FrameworkLogger.getInstance().clearLog();
		FrameworkLogger.getInstance().setLevel(Level.SEVERE);
		try {
			mSavePointBuffer.add(action);
		} catch (IllegalStateException pE) {
			String log = FrameworkLogger.getInstance().getLog();
			System.out.println(log);
			assertTrue(log.matches("^.*SEVERE.*com.ownimage.framework.undo.SavePointBuffer.*SavePointBuffer.*has been locked.  add is not allowed.\n"));
			return;
		}
		fail();
	}

	// should fail cant redo until locked and undo called
	@Test(expected = IllegalStateException.class)
	public void SavePointBuffer_redo_0_00a() {
		SavePointBuffer one = new SavePointBuffer("empty", new Id("x"));
		one.add(new UndoRedoActionASSISTANT("test"));
		one.redo();
	}

	// should fail cant redo until locked and undo called
	@Test(expected = IllegalStateException.class)
	public void SavePointBuffer_redo_0_00b() {
		SavePointBuffer one = new SavePointBuffer("empty", new Id("x"));
		one.add(new UndoRedoActionASSISTANT("test"));
		one.lock();
		one.redo();
	}

	// should fail cant call redo out of sequence
	@Test(expected = IllegalStateException.class)
	public void SavePointBuffer_redo_0_01() {
		mSavePointBuffer.redo();
		fail();
	}

	@Test
	public void SavePointBuffer_redo_0_02() {
		mSavePointBuffer.undo();
		reset();
		mSavePointBuffer.redo();
		assertEquals("undoseq", 1, mUndoRedoActions[0].getFiredSequence());
		assertEquals("undoseq", 2, mUndoRedoActions[1].getFiredSequence());
		assertEquals("undoseq", 3, mUndoRedoActions[2].getFiredSequence());
		assertEquals("undoseq", 4, mUndoRedoActions[3].getFiredSequence());
		assertEquals("undoseq", 5, mUndoRedoActions[4].getFiredSequence());
	}

	// should fail cant call redo out of sequence
	@Test(expected = IllegalStateException.class)
	public void SavePointBuffer_redo_0_03() {
		mSavePointBuffer.undo();
		mSavePointBuffer.undo();
		mSavePointBuffer.redo();
		fail();
	}

	// cant undo until locked
	@Test(expected = IllegalStateException.class)
	public void SavePointBuffer_undo_0_00() {
		SavePointBuffer empty = new SavePointBuffer("empty", new Id("x"));
		empty.undo();
	}

	@Test
	public void SavePointBuffer_undo_0_01() {
		SavePointBuffer empty = new SavePointBuffer("empty", new Id("x"));
		empty.lock();
		empty.undo();
	}

	@Test
	public void SavePointBuffer_undo_0_02() {
		mSavePointBuffer.undo();
		assertEquals("undoseq", 1, mUndoRedoActions[4].getFiredSequence());
		assertEquals("undoseq", 2, mUndoRedoActions[3].getFiredSequence());
		assertEquals("undoseq", 3, mUndoRedoActions[2].getFiredSequence());
		assertEquals("undoseq", 4, mUndoRedoActions[1].getFiredSequence());
		assertEquals("undoseq", 5, mUndoRedoActions[0].getFiredSequence());
	}

	// should not be able to call undos out of sequence
	@Test(expected = IllegalStateException.class)
	public void SavePointBuffer_undo_0_03() {
		mSavePointBuffer.undo();
		mSavePointBuffer.undo();
		fail(); ///
	}

	@After
	public void setUpAfter() throws Exception {
	}

	@Before
	public void setUpBefore() throws Exception {
		UndoRedoActionSequenceASSISTANT.resetGlobalSequence();
		mSavePointBuffer = new SavePointBuffer("name", new Id("x"));
		mUndoRedoActions = new UndoRedoActionSequenceASSISTANT[5];

		for (int i = 0; i < 5; i++) {
			UndoRedoActionSequenceASSISTANT action = new UndoRedoActionSequenceASSISTANT("UndoRedoAction " + i);
			mUndoRedoActions[i] = action;
			mSavePointBuffer.add(action);
		}

		mSavePointBuffer.lock();
	}

}
