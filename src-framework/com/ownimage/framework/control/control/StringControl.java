/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.io.File;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.StringMetaType;
import com.ownimage.framework.control.type.StringType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class StringControl extends ControlBase<StringControl, StringType, StringMetaType, String, IView> {

	public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");
	public final static String mClassname = ControlBase.class.getName();
    public final static Logger mLogger = Framework.getLogger();
	public final static long serialVersionUID = 1L;

	public StringControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final String pValue) {
		super(pDisplayName, pPropertyName, pContainer, new StringType(pValue));
	}

	public StringControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final StringType pValue) {
		super(pDisplayName, pPropertyName, pContainer, pValue);
	}

	@Override
	public IView createView() { // TODO can we push this into the base class
		IView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	public File getFile() {
		return new File(getValue());
	}

}
