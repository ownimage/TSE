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


    public final static int SW = 0;
    public final static int S = 1;
    public final static int SE = 2;
    public final static int W = 3;
    public final static int E = 5;
    public final static int NW = 6;
    public final static int N = 7;
    public final static int NE = 8;

    public final static byte ALL = (byte) 127;
    public final static byte EDGE = (byte) 64;
    public final static byte VISITED = (byte) 32;
    public final static byte IN_CHAIN = (byte) 16;
    public final static byte NODE = (byte) 8;

}
