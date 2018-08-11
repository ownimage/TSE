/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.math.RectangleSize;
import com.ownimage.perception.render.IBatchTransform;

public interface ITransform extends IBatchTransform, IPersist { // extends , IGrafitti, ITransformColor, IProgressBar,
    // IPictureReadOnly {

    public Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

    ITransform duplicate();

    IViewable<?> getContent();

    // TODO why do I need get Content and getControls

    IContainer getControls();

    @Override
    String getDisplayName();

    int getHeight();

    @Override
    ITransform getPreviousTransform();

    int getWidth();

    RectangleSize getSize();

    int getOversample();

    void setPreviousTransform(ITransform pPreviousTransform);

    void setValues();

    /**
     * Update the input preview image.
     */
    void updatePreview();

    /*
     * This is called once after construction to let the Transform know that it should start processing event properly.
     */
    void setInitialized();

}
