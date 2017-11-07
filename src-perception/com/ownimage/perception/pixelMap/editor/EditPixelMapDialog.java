/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2014 ownimage.com, Keith Hart
 */
package com.ownimage.perception.pixelMap.editor;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ownimage.perception.Perception;
import com.ownimage.perception.control.combo.PictureControl;
import com.ownimage.perception.control.controller.Control;
import com.ownimage.perception.control.controller.ControlSelector;
import com.ownimage.perception.control.controller.IControl;
import com.ownimage.perception.control.controller.IControlPrimative;
import com.ownimage.perception.control.controller.IMouseControl;
import com.ownimage.perception.control.group.ControlContainer;
import com.ownimage.perception.control.group.ControlContainerDialog;
import com.ownimage.perception.control.model.DoubleControl;
import com.ownimage.perception.math.KMath;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.math.Rectangle;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelAction;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelChain.Thickness;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.SegmentFactory.SegmentType;
import com.ownimage.perception.transform.BaseTransform;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CropTransform;
import com.ownimage.perception.transform.GraphicsHelper;
import com.ownimage.perception.transform.cannyEdge.PreviewPosition;
import com.ownimage.perception.ui.client.ICUI;
import com.ownimage.perception.ui.factory.GUIFactory;
import com.ownimage.perception.ui.server.DynamicUI;
import com.ownimage.perception.ui.server.INamedTabsUISource;
import com.ownimage.perception.undo.IUndoRedoBuffer;
import com.ownimage.perception.util.IPicture;
import com.ownimage.perception.util.Picture;
import com.ownimage.perception.util.Version;
import com.ownimage.perception.util.logging.PerceptionLogger;

public class EditPixelMapDialog extends ControlContainerDialog {

	public enum EditorType {
		Pixel, Segment
	}

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = EditPixelMapDialog.class.getName();

	public final static Logger mLogger = Logger.getLogger(mClassname);

	public final static long serialVersionUID = 1L;;

	private boolean mIsInitialized = false;
	private EditorType mType;

	private final IControlPrimative<Double> mWidth = new Control<Double>("Width", "width", new DoubleControl(0.1));

	private EditPixelMapControlSelectorBase mInitialControlSelector;
	private EditPixelMapControlSelectorBase mControlSelectorDelegate; // this is needed to keep track of the delegate so that it can be reset on a close dialog

	private final Vector<IVertex> mVisibleVertexes = new Vector<IVertex>();

	private Properties mUndo;

	private Picture mCache;

	// context variables
	private PixelChain mSelectedPixelChain;
	private IVertex mSelectedVertex;;
	private ISegment mSelectedSegment;
	private List<ISegment> mSegmentsAtSelectedVertexPosition = new Vector<ISegment>();
	private final ControlContainer mSegmentColorsTab = new ControlContainer(this, "Segment", "segment");
	private final ControlContainer mContextTab = new ControlContainer(this, "Context", "context");

	// UI Tabs
	private INamedTabsUISource mTabs = new DynamicUI.NamedTabsUISource();
	private final ControlContainer mPositionTab = new ControlContainer(this, "Position", "position");
	private final ControlContainer mPixelColorsTab = new ControlContainer(this, "Pixels", "pixels");

	// Position Tab
	private final PictureControl mPreviewPicture = addControl(new PictureControl(this, "Preview", "preview", 100, 100, 1000, 100, getControlSelector()));;
	private final PreviewPosition mPreviewPosition = addControl(new PreviewPosition(getTransform(), "Edit Preview", "editPreview"));
	private final IControlPrimative<Integer> mZoom = createIntegerControl("Zoom", "zoom", 4, 1, 16, 1);
	private final IControlPrimative<Boolean> mUseCachePreviewDraw = createBooleanControl("Quick Background Draw", "quickBackgroundDraw", true);

	// PixelColor Tab
	private final IControlPrimative<String> mPixelEditModeControl;
	private final IControlPrimative<Boolean> mThinButton;
	private final IControlPrimative<Boolean> mRegenerateLinesButton;

