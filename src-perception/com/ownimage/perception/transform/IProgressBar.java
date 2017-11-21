package com.ownimage.perception.transform;

public interface IProgressBar {
	// at some point need to look at overlap with IProgressObserver

	public abstract void hideProgressBar();

	public abstract void showProgressBar();

	public abstract void showProgressBar(String pInfoString, int pPercent);

}