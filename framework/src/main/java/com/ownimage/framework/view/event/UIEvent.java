package com.ownimage.framework.view.event;

import com.ownimage.framework.control.control.IControl;
import lombok.NonNull;
import org.immutables.value.Value;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Value.Immutable
public abstract class UIEvent implements IUIEvent {

    public static ImmutableUIEvent createKeyEvent(
            @NonNull final UIEvent.EventType pEventType,
            final IControl pSource,
            @NonNull final String pKey,
            final boolean pCtrl,
            final boolean pAlt,
            final boolean pShift
    ) {
        if (!pEventType.isKeyEvent()) {
            throw new IllegalArgumentException("pEventType = " + pEventType + ", is not a valid Key event.");
        }

        return ImmutableUIEvent.builder()
                .eventType(pEventType)
                .source(pSource)
                .when(new Date())
                .key(pKey)
                .isCtrl(pCtrl)
                .isAlt(pAlt)
                .isShift(pShift)
                .build();
    }

    public static ImmutableUIEvent createMouseEvent(
            @NonNull final UIEvent.EventType pEventType,
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

        return ImmutableUIEvent.builder()
                .eventType(pEventType)
                .source(pSource)
                .when(new Date())
                .width(pWidth)
                .height(pHeight)
                .x(pX)
                .y(pY)
                .isCtrl(pCtrl)
                .isAlt(pAlt)
                .isShift(pShift)
                .build();
    }

    public static ImmutableUIEvent createMouseEvent(@NonNull final IUIEvent pEvent, final int pX, final int pY) {
        if (!pEvent.getEventType().isMouseEvent()) {
            throw new IllegalArgumentException("pEventType = " + pEvent.getEventType() + ", is not a valid Mouse event");
        }

        return ImmutableUIEvent.builder()
                .eventType(pEvent.getEventType())
                .source(pEvent.getSource())
                .when(pEvent.getWhen())
                .width(pEvent.getWidth())
                .height(pEvent.getHeight())
                .x(pX)
                .y(pY)
                .isCtrl(pEvent.isCtrl())
                .isAlt(pEvent.isAlt())
                .isShift(pEvent.isShift())
                .build();
    }

    public static ImmutableUIEvent createMouseScrollEvent(
            @NonNull final UIEvent.EventType pEventType,
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

        return ImmutableUIEvent.builder()
                .eventType(pEventType)
                .source(pSource)
                .when(new Date())
                .scroll(pScroll)
                .width(pWidth)
                .height(pHeight)
                .x(pX)
                .y(pY)
                .isCtrl(pCtrl)
                .isAlt(pAlt)
                .isShift(pShift)
                .build();
    }

    public static ImmutableUIEvent delta(final ImmutableUIEvent pDragEvent, final ImmutableUIEvent pDragStartEvent) {
        if (pDragEvent.getDeltaX().isPresent() || pDragEvent.getDeltaY().isPresent()) {
            throw new IllegalStateException("delta can only be called once during the lifetime of a UIEvent.");
        }
        return pDragEvent
                .withDeltaX(Optional.of(pDragEvent.getX() - pDragStartEvent.getX()))
                .withDeltaY(Optional.of(pDragEvent.getY() - pDragStartEvent.getY()));
    }

    public abstract UIEvent.EventType getEventType();

    public abstract Integer getHeight();

    @Value.Default
    public int getScroll() {
        return 0;
    }

    public abstract IControl getSource();

    public abstract Date getWhen();

    public abstract Integer getWidth();

    public abstract Integer getX();

    public abstract Integer getY();

    public abstract Optional<Integer> getDeltaX();

    public abstract Optional<Integer> getDeltaY() ;

    @Value.Default
    public String getKey() {
        return "";
    }

    public abstract boolean isAlt();

    public abstract boolean isCtrl();

    public abstract boolean isShift();

    @Value.Derived
    public boolean isNormal() {
        return !isAlt() && !isCtrl() && !isShift();
    }

    @Value.Derived
    public Optional<Double> getNormalizedDeltaX() {
        return getDeltaX().map(dx -> Optional.of((double) dx / getWidth())).orElse(Optional.empty());
    }

    @Value.Derived
    public Optional<Double> getNormalizedDeltaY() {
        return getDeltaY().map(dy -> Optional.of((double) dy / getHeight())).orElse(Optional.empty());
    }

    @Value.Derived
    public double getNormalizedX() {
        final double x = (double) getX() / getWidth();
        return x;
    }

    @Value.Derived
    public double getNormalizedY() {
        final double y = (double) getY() / getHeight();
        return y;
    }

    @Override
    public String toString() {
        final DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        final StringBuilder sb = new StringBuilder();
        sb.append("mUIEvent:{");
        sb.append("mSource:" + getSource());
        sb.append(",mEventType:" + getEventType());
        sb.append(",mWhen:" + df.format(getWhen()));
        sb.append(",mWidth:" + getWidth());
        sb.append(",mScroll:" + getScroll());
        sb.append(",mHeight:" + getHeight());
        sb.append(",mX:" + getX());
        sb.append(",mY:" + getY());
        sb.append(",mDeltaX:" + getDeltaX());
        sb.append(",mDeltaY:" + getDeltaY());
        sb.append(",mKey:" + getKey());
        sb.append(",mCtrl:" + isCtrl());
        sb.append(",mAlt:" + isAlt());
        sb.append(",mShift:" + isShift());
        sb.append("}");

        return sb.toString();
    }

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
}
