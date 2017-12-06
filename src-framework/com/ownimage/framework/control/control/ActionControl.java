/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.util.logging.Logger;

import com.ownimage.framework.app.menu.IMenuItem;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.BooleanType;
import com.ownimage.framework.control.type.IMetaType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

/**
 * The Class ActionControl uses the BooleanType only so that it can use the rest of the framework. The value is is not persisted.
 */
public class ActionControl extends ControlBase<ActionControl, BooleanType, IMetaType<Boolean>, Boolean, IView> implements IMenuItem {

	public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
	public final static String mClassname = ControlBase.class.getName();
	public final static Logger mLogger = Logger.getLogger(mClassname);

	public final static long serialVersionUID = 1L;

	private final IAction mAction;
	private boolean mFullSize = true; // indicates that the button should be rendered full width.
	private String mImageName;

	public ActionControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final IAction pAction) {
		super(pDisplayName, pPropertyName, pContainer, new BooleanType(false));
		Framework.checkNotNull(mLogger, pAction, "pAction");
		mAction = pAction;
	}

	private ActionControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final IAction pAction, final boolean pFullSize) {
		this(pDisplayName, pPropertyName, pContainer, pAction);
		mFullSize = pFullSize;
	}

	private ActionControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final IAction pAction, final String pImageName) {
		this(pDisplayName, pPropertyName, pContainer, pAction);
		mFullSize = false;
		mImageName = pImageName;
	}

	public static ActionControl create(final String pDisplayName, final IContainer pContainer, final IAction pAction) {
		return new ActionControl(pDisplayName, "ActionControlCreate", pContainer, pAction);
	}

	// TODO should really have a list of defined images?
	public static ActionControl createImage(final String pDisplayName, final IContainer pContainer, final IAction pAction, final String pImageName) {
		return new ActionControl(pDisplayName, "ActionControlCreate", pContainer, pAction, pImageName);
	}

	public static ActionControl createSmall(final String pDisplayName, final IContainer pContainer, final IAction pAction) {
		return new ActionControl(pDisplayName, "ActionControlCreate", pContainer, pAction, false);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public IView createMenuItemView() { // TODO can we push this into the base class
		IView view = ViewFactory.getInstance().createMenuItemView(this);
		addView(view);
		return view;
	}

	@Override
	public IView createView() { // TODO can we push this into the base class
		IView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	public String getImageName() {
		return mImageName;
	}

	public boolean hasImage() {
		return mImageName != null && mImageName.length() != 0;
	}

	public boolean isFullSize() {
		return mFullSize;
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	public void performAction() {
		mAction.performAction();
	}

	@Override
	public void read(final IPersistDB pDB, final String pId) {
		// do nothing
	}

	@Override
	public void write(final IPersistDB pDB, final String pId) {
		// do nothing
	}

}
