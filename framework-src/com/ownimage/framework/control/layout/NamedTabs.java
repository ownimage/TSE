package com.ownimage.framework.control.layout;

import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ControlBase;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.factory.ViewFactory;

public class NamedTabs extends ViewableBase<NamedTabs, ISingleSelectView> implements INamedTabs {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	public final static Logger mLogger = Framework.getLogger();

	private final String mDisplayName;
	private final String mPropertyName;

	private final Vector<String> mTabNames = new Vector<String>();
	private final Vector<IViewable<?>> mContent = new Vector<IViewable<?>>();

	private int mSelectedIndex;

	public NamedTabs(final String pDisplayName, final String pPropertyName) {
		Framework.logEntry(mLogger);
		ControlBase.validate(pDisplayName, pPropertyName);

		mDisplayName = pDisplayName;
		mPropertyName = pPropertyName;

		Framework.logExit(mLogger);
	}

	public INamedTabs addTab(final IContainer pContainer) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pContainer, "pContent");
		addTab(pContainer.getDisplayName(), pContainer);
		Framework.logExit(mLogger);
		return this;
	}

	public INamedTabs addTab(final String pTabName, final IViewable<?> pContainer) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pTabName, "pTabName");
		Framework.checkNotNull(mLogger, pContainer, "pContent");

		checkUniqueContainerPropertyName(pTabName);

		mTabNames.add(pTabName);
		mContent.add(pContainer);

		invokeOnAllViews((view) -> view.redraw());

		Framework.logExit(mLogger);
		return this;
	}

	private void checkUniqueContainerPropertyName(final String pPropertyName) {
		boolean exists = mTabNames.contains(pPropertyName);
		if (exists) { throw new IllegalArgumentException("pPropertyName = " + pPropertyName + ", already exists for a IContainer in this NamedTabs collection."); }
	}

	@Override
	public ISingleSelectView createView() {
		ISingleSelectView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	@Override
	public String getDisplayName() {
		return mDisplayName;
	}

	@Override
	public int getSelectedIndex() {
		return mSelectedIndex;
	}

	@Override
	public String[] getTabNames() {
		return mTabNames.toArray(new String[mTabNames.size()]);
	}

	@Override
	public IViewable<?> getViewable(final String pTabName) {
		final int index = mTabNames.indexOf(pTabName);
		if (index != -1) { return mContent.get(index); }
		return null;
	}

	public void setSelectedIndex(final int pInt) {
		setSelectedIndex(pInt, null);
	}

	@Override
	public void setSelectedIndex(final int pInt, final ISingleSelectView pView) {
		mSelectedIndex = pInt;
		invokeOnAllViewsExcept(pView, (v) -> v.setSelectedIndex(mSelectedIndex));
	}

}
