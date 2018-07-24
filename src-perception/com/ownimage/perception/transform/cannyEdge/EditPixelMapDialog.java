package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.ColorControl;
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
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
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
    private final IntegerControl mVCC1 = new IntegerControl("Vertex test 1", "vertexTest1", mVertexControlContainer, 2, 2, 15, 1);
    private final IntegerControl mVCC2 = new IntegerControl("Vertex test 2", "vertexTest2", mVertexControlContainer, 2, 2, 15, 1);
    private final IntegerControl mVCC3 = new IntegerControl("Vertex test 3", "vertexTest3", mVertexControlContainer, 2, 2, 15, 1);

    private boolean mDialogIsAlive = false;

    private int mMouseDragStartX;
    private int mMouseDragStartY;
    private Optional<Pixel> mMouseLastPixelPosition = Optional.empty();
    private int mMouseLastSize;

    public EditPixelMapDialog(final ITransform pTransform, final PixelMap pPixelMap, final String pDisplayName, final String pPropertyName, IUndoRedoBufferProvider undoRedoBufferProvider) {
        super(pDisplayName, pPropertyName, undoRedoBufferProvider);
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");
        Framework.checkParameterNotNull(mLogger, pPixelMap, "pPixelMap");
        mTransform = pTransform;
        mPixelMap = pPixelMap;
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

    public void showDialog(final ActionControl pOk, final ActionControl pCancel) {
        mDialogIsAlive = true;

        mCropTransform.setPreviousTransform(mTransform.getPreviousTransform());
        updatePreview();
        Perception
                .getPerception()
                .showDialog(this,
                            DialogOptions.builder().withCompleteFunction(this::closeDialog).build(),
                            getUndoRedoBuffer(),
                            pCancel, pOk);
    }

    private void closeDialog(Optional<ActionControl> pActionControl) {
        mDialogIsAlive = false;
    }

    @Override
    public void grafitti(final GrafittiHelper pGrafittiHelper) {
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

        int x = getViewOriginX() + (pEvent.getX() * getWidth() / (getZoom() * getPreviewSize()));
        int y = getViewOriginY() + (pEvent.getY() * getHeight() / (getZoom() * getPreviewSize()));
        System.out.format("pEvent = %d, %d, x = %d, y = %d\n", pEvent.getX(), pEvent.getY(), x, y);
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

    private void actionPixelOn(final Pixel pPixel) {
        pPixel.setEdge(true);
        mPictureControl.redrawGrafitti();
    }

    private void actionPixelToggle(final Pixel pPixel) {
        pPixel.setEdge(!pPixel.isEdge());
        mPictureControl.redrawGrafitti();
    }

    private void actionPixelChainDelete(final Pixel pPixel) {
        mPixelMap.getPixelChain(pPixel).stream().forEach(pc -> pc.delete());
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
        grafittiCursorRemovePrevious();
    }


    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
        Framework.logEntry(Framework.mLogger);
        final Optional<Pixel> pixel = eventToPixel(pEvent);
        if (isGeneralView()) mouseDragEventGeneralView(pEvent, pixel);
        if (isPixelView()) mouseDragEventPixelView(pEvent, pixel);
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

    private void actionPixelOff(final Pixel pPixel) {
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
                    mPixelMap.getPixelChain(p).stream().forEach(pc -> pc.delete());
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
        grafittiCursor(pEvent, eventToPixel(pEvent));
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
