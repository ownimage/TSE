/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.view;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.control.IAction;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.UndoRedoBuffer;
import com.ownimage.framework.util.Framework;

import java.util.Optional;
import java.util.logging.Logger;

public interface IAppControlView {

    Logger mLogger = Framework.getLogger();

    class DialogOptions {

        public static DialogOptions NONE = DialogOptions.builder().build();

        public static Builder builder() {
            return new Builder();
        }

        private IAction mCompleteFunction = () -> {
        };

        public static class Builder {
            private final DialogOptions mDialogOptions = new DialogOptions();

            private Builder() {
            }

            public DialogOptions build() {
                final DialogOptions dialogOptions = new DialogOptions();
                dialogOptions.mCompleteFunction = mDialogOptions.mCompleteFunction;
                return dialogOptions;
            }

            public Builder withCompleteFunction(final IAction pCompleteFunciton) {
                Framework.checkParameterNotNull(mLogger, pCompleteFunciton, "pCompleteFunction");
                mDialogOptions.mCompleteFunction = pCompleteFunciton;
                return this;
            }
        }

        private DialogOptions() {
        }

        public Optional<IAction> getCompleteFunction() {
            return Optional.ofNullable(mCompleteFunction);
        }

    }

    void exit();

    void redraw();

    void showDialog(IViewable pViewable, DialogOptions pOptions, ActionControl... pButtons);

    void showDialog(IViewable pViewable, DialogOptions pOptions, final UndoRedoBuffer mUndoRedo, ActionControl... pButtons);

}
