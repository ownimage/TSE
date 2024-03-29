/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view.javafx;

import com.ownimage.framework.control.layout.INamedTabs;
import com.ownimage.framework.view.ISingleSelectView;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;

public class NamedTabsView extends ViewBase<INamedTabs> implements ISingleSelectView {

    private TabPane mUI = new TabPane();

    public NamedTabsView(final INamedTabs pNamedTabs) {
        super(pNamedTabs);
        createView();
    }

    private void createView() {

        mUI = new TabPane();
        mUI.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        for (final String name : mControl.getTabNames()) {

            final Tab tab = new Tab();
            tab.setText(name);
            tab.setContent(((FXView) (mControl.getViewable(name).createView())).getUI());

            mUI.getTabs().add(tab);
        }

        setSelectedIndex(mControl.getSelectedIndex());

        // adding the listener needs to be after creating the TabPane otherwise it fires an unwanted tab change
        mUI.getSelectionModel().selectedItemProperty()
                .addListener((ov, oldTab, newTab) -> mControl.setSelectedIndex(mUI.getTabs().indexOf(newTab), this));
    }

    @Override
    public Node getUI() {
        return mUI;
    }

    @Override
    public void redraw() {
    }

    @Override
    public void setSelectedIndex(final int pInt) {
        runOnFXApplicationThread(() -> mUI.getSelectionModel().select(pInt));
    }

}
