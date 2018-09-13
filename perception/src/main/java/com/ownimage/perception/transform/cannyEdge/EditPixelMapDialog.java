/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.awt.*;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IGrafitti;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.ContainerList;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Rectangle;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.event.UIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.CropTransform;
import com.ownimage.perception.transform.ITransform;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator, IGrafitti {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private enum PixelAction {
        On("On"),
        Off("Off"),
        OffWide("Off Wide"),
        OffVeryWide("Off Very Wide"),
        OffVeryVeryWide("Off Very Very Wide"),
        Toggle("Toggle"),
        DeletePixelChain("Delete Pixel Chain");

        private final String mName;

        PixelAction(String pName) {
            mName = pName;
        }

        public String toString() {
            return mName;
        }

    }

    private final PixelMap mPixelMap;
    private final ActionControl mOkAction;
    private final ActionControl mCancelAction;
    private IDialogView mEditPixelMapDialogView;

    private boolean mMutating = false;

    private final ITransform mTransform;
    private final CropTransform mCropTransform;

    private PictureControl mPictureControl = new PictureControl("Preview", "preview", NullContainer,
                                                                new PictureType(100, 100));

    private final IContainer mGeneralContainer = newContainer("General", "general", true).addTitle().addBottomPadding();
    private final BooleanControl mShowCurves = new BooleanControl("Show Curves", "showCurves", mGeneralContainer, false);
    private final BooleanControl mAutoUpdateCurves = new BooleanControl("Auto Update Curves", "autoUpdateCurves", mGeneralContainer, false);
    private final ActionControl mUpateCurves = new ActionControl("Update Curves", "updateCurves", mGeneralContainer, this::updatePreview);

    private final ContainerList mContainerList = new ContainerList("Edit PixelMap", "editPixelMap");
    private final IContainer mMoveContainer = mContainerList.add(newContainer("Move", "move", true));
    private final IContainer mPixelControlContainer = mContainerList.add(newContainer("Pixel", "pixel", true));
    private final IContainer mVertexControlContainer = mContainerList.add(newContainer("Vertex", "vertex", true));

    // General Container
    private final IntegerControl mPixelMapWidth;
    private final IntegerControl mPixelMapHeight;
    private final IntegerControl mPreviewSize;
    private final IntegerControl mZoom;
    private final IntegerControl mViewOriginX;
    private final IntegerControl mViewOriginY;

    // Pixel Container
    private final ColorControl mEdgeColor = new ColorControl("Edge Color", "edgeColor", mPixelControlContainer, getProperties().getCETEPMDEdgeColor());
    private final ColorControl mNodeColor = new ColorControl("Node Color", "nodeColor", mPixelControlContainer, getProperties().getCETEPMDNodeColor());
    private final BooleanControl mShowGrafitti = new BooleanControl("Show Grafitti", "showGrafitti", mPixelControlContainer, true);
    private final ObjectControl<PixelAction> mPixelAction =
            new ObjectControl("Pixel Action", "pixelAction", mPixelControlContainer, PixelAction.On, PixelAction.values());

    private final IControlChangeListener mGrafittiChangeRequired = (c, m) -> mPictureControl.drawGrafitti();

    // Vertex Container
    private final DoubleControl mVCC1 = new DoubleControl("Vertex test 1", "vertexTest1", mVertexControlContainer, 0.5);
    private final IntegerControl mVCC2 = new IntegerControl("Vertex test 2", "vertexTest2", mVertexControlContainer, 2, 2, 15, 1);
    private final IntegerControl mVCC3 = new IntegerControl("Vertex test 3", "vertexTest3", mVertexControlContainer, 2, 2, 15, 1);

    private boolean mDialogIsAlive = false;

    private int mMouseDragStartX;
    private int mMouseDragStartY;
    private Pixel mMouseLastPixelPosition = null;
    private Pixel mMouseDragLastPixel = null;
    private int mMouseLastSize;
    private Id mSavepointId;

    public EditPixelMapDialog(final ITransform pTransform, final PixelMap pPixelMap, final String pDisplayName, final String pPropertyName, final ActionControl pOkAction, final ActionControl pCancelAction) {
        super(pDisplayName, pPropertyName, Services.getServices().getUndoRedoBuffer());
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");
        Framework.checkParameterNotNull(mLogger, pPixelMap, "pPixelMap");

        mTransform = pTransform;
        mPixelMap = pPixelMap;
        mOkAction = pOkAction;
        mCancelAction = pCancelAction;

        Properties properties = Services.getServices().getProperties();

        mPixelMapWidth = new IntegerControl("PixelMap Height", "pixelMapWidth", mGeneralContainer, getWidth(), 0, getWidth(), 50).setEnabled(false);
        mPixelMapHeight = new IntegerControl("PixelMap Height", "pixelMapHeight", mGeneralContainer, getHeight(), 0, getHeight(), 50).setEnabled(false);
        mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, properties.getCETEPMDPreviewSize(), properties.CETEPMDPreviewSizeModel);
        mZoom = new IntegerControl("Zoom", "zoom", mGeneralContainer, properties.getCETEPMDZoom(), properties.CETEPMDZoomModel);
        mViewOriginX = new IntegerControl("View X", "x", mMoveContainer, 0, 0, getWidth(), 50);
        mViewOriginY = new IntegerControl("View Y", "y", mMoveContainer, 0, 0, getHeight(), 50);
        mCropTransform = new CropTransform(Services.getServices().getPerception(), true);
        mPictureControl.setGrafitti(this);
        mPictureControl.setUIListener(this);
        updatePreview();

        mEdgeColor.addControlChangeListener(mGrafittiChangeRequired);
        mNodeColor.addControlChangeListener(mGrafittiChangeRequired);
        mShowGrafitti.addControlChangeListener(mGrafittiChangeRequired);
    }

    private void updatePreview() {
        setCrop();
        if (mTransform.isInitialized()) {
            if (getPreviewSize() != mPictureControl.getWidth()) {
                final PictureType pictureType = new PictureType(getPreviewSize(), getPreviewSize());
                mPictureControl.setValue(pictureType);
            }
            Services.getServices().getRenderService()
                    .getRenderJobBuilder("EditPixelMapDialog::updatePreview", mPictureControl, mCropTransform)
                    .withAllowTerminate(false)
                    .build()
                    .run();
        }
    }

    @Override
    public boolean validateControl(final Object pControl) {
        if (pControl == mViewOriginX) {
            boolean valid = getWidth() > getViewOriginX() + getWidth() / getZoom();
            return valid;
        }
        if (pControl == mViewOriginY) {
            boolean valid = getHeight() > getViewOriginY() + getHeight() / getZoom();
            return valid;
        }
        if (pControl == mZoom) {
            if (getViewOriginX() + getWidth() / getZoom() > getWidth()) {
                mViewOriginX.setValue(getWidth() - Math.floorDiv(getWidth(), getZoom()));
            }
            if (getViewOriginY() + getHeight() / getZoom() > getHeight()) {
                mViewOriginY.setValue(getHeight() - Math.floorDiv(getHeight(), getZoom()));
            }
            return true;
        }
        return true;
    }

    private int getWidth() {
        return mPixelMap.getWidth();
    }

    private int getHeight() {
        return mPixelMap.getHeight();
    }

    private int getPreviewSize() {
        return mPreviewSize.getValue();
    }

    private int getZoom() {
        return mZoom.getValue();
    }

    private int getViewOriginX() {
        return mViewOriginX.getValue();
    }

    private int getViewOriginY() {
        return mViewOriginY.getValue();
    }

    private boolean isMutating() {
        return mMutating;
    }

    private void setMutating(boolean pMutating) {
        mMutating = pMutating;
    }

    @Override
    public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
        mLogger.fine(() -> "controlChangeEvent for " + pControl.getDisplayName());
        if (isMutating()) return;

        if (pControl != null) {
            if (pControl.isOneOf(mViewOriginX, mViewOriginY, mZoom, mPreviewSize, mShowCurves)) {
                updatePreview();
            }

            if (pControl.isOneOf(mShowCurves, mAutoUpdateCurves)) {
                updateControlVisibility();
            }
        }
    }

    private void updateControlVisibility() {
        mAutoUpdateCurves.setVisible(mShowCurves.getValue());
        mUpateCurves.setVisible(mShowCurves.getValue() && !mAutoUpdateCurves.getValue());
    }

    @Override
    public IView createView() {
        VFlowLayout vflow = new VFlowLayout(mGeneralContainer, mContainerList);
        HFlowLayout hflow = new HFlowLayout(mPictureControl, vflow);
        IView view = ViewFactory.getInstance().createView(hflow);
        addView(view);
        return view;
    }

    private IDialogView getDialogView() {
        if (mEditPixelMapDialogView == null) {
            mEditPixelMapDialogView = ViewFactory.getInstance().createDialog(this,
                                                                             DialogOptions.builder().withCompleteFunction(this::dialogClose).build(),
                                                                             getUndoRedoBuffer(),
                                                                             mCancelAction, mOkAction);
        }
        return mEditPixelMapDialogView;
    }

    public void showDialog() {
        mDialogIsAlive = true;
        updatePreview();
        getDialogView().showModal();
    }

    private void dialogClose() {
        mDialogIsAlive = false;
    }

    public UndoRedoBuffer getUndoRedoBuffer() {
        return Services.getServices().getUndoRedoBuffer();
    }

    @Override
    synchronized public void grafitti(final GrafittiHelper pGrafittiHelper) {
        Framework.checkStateNotNull(mLogger, mPixelMap, "mPixelMap");

        int xSize = Math.floorDiv(getWidth(), getZoom()) + 1;
        int ySize = Math.floorDiv(getHeight(), getZoom()) + 1;

        grafitti(pGrafittiHelper, getViewOriginX(), getViewOriginY(), getViewOriginX() + xSize, getViewOriginY() + ySize);

        if (mShowGrafitti.getValue()) {
            mPixelMap.forEachPixelChain(pc -> grafittiPixelChain(pGrafittiHelper, pc));
        }
    }

    private void grafitti(final GrafittiHelper pGrafittiHelper, int xMin, int yMin, int xMax, int yMax) {
        Range2D range = new Range2D(xMin, xMax, yMin, yMax);
        range.forEach((x, y) -> {
            Pixel pixel = mPixelMap.getPixelAt(x, y);
            if (pixel.isNode() || pixel.isEdge()) {
                grafittiPixel(pGrafittiHelper, pixel);
            }
        });
    }

    private void grafittiPixel(GrafittiHelper pGrafittiHelper, final Pixel pPixel) {
        Rectangle r = pixelToGrafittiRectangle(pPixel);
        Color c = getPixelColor(pPixel);
        pGrafittiHelper.clearRectangle(r);
        pGrafittiHelper.drawFilledRectangle(r, c);
    }

    private Color getPixelColor(Pixel pPixel) {
        return pPixel.isNode() ? mNodeColor.getValue() : mEdgeColor.getValue();
    }

    private Rectangle pixelToGrafittiRectangle(Pixel pPixel) {
        int x = pPixel.getX();
        int y = pPixel.getY();
        double x1 = pixelXToGrafittiX(x, getViewOriginX(), getWidth(), getZoom());
        double x2 = pixelXToGrafittiX(x + 1, getViewOriginX(), getWidth(), getZoom());
        double y1 = pixelYToGrafittiY(y, getViewOriginY(), getHeight(), getZoom());
        double y2 = pixelYToGrafittiY(y + 1, getViewOriginY(), getHeight(), getZoom());
        return new Rectangle(x1, y1, x2, y2);
    }

    private double pixelXToGrafittiX(int pX, int pXMin, int pPMWidth, int pZoom) {
        return (double) (pX - pXMin) * pZoom / pPMWidth;
    }

    private double pixelYToGrafittiY(int pY, int pYMin, int pPMHeight, int pZoom) {
        return (double) (pY - pYMin) * pZoom / pPMHeight;
    }

    private void grafittiPixelChain(GrafittiHelper pGrafittiHelper, PixelChain pPixelChain) {
        pPixelChain.getAllSegments().forEach(s -> grafittiSegment(pGrafittiHelper, s));
    }

    private void grafittiSegment(GrafittiHelper pGrafittiHelper, ISegment pSegment) {
        SegmentGrafittiHelper segmentGrafittiHelper = new SegmentGrafittiHelper(pGrafittiHelper, this::UHVWtoView);
        mPixelMap.getPixelChainForSegment(pSegment).ifPresent(pc -> pSegment.graffiti(pc, segmentGrafittiHelper));
    }

    private Point UHVWtoView(Point pUHVW) {
        double x = (getWidth() * pUHVW.getX() - getViewOriginX()) * getZoom() / getWidth();
        double y = (getHeight() * pUHVW.getY() - getViewOriginY()) * getZoom() / getHeight();
        return new Point(x, y);
    }

    private Optional<Pixel> eventToPixel(IUIEvent pEvent) {
        return eventXYToPixel(pEvent.getX(), pEvent.getY());
    }

    private Optional<Pixel> eventXYToPixel(int pX, int pY) {
        int x = getViewOriginX() + (pX * getWidth() / (getZoom() * getPreviewSize()));
        int y = getViewOriginY() + (pY * getHeight() / (getZoom() * getPreviewSize()));
        return mPixelMap.getOptionalPixelAt(x, y);
    }

    private void setCrop() {
        if (mPixelMap != null) {
            double left = (double) getViewOriginX() / getWidth();
            double right = left + 1.0d / getZoom();
            double bottom = (double) getViewOriginY() / getHeight();
            double top = bottom + 1.0d / getZoom();
            mCropTransform.setCrop(left, bottom, right, top);
            if (mShowCurves.getValue())
                mCropTransform.setPreviousTransform(mTransform);
            else mCropTransform.setPreviousTransform(mTransform.getPreviousTransform());
        }
    }

    @Override
    public void mouseClickEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isPixelView()) {
            pixel.ifPresent(p -> mouseClickEventPixelView(pEvent, p));
        }
    }

    private void autoUpdatePreview() {
        if (mAutoUpdateCurves.getValue()) updatePreview();
    }

    private void mouseClickEventPixelView(final IUIEvent pEvent, final Pixel pPixel) {
        if (pPixel != null) {
            if (isPixelActionOn()) actionPixelOn(pPixel);
            if (isPixelActionOff()) actionPixelOff(pPixel);
            if (isPixelActionToggle()) actionPixelToggle(pPixel);
            if (isPixelActionDeletePixelChain()) actionPixelChainDelete(pPixel);
            autoUpdatePreview();
        }
        grafittiCursor(pEvent, pPixel);
    }

    private boolean isPixelActionOn() {
        return mPixelAction.getValue() == PixelAction.On;
    }

    private boolean isPixelActionOff() {
        final EnumSet<PixelAction> set = EnumSet.of(PixelAction.Off, PixelAction.OffWide, PixelAction.OffVeryWide, PixelAction.OffVeryVeryWide);
        return set.contains(mPixelAction.getValue());
    }

    private boolean isPixelActionToggle() {
        return mPixelAction.getValue() == PixelAction.Toggle;
    }

    private boolean isPixelActionDeletePixelChain() {
        return mPixelAction.getValue() == PixelAction.DeletePixelChain;
    }

    synchronized private void actionPixelOn(final Pixel pPixel) {
        pPixel.setEdge(true);
        mPictureControl.drawGrafitti();
    }

    synchronized private void actionPixelToggle(final Pixel pPixel) {
        pPixel.setEdge(!pPixel.isEdge());
        mPictureControl.drawGrafitti();
    }

    synchronized private void actionPixelChainDelete(final Pixel pPixel) {
        mPixelMap.getPixelChains(pPixel).forEach(pc -> pc.delete(mPixelMap));
        mPictureControl.drawGrafitti();
    }

    @Override
    public void mouseDoubleClickEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
    }

    @Override
    public void mouseDragEndEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
        if (isPixelView()) {
            mouseDragEndEventPixelView(pEvent, null);
        }
        Framework.logExit(mLogger);
    }

    private void mouseDragEndEventPixelView(final IUIEvent pEvent, Pixel pPixel) {
        getUndoRedoBuffer().endSavepoint(mSavepointId);
        mSavepointId = null;
        autoUpdatePreview();
        updateGrafitti();
        mMouseDragLastPixel = null;
    }


    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
        Framework.logEntry(Framework.mLogger);
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isMoveView()) {
            pixel.ifPresent(p -> mouseDragEventMoveView(pEvent, p));
        }
        if (isPixelView()) {
            pixel.ifPresent(p -> mouseDragEventPixelViewFillIn(pEvent, p));
        }
    }

    /**
     * Fills in the gaps in the drag event so that all the pixels are connected.
     **/
    private void mouseDragEventPixelViewFillIn(IUIEvent pEvent, Pixel pPixel) {
        mLogger.fine(() -> String.format("mouseDragEventPixelViewFillIn %s, %s", pPixel, mMouseDragLastPixel));
        if (pPixel != null && (isPixelActionOn() || isPixelActionOff())) {
            if (mMouseDragLastPixel != null && !mMouseDragLastPixel.equals(pPixel)) {
                mLogger.fine("mouseDragEventPixelViewFillIn ...");
                int dX = pPixel.getX() - mMouseDragLastPixel.getX();
                int dY = pPixel.getY() - mMouseDragLastPixel.getY();
                if (Math.abs(dX) >= Math.abs(dY)) { // fill in missing x
                    int from = Math.min(mMouseDragLastPixel.getX(), pPixel.getX());
                    int to = Math.max(mMouseDragLastPixel.getX(), pPixel.getX());
                    IntStream.range(from, to).forEach(x -> {
                        int y = (int) Math.round(mMouseDragLastPixel.getY() + (((double) x - mMouseDragLastPixel.getX()) / dX) * dY);
                        final Optional<Pixel> pixel = mPixelMap.getOptionalPixelAt(x, y);
                        mLogger.fine(() -> String.format("mouseDragEventPixelViewFillIn X  %s, %s", x, y));
                        pixel.ifPresent(p -> mouseDragEventPixelView(UIEvent.createMouseEvent(pEvent, x, y), p));
                    });
                } else { // fill in missing y
                    int from = Math.min(mMouseDragLastPixel.getY(), pPixel.getY());
                    int to = Math.max(mMouseDragLastPixel.getY(), pPixel.getY());
                    IntStream.range(from, to).forEach(y -> {
                        int x = (int) Math.round(mMouseDragLastPixel.getX() + (((double) y - mMouseDragLastPixel.getY()) / dY) * dX);
                        final Optional<Pixel> pixel = mPixelMap.getOptionalPixelAt(x, y);
                        mLogger.fine(() -> String.format("mouseDragEventPixelViewFillIn Y  %s, %s", x, y));
                        pixel.ifPresent(p -> mouseDragEventPixelView(UIEvent.createMouseEvent(pEvent, x, y), p));
                    });
                }
            }
        }
        mouseDragEventPixelView(pEvent, pPixel);
        mMouseDragLastPixel = pPixel;
    }

    private void mouseDragEventMoveView(final IUIEvent pEvent, Pixel pPixel) {
        setMutating(true);
        int x = mViewOriginX.getValue();
        int y = mViewOriginY.getValue();
        mViewOriginX.setValue((int) (mMouseDragStartX - pEvent.getNormalizedDeltaX() * mTransform.getWidth() / getZoom()));
        mViewOriginY.setValue((int) (mMouseDragStartY - pEvent.getNormalizedDeltaY() * mTransform.getHeight() / getZoom()));
        if (x != mViewOriginX.getValue() || y != mViewOriginY.getValue()) updatePreview();
        setMutating(false);
    }

    private void mouseDragEventPixelView(final IUIEvent pEvent, Pixel pPixel) {
        if (pPixel != null) {
            if (isPixelActionOn()) actionPixelOn(pPixel);
            if (isPixelActionOff()) actionPixelOff(pPixel);
            if (isPixelActionToggle()) mouseDragEventPixelViewToggle(pEvent, pPixel);
            if (isPixelActionDeletePixelChain()) actionDeletePixelChain(pPixel);
            grafittiCursor(pEvent, pPixel);
        }
    }

    synchronized private void actionPixelOff(final Pixel pPixel) {
        int size = getCursorSize();
        double radius = (double) size / mPixelMapHeight.getValue();

        new Range2D(pPixel.getX() - size, pPixel.getX() + size, pPixel.getY() - size, pPixel.getY() + size)
                .forEach((x, y) ->
                                 mPixelMap.getOptionalPixelAt(x, y)
                                         .filter(p -> pPixel.getUHVWPoint().distance(p.getUHVWPoint()) < radius)
                                         .filter(Pixel::isEdge)
                                         .ifPresent(p -> p.setEdge(false))
                );
        updateGrafitti();
    }

    private void grafittiCursor(IUIEvent pEvent, Pixel pPixel) {
        if (pPixel != null) {
            IGrafitti g = grafittiHelper ->
                    grafittiHelper.drawCircle(pEvent.getNormalizedX(), pEvent.getNormalizedY(), getRadius(), Color.red, false);
            mPictureControl.drawCursor(g);
            mMouseLastPixelPosition = pPixel;
            mMouseLastSize = getCursorSize();
        }
    }

    private double getRadius() {
        return (double) getCursorSize() * getZoom() / mPixelMapHeight.getValue();
    }

    private int getCursorSize() {
        if (mPixelAction.getValue() == PixelAction.OffWide) return 5;
        if (mPixelAction.getValue() == PixelAction.OffVeryWide) return 15;
        if (mPixelAction.getValue() == PixelAction.OffVeryVeryWide) return 45;
        return 1;
    }

    /**
     * this is to remove the cursor that has been drawn on the previous grafittiCursor call
     */
    private void updateGrafitti() {
        if (mMouseLastPixelPosition != null) {
            IGrafitti g = grafittiHelper -> {
                double x1 = pixelXToGrafittiX(mMouseLastPixelPosition.getX() - mMouseLastSize - 1, getViewOriginX(), getWidth(), getZoom());
                double y1 = pixelYToGrafittiY(mMouseLastPixelPosition.getY() - mMouseLastSize - 1, getViewOriginY(), getHeight(), getZoom());
                double x2 = pixelXToGrafittiX(mMouseLastPixelPosition.getX() + mMouseLastSize + 1, getViewOriginX(), getWidth(), getZoom());
                double y2 = pixelYToGrafittiY(mMouseLastPixelPosition.getY() + mMouseLastSize + 1, getViewOriginY(), getHeight(), getZoom());
                grafittiHelper.clearRectangle(x1, y1, x2, y2);
                grafitti(grafittiHelper);
            };
            mPictureControl.updateGrafitti(g);
        }
    }

    private void mouseDragEventPixelViewToggle(final IUIEvent pEvent, Pixel pPixel) {
        if (pPixel != null && !pPixel.equals(mMouseLastPixelPosition)) {
            actionPixelToggle(pPixel);
            mMouseLastPixelPosition = pPixel;
        }
    }

    private void actionDeletePixelChain(Pixel pPixel) {
        if (pPixel != null && pPixel.isEdge()) {
            mPixelMap.getPixelChains(pPixel).forEach(pc -> pc.delete(mPixelMap));
        }
        mPictureControl.drawGrafitti();
    }

    @Override
    public void mouseMoveEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
        Optional<Pixel> pixel = eventToPixel(pEvent);
        pixel.filter(p -> !p.equals(mMouseLastPixelPosition))
                .ifPresent(p -> {
                    mMouseLastPixelPosition = p;
                    grafittiCursor(pEvent, p);
                });
    }

    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        mLogger.fine(() -> "mouseDragStartEvent");
        if (isMoveView()) mouseDragStartEventMoveView(pEvent);
        if (isPixelView()) mouseDragStartEventPixelView(pEvent);
    }

    private void mouseDragStartEventMoveView(final IUIEvent pEvent) {
        mLogger.fine(() -> "mouseDragStartEventMoveView");
        mMouseDragStartX = getViewOriginX();
        mMouseDragStartY = getViewOriginY();
    }

    private void mouseDragStartEventPixelView(final IUIEvent pEvent) {
        mSavepointId = getUndoRedoBuffer().startSavepoint("mouseDrag");
        eventToPixel(pEvent).ifPresent(p -> {
            grafittiCursor(pEvent, p);
            mMouseDragLastPixel = p;
        });
    }

    @Override
    public void scrollEvent(final IUIEvent pEvent) {
        mLogger.fine(() -> "scrollEvent");
    }

    @Override
    public void keyPressed(final IUIEvent pEvent) {
        mLogger.fine(() -> "keyPressed " + pEvent.getKey());
        if ("G".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mMoveContainer);
        if ("P".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mPixelControlContainer);
        if ("V".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mVertexControlContainer);
        mPictureControl.drawGrafitti(); // TODO should only be for the undo redo events
    }

    @Override
    public void keyReleased(final IUIEvent pEvent) {
        mLogger.fine(() -> "keyReleased " + pEvent.getKey());
    }

    @Override
    public void keyTyped(final IUIEvent pEvent) {
        mLogger.fine(() -> "keyTyped " + pEvent.getKey());
    }


    @Override
    public void read(final IPersistDB pDB, final String pId) {
        super.read(pDB, pId);
        mContainerList.setSelectedIndex(pDB.read(pId + ".selectedContainer"));
    }

    @Override
    public void write(final IPersistDB pDB, final String pId) throws IOException {
        super.write(pDB, pId);
        pDB.write(pId + ".selectedContainer", String.valueOf(mContainerList.getSelectedIndex()));
    }

    private boolean isMoveView() {
        return mContainerList.getSelectedContainer() == mMoveContainer;
    }

    private boolean isPixelView() {
        return mContainerList.getSelectedContainer() == mPixelControlContainer;
    }

    private boolean isVertexView() {
        return mContainerList.getSelectedContainer() == mVertexControlContainer;
    }

    private Properties getProperties() {
        return Services.getServices().getProperties();
    }
}
