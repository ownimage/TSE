/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.view;

import java.util.Optional;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.IUndoRedoBuffer;
import com.ownimage.framework.util.Framework;

public interface IAppControlView {

    Logger mLogger = Framework.getLogger();

    class DialogOptions {

        public static DialogOptions NONE = new DialogOptions.Builder().build();

        public static Builder builder() {
            return new Builder();
        }

        private IAction mCompleteFunciton;

        public static class Builder {
            private DialogOptions mDialogOptions = new DialogOptions();

            private Builder() {
            }

            public DialogOptions build() {
                DialogOptions dialogOptions = new DialogOptions();
                dialogOptions.mCompleteFunciton = mDialogOptions.mCompleteFunciton;
                return dialogOptions;
            }

            public Builder withCompleteFunction(IAction pCompleteFunciton) {
                Framework.checkParameterNotNull(mLogger, pCompleteFunciton, "pCompleteFunction");
                mDialogOptions.mCompleteFunciton = pCompleteFunciton;
                return this;
            }
        }

        private DialogOptions() {
        }

        public Optional<IAction> getCompleteFunction() {
            return Optional.ofNullable(mCompleteFunciton);
        }

    }

    void exit();

    void redraw();

    void showDialog(IViewable pViewable, DialogOptions pOptions, ActionControl... pButtons);

    void showDialog(IViewable pViewable, DialogOptions pOptions, final IUndoRedoBuffer mUndoRedo, ActionControl... pButtons);

}
