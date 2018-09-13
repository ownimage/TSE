/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IGrafittiImp;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.event.UIEvent;
import com.ownimage.framework.view.event.UIEvent.EventType;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class PictureView extends ViewBase<PictureControl> implements IPictureView {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private Image mImage;

    private GrafittiImp mGrafittiImp;
    private GraphicsContext mGrafittiContext;

    private GrafittiImp mCursorImp;
    private GraphicsContext mCursorContext;

    private final StackPane mUI;

    public PictureView(final PictureControl pPictureControl) {
        super(pPictureControl);

        pPictureControl.addControlChangeListener(this);

        mUI = new StackPane();
        mUI.setAlignment(Pos.TOP_LEFT);
        mUI.setUserData(this); // stops the view being garbage collected
        mUI.setFocusTraversable(true);
        mUI.requestFocus();

        mUI.setOnMouseClicked(this::mouseClickedEvent);
        mUI.setOnMouseDragged(this::mouseDraggedEvent);
        mUI.setOnMousePressed(this::mousePressedEvent);
        mUI.setOnMouseReleased(this::mouseReleasedEvent);
        mUI.setOnMouseMoved(this::mouseMovedEvent);
        mUI.setOnScroll(this::scrollEvent);

        updatePicture();
    }

    @Override
    public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
        if (pControl == mControl) {
            runOnFXApplicationThread(() -> {
                updatePicture();
                mControl.drawGrafitti();
                resizeTopParent();
            });
        }
    }

    private UIEvent createMouseEvent(final EventType pEventType, final MouseEvent pME) {
        final int width = mControl.getValue().getWidth();
        final int height = mControl.getValue().getHeight();
        final int x = (int) pME.getX();
        final int y = (int) (height - pME.getY());
        final UIEvent event = UIEvent.createMouseEvent(pEventType, mControl, width, height, x, y, pME.isControlDown(), pME.isAltDown(), pME.isShiftDown());
        return event;
    }

    private UIEvent createScrollEvent(final ScrollEvent pSE) {
        final int width = mControl.getValue().getWidth();
        final int height = mControl.getValue().getHeight();
        final int x = (int) pSE.getX();
        final int y = (int) (height - pSE.getY());
        final int scroll = (int) Math.signum(pSE.getDeltaY());
        final UIEvent event = UIEvent.createMouseScrollEvent(EventType.Scroll, mControl, scroll, width, height, x, y, pSE.isControlDown(), pSE.isAltDown(), pSE.isShiftDown());
        return event;
    }

    @Override
    public Node getUI() {
        return mUI;
    }

    private void mouseClickedEvent(final MouseEvent pME) {
        Framework.logEntry(mLogger);
        if (pME.getClickCount() <= 2) {

            if (pME.getClickCount() == 1) {
                mLogger.finest("createMouseEvent Click");
                final UIEvent event = createMouseEvent(EventType.Click, pME);
                queueApplicationEvent(() -> mControl.uiEvent(event));
            }
            if (pME.getClickCount() == 2) {
                mLogger.finest("createMouseEvent DoubleClick");
                final UIEvent event = createMouseEvent(EventType.DoubleClick, pME);
                queueApplicationEvent(() -> mControl.uiEvent(event));
            }
        }
    }

    private void mouseDraggedEvent(final MouseEvent pME) {
        Framework.logEntry(mLogger);
        final UIEvent event = createMouseEvent(EventType.Drag, pME);
        queueApplicationEvent(() -> mControl.uiEvent(event));
    }

    private void mousePressedEvent(final MouseEvent pME) {
        final UIEvent event = createMouseEvent(EventType.MouseDown, pME);
        queueApplicationEvent(() -> mControl.uiEvent(event));
    }

    private void mouseReleasedEvent(final MouseEvent pME) {
        final UIEvent event = createMouseEvent(EventType.MouseUp, pME);
        queueApplicationEvent(() -> mControl.uiEvent(event));
    }

    private void mouseMovedEvent(final MouseEvent pME) {
        final UIEvent event = createMouseEvent(EventType.MouseMoved, pME);
        queueApplicationEvent(() -> mControl.uiEvent(event));
    }

    @Override
    public void drawGrafitti(final Consumer<IGrafittiImp> pGrafitti) {
        runOnFXApplicationThread(() -> {
            mGrafittiContext.clearRect(0, 0, mImage.getWidth(), mImage.getHeight());
            pGrafitti.accept(mGrafittiImp);
        });
    }

    @Override
    public void drawCursor(final Consumer<IGrafittiImp> pGrafitti) {
        runOnFXApplicationThread(() -> {
            mCursorContext.clearRect(0, 0, mImage.getWidth(), mImage.getHeight());
            pGrafitti.accept(mCursorImp);
        });
    }

    @Override
    public void updateGrafitti(final Consumer<IGrafittiImp> pGrafitti) {
        runOnFXApplicationThread(() -> pGrafitti.accept(mGrafittiImp));
    }


    private void resizeTopParent() {
        Parent parent = mUI;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        if (parent.getScene() != null && parent.getScene().getWindow() != null) {
            parent.getScene().getWindow().sizeToScene();
        }
    }

    private void scrollEvent(final ScrollEvent pSE) {
        Framework.logEntry(mLogger);
        final UIEvent event = createScrollEvent(pSE);
        queueApplicationEvent(() -> mControl.uiEvent(event));
        Framework.logExit(mLogger);
    }

    private void updatePicture() {
        final BufferedImage bufferedImage = mControl.getValue().getBufferedImage();
        mImage = SwingFXUtils.toFXImage(bufferedImage, null);

        final ImageView imageView = new ImageView();
        imageView.setImage(mImage);

        final double width = mImage.getWidth();
        final double height = mImage.getHeight();

        final Canvas grafittiCanvas = new Canvas(width, height);
        mGrafittiContext = grafittiCanvas.getGraphicsContext2D();
        mGrafittiImp = new GrafittiImp(mGrafittiContext, width, height);

        final Canvas cursorCanvas = new Canvas(width, height);
        mCursorContext = cursorCanvas.getGraphicsContext2D();
        mCursorImp = new GrafittiImp(mCursorContext, width, height);

        mUI.getChildren().clear();
        mUI.getChildren().addAll(imageView, grafittiCanvas, cursorCanvas);
    }

}
