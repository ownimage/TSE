package com.ownimage.perception.persist;

import java.util.TreeMap;

import com.ownimage.framework.persist.IPersistDB;

public class PersistDBImpl implements IPersistDB {
	
	private TreeMap<String, String> values = new TreeMap<String, String>();

	@Override
	public String read(String pId) {
		return values.get(pId);
	}

	@Override
	public void write(String pId, String pValue) {
		values.put(pId, pValue);
		System.out.println("PersistDBImpl.write(\""+ pId + "\", \"" + pValue + "\")");
	}

}
