/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.event;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.util.Framework;
import lombok.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class UIEvent implements IUIEvent {

    public enum EventType {
        Click, DoubleClick, Drag, MouseDown, MouseUp, MouseMoved, Scroll, KeyPressed, KeyReleased, KeyTyped;

        public boolean isMouseEvent() {
            return this == EventType.Click
                    || this == EventType.DoubleClick
                    || this == EventType.Drag
                    || this == EventType.MouseDown
                    || this == EventType.MouseUp
                    || this == EventType.MouseMoved;
        }

        public boolean isKeyEvent() {
            return this == EventType.KeyPressed
                    || this == EventType.KeyReleased
                    || this == EventType.KeyTyped;
        }

        public boolean isScrollEvent() {
            return this == EventType.Scroll;
        }

    }


    private final static Logger mLogger = Framework.getLogger();

    private IControl mSource;
    private EventType mEventType;
    private Date mWhen;

    private Integer mWidth;
    private Integer mHeight;
    private Integer mX;
    private Integer mY;
    private Integer mDeltaX;
    private Integer mDeltaY;

    private int mScroll;

    private boolean mCtrl;
    private boolean mAlt;
    private boolean mShift;

    private String mKey;

    private UIEvent() {
    }

    public static UIEvent createKeyEvent(
            @NonNull final EventType pEventType,
            final IControl pSource,
            final String pKey,
            final boolean pCtrl,
            final boolean pAlt,
            final boolean pShift
    ) {
        if (!pEventType.isKeyEvent()) {
            throw new IllegalArgumentException("pEventType = " + pEventType + ", is not a valid Key event.");
        }

        final UIEvent uiEvent = new UIEvent();

        uiEvent.mEventType = pEventType;
        uiEvent.mSource = pSource;
        uiEvent.mWhen = new Date();
        uiEvent.mKey = pKey;
        uiEvent.mCtrl = pCtrl;
        uiEvent.mAlt = pAlt;
        uiEvent.mShift = pShift;

        return uiEvent;
    }

    public static UIEvent createMouseEvent(
            @NonNull final EventType pEventType,
            final IControl pSource,
            final int pWidth,
            final int pHeight,
            final int pX,
            final int pY,
            final boolean pCtrl,
            final boolean pAlt,
            final boolean pShift
    ) {
        if (!pEventType.isMouseEvent()) {
            throw new IllegalArgumentException("pEventType = " + pEventType + ", is not a valid Mouse event");
        }

        final UIEvent uiEvent = new UIEvent();

        uiEvent.mEventType = pEventType;
        uiEvent.mSource = pSource;
        uiEvent.mWhen = new Date();
        uiEvent.mWidth = pWidth;
        uiEvent.mHeight = pHeight;
        uiEvent.mX = pX;
        uiEvent.mY = pY;
        uiEvent.mCtrl = pCtrl;
        uiEvent.mAlt = pAlt;
        uiEvent.mShift = pShift;

        return uiEvent;
    }

    public static UIEvent createMouseEvent(@NonNull final IUIEvent pEvent, final int pX, final int pY) {
        if (!pEvent.getEventType().isMouseEvent()) {
            throw new IllegalArgumentException("pEventType = " + pEvent.getEventType() + ", is not a valid Mouse event");
        }

        final UIEvent uiEvent = new UIEvent();

        uiEvent.mEventType = pEvent.getEventType();
        uiEvent.mSource = pEvent.getSource();
        uiEvent.mWhen = pEvent.getWhen();
        uiEvent.mWidth = pEvent.getWidth();
        uiEvent.mHeight = pEvent.getHeight();
        uiEvent.mX = pX;
        uiEvent.mY = pY;
        uiEvent.mCtrl = pEvent.isCtrl();
        uiEvent.mAlt = pEvent.isAlt();
        uiEvent.mShift = pEvent.isShift();
        uiEvent.mDeltaX = 0;
        uiEvent.mDeltaY = 0;

        return uiEvent;
    }

    public static UIEvent createMouseScrollEvent(
            @NonNull final EventType pEventType,
            final IControl pSource,
            final int pScroll,
            final int pWidth,
            final int pHeight,
            final int pX,
            final int pY,
            final boolean pCtrl,
            final boolean pAlt,
            final boolean pShift
    ) {
        if (!pEventType.isScrollEvent()) {
            throw new IllegalArgumentException("pEventType = " + pEventType + ", is not a valid scroll event.");
        }

        final UIEvent uiEvent = new UIEvent();

        uiEvent.mEventType = pEventType;
        uiEvent.mSource = pSource;
        uiEvent.mScroll = pScroll;
        uiEvent.mWhen = new Date();
        uiEvent.mWidth = pWidth;
        uiEvent.mHeight = pHeight;
        uiEvent.mX = pX;
        uiEvent.mY = pY;
        uiEvent.mCtrl = pCtrl;
        uiEvent.mAlt = pAlt;
        uiEvent.mShift = pShift;

        return uiEvent;
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
        final double dx = (double) mDeltaX / mWidth;
        return dx;
    }

    @Override
    public double getNormalizedDeltaY() {
        final double dy = (double) mDeltaY / mHeight;
        return dy;
    }

    @Override
    public double getNormalizedX() {
        final double x = (double) mX / mWidth;
        return x;
    }

    @Override
    public double getNormalizedY() {
        final double y = (double) mY / mHeight;
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
    public String getKey() {
        return mKey;
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
        if (mDeltaX != null) {
            throw new IllegalStateException("setDelta can only be called once during the lifetime of a UIEvent.");
        }
        mDeltaX = mX - pDragStartEvent.getX();
        mDeltaY = mY - pDragStartEvent.getY();
    }

    @Override
    public String toString() {
        final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        final StringBuffer sb = new StringBuffer();
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
        sb.append(",mKey:" + mKey);
        sb.append(",mCtrl:" + mCtrl);
        sb.append(",mAlt:" + mAlt);
        sb.append(",mShift:" + mShift);
        sb.append("}");

        return sb.toString();
    }
}
