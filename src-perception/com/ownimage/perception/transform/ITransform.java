/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.perception.transform;

import com.ownimage.framework.control.container.IContainer;
import com.ownimage.framework.control.layout.IViewable;
import com.ownimage.framework.persist.IPersist;
import com.ownimage.framework.util.Version;
import com.ownimage.perception.render.IBatchTransform;

public interface ITransform extends IBatchTransform, IPersist { // extends , IGrafitti, ITransformColor, IProgressBar,
    // IPictureReadOnly {

    public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

    public ITransform duplicate();

    public IViewable<?> getContent();

    // TODO why do I need get Content and getControls

    public IContainer getControls();

    @Override
    public String getDisplayName();

    public int getHeight();

    @Override
    public ITransform getPreviousTransform();

    public int getWidth();

    public int getOversample();

    public void setPreviousTransform(ITransform pPreviousTransform);

    public void setValues();

    /**
     * Update the input preview image.
     */
    public void updatePreview();

}
