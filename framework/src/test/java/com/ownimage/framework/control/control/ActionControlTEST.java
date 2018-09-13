/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.Container;
import com.ownimage.framework.undo.IUndoRedoProviderASSISTANT;
import org.junit.*;

public class ActionControlTEST {

    private Container mContainer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    // Test 01 Constructors
    // Test allows space in name
    @Test
    public void ActionControl_0_00() {
        final ActionControl a = new ActionControl("Action Control", "actionControl", mContainer, () -> actionTestNull());
        // TODO
    }

    private void actionTestNull() {
    }

    @Before
    public void setUp() throws Exception {
        mContainer = new Container("x", "x", new IUndoRedoProviderASSISTANT());
    }

    @After
    public void tearDown() throws Exception {
    }

}
