package com.ownimage.perception.transform;

import java.awt.*;
import java.util.Optional;

public interface IPictureSource {

	public Optional<Color> getColor(int x, int y);

	public int getHeight();

	public int getWidth();

}
