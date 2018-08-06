package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
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
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.ContainerList;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Id;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IDialogView;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.event.UIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Rectangle;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.CropTransform;
import com.ownimage.perception.transform.ITransform;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator, IGrafitti {

    public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private enum PixelAction {
        On("On"),
        Off("Off"),
        OffWide("Off Wide"),
        OffVeryWide("Off Very Wide"),
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

    private final ITransform mTransform;
    private final CropTransform mCropTransform;

    PictureControl mPictureControl = new PictureControl("Test Integer Control", "gausianKernelWidth", NullContainer.NullContainer,
                                                        new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(), 100, 100));

    private final ContainerList mContainerList = new ContainerList("Edit PixelMap", "editPixelMap");
    private final IContainer mGeneralContainer = mContainerList.add(newContainer("General", "general", true));
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
    private final ColorControl mEdgeColor = new ColorControl("Edge Color", "edgeColor", mPixelControlContainer, Color.GREEN);
    private final ColorControl mNodeColor = new ColorControl("Node Color", "nodeColor", mPixelControlContainer, Color.RED);
    private final BooleanControl mShowGrafitti = new BooleanControl("Show Grafitti", "showGrafitti", mPixelControlContainer, true);
    private final ObjectControl<PixelAction> mPixelAction =
            new ObjectControl("Pixel Action", "pixelAction", mPixelControlContainer, PixelAction.On, PixelAction.values());

    // Vertex Container
    private final DoubleControl mVCC1 = new DoubleControl("Vertex test 1", "vertexTest1", mVertexControlContainer, 0.5);
    private final IntegerControl mVCC2 = new IntegerControl("Vertex test 2", "vertexTest2", mVertexControlContainer, 2, 2, 15, 1);
    private final IntegerControl mVCC3 = new IntegerControl("Vertex test 3", "vertexTest3", mVertexControlContainer, 2, 2, 15, 1);

    private boolean mDialogIsAlive = false;

    private int mMouseDragStartX;
    private int mMouseDragStartY;
    private Optional<Pixel> mMouseLastPixelPosition = Optional.empty();
    private Optional<Pixel> mMouseDragLastPixel = Optional.empty();
    private int mMouseLastSize;
    private Id mSavepointId;

    public EditPixelMapDialog(final ITransform pTransform, final PixelMap pPixelMap, final String pDisplayName, final String pPropertyName, final ActionControl pOkAction, final ActionControl pCancelAction) {
        super(pDisplayName, pPropertyName, Perception.getPerception().getUndoRedoBuffer()); // TODO need to remove the @Deprecated call
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");
        Framework.checkParameterNotNull(mLogger, pPixelMap, "pPixelMap");

        mTransform = pTransform;
        mPixelMap = pPixelMap;
        mOkAction = pOkAction;
        mCancelAction = pCancelAction;

        mPixelMapWidth = new IntegerControl("PixelMap Height", "pixelMapWidth", mGeneralContainer, getWidth(), 0, getWidth(), 50).setEnabled(false);
        mPixelMapHeight = new IntegerControl("PixelMap Height", "pixelMapHeight", mGeneralContainer, getHeight(), 0, getHeight(), 50).setEnabled(false);
        mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, 600, 100, 1000, 50);
        mZoom = new IntegerControl("Zoom", "zoom", mGeneralContainer, 2, 1, 16, 2);
        mViewOriginX = new IntegerControl("View X", "x", mGeneralContainer, 0, 0, getWidth(), 50);
        mViewOriginY = new IntegerControl("View Y", "y", mGeneralContainer, 0, 0, getHeight(), 50);
        mCropTransform = new CropTransform(Perception.getPerception(), true);
        mPictureControl.setGrafitti(this);
        mPictureControl.setUIListener(this);
        updatePreview();
    }

    private void updatePreview() {
        if (getPreviewSize() != mPictureControl.getWidth()) {
            final PictureType pictureType = new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(), getPreviewSize(), getPreviewSize());
            mPictureControl.setValue(pictureType);
        }
        setCrop();
        Perception.getPerception().getRenderService().transform(mPictureControl, mCropTransform);
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

    @Override
    public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
        System.out.println("controlChanteEvent for " + pControl.getDisplayName());
        updatePreview();
    }

    @Override
    public IView createView() {
        HFlowLayout hflow = new HFlowLayout(mPictureControl, mContainerList);
        IView view = ViewFactory.getInstance().createView(hflow);
        addView(view);
        return view;
    }

    private IDialogView getDialogView() {
        if (mEditPixelMapDialogView == null) {
            mEditPixelMapDialogView = ViewFactory.getInstance().createDialog(this,
                                                                             DialogOptions.builder().withCompleteFunction(this::closeDialog).build(),
                                                                             getUndoRedoBuffer(),
                                                                             mCancelAction, mOkAction);
        }
        return mEditPixelMapDialogView;
    }

    public void showDialog() {
        mDialogIsAlive = true;
        mCropTransform.setPreviousTransform(mTransform.getPreviousTransform());
        updatePreview();
        getDialogView().showModal();
    }

    private void closeDialog(Optional<ActionControl> pActionControl) {
        mDialogIsAlive = false;
    }

    public UndoRedoBuffer getUndoRedoBuffer() {
        return Perception.getPerception().getUndoRedoBuffer();
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
        Color c = pPixel.isNode() ? mNodeColor.getValue() : mEdgeColor.getValue();
        return c;
    }

    private Rectangle pixelToGrafittiRectangle(Pixel pPixel) {
        int x = pPixel.getX();
        int y = pPixel.getY();
        double x1 = pixelXToGrafittiX(x, getViewOriginX(), getWidth(), getZoom());
        double x2 = pixelXToGrafittiX(x + 1, getViewOriginX(), getWidth(), getZoom());
        double y1 = pixelYToGrafittiY(y, getViewOriginY(), getHeight(), getZoom());
        double y2 = pixelYToGrafittiY(y + 1, getViewOriginY(), getHeight(), getZoom());
        Rectangle r = new Rectangle(x1, y1, x2, y2);
        return r;
    }

    private double pixelXToGrafittiX(int pX, int pXMin, int pPMWidth, int pZoom) {
        double x = (double) (pX - pXMin) * pZoom / pPMWidth;
        return x;
    }

    private double pixelYToGrafittiY(int pY, int pYMin, int pPMHeight, int pZoom) {
        double y = (double) (pY - pYMin) * pZoom / pPMHeight;
        return y;
    }

    private void grafittiPixelChain(GrafittiHelper pGrafittiHelper, PixelChain pPixelChain) {
        pPixelChain.getAllSegments().forEach(s -> grafittiSegment(pGrafittiHelper, s));
    }

    private void grafittiSegment(GrafittiHelper pGrafittiHelper, ISegment pSegment) {
        SegmentGrafittiHelper segmentGrafittiHelper = new SegmentGrafittiHelper(pGrafittiHelper, this::UHVWtoView);
        pSegment.graffiti(segmentGrafittiHelper);
    }

    private Point UHVWtoView(Point pUHVW) {
        double x = (double) (getWidth() * pUHVW.getX() - getViewOriginX()) * getZoom() / getWidth();
        double y = (double) (getHeight() * pUHVW.getY() - getViewOriginY()) * getZoom() / getHeight();
        return new Point(x, y);
    }

    private Optional<Pixel> eventToPixel(IUIEvent pEvent) {
        return eventXYToPixel(pEvent.getX(), pEvent.getY());
    }

    private Optional<Pixel> eventXYToPixel(int pX, int pY) {
        int x = getViewOriginX() + (pX * getWidth() / (getZoom() * getPreviewSize()));
        int y = getViewOriginY() + (pY * getHeight() / (getZoom() * getPreviewSize()));
        System.out.format("pEvent = %d, %d, x = %d, y = %d\n", pX, pY, x, y);
        return mPixelMap.getOptionalPixelAt(x, y);
    }

    private void setCrop() {
        if (mPixelMap != null) {
            double left = (double) getViewOriginX() / getWidth();
            double right = left + 1.0d / getZoom();
            double bottom = (double) getViewOriginY() / getHeight();
            double top = bottom + 1.0d / getZoom();
            System.out.print("lrbt " + left + " " + right + " " + bottom + " " + top);
            mCropTransform.setCrop(left, bottom, right, top);
        }
    }

    @Override
    public void mouseClickEvent(final IUIEvent pEvent) {
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isPixelView()) mouseClickEventPixelView(pEvent, pixel);
    }

    private void mouseClickEventPixelView(final IUIEvent pEvent, final Optional<Pixel> pPixel) {
        pPixel.ifPresent(pixel -> {
            if (isPixelActionOn()) actionPixelOn(pixel);
            if (isPixelActionOff()) actionPixelOff(pixel);
            if (isPixelActionOffWide()) actionPixelOff(pixel);
            if (isPixelActionOffVeryWide()) actionPixelOff(pixel);
            if (isPixelActionToggle()) actionPixelToggle(pixel);
            if (isPixelActionDeletePixelChain()) actionPixelChainDelete(pixel);
        });
        grafittiCursor(pEvent, pPixel);
    }

    private boolean isPixelActionOn() {
        return mPixelAction.getValue() == PixelAction.On;
    }

    private boolean isPixelActionOff() {
        return mPixelAction.getValue() == PixelAction.Off;
    }

    private boolean isPixelActionOffWide() {
        return mPixelAction.getValue() == PixelAction.OffWide;
    }

    private boolean isPixelActionOffVeryWide() {
        return mPixelAction.getValue() == PixelAction.OffVeryWide;
    }

    private boolean isPixelActionToggle() {
        return mPixelAction.getValue() == PixelAction.Toggle;
    }

    private boolean isPixelActionDeletePixelChain() {
        return mPixelAction.getValue() == PixelAction.DeletePixelChain;
    }

    synchronized private void actionPixelOn(final Pixel pPixel) {
        pPixel.setEdge(true);
        mPictureControl.redrawGrafitti();
    }

    synchronized private void actionPixelToggle(final Pixel pPixel) {
        pPixel.setEdge(!pPixel.isEdge());
        mPictureControl.redrawGrafitti();
    }

    synchronized private void actionPixelChainDelete(final Pixel pPixel) {
        mPixelMap.getPixelChains(pPixel).stream().forEach(pc -> pc.delete());
        mPictureControl.redrawGrafitti();
    }

    @Override
    public void mouseDoubleClickEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
    }

    @Override
    public void mouseDragEndEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
        Optional<Pixel> pixel = Optional.empty(); // this is not used but is set here to make the layer's interfaces consistent
        if (isPixelView()) mouseDragEndEventPixelView(pEvent, pixel);
    }

    private void mouseDragEndEventPixelView(final IUIEvent pEvent, Optional<Pixel> pPixel) {
        getOptionalUndoRedoBuffer().ifPresent(u -> {
            u.endSavepoint(mSavepointId);
            mSavepointId = null;
        });
        grafittiCursorRemovePrevious();
        mMouseDragLastPixel = Optional.empty();
    }


    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
        Framework.logEntry(Framework.mLogger);
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isGeneralView()) mouseDragEventGeneralView(pEvent, pixel);
        if (isPixelView()) mouseDragEventPixelViewFillIn(pEvent, pixel);
    }

    /**
     * Fills in the gaps in the drag event so that all the pixels are connected.
     **/
    private void mouseDragEventPixelViewFillIn(IUIEvent pEvent, Optional<Pixel> pPixel) {
        System.out.println(String.format("############## mouseDragEventPixelViewFillIn %s, %s", pPixel, mMouseDragLastPixel));
        if (isPixelActionOn() || isPixelActionOff()) {
            if (mMouseDragLastPixel.isPresent() && pPixel.isPresent() && !mMouseDragLastPixel.get().equals(pPixel.get())) {
                System.out.println(String.format("############## mouseDragEventPixelViewFillIn ..."));
                int dX = pPixel.get().getX() - mMouseDragLastPixel.get().getX();
                int dY = pPixel.get().getY() - mMouseDragLastPixel.get().getY();
                if (Math.abs(dX) >= Math.abs(dY)) { // fill in missing x
                    int from = Math.min(mMouseDragLastPixel.get().getX(), pPixel.get().getX());
                    int to = Math.max(mMouseDragLastPixel.get().getX(), pPixel.get().getX());
                    IntStream.range(from, to).forEach(x -> {
                        int y = (int) Math.round(mMouseDragLastPixel.get().getY() + (((double) x - mMouseDragLastPixel.get().getX()) / dX) * dY);
                        final Optional<Pixel> pixel = mPixelMap.getOptionalPixelAt(x, y);
                        System.out.println(String.format("############## mouseDragEventPixelViewFillIn X  %s, %s", x, y));
                        pixel.ifPresent(p -> mouseDragEventPixelView(UIEvent.createMouseEvent(pEvent, x, y), pixel));
                    });
                } else { // fill in missing y
                    int from = Math.min(mMouseDragLastPixel.get().getY(), pPixel.get().getY());
                    int to = Math.max(mMouseDragLastPixel.get().getY(), pPixel.get().getY());
                    IntStream.range(from, to).forEach(y -> {
                        int x = (int) Math.round(mMouseDragLastPixel.get().getX() + (((double) y - mMouseDragLastPixel.get().getY()) / dY) * dX);
                        final Optional<Pixel> pixel = mPixelMap.getOptionalPixelAt(x, y);
                        System.out.println(String.format("############## mouseDragEventPixelViewFillIn Y  %s, %s", x, y));
                        pixel.ifPresent(p -> mouseDragEventPixelView(UIEvent.createMouseEvent(pEvent, x, y), pixel));
                    });
                }
            }
        }
        mouseDragEventPixelView(pEvent, pPixel);
        mMouseDragLastPixel = pPixel;
    }

    private void mouseDragEventGeneralView(final IUIEvent pEvent, Optional<Pixel> pPixel) {
        mViewOriginX.setValue((int) (mMouseDragStartX - pEvent.getNormalizedDeltaX() * mTransform.getWidth() / getZoom()));
        mViewOriginY.setValue((int) (mMouseDragStartY - pEvent.getNormalizedDeltaY() * mTransform.getHeight() / getZoom()));
    }

    private void mouseDragEventPixelView(final IUIEvent pEvent, Optional<Pixel> pPixel) {
        pPixel.ifPresent(pixel -> {
            if (isPixelActionOn()) actionPixelOn(pixel);
            if (isPixelActionOff()) actionPixelOff(pixel);
            if (isPixelActionOffWide()) actionPixelOff(pixel);
            if (isPixelActionOffVeryWide()) actionPixelOff(pixel);
            if (isPixelActionToggle()) mouseDragEventPixelViewToggle(pEvent, pPixel);
            if (isPixelActionDeletePixelChain()) mouseDragEventPixelViewDeletePixelChain(pEvent, pPixel);
            grafittiCursor(pEvent, pPixel);
        });
    }

    synchronized private void actionPixelOff(final Pixel pPixel) {
        int size = getCursorSize();
        double radius = (double) size * getZoom() / mPixelMapHeight.getValue();

        new Range2D(pPixel.getX() - size, pPixel.getX() + size, pPixel.getY() - size, pPixel.getY() + size)
                .forEach((x, y) -> {
                    mPixelMap.getOptionalPixelAt(x, y)
                            .filter(p -> pPixel.getUHVWPoint().distance(p.getUHVWPoint()) < radius)
                            .filter(p -> p.isEdge())
                            .ifPresent(p -> {
                                p.setEdge(false);
                            });
                });
        grafittiCursorRemovePrevious();
    }

    private void grafittiCursor(IUIEvent pEvent, Optional<Pixel> pixel) {
        pixel.ifPresent(p -> {
            IGrafitti g = grafittiHelper -> {
                grafittiHelper.drawCircle(pEvent.getNormalizedX(), pEvent.getNormalizedY(), getRadius(), Color.red, false);
            };
            mPictureControl.redrawGrafitti(g);
            mMouseLastPixelPosition = pixel;
            mMouseLastSize = getCursorSize();
        });

    }

    private double getRadius() {
        double radius = (double) getCursorSize() * getZoom() / mPixelMapHeight.getValue();
        return radius;
    }

    private int getCursorSize() {
        if (mPixelAction.getValue() == PixelAction.OffWide) return 3;
        if (mPixelAction.getValue() == PixelAction.OffVeryWide) return 5;
        return 1;
    }

    /**
     * this is to remove the cursor that has been drawn on the previous grafittiCursor call
     */
    private void grafittiCursorRemovePrevious() {
        mMouseLastPixelPosition.ifPresent(p -> {
            IGrafitti g = grafittiHelper -> {
                double x1 = pixelXToGrafittiX(p.getX() - mMouseLastSize - 1, getViewOriginX(), getWidth(), getZoom());
                double y1 = pixelYToGrafittiY(p.getY() - mMouseLastSize - 1, getViewOriginY(), getHeight(), getZoom());
                double x2 = pixelXToGrafittiX(p.getX() + mMouseLastSize + 1, getViewOriginX(), getWidth(), getZoom());
                double y2 = pixelYToGrafittiY(p.getY() + mMouseLastSize + 1, getViewOriginY(), getHeight(), getZoom());
                grafittiHelper.clearRectangle(x1, y1, x2, y2);
                grafitti(grafittiHelper);
            };
            mPictureControl.redrawGrafitti(g);
        });
    }

    private void mouseDragEventPixelViewToggle(final IUIEvent pEvent, Optional<Pixel> pPixel) {
        pPixel.filter(p -> !mMouseLastPixelPosition.isPresent() || !p.equals(mMouseLastPixelPosition.get()))
                .ifPresent(p -> {
                    actionPixelToggle(p);
                    mMouseLastPixelPosition = pPixel;
                });
    }

    private void mouseDragEventPixelViewDeletePixelChain(final IUIEvent pEvent, Optional<Pixel> pPixel) {
        pPixel
                .filter(p -> p.isEdge())
                .ifPresent(p -> {
                    mPixelMap.getPixelChains(p).stream().forEach(pc -> pc.delete());
                });
        mPictureControl.redrawGrafitti();
    }

    @Override
    public void mouseMoveEvent(final IUIEvent pEvent) {
        Framework.logEntry(mLogger);
        Optional<Pixel> pixel = eventToPixel(pEvent);
        pixel.filter(p -> !p.equals(mMouseLastPixelPosition.get()))
                .ifPresent(p -> {
                    grafittiCursorRemovePrevious();
                    grafittiCursor(pEvent, pixel);
                });
    }

    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        System.out.println("mouseDragStartEvent");
        if (isGeneralView()) mouseDragStartEventGeneralView(pEvent);
        if (isPixelView()) mouseDragStartEventPixelView(pEvent);
    }

    private void mouseDragStartEventGeneralView(final IUIEvent pEvent) {
        System.out.println("mouseDragStartEventGeneralView");
        mMouseDragStartX = getViewOriginX();
        mMouseDragStartY = getViewOriginY();
    }

    private void mouseDragStartEventPixelView(final IUIEvent pEvent) {
        getOptionalUndoRedoBuffer().ifPresent(u -> mSavepointId = u.startSavepoint("mouseDrag"));
        grafittiCursor(pEvent, eventToPixel(pEvent));
    }

    public Optional<UndoRedoBuffer> getOptionalUndoRedoBuffer() { // TODO not sure that this is needed
        return Perception.getPerception().getOptionalUndoRedoBuffer();
    }

    @Override
    public void scrollEvent(final IUIEvent pEvent) {
        System.out.println("scrollEvent");
    }

    @Override
    public void keyPressed(final IUIEvent pEvent) {
        System.out.println("keyPressed " + pEvent.getKey());
        if ("G".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mGeneralContainer);
        if ("P".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mPixelControlContainer);
        if ("V".equals(pEvent.getKey())) mContainerList.setSelectedIndex(mVertexControlContainer);
        mPictureControl.redrawGrafitti(); // TODO should only be for the undo redo events
    }

    @Override
    public void keyReleased(final IUIEvent pEvent) {
        System.out.println("keyReleased " + pEvent.getKey());
    }

    @Override
    public void keyTyped(final IUIEvent pEvent) {
        System.out.println("keyTyped " + pEvent.getKey());
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

    private boolean isGeneralView() {
        return mContainerList.getSelectedContainer() == mGeneralContainer;
    }

    private boolean isPixelView() {
        return mContainerList.getSelectedContainer() == mPixelControlContainer;
    }

    private boolean isVertexView() {
        return mContainerList.getSelectedContainer() == mVertexControlContainer;
    }
}
