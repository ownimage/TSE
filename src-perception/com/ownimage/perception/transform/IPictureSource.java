package com.ownimage.perception.transform;

import java.awt.Color;

public interface IPictureSource {

	public Color getColor(int x, int y);

	public int getHeight();

	public int getWidth();

}
