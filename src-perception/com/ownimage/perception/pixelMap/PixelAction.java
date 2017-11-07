package com.ownimage.perception.pixelMap;

import java.util.Vector;

import com.ownimage.perception.pixelMap.editor.EditPixelMapDialog;

/**
 * The Class PixelAction is a singleton helper class for PixelMap. Its purpose is to process changes to the PixelMap pixel by pixel.
 */
public class PixelAction {

	public static enum Action {
		On, Off, OffWide, OffVeryWide, Toggle, DeleteChain
	};

	private final PixelMap mPixelMap;

	private final Vector<PixelChain> mDirtyPixelChains = new Vector<PixelChain>();
	private final Vector<Node> mNewNodes = new Vector<Node>();
	private final Vector<Node> mDirtyNodes = new Vector<Node>();

	public PixelAction(final PixelMap pPixelMap) {
		mPixelMap = pPixelMap;
	}

	private void addDirtyPixelChain(final Pixel pPixel) {
		final PixelChain pixelChain = mPixelMap.findPixelChain(pPixel);
		addDirtyPixelChain(pixelChain);
	}

	private void addDirtyPixelChain(final PixelChain pPixelChain) {
		if (pPixelChain != null && !mDirtyPixelChains.contains(pPixelChain)) {
			mDirtyPixelChains.add(pPixelChain);
		}
	}

	private void calcIsNode(final Pixel pPixel) {
		final boolean wasNode = pPixel.isNode();
		final boolean isNode = pPixel.calcIsNode();
		final Node node = mPixelMap.findNodeInPixelChains(pPixel);

		if (wasNode && isNode) {
			if (node != null) {
				mDirtyNodes.add(node);
			}
		}

		if (wasNode && !isNode) {
			cleanPixel(pPixel);
			if (node != null) {
				for (final PixelChain pixelChain : node.getPixelChains()) {
					addDirtyPixelChain(pixelChain);
				}
			}
		}

		if (!wasNode && isNode) {
			mNewNodes.add(new Node(pPixel));
			addDirtyPixelChain(pPixel);
		}

		if (!wasNode && !isNode) {
			// do nothing
		}
	}

	private void cleanDirtyNodes() {
		for (final Node node : mDirtyNodes) {
			cleanPixel(node);
			for (final PixelChain pixelChain : node.getPixelChains()) {
				cleanPixelChain(pixelChain);
			}
		}
	}

	private void cleanDirtyPixelChains() {
		for (final PixelChain pixelChain : mDirtyPixelChains) {
			cleanPixelChain(pixelChain);
		}
	}

	private void cleanPixel(final Pixel pPixel) {
		pPixel.setInChain(false);
		pPixel.setVisited(false);
	}

	private void cleanPixelChain(final PixelChain pPixelChain) {
		if (pPixelChain != null) {
			pPixelChain.setVisited(false);
			pPixelChain.setInChain(false);
			mPixelMap.removePixelChain(pPixelChain);
		}
	}

	private Vector<PixelChain> generateDirtyPixelChains() {
		final Vector<PixelChain> pixelChains = new Vector<PixelChain>();

		for (final PixelChain pixelChain : mDirtyPixelChains) {
			if (pixelChain != null) {
				Node node;
				if (pixelChain.firstPixel().isNode()) {
					node = new Node(pixelChain.firstPixel());
					pixelChains.addAll(mPixelMap.generateChains(node));
				}

				if (pixelChain.lastPixel().isNode()) {
					node = new Node(pixelChain.lastPixel());
					pixelChains.addAll(mPixelMap.generateChains(node));
				}
			}
		}

		return pixelChains;
	}

	private Vector<PixelChain> generatePixelChainsForDirtyNodes() {
		final Vector<PixelChain> pixelChains = new Vector<PixelChain>();
		for (final Node node : mDirtyNodes) {
			pixelChains.addAll(mPixelMap.generateChains(node));
		}
		return pixelChains;
	}

