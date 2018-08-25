/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */

package com.ownimage.framework.persist;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

// from http://www.rgagnon.com/javadetails/java-0614.html

@SuppressWarnings("serial")
public class SortedProperties extends Properties implements IPersistDB {


    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized Enumeration keys() {
        Enumeration keysEnum = super.keys();
        Vector keyList = new Vector();
        while (keysEnum.hasMoreElements()) {
            keyList.add(keysEnum.nextElement());
        }
        Collections.sort(keyList);
        return keyList.elements();
    }

    @Override
    public String read(final String pId) {
        return getProperty(pId);
    }

    @Override
    public void write(final String pId, final String pValue) {
        setProperty(pId, pValue);

    }

}