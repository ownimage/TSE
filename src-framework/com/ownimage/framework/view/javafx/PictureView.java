package com.ownimage.framework.view.javafx;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.IControl;
import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
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

	public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
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

		mUI.setOnMouseClicked((e) -> mouseClickedEvent(e));
		mUI.setOnMouseDragged((e) -> mouseDraggedEvent(e));
		mUI.setOnMousePressed((e) -> mousePressedEvent(e));
		mUI.setOnMouseReleased((e) -> mouseReleasedEvent(e));
		mUI.setOnScroll((e) -> scrollEvent(e));

		updatePicture();
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		if (pControl == mControl) {
			Platform.runLater(() -> {
				updatePicture();
				redrawGrafitti();
				resizeTopParent();
			});
		}
	}

	private UIEvent createMouseEvent(final EventType pEventType, final MouseEvent pME) {
		int width = mControl.getValue().getWidth();
		int height = mControl.getValue().getHeight();
		int x = (int) pME.getX();
		int y = (int) (height - pME.getY());
		UIEvent event = new UIEvent(pEventType, mControl, width, height, x, y, pME.isControlDown(), pME.isAltDown(), pME.isShiftDown());
		return event;
	}

	private UIEvent createScrollEvent(final ScrollEvent pSE) {
		int width = mControl.getValue().getWidth();
		int height = mControl.getValue().getHeight();
		int x = (int) pSE.getX();
		int y = (int) (height - pSE.getY());
		int scroll = (int) Math.signum(pSE.getDeltaY());
		UIEvent event = new UIEvent(EventType.Scroll, mControl, scroll, width, height, x, y, pSE.isControlDown(), pSE.isAltDown(), pSE.isShiftDown());
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
			mControl.uiEvent(event);
		}
	}

	private void mouseDraggedEvent(final MouseEvent pME) {
		UIEvent event = createMouseEvent(EventType.Drag, pME);
		mControl.uiEvent(event);
	}

	private void mousePressedEvent(final MouseEvent pME) {
		UIEvent event = createMouseEvent(EventType.MouseDown, pME);
		mControl.uiEvent(event);
	}

	private void mouseReleasedEvent(final MouseEvent pME) {
		UIEvent event = createMouseEvent(EventType.MouseUp, pME);
		mControl.uiEvent(event);
	}

	@Override
	public void redrawGrafitti() {
		mGraphicsContext.clearRect(0, 0, mImage.getWidth(), mImage.getHeight());
		mControl.drawGrafitti(mGrafittiImp);
	}

	private void resizeTopParent() {
		Parent parent = mUI;
		while (parent.getParent() != null) {
			parent = parent.getParent();
		}
		parent.getScene().getWindow().sizeToScene();
	}

	private void scrollEvent(final ScrollEvent pSE) {
		Framework.logEntry(mLogger);
		UIEvent event = createScrollEvent(pSE);
		mControl.uiEvent(event);
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
