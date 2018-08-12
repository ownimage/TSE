/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.BooleanType;
import com.ownimage.framework.control.type.IMetaType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

public class BooleanControl extends ControlBase<BooleanControl, BooleanType, IMetaType<Boolean>, Boolean, IView> {

	public class BooleanProperty {
		public boolean getValue() {
			return BooleanControl.this.getValue();
		}
	}


	public final static String mClassname = ControlBase.class.getName();
	public final static Logger mLogger = Framework.getLogger();

	public final static long serialVersionUID = 1L;

	public BooleanControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final boolean pValue) {

		super(pDisplayName, pPropertyName, pContainer, new BooleanType(pValue));
	}

	@Override
	public IView createView() { // TODO can we push this into the base class
		IView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	public BooleanProperty getProperty() {
		return new BooleanProperty();
	}

}
