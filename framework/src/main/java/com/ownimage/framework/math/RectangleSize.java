/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.math;

import com.ownimage.framework.control.control.PictureControl;
import com.ownimage.framework.control.type.PictureType;

import java.util.Objects;

public class RectangleSize {


    private final int mWidth;
    private final int mHeight;

    public RectangleSize(final int pWidth, final int pHeight) {
        this.mWidth = pWidth;
        this.mHeight = pHeight;
    }

    public RectangleSize(final PictureControl pPictureControl) {
        this(pPictureControl.getWidth(), pPictureControl.getHeight());
    }

    public RectangleSize(final PictureType pPictureType) {
        this(pPictureType.getWidth(), pPictureType.getHeight());
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RectangleSize that = (RectangleSize) o;
        return mWidth == that.mWidth &&
                mHeight == that.mHeight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mWidth, mHeight);
    }

    public RectangleSize scaleToSquare(final int pSize) {
        if (mWidth >= mHeight) {
            return new RectangleSize(pSize, mHeight * pSize / mWidth);
        } else {
            return new RectangleSize(mWidth * pSize / mHeight, pSize);
        }
    }

    @Override
    public String toString() {
        return String.format("RectangleSize(%s, %s)", mWidth, mHeight);
    }
}
