/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

/**
 * The Interface PixelConstants. This is so that these constants can be shared between all of the Pixel related classes.
 */
public interface PixelConstants {


    int SW = 0;
    int S = 1;
    int SE = 2;
    int W = 3;
    int E = 5;
    int NW = 6;
    int N = 7;
    int NE = 8;

    byte ALL = (byte) 127;
    byte EDGE = (byte) 64;
    byte VISITED = (byte) 32;
    byte IN_CHAIN = (byte) 16;
    byte NODE = (byte) 8;

}
