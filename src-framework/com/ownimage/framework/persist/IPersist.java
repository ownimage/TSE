/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012-2015 ownimage.com, Keith Hart
 */
package com.ownimage.framework.persist;

import java.io.IOException;

import com.ownimage.framework.util.Version;

public interface IPersist {

    public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");

    public String getPropertyName();

    /**
     * Checks if is persistent.
     *
     * @return true, if is persistent
     */
    public boolean isPersistent();

    /**
     * Checks whether this object can read itself, it is uses as a null test.
     *
     * @param pDB persistent data source
     * @param pId key
     * @return whether the pDB source contains the key
     */
    default boolean canRead(IPersistDB pDB, String pId) {
        return false;
    }

    public void read(IPersistDB pDB, String pId);

    public void write(IPersistDB pDB, String pId) throws IOException;
}