	// SegmentColor Tab
	private final IControlPrimative<Boolean> mShowGrafitti = createBooleanControl("Show Graffiti", "showGraffiti", true);
	private final IControlPrimative<Double> mBackgroundOpacity = createDoubleControl("Background Opacity", "BackgroundOpacity", 1.0d, new DoubleControl.DoubleMetaModel(0.0d, 1.0d, 0.1d, DoubleControl.BOTH));
	private final IControlPrimative<Color> mNodeColor = createColorControl("Node color", "nodeColor", Color.RED);
	private final IControlPrimative<Color> mEdgeColor = createColorControl("Edge color", "edgeColor", Color.GREEN);
	private final IControlPrimative<Color> mVertexColor = createColorControl("Vertex color", "vertexColor", Color.RED);
	private final IControlPrimative<Color> mVertexSelectedColor = createColorControl("Selected Vertex color", "vertexSelectedColor", Color.BLUE);
	private final IControlPrimative<Double> mVertexThickness = createDoubleControl("Vertex thickness", "vertexThickness", 1.0d, 1.0d, 10.0d);
	private final IControlPrimative<Color> mSegmentColor = createColorControl("Segment color", "segmentColor", Color.ORANGE);
	private final IControlPrimative<Color> mSegmentSelectedColor = createColorControl("Selected segment color", "selectedSegmentColor", Color.YELLOW);
	private final IControlPrimative<Color> mSegmentAttachedColor = createColorControl("Attached segment color", "attachedSegmentColor", Color.BLUE);
	private final IControlPrimative<Double> mSegmentThickness = createDoubleControl("Segment thickness", "segmentThickness", 1.0d, 1.0d, 10.0d);
	private final IControlPrimative<Color> mControlLineColor = createColorControl("Control line color", "controlLineColor", Color.RED);
	private final IControlPrimative<Color> mControlPointColor = createColorControl("Control point color", "controlPointColor", Color.BLACK);
	// private final IControlPrimative<Color> mControlPointSelectedColor = mSegmentColorsTab.createColorControl("Selected control point color", "controlPointSelectedColor", Color.GRAY);
	// private final IControlPrimative<Double> mControlPointThickness = mSegmentColorsTab.createDoubleControl("ControlPoint thickness", "controlPointThickness", 1.0d, 1.0d, 10.0d);

	// Context Tab
	private final IControlPrimative<String> mPixelChainThicknessControl = createStringControl("Pixel Chain Thickness", "pixelChainThickness", Thickness.Normal.toString(), Thickness.values());
	private final IControlPrimative<String> mSelectedSegmentTypeControl = createStringControl("SegmentType", "segmentType", SegmentType.Straight.toString(), SegmentType.values());;
	private final IControlPrimative<Boolean> mBackgroundRedrawButton = createButtonControl("Redraw Background", "redrawBackground");

	private ICUI mUI;
	private EditPixelMapPicture mPictureUI;

	public static EditPixelMapDialog createPixelEditorDialog(final CannyEdgeTransform pCannyEdgeTransform, final String pDisplayName, final String pPropertyName) {
		final EditPixelMapDialog editor = new EditPixelMapDialog(pCannyEdgeTransform, pDisplayName, pPropertyName);
		editor.mType = EditorType.Pixel;
		editor.mInitialControlSelector = new PictureControlSelector(editor);
		return editor;
	}

	public static EditPixelMapDialog createSegmentEditDialog(final CannyEdgeTransform pCannyEdgeTransform, final String pDisplayName, final String pPropertyName) {
		final EditPixelMapDialog editor = new EditPixelMapDialog(pCannyEdgeTransform, pDisplayName, pPropertyName);
		editor.mType = EditorType.Segment;
		editor.mInitialControlSelector = new PictureControlSelector(editor);
		return editor;
	}

