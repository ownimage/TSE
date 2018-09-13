/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.framework.control.type;

import java.awt.*;
import java.util.Optional;

public interface IPictureSource {

    public Optional<Color> getColor(int x, int y);

    public int getHeight();

    public int getWidth();

}
