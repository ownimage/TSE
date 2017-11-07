package com.ownimage.framework.view.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class UIEvent implements IUIEvent {

	public enum EventType {
		Click, DoubleClick, Drag, MouseDown, MouseUp, Scroll
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	private final static Logger mLogger = Framework.getLogger();;

	private final IControl mSource;
	private final EventType mEventType;
	private final Date mWhen;

	private final Integer mWidth;
	private final Integer mHeight;
	private final Integer mX;
	private final Integer mY;
	private Integer mDeltaX;
	private Integer mDeltaY;

	private int mScroll;

	private final boolean mCtrl;
	private final boolean mAlt;
	private final boolean mShift;

	public UIEvent(final EventType pEventType, final IControl pSource, final int pWidth, final int pHeight, final int pX, final int pY, final boolean pCtrl, final boolean pAlt,
			final boolean pShift) {
		Framework.checkNotNull(mLogger, pEventType, "pEventType");
		if (pEventType != EventType.Click && pEventType != EventType.DoubleClick && pEventType != EventType.Drag && pEventType != EventType.MouseDown
				&& pEventType != EventType.MouseUp) { throw new IllegalArgumentException("pEventType = " + pEventType + ", it needs to be one of Click, DoubleClick, Drag, MouseDown, MouseUp."); }

		mEventType = pEventType;
		mSource = pSource;
		mWhen = new Date();
		mWidth = pWidth;
		mHeight = pHeight;
		mX = pX;
		mY = pY;
		mCtrl = pCtrl;
		mAlt = pAlt;
		mShift = pShift;
	}

	public UIEvent(final EventType pEventType, final IControl pSource, final int pScroll, final int pWidth, final int pHeight, final int pX, final int pY, final boolean pCtrl, final boolean pAlt,
			final boolean pShift) {
		Framework.checkNotNull(mLogger, pEventType, "pEventType");
		if (pEventType != EventType.Scroll) { throw new IllegalArgumentException("pEventType = " + pEventType + ", it needs to be Scroll."); }

		mEventType = pEventType;
		mSource = pSource;
		mScroll = pScroll;
		mWhen = new Date();
		mWidth = pWidth;
		mHeight = pHeight;
		mX = pX;
		mY = pY;
		mCtrl = pCtrl;
		mAlt = pAlt;
		mShift = pShift;
	}

	@Override
	public Integer getDeltaX() {
		return mDeltaX;
	}

	@Override
	public Integer getDeltaY() {
		return mDeltaY;
	}

	@Override
	public EventType getEventType() {
		return mEventType;
	}

	@Override
	public Integer getHeight() {
		return mHeight;
	}

	@Override
	public double getNormalizedDeltaX() {
		double dx = (double) mDeltaX / mWidth;
		return dx;
	}

	@Override
	public double getNormalizedDeltaY() {
		double dy = (double) mDeltaY / mHeight;
		return dy;
	}

	@Override
	public double getNormalizedX() {
		double x = (double) mX / mWidth;
		return x;
	}

	@Override
	public double getNormalizedY() {
		double y = (double) mY / mHeight;
		return y;
	}

	@Override
	public int getScroll() {
		return mScroll;
	}

	@Override
	public IControl getSource() {
		return mSource;
	}

	@Override
	public Date getWhen() {
		return mWhen;
	}

	@Override
	public Integer getWidth() {
		return mWidth;
	}

	@Override
	public Integer getX() {
		return mX;
	}

	@Override
	public Integer getY() {
		return mY;
	}

	@Override
	public boolean isAlt() {
		return mAlt;
	}

	@Override
	public boolean isCtrl() {
		return mCtrl;
	}

	@Override
	public boolean isNormal() {
		return !isAlt() && !isCtrl() && !isShift();
	}

	@Override
	public boolean isShift() {
		return mShift;
	}

	@Override
	public void setDelta(final IUIEvent pDragStartEvent) {
		if (mDeltaX != null) { throw new IllegalStateException("setDelta can only be called once during the lifetime of a UIEvent."); }
		mDeltaX = mX - pDragStartEvent.getX();
		mDeltaY = mY - pDragStartEvent.getY();
	}

	@Override
	public String toString() {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		StringBuffer sb = new StringBuffer();
		sb.append("mUIEvent:{");
		sb.append("mSource:" + mSource);
		sb.append(",mEventType:" + mEventType);
		sb.append(",mWhen:" + df.format(mWhen));
		sb.append(",mWidth:" + mWidth);
		sb.append(",mScroll:" + mScroll);
		sb.append(",mHeight:" + mHeight);
		sb.append(",mX:" + mX);
		sb.append(",mY:" + mY);
		sb.append(",mDeltaX:" + mDeltaX);
		sb.append(",mDeltaY:" + mDeltaY);
		sb.append(",mCtrl:" + mCtrl);
		sb.append(",mAlt:" + mAlt);
		sb.append(",mShift:" + mShift);
		sb.append("}");

		return sb.toString();
	}
}