	public EditPixelMapDialog(final CannyEdgeTransform pTransform, final String pDisplayName, final String pPropertyName) {
		super(pTransform, pDisplayName, pPropertyName);

		if (pTransform == null) {
			throw new IllegalArgumentException("pTransform must not be null.");
		}

		if (pTransform == null || pDisplayName == null || pPropertyName == null) {
			final StringBuffer msg = new StringBuffer();
			msg.append("EditPixelMap constructor ... all the arguments must be non null {\n");
			msg.append("pTransform: " + pTransform);
			msg.append("pDisplayName: " + pDisplayName);
			msg.append("pPropertyName: " + pPropertyName);
			msg.append("}\n");
			throw new IllegalArgumentException(msg.toString());
		}

		mPreviewPosition.addMouseControls(getTransform());

		// Position Tab and controls
		mPositionTab.addControl(mPreviewPicture.getSizeControl());
		mPositionTab.addControl(mZoom);
		mPositionTab.addControl(mPreviewPosition);
		mPositionTab.addControl(mUseCachePreviewDraw);

		// mPixelColorsTab
		mPixelEditModeControl = createStringControl("Pixel Edit Mode", "pixelEditMode", PixelAction.Action.On.toString(), PixelAction.Action.values());
		mThinButton = createButtonControl("Thin", "thin");
		mRegenerateLinesButton = createButtonControl("Regenerate Lines", "regenerateLines");

		// mPixelColorsTab
		mPixelColorsTab.addControl(mPreviewPicture.getSizeControl());
		mPixelColorsTab.addControl(mZoom);
		mPixelColorsTab.addControl(mPreviewPosition);
		mPixelColorsTab.addControl(mShowGrafitti);
		mPixelColorsTab.addControl(mUseCachePreviewDraw);
		mPixelColorsTab.addControl(mBackgroundOpacity);
		mPixelColorsTab.addControl(mEdgeColor);
		mPixelColorsTab.addControl(getTransform().getLineToleranceControl());
		mPixelColorsTab.addControl(getTransform().getLineCurvePreferenceControl());
		mPixelColorsTab.addControl(mPixelEditModeControl);
		mPixelColorsTab.addControl(mThinButton);
		mPixelColorsTab.addControl(mRegenerateLinesButton);

		// mSegmentColorsTab
		mSegmentColorsTab.addControl(mShowGrafitti);
		mSegmentColorsTab.addControl(mBackgroundOpacity);
		mSegmentColorsTab.addControl(mNodeColor);
		mSegmentColorsTab.addControl(mEdgeColor);
		mSegmentColorsTab.addControl(mVertexColor);
		mSegmentColorsTab.addControl(mVertexSelectedColor);
		mSegmentColorsTab.addControl(mVertexThickness);
		mSegmentColorsTab.addControl(mSegmentColor);
		mSegmentColorsTab.addControl(mSegmentSelectedColor);
		mSegmentColorsTab.addControl(mSegmentAttachedColor);
		mSegmentColorsTab.addControl(mSegmentThickness);
		mSegmentColorsTab.addControl(mControlLineColor);
		mSegmentColorsTab.addControl(mControlPointColor);

		// Context Tab and Controls
		mContextTab.addControl(mShowGrafitti);
		mContextTab.addControl(mPixelChainThicknessControl);
		mContextTab.addControl(mSelectedSegmentTypeControl);
		mContextTab.addControl(mBackgroundRedrawButton);
		mPixelChainThicknessControl.setEnabled(false);
		mSelectedSegmentTypeControl.setEnabled(false);
	}

	@Override
	public void actionCancel() {
		mLogger.entering(mClassname, "actionCancel");

		getTransform().read(mUndo, "");
		mUndo = null;
		getPixelMap().indexSegments();
		getTransform().hideStatus();
		getTransform().repaintTransform(false);

		mLogger.exiting(mClassname, "actionCancel");
	}

	@Override
	public void actionOK() {
		mLogger.entering(mClassname, "actionOK");

		mUndo = null;
		getPixelMap().indexSegments();
		getTransform().hideStatus();
		getTransform().repaintTransform(false);

		mLogger.exiting(mClassname, "actionOK");
	}

	public void addPixelChain(final PixelChain pPixelChain) {
		getPixelMap().addPixelChains(pPixelChain);
	}

	private void addVertexes(final Vector<IVertex> pVisibleVertexes, final Pixel pOrigin, final Pixel pTopLeft) {
		getPixelMap().addVertexes(mVisibleVertexes, pOrigin, pTopLeft);
	}

	private void calcSegmentsAtSelectedVertexPosition() {
		mLogger.entering(mClassname, "getSegmentsAtCurrentVertexPosition");

		// note a TreeSet is used here as it only allows 1 null, this is removed (if it exists) at the end. This is simplier than doing all of the null tests.
		mSegmentsAtSelectedVertexPosition = new Vector<ISegment>();

		if (getSelectedVertex() != null) {
			for (final IVertex vertex : getVertexesAtSelectedVertexPosition()) {
				if (vertex.getStartSegment() != null) {
					mSegmentsAtSelectedVertexPosition.add(vertex.getStartSegment());
				}
				if (vertex.getEndSegment() != null) {
					mSegmentsAtSelectedVertexPosition.add(vertex.getEndSegment());
				}
			}
		}

		mLogger.exiting(mClassname, "getSelectedControlPoint");
	}

	public void calcVisibleVertexes() {
		mLogger.entering(mClassname, "calcVisibleVertexes");
		mVisibleVertexes.removeAllElements();
		final Pixel origin = getOriginPixel();
		final Pixel topLeft = getTopRightPixel();

		addVertexes(mVisibleVertexes, origin, topLeft);

		mLogger.exiting(mClassname, "calcVisibleVertexes");
	}

