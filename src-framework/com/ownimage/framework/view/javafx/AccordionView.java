package com.ownimage.framework.view.javafx;

import java.util.List;
import java.util.Vector;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.layout.IContainerList;
import com.ownimage.framework.view.ISingleSelectView;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;

public class AccordionView extends ViewBase<IContainerList> implements ISingleSelectView {

	private final BorderPane mUI = new BorderPane();
	private final ScrollPane mScroller = new ScrollPane();
	private Accordion mAccordion;
	private boolean mIsMutating = false;

	/**
	 * The Panes list. This is used to support the setSelectedIndex as Accordion does not support setExpandedPane(int) only
	 * setExpandedPane(TitledPane). This is used to convert from one to the other.
	 */
	private List<TitledPane> mPanes;

	public AccordionView(final IContainerList pContainerList) {
		super(pContainerList);
		createView();
	}

	private void createView() {
		// mUI.getChildren().remove(mScroller);
		mAccordion = new Accordion();
		mPanes = new Vector<TitledPane>();

		for (int i = 0, max = mControl.getCount(); i < max; i++) {
			IContainer container = mControl.getContainer(i);
			String name = container.getDisplayName();
			Node content = ((FXView) container.createView()).getUI();
			TitledPane pane = new TitledPane(name, content);
			mAccordion.getPanes().add(pane);
			mPanes.add(pane);
		}

		// the listener needs to be added after the index is set
		setSelectedIndex(mControl.getSelectedIndex());
		mAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
			@Override
			public void changed(final ObservableValue<? extends TitledPane> pOV, final TitledPane pOldValue, final TitledPane pNewValue) {
				if (!mIsMutating) {
					int index = getIndex(pNewValue);
					mControl.setSelectedIndex(index, AccordionView.this);
				}
			}
		});

		mScroller.setContent(mAccordion);
		mScroller.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		mUI.setCenter(mScroller);
	}

	private int getIndex(final TitledPane pTitlePane) {
		return mPanes.indexOf(pTitlePane);
	}

	@Override
	public Node getUI() {
		return mUI;
	}

	@Override
	public void redraw() {
		createView();
	}

	@Override
	public void setSelectedIndex(final int pInt) {
		try {
			mIsMutating = true;

			if (pInt < -1) { throw new IllegalArgumentException("pInt = " + pInt + ".  pInt must be >= -1."); }

			int numberOfPanes = mPanes.size();
			if (pInt >= numberOfPanes) { throw new IllegalArgumentException("pInt = " + pInt + ".   mPanes.size() = " + numberOfPanes + " pInt must be < mPanes.size()."); }

			if (pInt >= 0) {
				TitledPane selected = mPanes.get(pInt);
				mAccordion.setExpandedPane(selected);
			} else {
				mAccordion.setExpandedPane(null);
			}
		} finally {
			mIsMutating = false;
		}
	}

}
