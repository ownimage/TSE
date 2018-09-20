package com.ownimage.framework.util.snapshot;


public interface Node<M> {

    M getMaster(Node<M> pToMe, final Object pSecret);
}