	public void changeControlSelector(final EditPixelMapControlSelectorBase pFrom, final EditPixelMapControlSelectorBase pTo) {
		mLogger.entering(mClassname, "changeControlSelector");
		PerceptionLogger.logParams(mLogger, "pFrom, pTo", pFrom, pTo);

		getControlSelector().removeDelegate(pFrom);
		getControlSelector().delegate(pTo);
		setControlSelectorDelegate(pTo);

		mLogger.exiting(mClassname, "changeControlSelector");
	}

	/**
	 * changePreview is called when the position or size of the preview image needs to be changed. This method will set the size on the mPreviewPosition (based on the zoom), and grafitti the
	 * Transform.
	 * 
	 * @param pForceRefresh
	 *            forces a refresh of the background image. Normally the background image is only refreshed if the dialog is visible. But there are circumstances where the refresh needs to happen when
	 *            the dialog is not visible, i.e. immediately before the showDialog() call to set up the preview.
	 */
	private void changePreview(final boolean pForceRefresh) {
		mLogger.entering(mClassname, "changePreview");

		if (isInitialized() && (isVisible() || pForceRefresh)) {
			System.out.println("start");
			final double height = (double) mPreviewPicture.getSize() / getPixelMap().getHeight() / mZoom.getInt();
			final double width = height / getPixelMap().getAspectRatio();
			mPreviewPosition.setSize(width, height);
			graffitiTransform();

			if (mUseCachePreviewDraw.getBoolean()) {
				final int maxX = mPreviewPicture.getPictureControl().getValue().getWidth();
				final int maxY = mPreviewPicture.getPictureControl().getValue().getHeight();

				for (int x = 0; x < maxX; x++) {
					for (int y = 0; y < maxY; y++) {
						final int cacheX = (int) (getTransformWidth() * (getOriginPoint().getX() + x * width / maxX));
						final int cacheY = (int) (getTransformHeight() * (getOriginPoint().getY() + y * height / maxY));
						final Color color = mCache.getColor(cacheX, cacheY);
						mPreviewPicture.getPictureControl().getValue().setColor(x, y, color);
					}
				}
				mPreviewPicture.getPictureControl().fireControlEvent(true);

			} else {
				final CropTransform cropTransform = new CropTransform(Perception.getInstance());
				cropTransform.setPreviousTransform(getTransform());
				cropTransform.setCrop(getBounds(), true);
				cropTransform.renderASynchronous(mPreviewPicture.getPictureControl().getValue());

			}
		}
		calcVisibleVertexes();
		mPreviewPicture.getPictureControl().fireControlEvent(false);
		mLogger.exiting(mClassname, "changePreview");
	}

	@Override
	public void controlChangeEvent(final IControl pControl, final boolean pIsMutating) {
		mLogger.entering(mClassname, "controlChangeEvent");
		PerceptionLogger.logParams(mLogger, "pControl, pIsMutating", pControl, pIsMutating);
		if (!isMutating()) {
			setMutating(true);

			try {
				if (pControl == mShowGrafitti) {
					graffiti();
					return;
				}

				if (pControl == mPixelChainThicknessControl && mSelectedPixelChain != null) {
					final Thickness thickness = Thickness.valueOf(mPixelChainThicknessControl.getString());
					mSelectedPixelChain.setThickness(thickness);
					mLogger.severe("Set thickness");
					return;
				}

				if (pControl == mSelectedSegmentTypeControl && mSelectedSegment != null) {
					mLogger.severe("Set type");
					mSelectedSegment = SegmentFactory.changeSegmentType(mSelectedSegment, SegmentType.valueOf(mSelectedSegmentTypeControl.getValue()));
					graffiti();
					return;
				}

				if (!pIsMutating && pControl.isOneOf(mPreviewPosition, mZoom, mPreviewPicture, mPreviewPicture.getSizeControl(), mBackgroundOpacity, mBackgroundRedrawButton)) {
					changePreview(false);
					return;
				}

				if (!pIsMutating && pControl != mPreviewPicture.getPictureControl()) {
					changePreview(false);
					graffiti();
				}

				if (pControl == mThinButton) {
					getPixelMap().process02_thin(null);
					graffiti();
				}

				if (pControl == mRegenerateLinesButton) {
					getTransform().postProcess();
					// getTransform().reapproximate();

					getPixelMap().process04a_removeLoneNodes();

					changePreview(false);
					graffiti();

				}

				if (pControl == mUseCachePreviewDraw) {
					changePreview(true);
				}

			} finally {
				setMutating(false);
				setValues();
			}
		}
		mLogger.exiting(mClassname, "controlChangeEvent");
	}

