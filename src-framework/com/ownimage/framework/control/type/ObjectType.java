/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

public class ObjectType<R> extends TypeBase<ObjectMetaType<R>, R> {

    public ObjectType(final R pValue, final ObjectMetaType<R> pMetaModel) {
        super(pValue, pMetaModel);

        if (pMetaModel == null) {
            throw new IllegalArgumentException("ObjectType pMetaModel cannot be null.");
        }
    }

    @Override
    public ObjectType<R> clone() {
        return new ObjectType<R>(mValue, mMetaModel);
    }

    @Override
    public void setString(final String pValue) {
        ObjectMetaType<R> meta = getMetaModel();
        if (meta != null) {
            meta.setString(this, pValue);
        }
    }

}
