package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.layout.HSplitLayout;

import javafx.scene.control.SplitPane;

public class HSplitView extends ViewBase<HSplitLayout> {

	private SplitPane mUI;

	public HSplitView(final HSplitLayout pHSplit) {
		super(pHSplit);
		createView();
	}

	private void createView() {
		mUI = new SplitPane();
		mUI.getItems().addAll((((FXView) (mControl.getLeft().createView())).getUI()), (((FXView) (mControl.getRight().createView())).getUI()));
		mUI.setDividerPositions(0.5f);
	}

	@Override
	public SplitPane getUI() {
		return mUI;
	}

}