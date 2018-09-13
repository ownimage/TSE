package com.ownimage.framework.persist;

import com.ownimage.framework.util.Framework;

import java.util.TreeMap;
import java.util.logging.Logger;

public class PersistDBImpl implements IPersistDB {

    public final static Logger mLogger = Framework.getLogger();

    private TreeMap<String, String> values = new TreeMap<String, String>();

    @Override
    public String read(String pId) {
        return values.get(pId);
    }

    @Override
    public void write(String pId, String pValue) {
        values.put(pId, pValue);
        mLogger.fine(() -> String.format("PersistDBImpl.write(%s, %s)", pId, pValue));
    }

}