	@Override
	public ICUI createUI() {
		mLogger.entering(mClassname, "createUI");
		if (mUI == null) {

			final DynamicUI.HorizontalFlowUISource main = new DynamicUI.HorizontalFlowUISource();
			mTabs = new DynamicUI.NamedTabsUISource();

			// if (mType == EditorType.Segment) {
			mTabs.addTab("Position", mPositionTab);
			mTabs.addTab("Segment Colors", mSegmentColorsTab);
			mTabs.addTab("Context", mContextTab);
			// }
			// if (mType == EditorType.Pixel) {
			mTabs.addTab("Pixel Colors", mPixelColorsTab);
			// }

			mPictureUI = new EditPixelMapPicture(this);
			main.setLeftUI(mPictureUI);
			main.setRightUI(mTabs);

			mLogger.exiting(mClassname, "createUI");

			mUI = GUIFactory.getInstance().createUI(main);
		}
		return mUI;
	}

	public double getAspectRatio() {
		final double aspectRatio = getPixelMap().getAspectRatio();
		mLogger.fine("aspectRatio = " + aspectRatio);
		return aspectRatio;
	}

	private Rectangle getBounds() {
		final Rectangle bounds = new Rectangle(getOriginPoint(), getTopRightPoint());
		return bounds;
	}

	public Color getControlLineColor() {
		return mControlLineColor.getColor();
	}

	public Color getControlPointColor() {
		return mControlPointColor.getColor();
	}

	@Override
	public ControlSelector getControlSelector() {
		return getTransform().getControlSelector();
	}

	public EditPixelMapControlSelectorBase getControlSelectorDelegate() {
		return mControlSelectorDelegate;
	}

	public double getCrossHairSize() {
		mLogger.entering(mClassname, "getCrossHairSize");
		final double crossHairSize = 2.0d / getTransformHeight();
		mLogger.exiting(mClassname, "getCrossHairSize", crossHairSize);
		return crossHairSize;

	}

	public Color getEdgeColor() {
		return mEdgeColor.getColor();
	}

	public double getHeight() {
		final double height = mPreviewPosition.getHeight();
		mLogger.fine("height = " + height);
		return height;
	}

	public Color getNodeColor() {
		return mNodeColor.getColor();
	}

	public Pixel getOriginPixel() {
		mLogger.entering(mClassname, "getOriginPixel");
		final Pixel originPixel = getPixelMap().getPixelAt(getOriginPoint());
		mLogger.exiting(mClassname, "getOriginPixel", originPixel);
		return originPixel;
	}

	public Point getOriginPoint() {
		mLogger.entering(mClassname, "getOriginPoint");
		final Point originPoint = getPreviewPosition().getPoint();
		mLogger.exiting(mClassname, "getOriginPoint", originPoint);
		return originPoint;
	}

	public IControlPrimative<IPicture> getPicture() {
		return mPreviewPicture.getPictureControl();
	}

	private EditPixelMapPicture getPictureUI() {
		return mPictureUI;
	}

	/**
	 * Gets the pixel at the specified position. The pX and pY are relative to the current view defined by x,y, width and height of the view.
	 * 
	 * @param pX
	 * @param pY
	 * @return the pixel
	 */
	public Pixel getPixel(final double pX, final double pY) {
		mLogger.entering(mClassname, "getPixel");
		PerceptionLogger.logParams(mLogger, "pX, pY", pX, pY);

		final double x = getX() + pX * getWidth();
		final double y = getY() + pY * getHeight();
		final Pixel pixel = getPixelMap().getPixelAt(x, y);

		mLogger.exiting(mClassname, "getPixel", pixel);
		return pixel;
	}

	public PixelAction.Action getPixelEditMode() {
		return PixelAction.Action.valueOf(mPixelEditModeControl.getValue());
	}

	public PixelMap getPixelMap() {
		return getTransform().getPixelMap();
	}

	public Point getPosition() {
		mLogger.entering(mClassname, "getPosition");
		final Point point = mPreviewPosition.getPoint();
		mLogger.exiting(mClassname, "getPosition", point);
		return point;
	}

	public PictureControl getPreviewPicture() {
		return mPreviewPicture;
	}

