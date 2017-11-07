package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.ScrollLayout;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;

public class ScrollView extends ViewBase<ScrollLayout> {

	private ScrollPane mUI;

	public ScrollView(final ScrollLayout pScroll) {
		super(pScroll);
		createView();
	}

	private static ScrollBarPolicy convertScrollBarPolicy(final ScrollLayout.Policy pPolicy) {
		switch (pPolicy) {
		case ALWAYS:
			return ScrollBarPolicy.ALWAYS;

		case AS_NEEDED:
			return ScrollBarPolicy.AS_NEEDED;

		default:
			return ScrollBarPolicy.NEVER;
		}
	}

	private void createView() {
		IViewable<?> content = mControl.getContent();
		FXView view = (FXView) content.createView();
		Node node = view.getUI();
		mUI = new ScrollPane(node);
		mUI.setHbarPolicy(convertScrollBarPolicy(mControl.getHorizPolicy()));
		mUI.setVbarPolicy(convertScrollBarPolicy(mControl.getVertPolicy()));
	}

	@Override
	public ScrollPane getUI() {
		return mUI;
	}

}
