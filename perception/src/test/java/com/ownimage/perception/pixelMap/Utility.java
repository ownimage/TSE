package com.ownimage.perception.pixelMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Utility {

    public static PixelMap createMap(final String[] map) {
        return createMap(map, null);
    }

    public static PixelMap createMap(final String[] map, final IPixelMapTransformSource transformSource) {
        final PixelMap pixelMap = new PixelMap(map[0].length(), map.length, true, transformSource);
        setMap(pixelMap, map);
        return pixelMap;
    }

    public static String[] getMap(final PixelMap pixelMap) {
        final String[] map = new String[pixelMap.getHeight()];
        for (int y = 0; y < pixelMap.getHeight(); y++) {
            final StringBuffer row = new StringBuffer();
            for (int x = 0; x < pixelMap.getWidth(); x++) {
                final Pixel p = pixelMap.getPixelAt(x, y);
                if (p.isNode()) row.append("N");
                else if (p.isEdge()) row.append("E");
                else row.append(" ");
            }
            map[y] = row.toString();
        }
        return map;
    }

    private static void setMap(final PixelMap pixelMap, final String[] map) {
        if (map.length != pixelMap.getHeight())
            throw new IllegalArgumentException("map.length != pixelMap.getHeight()");
        int y = 0;
        for (final String string : map) {
            if (string.length() != pixelMap.getWidth())
                throw new IllegalArgumentException("string.length() != pixelMap.getWidth() with:" + string + ", y=" + y);
            for (int x = 0; x < string.length(); x++) {
                final char c = string.charAt(x);
                switch (c) {
                    case ' ':
                        break;
                    case 'N':
                        pixelMap.setValueNoUndo(x, y, (byte) (Pixel.NODE | Pixel.EDGE));
                        break;
                    case 'E':
                        pixelMap.setValueNoUndo(x, y, Pixel.EDGE);
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
