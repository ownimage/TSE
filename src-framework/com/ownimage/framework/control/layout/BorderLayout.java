/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.control.layout;

import com.ownimage.framework.view.IBorderView;
import com.ownimage.framework.view.factory.ViewFactory;

public class BorderLayout extends ViewableBase<IViewable, IBorderView> {

	private IViewable<?> mTop;

	private IViewable<?> mBottom;

	private IViewable<?> mLeft;

	private IViewable<?> mRight;

	private IViewable<?> mCenter;

	public BorderLayout() {
	}

	@Override
	public IBorderView createView() {
		IBorderView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	public IViewable<?> getBottom() {
		return mBottom;
	}

	public IViewable<?> getCenter() {
		return mCenter;
	}

	public IViewable<?> getLeft() {
		return mLeft;
	}

	public IViewable<?> getRight() {
		return mRight;
	}

	public IViewable<?> getTop() {
		return mTop;
	}

	public void setBottom(final IViewable<?> pBottom) {
		mBottom = pBottom;
		invokeOnAllViews((v) -> v.redrawBottom());
	}

	public void setCenter(final IViewable<?> pCenter) {
		mCenter = pCenter;
		invokeOnAllViews((v) -> v.redrawCenter());
	}

	public void setLeft(final IViewable<?> pLeft) {
		mLeft = pLeft;
		invokeOnAllViews((v) -> v.redrawLeft());
	}

	public void setRight(final IViewable<?> pRight) {
		mRight = pRight;
		invokeOnAllViews((v) -> v.redrawRight());
	}

	public void setTop(final IViewable<?> pTop) {
		mTop = pTop;
		invokeOnAllViews((v) -> v.redrawTop());
	}

}
