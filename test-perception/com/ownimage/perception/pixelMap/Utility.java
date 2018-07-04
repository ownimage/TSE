package com.ownimage.perception.pixelMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Utility {

    public static String[] getMap(final PixelMap pixelMap) {
        String[] map = new String[pixelMap.getHeight()];
        for (int y = 0; y < pixelMap.getHeight(); y++) {
            StringBuffer row = new StringBuffer();
            for (int x = 0; x < pixelMap.getWidth(); x++) {
                Pixel p = pixelMap.getPixelAt(x, y);
                if (p.isNode()) row.append("N");
                else if (p.isEdge()) row.append("E");
                else row.append(" ");
            }
            map[y] = row.toString();
        }
        return map;
    }

    public static void setMap(final PixelMap pixelMap, final String[] map) {
        if (map.length != pixelMap.getHeight())
            throw new IllegalArgumentException("map.length != pixelMap.getHeight()");
        int y = 0;
        for (String string : map) {
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
    public static void assertMapEquals(String[] expected, String[] actual) {
        if (expected == null) fail("a must not be null");
        if (actual == null) fail("b must not be null");
        if (expected.length != actual.length) fail("a and b must be the same size");
        if (expected.length == 0) return;
        int len = expected[0].length();
        StringBuffer expectedBuffer = new StringBuffer();
        StringBuffer actualBuffer = new StringBuffer();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i].contains("\n")) fail("a contains newline in string " + i);
            if (actual[i].contains("\n")) fail("b contains newline in string " + i);
            expectedBuffer.append(expected[i]).append("\n");
            actualBuffer.append(actual[i]).append("\n");
        }
        assertEquals(expectedBuffer.toString().replace(" ", "."), actualBuffer.toString().replace(" ", "."));
    }
}
