/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.NullMetaType;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IGrafittiImp;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.factory.ViewFactory;

public class PictureControl extends ControlBase<PictureControl, PictureType, NullMetaType<PictureType>, PictureType, IPictureView> {

    public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
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
        IPictureView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    public void drawGrafitti(final IGrafittiImp pGrafittiImp) {
        if (mGrafitti != null) {
            GrafittiHelper grafittiHelper = new GrafittiHelper(pGrafittiImp);
            mGrafitti.grafitti(grafittiHelper);
        }

    }

    public int getHeight() {
        return getValue().getHeight();
    }

    public int getWidth() {
        return getValue().getWidth();
    }

    public void redrawGrafitti() {
        mViews.invokeAll(view -> view.redrawGrafitti());
        System.out.println("redraw grafitti");
    }

    public void setGrafitti(final IGrafitti pGrafitti) {
        if (mGrafitti != null) {
            throw new IllegalStateException("Cannot setSetGrafitti once it has been set");
        }

        mGrafitti = pGrafitti;
    }

    public void setUIListener(final IUIEventListener pUIEventListener) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pUIEventListener, "pUIEventListener");
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

    public void uiEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pEvent, "pEvent");

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
                    }
                    break;
                case Scroll:
                    mUIEventListener.scrollEvent(pEvent);
                    break;
            }

        }

        Framework.logExit(mLogger);
    }

}
