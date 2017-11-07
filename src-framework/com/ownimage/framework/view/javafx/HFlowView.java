package com.ownimage.framework.view.javafx;

import java.util.Iterator;

import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.IViewable;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class HFlowView extends ViewBase<HFlowLayout> {

	private HBox mUI;

	public HFlowView(final HFlowLayout pHFlow) {
		super(pHFlow);
		createView();
	}

	private void createView() {
		mUI = new HBox();

		Iterator<IViewable<?>> children = mControl.getViewableChildrenIterator();
		while (children.hasNext()) {
			IViewable child = children.next();
			mUI.getChildren().add(((FXView) (child.createView())).getUI());
		}
	}

	@Override
	public Pane getUI() {
		return mUI;
	}

}
