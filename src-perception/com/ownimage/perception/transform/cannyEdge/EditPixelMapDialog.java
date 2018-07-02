package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.GrafittiHelper;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IGrafitti;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.ContainerList;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.VFlowLayout;
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
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.transform.ITransform;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator, IGrafitti {

    public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private PixelMap mPixelMap;

    private final ITransform mTransform;
    PictureControl mPictureControl = new PictureControl("Test Integer Control", "gausianKernelWidth", NullContainer.NullContainer,
                                                        new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(), 100, 100));

    private IContainer mGeneralContainer = newContainer("General", "general", true);
    private IntegerControl mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, 600, 100, 1000, 50);
    private IntegerControl mZoom = new IntegerControl("Zoom", "zoom", mGeneralContainer, 2, 1, 16, 2);
    private IntegerControl mX;
    private IntegerControl mY;


    private ContainerList mContainerList = new ContainerList("Edit PixelMap", "editPixelMap");
    private IContainer mPixelControlContainer = mContainerList.add(new Container("Pixel", "pixel", this));
    private IContainer mVertexControlContainer = mContainerList.add(new Container("Vertex", "vertex", this));

    private IntegerControl mPCC1 = new IntegerControl("Pixel test 1", "pixelTest1", mPixelControlContainer, 2, 2, 15, 1);
    private IntegerControl mPCC2 = new IntegerControl("Pixel test 2", "pixelTest2", mPixelControlContainer, 2, 2, 15, 1);
    private IntegerControl mPCC3 = new IntegerControl("Pixel test 3", "pixelTest3", mPixelControlContainer, 2, 2, 15, 1);
    private IntegerControl mVCC1 = new IntegerControl("Vertex test 1", "vertexTest1", mVertexControlContainer, 2, 2, 15, 1);
    private IntegerControl mVCC2 = new IntegerControl("Vertex test 2", "vertexTest2", mVertexControlContainer, 2, 2, 15, 1);
    private IntegerControl mVCC3 = new IntegerControl("Vertex test 3", "vertexTest3", mVertexControlContainer, 2, 2, 15, 1);

    public EditPixelMapDialog(final ITransform pTransform, final String pDisplayName, final String pPropertyName, IUndoRedoBufferProvider undoRedoBufferProvider) {
        super(pDisplayName, pPropertyName, undoRedoBufferProvider);
        Framework.checkNotNull(mLogger, pTransform, "pTransform");
        mTransform = pTransform;
        mPictureControl.setGrafitti(this);
        mPictureControl.setUIListener(this);
        updatePreview();
    }

    private void updatePreview() {
        if (mPreviewSize.getValue() != mPictureControl.getWidth()) {
            final PictureType pictureType = new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(), mPreviewSize.getValue(), mPreviewSize.getValue());
            mPictureControl.setValue(pictureType);
        }
        Perception.getPerception().getRenderService().transform(mPictureControl, mTransform.getPreviousTransform());
        //mPictureControl.setValue(pictureType, null, false);
    }

    @Override
    public boolean validateControl(final Object pControl) {
        return true;
    }

    @Override
    public void controlChangeEvent(final IControl<?, ?, ?, ?> pControl, final boolean pIsMutating) {
        System.out.println("controlChanteEvent for " + pControl.getDisplayName());
        updatePreview();
    }

    @Override
    public IView createView() {
        VFlowLayout vflow = new VFlowLayout(mGeneralContainer, mContainerList);
        HFlowLayout hflow = new HFlowLayout(mPictureControl, vflow);
        IView view = ViewFactory.getInstance().createView(hflow);
        addView(view);
        return view;
    }

    public void showDialog(final ActionControl pOk, final ActionControl pCancel) {
        Perception.getPerception().showDialog(this, new DialogOptions(), getUndoRedoBuffer(), pCancel, pOk);
    }

    @Override
    public void grafitti(final GrafittiHelper pGrafittiHelper) {
        if (mPixelMap != null) {
            mPixelMap.forEach((x, y) -> {
                if (mPixelMap.getValue(x, y) != 0) {
                    double x1 = (double) x / mPixelMap.getWidth();
                    double x2 = (double) (x + 1) / mPixelMap.getWidth();
                    double y1 = (double) y / mPixelMap.getHeight();
                    double y2 = (double) (y + 1) / mPixelMap.getHeight();
                    Rectangle r = new Rectangle(x1, y1, x2, y2);
                    pGrafittiHelper.drawFilledRectangle(r, Color.YELLOW);
                }
            });
            pGrafittiHelper.drawCircle(.5, .5, .2, Color.RED, false);
        }
    }

    public void setPixelMap(final PixelMap pPixelMap) {
        Framework.checkNotNull(mLogger, pPixelMap, "pPixelMap");
        Framework.checkNoChangeOnceSet(mLogger, mPixelMap, "mPixelMap");
        mPixelMap = pPixelMap;
         mX = new IntegerControl("X", "x", mGeneralContainer, 0, 0, mPixelMap.getWidth(), 50);
        mY = new IntegerControl("Y", "y", mGeneralContainer, 0, 0, mPixelMap.getHeight(), 50);
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
        System.out.println("mouseDragEvent");
    }


    @Override
    public void mouseDragStartEvent(final IUIEvent pEvent) {
        System.out.println("mouseDragStartEvent");
    }

    @Override
    public void scrollEvent(final IUIEvent pEvent) {
        System.out.println("scrollEvent");
    }


    @Override
    public void keyPressed(final IUIEvent pEvent) {
        System.out.println("keyPressed " + pEvent.getKey());
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
