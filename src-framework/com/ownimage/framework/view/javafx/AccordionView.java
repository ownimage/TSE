package com.ownimage.framework.view.javafx;

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

	private Accordion mAccordion = new Accordion();

	public AccordionView(final IContainerList pContainerList) {
		super(pContainerList);
		createView();
	}

	private void createView() {
        mUI.getChildren().remove(mScroller);
		mAccordion.getPanes().clear();

		for (int i = 0, max = mControl.getCount(); i < max; i++) {
			IContainer container = mControl.getContainer(i);
			String name = container.getDisplayName();
			Node content = ((FXView) container.createView()).getUI();
			TitledPane pane = new TitledPane(name, content);
			mAccordion.getPanes().add(pane);
		}

		// the listener needs to be added after the index is set
		setSelectedIndex(mControl.getSelectedIndex());
		mAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
			@Override
			public void changed(final ObservableValue<? extends TitledPane> pOV, final TitledPane pOldValue, final TitledPane pNewValue) {
                if (!isMutating()) {
					int index = getIndex(pNewValue);
					mControl.setSelectedIndex(index, AccordionView.this);
				}
			}
		});

		mScroller.setContent(mAccordion);
		mScroller.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		mUI.setCenter(null);
		mUI.setCenter(mScroller);
	}

	private int getIndex(final TitledPane pTitlePane) {
        return mAccordion.getPanes().indexOf(pTitlePane);
	}

	@Override
	public Node getUI() {
		return mUI;
	}

	@Override
	public void redraw() {
			runOnFXApplicationThread(this::createView);
	}

	@Override
	public void setSelectedIndex(final int pInt) {
		runOnFXApplicationThread(() -> setSelectedIndexAsync(pInt));
	}

	private void setSelectedIndexAsync(final int pInt) {
		try {
            setMutating(true);

			if (pInt < -1) { throw new IllegalArgumentException("pInt = " + pInt + ".  pInt must be >= -1."); }

            int numberOfPanes = mAccordion.getPanes().size();
			if (pInt >= numberOfPanes) {
				throw new IllegalArgumentException("pInt = " + pInt + ".   mPanes.size() = " + numberOfPanes + " pInt must be < mPanes.size().");
			}

			if (pInt >= 0) {
                TitledPane selected = mAccordion.getPanes().get(pInt);
				mAccordion.setExpandedPane(selected);
			} else {
				mAccordion.setExpandedPane(null);
			}
		} finally {
            setMutating(false);
		}
	}


}
