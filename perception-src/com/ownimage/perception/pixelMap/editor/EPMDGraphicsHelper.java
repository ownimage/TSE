/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.editor;

import java.awt.Color;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.perception.control.combo.PictureControl;
import com.ownimage.perception.math.Line;
import com.ownimage.perception.math.Point;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.editor.EditPixelMapDialog.EditorType;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.GraphicsHelper;
import com.ownimage.perception.ui.graphics.Path;
import com.ownimage.perception.util.Version;

public class EPMDGraphicsHelper extends GraphicsHelper {

	public final static Version mVersion = new Version(4, 0, 1, "2014/05/15 07:19");
	public final static String mClassname = EPMDGraphicsHelper.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final EditPixelMapDialog mEditPixelMapDialog;

	public EPMDGraphicsHelper(final EditPixelMapDialog pEditPixelMapDialog, final GraphicsHelper pGraphics) {
		super(pGraphics);

		if (pEditPixelMapDialog == null) {
			throw new IllegalArgumentException("pEditPixelMapDialog must not be null");
		}
		mEditPixelMapDialog = pEditPixelMapDialog;
	}

	private double getAspectRatio() {
		return mEditPixelMapDialog.getAspectRatio();
	}

	private Color getControlLineColor() {
		return mEditPixelMapDialog.getControlLineColor();
	}

	private Color getControlPointColor() {
		return mEditPixelMapDialog.getControlPointColor();
	}

	private double getCrossHairSize() {
		return mEditPixelMapDialog.getCrossHairSize();
	}

	private Color getEdgeColor() {
		return mEditPixelMapDialog.getEdgeColor();
	}

	private Color getNodeColor() {
		return mEditPixelMapDialog.getNodeColor();
	}

	private Pixel getOriginPixel() {
		return mEditPixelMapDialog.getOriginPixel();
	}

	private PixelMap getPixelMap() {
		return mEditPixelMapDialog.getPixelMap();
	}

	private PictureControl getPreviewPicture() {
		return mEditPixelMapDialog.getPreviewPicture();
	}

	private Color getSegmentAttachedColor() {
		return mEditPixelMapDialog.getSegmentAttachedColor();
	}

	private Color getSegmentColor() {
		return mEditPixelMapDialog.getSegmentColor();
	}

	private EditorType getType() {
		return mEditPixelMapDialog.getType();
	}

	private Color getSegmentSelectedColor() {
		return mEditPixelMapDialog.getSegmentSelectedColor();
	}

	private double getSegmentThickness() {
		return mEditPixelMapDialog.getSegmentThickness();
	}

	private double getTransformHeight() {
		return mEditPixelMapDialog.getTransformHeight();
	}

	private int getTransformWidth() {
		return mEditPixelMapDialog.getTransformWidth();
	}

	private Color getVertexColor() {
		return mEditPixelMapDialog.getVertexColor();
	}

	private Color getVertexSelectedColor() {
		return mEditPixelMapDialog.getVertexSelectedColor();
	}

	private double getVertexThickness() {
		return mEditPixelMapDialog.getVertexThickness();
	}

	private Vector<IVertex> getVisibleVertexes() {
		return mEditPixelMapDialog.getVisibleVertexes();
	}

	private int getZoom() {
		return mEditPixelMapDialog.getZoom();
	}

	public void graffiitControlLine(final Point pA, final Point pB) {
		drawLine(pA, pB, getControlLineColor());

	}

	public void graffitiControlPoint(final Point pPoint) {
		if (pPoint != null) {
			final double penWidth = getPenWidth();
			setPenWidth(penWidth * getVertexThickness());
			drawCrossHairs(pPoint, getControlPointColor(), getCrossHairSize());
			setPenWidth(penWidth);
		}
	}

	public void graffitiPixelChain(final PixelChain pPixelChain, final Color pColor) {
		if (pPixelChain != null) {
			final Path path = pPixelChain.toPath();
			drawPath(path, pColor);
		}
	}

