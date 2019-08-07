/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.NullMetaType;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.RectangleSize;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IGrafittiImp;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import lombok.NonNull;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

public class PictureControl
        extends ControlBase<PictureControl, PictureType, NullMetaType<PictureType>, PictureType, IPictureView>
        implements IRawUIEventListener {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final Integer mKey;
    private IGrafitti mGrafitti;

    /**
     * This is set to false when the first MouseDown UIEvent is received. When a MouseDrag UIEvent is received if this is false a
     * mouseDragStart is called and it is set to true before the mouseDrag is called. Hence mouseDragStart will only be called once.
     * When a MouseReleased event is received if this is true then mouseDragStop is called. When a MouseClicked or
     * MouseDoubleClicked event is received then if this is true the corresponding mouseClick or mouseDoubleClick is suppressed.
     */
    private boolean mIsDragging;
    private IUIEventListener mUIEventListener;
    private IUIEvent mDragStartEvent;

    public PictureControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final PictureType pPicture) {
        super(pDisplayName, pPropertyName, pContainer, pPicture);
        mKey = getValue().lock();
    }

    @Override
    public IView createView() {
        final IPictureView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    public void drawGrafitti(final IGrafittiImp pGrafittiImp) {
        if (mGrafitti != null) {
            final GrafittiHelper grafittiHelper = new GrafittiHelper(pGrafittiImp);
            mGrafitti.graffiti(grafittiHelper);
        }
    }

    public int getHeight() {
        return getValue().getHeight();
    }

    public int getWidth() {
        return getValue().getWidth();
    }

    public void drawGrafitti() {
        mViews.invokeAll(view -> view.drawGrafitti(this::drawGrafitti));
    }

    public void updateGrafitti(@NonNull final IGrafitti pGrafitti) {
        mViews.invokeAll(view -> view.updateGraffiti(grafittiImp -> pGrafitti.graffiti(new GrafittiHelper(grafittiImp))));
    }

    public void drawCursor(@NonNull final IGrafitti pGrafitti) {
        mViews.invokeAll(view -> view.drawCursor(grafittiImp -> pGrafitti.graffiti(new GrafittiHelper(grafittiImp))));
    }

    public void setGrafitti(final IGrafitti pGrafitti) {
        Framework.checkStateNoChangeOnceSet(mLogger, mGrafitti, "mGrafitti");
        mGrafitti = pGrafitti;
    }

    public void setUIListener(@NonNull final IUIEventListener pUIEventListener) {
        Framework.logEntry(mLogger);
        if (mUIEventListener != null) {
            throw new IllegalStateException("setUIListener can only be called once during the lifetime of a control.");
        }

        mUIEventListener = pUIEventListener;

        Framework.logExit(mLogger);
    }

    @Override
    public boolean setValue(final PictureType pValue, final IView pView, final boolean pIsMutating) {
        // does not do a validation as pictures can not be validated
        // does not do a redo entry as picture changes are a consequence

        setDirty(true);
        mValue = pValue;
        fireControlChangeEvent(pView, pIsMutating);
        return true;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public void uiEvent(@NonNull final IUIEvent pEvent) {
        Framework.logEntry(mLogger);

        mLogger.fine(() -> String.format("uiEvent type=%s, isDragging=%s", pEvent.getEventType(), mIsDragging));
        final LocalTime start = LocalTime.now();

        if (mUIEventListener != null) {
            switch (pEvent.getEventType()) {
                case Click:
                    if (!mIsDragging) {
                        mUIEventListener.mouseClickEvent(pEvent);
                    }
                    break;

                case DoubleClick:
                    if (!mIsDragging) {
                        mUIEventListener.mouseDoubleClickEvent(pEvent);
                    }
                    break;

                case Drag:
                    if (!mIsDragging) {
                        mIsDragging = true;
                        mDragStartEvent = pEvent;
                        mUIEventListener.mouseDragStartEvent(pEvent);
                    }
                    pEvent.setDelta(mDragStartEvent);
                    mUIEventListener.mouseDragEvent(pEvent);
                    break;

                case MouseDown:
                    mIsDragging = false;
                    break;

                case MouseUp:
                    if (mIsDragging) {
                        pEvent.setDelta(mDragStartEvent);
                        mUIEventListener.mouseDragEndEvent(pEvent);
                        mIsDragging = false;
                    }
                    break;
                case MouseMoved:
                    if (!mIsDragging) {
                        mUIEventListener.mouseMoveEvent(pEvent);
                    }
                    break;
                case Scroll:
                    mUIEventListener.scrollEvent(pEvent);
                    break;
            }


            final LocalTime end = LocalTime.now();
            final long elapsed = ChronoUnit.MICROS.between(start, end);
            mLogger.fine(String.format("%s took %s ms", pEvent.getEventType(), elapsed));
        }

        Framework.logExit(mLogger);
    }

    public RectangleSize getSize() {
        return mValue.getSize();
    }

    @Override
    public String toString() {
        return String.format("[%s %s]", getDisplayName(), System.identityHashCode(this));
    }

}
