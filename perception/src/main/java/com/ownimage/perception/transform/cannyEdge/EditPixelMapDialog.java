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
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.Bounds;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Rectangle;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.*;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.event.ImmutableUIEvent;
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
import lombok.val;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator, IGrafitti {
    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;
    private final ActionControl mOkAction;
    private final ActionControl mCancelAction;
    private final CannyEdgeTransform mCannyEdgeTransform;
    private final CropTransform mCropTransform;
    private final PictureControl mPictureControl;
    private final IContainer mGeneralContainer;

    private final IntegerControl mPixelMapWidth;
    private final IntegerControl mPixelMapHeight;
    private final IntegerControl mPreviewSize;
    private final IntegerControl mZoom;
    private final IntegerControl mViewOriginX;
    private final IntegerControl mViewOriginY;
    private final BooleanControl mShowGraffiti;
    private final BooleanControl mShowCurves;
    private final BooleanControl mAutoUpdateCurves;
    private final ActionControl mUpdateCurves;
    private final BooleanControl mShowEdges;
    private final DoubleControl mEdgesOpacity;
    private final ColorControl mEdgeColor;
    private final ColorControl mNodeColor;
    private final ColorControl mWorkingColor;
    private final ObjectControl<PixelAction> mPixelAction;
    private final ObjectControl<PixelChain.Thickness> mThickness;

    private final Collection<Pixel> mWorkingPixelsArray = new HashSet();

    // these are to prevent the garbage collection of the validators
    private final IControlValidator<?> mZoomValidator = this::zoomValidator;
    private final IControlValidator<?> mViewOriginXValidator = this::viewOriginXValidator;
    private final IControlValidator<?> mViewOriginYValidator = this::viewOriginYValidator;

    private PixelMap mPixelMap;
    private PixelMap mUndoPixelMap;
    private IDialogView mEditPixelMapDialogView;
    private IView mView;
    private ReentrantLock mViewEnabledLock = new ReentrantLock();
    private UndoRedoBuffer mPixelMapUndoRedoBuffer;
    private boolean mMutating = false;
    private boolean mIsMoveModeActive = false;
    private int mMouseDragStartX;
    private int mMouseDragStartY;
    private Pixel mMouseLastPixelPosition = null;
    private Pixel mMouseDragLastPixel = null;
    private Id mSavepointId;

    private boolean mAutoUpdateCurvesDirty = false;

    public EditPixelMapDialog(@NonNull final CannyEdgeTransform pTransform) {
        super("Edit PixelMap Dialog", "pixelMapEditor", Services.getServices().getUndoRedoBuffer());
        mCannyEdgeTransform = pTransform;
        mPixelMap = pTransform.getPixelMap().orElseThrow();
        mOkAction = ActionControl.create("OK", NullContainer, () -> {
        });
        mCancelAction = ActionControl.create("Cancel", NullContainer, () -> {
            setPixelMap(getUndoPixelMap());
        });

        mPictureControl = new PictureControl("Preview", "preview", NullContainer, new PictureType(100, 100));
        mGeneralContainer = newContainer("General", "general", true).addBottomPadding();
        mPixelMapWidth = new IntegerControl("PixelMap Width", "pixelMapWidth", mGeneralContainer, getWidth(), 0, 10000, 50).setEnabled(false);
        mPixelMapHeight = new IntegerControl("PixelMap Height", "pixelMapHeight", mGeneralContainer, getHeight(), 0, 10000, 50).setEnabled(false);
        mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, getProperties().getCETEPMDPreviewSize(), getProperties().CETEPMDPreviewSizeModel);
        mZoom = new IntegerControl("Zoom", "zoom", mGeneralContainer, getProperties().getCETEPMDZoom(), getProperties().CETEPMDZoomModel);
        mViewOriginX = new IntegerControl("View X", "x", mGeneralContainer, 0, 0, getWidth(), 50);
        mViewOriginY = new IntegerControl("View Y", "y", mGeneralContainer, 0, 0, getHeight(), 50);
        mShowGraffiti = new BooleanControl("Show Graffiti", "showGraffiti", mGeneralContainer, true);
        mShowCurves = new BooleanControl("Show Curves", "showCurves", mGeneralContainer, false);
        mAutoUpdateCurves = new BooleanControl("Auto Update Curves", "autoUpdateCurves", mGeneralContainer, false);
        mUpdateCurves = new ActionControl("Update Curves", "updateCurves", mGeneralContainer, this::updateCurves);
        mShowEdges = new BooleanControl("Show Edges", "showEdges", mGeneralContainer, true);
        mEdgesOpacity = new DoubleControl("Edges Opacity", "edgesOpacity", mGeneralContainer, 1.0d);
        mEdgeColor = new ColorControl("Edge Color", "edgeColor", mGeneralContainer, getProperties().getCETEPMDEdgeColor());
        mNodeColor = new ColorControl("Node Color", "nodeColor", mGeneralContainer, getProperties().getCETEPMDNodeColor());
        mWorkingColor = new ColorControl("Working Color", "workingColor", mGeneralContainer, getProperties().getCETEPMDWorkingColor());
        mPixelAction = new ObjectControl("Pixel Action", "pixelAction", mGeneralContainer, PixelAction.On, PixelAction.values());
        mThickness = new ObjectControl("Thickness", "Thickness", mGeneralContainer, IPixelChain.Thickness.None, IPixelChain.Thickness.values());

        mCropTransform = new CropTransform(Services.getServices().getPerception(), true);
        mPictureControl.setGrafitti(this);
        mPictureControl.setUIListener(this);

        updateControlVisibility();
        updateCurves();

        mEdgeColor.addControlChangeListener(this::mGrafitiChangeListener);
        mNodeColor.addControlChangeListener(this::mGrafitiChangeListener);
        mShowGraffiti.addControlChangeListener(this::mGrafitiChangeListener);

        mZoom.addControlValidator(mZoomValidator);
        mViewOriginX.addControlValidator(mViewOriginXValidator);
        mViewOriginY.addControlValidator(mViewOriginYValidator);
    }

    private boolean zoomValidator(Object o) {
        val h = mPixelMapHeight.getValue();
        val w = mPixelMapWidth.getValue();
        val x = mViewOriginX.getValue();
        val y = mViewOriginY.getValue();
        val hoz = (double) h / mZoom.getValidateValue();
        val woz = (double) w / mZoom.getValidateValue();
        return (y + hoz) <= h && (x + woz) <= w;
    }

    private boolean viewOriginXValidator(Object o) {
        val w = mPixelMapWidth.getValue();
        val x = mViewOriginX.getValidateValue();
        val woz = (double) w / mZoom.getValidateValue();
        return (x + woz) <= w;
    }

    private boolean viewOriginYValidator(Object o) {
        val h = mPixelMapHeight.getValue();
        val y = mViewOriginY.getValidateValue();
        val hoz = (double) h / mZoom.getValidateValue();
        return (y + hoz) <= h;
    }

    public PixelMap getPixelMap() {
        return mPixelMap;
    }

    private void setPixelMap(@NonNull PixelMap pPixelMap) {
        mPixelMap = pPixelMap;
    }

    private PixelMap getUndoPixelMap() {
        return mUndoPixelMap;
    }

    private void setUndoPixelMap(@NonNull PixelMap pUndoPixelMap) {
        mUndoPixelMap = pUndoPixelMap;
    }

    private void mGrafitiChangeListener(Object pControl, boolean pIsMutating) {
        drawGraffiti();
    }

    private void updateCurves() {
        mAutoUpdateCurvesDirty = false;
        setCrop();
        if (mCannyEdgeTransform.isInitialized()) {
            mCannyEdgeTransform.setPixelMap(getPixelMap());
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
        return getPixelMap().getWidth();
    }

    private int getHeight() {
        return getPixelMap().getHeight();
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
            if (pControl.isOneOf(mShowCurves, mAutoUpdateCurves, mPixelAction, mShowEdges)) {
                updateControlVisibility();
            }
            if (pControl.isOneOf(mAutoUpdateCurves)) {
                if (mAutoUpdateCurvesDirty) updateCurves();
            }
            if (pControl.isOneOf(mShowGraffiti, mShowEdges, mEdgesOpacity, mNodeColor, mEdgeColor)) {
                drawGraffiti();
            }
        }
    }

    private void updateControlVisibility() {
        mAutoUpdateCurves.setVisible(mShowCurves.getValue());
        mUpdateCurves.setVisible(mShowCurves.getValue() && !mAutoUpdateCurves.getValue());
        mThickness.setEnabled(isPixelActionChainThickness());
        mEdgeColor.setVisible(mShowEdges.getValue());
        mNodeColor.setVisible(mShowEdges.getValue());
        mEdgesOpacity.setVisible(mShowEdges.getValue());
    }

    @Override
    public IView createView() {
        if (mView == null) {
            final HFlowLayout hflow = new HFlowLayout(mPictureControl, mGeneralContainer);
            mView = ViewFactory.getInstance().createView(hflow);
            addView(mView);
        }
        return mView;
    }

    private IDialogView getDialogView() {
        if (mEditPixelMapDialogView == null) {
            mEditPixelMapDialogView = ViewFactory.getInstance().createDialog(this,
                    DialogOptions.builder().withCompleteFunction(() -> {
                        mViewEnabledLock.lock();
                        mViewEnabledLock.unlock();
                        mCancelAction.performAction();
                    }).build(),
                    mPixelMapUndoRedoBuffer,
                    mCancelAction,
                    mOkAction
            );
        }
        return mEditPixelMapDialogView;
    }

    public PixelMap showDialog() {
        mPixelMapUndoRedoBuffer = new EPMDUndoRedoBuffer();
        PixelMap pixelMap = mCannyEdgeTransform.getPixelMap().get();
        getPixelMap().checkCompatibleSize(pixelMap);
        setPixelMap(pixelMap);
        setUndoPixelMap(pixelMap);
        setViewEnabled(true);
        updateCurves();
        getDialogView().showModal(mPixelMapUndoRedoBuffer);
        return mPixelMap;
    }

    public UndoRedoBuffer getUndoRedoBuffer() {
        return mPixelMapUndoRedoBuffer;
    }

    @Override
    synchronized public void graffiti(final GrafittiHelper pGrafittiHelper) {
        Framework.logEntry(mLogger);
        Framework.checkStateNotNull(mLogger, getPixelMap(), "mPixelMap");
        if (mShowEdges.getValue()) {
            final int xSize = Math.floorDiv(getWidth(), getZoom()) + 1;
            final int ySize = Math.floorDiv(getHeight(), getZoom()) + 1;
            graffiti(pGrafittiHelper, getViewOriginX(), getViewOriginY(), getViewOriginX() + xSize, getViewOriginY() + ySize);
        }
        if (mShowGraffiti.getValue()) {
            getPixelMap().streamPixelChains().forEach(pc -> graffitiPixelChain(pGrafittiHelper, pc));
        }
    }

    private void graffiti(final GrafittiHelper pGrafittiHelper, final int xMin, final int yMin, final int xMax, final int yMax) {
        Framework.logEntry(mLogger);
        final Range2D range = new Range2D(xMin, xMax, yMin, yMax);
        range.forEach((x, y) -> {
            final Pixel pixel = getPixelMap().getPixelAt(x, y);
            if (pixel.isNode(getPixelMap()) || pixel.isEdge(getPixelMap())) {
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
        val color = pPixel.isNode(getPixelMap()) ? mNodeColor.getValue() : mEdgeColor.getValue();
        return KColor.alphaMultiply(color, mEdgesOpacity.getValue());
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
        pPixelChain.streamSegments().forEach(s -> graffitiSegment(pGrafittiHelper, pPixelChain, s));
    }

    private void graffitiSegment(
            final GrafittiHelper pGrafittiHelper,
            final PixelChain pPixelChain,
            final ISegment pSegment
    ) {
        Framework.logEntry(mLogger);
        final SegmentGraffitiHelper segmentGrafittiHelper = new SegmentGraffitiHelper(pGrafittiHelper, this::UHVWtoView);
        pSegment.graffiti(getPixelMap(), pPixelChain, segmentGrafittiHelper);
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
        return getPixelMap().getOptionalPixelAt(x, y);
    }

    private void setCrop() {
        if (getPixelMap() != null) {
            final double left = (double) getViewOriginX() / getWidth();
            final double right = left + 1.0d / getZoom();
            final double bottom = (double) getViewOriginY() / getHeight();
            final double top = bottom + 1.0d / getZoom();
            mCropTransform.setCrop(left, bottom, right, top);
            if (mShowCurves.getValue() && !mMutating)
                mCropTransform.setPreviousTransform(mCannyEdgeTransform);
            else mCropTransform.setPreviousTransform(mCannyEdgeTransform.getPreviousTransform());
        }
    }

    @Override
    public void mouseClickEvent(final ImmutableUIEvent pEvent) {
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isPixelModeActive()) {
            pixel.ifPresent(p -> mouseClickEventPixelView(pEvent, p));
        }
    }

    private void autoUpdateCurves() {
        if (mAutoUpdateCurves.getValue()) updateCurves();
        else {
            mAutoUpdateCurvesDirty = true;
            drawGraffiti();
        }
    }

    private void mouseClickEventPixelView(@NonNull final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        disableDialogWhile(() -> {
            boolean change = false;
            if (pPixel != null) {
                if (isPixelActionOn()) change |= actionPixelOn(pPixel);
                if (isPixelActionOff()) change |= actionPixelOff(pPixel);
                if (isPixelActionToggle()) change |= actionPixelToggle(pPixel);
                if (isPixelActionDeletePixelChain()) change |= mouseClickEventPixelViewPixelChainDelete(pPixel);
                if (isPixelActionChainThickness()) change |= actionPixelChainThickness(pPixel);
                if (isPixelActionCopyToClipboard()) actionCopyToClipboard(pPixel);
                if (isPixelActionChainApproximateCurvesOnly()) change |= actionPixelChainApproximateCurvesOnly(pPixel);
                if (isPixelActionChainDeleteAllButThis()) change |= actionPixelChainDeleteAllButThis(pPixel);
                if (change) {
                    autoUpdateCurves();
                }
            }
            graffitiCursor(pEvent, pPixel);
        });
    }

    private boolean mouseClickEventPixelViewPixelChainDelete(@NonNull final Pixel pPixel) {
        mWorkingPixelsArray.clear();
        addPixelsToWorkingPixelsArray(pPixel, getCursorSize());
        return actionPixelChainDelete(mWorkingPixelsArray);
    }

    private void actionCopyToClipboard(Pixel pPixel) {
        StringBuilder builder = new StringBuilder();
        StrongReference<Bounds> bounds = new StrongReference<>(null);
        getPixelMap().getPixelChains(pPixel).stream().findFirst().ifPresent(pixelChain -> {
            bounds.set(new Bounds());
            pixelChain.streamPixels().forEach(pixel -> bounds.set(bounds.get().getBounds(pixel)));
            builder.append("int xMargin = 2;\n");
            builder.append("int yMargin = 2;\n");
            builder.append("Pixel offset = new Pixel(xMargin, yMargin);\n");

            builder.append("IPixelMapTransformSource ts = new PixelMapTransformSource(");
            builder.append(getPixelMap().getHeight());
            builder.append(", ");
            builder.append(getPixelMap().getLineTolerance());
            builder.append(", ");
            builder.append(getPixelMap().getLineCurvePreference());
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

    private boolean isPixelActionChainThickness() {
        return mPixelAction.getValue() == PixelAction.PixelChainThickness
                || mPixelAction.getValue() == PixelAction.PixelChainThicknessWide
                || mPixelAction.getValue() == PixelAction.PixelChainThicknessVeryWide;
    }

    private boolean isPixelActionChainApproximateCurvesOnly() {
        return mPixelAction.getValue() == PixelAction.ApproximateCurvesOnly;
    }

    private boolean isPixelActionChainDeleteAllButThis() {
        return mPixelAction.getValue() == PixelAction.DeleteAllButThisPixelChain;
    }

    private boolean isPixelActionCopyToClipboard() {
        return mPixelAction.getValue() == PixelAction.CopyToClipboard;
    }

    public boolean actionPixelOn(@NonNull Collection<Pixel> pPixels) {
        if (pPixels.isEmpty()) return false;
        final PixelMap undo = getPixelMap();
        setPixelMap(getPixelMap().actionPixelOn(mWorkingPixelsArray));
        if (getPixelMap() != undo) {
            addUndoRedoEntry("Action Pixel On", undo, getPixelMap());
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelOn(@NonNull final Pixel pPixel) {
        final PixelMap undo = getPixelMap();
        setPixelMap(getPixelMap().actionPixelOn(pPixel));
        if (getPixelMap() != undo) {
            addUndoRedoEntry("Action Pixel On", undo, getPixelMap());
            return true;
        }
        return false;
    }

    @Override
    public void mouseDoubleClickEvent(final ImmutableUIEvent pEvent) {
    }

    @Override
    public void mouseDragEndEvent(final ImmutableUIEvent pEvent) {
        try {
            disableDialogWhile(() -> {
                if (isPixelModeActive()) {
                    mouseDragEndEventPixelView(pEvent);
                } else if (isMoveModeActive()) {
                    updateCurves();
                }
            });
        } finally {
            setMoveModeInActive();
            mViewEnabledLock.unlock();
        }
        Framework.logExit(mLogger);
    }

    private void mouseDragEndEventPixelView(final IUIEvent pEventl) {
        Framework.logValue(mLogger, "mDragPixels.size()", mWorkingPixelsArray.size());
        Framework.logValue(mLogger, "mPixelMap", getPixelMap());

        try {
            if (isPixelActionOn()) {
                actionPixelOn(mWorkingPixelsArray);
            }
            if (isPixelActionDeletePixelChain()) {
                actionPixelChainDelete(mWorkingPixelsArray);
            }
            if (isPixelActionChainThickness()) {
                actionPixelChainThickness(mWorkingPixelsArray);
            }
            mWorkingPixelsArray.clear();
            autoUpdateCurves();
            mMouseDragLastPixel = null;
        } finally {
            getUndoRedoBuffer().endSavepoint(mSavepointId);
            mSavepointId = null;
        }
        Framework.logExit(mLogger);
    }

    @Override
    public void mouseDragEvent(final ImmutableUIEvent pEvent) {
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isMoveModeActive()) {
            pixel.ifPresent(p -> mouseDragEventMoveView(pEvent, p));
        }
        if (isPixelModeActive()) {
            pixel.ifPresent(p -> {
                graffitiCursor(pEvent, p);
                mouseDragEventPixelViewFillIn(pEvent, p);
            });
        }
    }

    /**
     * Fills in the gaps in the drag event so that all the pixels are connected.
     **/
    private void mouseDragEventPixelViewFillIn(final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        mLogger.fine(() -> String.format("mouseDragEventPixelViewFillIn %s, %s", pPixel, mMouseDragLastPixel));
        if (pPixel != null && (isPixelActionOn() || isPixelActionOff() || isPixelActionDeletePixelChain() || isPixelActionChainThickness())) {
            if (mMouseDragLastPixel != null && !mMouseDragLastPixel.equals(pPixel)) {
                mLogger.fine("mouseDragEventPixelViewFillIn ...");
                final int dX = pPixel.getX() - mMouseDragLastPixel.getX();
                final int dY = pPixel.getY() - mMouseDragLastPixel.getY();
                if (Math.abs(dX) >= Math.abs(dY)) { // fill in missing x
                    final int from = Math.min(mMouseDragLastPixel.getX(), pPixel.getX());
                    final int to = Math.max(mMouseDragLastPixel.getX(), pPixel.getX());
                    IntStream.range(from, to).forEach(x -> {
                        final int y = (int) Math.round(mMouseDragLastPixel.getY() + (((double) x - mMouseDragLastPixel.getX()) / dX) * dY);
                        final Optional<Pixel> pixel = getPixelMap().getOptionalPixelAt(x, y);
                        mLogger.fine(() -> String.format("mouseDragEventPixelViewFillIn X  %s, %s", x, y));
                        pixel.ifPresent(p -> mouseDragEventPixelView(UIEvent.createMouseEvent(pEvent, x, y), p));
                    });
                } else { // fill in missing y
                    final int from = Math.min(mMouseDragLastPixel.getY(), pPixel.getY());
                    final int to = Math.max(mMouseDragLastPixel.getY(), pPixel.getY());
                    IntStream.range(from, to).forEach(y -> {
                        final int x = (int) Math.round(mMouseDragLastPixel.getX() + (((double) y - mMouseDragLastPixel.getY()) / dY) * dX);
                        final Optional<Pixel> pixel = getPixelMap().getOptionalPixelAt(x, y);
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
        if (!pEvent.getDeltaX().isPresent() || !pEvent.getDeltaY().isPresent()) {
            mLogger.severe(pEvent.toString());
            throw new RuntimeException("this does not look like dragEvent no getDeltaX or getDeltaY");
        }

        try {
            setMutating(true);
            final int x = mViewOriginX.getValue();
            final int y = mViewOriginY.getValue();
            mViewOriginX.setValue((int) (mMouseDragStartX - pEvent.getNormalizedDeltaX().get() * mCannyEdgeTransform.getWidth() / getZoom()));
            mViewOriginY.setValue((int) (mMouseDragStartY - pEvent.getNormalizedDeltaY().get() * mCannyEdgeTransform.getHeight() / getZoom()));
            if (x != mViewOriginX.getValue() || y != mViewOriginY.getValue()) updateCurves();
        } finally {
            setMutating(false);
        }
    }

    private void mouseDragEventPixelView(final IUIEvent pEvent, @NonNull final Pixel pPixel) {
        boolean change = false;
        if (pPixel != null) {
            if (isPixelActionOn()) mouseDragEventPixelViewOn(pPixel);
            if (isPixelActionOff()) change |= actionPixelOff(pPixel);
            if (isPixelActionToggle()) change |= mouseDragEventPixelViewToggle(pPixel);
            if (isPixelActionDeletePixelChain()) mouseDragEventPixelAddWorkingPixels(pPixel, getCursorSize());
            if (isPixelActionChainThickness()) mouseDragEventPixelAddWorkingPixels(pPixel, getCursorSize());
            if (change) {
                autoUpdateCurves();
            }
        }
    }

    private void addPixelsToWorkingPixelsArray(Pixel pPixel, int pCursorSize) {
        final double radius = (double) pCursorSize / getHeight();
        new Range2D(pPixel.getX() - pCursorSize, pPixel.getX() + pCursorSize,
                pPixel.getY() - pCursorSize, pPixel.getY() + pCursorSize)
                .forEachParallelThread(Services.getServices().getProperties().getRenderThreadPoolSize(), ip ->
                        getPixelMap().getOptionalPixelAt(ip)
                                .filter(Predicate.not(mWorkingPixelsArray::contains))
                                .filter(p -> pPixel.getUHVWMidPoint(getPixelMap())
                                        .distance(p.getUHVWMidPoint(getPixelMap())) < radius)
                                .filter(pPixel1 -> pPixel1.isEdge(getPixelMap()))
                                .map(this::graffitiPixelWorkingColor)
                                .ifPresent(mWorkingPixelsArray::add)
                );
    }

    private void mouseDragEventPixelAddWorkingPixels(Pixel pPixel, int pCursorSize) {
        graffitiPixelWorkingColor(pPixel);
        addPixelsToWorkingPixelsArray(pPixel, pCursorSize);
    }

    private void mouseDragEventPixelViewOn(@NonNull final Pixel pPixel) {
        if (pPixel.isEdge(getPixelMap())) return;
        if (mWorkingPixelsArray.contains(pPixel)) return;
        mWorkingPixelsArray.add(pPixel);
        graffitiPixelWorkingColor(pPixel);
    }

    private void graffitiPixel(@NonNull Pixel pPixel, @NonNull ColorControl pColor) {
        Framework.logEntry(mLogger);
        mPictureControl.updateGrafitti(
                g -> g.drawFilledRectangle(pixelToGrafittiRectangle(pPixel), pColor)
        );
    }

    private Pixel graffitiPixelWorkingColor(@NonNull Pixel pPixel) {
        Framework.logEntry(mLogger);
        mPictureControl.updateGrafitti(
                g -> g.drawFilledRectangle(pixelToGrafittiRectangle(pPixel), mWorkingColor)
        );
        return pPixel;
    }

    synchronized private boolean actionPixelOff(@NonNull final Pixel pPixel) {
        final PixelMap undo = getPixelMap();
        setPixelMap(getPixelMap().actionPixelOff(pPixel, getCursorSize()));
        boolean changesMade = getPixelMap() != undo;
        if (changesMade) {
            addUndoRedoEntry("Action Pixel Off", undo, getPixelMap());
        }
        return changesMade;
    }

    private void addUndoRedoEntry(final String pDescription, final PixelMap pUndo, final PixelMap pRedo) {
        getUndoRedoBuffer().add(pDescription, () -> setPixelMap(pUndo), () -> setPixelMap(pRedo));
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

    private boolean mouseDragEventPixelViewToggle(@NonNull final Pixel pPixel) {
        boolean change = false;
        if (!pPixel.equals(mMouseLastPixelPosition)) {
            change |= actionPixelToggle(pPixel);
            mMouseLastPixelPosition = pPixel;
        }
        return change;
    }

    private boolean actionPixelChainDelete(@NonNull Collection<Pixel> pPixels) {
        final PixelMap undo = getPixelMap();
        setPixelMap(getPixelMap().actionDeletePixelChain(pPixels));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Delete PixelChain", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private boolean actionPixelChainThickness(@NonNull Collection<Pixel> pPixels) {
        final PixelMap undo = getPixelMap();
        setPixelMap(getPixelMap().actionSetPixelChainThickness(pPixels, mThickness.getValue()));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Action PixelChain Thickness", undo, getPixelMap());
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelToggle(@NonNull final Pixel pPixel) {
        final PixelMap undo = getPixelMap();
        graffitiPixelWorkingColor(pPixel);
        setPixelMap(getPixelMap().actionPixelToggle(pPixel));
        addUndoRedoEntry("Action Pixel Toggle", undo, getPixelMap());
        return true;
    }

    synchronized private boolean actionPixelChainDeleteAllButThis(@NonNull final Pixel pPixel) {
        final PixelMap undo = getPixelMap();
        setPixelMap(getPixelMap().actionPixelChainDeleteAllButThis(pPixel));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Delete all but this PixelChain", undo, getPixelMap());
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelChainApproximateCurvesOnly(@NonNull final Pixel pPixel) {
        final PixelMap undo = getPixelMap();
        setPixelMap(getPixelMap().actionPixelChainApproximateCurvesOnly(pPixel));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Approximate Curves Only PixelChain", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private boolean actionPixelChainThickness(@NonNull final Pixel pPixel) {
        final PixelMap undo = getPixelMap();
        if (!getPixelMap().getPixelChains(pPixel).isEmpty()) {
            graffitiPixelWorkingColor(pPixel);
        }
        setPixelMap(getPixelMap().actionSetPixelChainThickness(pPixel, mThickness.getValue()));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Action PixelChain Thickness", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private void disableDialogWhile(IAction pAction) {
        setViewEnabled(false);
        try {
            pAction.performAction();
        } finally {
            setViewEnabled(true);
        }
    }

    @Override
    public void mouseMoveEvent(final ImmutableUIEvent pEvent) {
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        pixel.filter(p -> !p.equals(mMouseLastPixelPosition))
                .ifPresent(p -> {
                    mMouseLastPixelPosition = p;
                    graffitiCursor(pEvent, p);
                });
    }

    private void setViewEnabled(boolean pEnabled) {
        if (mEditPixelMapDialogView != null) {
            mEditPixelMapDialogView.setEnabled(pEnabled);
        }
    }

    @Override
    public void mouseDragStartEvent(final ImmutableUIEvent pEvent) {
        setViewEnabled(false);
        mViewEnabledLock.lock();
        if (pEvent.isShift()) setMoveModeActive();
        if (isMoveModeActive()) mouseDragStartEventMoveView(pEvent);
        if (isPixelModeActive()) mouseDragStartEventPixelView(pEvent);
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
        mWorkingPixelsArray.clear();
    }

    @Override
    public void scrollEvent(final ImmutableUIEvent pEvent) {
        mZoom.setValue(getZoom() - pEvent.getScroll());
    }

    @Override
    public void keyReleased(final ImmutableUIEvent pEvent) {
        mLogger.fine(() -> "keyReleased " + pEvent.getKey());
    }

    @Override
    public void keyTyped(final ImmutableUIEvent pEvent) {
        mLogger.fine(() -> "keyTyped " + pEvent.getKey());
    }

    private boolean isMoveModeActive() {
        return mIsMoveModeActive;
    }

    private void setMoveModeActive() {
        mIsMoveModeActive = true;
    }

    private void setMoveModeInActive() {
        mIsMoveModeActive = false;
    }

    private boolean isPixelModeActive() {
        return !isMoveModeActive();
    }

    private Properties getProperties() {
        return Services.getServices().getProperties();
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
        PixelChainThickness("Thickness", 2),
        PixelChainThicknessWide("Thickness Wide", 15),
        PixelChainThicknessVeryWide("Thickness Very Wide", 45),
        CopyToClipboard("Copy To Clipboard", 1),
        ApproximateCurvesOnly("Approximate Curves Only", 1),
        DeleteAllButThisPixelChain("Delete all but this PixelChain", 1);

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

    private class EPMDUndoRedoBuffer extends UndoRedoBuffer {

        public EPMDUndoRedoBuffer() {
            super(100);
        }

        private boolean update(boolean mB) {
            EditPixelMapDialog.this.updateCurves();
            EditPixelMapDialog.this.drawGraffiti();
            return mB;
        }

        @Override
        public synchronized boolean redo() {
            return update(super.redo());
        }

        @Override
        public synchronized boolean undo() {
            return update(super.undo());
        }
    }
}
