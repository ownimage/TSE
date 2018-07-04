package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.ColorControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IGrafitti;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.ContainerList;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.math.Rectangle;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.transform.CropTransform;
import com.ownimage.perception.transform.ITransform;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator, IGrafitti {

    public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

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

    // Vertex Container
    private final IntegerControl mVCC1 = new IntegerControl("Vertex test 1", "vertexTest1", mVertexControlContainer, 2, 2, 15, 1);
    private final IntegerControl mVCC2 = new IntegerControl("Vertex test 2", "vertexTest2", mVertexControlContainer, 2, 2, 15, 1);
    private final IntegerControl mVCC3 = new IntegerControl("Vertex test 3", "vertexTest3", mVertexControlContainer, 2, 2, 15, 1);

    private Map<String, IContainer> mKeyToContainerMap = new HashMap();
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

        mKeyToContainerMap.put("G", mGeneralContainer);
        mKeyToContainerMap.put("P", mPixelControlContainer);
        mKeyToContainerMap.put("V", mVertexControlContainer);
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
        mCropTransform.setPreviousTransform(mTransform.getPreviousTransform());
        updatePreview();
        Perception.getPerception().showDialog(this, new DialogOptions(), getUndoRedoBuffer(), pCancel, pOk);
    }

    @Override
    public void grafitti(final GrafittiHelper pGrafittiHelper) {
        Framework.checkStateNotNull(mLogger, mPixelMap, "mPixelMap");
        int zoom = mZoom.getValue();

        // going to scan over pixelMap
        int xMin = mViewOriginX.getValue();
        int yMin = mViewOriginY.getValue();
        int xSize = 1 + Math.floorDiv(mPixelMap.getWidth(), zoom);
        int ySize = 1 + Math.floorDiv(mPixelMap.getHeight(), zoom);

        System.out.println(xMin + " " + yMin + " " + xSize + " " + ySize);

        mPixelMap.forEach((x, y) -> {
            if (mPixelMap.getValue(x, y) != 0) {
                double x1 = (double) (x - xMin) * zoom / mPixelMap.getWidth();
                double x2 = (double) (x + 1 - xMin) * zoom / mPixelMap.getWidth();
                double y1 = (double) (y - yMin) * zoom / mPixelMap.getHeight();
                double y2 = (double) (y + 1 - yMin) * zoom / mPixelMap.getHeight();
                Rectangle r = new Rectangle(x1, y1, x2, y2);
                Pixel pixel = mPixelMap.getPixelAt(x, y);
                Color c =
                        pixel.isNode() ? mNodeColor.getValue() :
                                pixel.isEdge() ? mEdgeColor.getValue() :
                                        Color.BLACK;
                pGrafittiHelper.drawFilledRectangle(r, c);
            }
        });
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
        System.out.println("mouseClickEvent");
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
        Optional.of(mKeyToContainerMap.get(pEvent.getKey()))
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
}
