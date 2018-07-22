package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

        private PixelAction(String pName) {
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
    private Optional<Pixel> mMouseDragLastPixel = Optional.empty();
    private int mMouseLastSize;

    public EditPixelMapDialog(final ITransform pTransform, final PixelMap pPixelMap, final String pDisplayName, final String pPropertyName, IUndoRedoBufferProvider undoRedoBufferProvider) {
        super(pDisplayName, pPropertyName, undoRedoBufferProvider);
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");
        Framework.checkParameterNotNull(mLogger, pPixelMap, "pPixelMap");
        mTransform = pTransform;
        mPixelMap = pPixelMap;
        mPixelMapWidth = new IntegerControl("PixelMap Height", "pixelMapWidth", mGeneralContainer, mPixelMap.getWidth(), 0, mPixelMap.getWidth(), 50).setEnabled(false);
        mPixelMapHeight = new IntegerControl("PixelMap Height", "pixelMapHeight", mGeneralContainer, mPixelMap.getHeight(), 0, mPixelMap.getHeight(), 50).setEnabled(false);
        mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, 600, 100, 1000, 50);
        mZoom = new IntegerControl("Zoom", "zoom", mGeneralContainer, 2, 1, 16, 2);
        mViewOriginX = new IntegerControl("View X", "x", mGeneralContainer, 0, 0, mPixelMap.getWidth(), 50);
        mViewOriginY = new IntegerControl("View Y", "y", mGeneralContainer, 0, 0, mPixelMap.getHeight(), 50);
        mCropTransform = new CropTransform(Perception.getPerception(), true);
        mPictureControl.setGrafitti(this);
        mPictureControl.setUIListener(this);
        updatePreview();
    }

    private void updatePreview() {
        if (mPreviewSize.getValue() != mPictureControl.getWidth()) {
            final PictureType pictureType = new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(), mPreviewSize.getValue(), mPreviewSize.getValue());
            mPictureControl.setValue(pictureType);
        }
        setCrop();
        Perception.getPerception().getRenderService().transform(mPictureControl, mCropTransform);
    }

    @Override
    public boolean validateControl(final Object pControl) {
        if (pControl == mViewOriginX) {
            boolean valid = mPixelMap.getWidth() > mViewOriginX.getValidateValue() + mPixelMap.getWidth() / mZoom.getValue();
            return valid;
        }
        if (pControl == mViewOriginY) {
            boolean valid = mPixelMap.getHeight() > mViewOriginY.getValidateValue() + mPixelMap.getHeight() / mZoom.getValue();
            return valid;
        }
        if (pControl == mZoom) {
            if (mViewOriginX.getValue() + mPixelMap.getWidth() / mZoom.getValidateValue() > mPixelMap.getWidth()) {
                mViewOriginX.setValue(mPixelMap.getWidth() - Math.floorDiv(mPixelMap.getWidth(), mZoom.getValidateValue()));
            }
            if (mViewOriginY.getValue() + mPixelMap.getHeight() / mZoom.getValidateValue() > mPixelMap.getHeight()) {
                mViewOriginY.setValue(mPixelMap.getHeight() - Math.floorDiv(mPixelMap.getHeight(), mZoom.getValidateValue()));
            }
            return true;
        }
        return true;
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

        int zoom = mZoom.getValue();
        int xSize = Math.floorDiv(mPixelMap.getWidth(), zoom) + 1;
        int ySize = Math.floorDiv(mPixelMap.getHeight(), zoom) + 1;
        int xMin = mViewOriginX.getValue();
        int yMin = mViewOriginY.getValue();

        grafitti(pGrafittiHelper, xMin, xMin + xSize, yMin, yMin + ySize, zoom);

        if (mShowGrafitti.getValue()) {
            mPixelMap.forEachPixelChain(pc -> drawPixelChain(pc, pGrafittiHelper));
        }
    }

    private void grafitti(final GrafittiHelper pGrafittiHelper, int xMin, int xMax, int yMin, int yMax, int mZoom) {
        Range2D range = new Range2D(xMin, xMax, yMin, yMax);
        range.forEach((x, y) -> {
            Pixel pixel = mPixelMap.getPixelAt(x, y);
            if (pixel.isNode() || pixel.isEdge()) {
                Rectangle r = pixelToGrafittiRectangle(pixel, xMin, yMin, mPixelMap.getWidth(), mPixelMap.getHeight(), mZoom);
                Color c = pixel.isNode() ? mNodeColor.getValue() : mEdgeColor.getValue();
                pGrafittiHelper.drawFilledRectangle(r, c);
            }
        });
    }

    private Rectangle pixelToGrafittiRectangle(Pixel pPixel, int xMin, int yMin, int pWidth, int pHeight, int zoom) {
        int x = pPixel.getX();
        int y = pPixel.getY();
        double x1 = pixelXToGrafittiX(x, xMin, pWidth, zoom);
        double x2 = pixelXToGrafittiX(x + 1, xMin, pWidth, zoom);
        double y1 = pixelYToGrafittiY(y, yMin, pHeight, zoom);
        double y2 = pixelYToGrafittiY(y + 1, yMin, pHeight, zoom);
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

    private void drawPixelChain(PixelChain pPixelChain, GrafittiHelper pGrafittiHelper) {
        pPixelChain.getAllSegments().forEach(s -> drawSegment(s, pGrafittiHelper));
    }

    private void drawSegment(ISegment pSegment, GrafittiHelper pGrafittiHelper) {
        //final Point start = UHVWtoView(pSegment.getStartPixel().getUHVWPoint());
        //final Point end = UHVWtoView(pSegment.getEndPixel().getUHVWPoint());
        //pGrafittiHelper.drawLine(start, end, Color.YELLOW);
        SegmentGrafittiHelper segmentGrafittiHelper = new SegmentGrafittiHelper(pGrafittiHelper, this::UHVWtoView);
        pSegment.graffiti(segmentGrafittiHelper);
    }

    private Point UHVWtoView(Point pUHVW) {
        int zoom = mZoom.getValue();
        int xMin = mViewOriginX.getValue();
        int yMin = mViewOriginY.getValue();
        int width = mPixelMap.getWidth();
        int height = mPixelMap.getHeight();
        double x = (double) (width * pUHVW.getX() - xMin) * zoom / width;
        double y = (double) (height * pUHVW.getY() - yMin) * zoom / height;
        return new Point(x, y);
    }

    private Optional<Pixel> eventToPixel(IUIEvent pEvent) {
        int zoom = mZoom.getValue();
        int size = mPreviewSize.getValue();
        int xMin = mViewOriginX.getValue();
        int yMin = mViewOriginY.getValue();
        int width = mPixelMap.getWidth();
        int height = mPixelMap.getHeight();
        int x = xMin + (pEvent.getX() * width / (zoom * size));
        int y = yMin + (pEvent.getY() * height / (zoom * size));
        System.out.format("pEvent = %d, %d, x = %d, y = %d\n", pEvent.getX(), pEvent.getY(), x, y);
        return mPixelMap.getOptionalPixelAt(x, y);
    }

    private void setCrop() {
        if (mPixelMap != null) {
            double left = (double) mViewOriginX.getValue() / mPixelMap.getWidth();
            double right = left + 1.0d / mZoom.getValue();
            double bottom = (double) mViewOriginY.getValue() / mPixelMap.getHeight();
            double top = bottom + 1.0d / mZoom.getValue();
            System.out.print("lrbt " + left + " " + right + " " + bottom + " " + top);
            mCropTransform.setCrop(left, bottom, right, top);
        }
    }

    @Override
    public void mouseClickEvent(final IUIEvent pEvent) {
        eventToPixel(pEvent)
                .ifPresent(pixel -> {
                    if (isPixelView()) mouseClickEventPixelView(pixel);
                });
    }

    private void mouseClickEventPixelView(final Pixel pPixel) {
        if (isPixelActionOn()) mouseClickEventPixelViewOn(pPixel);
        if (isPixelActionOff()) mouseClickEventPixelViewOff(pPixel);
        if (isPixelActionToggle()) mouseClickEventPixelViewToggle(pPixel);
        if (isPixelActionDeletePixelChain()) mouseClickEventPixelViewDeletePixelChain(pPixel);
        if (isPixelActionOffVeryWide()) mouseClickEventPixelViewOffVeryWide(pPixel);
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

    private void mouseClickEventPixelViewOn(final Pixel pPixel) {
        pPixel.setEdge(true);
        cleanPixel(pPixel);
        mPictureControl.redrawGrafitti();
    }

    private void cleanPixel(final Pixel pPixel) { // TODO should this be a member of the Pixel
        pPixel.setInChain(false);
        pPixel.setVisited(false);
    }

    private void mouseClickEventPixelViewOff(final Pixel pPixel) {
        pPixel.setEdge(false);
        cleanPixel(pPixel);
        mPictureControl.redrawGrafitti();
    }

    private void mouseClickEventPixelViewOffWide(final Pixel pPixel) {
    }

    private void mouseClickEventPixelViewOffVeryWide(final Pixel pPixel) {
        //mouseEventPixelViewOff();
    }

    private void mouseClickEventPixelViewToggle(final Pixel pPixel) {
        pPixel.setEdge(!pPixel.isEdge());
        cleanPixel(pPixel);
        mPictureControl.redrawGrafitti();
    }

    private void mouseClickEventPixelViewDeletePixelChain(final Pixel pPixel) {
        mPixelMap.getPixelChain(pPixel).stream().forEach(pc -> pc.delete());
        cleanPixel(pPixel);
        mPictureControl.redrawGrafitti();
    }

    @Override
    public void mouseDoubleClickEvent(final IUIEvent pEvent) {
        System.out.println("mouseDoubleClickEvent");
    }

    @Override
    public void mouseDragEndEvent(final IUIEvent pEvent) {
        System.out.println("mouseDragEndEvent");
        if (isPixelView()) mouseDragEndEventPixelView(pEvent);
    }

    private void mouseDragEndEventPixelView(final IUIEvent pEvent) {
        redrawRepairLastMouseDrag();
    }


    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
        System.out.println("mouseDragEvent " + pEvent.getDeltaX());
        if (isGeneralView()) mouseDragEventGeneralView(pEvent);
        if (isPixelView()) mouseDragEventPixelView(pEvent);
    }

    private void mouseDragEventGeneralView(final IUIEvent pEvent) {
        mViewOriginX.setValue((int) (mMouseDragStartX - pEvent.getNormalizedDeltaX() * mTransform.getWidth() / mZoom.getValue()));
        mViewOriginY.setValue((int) (mMouseDragStartY - pEvent.getNormalizedDeltaY() * mTransform.getHeight() / mZoom.getValue()));
    }

    private void mouseDragEventPixelView(final IUIEvent pEvent) {
        if (mPixelAction.getValue() == PixelAction.On) mouseDragEventPixelViewOn(pEvent);
        if (mPixelAction.getValue() == PixelAction.Off) mouseEventPixelViewOff(pEvent);
        if (mPixelAction.getValue() == PixelAction.OffWide) mouseEventPixelViewOff(pEvent);
        if (mPixelAction.getValue() == PixelAction.OffVeryWide) mouseEventPixelViewOff(pEvent);
        if (mPixelAction.getValue() == PixelAction.Toggle) mouseDragEventPixelViewToggle(pEvent);
        if (mPixelAction.getValue() == PixelAction.DeletePixelChain) mouseDragEventPixelViewDeletePixelChain(pEvent);
    }

    private void mouseDragEventPixelViewOn(final IUIEvent pEvent) {
        eventToPixel(pEvent)
                .filter(p -> !p.isEdge())
                .ifPresent(p -> {
                    p.setEdge(true);
                    mPictureControl.redrawGrafitti();
                });
    }

    private void mouseEventPixelViewOff(final IUIEvent pEvent) {
        int size = getCursorSize();
        double radius = (double) size * mZoom.getValue() / mPixelMapHeight.getValue();

        Optional<Pixel> pixel = eventToPixel(pEvent);
        pixel
                .ifPresent(p1 -> {
                    new Range2D(p1.getX() - size, p1.getX() + size, p1.getY() - size, p1.getY() + size)
                            .forEach((x, y) -> {
                                mPixelMap.getOptionalPixelAt(x, y)
                                        .filter(p -> p1.getUHVWPoint().distance(p.getUHVWPoint()) < radius)
                                        .filter(p -> p.isEdge())
                                        .ifPresent(p -> {
                                            p.setEdge(false);

                                        });

                            });
                });

        grafittiCursor(pEvent, pixel);
    }

    private void grafittiCursor(IUIEvent pEvent, Optional<Pixel> pixel) {
        pixel.ifPresent(p -> {
            redrawRepairLastMouseDrag();
            IGrafitti g = grafittiHelper -> {
                grafittiHelper.drawCircle(pEvent.getNormalizedX(), pEvent.getNormalizedY(), getRadius(), Color.red, false);
            };
            mPictureControl.redrawGrafitti(g);
            mMouseDragLastPixel = pixel;
            mMouseLastSize = getCursorSize();
        });

    }

    private double getRadius() {
        double radius = (double) getCursorSize() * mZoom.getValue() / mPixelMapHeight.getValue();
        return radius;
    }

    private int getCursorSize() {
        if (mPixelAction.getValue() == PixelAction.OffWide) return 3;
        if (mPixelAction.getValue() == PixelAction.OffVeryWide) return 5;
        return 1;
    }

    private void redrawRepairLastMouseDrag() {
        // this is to remove the cursor that has been drawn on the previous mouseDrag operation
        mMouseDragLastPixel.ifPresent(p -> {
            IGrafitti g = grafittiHelper -> {
                double x1 = pixelXToGrafittiX(p.getX() - mMouseLastSize - 1, mViewOriginX.getValue(), mPixelMap.getWidth(), mZoom.getValue());
                double y1 = pixelYToGrafittiY(p.getY() - mMouseLastSize - 1, mViewOriginY.getValue(), mPixelMap.getHeight(), mZoom.getValue());
                double x2 = pixelXToGrafittiX(p.getX() + mMouseLastSize + 1, mViewOriginX.getValue(), mPixelMap.getWidth(), mZoom.getValue());
                double y2 = pixelYToGrafittiY(p.getY() + mMouseLastSize + 1, mViewOriginY.getValue(), mPixelMap.getHeight(), mZoom.getValue());
                grafittiHelper.clearRectangle(x1, y1, x2, y2);
                grafitti(grafittiHelper);
            };
            mPictureControl.redrawGrafitti(g);
        });
    }

    private void mouseDragEventPixelViewToggle(final IUIEvent pEvent) {
        Optional<Pixel> pixel = eventToPixel(pEvent);
        pixel
                .filter(p -> !mMouseDragLastPixel.isPresent() || !p.equals(mMouseDragLastPixel.get()))
                .ifPresent(p -> {
                    p.setEdge(!p.isEdge());
                    mMouseDragLastPixel = pixel;
                    mPictureControl.redrawGrafitti();
                });
    }

    private void mouseDragEventPixelViewDeletePixelChain(final IUIEvent pEvent) {
        eventToPixel(pEvent)
                .filter(p -> p.isEdge())
                .ifPresent(p -> {
                    mPixelMap.getPixelChain(p).stream().forEach(pc -> pc.delete());
                    cleanPixel(p);
                });
        mPictureControl.redrawGrafitti();
    }

    @Override
    public void mouseMoveEvent(final IUIEvent pEvent) {
        grafittiCursor(pEvent, eventToPixel(pEvent));
    }

    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        System.out.println("mouseDragStartEvent");
        if (isGeneralView()) mouseDragStartEventGeneralView(pEvent);
        if (isPixelView()) mouseDragStartEventPixelView(pEvent);
    }

    private void mouseDragStartEventGeneralView(final IUIEvent pEvent) {
        System.out.println("mouseDragStartEventGeneralView");
        mMouseDragStartX = mViewOriginX.getValue();
        mMouseDragStartY = mViewOriginY.getValue();
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
