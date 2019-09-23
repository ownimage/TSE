/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.google.common.annotations.VisibleForTesting;
import com.ownimage.framework.app.menu.MenuAction;
import com.ownimage.framework.app.menu.MenuControl;
import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.control.*;
import com.ownimage.framework.control.event.IControlChangeListener;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.control.layout.*;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.KColor;
import com.ownimage.framework.view.*;
import com.ownimage.framework.view.factory.IViewFactory;
import com.ownimage.framework.view.factory.ViewFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Vector;

public class FXViewFactory implements IViewFactory {

    private static final FXViewFactory mFXViewFactory = new FXViewFactory();

    private final Vector<IControlChangeListener<?>> mListeners = new Vector<>();

    private final UndoRedoBuffer mPropertiesUndoRedo = new UndoRedoBuffer(100);

    private final IContainer mPropertiesContainer = new Container("FXView", "fxView", mPropertiesUndoRedo);
    // define Controls
    private final IntegerControl mControlWidth = new IntegerControl("Control Width", "controlWidth", mPropertiesContainer, 200, 50, 500, 20);
    private final IntegerControl mLabelWidth = new IntegerControl("Label Width", "labelWidth", mPropertiesContainer, 150, 50, 500, 20);
    private final StringControl mDoubleFormat = new StringControl("Double format", "doubleFormat", mPropertiesContainer, "%.2f");
    private final IntegerControl mSliderValueWidthWidth = new IntegerControl("Slider Value Width", "sliderValueWidth", mPropertiesContainer, 50, 10, 100, 20);
    private final IntegerControl mLabelRightPadding = new IntegerControl("Label Right Padding", "labelRightPadding", mPropertiesContainer, 5, 0, 20, 1);
    private final IntegerControl mContainerBottomPadding = new IntegerControl("Container Bottom Padding", "containerBottomPadding", mPropertiesContainer, 5, 0, 20, 1);
    private final IntegerControl mSmallButtonWidth = new IntegerControl("Small Button Width", "smallButtonWidth", mPropertiesContainer, 50, 20, 150, 10);
    private final IntegerControl mProgressBarHeight = new IntegerControl("Progress Bar Height", "progressBarHeight", mPropertiesContainer, 25, 5, 50, 5);
    private final ColorControl mProgressNormalColor = new ColorControl("Progress Bar Color Normal", "progressColorNormal", mPropertiesContainer, java.awt.Color.BLUE);
    private final ColorControl mProgressCompleteColor = new ColorControl("Progress Bar Color Complete", "progressColorComplete", mPropertiesContainer, java.awt.Color.GREEN);

    // define exposed properties
    public SimpleIntegerProperty controlWidthProperty = new SimpleIntegerProperty(mControlWidth.getValue());
    public SimpleIntegerProperty labelWidthProperty = new SimpleIntegerProperty(mLabelWidth.getValue());
    public SimpleStringProperty doubleFormat = new SimpleStringProperty(mDoubleFormat.getValue());
    public SimpleIntegerProperty sliderValueWidthProperty = new SimpleIntegerProperty(mSliderValueWidthWidth.getValue());
    public SimpleIntegerProperty labelRightPaddingProperty = new SimpleIntegerProperty(mLabelRightPadding.getValue());
    public SimpleIntegerProperty containerBottomPaddingProperty = new SimpleIntegerProperty(mContainerBottomPadding.getValue());
    public SimpleIntegerProperty smallButtonWidthProperty = new SimpleIntegerProperty(mSmallButtonWidth.getValue());
    public SimpleIntegerProperty progressBarHeight = new SimpleIntegerProperty(mProgressBarHeight.getValue());

    public int getProgressBarHeight() {
        return mProgressBarHeight.getValue();
    }

    public String getProgressNormalColorHex() {
        return KColor.toHex(mProgressNormalColor.getValue());
    }

    public String getProgressCompleteColorHex() {
        return KColor.toHex(mProgressCompleteColor.getValue());
    }

    public FXViewFactory() {
        // define listeners, the property is updated with the value of the control
        addListener(mControlWidth, (c, m) -> controlWidthProperty.set(((IntegerControl) c).getValue()));
        addListener(mLabelWidth, (c, m) -> labelWidthProperty.set(((IntegerControl) c).getValue()));
        addListener(mDoubleFormat, (c, m) -> doubleFormat.set(((StringControl) c).getValue()));
        addListener(mSliderValueWidthWidth, (c, m) -> sliderValueWidthProperty.set(((IntegerControl) c).getValue()));
        addListener(mLabelRightPadding, (c, m) -> labelRightPaddingProperty.set(((IntegerControl) c).getValue()));
        addListener(mContainerBottomPadding, (c, m) -> containerBottomPaddingProperty.set(((IntegerControl) c).getValue()));
        addListener(mSmallButtonWidth, (c, m) -> smallButtonWidthProperty.set(((IntegerControl) c).getValue()));
        addListener(mProgressBarHeight, (c, m) -> progressBarHeight.set(((IntegerControl) c).getValue()));
    }