	public PreviewPosition getPreviewPosition() {
		return mPreviewPosition;
	}

	public Color getSegmentAttachedColor() {
		return mSegmentAttachedColor.getColor();
	}

	public Color getSegmentColor() {
		return mSegmentColor.getColor();
	}

	/**
	 * Gets the segments at current vertex position. This method returns a list of all of the segments that are attached at the selected vertex position. Note that the current vertex will only have
	 * two segments attached to it, but there may be other vertexes at the same position.
	 * 
	 * @return the segments at current vertex position
	 */
	public List<ISegment> getSegmentsAtSelectedVertexPosition() {
		return mSegmentsAtSelectedVertexPosition;
	}

	public Color getSegmentSelectedColor() {
		return mSegmentSelectedColor.getColor();
	}

	public double getSegmentThickness() {
		return mSegmentThickness.getDouble();
	}

	public PixelChain getSelectedPixelChain() {
		mLogger.entering(mClassname, "getSelectedPixelChain");
		mLogger.exiting(mClassname, "getSelectedPixelChain", mSelectedPixelChain);
		return mSelectedPixelChain;
	}

	public ISegment getSelectedSegment() {
		mLogger.entering(mClassname, "getSelectedSegment");
		mLogger.exiting(mClassname, "getSelectedSegment", mSelectedSegment);
		return mSelectedSegment;
	}

	public IVertex getSelectedVertex() {
		mLogger.entering(mClassname, "getSelectedVertex");
		mLogger.exiting(mClassname, "getSelectedVertex", mSelectedVertex);
		return mSelectedVertex;
	}

	public Pixel getTopRightPixel() {
		mLogger.entering(mClassname, "getTopLeftPixel");
		final Pixel originPixel = getOriginPixel();

		final int x = originPixel.getX() + (int) (getPreviewPicture().getSize() / getZoom() * getAspectRatio());
		final int y = originPixel.getY() + getPreviewPicture().getSize() / getZoom();

		if (mLogger.isLoggable(Level.FINE)) {
			mLogger.fine("originPixel.getX() = " + originPixel.getX());
			mLogger.fine("originPixel.getY() = " + originPixel.getY());
			mLogger.fine("getAspectRatio() = " + getAspectRatio());
			mLogger.fine("getPreviewPicture().getSize() = " + getPreviewPicture().getSize());
			mLogger.fine("getZoom() = " + getZoom());
			mLogger.fine("getTransformWidth() = " + getTransformWidth());
			mLogger.fine("getTransformHeight() = " + getTransformHeight());
			mLogger.fine("x = " + x);
			mLogger.fine("y = " + y);
		}

		final Pixel pixel = getPixelMap().getPixelAt(x, y);
		mLogger.exiting(mClassname, "getTopLeftPixel", pixel);
		return pixel;
	}

	public Point getTopRightPoint() {
		final Point originPoint = getOriginPoint();

		final double x = originPoint.getX() + getWidth();
		final double y = originPoint.getY() + getHeight();

		return new Point(x, y);
	}

	@Override
	public CannyEdgeTransform getTransform() {
		return (CannyEdgeTransform) super.getTransform();
	}

	public int getTransformHeight() {
		final int transformHeight = getTransform().getHeight();
		mLogger.fine("transformHeight = " + transformHeight);
		return transformHeight;
	}

	public int getTransformWidth() {
		final int transformWidth = getTransform().getWidth();
		mLogger.fine("transformWidth = " + transformWidth);
		return transformWidth;
	}

	public EditorType getType() {
		return mType;
	}

	private ICUI getUI() {
		return mUI;
	}

	public IUndoRedoBuffer getUndoRedoBuffer() {
		return getPixelMap().getUndoRedoBuffer();
	}

	public Color getVertexColor() {
		return mVertexColor.getColor();
	}

	public Vector<IVertex> getVertexesAtSelectedVertexPosition() {
		mLogger.entering(mClassname, "getVertexesAtSelectedVertexPosition");
		final Vector<IVertex> vertexes = new Vector<IVertex>();

		if (getSelectedVertex() != null) {
			for (final IVertex vertex : getVisibleVertexes()) {
				if (getSelectedVertex().samePosition(vertex)) {
					vertexes.add(vertex);
				}
			}
		}

		mLogger.exiting(mClassname, "getVertexesAtSelectedVertexPosition", vertexes);
		return vertexes;
	}

	public Color getVertexSelectedColor() {
		return mVertexSelectedColor.getColor();
	}

	public double getVertexThickness() {
		return mVertexThickness.getDouble();
	}

