/**
 * This code is part of the Perception programme. All code copyright (c) 2012, 2014 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap.editor;

import java.util.logging.Logger;

import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.Vertex;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.util.Version;
import com.ownimage.perception.util.logging.PerceptionLogger;

public class AddPixelChainControlSelector extends EditPixelMapControlSelectorBase {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = AddPixelChainControlSelector.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final PixelChain mPixelChain;

	public AddPixelChainControlSelector(final EditPixelMapControlSelectorBase pParent, final double pX, final double pY) {
		super(pParent);
		final Pixel pixel = getPixel(pX, pY);
		final Node node = new Node(pixel);
		mPixelChain = new PixelChain(node);
		addPixelChain(mPixelChain);
		setSelectedVertex(mPixelChain.getStartVertex());
	}

	private void actionAddVertex(final double pX, final double pY) {
		mLogger.entering(mClassname, "actionAddVertex");
		PerceptionLogger.logParams(mLogger, "pX, pY", pX, pY);
		final Pixel pixel = getPixel(pX, pY);
		final Node node = new Node(pixel);
		mPixelChain.add(node);

		final IVertex startVertex = mPixelChain.getJoinVertex();
		final int endIndex = mPixelChain.count() - 1;
		final IVertex endVertex = Vertex.createVertex(mPixelChain, endIndex);

		final ISegment segment = SegmentFactory.createTempStraightSegment(startVertex, endVertex);
		segment.attachToVertexes(false);
		mPixelChain.addSegment(segment);
		setSelectedVertex(endVertex);
		mLogger.exiting(mClassname, "actionAddVertex");
	}

	@Override
	public String getDisplayName() {
		mLogger.entering(mClassname, "getDisplayName");
		final String displayName = "Add PixelChain";
		mLogger.exiting(mClassname, "getDisplayName", displayName);
		return displayName;
	}

	@Override
	public boolean setSelected(final double pX, final double pY, final Modifier pModifier) {
		mLogger.entering(mClassname, "setSelected");
		final boolean result = true;

		if (pModifier.isNormal()) {
			actionAddVertex(pX, pY);
		}

		if (pModifier.isAltDown()) {
			mLogger.fine("setSelected isAltDown");
			calcVisibleVertexes();
			revertControlSelector();
		}

		mLogger.exiting(mClassname, "setSelected", result);
		return result;
	}
}
