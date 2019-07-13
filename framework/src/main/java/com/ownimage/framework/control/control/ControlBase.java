/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.event.ControlEventDispatcher;
import com.ownimage.framework.control.event.EventDispatcher;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.event.IControlEventDispatcher;
import com.ownimage.framework.control.event.IControlValidator;
import com.ownimage.framework.control.type.IMetaType;
import com.ownimage.framework.control.type.IType;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.undo.IUndoRedoAction;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;
import java.util.regex.Pattern;

//import com.ownimage.perception.ui.client.IControlUI;

/**
 * The Class ControlBase is the base for all control. A Control MUST exist in the context of a Container..
 *
 * @param <C> the generic type
 * @param <T> the generic type
 * @param <M> the generic type
 * @param <R> the generic type
 */
public class ControlBase<C extends IControl<C, T, M, R>, T extends IType<M, R>, M extends IMetaType<R>, R, V extends IView>
        implements IControl<C, T, M, R> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    /**
     * Validates the pDisplayName and the pPropertyName to make sure that they are not null, zero length. In addition pDisplayName
     * must be [a-zA-Z0-9 ]+ and pPropertyName must be [a-zA-Z0-9]+. This is pulled out as a separate function so the same rules can
     * be used for other objects, e.g. Container.
     *
     * @param pDisplayName  the display name
     * @param pPropertyName the property name
     */
    public static void validate(final String pDisplayName, final String pPropertyName) {
        Framework.checkParameterNotNullOrEmpty(mLogger, pDisplayName, "pDisplayName");
        Framework.checkParameterNotNullOrEmpty(mLogger, pPropertyName, "pPropertyName");

        if (!Pattern.matches("[a-zA-Z0-9 ]*",
                pDisplayName)) {
            throw new IllegalArgumentException("pDisplayName must only contain characters \"a-zA-Z0-9 \", pDisplayName = \"" + pDisplayName + "\".");
        }

        if (!Pattern.matches("[a-zA-Z0-9]*", pPropertyName)) {
            throw new IllegalArgumentException("pPropertyName must only contain characters a-zA-Z0-9, pPropertyName = \"" + pPropertyName + "\".");
        }
    }

    private boolean mDragging = false;
    private boolean mUndoEnabled = true;

    private double mDragStartNormalizedValue = 0;
    private R mDragStartValue;

    protected T mValue;

    private R mValidateValue;
    private final String mDisplayName;

    private final String mPropertyName;
    private boolean mValid = true;
    private boolean mEnabled = true;

    private boolean mVisible = true;
    private boolean mIsPersistent = true;

    private boolean mIsDirty = false;

    private final IContainer mContainer;
    protected final EventDispatcher<V> mViews;

    private final IControlEventDispatcher mEventDispatcher = new ControlEventDispatcher(this);
    private final EventDispatcher<IVisibileListener> mVisibilityDispatcher = new EventDispatcher<>(this);
    private final EventDispatcher<IEnabledListener> mEnabledDispatcher = new EventDispatcher<>(this);

    public ControlBase(final String pDisplayName, final String pPropertyName, final IContainer pContainer, final T pValue) {
        validate(pDisplayName, pPropertyName);

        if (pContainer == null) {
            throw new IllegalArgumentException("pContainer must not be null.");
        }

        mDisplayName = pDisplayName;
        mPropertyName = pPropertyName;
        mValue = pValue;
        mValidateValue = pValue.getValue();
        mContainer = pContainer;

        mViews = new EventDispatcher<>(this);
        // resetValidateValue();
        // TODO

        pContainer.addControl(this);
        // TODO addControlEventListener(pContainer);
    }

    @Override
    public C addControlChangeListener(final IControlChangeListener<?> pListener) {
        Framework.checkParameterNotNull(mLogger, pListener, "pListener");
        mEventDispatcher.addControlChangeListener(pListener);
        return (C) this;
    }

    public C addTypedControlChangeListener(@NotNull final IControlChangeListener<C> pListener) {
        mEventDispatcher.addControlChangeListener(pListener);
        return (C) this;
    }

    @Override
    public C addControlValidator(final IControlValidator<?> pValidator) {
        Framework.checkParameterNotNull(mLogger, pValidator, "pValidator");
        mEventDispatcher.addControlValidator(pValidator);
        return (C) this;
    }

    public C addTypedControlValidator(@NotNull final IControlValidator<C> pValidator) {
        mEventDispatcher.addControlValidator(pValidator);
        return (C) this;
    }

    protected void addView(final V pView) {
        Framework.checkParameterNotNull(mLogger, pView, "pView");
        mViews.addListener(pView);
    }

    @Override
    public void clean() {
        setDirty(false);
    }

    @Override
    public C clone(final IContainer pContainer) {
        throw new UnsupportedOperationException("clone(IContainer pContainer)");
    }

    protected void createUndoRedoAction(final R pOldValue, final R pNewValue) {
        final IUndoRedoAction redo = new IUndoRedoAction() {

            private final boolean mOldDirtyFlag = isDirty();

            @Override
            public String getDescription() {
                return "set value on " + getDisplayName();
            }

            @Override
            public void redo() {
                mLogger.fine(() -> getDescription() + "\nredo setValue to " + pOldValue);
                mValue.setValue(pNewValue);
                mValidateValue = pNewValue;
                fireControlChangeEvent();
            }

            @Override
            public void undo() {
                mLogger.fine(() -> getDescription() + "\nundo setValue to " + pOldValue);
                mValue.setValue(pOldValue);
                mValidateValue = pOldValue;
                setDirty(mOldDirtyFlag);
                fireControlChangeEvent();
            }

            @Override
            public String toString() {
                return new StringBuilder("IUndoRedoAction ").append(getDescription())
                        .append("\nOldValue=").append(pOldValue)
                        .append("\nNewValue=").append(pNewValue)
                        .toString();
            }
        };

        mContainer.getUndoRedoBuffer().add(redo);
    }

    @Override
    public IView createView() {
        throw new UnsupportedOperationException("createView not defined");
    }

    @Override
    public void drag(final double pDelta) {
        Framework.logEntry(mLogger);
        setNormalizedValue(mDragStartNormalizedValue + pDelta);
        Framework.logExit(mLogger);
    }

    @Override
    public void drag(final double pXDelta, final double pYDelta) {
        throw new UnsupportedOperationException("drag(double pXDelta, double pYDelta) is not supported");
    }

    @Override
    public void dragEnd() {
        Framework.logEntry(mLogger);

        setDragging(false);
        if (isUndoEnabled()) createUndoRedoAction(getDragStartValue(), getValue());

        Framework.logExit(mLogger);
    }

    @Override
    public void dragStart() {
        Framework.logEntry(mLogger);

        setDragging(true);
        mDragStartNormalizedValue = getNormalizedValue();
        mDragStartValue = getValue();

        Framework.logExit(mLogger);
    }

    @Override
    public void fireControlChangeEvent() {
        fireControlChangeEvent(null, false);
    }

    @Override
    public void fireControlChangeEvent(final IView pView, final boolean pIsMutating) {
        mEventDispatcher.fireControlChangeEvent(this, pView, pIsMutating);
    }

    @Override
    public boolean fireControlValidate() {
        return mEventDispatcher.fireControlValidate(this);
    }

    public IContainer getContainer() {
        return mContainer;
    }

    @Override
    public String getDisplayName() {
        return mDisplayName;
    }

    protected R getDragStartValue() {
        return mDragStartValue;
    }

    @Override
    public M getMetaType() {
        return mValue.getMetaModel();
    }

    public double getNormalizedValue() {
        throw new UnsupportedOperationException("getNormalizedValue() is not supported.");
    }

    @Override
    public String getPropertyName() {
        return mPropertyName;
    }

    @Override
    public String getString() {
        return mValue.getString();
    }

    public UndoRedoBuffer getUndoRedoBuffer() {
        return mContainer.getUndoRedoBuffer();
    }

    @Override
    public R getValidateValue() {
        return mValidateValue;
    }

    @Override
    public R getValue() {
        return mValue.getValue();
    }

    @Override
    public boolean isDirty() {
        return mIsDirty;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    @Override
    public boolean isPersistent() {
        return mIsPersistent;
    }

    @Override
    public boolean isValid() {
        return mValid;
    }

    @Override
    public boolean isVisible() {
        return mVisible;
    }

    @Override
    public boolean isXYControl() {
        return false;
    }

    @Override
    public void read(final IPersistDB pDB, final String pId) {
        if (pDB == null) {
            throw new IllegalArgumentException("pDB must not be null");
        }
        if (pId == null) {
            throw new IllegalArgumentException("pId must not be null");
        }

        if (isPersistent()) {
            final String value = pDB.read(getPrefix(pId) + mPropertyName);
            if (value != null) {
                mValue.setString(value);
            }
            resetValidateValue();

            fireControlChangeEvent(null, false);
        }
    }

    @Override
    public void removeControlChangeListener(final IControlChangeListener pLIstener) {
        mEventDispatcher.removeControlChangeListener(pLIstener);
    }

    @Override
    public void removeControlValidator(final IControlValidator pValidator) {
        mEventDispatcher.removeControlValidator(pValidator);
    }

    private void resetValidateValue() {
        mValidateValue = mValue.getValue();
    }

    protected void setDirty(final boolean pIsDirty) {
        mIsDirty = pIsDirty;
    }

    protected void setDragging(final boolean pDragging) {
        mDragging = pDragging;
    }

    protected void setDragStartValue(final R pDragStartValue) {
        mDragStartValue = pDragStartValue;
    }

    @Override
    public C setEnabled(final boolean pEnabled) {
        mEnabled = pEnabled;
        mViews.invokeAll(ui -> ui.setEnabled(pEnabled));
        mEnabledDispatcher.invokeAll(l -> l.enabledChangeEvent(this, mEnabled));
        return (C) this;
    }

    public boolean setNormalizedValue(final double pNormalizedValue) {
        throw new UnsupportedOperationException("setNormalizedValue(double pNormalizedValue) is not supported.");
    }

    @Override
    public C setTransient() {
        mIsPersistent = false;
        return (C) this;
    }

    @Override
    public void setValid(final boolean pValid) {
        mValid = pValid;
    }

    @Override
    public boolean setValue(final R pValue) {
        return setValue(pValue, null, false);
    }

    @Override
    public boolean setValue(final R pValue, final IView pView, final boolean pIsMutating) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pValue", pValue);

        final R originalValue = getValue();

        mValidateValue = pValue;
        boolean isValid = mEventDispatcher.fireControlValidate(this);

        if (isValid && mContainer != null) {
            isValid &= mContainer.fireControlValidate(this);
        }

        if (isValid) {
            setDirty(true);

            if (mValue.getClass() == pValue.getClass()) { // added for PictureControl
                mValue = (T) pValue;
            } else {
                mValue.setValue(pValue);
            }

            fireControlChangeEvent(pView, pIsMutating);
        }

        if (!mDragging && isUndoEnabled()) {
            createUndoRedoAction(originalValue, pValue);
        }
        Framework.logExit(mLogger);
        return isValid;
    }

    private boolean isUndoEnabled() {
        return mUndoEnabled;
    }

    /**
     * This removes the control from the Undo/Redo mechanism.
     *
     * @param pUndoEnabled whether this should participate or not
     * @return this control
     */
    public C setUndoEnabled(final boolean pUndoEnabled) {
        mUndoEnabled = pUndoEnabled;
        return (C) this;
    }

    @Override
    public C setVisible(final boolean pVisible) {
        mVisible = pVisible;
        mViews.invokeAll(ui -> ui.setVisible(pVisible));
        mVisibilityDispatcher.invokeAll(l -> l.visibilityChangeEvent(this, mVisible));
        return (C) this;
    }

    @Override
    public String toString() {
        final String className = this.getClass().getSimpleName();

        final StringBuffer buffer = new StringBuffer();
        buffer.append("{className:" + className);
        buffer.append(",mDisplayName:" + mDisplayName);
        buffer.append(",mPropertyName:" + mPropertyName);
        buffer.append(",mValue:" + mValue);
        buffer.append("}");
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ownimage.framework.persist.IPersist#write(com.ownimage.framework.persist.IPersistDB, java.lang.String)
     */
    @Override
    public void write(final IPersistDB pDB, final String pId) {
        if (pDB == null) {
            throw new IllegalArgumentException("pDB must not be null");
        }
        if (pId == null) {
            throw new IllegalArgumentException("pId must not be null");
        }
        if (mPropertyName == null || mPropertyName.length() == 0) {
            throw new RuntimeException("Cannot write a property that has no mPropertyName");
        }

        if (isPersistent()) {
            pDB.write(getPrefix(pId) + mPropertyName, mValue.getString());
        }
    }

    protected String getPrefix(final String pId) {
        return pId == null || pId.length() == 0 ? "" : pId + ".";
    }

    @Override
    public void addVisibileListener(final IVisibileListener pListener) {
        mVisibilityDispatcher.addListener(pListener);
    }

    @Override
    public void removeVisibleListener(final IVisibileListener pListener) {
        mVisibilityDispatcher.removeListener(pListener);
    }

    @Override
    public void addEnabledListener(final IEnabledListener pListener) {
        mEnabledDispatcher.addListener(pListener);
    }

    @Override
    public void removeEnabledListener(final IEnabledListener pListener) {
        mEnabledDispatcher.removeListener(pListener);
    }
}