	public Vector<IVertex> getVisibleVertexes() {
		return mVisibleVertexes;
	}

	public double getWidth() {
		final double width = mPreviewPosition.getWidth();
		mLogger.fine("width = " + width);
		return width;
	}

	public IControlPrimative<Double> getWidthControl() {
		return mWidth;
	}

	public double getX() {
		mLogger.entering(mClassname, "getX");
		final double x = getPosition().getX();
		mLogger.exiting(mClassname, "getX");
		return x;
	}

	public double getY() {
		mLogger.entering(mClassname, "getY");
		final double y = getPosition().getY();
		mLogger.exiting(mClassname, "getY", y);
		return y;
	}

	public int getZoom() {
		final int zoom = mZoom.getInt();
		mLogger.fine("zoom = " + zoom);
		return zoom;
	}

	/**
	 * Forces a redraw of the dialog's picture grafitti.
	 */
	public void graffiti() {
		mPreviewPicture.getPictureControl().fireControlEvent(false);
	}

	@Override
	public void graffiti(final GraphicsHelper pGraphics) {
		mLogger.entering(mClassname, "graffiti(GraphicsHelper pGraphics)");
		final EPMDGraphicsHelper graphicsHelper = new EPMDGraphicsHelper(this, pGraphics);

		if (mType == EditorType.Pixel || mShowGrafitti.getValue()) {

			graphicsHelper.graffitiPixels();
		}

		if (mType == EditorType.Segment || mShowGrafitti.getValue()) {

			final Point halfPixel = getPixelMap().getUHVWHalfPixel();
			pGraphics.translate(halfPixel);

			graphicsHelper.graffitiPixelChains();
			graphicsHelper.graffitiVisibleVertexes();

			if (getControlSelectorDelegate() != null) {
				getControlSelectorDelegate().graffiti(graphicsHelper);
			}
		}

		if (getControlSelectorDelegate() != null) {
			getControlSelectorDelegate().graffitiName(graphicsHelper);
		}
		mLogger.exiting(mClassname, "graffiti(GraphicsHelper pGraphics)");
	}

	private void graffitiTransform() {
		getTransform().graffiti();
	}

	public void graffitiTransform(final GraphicsHelper pGraphics) {
		getPreviewPosition().graffiti(pGraphics);
	}

	public void grafitiCursor() {
		if (getPictureUI() != null) {
			getPictureUI().grafittiCursor();
		}
	}

	public void grafittiCursor(final GraphicsHelper pGraphics) {
		final EditPixelMapControlSelectorBase delegate = getControlSelectorDelegate();
		if (delegate != null) {
			delegate.graffitiCursor();
		}
	}

	@Override
	public boolean isControlSelected(final IMouseControl pControl) {
		return getTransform().getControlSelector().isControlSelected(pControl);
	}

	public boolean isInitialized() {
		return mIsInitialized;
	}

	public void setControlSelectorDelegate(final EditPixelMapControlSelectorBase pControlSelectorDelegate) {
		mControlSelectorDelegate = pControlSelectorDelegate;
	}

	public void setInitialized() {
		mLogger.entering(mClassname, "setInitialized");

		mIsInitialized = true;
		changePreview(false);

		mLogger.exiting(mClassname, "setInitialized");
	}

	public void setSelectedPixelChain(final PixelChain pSelectedPixelChain) {
		mLogger.severe("setSelectedPixelChain");

		mSelectedPixelChain = pSelectedPixelChain;
		mPixelChainThicknessControl.setEnabled(mSelectedPixelChain != null);
		mPixelChainThicknessControl.setValue(mSelectedPixelChain.getThickness().toString());
	}

	public void setSelectedSegment(final ISegment pSegment) {
		mLogger.entering(mClassname, "setSelectedSegment");
		PerceptionLogger.logParams(mLogger, "pSegment", pSegment);

		if (pSegment == null) {
			throw new IllegalArgumentException("pSegment must not be null");
		}

		try {
			setMutating(true);
			mSelectedSegment = pSegment;
			mSelectedSegmentTypeControl.setValue(mSelectedSegment.getSegmentType().toString());
			mSelectedSegmentTypeControl.setEnabled(true);
			setSelectedPixelChain(mSelectedSegment.getPixelChain());
		} finally {
			setMutating(false);
		}

		mLogger.exiting(mClassname, "setSelectedSegment");
	}

