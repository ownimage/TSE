package com.ownimage.framework.control.layout;

import java.util.Iterator;
import java.util.Vector;

import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class HSplitLayout extends ViewableBase<IViewable, IView> {

	final IViewable<?> mLeft;
	final IViewable<?> mRight;
	Vector<IViewable<?>> mViewables = new Vector<IViewable<?>>();

	public HSplitLayout(final IViewable<?> pLeft, final IViewable<?> pRight) {
		mLeft = pLeft;
		mRight = pRight;
	}

	@Override
	public IView createView() {
		IView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	public IViewable<?> getLeft() {
		return mLeft;
	}

	public IViewable<?> getRight() {
		return mRight;
	}

	public Iterator<IViewable<?>> getViewableChildrenIterator() {
		return mViewables.iterator();
	}

}
