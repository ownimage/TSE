package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
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

    private Map<PixelAction, Consumer<Pixel>> mActionMap = new HashMap<>();

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

    private Map<String, IContainer> mKeyToContainerMap = new HashMap();

    private BlockingQueue<IUIEvent> mMouseEventQueue = new LinkedBlockingQueue();

    private boolean mDialogIsAlive = false;

    private int mMouseDragStartX;
    private int mMouseDragStartY;

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

        setupKeyToContainerMap();
        setupActionMap();
    }

    private void setupKeyToContainerMap() {
        mKeyToContainerMap.put("G", mGeneralContainer);
        mKeyToContainerMap.put("P", mPixelControlContainer);
        mKeyToContainerMap.put("V", mVertexControlContainer);
    }

    private void setupActionMap() {
        mActionMap.put(PixelAction.On, this::mouseClickEventPixelViewOn);
        mActionMap.put(PixelAction.Off, this::mouseClickEventPixelViewOff);
        mActionMap.put(PixelAction.Toggle, this::mouseClickEventPixelViewToggle);
        mActionMap.put(PixelAction.DeletePixelChain, this::mouseClickEventPixelViewDeletePixelChain);
        mActionMap.put(PixelAction.OffVeryWide, this::mouseClickEventPixelViewOffVeryWide);
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
        startMouseEventQueue();

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
        int xMin = mViewOriginX.getValue();
        int yMin = mViewOriginY.getValue();

        mPixelMap.forEach((x, y) -> {
            Pixel pixel = mPixelMap.getPixelAt(x, y);
            if (pixel.isNode() || pixel.isEdge()) {
                Rectangle r = pixelToRectangle(pixel, xMin, yMin, zoom);
                Color c = pixel.isNode() ? mNodeColor.getValue() : mEdgeColor.getValue();
                pGrafittiHelper.drawFilledRectangle(r, c);
            }
        });

        if (mShowGrafitti.getValue()) {
            mPixelMap.forEachPixelChain(pc -> drawPixelChain(pc, pGrafittiHelper));
        }
    }

    private Rectangle pixelToRectangle(Pixel pPixel, int xMin, int yMin, int zoom) {
        int x = pPixel.getX();
        int y = pPixel.getY();
        double x1 = (double) (x - xMin) * zoom / mPixelMap.getWidth();
        double x2 = (double) (x + 1 - xMin) * zoom / mPixelMap.getWidth();
        double y1 = (double) (y - yMin) * zoom / mPixelMap.getHeight();
        double y2 = (double) (y + 1 - yMin) * zoom / mPixelMap.getHeight();
        Rectangle r = new Rectangle(x1, y1, x2, y2);
        return r;
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

    private Pixel eventToPixel(IUIEvent pEvent) {
        int zoom = mZoom.getValue();
        int size = mPreviewSize.getValue();
        int xMin = mViewOriginX.getValue();
        int yMin = mViewOriginY.getValue();
        int width = mPixelMap.getWidth();
        int height = mPixelMap.getHeight();
        int x = xMin + (pEvent.getX() * width / (zoom * size));
        int y = yMin + (pEvent.getY() * height / (zoom * size));
        System.out.format("pEvent = %d, %d, x = %d, y = %d\n", pEvent.getX(), pEvent.getY(), x, y);
        return mPixelMap.getPixelAt(x, y);
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
        mMouseEventQueue.offer(pEvent);
    }

    private void startMouseEventQueue() {
        new Thread(this::processMouseEventQueue, "EditPixelMapDialog mouseEventQueue processor").start();
    }

    private void processMouseEventQueue() {
        while (mDialogIsAlive) {
            try {
                IUIEvent event = mMouseEventQueue.poll(1, TimeUnit.SECONDS);
                System.out.println("Loop " + Thread.currentThread());
                if (event != null) mouseClickEventAsync(event);
            } catch (InterruptedException e) {
                // do nothing handled by loop retry
            }
        }
    }

    private void mouseClickEventAsync(final IUIEvent pEvent) {
        if (isPixelView()) mouseClickEventPixelView(eventToPixel(pEvent));
    }

    private void mouseClickEventPixelView(final Pixel pPixel) {
        final Optional<Consumer<Pixel>> action = Optional.ofNullable(mActionMap.get(mPixelAction.getValue()));
        action.ifPresent(a -> a.accept(pPixel));
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
        IGrafitti g = grafittiHelper -> {
            grafittiHelper.clearRectangle(0.0d, 0.0d, 0.5d, 0.5d);
            grafittiHelper.drawFilledRectangle(0.25, 0.25, 0.75, 0.75, Color.red);
        };
        mPictureControl.redrawGrafitti(g);
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
    }

    @Override
    public void mouseDragEvent(final IUIEvent pEvent) {
        System.out.println("mouseDragEvent " + pEvent.getDeltaX());
        if (mContainerList.getSelectedContainer() == mGeneralContainer) {
            mViewOriginX.setValue((int) (mMouseDragStartX - pEvent.getNormalizedDeltaX() * mTransform.getWidth() / mZoom.getValue()));
            mViewOriginY.setValue((int) (mMouseDragStartY - pEvent.getNormalizedDeltaY() * mTransform.getHeight() / mZoom.getValue()));
        }
    }


    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        System.out.println("mouseDragStartEvent");
        mMouseDragStartX = mViewOriginX.getValue();
        mMouseDragStartY = mViewOriginY.getValue();
    }

    @Override
    public void scrollEvent(final IUIEvent pEvent) {
        System.out.println("scrollEvent");
    }

    @Override
    public void keyPressed(final IUIEvent pEvent) {
        System.out.println("keyPressed " + pEvent.getKey());
        Optional.ofNullable(mKeyToContainerMap.get(pEvent.getKey()))
                .ifPresent(c -> mContainerList.setSelectedIndex(c));
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