	public void setSelectedSegmentNull() {
		mLogger.entering(mClassname, "setSelectedSegmentNull");
		mSelectedSegment = null;
		mSelectedSegmentTypeControl.setEnabled(false);
		calcSegmentsAtSelectedVertexPosition();

		mLogger.exiting(mClassname, "setSelectedSegmentNull");
	}

	public void setSelectedVertex(final IVertex pVertex) {
		mLogger.entering(mClassname, "setSelectedVertex");
		PerceptionLogger.logParams(mLogger, "pVertex", pVertex);

		if (pVertex == null) {
			throw new IllegalArgumentException("pVertex must not be null");
		}

		mSelectedVertex = pVertex;
		setSelectedPixelChain(mSelectedVertex.getPixelChain());
		calcSegmentsAtSelectedVertexPosition();

		mLogger.exiting(mClassname, "setSelectedVertex");
	}

	public void setSelectedVertexNull() {
		mLogger.entering(mClassname, "setSelectedVertexNull");
		mSelectedVertex = null;
		mLogger.exiting(mClassname, "setSelectedVertexNull");
	}

	@Override
	protected void setValues() {
		mLogger.entering(mClassname, "setValues");

		final PixelMap pixelMap = getPixelMap();
		final double width = (double) mPreviewPicture.getSize() / (pixelMap.getHeight() * mZoom.getInt());
		mWidth.setValue(width);

		mLogger.exiting(mClassname, "setValues");
	}

	public void setZoom(final int pZoom) {
		mLogger.entering(mClassname, "setZoom");
		mZoom.setValue(pZoom);
		mLogger.exiting(mClassname, "getVisibleVertexes");
	}

	@Override
	public void showDialog() {
		mLogger.entering(mClassname, "showDialog");

		try {
			mUndo = new Properties();
			getTransform().write(mUndo, "");

			setControlSelectorDelegate(mInitialControlSelector);
			getControlSelector().delegate(mInitialControlSelector);

			final PixelMap pixelMap = getTransform().getPixelMap();
			mCache = new Picture(pixelMap.getWidth(), pixelMap.getHeight());
			getTransform().getPreviousTransform().renderSynchronous(mCache);
			changePreview(true);
			super.showDialog();
		} catch (final Throwable pT) {
			mLogger.log(Level.SEVERE, "Problem opening dialog", pT.getMessage());
		} finally {
			getControlSelector().removeDelegate(mControlSelectorDelegate);
		}

		mLogger.exiting(mClassname, "showDialog");
	}

	@Override
	public void showModalDialog() {
		System.out.println("Opening dialog");

		if (mCache == null || mCache.getWidth() != getTransformWidth() || mCache.getHeight() != getTransformHeight()) {
			mCache = new Picture(getTransformWidth(), getTransformHeight());

			final IControlPrimative<IPicture> pictureControl = new ControlContainer().createPictureControl(mCache);
			BaseTransform.renderPreviewImage(pictureControl, getTransform());
		}

		System.out.println("rendered");

		super.showModalDialog();
	}

	public Vector<IVertex> sortVisibleVertexes(final Pixel pPixel) {
		mLogger.entering(mClassname, "sortVisibleVertexes");
		PerceptionLogger.logParams(mLogger, "pPixel", pPixel);

		final Vector<IVertex> visibleVertexs = getVisibleVertexes();
		Collections.sort(visibleVertexs, new Comparator<IVertex>() {

			@Override
			// Note: this comparator imposes orderings that are inconsistent
			// with equals.
			public int compare(final IVertex pVertex0, final IVertex pVertex1) {
				final int dist0 = KMath.square(pVertex0.getX() - pPixel.getX()) + KMath.square(pVertex0.getY() - pPixel.getY());
				final int dist1 = KMath.square(pVertex1.getX() - pPixel.getX()) + KMath.square(pVertex1.getY() - pPixel.getY());
				return dist0 - dist1;
			}
		});

		if (getVisibleVertexes().size() != 0) {
			final IVertex vertex = getVisibleVertexes().firstElement();
			setSelectedVertex(vertex);
		}
		mLogger.exiting(mClassname, "sortVisibleVertexes", visibleVertexs);
		return visibleVertexs;
	}

	public void zoomIn() {
		mLogger.entering(mClassname, "zoomIn");
		setZoom(getZoom() + 1);
		mLogger.exiting(mClassname, "zoomIn");
	}

	public void zoomOut() {
		mLogger.entering(mClassname, "zoomOut");
		setZoom(getZoom() - 1);
		mLogger.exiting(mClassname, "zoomOut");
	}

}