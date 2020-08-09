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

    @Test
    public void refineCornersProblem() {
        int xMargin = 2;
        int yMargin = 2;
        Pixel offset = new Pixel(xMargin, yMargin);
        IPixelMapTransformSource ts = new PixelMapTransformSource(1000, 1.2, 1.2);
        PixelMap pixelMap = new PixelMap(64 + 2 * xMargin, 74 + 2 * yMargin, false, ts);
        pixelMap.actionProcess(null);
        pixelMap = pixelMap.actionPixelOn(new Pixel(55, 73).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(54, 73).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(53, 73).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(52, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(51, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(50, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(49, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(48, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(47, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(46, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(45, 72).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(44, 71).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(43, 71).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(42, 71).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(41, 71).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(40, 71).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(39, 71).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(38, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(37, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(36, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(35, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(34, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(33, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(32, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(31, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(30, 70).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(29, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(28, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(27, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(26, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(25, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(24, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(23, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(22, 69).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(21, 68).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(20, 68).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(19, 68).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(18, 68).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(17, 68).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(16, 67).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(15, 67).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(14, 67).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(13, 67).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(12, 67).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(11, 66).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(10, 66).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(9, 66).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(8, 66).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(7, 66).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(6, 66).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(5, 65).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(4, 65).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(3, 64).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(2, 63).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 62).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 61).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 60).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 59).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 58).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 57).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 56).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 55).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 54).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 53).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 52).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 51).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 50).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 49).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 48).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 47).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 46).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 45).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 44).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 43).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 42).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 41).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 40).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 39).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 38).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 37).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 36).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 35).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 34).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 33).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 32).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 31).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 30).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 29).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 28).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 27).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 26).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 25).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 24).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 23).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 22).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 21).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 20).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 19).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 18).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 17).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 16).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 15).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 14).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 13).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 12).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 11).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 10).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 9).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 8).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 5).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(0, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 3).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(1, 2).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(2, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(3, 0).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(4, 0).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(5, 0).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(6, 0).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(7, 0).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(8, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(9, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(10, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(11, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(12, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(13, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(14, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(15, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(16, 1).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(17, 2).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(18, 2).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(19, 2).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(20, 2).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(21, 2).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(22, 2).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(23, 3).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(24, 3).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(25, 3).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(26, 3).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(27, 3).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(28, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(29, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(30, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(31, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(32, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(33, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(34, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(35, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(36, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(37, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(38, 4).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(39, 5).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(40, 5).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(41, 5).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(42, 5).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(43, 5).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(44, 5).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(45, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(46, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(47, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(48, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(49, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(50, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(51, 6).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(52, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(53, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(54, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(55, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(56, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(57, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(58, 7).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(59, 8).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(60, 8).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(61, 9).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(61, 10).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(62, 11).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(62, 12).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 13).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 14).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 15).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 16).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 17).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 18).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 19).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 20).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 21).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 22).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 23).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 24).add(offset));
        pixelMap = pixelMap.actionPixelOn(new Pixel(63, 25).add(offset));


        assertEquals(1, pixelMap.getPixelChainCount());
    }
}