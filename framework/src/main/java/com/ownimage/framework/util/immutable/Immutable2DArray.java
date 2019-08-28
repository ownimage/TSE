package com.ownimage.framework.util.immutable;

import com.ownimage.framework.util.Framework;
import lombok.val;

import java.util.logging.Logger;

public class Immutable2DArray<E> {

    private static Logger mLogger = Framework.getLogger();

    final private int mWidth;
    final private int mHeight;
    final private int mShift;
    final private int mHashGenShift;
    final private int mSize;
    final private int mAnd;
    private Node<E> mNode;

    private class Node<E> {
        final private Node<E>[] mNodes;
        final private E[] mValues;

        public Node() {
            mNodes = new Node[mSize];
            mValues = (E[]) new Object[mSize];
        }

        public Node(final Node<E> pFrom) {
            mNodes = pFrom.mNodes.clone();
            mValues = pFrom.mValues.clone();
        }

        public E get(int pHash) {
            val index = pHash & mAnd;
            val rest = pHash >> mShift;
            if (rest == 0) return mValues[index];
            var node = mNodes[index];
            if (node != null) return node.get(rest);
            return null;
        }

        public Node<E> set(int pHash, E pValue) {
            val index = pHash & mAnd;
            val rest = pHash >> mShift;
            if (rest == 0) {
                val myValue = mValues[index];
                if ((pValue == null && myValue == null) || (pValue != null && pValue.equals(myValue))) {
                    return this;
                }
                var clone = new Node<E>(this);
                clone.mValues[index] = pValue;
                return clone;
            }
            var node = mNodes[index] == null
                    ? new Node<E>().set(rest, pValue)
                    : mNodes[index].set(rest, pValue);
            if (node == mNodes[index]) return this;
            var clone = new Node<E>(this);
            clone.mNodes[index] = node;
            return clone;
        }
    }

    public Immutable2DArray(final int pWidth, final int pHeight) {
        this(pWidth, pHeight, 5);
    }

    public Immutable2DArray(final int pWidth, final int pHeight, final int pFactor) {
        Framework.checkParameterGreaterThanEqual(mLogger, pFactor, 0, "pFactor");
        mShift = pFactor;
        mSize = 1 << pFactor;
        mAnd = mSize - 1;
        mWidth = pWidth;
        mHeight = pHeight;
        mNode = new Node();
        mHashGenShift = hashGenShift(pWidth);
    }

    public Immutable2DArray(final Immutable2DArray pOther) {
        mShift = pOther.mShift;
        mSize = pOther.mSize;
        mAnd = pOther.mAnd;
        mWidth = pOther.mWidth;
        mHeight = pOther.mHeight;
        mNode = pOther.mNode;
        mHashGenShift = pOther.mHashGenShift;
    }

    private int hashGenShift(int pWidth) {
        var i = 1;
        while (1 << i < pWidth) i++;
        return i;
    }

    public E get(final int pX, final int pY) {
        val hash = hash(pX, pY);
        return mNode.get(hash);
    }

    public Immutable2DArray<E> set(final int pX, final int pY, final E pValue) {
        val hash = hash(pX, pY);
        val node = mNode.set(hash, pValue);
        if (node == mNode) return this;
        return withNode(node);
    }

    public Immutable2DArray<E> clear() {
        return new Immutable2DArray<>(mWidth, mHeight);
    }

    private int hash(final int pX, final int pY) {
        Framework.checkParameterGreaterThanEqual(mLogger, pX, 0, "pX");
        Framework.checkParameterGreaterThanEqual(mLogger, pY, 0, "pY");
        Framework.checkParameterLessThan(Framework.mLogger, pX, mWidth, "pX");
        Framework.checkParameterLessThan(Framework.mLogger, pY, mHeight, "pY");
        return (pY << mHashGenShift) + pX;
    }

    private Immutable2DArray<E> withNode(final Node<E> pNode) {
        val clone = new Immutable2DArray<E>(this);
        clone.mNode = pNode;
        return clone;
    }
}
