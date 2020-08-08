package com.ownimage.perception.pixelMap;

/**
 * This class needs to remain here for the deserialization of existing transforms.
 */
public class IPixelChain {

    public enum Thickness {
        Thick, Normal() {
            public String toString() {
                return "Medium";
            }
        }, Thin, None
    }
}