	public void graffitiPixelChains() {
		final double originalPenWidth = getPenWidth();
		final double penWidth = getZoom() * getSegmentThickness() / 2.0d;
		setPenWidth(penWidth);

		for (final PixelChain pixelChain : getPixelMap().getAllPixelChains()) {
			graffitiPixelChain(pixelChain, getSegmentColor());
		}

		setPenWidth(originalPenWidth);
	}

	public void graffitiPixels() {
		final Pixel originPixel = getOriginPixel();
		final int zoom = getZoom();

		for (int x = originPixel.getX(); x <= originPixel.getX() + getPreviewPicture().getSize() / zoom; x++) {
			for (int y = originPixel.getY(); y <= originPixel.getY() + getPreviewPicture().getSize() / zoom; y++) {
				final Pixel pixel = getPixelMap().getPixelAt(x, y);
				Color color = null;

				if (pixel.isNode()) {
					color = getNodeColor();
				} else if (pixel.isEdge()) {
					color = getEdgeColor();
				}

				if (color != null) {
					final Point point = pixel.getUHVWPoint();
					final double x1 = point.getX();
					final double y1 = point.getY();
					final double x2 = x1 + getAspectRatio() / getTransformWidth();
					final double y2 = y1 + 1.0d / getTransformHeight();

					drawFilledRectangle(x1, y1, x2, y2, color);

				}
			}
		}
	}

	public void graffitiSegment(final ISegment pSegment) {
		graffitiSegment(pSegment, getSegmentColor());
	}

	private void graffitiSegment(final ISegment pSegment, final Color pColor) {
		if (pSegment != null) {
			final Path path = new Path();
			pSegment.addToPath(path);
			final double originalPenWidth = getPenWidth();
			setPenWidth(getZoom() * getSegmentThickness() / 2.0d);
			drawPath(path, pColor);
			setPenWidth(originalPenWidth);
		}
	}

	public void graffitiSegmentAttached(final ISegment pSegment) {
		graffitiSegment(pSegment, getSegmentAttachedColor());
	}

	public void graffitiSegmentSelected(final ISegment pSegment) {
		if (pSegment != null) {
			graffitiSegment(pSegment, getSegmentSelectedColor());
			final Point controlPoint = pSegment.getControlPoint();
			if (controlPoint != null) {
				drawCrossHairs(controlPoint, getVertexSelectedColor(), getCrossHairSize());
			}
		}
	}

	public void graffitiSelectedControlPoint(final Point pPoint) {
		if (pPoint != null) {
			final double penWidth = getPenWidth();
			setPenWidth(penWidth * getVertexThickness());
			drawCrossHairs(pPoint, getVertexSelectedColor(), getCrossHairSize());
			setPenWidth(penWidth);
		}
	}

	public void graffitiVertex(final IVertex pVertex) {
		graffitiVertex(pVertex, getVertexColor());
	}

	private void graffitiVertex(final IVertex pVertex, final Color pColor) {
		if (pVertex != null) {
			final double penWidth = getPenWidth();
			setPenWidth(penWidth * getVertexThickness());
			final Point point = pVertex.getUHVWPoint();
			drawCrossHairs(point, pColor, getCrossHairSize());

			final Line tangent = pVertex.getTangent();
			final Point tangentStart = point;
			final Point tangentEnd = tangent.getPoint(0.005d);
			setPenWidth(penWidth * getVertexThickness() * 2.0);
			drawLine(tangentStart, tangentEnd, Color.WHITE);

			setPenWidth(penWidth);
		}
	}

	public void graffitiVertexSelected(final IVertex pVertex) {
		graffitiVertex(pVertex, getVertexSelectedColor());
	}

	public void graffitiVisibleVertexes() {
		if (getVisibleVertexes().size() != 0) {
			for (final PixelChain pixelChain : getPixelMap().getAllPixelChains()) {
				pixelChain.grafittiVertexsAndControlLines(this);
			}
		}
	}

}