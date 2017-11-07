/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */
package com.ownimage.perception.transformSequence;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.ObjectControl;
import com.ownimage.framework.control.layout.BorderLayout;
import com.ownimage.framework.control.layout.HFlowLayout;
import com.ownimage.framework.control.layout.IContainerList;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.ViewableBase;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;
import com.ownimage.framework.view.IAppControlView.DialogOptions;
import com.ownimage.framework.view.ISingleSelectView;
import com.ownimage.framework.view.factory.ViewFactory;
import com.ownimage.perception.app.Perception;
import com.ownimage.perception.transform.BorderTransform;
import com.ownimage.perception.transform.CannyEdgeTransform;
import com.ownimage.perception.transform.CircleMaskTransform;
import com.ownimage.perception.transform.CropTransform;
import com.ownimage.perception.transform.ITransform;
import com.ownimage.perception.transform.ImageLoadTransform;
import com.ownimage.perception.transform.LayerCakeTransform;
import com.ownimage.perception.transform.PolarTransform;
import com.ownimage.perception.transform.QuadSpaceTransform;
import com.ownimage.perception.transform.RotateTransform;
import com.ownimage.perception.transform.RuleOfThirdsTransform;
import com.ownimage.perception.transform.SoftSquarePolarTransform;
import com.ownimage.perception.transform.SquarePolarTransform;
import com.ownimage.perception.transform.VariableStretch3Transform;
import com.ownimage.perception.transform.WoodcutTransform;

public class TransformSequence extends ViewableBase<TransformSequence, ISingleSelectView> implements IPersist, IContainerList {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	private final static Logger mLogger = Logger.getLogger(TransformSequence.class.getName());

	private final Perception mPerception;

	private Vector<ITransform> mTransforms;
	private int mSelectedIndex;
	private BorderLayout mBorderLayout;

	private final ActionControl mUpAction;
	private final ActionControl mDownAction;
	private final ActionControl mAddAction;
	private final ActionControl mRemoveAction;

	private boolean mDirty = false;

	private Vector<ITransform> mAvailableTransforms;
	private Map<String, ITransform> mAvailableTransformNameMap;

	public TransformSequence(final Perception pPerception, final File pFile) {
		mPerception = pPerception;
		setAvailableTransforms();

		Container tempContainer = new Container("Transform Sequence", "transformSequence", mPerception);
		mUpAction = ActionControl.createImage("Up", tempContainer, () -> upAction(), "/icon/up.png");
		mDownAction = ActionControl.createImage("Down", tempContainer, () -> downAction(), "/icon/down.png");
		mAddAction = ActionControl.createImage("Add", tempContainer, () -> addAction(), "/icon/plus.png");
		mRemoveAction = ActionControl.createImage("Remove", tempContainer, () -> removeAction(), "/icon/minus.png");

		mTransforms = new Vector<ITransform>();

		ImageLoadTransform ilt = new ImageLoadTransform(pPerception, pFile);
		mTransforms.add(ilt);

		RotateTransform rt = new RotateTransform(pPerception);
		rt.setPreviousTransform(ilt);
		mTransforms.add(rt);

		RotateTransform rt2 = new RotateTransform(pPerception);
		rt2.setPreviousTransform(rt);
		mTransforms.add(rt2);

		VariableStretch3Transform vst = new VariableStretch3Transform(mPerception);
		vst.setPreviousTransform(rt2);
		mTransforms.add(vst);

		CircleMaskTransform cm1 = new CircleMaskTransform(mPerception);
		cm1.setPreviousTransform(vst);
		mTransforms.add(cm1);

		CircleMaskTransform cm2 = new CircleMaskTransform(mPerception);
		cm2.setPreviousTransform(cm1);
		mTransforms.add(cm2);

	}

	private void addAction() {
		Framework.logEntry(mLogger);

		ObjectControl<ITransform> transformControl = new ObjectControl<>("Select Transform", "transform", NullContainer, getAvailableTransforms().get(0), getAvailableTransforms());
		ActionControl okAction = ActionControl.create("OK", NullContainer, () -> addTransform(transformControl.getValue()));
		mPerception.showDialog(transformControl, new DialogOptions(), okAction);
		Framework.logExit(mLogger);
	}

