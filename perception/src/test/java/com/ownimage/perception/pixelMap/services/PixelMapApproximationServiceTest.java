package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.persist.PersistDB;
import com.ownimage.framework.view.javafx.FXViewFactory;
import com.ownimage.perception.pixelMap.Utility;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PixelMapApproximationServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapService pixelMapService = context.getBean(PixelMapService.class);
    private PixelMapApproximationService underTest = context.getBean(PixelMapApproximationService.class);
    private PixelMapValidationService pixelMapValidationService = context.getBean(PixelMapValidationService.class);

    @BeforeClass
    public static void setViewFactory() throws Exception {
        FXViewFactory.clearViewFactory();
        FXViewFactory.setAsViewFactory();
    }

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Test
    public void actionProcess() throws IOException {
        // GIVEN
        var is = getClass().getResourceAsStream("NY2.transform");
        var db = new PersistDB(is);
        var pixelMap = pixelMapService.read(db, "transform.2.cannyEdge");
        var tolerance = 1.2d / 1520;
        var lineCurvePreference = 1.2d;
        // WHEN
        var actual = underTest.actionProcess(pixelMap, tolerance, lineCurvePreference, null);
        // THEN
        assertEquals(1074, actual.width());
        assertEquals(1520, actual.height());
        assertTrue(3100 <  actual.pixelChains().size());
        assertTrue(87000 < actual.data().size());
        pixelMapValidationService.validate(actual);
    }

    @Test
    public void checkAllDataEdgesHave2Neighbours() {
        // GIVEN pixel map with valid useage of 3 neighbours
        String[] input = {
                "  E     ",
                " N NEE  ",
                "   E  E ",
                "   E    ",
                "   E    ",
                "  E     ",
        };
        String[] expected = {
                "        ",
                "    EE  ",
                "   E  N ",
                "   E    ",
                "   E    ",
                "  N     ",
        };        // WHEN
        var actual = Utility.createMap(input, true);
        // THEN
        Utility.assertMapEquals(expected, Utility.toStrings(actual));
        pixelMapValidationService.validate(actual);
    }
}