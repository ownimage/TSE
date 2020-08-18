package com.ownimage.perception.pixelMap;

import com.ownimage.perception.pixelMap.immutable.IXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.PixelMap;
import com.ownimage.perception.pixelMap.services.Config;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.PixelMapApproximationService;
import com.ownimage.perception.pixelMap.services.PixelMapService;
import com.ownimage.perception.pixelMap.services.PixelService;
import com.ownimage.perception.transform.CannyEdgeTransform;
import org.jetbrains.annotations.NotNull;
import org.junit.ComparisonFailure;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Utility {

    private static ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private static PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private static PixelMapApproximationService pixelMapApproximationService = context.getBean(PixelMapApproximationService.class);
    private static PixelChainService pixelChainService = context.getBean(PixelChainService.class);
    private static PixelService pixelService = context.getBean(PixelService.class);

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

    public static ImmutablePixelMap createMap(final int pX, final int pY) {
        var pixelMap = ImmutablePixelMap.builder().width(pX).height(pY).is360(true).build();
        var transformSource = Utility.getDefaultTransformSource(pY);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        return pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
    }

    public static IPixelMapTransformSource getTransformSource(String[] map) {
        return getDefaultTransformSource(map.length);
    }

    public static ImmutablePixelMap createMap(final String[] map, boolean process) {
        return createMap(map, getDefaultTransformSource(map.length), process);
    }

    public static ImmutablePixelMap createMap(String[] map, IPixelMapTransformSource transformSource, boolean process) {
        ImmutablePixelMap pixelMap = ImmutablePixelMap.builder()
                .width(map[0].length()).height(map.length).is360(true).build();
        pixelMap = setMap(pixelMap, map);
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        return process
                ? pixelMapApproximationService.actionProcess(pixelMap, tolerance, lineCurvePreference, null)
                : pixelMap;
    }

    public static String[] toStrings(final ImmutablePixelMap pPixelMap) {
        final String[] map = new String[pPixelMap.height()];
        for (int y = 0; y < pPixelMap.height(); y++) {
            final StringBuffer row = new StringBuffer();
            for (int x = 0; x < pPixelMap.width(); x++) {
                final Pixel p = pixelMapService.getPixelAt(pPixelMap, x, y);
                if (pixelService.isNode(pPixelMap, p)) row.append("N");
                else if (pixelService.isEdge(pPixelMap, p)) row.append("E");
                else row.append(" ");
            }
            map[y] = row.toString();
        }
        return map;
    }

    private static ImmutablePixelMap setMap(final PixelMap pixelMap, final String[] map) {
        var result = ImmutablePixelMap.copyOf(pixelMap);
        if (map.length != pixelMap.height())
            throw new IllegalArgumentException("map.pixelLength != pixelMap.getHeight()");
        int y = 0;
        for (final String string : map) {
            if (string.length() != pixelMap.width())
                throw new IllegalArgumentException("string.pixelLength() != pixelMap.getWidth() with:" + string + ", y=" + y);
            for (int x = 0; x < string.length(); x++) {
                final char c = string.charAt(x);
                switch (c) {
                    case ' ':
                        break;
                    case 'N':
                        result = pixelMapService.setValue(result, x, y, (byte) (Pixel.NODE | Pixel.EDGE));
                        break;
                    case 'E':
                        result = pixelMapService.setValue(result, x, y, Pixel.EDGE);
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected char:" + c + ", y=" + y);
                }
            }
            y++;
        }
        return result;
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

    public static void assertSamePixels(@NotNull PixelMap pixelMap, @NotNull PixelChain pc1, @NotNull PixelChain pc2) {
        var pc1Readable = pc1.toReadableString();
        var pc2Readable = pc2.toReadableString();
        if (pc1Readable.equals(pc2Readable)) {
            return;
        }
        var pc2ReadbleReverse = pixelChainService.reverse(pixelMap, pc2).toReadableString();
        if (pc1Readable.equals(pc2ReadbleReverse)) {
            return;
        }
        throw new ComparisonFailure("PixelChains do not contain same pixels", pc1Readable, pc2Readable);
    }

    public static ImmutablePixelChain createPixelChain(@NotNull PixelMap pixelMap, @NotNull IXY start, @NotNull Pixel... pixels) {
        var startNode = com.ownimage.perception.pixelMap.immutable.ImmutableNode.of(start.getX(), start.getY());
        var pixelChain = pixelChainService.createStartingPixelChain(pixelMap, startNode);
        for (var pixel : pixels) {
            pixelChain = pixelChainService.add(pixelChain, pixel);
        }
        return pixelChain;
    }

}