	private Vector<PixelChain> generatePixelChainsForNewNodes() {
		final Vector<PixelChain> pixelChains = new Vector<PixelChain>();
		for (final Node node : mNewNodes) {
			cleanPixel(node);
			pixelChains.addAll(mPixelMap.generateChains(node));
		}
		return pixelChains;
	}

	public synchronized void pixelAction(final Pixel pPixel, final EditPixelMapDialog pEPMD) {
		final Action action = pEPMD.getPixelEditMode();
		mDirtyPixelChains.clear();
		mNewNodes.clear();
		mDirtyNodes.clear();

		switch (action) {
		case On:
			pixelActionOn(pPixel);
			break;
		case Off:
			pixelActionOff(pPixel);
			break;
		case OffWide:
			pixelActionOffWide(pPixel, 2);
			break;
		case OffVeryWide:
			pixelActionOffWide(pPixel, 15);
			break;
		case Toggle:
			pixelActionToggle(pPixel);
			break;
		case DeleteChain:
			pixelActionDeleteChain(pPixel);
			break;
		default:
			break;
		}

		for (final Pixel p : pPixel.getNeighbours()) {
			thin(p);
		}
		thin(pPixel);

		calcIsNode(pPixel);
		for (final Pixel pixel : pPixel.getNeighbours()) {
			calcIsNode(pixel);
			addDirtyPixelChain(pixel);
		}

		cleanDirtyPixelChains();
		cleanDirtyNodes();

		final Vector<PixelChain> pixelChains = new Vector<PixelChain>();
		pixelChains.addAll(generatePixelChainsForNewNodes());
		pixelChains.addAll(generateDirtyPixelChains());
		pixelChains.addAll(generatePixelChainsForDirtyNodes());

		if (pPixel.isEdge() && !pPixel.isNode() && !pPixel.isVisited()) {
			// then this has closed a loop. set the pixel to a node and generate chains
			pPixel.setNode(true);
			final Node node = new Node(pPixel);
			cleanPixel(node);
			pixelChains.addAll(mPixelMap.generateChains(node));
		}

		mPixelMap.addPixelChains(pixelChains);
		for (final PixelChain pixelChain : pixelChains) {
			pixelChain.approximate();
		}

		mPixelMap.indexSegments();//

	}

	private void pixelActionDeleteChain(final Pixel pPixel) {
		final PixelChain pixelChain = mPixelMap.findPixelChain(pPixel);
		pixelChain.delete();
	}

	private void pixelActionOff(final Pixel pPixel) {
		if (pPixel.isEdge()) {
			pPixel.setEdge(false);
			cleanPixel(pPixel);

			final PixelChain pixelChain = mPixelMap.findPixelChain(pPixel);
			if (pixelChain != null) {
				mDirtyPixelChains.add(pixelChain);
			}
		}
	}

	private void pixelActionOffWide(final Pixel pPixel, final int pWidth) {
		for (int x = Math.max(0, pPixel.getX() - pWidth); x <= Math.min(mPixelMap.getWidth(), pPixel.getX() + pWidth); x++) {
			for (int y = Math.max(0, pPixel.getY() - pWidth); y <= Math.min(mPixelMap.getHeight(), pPixel.getY() + pWidth); y++) {
				final Pixel pixel = mPixelMap.getPixelAt(x, y);
				pixelActionOff(pixel);
			}
		}

	}

	private void pixelActionOn(final Pixel pPixel) {
		pPixel.setEdge(true);
		cleanPixel(pPixel);
	}

	private void pixelActionToggle(final Pixel pPixel) {
		if (pPixel.isEdge()) {
			pixelActionOff(pPixel);
		} else {
			pixelActionOn(pPixel);
		}

	}

	private void thin(final Pixel pPixel) {
		final boolean wasNode = pPixel.isNode();
		if (pPixel.thin()) {
			final PixelChain pixelChain = mPixelMap.findPixelChain(pPixel);
			if (pixelChain != null) {
				mDirtyPixelChains.add(pixelChain);
			}
			cleanPixel(pPixel);
		}
		// if (wasNode && !pPixel.isEdge()) {
		// pPixel.setNode(false);
		// }
	}

}
