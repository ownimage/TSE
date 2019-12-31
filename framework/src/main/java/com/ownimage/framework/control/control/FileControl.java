/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.control;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.type.FileType;
import com.ownimage.framework.control.type.FileType.FileControlType;
import com.ownimage.framework.control.type.StringMetaType;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.view.IView;
import com.ownimage.framework.view.factory.ViewFactory;
import io.vavr.Tuple2;
import io.vavr.collection.List;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

public class FileControl extends ControlBase<FileControl, FileType, StringMetaType, String, IView> {


    public final static Logger mLogger = Framework.getLogger();
    public final static long serialVersionUID = 1L;

    private File mFile;

    public FileControl(
            final String pDisplayName,
            final String pPropertyName,
            final IContainer pContainer,
            final String pValue,
            final FileControlType pFileControlType,
            final List<Tuple2<String, List<String>>> pExtensions
    ) {
        super(pDisplayName, pPropertyName, pContainer, new FileType(pValue, pFileControlType, pExtensions));
    }

    public static FileControl createFileOpen(
            final String pDisplayName,
            final IContainer pContainer,
            final String pFilename
    ) {
        return new FileControl(pDisplayName, "fileName", pContainer, pFilename, FileControlType.FILEOPEN, null);
    }

    public static FileControl createFileOpen(
            final String pDisplayName,
            final IContainer pContainer,
            final String pFilename,
            final List<Tuple2<String, List<String>>> pExtensions
    ) {
        return new FileControl(
                pDisplayName,
                "fileName",
                pContainer,
                pFilename,
                FileControlType.FILEOPEN,
                pExtensions
        );
    }

    public static FileControl createFileSave(
            final String pDisplayName,
            final IContainer pContainer,
            final String pFilename
    ) {
        return new FileControl(pDisplayName, "fileName", pContainer, pFilename, FileControlType.FILESAVE, null);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public IView createView() { // TODO can we push this into the base class
        final IView view = ViewFactory.getInstance().createView(this);
        addView(view);
        return view;
    }

    public synchronized File getFile() {
        if (mFile == null) {
            final String file = getValue();
            mFile = file != null ? new File(file) : null;
        }
        return mFile;
    }

    public FileControlType getFileControlType() {
        return mValue.getFileControlType();
    }

    public Optional<List<Tuple2<String, List<String>>>> getExtensions() {
        return mValue.getExtensions();
    }

    @Override
    public boolean setValue(final String pValue) {
        mFile = null;
        return super.setValue(pValue);
    }

    @Override
    public boolean setValue(final String pValue, final IView pView, final boolean pIsMutating) {
        mFile = null;
        return super.setValue(pValue, pView, pIsMutating);
    }

    public void showDialog() {
        ViewFactory.getInstance().showDialog(this);

    }

}
