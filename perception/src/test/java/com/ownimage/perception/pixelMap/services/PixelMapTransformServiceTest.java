package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.Utility;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.XY;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.util.logging.LogManager;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class PixelMapTransformServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapTransformService underTest = context.getBean(PixelMapTransformService.class);
    private PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private PixelChainService pixelChainService = context.getBean(PixelChainService.class);
    private PixelMapApproximationService pixelMapApproximationService = context.getBean(PixelMapApproximationService.class);

    @BeforeAll
    public static void turnLoggingOff() {
        LogManager.getLogManager().reset();
    }

    public static Stream<TestData> transformGetLineColor() {
        var black = new Color(0, 0, 0, 0);
        var blue = new Color(0, 0, 255, 255);
        var green = new Color(0, 255, 0, 255);
        return Stream.of(
                new TestData(ImmutableIXY.of(5, 1), black, blue, green, green, "hits line"),
                new TestData(ImmutableIXY.of(5, 1), black, blue, null, blue, "hits line, no chain color set"),
                new TestData(ImmutableIXY.of(5, 8), black, blue, green, black, "misses line")
        );

    }

    @ParameterizedTest
    @MethodSource("transformGetLineColor")
    public void transformGetLineColor(@NotNull PixelMapTransformServiceTest.TestData data) {
        // GIVEN
        String[] input = {
                "          ",
                "  NEEEEN  ",
                "          ",
                "          ",
                "          ",
                "          ",
                "          ",
                "          ",
                "          ",
                "          ",
        };
        var pixelMap = StrongReference.of(Utility.createMap(input, true));
        assertEquals(1, pixelMap.get().pixelChains().size());
        var height = pixelMap.get().height();
        var transformSource = Utility.getDefaultTransformSource(height);
        var originalPixelChain = pixelMap.get().pixelChains().stream().findFirst().orElseThrow();
        var newPixelChain = StrongReference.of(
                originalPixelChain.withThickness(IPixelChain.Thickness.Thick));
        if (data.chainColor != null) {
            newPixelChain.update(pc -> pc.withColor(data.chainColor));
        }
        pixelMap.update(pm -> pixelMapService.removePixelChain(pm, originalPixelChain));
        pixelMap.update(pm -> pixelMapService.addPixelChain(pm, newPixelChain.get()));
        // WHEN
        var actual = underTest.transformGetLineColor(pixelMap.get(), transformSource,
                data.testPoint.getUHVWMidPoint(height), data.color, data.defaultLineColor,
                1.0d, 4.0d, false);
        // THEN
        assertEquals(data.expected, actual);
    }

    public static class TestData {
        @Getter
        private XY testPoint;
        @Getter
        private Color color;
        @Getter
        private Color defaultLineColor;
        @Getter
        private Color chainColor;
        @Getter
        private Color expected;
        @Getter
        private String description;

        public TestData(XY testPoint, Color color, Color defaultLineColor, Color chainColor, Color expected, String description) {
            this.testPoint = testPoint;
            this.color = color;
            this.defaultLineColor = defaultLineColor;
            this.chainColor = chainColor;
            this.expected = expected;
            this.description = description;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("TestData{");
            sb.append("testPoint=").append(testPoint);
            sb.append(", color=").append(color);
            sb.append(", defaultLineColor=").append(defaultLineColor);
            sb.append(", chainColor=").append(chainColor);
            sb.append(", expected=").append(expected);
            sb.append(", description='").append(description).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

}