	private void addAvailableTransform(final ITransform pTransform) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pTransform, "pTransform");

		mAvailableTransforms.add(pTransform);
		mAvailableTransformNameMap.put(pTransform.getPropertyName(), pTransform);

		Framework.logExit(mLogger);
	}

	private void addTransform(final ITransform pTransform) {
		Framework.logEntry(mLogger);
		Framework.checkNotNull(mLogger, pTransform, "pTransform");

		ITransform transform = pTransform.duplicate();
		int index = getSelectedIndex() + 1;
		mTransforms.add(index, transform);
		setPreviousTransforms();
		redraw();
		mPerception.refreshPreview();
		setSelectedIndex(index);

		Framework.logExit(mLogger);
	}

	@Override
	public ISingleSelectView createView() {
		ISingleSelectView view = ViewFactory.getInstance().createView(this);
		addView(view);
		return view;
	}

	private void downAction() {
		Framework.logEntry(mLogger);
		Framework.checkGreaterThan(mLogger, mSelectedIndex, 0, "Cannot move ImageLoadTransform.");
		Framework.checkLessThan(mLogger, mSelectedIndex, mTransforms.size(), "Cannot move the last transform down.");

		int newIndex = mSelectedIndex + 1;
		ITransform transform = mTransforms.remove(mSelectedIndex);
		mTransforms.add(newIndex, transform);
		setPreviousTransforms();
		redraw();
		mPerception.refreshPreview();
		setSelectedIndex(newIndex);

		Framework.logExit(mLogger);
	}

	public Vector<ITransform> getAvailableTransforms() {
		return mAvailableTransforms;
	}

	@Override
	public IContainer getContainer(final int pTab) {
		ITransform transform = mTransforms.get(pTab);
		IContainer controls = transform.getControls();
		return controls;
	}

	public IViewable<?> getContent() {

		HFlowLayout buttonLayout = new HFlowLayout(mAddAction, mRemoveAction, mUpAction, mDownAction);

		BorderLayout buttonPanelAccordionLayout = new BorderLayout();
		buttonPanelAccordionLayout.setTop(buttonLayout);
		buttonPanelAccordionLayout.setCenter(this);

		ITransform transform = getFirstTransform();

		mBorderLayout = new BorderLayout();
		mBorderLayout.setCenter(transform.getContent());
		mBorderLayout.setRight(buttonPanelAccordionLayout);

		return mBorderLayout;
	}

	@Override
	public int getCount() {
		return mTransforms.size();
	}

	public ImageLoadTransform getFirstTransform() {
		return (ImageLoadTransform) mTransforms.firstElement();
	}

	public ITransform getLastTransform() {
		return mTransforms.lastElement();
	}

	@Override
	public String getPropertyName() {
		return "TransformSequence";
	}

	@Override
	public int getSelectedIndex() {
		return mSelectedIndex;
	}

	public ITransform getTransform(final int pIndex) {
		return mTransforms.get(pIndex);
	}

	private int getTransformCount(final java.util.Properties pProperites, final String pId) {
		String transformName = pProperites.getProperty(pId + ".0");
		int cnt = 1;

		while (transformName != null && transformName.length() != 0) {
			transformName = pProperites.getProperty(pId + "." + ++cnt);
		}

		return cnt;
	}

	public boolean isDirty() {
		return mDirty;
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public void read(final IPersistDB pDB, final String pId) {
		Framework.logEntry(mLogger);

		Vector<ITransform> save = mTransforms;
		mTransforms = new Vector<>();
		mTransforms.add(save.firstElement());
		try {
			int i = 1;
			do {
				String transformName = pDB.read("transform." + i + ".name");
				mLogger.severe("transform." + i++ + ".name=" + transformName);
				if (transformName == null) {
					break;
				}
				ITransform template = mAvailableTransformNameMap.get(transformName);
				ITransform transform = template.duplicate();
				mTransforms.add(transform);

			} while (true);

			for (i = 0; i < mTransforms.size(); i++) {
				ITransform transform = mTransforms.get(i);
				transform.read(pDB, "transform." + i);
				transform.setValues();
			}

		} catch (Throwable pT) {
			mTransforms = save;
			throw new RuntimeException("Transform->Open failed.", pT);

		} finally {
			setPreviousTransforms();
			redraw();
			mPerception.refreshPreview();
		}

		Framework.logExit(mLogger);
	}

	private void removeAction() {
		Framework.logEntry(mLogger);

		int index = getSelectedIndex();
		if (0 < index && index < mTransforms.size()) {
			mTransforms.remove(index);
			setPreviousTransforms();

			if (index >= mTransforms.size()) {
				index = mTransforms.size() - 1;
			}
			setSelectedIndex(index); // this is to force the button enables to refresh
			redraw();
			mPerception.refreshPreview();
		}

		Framework.logExit(mLogger);
	}

	private void setAvailableTransforms() {
		Framework.logEntry(mLogger);

		mAvailableTransforms = new Vector<>();
		mAvailableTransformNameMap = new HashMap<>();

		addAvailableTransform(new BorderTransform(mPerception));
		addAvailableTransform(new CannyEdgeTransform(mPerception));
		addAvailableTransform(new CircleMaskTransform(mPerception));
		addAvailableTransform(new CropTransform(mPerception));
		addAvailableTransform(new LayerCakeTransform(mPerception));
		addAvailableTransform(new PolarTransform(mPerception));
		addAvailableTransform(new RuleOfThirdsTransform(mPerception));
		addAvailableTransform(new RotateTransform(mPerception));
		addAvailableTransform(new QuadSpaceTransform(mPerception));
		addAvailableTransform(new SoftSquarePolarTransform(mPerception));
		addAvailableTransform(new SquarePolarTransform(mPerception));
		addAvailableTransform(new VariableStretch3Transform(mPerception));
		addAvailableTransform(new WoodcutTransform(mPerception));

		Collections.sort(mAvailableTransforms, (t1, t2) -> {
			return t1.getDisplayName().compareTo(t2.getDisplayName());
		});

		Framework.logExit(mLogger);
	}

	public void setDirty() {
		mDirty = true;
	}

	private void setDirty(final boolean pDirty) {
		mDirty = pDirty;
	}

	private void setPreviousTransforms() {
		for (int i = 1; i < mTransforms.size(); i++) {
			mTransforms.get(i).setPreviousTransform(mTransforms.get(i - 1));
		}
	}

	@Override
	public void setSelectedIndex(final int pIndex) {
		setSelectedIndex(pIndex, null);
	}

	@Override
	public void setSelectedIndex(final int pIndex, final ISingleSelectView pView) {
		Framework.logEntry(mLogger);
		Framework.checkGreaterThanEqual(mLogger, pIndex, -1, "pIndex = %d, it must be greater than or equal to %d.");
		Framework.checkLessThan(mLogger, pIndex, mTransforms.size(), "pIndex = %d, it must be smaller than mTransforms.size() = %d.");
		Framework.logParams(mLogger, "pIndex", pIndex);

		if (pIndex != -1) { // -1 indicates that the view is closing
			mSelectedIndex = pIndex;
			invokeOnAllViewsExcept(pView, (v) -> v.setSelectedIndex(mSelectedIndex));

			ITransform transform = (mSelectedIndex == -1) ? getFirstTransform() : getTransform(mSelectedIndex);
			IViewable<?> content = transform.getContent();
			mBorderLayout.setCenter(content);
			transform.updatePreview();

			mRemoveAction.setEnabled(pIndex != 0);
			mUpAction.setEnabled(pIndex > 1);
			mDownAction.setEnabled(pIndex + 1 < mTransforms.size());
		}

		Framework.logExit(mLogger);
	}

	private void upAction() {
		Framework.logEntry(mLogger);
		Framework.checkGreaterThan(mLogger, mSelectedIndex, 1, "Cannot move ImageLoadTransform, or the first transform after that up.");

		int newIndex = mSelectedIndex - 1;
		ITransform transform = mTransforms.remove(mSelectedIndex);
		mTransforms.add(newIndex, transform);
		setPreviousTransforms();
		redraw();
		mPerception.refreshPreview();
		setSelectedIndex(newIndex);

		Framework.logExit(mLogger);
	}

	@Override
	public void write(final IPersistDB pDB, final String pId) {
		ITransform[] transforms = mTransforms.toArray(new ITransform[mTransforms.size()]);
		for (int i = 0; i < transforms.length; i++) {
			transforms[i].write(pDB, pId + "." + i);
		}
	}
}
