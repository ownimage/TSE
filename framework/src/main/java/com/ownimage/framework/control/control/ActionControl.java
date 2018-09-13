/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.app.menu.IMenuItem;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.BooleanType;
import com.ownimage.framework.control.type.IMetaType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;

import java.util.logging.Logger;

import static com.ownimage.framework.control.container.NullContainer.NullContainer;

/**
 * The Class ActionControl uses the BooleanType only so that it can use the rest of the framework. The value is is not persisted.
 */
public class ActionControl extends ControlBase<ActionControl, BooleanType, IMetaType<Boolean>, Boolean, IView> implements IMenuItem {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private final IAction mAction;
    private boolean mFullSize = true; // indicates that the button should be rendered full width.
    private String mImageName;

    public ActionControl(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final IAction pAction) {
        super(pDisplayName, pPropertyName, pContainer, new BooleanType(false));
        Framework.checkParameterNotNull(mLogger, pAction, "pAction");
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

    /**
     * Creates an aciton control based on this one but with a NullContainer and the specified methods wrapping the action.
     *
     * @param pDoBefore action to do before normal action is carried out
     * @param pDoAfter  action to do after normal action is carried out
     * @return a new ActionControl
     */
    public ActionControl doWrap(IAction pDoBefore, IAction pDoAfter) {
        IAction action = () -> {
            if (pDoBefore != null) pDoBefore.performAction();
            performAction();
            if (pDoAfter != null) pDoAfter.performAction();
        };
        ActionControl ac = new ActionControl(getDisplayName(), getPropertyName(), NullContainer, action);
        ac.mFullSize = this.mFullSize;
        ac.mImageName = this.mImageName;
        return ac;
    }

    /**
     * Creates an aciton control based on this one but with a NullContainer and the specified method running before the normal performAction
     *
     * @param pDoBefore action to do before normal action is carried out
     * @return a new ActionControl
     */
    public ActionControl doBefore(IAction pDoBefore) {
        return doWrap(pDoBefore, null);
    }

    /**
     * Creates an aciton control based on this one but with a NullContainer and the specified method running after the normal performAction
     *
     * @param pDoAfter action to do after normal action is carried out
     * @return a new ActionControl
     */
    public ActionControl doAfter(IAction pDoAfter) {
        return doWrap(pDoAfter, null);
    }

}