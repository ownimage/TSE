package com.ownimage.framework.util.snapshot;

public class Link<M> implements Node<M> {

    private Node<M> mToMaster;

    public Link(final Node<M> pToMaster) {
        mToMaster = pToMaster;
    }

    @Override
    public M getMaster(final Node<M> pToMe, final Object pSecret) {
        M master = mToMaster.getMaster(this, pSecret);
        mToMaster = pToMe; // only set this one have sucessfully gotten the master
        return master;
    }

}
