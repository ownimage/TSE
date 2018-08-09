package com.ownimage.perception.math;

import java.util.Objects;

public class RectangleSize {


    private int mWidth;
    private int mHeight;

    public RectangleSize(final int pWidth, final int pHeight) {
        this.mWidth = pWidth;
        this.mHeight = pHeight;
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

    public RectangleSize scaleToSquare(int pSize) {
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
