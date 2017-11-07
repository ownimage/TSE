/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import java.util.logging.Logger;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.container.NullContainer;
import com.ownimage.framework.control.layout.VFlowLayout;
import com.ownimage.framework.control.type.IMetaType;
import com.ownimage.framework.control.type.PointType;
import com.ownimage.framework.control.type.StringMetaType;
import com.ownimage.framework.control.type.StringType;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IView;
import com.ownimage.perception.math.Point;

public class PointControl extends ControlBase<PointControl, PointType, IMetaType<Point>, Point, IView> {

	public class PointProperty {
		public Point getValue() {
			return PointControl.this.getValue();
		}
	}

	public final static Version mVersion = new Version(5, 0, 0, "2015/11/26 20:48");

	public final static Logger mLogger = Logger.getLogger(mClassname);

	public final static long serialVersionUID = 1L;

	private final DoubleControl mX = new DoubleControl("x", "x", NullContainer.NullContainer, 0.0d);

	private final DoubleControl mY = new DoubleControl("y", "y", NullContainer.NullContainer, 0.0d);

	private VFlowLayout mViewable;

	public PointControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final Point pValue) {

		super(pDisplayName, pPropertyName, pContainer, new PointType(pValue));

		mX.setValue(pValue.getX());
		mY.setValue(pValue.getY());
		mX.setEnabled(false);
		mY.setEnabled(false);

	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public synchronized IView createView() { // TODO can we push this into the base class
		if (mViewable == null) {
			StringControl label = new StringControl("label", "label", NullContainer.NullContainer, new StringType(getDisplayName(), new StringMetaType(StringMetaType.DisplayType.LABEL)));
			mViewable = new VFlowLayout(label, mX, mY);
		}

		return mViewable.createView();
	}

	@Override
	public void drag(final double pDelta) {
		throw new UnsupportedOperationException("drag(final double pDelta) is not supported.");
	}

	@Override
	public void drag(final double pXDelta, final double pYDelta) {
		mX.setValue(getDragStartValue().getX() + pXDelta);
		mY.setValue(getDragStartValue().getY() + pYDelta);
		setValue(new Point(mX.getValue(), mY.getValue()));
	}

	@Override
	public void dragEnd() {
		setDragging(false);
		createUndoRedoAction(getDragStartValue(), getValue());
	}

	@Override
	public void dragStart() {
		setDragging(true);
		setDragStartValue(getValue());
	}

	public PointProperty getProperty() {
		return new PointProperty();
	}

	@Override
	public boolean isXYControl() {
		return true;
	}

	@Override
	public boolean setValue(final Point pValue, final IView pView, final boolean pIsMutating) {
		boolean result = super.setValue(pValue, pView, pIsMutating);
		mX.setValue(pValue.getX());
		mY.setValue(pValue.getY());
		return result;
	}

}
