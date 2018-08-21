/*
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.com, Keith Hart
 */
package com.ownimage.perception.transformSequence;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

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
import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.util.Framework;
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
import com.ownimage.perception.transform.OutputTransform;
import com.ownimage.perception.transform.PolarTransform;
import com.ownimage.perception.transform.QuadSpaceTransform;
import com.ownimage.perception.transform.RotateTransform;
import com.ownimage.perception.transform.RuleOfThirdsTransform;
import com.ownimage.perception.transform.SoftSquarePolarTransform;
import com.ownimage.perception.transform.SquarePolarTransform;
import com.ownimage.perception.transform.VariableStretch3Transform;
import com.ownimage.perception.transform.WoodcutTransform;

public class TransformSequence extends ViewableBase<TransformSequence, ISingleSelectView> implements IPersist, IContainerList {


    private final static Logger mLogger = Framework.getLogger();

    private final Perception mPerception;

    private Vector<ITransform> mTransforms;
    private int mSelectedIndex;
    private BorderLayout mBorderLayout = new BorderLayout();

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

        mUpAction = ActionControl.createImage("Up", NullContainer, this::upAction, "/icon/up.png");
        mDownAction = ActionControl.createImage("Down", NullContainer, this::downAction, "/icon/down.png");
        mAddAction = ActionControl.createImage("Add", NullContainer, this::addAction, "/icon/plus.png");
        mRemoveAction = ActionControl.createImage("Remove", NullContainer, this::removeAction, "/icon/minus.png");

        mTransforms = new Vector<>();

