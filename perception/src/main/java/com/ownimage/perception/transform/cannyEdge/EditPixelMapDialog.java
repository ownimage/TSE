/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.ContainerList;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.Bounds;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Rectangle;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.event.UIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CropTransform;
import lombok.NonNull;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator, IGrafitti {
    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private enum GraffitiAction {
        On, Off
    }

    private enum PixelAction {
        On("On", 1),
        Off("Off", 1),
        OffWide("Off Wide", 5),
        OffVeryWide("Off Very Wide", 15),
        OffVeryVeryWide("Off Very Very Wide", 45),
        Toggle("Toggle", 1),
        DeletePixelChain("Delete Pixel Chain", 1),
        DeletePixelChainWide("Delete Pixel Chain Wide", 15),
        DeletePixelChainVeryWide("Delete Pixel Chain Very Wide", 45),
        PixelChainThickness("Change Pixel Chain Thickness", 2),
        CopyToClipboard("Copy To Clipboard", 1),
        ApproximateCurvesOnly("Approximate Curves Only", 1);

        private final String mName;
        private final int mCursorSize;

        PixelAction(final String pName, int pCursorSize) {
            mName = pName;
            mCursorSize = pCursorSize;
        }

        public String toString() {
            return mName;
        }

        public int getCursorSize() {
            return mCursorSize;
        }
    }

    private PixelMap mPixelMap;
    final StrongReference<PixelMap> mResultantPixelMap;
    private final ActionControl mOkAction;
    private final ActionControl mCancelAction;
    private IDialogView mEditPixelMapDialogView;
    private UndoRedoBuffer mPixelMapUndoRedoBuffer = new UndoRedoBuffer(100);
    private boolean mMutating = false;
    private final CannyEdgeTransform mTransform;
    private IView mView;
    private final CropTransform mCropTransform;
    private final PictureControl mPictureControl = new PictureControl("Preview", "preview",
            NullContainer, new PictureType(100, 100));
    private final IContainer mGeneralContainer = newContainer("General", "general",
            true).addTitle().addBottomPadding();
    private final BooleanControl mShowCurves = new BooleanControl("Show Curves", "showCurves",
            mGeneralContainer, false);
    private final BooleanControl mAutoUpdateCurves = new BooleanControl("Auto Update Curves", "autoUpdateCurves",
            mGeneralContainer, false);
    private final ActionControl mUpdateCurves = new ActionControl("Update Curves", "updateCurves",
            mGeneralContainer, this::updateCurves);
    private final ContainerList mContainerList = new ContainerList("Edit PixelMap", "editPixelMap");
    private final IContainer mMoveContainer = mContainerList.add(newContainer("Move", "move", true));
    private final IContainer mPixelControlContainer = mContainerList.add(newContainer("Pixel", "pixel", true));
    // General Container
    private final IntegerControl mPixelMapWidth;
    private final IntegerControl mPixelMapHeight;
    private final IntegerControl mPreviewSize;
    private final IntegerControl mZoom;
    private final IntegerControl mViewOriginX;
    private final IntegerControl mViewOriginY;
    // Pixel Container
    private final ColorControl mEdgeColor = new ColorControl("Edge Color", "edgeColor",
            mPixelControlContainer, getProperties().getCETEPMDEdgeColor());
    private final ColorControl mNodeColor = new ColorControl("Node Color", "nodeColor",
            mPixelControlContainer, getProperties().getCETEPMDNodeColor());
    private final ColorControl mWorkingColor = new ColorControl("Working Color", "workingColor",
            mPixelControlContainer, getProperties().getCETEPMDWorkingColor());
    private final BooleanControl mShowGraffiti = new BooleanControl("Show Graffiti", "showGraffiti",
            mPixelControlContainer, true);
    private final ObjectControl<PixelAction> mPixelAction = new ObjectControl("Pixel Action", "pixelAction",
            mPixelControlContainer, PixelAction.On, PixelAction.values());
    private final ObjectControl<PixelChain.Thickness> mThickness = new ObjectControl("Thickness", "Thickness",
            mPixelControlContainer, IPixelChain.Thickness.None, IPixelChain.Thickness.values());
    private boolean mDialogIsAlive = false;
    private int mMouseDragStartX;
    private int mMouseDragStartY;
    private Pixel mMouseLastPixelPosition = null;
    private Pixel mMouseDragLastPixel = null;
    private Id mSavepointId;
    private final ArrayList<Pixel> mDragPixels = new ArrayList();
    private boolean mAutoUpdateCurvesDirty = false;

    public EditPixelMapDialog(
            @NonNull final CannyEdgeTransform pTransform,
            @NonNull final PixelMap pPixelMap,
            final String pDisplayName,
            final String pPropertyName,
            final ActionControl pOkAction,
            final ActionControl pCancelAction
    ) {
        super(pDisplayName, pPropertyName, Services.getServices().getUndoRedoBuffer());
        mTransform = pTransform;
        mPixelMap = pPixelMap;
        mResultantPixelMap = new StrongReference<>(mPixelMap);
        mOkAction = pOkAction;
        mCancelAction = pCancelAction;
        final Properties properties = Services.getServices().getProperties();
        mPixelMapWidth = new IntegerControl("PixelMap Width", "pixelMapWidth", mGeneralContainer, getWidth(), 0, getWidth(), 50).setEnabled(false);
        mPixelMapHeight = new IntegerControl("PixelMap Height", "pixelMapHeight", mGeneralContainer, getHeight(), 0, getHeight(), 50).setEnabled(false);
        mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, properties.getCETEPMDPreviewSize(), properties.CETEPMDPreviewSizeModel);
        mZoom = new IntegerControl("Zoom", "zoom", mGeneralContainer, properties.getCETEPMDZoom(), properties.CETEPMDZoomModel);
        mViewOriginX = new IntegerControl("View X", "x", mMoveContainer, 0, 0, getWidth(), 50);
        mViewOriginY = new IntegerControl("View Y", "y", mMoveContainer, 0, 0, getHeight(), 50);
        mCropTransform = new CropTransform(Services.getServices().getPerception(), true);
        mPictureControl.setGrafitti(this);
        mPictureControl.setUIListener(this);
        updateControlVisibility();
        updateCurves();
        mEdgeColor.addControlChangeListener(this::mGrafittiChangeListener);
        mNodeColor.addControlChangeListener(this::mGrafittiChangeListener);
        mShowGraffiti.addControlChangeListener(this::mGrafittiChangeListener);
    }

    public PixelMap getPixelMap() {
        return mPixelMap;
    }

    private void mGrafittiChangeListener(Object pControl, boolean pIsMutating) {
        mPictureControl.drawGrafitti();
    }

    private void updateCurves() {
        mAutoUpdateCurvesDirty = false;
        setCrop();
        if (mTransform.isInitialized()) {
            mTransform.setPixelMap(mPixelMap);
            if (getPreviewSize() != mPictureControl.getWidth()) {
                final PictureType pictureType = new PictureType(getPreviewSize(), getPreviewSize());
                mPictureControl.setValue(pictureType);
            }
            Services.getServices().getRenderService()
                    .getRenderJobBuilder("EditPixelMapDialog::updateCurves", mPictureControl, mCropTransform)
                    .withAllowTerminate(false)
                    .build()
                    .run();
        }
    }

    @Override
    public boolean validateControl(final Object pControl) {
        if (pControl == mViewOriginX) {
            final boolean valid = getWidth() > getViewOriginX() + getWidth() / getZoom();
            return valid;
        }
        if (pControl == mViewOriginY) {
            final boolean valid = getHeight() > getViewOriginY() + getHeight() / getZoom();
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

    private void setMutating(final boolean pMutating) {
        mMutating = pMutating;
    }

    @Override
    public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
        mLogger.fine(() -> "controlChangeEvent for " + pControl.getDisplayName());
        if (isMutating()) return;
        if (pControl != null) {
            if (pControl.isOneOf(mViewOriginX, mViewOriginY, mZoom, mPreviewSize, mShowCurves)) {
                updateCurves();
            }
            if (pControl.isOneOf(mShowCurves, mAutoUpdateCurves, mPixelAction)) {
                updateControlVisibility();
            }
            if (pControl.isOneOf(mAutoUpdateCurves)) {
                if (mAutoUpdateCurvesDirty) updateCurves();
            }
            if (pControl.isOneOf(mShowGraffiti)) {
                drawGraffiti();
            }
        }
    }

    private void updateControlVisibility() {
        mAutoUpdateCurves.setVisible(mShowCurves.getValue());
        mUpdateCurves.setVisible(mShowCurves.getValue() && !mAutoUpdateCurves.getValue());
        mThickness.setEnabled(mPixelAction.getValue() == PixelAction.PixelChainThickness);
    }

    @Override
    public IView createView() {
        if (mView == null) {
            final VFlowLayout vflow = new VFlowLayout(mGeneralContainer, mContainerList);
            final HFlowLayout hflow = new HFlowLayout(mPictureControl, vflow);
            mView = ViewFactory.getInstance().createView(hflow);
            addView(mView);
        }
        return mView;
    }

    private IDialogView getDialogView() {
        if (mEditPixelMapDialogView == null) {
            mEditPixelMapDialogView = ViewFactory.getInstance().createDialog(this,
                    DialogOptions.builder().withCompleteFunction(() -> {
                        mTransform.setPixelMap(mResultantPixelMap.get());
                        dialogClose();
                    }).build(),
                    mPixelMapUndoRedoBuffer,
                    mCancelAction,
                    mOkAction.doBefore(() -> mResultantPixelMap.set(mPixelMap)));
        }
        return mEditPixelMapDialogView;
    }

    public void showDialog() {
        PixelMap pixelMap = mTransform.getPixelMap().get();
        mPixelMap.checkCompatibleSize(pixelMap);
        mPixelMap = pixelMap;
        mResultantPixelMap.set(pixelMap);
        mDialogIsAlive = true;
        updateCurves();
        getDialogView().showModal();
    }

    private void dialogClose() {
        mDialogIsAlive = false;
    }

    public UndoRedoBuffer getUndoRedoBuffer() {
        return mPixelMapUndoRedoBuffer;
    }

    @Override
    synchronized public void graffiti(final GrafittiHelper pGrafittiHelper) {
        Framework.logEntry(mLogger);
        Framework.checkStateNotNull(mLogger, mPixelMap, "mPixelMap");
        final int xSize = Math.floorDiv(getWidth(), getZoom()) + 1;
        final int ySize = Math.floorDiv(getHeight(), getZoom()) + 1;
        graffiti(pGrafittiHelper, getViewOriginX(), getViewOriginY(), getViewOriginX() + xSize, getViewOriginY() + ySize);
        if (mShowGraffiti.getValue()) {
            mPixelMap.streamPixelChains().forEach(pc -> graffitiPixelChain(pGrafittiHelper, pc));
        }
    }

    private void graffiti(final GrafittiHelper pGrafittiHelper, final int xMin, final int yMin, final int xMax, final int yMax) {
        Framework.logEntry(mLogger);
        final Range2D range = new Range2D(xMin, xMax, yMin, yMax);
        range.forEach((x, y) -> {
            final Pixel pixel = mPixelMap.getPixelAt(x, y);
            if (pixel.isNode(mPixelMap) || pixel.isEdge(mPixelMap)) {
                graffitiPixel(pGrafittiHelper, pixel);
            }
        });
    }

    private void graffitiPixel(final GrafittiHelper pGrafittiHelper, @NonNull final Pixel pPixel) {
        Framework.logEntry(mLogger);
        final Rectangle r = pixelToGrafittiRectangle(pPixel);
        final Color c = getPixelColor(pPixel);
        pGrafittiHelper.clearRectangle(r);
        pGrafittiHelper.drawFilledRectangle(r, c);
    }

    private Color getPixelColor(@NonNull final Pixel pPixel) {
        return pPixel.isNode(mPixelMap) ? mNodeColor.getValue() : mEdgeColor.getValue();
    }

    private Rectangle pixelToGrafittiRectangle(@NonNull final Pixel pPixel) {
        final int x = pPixel.getX();
        final int y = pPixel.getY();
        final double x1 = pixelXToGrafittiX(x, getViewOriginX(), getWidth(), getZoom());
        final double x2 = pixelXToGrafittiX(x + 1, getViewOriginX(), getWidth(), getZoom());
        final double y1 = pixelYToGrafittiY(y, getViewOriginY(), getHeight(), getZoom());
        final double y2 = pixelYToGrafittiY(y + 1, getViewOriginY(), getHeight(), getZoom());
        return new Rectangle(x1, y1, x2, y2);
    }

    private double pixelXToGrafittiX(final int pX, final int pXMin, final int pPMWidth, final int pZoom) {
        return (double) (pX - pXMin) * pZoom / pPMWidth;
    }

    private double pixelYToGrafittiY(final int pY, final int pYMin, final int pPMHeight, final int pZoom) {
        return (double) (pY - pYMin) * pZoom / pPMHeight;
    }

    private void graffitiPixelChain(final GrafittiHelper pGrafittiHelper, final PixelChain pPixelChain) {
        Framework.logEntry(mLogger);
        pPixelChain.streamSegments().forEach(s -> graffitiSegment(pGrafittiHelper, s));
    }

    private void graffitiSegment(final GrafittiHelper pGrafittiHelper, final ISegment pSegment) {
        Framework.logEntry(mLogger);
        final SegmentGraffitiHelper segmentGrafittiHelper = new SegmentGraffitiHelper(pGrafittiHelper, this::UHVWtoView);
        mPixelMap.getPixelChainForSegment(pSegment).ifPresent(pc -> pSegment.graffiti(mPixelMap, pc, segmentGrafittiHelper));
    }

    private Point UHVWtoView(final Point pUHVW) {
        final double x = (pUHVW.getX() * getHeight() - getViewOriginX()) * getZoom() / getWidth();
        final double y = (pUHVW.getY() * getHeight() - getViewOriginY()) * getZoom() / getHeight();
        return new Point(x, y);
    }

    private Optional<Pixel> eventToPixel(final IUIEvent pEvent) {
        return eventXYToPixel(pEvent.getX(), pEvent.getY());
    }

    private Optional<Pixel> eventXYToPixel(final int pX, final int pY) {
        final int x = getViewOriginX() + (pX * getWidth() / (getZoom() * getPreviewSize()));
        final int y = getViewOriginY() + (pY * getHeight() / (getZoom() * getPreviewSize()));
        return mPixelMap.getOptionalPixelAt(x, y);
    }

    private void setCrop() {
        if (mPixelMap != null) {
            final double left = (double) getViewOriginX() / getWidth();
            final double right = left + 1.0d / getZoom();
            final double bottom = (double) getViewOriginY() / getHeight();
            final double top = bottom + 1.0d / getZoom();
            mCropTransform.setCrop(left, bottom, right, top);
            if (mShowCurves.getValue())
                mCropTransform.setPreviousTransform(mTransform);
            else mCropTransform.setPreviousTransform(mTransform.getPreviousTransform());
        }
    }

    @Override
    public void mouseClickEvent(final IUIEvent pEvent) {
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isPixelView()) {
            pixel.ifPresent(p -> mouseClickEventPixelView(pEvent, p));
        }
    }

    private void autoUpdatePreview() {
        if (mAutoUpdateCurves.getValue()) updateCurves();
        else mAutoUpdateCurvesDirty = true;
    }

    private void mouseClickEventPixelView(@NonNull final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        boolean change = false;
        if (pPixel != null) {
            if (isPixelActionOn()) change |= actionPixelOn(pPixel);
            if (isPixelActionOff()) change |= actionPixelOff(pPixel);
            if (isPixelActionToggle()) change |= actionPixelToggle(pPixel);
            if (isPixelActionDeletePixelChain()) change |= actionDeletePixelChain(pPixel);
            if (isPixelChainThickness()) change |= actionPixelChainThickness(pPixel);
            if (isCopyToClipboard()) actionCopyToClipboard(pPixel);
            if (isPixelChainApproximateCurvesOnly()) actionPixelChainApproximateCurvesOnly(pPixel);
            if (change) {
                drawGrafitti();
                autoUpdatePreview();
            }
        }
        graffitiCursor(pEvent, pPixel);
    }

    private void actionCopyToClipboard(Pixel pPixel) {
        StringBuilder builder = new StringBuilder();
        StrongReference<Bounds> bounds = new StrongReference<>(null);
        mPixelMap.getPixelChains(pPixel).stream().findFirst().ifPresent(pixelChain -> {
            bounds.set(new Bounds());
            pixelChain.streamPixels().forEach(pixel -> bounds.set(bounds.get().getBounds(pixel)));
            builder.append("int xMargin = 2;\n");
            builder.append("int yMargin = 2;\n");
            builder.append("Pixel offset = new Pixel(xMargin, yMargin);\n");

            builder.append("IPixelMapTransformSource ts = new PixelMapTransformSource(");
            builder.append(mPixelMap.getHeight());
            builder.append(", ");
            builder.append(mPixelMap.getLineTolerance());
            builder.append(", ");
            builder.append(mPixelMap.getLineCurvePreference());
            builder.append(");\n");

            builder.append("PixelMap pixelMap = new PixelMap(");
            builder.append(bounds.get().getWidth() + " + 2 * xMargin");
            builder.append(", ");
            builder.append(bounds.get().getHeight() + " + 2 * yMargin");
            builder.append(", false, ts);\n");

            builder.append("pixelMap.actionProcess(null);\n");

            pixelChain.streamPixels().forEach(pixel -> {
                builder.append("pixelMap = pixelMap.actionPixelOn(new Pixel(");
                builder.append(pixel.minus(bounds.get().getLowerLeft()).getX());
                builder.append(", ");
                builder.append(pixel.minus(bounds.get().getLowerLeft()).getY());
                builder.append(").add(offset));\n");
            });
        });
        builder.append("\n\nassertEquals(1, pixelMap.getPixelChainCount());\n");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
        System.out.println("CLIPBOARD: " + builder);
    }

    private void drawGrafitti() {
        Framework.logEntry(mLogger);
        mPictureControl.drawGrafitti();
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
        return mPixelAction.getValue() == PixelAction.DeletePixelChain
                || mPixelAction.getValue() == PixelAction.DeletePixelChainWide
                || mPixelAction.getValue() == PixelAction.DeletePixelChainVeryWide;
    }

    private boolean isPixelChainThickness() {
        return mPixelAction.getValue() == PixelAction.PixelChainThickness;
    }

    private boolean isPixelChainApproximateCurvesOnly() {
        return mPixelAction.getValue() == PixelAction.ApproximateCurvesOnly;
    }

    private boolean isCopyToClipboard() {
        return mPixelAction.getValue() == PixelAction.CopyToClipboard;
    }

    public boolean actionPixelOn(@NonNull List<Pixel> pPixels) {
        if (pPixels.isEmpty()) return false;
        final PixelMap undo = mPixelMap;
        mPixelMap = mPixelMap.actionPixelOn(pPixels);
        if (mPixelMap != undo) {
            addUndoRedoEntry("Action Pixel Off", undo, mPixelMap);
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelOn(@NonNull final Pixel pPixel) {
        final PixelMap undo = mPixelMap;
        mPixelMap = mPixelMap.actionPixelOn(pPixel);
        if (mPixelMap != undo) {
            addUndoRedoEntry("Action Pixel Off", undo, mPixelMap);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDoubleClickEvent(final IUIEvent pEvent) {
    }

    @Override
    public void mouseDragEndEvent(final IUIEvent pEvent) {
        if (isPixelView()) {
            mouseDragEndEventPixelView(pEvent);
        }
        Framework.logExit(mLogger);
    }

    private void mouseDragEndEventPixelView(final IUIEvent pEventl) {
        Framework.logValue(mLogger, "mDragPixels.size()", mDragPixels.size());
        Framework.logValue(mLogger, "mPixelMap", mPixelMap);
        if (isPixelActionOn()) {
            actionPixelOn(mDragPixels);
        }
        mDragPixels.clear();
        getUndoRedoBuffer().endSavepoint(mSavepointId);
        mSavepointId = null;
        drawGrafitti();
        autoUpdatePreview();
        mMouseDragLastPixel = null;
        Framework.logExit(mLogger);
    }

    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
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
    private void mouseDragEventPixelViewFillIn(final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        mLogger.fine(() -> String.format("mouseDragEventPixelViewFillIn %s, %s", pPixel, mMouseDragLastPixel));
        if (pPixel != null && (isPixelActionOn() || isPixelActionOff())) {
            if (mMouseDragLastPixel != null && !mMouseDragLastPixel.equals(pPixel)) {
                mLogger.fine("mouseDragEventPixelViewFillIn ...");
                final int dX = pPixel.getX() - mMouseDragLastPixel.getX();
                final int dY = pPixel.getY() - mMouseDragLastPixel.getY();
                if (Math.abs(dX) >= Math.abs(dY)) { // fill in missing x
                    final int from = Math.min(mMouseDragLastPixel.getX(), pPixel.getX());
                    final int to = Math.max(mMouseDragLastPixel.getX(), pPixel.getX());
                    IntStream.range(from, to).forEach(x -> {
                        final int y = (int) Math.round(mMouseDragLastPixel.getY() + (((double) x - mMouseDragLastPixel.getX()) / dX) * dY);
                        final Optional<Pixel> pixel = mPixelMap.getOptionalPixelAt(x, y);
                        mLogger.fine(() -> String.format("mouseDragEventPixelViewFillIn X  %s, %s", x, y));
                        pixel.ifPresent(p -> mouseDragEventPixelView(UIEvent.createMouseEvent(pEvent, x, y), p));
                    });
                } else { // fill in missing y
                    final int from = Math.min(mMouseDragLastPixel.getY(), pPixel.getY());
                    final int to = Math.max(mMouseDragLastPixel.getY(), pPixel.getY());
                    IntStream.range(from, to).forEach(y -> {
                        final int x = (int) Math.round(mMouseDragLastPixel.getX() + (((double) y - mMouseDragLastPixel.getY()) / dY) * dX);
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

    private void mouseDragEventMoveView(final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        setMutating(true);
        final int x = mViewOriginX.getValue();
        final int y = mViewOriginY.getValue();
        mViewOriginX.setValue((int) (mMouseDragStartX - pEvent.getNormalizedDeltaX() * mTransform.getWidth() / getZoom()));
        mViewOriginY.setValue((int) (mMouseDragStartY - pEvent.getNormalizedDeltaY() * mTransform.getHeight() / getZoom()));
        if (x != mViewOriginX.getValue() || y != mViewOriginY.getValue()) updateCurves();
        setMutating(false);
    }

    private void mouseDragEventPixelView(final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        boolean change = false;
        if (pPixel != null) {
            if (isPixelActionOn()) mouseDragEventPixelViewOn(pEvent, pPixel);
            if (isPixelActionOff()) change |= actionPixelOff(pPixel);
            if (isPixelActionToggle()) change |= mouseDragEventPixelViewToggle(pEvent, pPixel);
            if (isPixelActionDeletePixelChain()) change |= actionDeletePixelChain(pPixel);
            if (change) {
                graffitiCursor(pEvent, pPixel);
            }
        }
    }

    private void mouseDragEventPixelViewOn(final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        if (pPixel.isEdge(mPixelMap)) return;
        if (mDragPixels.contains(pPixel)) return;
        mDragPixels.add(pPixel);
        graffitiPixelWorkingColor(pPixel);
    }

    private void graffitiPixel(@NonNull Pixel pPixel, @NonNull ColorControl pColor) {
        Framework.logEntry(mLogger);
        mPictureControl.updateGrafitti(
                g -> g.drawFilledRectangle(pixelToGrafittiRectangle(pPixel), pColor)
        );
    }

    private void graffitiPixelWorkingColor(@NonNull Pixel pPixel) {
        Framework.logEntry(mLogger);
        mPictureControl.updateGrafitti(
                g -> g.drawFilledRectangle(pixelToGrafittiRectangle(pPixel), mWorkingColor)
        );
    }

    synchronized private boolean actionPixelOff(@NonNull final Pixel pPixel) {
        final PixelMap undo = mPixelMap;
        mPixelMap = mPixelMap.actionPixelOff(pPixel, getCursorSize());
        boolean changesMade = mPixelMap != undo;
        if (changesMade) {
            addUndoRedoEntry("Action Pixel Off", undo, mPixelMap);
        }
        return changesMade;
    }

    private void addUndoRedoEntry(final String pDescription, final PixelMap pUndo, final PixelMap pRedo) {
        getUndoRedoBuffer().add(pDescription, () -> setPixelMap(pUndo), () -> setPixelMap(pRedo));
    }

    private void setPixelMap(PixelMap pPixelMap) {
        mPixelMap = pPixelMap;
    }

    private void graffitiCursor(final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        Framework.logEntry(mLogger);
        if (pPixel != null) {
            final IGrafitti g = graffitiHelper ->
                    graffitiHelper.drawCircle(pEvent.getNormalizedX(), pEvent.getNormalizedY(), getRadius(), Color.red, false);
            mPictureControl.drawCursor(g);
            mMouseLastPixelPosition = pPixel;
        }
    }

    private double getRadius() {
        return (double) getCursorSize() * getZoom() / mPixelMapHeight.getValue();
    }

    private int getCursorSize() {
        return mPixelAction.getValue().getCursorSize();
    }

    private void drawGraffiti() {
        Framework.logEntry(mLogger);
        mPictureControl.drawGrafitti();
    }

    private boolean mouseDragEventPixelViewToggle(@NonNull final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        boolean change = false;
        if (!pPixel.equals(mMouseLastPixelPosition)) {
            change |= actionPixelToggle(pPixel);
            mMouseLastPixelPosition = pPixel;
        }
        return change;
    }

    private boolean actionDeletePixelChain(@NonNull final Pixel pPixel) {
        final PixelMap undo = mPixelMap;
        mPixelMap = mPixelMap.actionDeletePixelChain(pPixel, getCursorSize());
        if (undo != mPixelMap) {
            addUndoRedoEntry("Delete PixelChain", undo, mPixelMap);
            mPictureControl.drawGrafitti();
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelToggle(@NonNull final Pixel pPixel) {
        final PixelMap undo = mPixelMap;
        graffitiPixelWorkingColor(pPixel);
        mPixelMap = mPixelMap.actionPixelToggle(pPixel);
        addUndoRedoEntry("Action Pixel Off", undo, mPixelMap);
        mPictureControl.drawGrafitti();
        return true;
    }

    synchronized private boolean actionPixelChainApproximateCurvesOnly(@NonNull final Pixel pPixel) {
        final PixelMap undo = mPixelMap;
        mPixelMap = mPixelMap.actionPixelChainApproximateCurvesOnly(pPixel);
        if (undo != mPixelMap) {
            addUndoRedoEntry("Reapproximate PixelChain", undo, mPixelMap);
            mPictureControl.drawGrafitti();
            return true;
        }
        return false;
    }

    private boolean actionPixelChainThickness(@NonNull final Pixel pPixel) {
        final PixelMap undo = mPixelMap;
        if (!mPixelMap.getPixelChains(pPixel).isEmpty()) {
            graffitiPixelWorkingColor(pPixel);
        }
        mPixelMap = mPixelMap.actionSetPixelChainThickness(pPixel, mThickness.getValue());
        if (undo != mPixelMap) {
            addUndoRedoEntry("Delete PixelChain", undo, mPixelMap);
            mPictureControl.drawGrafitti();
            return true;
        }
        return false;
    }

    @Override
    public void mouseMoveEvent(final IUIEvent pEvent) {
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        pixel.filter(p -> !p.equals(mMouseLastPixelPosition))
                .ifPresent(p -> {
                    mMouseLastPixelPosition = p;
                    graffitiCursor(pEvent, p);
                });
    }

    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        if (isMoveView()) mouseDragStartEventMoveView(pEvent);
        if (isPixelView()) mouseDragStartEventPixelView(pEvent);
    }

    private void mouseDragStartEventMoveView(final IUIEvent pEvent) {
        mMouseDragStartX = getViewOriginX();
        mMouseDragStartY = getViewOriginY();
    }

    private void mouseDragStartEventPixelView(final IUIEvent pEvent) {
        mSavepointId = getUndoRedoBuffer().startSavepoint("MouseDrag");
        eventToPixel(pEvent).ifPresent(p -> {
            graffitiCursor(pEvent, p);
            mMouseDragLastPixel = p;
        });
        mDragPixels.clear();
    }

    @Override
    public void scrollEvent(final IUIEvent pEvent) {
    }

    @Override
    public void keyPressed(final IUIEvent pEvent) {
        mLogger.fine(() -> "keyPressed " + pEvent.getKey());
        if ("G".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mMoveContainer);
        if ("P".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mPixelControlContainer);
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

    private Properties getProperties() {
        return Services.getServices().getProperties();
    }
}
