package com.ownimage.framework.view;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ownimage.framework.control.control.ActionControl;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.undo.IUndoRedoBuffer;
import com.ownimage.framework.util.Framework;

public interface IAppControlView {

    public final static Logger mLogger = Framework.getLogger();

    public class DialogOptions {

        public static DialogOptions NONE = new DialogOptions();

        public static Builder builder() {
            return new Builder();
        }

        private Optional<Consumer<Optional<ActionControl>>> mCompleteFunciton;

        public static class Builder {
            private Optional<Consumer<Optional<ActionControl>>> mCompleteFunciton = Optional.empty();

            private Builder() {
            }

            public DialogOptions build() {
                DialogOptions dialogOptions = new DialogOptions();
                dialogOptions.mCompleteFunciton = mCompleteFunciton;
                return dialogOptions;
            }

            public Builder withCompleteFunction(Consumer<Optional<ActionControl>> pCompleteFunciton) {
                Framework.checkParameterNotNull(mLogger, mCompleteFunciton, "mCompleteFunction");
                mCompleteFunciton = Optional.of(pCompleteFunciton);
                return this;
            }
        }

        private DialogOptions() {
        }

        public Optional<Consumer<Optional<ActionControl>>> getCompleteFunction() {
            return mCompleteFunciton;
        }

    }

    public void exit();

    public void redraw();

    public void showDialog(IViewable pViewable, DialogOptions pOptions, ActionControl... pButtons);

    public void showDialog(IViewable pViewable, DialogOptions pOptions, final IUndoRedoBuffer mUndoRedo, ActionControl... pButtons);

}
