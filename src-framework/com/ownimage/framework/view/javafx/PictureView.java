package com.ownimage.framework.view.javafx;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IGrafittiImp;
import com.ownimage.framework.view.IPictureView;
import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.framework.view.event.UIEvent;
import com.ownimage.framework.view.event.UIEvent.EventType;

import javafx.application.Platform;
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

public class PictureView extends ViewBase<PictureControl> implements IPictureView {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private Image mImage;
    private GrafittiImp mGrafittiImp;

    private final StackPane mUI;
    private GraphicsContext mGraphicsContext;

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
            Platform.runLater(() -> {
                updatePicture();
                mControl.redrawGrafitti();
                resizeTopParent();
            });
        }
    }

    private UIEvent createMouseEvent(final EventType pEventType, final MouseEvent pME) {
        int width = mControl.getValue().getWidth();
        int height = mControl.getValue().getHeight();
        int x = (int) pME.getX();
        int y = (int) (height - pME.getY());
        UIEvent event = UIEvent.createMouseEvent(pEventType, mControl, width, height, x, y, pME.isControlDown(), pME.isAltDown(), pME.isShiftDown());
        return event;
    }

    private UIEvent createScrollEvent(final ScrollEvent pSE) {
        int width = mControl.getValue().getWidth();
        int height = mControl.getValue().getHeight();
        int x = (int) pSE.getX();
        int y = (int) (height - pSE.getY());
        int scroll = (int) Math.signum(pSE.getDeltaY());
        UIEvent event = UIEvent.createMouseScrollEvent(EventType.Scroll, mControl, scroll, width, height, x, y, pSE.isControlDown(), pSE.isAltDown(), pSE.isShiftDown());
        return event;
    }

    @Override
    public Node getUI() {
        return mUI;
    }

    private void mouseClickedEvent(final MouseEvent pME) {
        if (pME.getClickCount() <= 2) {

            IUIEvent event = null;
            if (pME.getClickCount() == 1) {
                event = createMouseEvent(EventType.Click, pME);
            }
            if (pME.getClickCount() == 2) {
                event = createMouseEvent(EventType.DoubleClick, pME);
            }
            ApplicationEventQueue.getInstance().queueEvent(event, mControl);
        }
    }

    private void mouseDraggedEvent(final MouseEvent pME) {
        UIEvent event = createMouseEvent(EventType.Drag, pME);
        ApplicationEventQueue.getInstance().queueEvent(event, mControl);
    }

    private void mousePressedEvent(final MouseEvent pME) {
        UIEvent event = createMouseEvent(EventType.MouseDown, pME);
        ApplicationEventQueue.getInstance().queueEvent(event, mControl);
    }

    private void mouseReleasedEvent(final MouseEvent pME) {
        UIEvent event = createMouseEvent(EventType.MouseUp, pME);
        ApplicationEventQueue.getInstance().queueEvent(event, mControl);
    }

    private void mouseMovedEvent(final MouseEvent pME) {
        UIEvent event = createMouseEvent(EventType.MouseMoved, pME);
        ApplicationEventQueue.getInstance().queueEvent(event, mControl);
        mLogger.fine(() -> String.format("ApplicationEventQueue.getInstance().getQueueSize() = ", ApplicationEventQueue.getInstance().getQueueSize()));
    }

    @Override
    public void redrawGrafitti(Consumer<IGrafittiImp> pGrafitti) {
        runOnFXApplicationThread(() -> {
            mGraphicsContext.clearRect(0, 0, mImage.getWidth(), mImage.getHeight());
            pGrafitti.accept(mGrafittiImp);
        });
    }

    @Override
    public void updateGrafitti(Consumer<IGrafittiImp> pGrafitti) {
        runOnFXApplicationThread(() -> {
            pGrafitti.accept(mGrafittiImp);
        });
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
        UIEvent event = createScrollEvent(pSE);
        ApplicationEventQueue.getInstance().queueEvent(event, mControl);
        Framework.logExit(mLogger);
    }

    private void updatePicture() {
        BufferedImage bufferedImage = mControl.getValue().getBufferedImage();
        mImage = SwingFXUtils.toFXImage(bufferedImage, null);

        ImageView imageView = new ImageView();
        imageView.setImage(mImage);

        double width = mImage.getWidth();
        double height = mImage.getHeight();

        Canvas canvas = new Canvas(width, height);
        mGraphicsContext = canvas.getGraphicsContext2D();
        mGrafittiImp = new GrafittiImp(mGraphicsContext, width, height);
        mUI.getChildren().clear();
        mUI.getChildren().addAll(imageView, canvas);
    }

}
