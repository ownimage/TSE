/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.transform.cannyEdge;

import com.google.common.collect.Lists;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.DoubleControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IGrafitti;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.math.Rectangle;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;
import com.ownimage.framework.util.KColor;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.event.ImmutableUIEvent;
import com.ownimage.framework.view.event.UIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Properties;
import com.ownimage.perception.app.Services;
import com.ownimage.perception.pixelMap.IPixelChain.Thickness;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.Segment;
import com.ownimage.perception.pixelMap.immutable.XY;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapActionService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelService;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CropTransform;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator, IGrafitti {
    public final static long serialVersionUID = 1L;
    private final static Logger mLogger = Framework.getLogger();

    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private static PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private static PixelService pixelService = context.getBean(PixelService.class);
    private static PixelMapActionService pixelMapActionService = context.getBean(PixelMapActionService.class);
    private static PixelChainService pixelChainService = context.getBean(PixelChainService.class);

    private final ActionControl mOkAction;
    private final ActionControl mCancelAction;
    private final CannyEdgeTransform mCannyEdgeTransform;
    private final CropTransform mCropTransform;
    private final PictureControl mPictureControl;
    private final IContainer mGeneralContainer;

    private final IntegerControl mPixelMapWidth;
    private final IntegerControl mPixelMapHeight;
    private final IntegerControl mPreviewSize;
    private final DoubleControl mZoom;
    private final ActionControl mZoomIn;
    private final ActionControl mZoomOut;
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
    private final ObjectControl<ThicknessOptions> mThicknessOption;
    private final ObjectControl<Thickness> mThickMapsTo;
    private final ObjectControl<Thickness> mMediumMapsTo;
    private final ObjectControl<Thickness> mThinMapsTo;
    private final ObjectControl<Thickness> mNoneMapsTo;
    private final ColorControl mChainColor;

    private final Collection<XY> mWorkingPixelsArray = new HashSet();

    private ImmutablePixelMap mPixelMap;
    private ImmutablePixelMap mUndoPixelMap;
    private IDialogView mEditPixelMapDialogView;
    private IView mView;
    private ReentrantLock mViewEnabledLock = new ReentrantLock();
    private UndoRedoBuffer mPixelMapUndoRedoBuffer;
    private boolean mMutating = false;
    private boolean mIsMoveModeActive = false;
    private int mMouseDragStartX;
    private int mMouseDragStartY;
    private XY mMouseLastPixelPosition = null;
    private XY mMouseDragLastPixel = null;
    private Id mSavepointId;

    private boolean mAutoUpdateCurvesDirty = false;

    public EditPixelMapDialog(@NonNull CannyEdgeTransform pTransform) {
        super("Edit PixelMap Dialog", "pixelMapEditor", Services.getServices().getUndoRedoBuffer());
        mCannyEdgeTransform = pTransform;
        mPixelMap = pTransform.getPixelMap().orElseThrow();
        mOkAction = ActionControl.create("OK", NullContainer, () -> {
        });
        mCancelAction = ActionControl.create("Cancel", NullContainer, () -> setPixelMap(getUndoPixelMap()));

        mPictureControl = new PictureControl("Preview", "preview", NullContainer, new PictureType(100, 100));
        mGeneralContainer = newContainer("General", "general", true).addBottomPadding();
        mPixelMapWidth = new IntegerControl("PixelMap Width", "pixelMapWidth", mGeneralContainer, getWidth(), 0, 10000, 50).setEnabled(false);
        mPixelMapHeight = new IntegerControl("PixelMap Height", "pixelMapHeight", mGeneralContainer, getHeight(), 0, 10000, 50).setEnabled(false);
        mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, getProperties().getCETEPMDPreviewSize(), getProperties().CETEPMDPreviewSizeModel);
        mZoom = new DoubleControl("Zoom", "zoom", mGeneralContainer, getProperties().getCETEPMDZoom(), getProperties().CETEPMDZoomModel);
        mZoomIn = new ActionControl("Zoom In", "zoomIn", mGeneralContainer, this::zoomIn);
        mZoomOut = new ActionControl("Zoom Out", "zoomOut", mGeneralContainer, this::zoomOut);
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
        mThicknessOption = new ObjectControl("Thickness", "thickness", mGeneralContainer, ThicknessOptions.None, ThicknessOptions.values());
        mThickMapsTo = new ObjectControl("Thick maps to", "thickMapsTo", mGeneralContainer, Thickness.Thick, Thickness.values());
        mMediumMapsTo = new ObjectControl("Medium maps to", "mediumMapsTo", mGeneralContainer, Thickness.Normal, Thickness.values());
        mThinMapsTo = new ObjectControl("Thin maps to", "thinMapsTo", mGeneralContainer, Thickness.Thin, Thickness.values());
        mNoneMapsTo = new ObjectControl("None maps to", "noneMapsTo", mGeneralContainer, Thickness.None, Thickness.values());
        mChainColor = new ColorControl("Chain Color", "chainColor", mGeneralContainer, mCannyEdgeTransform.getLineColor());

        mCropTransform = new CropTransform(Services.getServices().getPerception(), true);
        mPictureControl.setGrafitti(this);
        mPictureControl.setUIListener(this);

        updateControlVisibility();
        updateCurves();

        mEdgeColor.addControlChangeListener(this::mGrafitiChangeListener);
        mNodeColor.addControlChangeListener(this::mGrafitiChangeListener);
        mShowGraffiti.addControlChangeListener(this::mGrafitiChangeListener);
    }

    private void zoomIn() {
        mZoom.setValue(mZoom.getValue() + 1);
    }

    private void zoomOut() {
        mZoom.setValue(mZoom.getValue() - 1);
    }

    public ImmutablePixelMap getPixelMap() {
        return mPixelMap;
    }

    private void setPixelMap(ImmutablePixelMap pPixelMap) {
        mPixelMap = pPixelMap;
    }

    private ImmutablePixelMap getUndoPixelMap() {
        return mUndoPixelMap;
    }

    private void setUndoPixelMap(ImmutablePixelMap pUndoPixelMap) {
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
                PictureType pictureType = new PictureType(getPreviewSize(), getPreviewSize());
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
    public boolean validateControl(Object pControl) {
        val h = mPixelMapHeight.getValue();
        val w = mPixelMapWidth.getValue();

        if (pControl == mViewOriginX) {
            val x = mViewOriginX.getValidateValue();
            val woz = (double) w / mZoom.getValue();
            return (x + woz) <= w;
        }
        if (pControl == mViewOriginY) {
            val y = mViewOriginY.getValidateValue();
            val hoz = (double) h / mZoom.getValue();
            return (y + hoz) <= h;
        }
        if (pControl == mZoom) {
            val x = mViewOriginX.getValue();
            val y = mViewOriginY.getValue();
            val hoz = (double) h / mZoom.getValidateValue();
            val woz = (double) w / mZoom.getValidateValue();
            return (y + hoz) <= h && (x + woz) <= w;
        }
        return true;
    }

    private int getWidth() {
        return getPixelMap().width();
    }

    private int getHeight() {
        return getPixelMap().height();
    }

    private int getPreviewSize() {
        return mPreviewSize.getValue();
    }

    private int getZoomInt() {
        return mZoom.getValue().intValue();
    }

    private Double getZoom() {
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
    public void controlChangeEvent(IControl<?, ?, ?, ?> pControl, boolean pIsMutating) {
        mLogger.fine(() -> "controlChangeEvent for " + pControl.getDisplayName());
        if (isMutating()) {
            return;
        }
        if (pControl != null) {
            if (pControl.isOneOf(mViewOriginX, mViewOriginY, mZoom, mPreviewSize, mShowCurves)) {
                updateCurves();
            }
            if (pControl.isOneOf(mShowCurves, mAutoUpdateCurves, mPixelAction, mShowEdges, mThicknessOption)) {
                updateControlVisibility();
            }
            if (pControl.isOneOf(mAutoUpdateCurves)) {
                if (mAutoUpdateCurvesDirty) {
                    updateCurves();
                }
            }
            if (pControl.isOneOf(mShowGraffiti, mShowEdges, mEdgesOpacity, mNodeColor, mEdgeColor)) {
                drawGraffiti();
            }
        }
    }

    private void updateControlVisibility() {
        mAutoUpdateCurves.setVisible(mShowCurves.getValue());
        mUpdateCurves.setVisible(mShowCurves.getValue() && !mAutoUpdateCurves.getValue());
        mThicknessOption.setVisible(isPixelActionChainThickness());
        mEdgeColor.setVisible(mShowEdges.getValue());
        mNodeColor.setVisible(mShowEdges.getValue());
        mEdgesOpacity.setVisible(mShowEdges.getValue());
        mChainColor.setVisible(isPixelActionChangeColor());

        boolean showThicknessMapper = isPixelActionChainThickness() && mThicknessOption.getValue() == ThicknessOptions.Map;
        mNoneMapsTo.setVisible(showThicknessMapper);
        mThinMapsTo.setVisible(showThicknessMapper);
        mMediumMapsTo.setVisible(showThicknessMapper);
        mThickMapsTo.setVisible(showThicknessMapper);
    }

    @Override
    public IView createView() {
        if (mView == null) {
            HFlowLayout hflow = new HFlowLayout(mPictureControl, mGeneralContainer);
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

    public ImmutablePixelMap showDialog() {
        mPixelMapUndoRedoBuffer = new EPMDUndoRedoBuffer();
        ImmutablePixelMap pixelMap = mCannyEdgeTransform.getPixelMap().orElseThrow();
        pixelMapService.checkCompatibleSize(getPixelMap(), pixelMap);
        setPixelMap(pixelMap);
        setUndoPixelMap(pixelMap);
        setViewEnabled(true);
        updateCurves();
        getDialogView().showModal(mPixelMapUndoRedoBuffer);
        return mPixelMap;
    }

    @Override
    public UndoRedoBuffer getUndoRedoBuffer() {
        return mPixelMapUndoRedoBuffer;
    }

    @Override
    synchronized public void graffiti(GrafittiHelper pGrafittiHelper) {
        Framework.logEntry(mLogger);
        Framework.checkStateNotNull(mLogger, getPixelMap(), "mPixelMap");
        if (mShowEdges.getValue()) {
            int xSize = Math.floorDiv(getWidth(), getZoomInt()) + 1;
            int ySize = Math.floorDiv(getHeight(), getZoomInt()) + 1;
            graffiti(pGrafittiHelper, getViewOriginX(), getViewOriginY(), getViewOriginX() + xSize, getViewOriginY() + ySize);
        }
        if (mShowGraffiti.getValue()) {
            getPixelMap().pixelChains().forEach(pc -> graffitiPixelChain(pGrafittiHelper, pc));
        }
    }

    private void graffiti(GrafittiHelper pGrafittiHelper, int xMin, int yMin, int xMax, int yMax) {
        Framework.logEntry(mLogger);
        Range2D range = new Range2D(xMin, xMax, yMin, yMax);
        range.forEach((x, y) -> {
            if (pixelService.isNode(getPixelMap(), x, y) || pixelService.isEdge(getPixelMap(), x, y)) {
                graffitiPixel(pGrafittiHelper, x, y);
            }
        });
    }

    private void graffitiPixel(GrafittiHelper pGrafittiHelper, int x, int y) {
        Framework.logEntry(mLogger);
        Rectangle r = pixelToGrafittiRectangle(x, y);
        Color c = getPixelColor(x, y);
        pGrafittiHelper.clearRectangle(r);
        pGrafittiHelper.drawFilledRectangle(r, c);
    }

    private Color getPixelColor(int x, int y) {
        val color = pixelService.isNode(getPixelMap(), x, y) ? mNodeColor.getValue() : mEdgeColor.getValue();
        return KColor.alphaMultiply(color, mEdgesOpacity.getValue());
    }

    private Rectangle pixelToGrafittiRectangle(@NotNull XY pixel) {
        return pixelToGrafittiRectangle(pixel.getX(), pixel.getY());
    }

    private Rectangle pixelToGrafittiRectangle(int x, int y) {
        double x1 = pixelXToGrafittiX(x, getViewOriginX(), getWidth(), getZoomInt());
        double x2 = pixelXToGrafittiX(x + 1, getViewOriginX(), getWidth(), getZoomInt());
        double y1 = pixelYToGrafittiY(y, getViewOriginY(), getHeight(), getZoomInt());
        double y2 = pixelYToGrafittiY(y + 1, getViewOriginY(), getHeight(), getZoomInt());
        return new Rectangle(x1, y1, x2, y2);
    }

    private double pixelXToGrafittiX(int pX, int pXMin, int pPMWidth, int pZoom) {
        return (double) (pX - pXMin) * pZoom / pPMWidth;
    }

    private double pixelYToGrafittiY(int pY, int pYMin, int pPMHeight, int pZoom) {
        return (double) (pY - pYMin) * pZoom / pPMHeight;
    }

    private void graffitiPixelChain(GrafittiHelper pGrafittiHelper, PixelChain pPixelChain) {
        Framework.logEntry(mLogger);
        pPixelChain.streamSegments().forEach(s -> graffitiSegment(pGrafittiHelper, pPixelChain, s));
    }

    private void graffitiSegment(
            GrafittiHelper pGrafittiHelper,
            PixelChain pPixelChain,
            Segment pSegment
    ) {
        Framework.logEntry(mLogger);
        SegmentGraffitiHelper segmentGrafittiHelper = new SegmentGraffitiHelper(pGrafittiHelper, this::UHVWtoView);
        pSegment.graffiti(getPixelMap(), pPixelChain, segmentGrafittiHelper);
    }

    private Point UHVWtoView(Point pUHVW) {
        double x = (pUHVW.getX() * getHeight() - getViewOriginX()) * getZoomInt() / getWidth();
        double y = (pUHVW.getY() * getHeight() - getViewOriginY()) * getZoomInt() / getHeight();
        return new Point(x, y);
    }

    private Optional<Pixel> eventToPixel(IUIEvent pEvent) {
        return eventXYToPixel(pEvent.getX(), pEvent.getY());
    }

    private Optional<Pixel> eventLastClickedToPixel(IUIEvent pEvent) {
        if (pEvent.getLastXClick().isPresent() && pEvent.getLastYClick().isPresent()) {
            return eventXYToPixel(pEvent.getLastXClick().get(), pEvent.getLastYClick().get());
        }
        return Optional.empty();
    }

    private Optional<Pixel> eventXYToPixel(int pX, int pY) {
        int x = getViewOriginX() + (pX * getWidth() / (getZoomInt() * getPreviewSize()));
        int y = getViewOriginY() + (pY * getHeight() / (getZoomInt() * getPreviewSize()));
        return pixelMapService.getOptionalPixelAt(getPixelMap(), x, y);
    }

    private void setCrop() {
        if (getPixelMap() != null) {
            double left = (double) getViewOriginX() / getWidth();
            double right = left + 1.0d / getZoomInt();
            double bottom = (double) getViewOriginY() / getHeight();
            double top = bottom + 1.0d / getZoomInt();
            mCropTransform.setCrop(left, bottom, right, top);
            if (mShowCurves.getValue() && !mMutating) {
                mCropTransform.setPreviousTransform(mCannyEdgeTransform);
            } else {
                mCropTransform.setPreviousTransform(mCannyEdgeTransform.getPreviousTransform());
            }
        }
    }

    @Override
    public void mouseClickEvent(ImmutableUIEvent pEvent) {
        Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isPixelModeActive()) {
            pixel.ifPresent(p -> mouseClickEventPixelView(pEvent, p));
        }
    }

    private void autoUpdateCurves() {
        if (mAutoUpdateCurves.getValue()) {
            updateCurves();
        } else {
            mAutoUpdateCurvesDirty = true;
            drawGraffiti();
        }
    }

    private void mouseClickEventPixelView(@NonNull IUIEvent pEvent, @NonNull Pixel pPixel) {
        disableDialogWhile(() -> {
            boolean change = false;
            if (pPixel != null) {
                if (isPixelActionOn()) {
                    change |= mouseClickPixelOn(pEvent, pPixel);
                }
                if (isPixelActionOff()) {
                    change |= actionPixelOff(pPixel);
                }
                if (isPixelActionToggle()) {
                    change |= actionPixelToggle(pPixel);
                }
                if (isPixelActionDeletePixelChain()) {
                    change |= mouseClickEventPixelViewPixelChainDelete(pPixel);
                }
                if (isPixelActionChainThickness()) {
                    change |= mouseClickEventPixelViewPixelChainThickness(pPixel);
                }
                if (isPixelActionVertex()) {
                    change |= mouseClickEventPixelViewVertex(pPixel, !pEvent.isShift());
                }
                if (isPixelActionCopyToClipboard()) {
                    actionCopyToClipboard(pPixel);
                }
                if (isPixelActionChainApproximateCurvesOnly()) {
                    change |= actionPixelChainApproximateCurvesOnly(pPixel);
                }
                if (isPixelActionChainDeleteAllButThis()) {
                    change |= actionPixelChainDeleteAllButThis(pPixel);
                }
                if (change) {
                    autoUpdateCurves();
                }
            }
            graffitiCursor(pEvent, pPixel);
        });
    }

    private boolean mouseClickPixelOn(@NonNull IUIEvent pEvent, @NonNull XY pPixel) {
        if (!pEvent.isShift()) {
            return actionPixelOn(pPixel);
        }
        return eventLastClickedToPixel(pEvent)
                .map(lastClickedPixel -> {
                    Collection<XY> pixels = new HashSet<>();
                    fillIn(pEvent, pPixel, lastClickedPixel, (iui, p) -> pixels.add(p));
                    return actionPixelOn(pixels);
                })
                .orElseGet(() -> actionPixelOn(pPixel));
    }

    private boolean mouseClickEventPixelViewPixelChainDelete(@NonNull Pixel pPixel) {
        mWorkingPixelsArray.clear();
        addPixelsToWorkingPixelsArray(pPixel, getCursorSize());
        return actionPixelChainDelete(mWorkingPixelsArray);
    }

    private boolean mouseClickEventPixelViewPixelChainThickness(@NonNull Pixel pPixel) {
        mWorkingPixelsArray.clear();
        addPixelsToWorkingPixelsArray(pPixel, getCursorSize());
        return actionPixelChainThickness(mWorkingPixelsArray);
    }

    private boolean mouseClickEventPixelViewVertex(@NonNull Pixel pixel, boolean add) {
        return actionPixelChainVertex(pixel, add);
    }

    private void actionCopyToClipboard(Pixel pPixel) {
//        StringBuilder builder = new StringBuilder();
//        StrongReference<Bounds> bounds = new StrongReference<>(null);
//        pixelMapService.getPixelChains(getPixelMap(), pPixel).stream().findFirst().ifPresent(pixelChain -> {
//            bounds.set(new Bounds());
//            pixelChain.streamPixels().forEach(pixel -> bounds.set(bounds.get().getBounds(pixel)));
//            builder.append("int xMargin = 2;\n");
//            builder.append("int yMargin = 2;\n");
//            builder.append("Pixel offset = new Pixel(xMargin, yMargin);\n");
//
//            builder.append("IPixelMapTransformSource ts = new PixelMapTransformSource(");
//            builder.append(getPixelMap().height());
//            builder.append(", ");
//            builder.append(mCannyEdgeTransform.getLineTolerance());
//            builder.append(", ");
//            builder.append(mCannyEdgeTransform.getLineCurvePreference());
//            builder.append(");\n");
//
//            builder.append("PixelMap pixelMap = new PixelMap(");
//            builder.append(bounds.get().getWidth() + " + 2 * xMargin");
//            builder.append(", ");
//            builder.append(bounds.get().getHeight() + " + 2 * yMargin");
//            builder.append(", false, ts);\n");
//
//            builder.append("pixelMap.actionProcess(null);\n");
//
//            pixelChain.streamPixels().forEach(pixel -> {
//                builder.append("pixelMap = pixelMap.actionPixelOn(new Pixel(");
//                builder.append(pixel.minus(bounds.get().getLowerLeft()).getX());
//                builder.append(", ");
//                builder.append(pixel.minus(bounds.get().getLowerLeft()).getY());
//                builder.append(").add(offset));\n");
//            });
//        });
//        builder.append("\n\nassertEquals(1, pixelMap.getPixelChainCount());\n");
//        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(builder.toString()), null);
//        mLogger.info(() -> "CLIPBOARD: " + builder);
    }

    private boolean isPixelActionOn() {
        return mPixelAction.getValue() == PixelAction.On;
    }

    private boolean isPixelActionOff() {
        EnumSet<PixelAction> set = EnumSet.of(PixelAction.Off, PixelAction.OffWide, PixelAction.OffVeryWide, PixelAction.OffVeryVeryWide);
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

    private boolean isPixelActionChangeColor() {
        return mPixelAction.getValue() == PixelAction.ChangeColor;
    }

    private boolean isPixelActionChainApproximateCurvesOnly() {
        return mPixelAction.getValue() == PixelAction.ApproximateCurvesOnly;
    }

    private boolean isPixelActionVertex() {
        return mPixelAction.getValue() == PixelAction.Vertex;
    }

    private boolean isPixelActionChainDeleteAllButThis() {
        return mPixelAction.getValue() == PixelAction.DeleteAllButThisPixelChain;
    }

    private boolean isPixelActionCopyToClipboard() {
        return mPixelAction.getValue() == PixelAction.CopyToClipboard;
    }

    public boolean actionPixelOn(@NonNull Collection<XY> pPixels) {
        if (pPixels.isEmpty()) {
            return false;
        }
        ImmutablePixelMap undo = getPixelMap();
        // TODO shouldnt really set when not needed
        setPixelMap(pixelMapActionService.actionPixelOn(getPixelMap(), pPixels, getLineTolerance(), getLineCurvePreference()));
        if (getPixelMap() != undo) {
            addUndoRedoEntry("Action Pixel On", undo, getPixelMap());
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelOn(@NotNull XY pPixel) {
        ImmutablePixelMap undo = getPixelMap();
        // TODO shouldnt really set when not needed
        setPixelMap(pixelMapActionService.actionPixelOn(getPixelMap(), Lists.newArrayList(pPixel), getLineTolerance(), getLineCurvePreference()));
        if (getPixelMap() != undo) {
            addUndoRedoEntry("Action Pixel On", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private double getLineTolerance() {
        return mCannyEdgeTransform.getLineTolerance() / mCannyEdgeTransform.getHeight();
    }

    private double getLineCurvePreference() {
        return mCannyEdgeTransform.getLineCurvePreference();
    }

    @Override
    public void mouseDoubleClickEvent(ImmutableUIEvent pEvent) {
    }

    @Override
    public void mouseDragEndEvent(ImmutableUIEvent pEvent) {
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

    private void mouseDragEndEventPixelView(IUIEvent pEvent) {
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
            if (isPixelActionChangeColor()) {
                actionPixelChainChangeColor(mWorkingPixelsArray);
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
    public void mouseDragEvent(ImmutableUIEvent pEvent) {
        Optional<Pixel> pixel = eventToPixel(pEvent);
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

    private void mouseDragEventPixelViewFillIn(IUIEvent pEvent, @NonNull XY pPixel) {
        fillIn(pEvent, pPixel, mMouseDragLastPixel, this::mouseDragEventPixelView);
        mMouseDragLastPixel = pPixel;
    }

    /**
     * Fills in the gaps in the drag event so that all the pixels are connected.
     **/
    private void fillIn(
            @NonNull IUIEvent pEvent,
            @NonNull XY pPixel,
            @NonNull XY pLastPixel,
            @NonNull BiConsumer<IUIEvent, XY> pFn
    ) {
        mLogger.fine(() -> String.format("pFn.accept %s, %s", pPixel, pLastPixel));
        if (pPixel != null && (isPixelActionOn() || isPixelActionOff() || isPixelActionDeletePixelChain() || isPixelActionChainThickness())) {
            if (pLastPixel != null && !pLastPixel.equals(pPixel)) {
                mLogger.fine("pFn.accept ...");
                int dX = pPixel.getX() - pLastPixel.getX();
                int dY = pPixel.getY() - pLastPixel.getY();
                if (Math.abs(dX) >= Math.abs(dY)) { // fill in missing x
                    int from = Math.min(pLastPixel.getX(), pPixel.getX());
                    int to = Math.max(pLastPixel.getX(), pPixel.getX());
                    IntStream.range(from, to).forEach(x -> {
                        int y = (int) Math.round(pLastPixel.getY() + (((double) x - pLastPixel.getX()) / dX) * dY);
                        Optional<Pixel> pixel = pixelMapService.getOptionalPixelAt(getPixelMap(), x, y);
                        mLogger.fine(() -> String.format("pFn.accept X  %s, %s", x, y));
                        pixel.ifPresent(p -> pFn.accept(UIEvent.createMouseEvent(pEvent, x, y), p));
                    });
                } else { // fill in missing y
                    int from = Math.min(pLastPixel.getY(), pPixel.getY());
                    int to = Math.max(pLastPixel.getY(), pPixel.getY());
                    IntStream.range(from, to).forEach(y -> {
                        int x = (int) Math.round(pLastPixel.getX() + (((double) y - pLastPixel.getY()) / dY) * dX);
                        Optional<Pixel> pixel = pixelMapService.getOptionalPixelAt(getPixelMap(), x, y);
                        mLogger.fine(() -> String.format("pFn.accept Y  %s, %s", x, y));
                        pixel.ifPresent(p -> pFn.accept(UIEvent.createMouseEvent(pEvent, x, y), p));
                    });
                }
            }
        }
        pFn.accept(pEvent, pPixel);
    }

    private void mouseDragEventMoveView(IUIEvent pEvent, @NonNull Pixel pPixel) {
        if (pEvent.getDeltaX().isEmpty() || pEvent.getDeltaY().isEmpty()) {
            mLogger.severe(pEvent.toString());
            throw new RuntimeException("this does not look like dragEvent no getDeltaX or getDeltaY");
        }

        try {
            setMutating(true);
            int x = mViewOriginX.getValue();
            int y = mViewOriginY.getValue();
            mViewOriginX.setValue((int) (mMouseDragStartX - pEvent.getNormalizedDeltaX().orElseThrow() * mCannyEdgeTransform.getWidth() / getZoomInt()));
            mViewOriginY.setValue((int) (mMouseDragStartY - pEvent.getNormalizedDeltaY().orElseThrow() * mCannyEdgeTransform.getHeight() / getZoomInt()));
            if (x != mViewOriginX.getValue() || y != mViewOriginY.getValue()) {
                updateCurves();
            }
        } finally {
            setMutating(false);
        }
    }

    private void mouseDragEventPixelView(IUIEvent pEvent, @NonNull XY pPixel) {
        boolean change = false;
        if (pPixel != null) {
            if (isPixelActionOn()) {
                mouseDragEventPixelViewOn(pPixel);
            }
            if (isPixelActionOff()) {
                change |= actionPixelOff(pPixel);
            }
            if (isPixelActionToggle()) {
                change |= mouseDragEventPixelViewToggle(pPixel);
            }
            if (isPixelActionDeletePixelChain()) {
                mouseDragEventPixelAddWorkingPixels(pPixel, getCursorSize());
            }
            if (isPixelActionChainThickness()) {
                mouseDragEventPixelAddWorkingPixels(pPixel, getCursorSize());
            }
            if (isPixelActionChangeColor()) {
                mouseDragEventPixelAddWorkingPixels(pPixel, getCursorSize());
            }
            if (change) {
                autoUpdateCurves();
            }
        }
    }

    private void addPixelsToWorkingPixelsArray(XY pPixel, int pCursorSize) {
        double radius = (double) pCursorSize / getHeight();
        new Range2D(pPixel.getX() - pCursorSize, pPixel.getX() + pCursorSize,
                pPixel.getY() - pCursorSize, pPixel.getY() + pCursorSize)
                .forEachParallelThread(Services.getServices().getProperties().getRenderThreadPoolSize(), ip ->
                        pixelMapService.getOptionalPixelAt(getPixelMap(), XY.of(ip))
                                .filter(Predicate.not(mWorkingPixelsArray::contains))
                                .filter(p -> pPixel.getUHVWMidPoint(mPixelMap.height())
                                        .distance(p.getUHVWMidPoint(mPixelMap.height())) < radius)
                                .filter(pPixel1 -> pixelService.isEdge(getPixelMap(), pPixel1))
                                .map(this::graffitiPixelWorkingColor)
                                .ifPresent(mWorkingPixelsArray::add)
                );
    }

    private void mouseDragEventPixelAddWorkingPixels(XY pPixel, int pCursorSize) {
        graffitiPixelWorkingColor(pPixel);
        addPixelsToWorkingPixelsArray(pPixel, pCursorSize);
    }

    private void mouseDragEventPixelViewOn(@NonNull XY pPixel) {
//        if (pPixel.isEdge(getPixelMap())) {
        if (pixelService.isEdge(getPixelMap(), pPixel)) {
            return;
        }
        if (mWorkingPixelsArray.contains(pPixel)) {
            return;
        }
        mWorkingPixelsArray.add(pPixel);
        graffitiPixelWorkingColor(pPixel);
    }

    private void graffitiPixel(@NonNull Pixel pPixel, @NonNull ColorControl pColor) {
        Framework.logEntry(mLogger);
        mPictureControl.updateGrafitti(
                g -> g.drawFilledRectangle(pixelToGrafittiRectangle(pPixel), pColor)
        );
    }

    private XY graffitiPixelWorkingColor(@NonNull XY pPixel) {
        Framework.logEntry(mLogger);
        mPictureControl.updateGrafitti(
                g -> g.drawFilledRectangle(pixelToGrafittiRectangle(pPixel), mWorkingColor)
        );
        return pPixel;
    }

    synchronized private boolean actionPixelOff(@NonNull XY pPixel) {
        ImmutablePixelMap undo = getPixelMap();
        setPixelMap(pixelMapActionService.actionPixelOff(getPixelMap(), pPixel, getCursorSize(), getLineTolerance(), getLineCurvePreference()));
        boolean changesMade = getPixelMap() != undo;
        if (changesMade) {
            addUndoRedoEntry("Action Pixel Off", undo, getPixelMap());
        }
        return changesMade;
    }

    private void addUndoRedoEntry(String pDescription, ImmutablePixelMap pUndo, ImmutablePixelMap pRedo) {
        getUndoRedoBuffer().add(pDescription, () -> setPixelMap(pUndo), () -> setPixelMap(pRedo));
    }

    private void graffitiCursor(IUIEvent pEvent, @NonNull Pixel pPixel) {
        Framework.logEntry(mLogger);
        if (pPixel != null) {
            IGrafitti g = graffitiHelper ->
                    graffitiHelper.drawCircle(pEvent.getNormalizedX(), pEvent.getNormalizedY(), getRadius(), Color.red, false);
            mPictureControl.drawCursor(g);
            mMouseLastPixelPosition = pPixel;
        }
    }

    private double getRadius() {
        return (double) getCursorSize() * getZoomInt() / mPixelMapHeight.getValue();
    }

    private int getCursorSize() {
        return mPixelAction.getValue().getCursorSize();
    }

    private void drawGraffiti() {
        Framework.logEntry(mLogger);
        mPictureControl.drawGrafitti();
    }

    private boolean mouseDragEventPixelViewToggle(@NonNull XY pPixel) {
        boolean change = false;
        if (!pPixel.equals(mMouseLastPixelPosition)) {
            change |= actionPixelToggle(pPixel);
            mMouseLastPixelPosition = pPixel;
        }
        return change;
    }

    private boolean actionPixelChainDelete(@NonNull Collection<XY> pPixels) {
        if (pPixels.isEmpty()) {
            return false;
        }
        ImmutablePixelMap undo = getPixelMap();
        setPixelMap(pixelMapActionService.actionDeletePixelChain(getPixelMap(), pPixels, getLineTolerance(), getLineCurvePreference()));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Delete PixelChain", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private boolean actionPixelChainVertex(@NonNull Pixel pixel, boolean add) {
        ImmutablePixelMap undo = getPixelMap();
        if (add) {
            setPixelMap(pixelMapActionService.actionVertexAdd(getPixelMap(), pixel, getLineCurvePreference()));
        } else {
            setPixelMap(pixelMapActionService.actionVertexRemove(getPixelMap(), pixel, getLineCurvePreference()));
        }
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Vertex action", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private boolean actionPixelChainChangeColor(@NonNull Collection<XY> pPixels) {
        ImmutablePixelMap undo = getPixelMap();
        setPixelMap(pixelMapActionService.actionSetPixelChainChangeColor(getPixelMap(), pPixels, mChainColor.getValue()));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Action PixelChain Thickness", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private boolean actionPixelChainThickness(@NonNull Collection<XY> pPixels) {
        if (pPixels.isEmpty()) {
            return false;
        }
        Function<PixelChain, Thickness> mapper = pc -> pc.thickness();
        switch (mThicknessOption.getValue()) {
            case None:
            case Thin:
            case Medium:
            case Thick:
                mapper = pc -> mThicknessOption.getValue().getPixelChainThickness();
                break;
            case Reset:
                var shortLength = mCannyEdgeTransform.getShortLineLength();
                var mediumLength = mCannyEdgeTransform.getMediumLineLength();
                var longLength = mCannyEdgeTransform.getLongLineLength();
                mapper = pc -> pixelChainService.getThickness(pc, shortLength, mediumLength, longLength);
                break;
            case Map:
                var map = new EnumMap<Thickness, Thickness>(Thickness.class);
                map.put(Thickness.Thick, mThickMapsTo.getValue());
                map.put(Thickness.Normal, mMediumMapsTo.getValue());
                map.put(Thickness.Thin, mThinMapsTo.getValue());
                map.put(Thickness.None, mNoneMapsTo.getValue());
                mapper = pc -> map.get(pc.thickness());
                break;
        }

        ImmutablePixelMap undo = getPixelMap();
        setPixelMap(pixelMapActionService.actionSetPixelChainThickness(getPixelMap(), pPixels, mapper));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Action PixelChain Thickness", undo, getPixelMap());
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelToggle(@NonNull XY pPixel) {
        ImmutablePixelMap undo = getPixelMap();
        graffitiPixelWorkingColor(pPixel);
        setPixelMap(pixelMapActionService.actionPixelToggle(getPixelMap(), pPixel, getLineTolerance(), getLineCurvePreference()));
        addUndoRedoEntry("Action Pixel Toggle", undo, getPixelMap());
        return true;
    }

    synchronized private boolean actionPixelChainDeleteAllButThis(@NonNull Pixel pPixel) {
        ImmutablePixelMap undo = getPixelMap();
        setPixelMap(pixelMapActionService.actionPixelChainDeleteAllButThis(getPixelMap(), pPixel));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Delete all but this PixelChain", undo, getPixelMap());
            return true;
        }
        return false;
    }

    synchronized private boolean actionPixelChainApproximateCurvesOnly(@NonNull Pixel pPixel) {
        ImmutablePixelMap undo = getPixelMap();
//        setPixelMap(pixelMapActionService.actionPixelChainApproximateCurvesOnly(getPixelMap(), mCannyEdgeTransform, pPixel));
        setPixelMap(pixelMapActionService.actionRerefine(getPixelMap(), mCannyEdgeTransform));
        if (undo != getPixelMap()) {
            addUndoRedoEntry("Approximate Curves Only PixelChain", undo, getPixelMap());
            return true;
        }
        return false;
    }

    private void disableDialogWhile(IAction pAction) {
        setViewEnabled(false);
        try {
            pAction.performAction();
        } catch (Exception ex) {
            Framework.logThrowable(mLogger, Level.SEVERE, ex);
        } finally {
            setViewEnabled(true);
        }
    }

    @Override
    public void mouseMoveEvent(ImmutableUIEvent pEvent) {
        Optional<Pixel> pixel = eventToPixel(pEvent);
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
    public void mouseDragStartEvent(ImmutableUIEvent pEvent) {
        setViewEnabled(false);
        mViewEnabledLock.lock();
        if (pEvent.isShift()) {
            setMoveModeActive();
        }
        if (isMoveModeActive()) {
            mouseDragStartEventMoveView(pEvent);
        }
        if (isPixelModeActive()) {
            mouseDragStartEventPixelView(pEvent);
        }
    }

    private void mouseDragStartEventMoveView(IUIEvent pEvent) {
        mMouseDragStartX = getViewOriginX();
        mMouseDragStartY = getViewOriginY();
    }

    private void mouseDragStartEventPixelView(IUIEvent pEvent) {
        mSavepointId = getUndoRedoBuffer().startSavepoint("MouseDrag");
        eventToPixel(pEvent).ifPresent(p -> {
            graffitiCursor(pEvent, p);
            mMouseDragLastPixel = p;
        });
        mWorkingPixelsArray.clear();
    }

    @Override
    public void scrollEvent(ImmutableUIEvent pEvent) {
        mZoom.setValue(getZoom() - pEvent.getScroll());
    }

    @Override
    public void keyReleased(ImmutableUIEvent pEvent) {
        mLogger.fine(() -> "keyReleased " + pEvent.getKey());
    }

    @Override
    public void keyTyped(ImmutableUIEvent pEvent) {
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

    private enum ThicknessOptions {
        Thick(Thickness.Thick),
        Medium(Thickness.Normal),
        Thin(Thickness.Thin),
        None(Thickness.None),
        Reset(null),
        Map(null);

        private Thickness mPixelChainThickness;

        ThicknessOptions(Thickness pPixelChainThickness) {
            this.mPixelChainThickness = pPixelChainThickness;
        }

        public Thickness getPixelChainThickness() {
            return mPixelChainThickness;
        }

        public boolean isNormal() {
            return mPixelChainThickness != null;
        }
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
        Vertex("Change Vertex", 1),
        ChangeColor("Change Color", 1),
        CopyToClipboard("Copy To Clipboard", 1),
        ApproximateCurvesOnly("Approximate Curves Only", 1),
        DeleteAllButThisPixelChain("Delete all but this PixelChain", 1);

        private final String mName;
        private final int mCursorSize;

        PixelAction(String pName, int pCursorSize) {
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
