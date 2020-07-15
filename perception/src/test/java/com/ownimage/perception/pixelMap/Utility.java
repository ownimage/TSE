package com.ownimage.perception.pixelMap;

import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.Services;
import com.ownimage.perception.transform.CannyEdgeTransform;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

class Utility {

    private static PixelMapService pixelMapService = Services.getDefaultServices().getPixelMapService();
    private static PixelMapApproximationService pixelMapApproximationService = Services.getDefaultServices().getPixelMapApproximationService();

    static IPixelMapTransformSource getDefaultTransformSource(final int pHeight) {
        return new IPixelMapTransformSource() {
            @Override
            public int getHeight() {
                return pHeight;
            }

            @Override
            public Color getLineColor() {
                return null;
            }

            @Override
            public double getLineCurvePreference() {
                return 1.2;
            }

            @Override
            public int getLineEndLengthPercent() {
                return 0;
            }

            @Override
            public int getLineEndLengthPixel() {
                return 0;
            }

            @Override
            public CannyEdgeTransform.LineEndLengthType getLineEndLengthType() {
                return null;
            }

            @Override
            public CannyEdgeTransform.LineEndShape getLineEndShape() {
                return null;
            }

            @Override
            public double getLineEndThickness() {
                return 0;
            }

            @Override
            public double getLineOpacity() {
                return 0;
            }

            @Override
            public double getLineTolerance() {
                return 1.2;
            }

            @Override
            public double getLongLineThickness() {
                return 0;
            }

            @Override
            public double getMediumLineThickness() {
                return 0;
            }

            @Override
            public Color getPixelColor() {
                return null;
            }

            @Override
            public Color getShadowColor() {
                return null;
            }

            @Override
            public double getShadowOpacity() {
                return 0;
            }

            @Override
            public double getShadowThickness() {
                return 0;
            }

            @Override
            public double getShadowXOffset() {
                return 0;
            }

            @Override
            public double getShadowYOffset() {
                return 0;
            }

            @Override
            public double getShortLineThickness() {
                return 0;
            }

            @Override
            public boolean getShowPixels() {
                return false;
            }

            @Override
            public boolean getShowLines() {
                return false;
            }

            @Override
            public boolean getShowShadow() {
                return false;
            }
        };
    }

    static PixelMap createMap(final int pX, final int pY) {
        final PixelMap pixelMap = new PixelMap(pX, pY, true, getDefaultTransformSource(pY));
        pixelMapApproximationService.actionProcess(pixelMap, null);
        return pixelMap;
    }

    static IPixelMapTransformSource getTransformSource(String[] map) {
        return getDefaultTransformSource(map.length);
    }

    static PixelMap createMap(final String[] map) {
        return createMap(map, getDefaultTransformSource(map.length));
    }

    static PixelMap createMap(final String[] map, final IPixelMapTransformSource transformSource) {
        final PixelMap pixelMap = new PixelMap(map[0].length(), map.length, true, transformSource);
        setMap(pixelMap, map);
        return pixelMap;
    }

    static String[] getMap(final PixelMap pPixelMap) {
        final String[] map = new String[pPixelMap.getHeight()];
        for (int y = 0; y < pPixelMap.getHeight(); y++) {
            final StringBuffer row = new StringBuffer();
            for (int x = 0; x < pPixelMap.getWidth(); x++) {
                final Pixel p = pixelMapService.getPixelAt(pPixelMap, x, y);
                if (p.isNode(pPixelMap)) row.append("N");
                else if (p.isEdge(pPixelMap)) row.append("E");
                else row.append(" ");
            }
            map[y] = row.toString();
        }
        return map;
    }

    private static void setMap(final PixelMap pixelMap, final String[] map) {
        if (map.length != pixelMap.getHeight())
            throw new IllegalArgumentException("map.pixelLength != pixelMap.getHeight()");
        int y = 0;
        for (final String string : map) {
            if (string.length() != pixelMap.getWidth())
                throw new IllegalArgumentException("string.pixelLength() != pixelMap.getWidth() with:" + string + ", y=" + y);
            for (int x = 0; x < string.length(); x++) {
                final char c = string.charAt(x);
                switch (c) {
                    case ' ':
                        break;
                    case 'N':
                        pixelMap.setValue(x, y, (byte) (Pixel.NODE | Pixel.EDGE));
                        break;
                    case 'E':
                        pixelMap.setValue(x, y, Pixel.EDGE);
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected char:" + c + ", y=" + y);
                }
            }
            y++;
        }
    }

    /**
     * This checks that a and b are equal by concatenating a string with line breaks.
     *
     * @param expected
     * @param actual
     */
    public static void assertMapEquals(final String[] expected, final String[] actual) {
        if (expected == null) fail("a must not be null");
        if (actual == null) fail("b must not be null");
        if (expected.length != actual.length) fail("a and b must be the same size");
        if (expected.length == 0) return;
        final int len = expected[0].length();
        final StringBuffer expectedBuffer = new StringBuffer();
        final StringBuffer actualBuffer = new StringBuffer();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i].contains("\n")) fail("a contains newline in string " + i);
            if (actual[i].contains("\n")) fail("b contains newline in string " + i);
            expectedBuffer.append(expected[i]).append("\n");
            actualBuffer.append(actual[i]).append("\n");
        }
        assertEquals(expectedBuffer.toString().replace(" ", "."), actualBuffer.toString().replace(" ", "."));
    }
}
