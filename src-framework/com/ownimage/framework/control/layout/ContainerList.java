package com.ownimage.framework.control.layout;

import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.control.event.EventDispatcher;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.factory.ViewFactory;

public class ContainerList implements IContainerList {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static String mClassname = ContainerList.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);
	public final static long serialVersionUID = 1L;

	private final String mPropertyName;
	private final String mDisplayName;

	private int mSelectedIndex = 0;
	private final Vector<IContainer> mContainers = new Vector<>();
	EventDispatcher<ISingleSelectView> mViews = new EventDispatcher<>(this);

	public ContainerList(final String pDisplayName, final String pPropertyName) {
		ControlBase.validate(pDisplayName, pPropertyName);
		mDisplayName = pDisplayName;
		mPropertyName = pPropertyName;
	}

	public IContainer add(final IContainer pContainer) {
		mContainers.add(pContainer);
		return pContainer;
	}

	@Override
	public ISingleSelectView createView() {
		ISingleSelectView view = ViewFactory.getInstance().createView(this);
		mViews.addListener(view);
		return view;
	}

	@Override
	public IContainer getContainer(final int pTab) {
		return mContainers.get(pTab);
	}

	@Override
	public int getCount() {
		return mContainers.size();
	}

	@Override
	public String getDisplayName() {
		return mDisplayName;
	}

	@Override
	public int getSelectedIndex() {
		return mSelectedIndex;
	}

	private void selectedIndexCheck(final int pSelectedIndex) {
		if (pSelectedIndex < -1 || pSelectedIndex > mContainers.size()) {
			throw new IllegalArgumentException(
					String.format("pSelectedIndex must be -1 or greater and less than mContainers.size.  pSelectedIndex = %s. mContaers.size = %s.", pSelectedIndex, mContainers.size()));
		}
	}

	@Override
	public void setSelectedIndex(final int pSelectedIndex) {
		System.out.println(String.format("setSelectedIndex(%s).", pSelectedIndex));
		selectedIndexCheck(pSelectedIndex);
		mSelectedIndex = pSelectedIndex;
		mViews.invokeAll((v) -> v.setSelectedIndex(mSelectedIndex));
	}

	@Override
	public void setSelectedIndex(final int pSelectedIndex, final ISingleSelectView pView) {
		System.out.println(String.format("setSelectedIndex(%s, %s).", pSelectedIndex, pView));
		selectedIndexCheck(pSelectedIndex);
		mSelectedIndex = pSelectedIndex;
		System.out.println("SelectedIndex = " + mSelectedIndex);
		mViews.invokeAllExcept(pView, (v) -> v.setSelectedIndex(mSelectedIndex));
	}
}
