package com.ownimage.perception.pixelMap;

import java.util.TreeMap;
import java.util.logging.Logger;

import com.ownimage.framework.util.Version;
import com.ownimage.perception.math.IntegerPoint;

public class PixelUndoRedoAction implements com.ownimage.framework.undo.IUndoRedoAction {
	private static class Value {
		private final byte mBefore;
		private byte mAfter;

		public Value(final byte pBefore, final byte pAfter) {
			mBefore = pBefore;
			mAfter = pAfter;
		}

		private byte getAfter() {
			return mAfter;
		}

		private byte getBefore() {
			return mBefore;
		}

		public void setAfter(final byte pAfter) {
			mAfter = pAfter;
		}

		@Override
		public String toString() {
			return "Value [mBefore=" + mBefore + ", mAfter=" + mAfter + "]";
		}

	}

	/** The Constant mVersion. */
	public final static Version mVersion = new Version(5, 0, 0, "2015/05/10 10:00");

	/** The Constant mLogger. */
	private final static Logger mLogger = Logger.getLogger(PixelUndoRedoAction.class.getName());

	private final PixelMap mPixelMap;

	private final TreeMap<IntegerPoint, Value> mAllPixelChanges = new TreeMap<IntegerPoint, Value>();

	public PixelUndoRedoAction(final PixelMap pPixelMap) {
		if (pPixelMap == null) { throw new IllegalArgumentException("pPixelMap must not be null."); }

		mPixelMap = pPixelMap;
	}

	public void addPixelBeforeAfter(final int pX, final int pY, final byte pBefore, final byte pAfter) {
		final IntegerPoint key = new IntegerPoint(pX, pY);
		Value value = mAllPixelChanges.get(key);
		if (value == null) {
			value = new Value(pBefore, pAfter);
			mAllPixelChanges.put(key, value);
		} else {
			value.setAfter(pAfter);
		}
	}

	@Override
	public String getDescription() {
		return "PixelUndoRedo";
	}

	@Override
	public void redo() {
		for (final IntegerPoint key : mAllPixelChanges.keySet()) {
			final Value value = mAllPixelChanges.get(key);
			mPixelMap.setValueNoUndo(key.getX(), key.getY(), value.getAfter());
		}
	}

	@Override
	public void undo() {
		for (final IntegerPoint key : mAllPixelChanges.keySet()) {
			final Value value = mAllPixelChanges.get(key);
			mPixelMap.setValueNoUndo(key.getX(), key.getY(), value.getBefore());
		}
	}

}