        ImageLoadTransform ilt = new ImageLoadTransform(pPerception, pFile);
        mTransforms.add(ilt);
    }

    private void addAction() {
        Framework.logEntry(mLogger);

        ObjectControl<ITransform> transformControl = new ObjectControl<>("Select Transform", "transform", NullContainer, getAvailableTransforms().get(0), getAvailableTransforms());
        ActionControl okAction = ActionControl.create("OK", NullContainer, () -> addTransform(transformControl.getValue()));
        mPerception.showDialog(transformControl, DialogOptions.NONE, okAction);
        Framework.logExit(mLogger);
    }

    private void addAvailableTransform(final ITransform pTransform) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");

        mAvailableTransforms.add(pTransform);
        mAvailableTransformNameMap.put(pTransform.getPropertyName(), pTransform);

        Framework.logExit(mLogger);
    }

    private void addTransform(final ITransform pTransform) {
        Framework.logEntry(mLogger);
        Framework.checkParameterNotNull(mLogger, pTransform, "pTransform");

        ITransform transform = pTransform.duplicate();
        int index = getSelectedIndex() + 1;
        mTransforms.add(index, transform);
        setPreviousTransforms();
        redraw();
        setSelectedIndex(index);
        mPerception.refreshOutputPreview();

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
        Framework.checkParameterGreaterThan(mLogger, mSelectedIndex, 0, "Cannot move ImageLoadTransform.  mSelectedIndex");
        Framework.checkParameterLessThan(mLogger, mSelectedIndex, mTransforms.size(), "Cannot move the last transform down.  mSelectedIndex");

        int newIndex = mSelectedIndex + 1;
        ITransform transform = mTransforms.remove(mSelectedIndex);
        mTransforms.add(newIndex, transform);
        setPreviousTransforms();
        redraw();
        mPerception.refreshOutputPreview();
        setSelectedIndex(newIndex);

        Framework.logExit(mLogger);
    }

    private Vector<ITransform> getAvailableTransforms() {
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

        ITransform transform = getSelectedTransform();

        mBorderLayout.setCenter(transform.getContent());
        mBorderLayout.setRight(buttonPanelAccordionLayout);

        return mBorderLayout;
    }

    @Override
    public int getCount() {
        return mTransforms.size();
    }

    private ImageLoadTransform getFirstTransform() {
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

    public void read(File pFile) throws Exception {
        try (FileInputStream fos = new FileInputStream(pFile)) {
            PersistDB db = new PersistDB();
            db.load(fos);
            read(db, "transform");
        }
    }

    @Override
    public void read(final IPersistDB pDB, final String pId) {
        Framework.logEntry(mLogger);

        Vector<ITransform> save = mTransforms;
        mTransforms = new Vector<>();
        mTransforms.add(save.firstElement());
        try {
            int i = 0;
            do {
                String transformName = pDB.read("transform." + i + ".name");
                mLogger.info("transform." + i + ".name=" + transformName);
                if (transformName == null) {
                    break;
                }
                if (i == 0) { // imageLoad
                    mTransforms.get(0).read(pDB, "transform.0");
                } else {
                    ITransform template = mAvailableTransformNameMap.get(transformName);
                    ITransform transform = template.duplicate();
                    transform.read(pDB, "transform." + i);
                    mTransforms.add(transform);
                }
                i++;
            } while (true);

            setPreviousTransforms();
            setSelectedIndex(pDB.read(pId + ".selectedContainer"));
            redraw();
        } catch (Throwable pT) {
            mTransforms = save;
            throw new RuntimeException("Transform->Open failed.", pT);

        } finally {
            mPerception.refreshOutputPreview();
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
            setSelectedIndex(index); // this is to force the button enabled to refresh
            redraw();
            mPerception.refreshOutputPreview();
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
        addAvailableTransform(new OutputTransform(mPerception));
        addAvailableTransform(new PolarTransform(mPerception));
        addAvailableTransform(new RuleOfThirdsTransform(mPerception));
        addAvailableTransform(new RotateTransform(mPerception));
        addAvailableTransform(new QuadSpaceTransform(mPerception));
        addAvailableTransform(new SoftSquarePolarTransform(mPerception));
        addAvailableTransform(new SquarePolarTransform(mPerception));
        addAvailableTransform(new VariableStretch3Transform(mPerception));
        addAvailableTransform(new WoodcutTransform(mPerception));

        Collections.sort(mAvailableTransforms, Comparator.comparing(ITransform::getDisplayName));

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
    public void setSelectedIndex(IContainer pContainer) {
        if (!(pContainer instanceof ITransform))
            throw new IllegalArgumentException("pContainer is not instanceof ITransform");

        int index = mTransforms.indexOf(pContainer);
        if (index >= 0) {
            setSelectedIndex(index);
        }
        throw new IllegalStateException("TransformSequence does not container specified container.");
    }

    @Override
    public void setSelectedIndex(final int pIndex) {
        setSelectedIndex(pIndex, null);
    }

    @Override
    public void setSelectedIndex(final int pIndex, final ISingleSelectView pView) {
        Framework.logEntry(mLogger);
        Framework.checkParameterGreaterThanEqual(mLogger, pIndex, -1, "pIndex");
        Framework.checkParameterLessThan(mLogger, pIndex, mTransforms.size(), "pIndex must be smaller than mTransforms.size().  pIndex");
        Framework.logParams(mLogger, "pIndex", pIndex);

        if (pIndex != -1) { // -1 indicates that the view is closing
            mSelectedIndex = pIndex;
            invokeOnAllViewsExcept(pView, (v) -> v.setSelectedIndex(mSelectedIndex));

            ITransform transform = (mSelectedIndex == -1) ? getFirstTransform() : getTransform(mSelectedIndex);
            IViewable<?> content = transform.getContent();
            mBorderLayout.setCenter(content);
            transform.refreshInputPreview();

            mRemoveAction.setEnabled(pIndex != 0);
            mUpAction.setEnabled(pIndex > 1);
            mDownAction.setEnabled(pIndex + 1 < mTransforms.size());
        }

        Framework.logExit(mLogger);
    }

    private void upAction() {
        Framework.logEntry(mLogger);
        Framework.checkParameterGreaterThan(mLogger, mSelectedIndex, 1, "Cannot move ImageLoadTransform, or the first transform after that up.  mSelectedIndex");

        int newIndex = mSelectedIndex - 1;
        ITransform transform = mTransforms.remove(mSelectedIndex);
        mTransforms.add(newIndex, transform);
        setPreviousTransforms();
        redraw();
        mPerception.refreshOutputPreview();
        setSelectedIndex(newIndex);

        Framework.logExit(mLogger);
    }

    @Override
    public void write(final IPersistDB pDB, final String pId) throws IOException {
        ITransform[] transforms = mTransforms.toArray(new ITransform[mTransforms.size()]);
        for (int i = 0; i < transforms.length; i++) {
            transforms[i].write(pDB, pId + "." + i);
        }
        pDB.write(pId + ".selectedContainer", String.valueOf(getSelectedIndex()));
    }

    public void resizeInputPreviews(final int pPreviewSize) {
        mTransforms.forEach(t -> t.resizeInputPreview(pPreviewSize));
        getSelectedTransform().refreshInputPreview();
    }

    private ITransform getSelectedTransform() {
        return getTransform(getSelectedIndex());
    }
}