    public static FXViewFactory getInstance() {
        return mFXViewFactory;
    }

    /**
     * Sets this as the view factory. MUST be called from the main thread.
     */
    public static void setAsViewFactory() {
        setAsViewFactory(true);
    }

    /**
     * Sets this as the view factory. MUST be called from the main thread.
     */
    @VisibleForTesting
    public static void setAsViewFactory(boolean pThrowException) {
        if (!"main".equals(Thread.currentThread().getName())) {
            throw new IllegalStateException("setAsViewFactory must be called from the main thread");
        }
        try {
            ViewFactory.setViewFactory(getInstance());
        } catch (IllegalStateException pISE) {
            if (pThrowException) throw pISE;
        }
        ApplicationEventQueue.getInstance(); // this initializes the event queue of the main thread group
    }

    /**
     * This clears the view factory, this is intended to be used for testing only.
     */
    public static void clearViewFactory() {
        ViewFactory.clearViewFactory();
    }

    /**
     * Adds the listener to the control. If the listener is lambda expression there is a good chance that it will end up being
     * garbage collected. Hence it is put into a persistent collection to prevent that from happening.
     *
     * @param pControl  the control
     * @param pListener the listener
     */
    private void addListener(final IControl<?, ?, ?, ?> pControl, final IControlChangeListener<?> pListener) {
        mListeners.add(pListener);
        pControl.addControlChangeListener(pListener);
    }

    @Override
    public IView createMenuItemView(final MenuAction pMenuAction) {
        return new MenuItemView(pMenuAction);
    }

    @Override
    public IView createMenuView(final MenuControl pMenuControl) {
        return new MenuView(pMenuControl);
    }

    @Override
    public IView createView(final ActionControl pActionControl) {
        return new ActionView(pActionControl);
    }

    @Override
    public IView createView(final BooleanControl pBooleanControl) {
        return new BooleanView(pBooleanControl);
    }

    @Override
    public IBorderView createView(final BorderLayout pBorder) {
        return new BorderView(pBorder);
    }

    @Override
    public IView createView(final ColorControl pColorControl) {
        return new ColorView(pColorControl);
    }

    @Override
    public IDoubleView createView(final DoubleControl pDoubleControl) {
        return new DoubleView(pDoubleControl);
    }

    @Override
    public IView createView(final FileControl pFileControl) {
        return new FileView(pFileControl);
    }

    @Override
    public IView createView(final HFlowLayout pHFlow) {
        return new HFlowView(pHFlow);
    }

    @Override
    public IView createView(final HSplitLayout pHSplit) {
        return new HSplitView(pHSplit);
    }

    @Override
    public IView createView(final IContainer pContainer) {
        return new ContainerView(pContainer);
    }

    @Override
    public ISingleSelectView createView(final IContainerList pContainerList) {
        return new AccordionView(pContainerList);
    }

    @Override
    public ISingleSelectView createView(final INamedTabs pNamedTabs) {
        return new NamedTabsView(pNamedTabs);
    }

    @Override
    public IView createView(final IntegerControl pIntegerControl) {
        return new IntegerView(pIntegerControl);
    }

    @Override
    public IView createView(final MenuControl pMenu) {
        return new MenuBarView(pMenu);
    }

    @Override
    public IView createView(final ObjectControl pObjectControl) {
        return new ObjectView(pObjectControl);
    }

    @Override
    public IPictureView createView(final PictureControl pPictureControl) {
        return new PictureView(pPictureControl);
    }

    @Override
    public IView createView(final ScrollLayout pScrollLayout) {
        return new ScrollView(pScrollLayout);
    }

    @Override
    public IView createView(final StringControl pStringControl) {
        return new StringView(pStringControl);
    }

    @Override
    public IView createView(final VFlowLayout pVFlow) {
        return new VFlowView(pVFlow);
    }

    @Override
    public IView createView(final ProgressControl pProgressControl) {
        return new ProgressView(pProgressControl);
    }

    @Override
    public UndoRedoBuffer getPropertiesUndoRedoBuffer() {
        return mPropertiesUndoRedo;
    }

    @Override
    public IContainer getViewFactoryPropertiesViewable() {
        return mPropertiesContainer;
    }

    @Override
    public void showDialog(final FileControl pFileControl) {
        final AppControlView app = AppControlView.getInstance();
        app.showDialog(pFileControl);
    }

    @Override
    public IDialogView createDialog(final IViewable pViewable, final IAppControlView.DialogOptions pDialogOptions, final UndoRedoBuffer pUndoRedo, final ActionControl... pButtons) {
        return new DialogView(pViewable, pDialogOptions, pUndoRedo, pButtons);
    }
}
