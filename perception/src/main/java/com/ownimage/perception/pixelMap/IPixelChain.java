package com.ownimage.perception.pixelMap;

public class IPixelChain {
    public enum Thickness {
        Thick, Normal() {
            public String toString() {
                return "Medium";
            }
        }, Thin, None
    }
}
