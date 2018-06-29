package com.ownimage.perception.transform.cannyEdge;

import java.awt.*;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.BooleanControl;
import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.IUIEventListener;
import com.ownimage.framework.control.control.IntegerControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.layout.ContainerList;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.control.type.PictureType;
import com.ownimage.framework.undo.IUndoRedoBufferProvider;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.pixelMap.PixelMap;

public class EditPixelMapDialog extends Container implements IUIEventListener, IControlValidator {

    PictureControl mPictureControl = new PictureControl("Test Integer Control", "gausianKernelWidth", NullContainer.NullContainer,
                                                        new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(), 100, 100));

    private IContainer mGeneralContainer = newContainer("General", "general", true);
    private IntegerControl ggg = new IntegerControl("ggg 1", "ggg", mGeneralContainer, 2, 2, 15, 1);
    private IntegerControl mPreviewSize = new IntegerControl("Preview Size", "previewSize", mGeneralContainer, 100, 100, 1000, 50);

    private ContainerList mContainerList = new ContainerList("Edit PixelMap", "editPixelMap");
    private IContainer mPixelControlContainer = mContainerList.add(new Container("Pixel", "pixel", this));
    private IContainer mVertexControlContainer = mContainerList.add(new Container("Vertex", "vertex", this));

    private IntegerControl mPCC1 = new IntegerControl("Pixel test 1", "pixelTest1", mPixelControlContainer, 2, 2, 15, 1);
    private  IntegerControl mPCC2 = new IntegerControl("Pixel test 2", "pixelTest2", mPixelControlContainer, 2, 2, 15, 1);
    private IntegerControl mPCC3 = new IntegerControl("Pixel test 3", "pixelTest3", mPixelControlContainer, 2, 2, 15, 1);
    private IntegerControl mVCC1 = new IntegerControl("Vertex test 1", "vertexTest1", mVertexControlContainer, 2, 2, 15, 1);
    private IntegerControl mVCC2 = new IntegerControl("Vertex test 2", "vertexTest2", mVertexControlContainer, 2, 2, 15, 1);
    private IntegerControl mVCC3 = new IntegerControl("Vertex test 3", "vertexTest3", mVertexControlContainer, 2, 2, 15, 1);

    public EditPixelMapDialog(final PixelMap pPixelMap, final String pDisplayName, final String pPropertyName, IUndoRedoBufferProvider undoRedoBufferProvider) {
        super(pDisplayName, pPropertyName, undoRedoBufferProvider);
        updatePreview();
    }

    private void updatePreview() {
        final PictureType pictureType = new PictureType(Perception.getPerception().getProperties().getColorOOBProperty(), mPreviewSize.getValue(), mPreviewSize.getValue());
        pictureType.setColor((x, y) -> {
            if (y > 50) {
                return Color.YELLOW;
            }
            if (x > 50) {
                return Color.BLUE;
            } else {
                return Color.GREEN;
            }
        });
        mPictureControl.setValue(pictureType, null, false);
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
}
