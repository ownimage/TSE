package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.persist.PersistDB;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.logging.LogManager;

import static org.junit.Assert.assertEquals;

public class PixelMapServiceTest {

    private ApplicationContext context = new AnnotationConfigApplicationContext(Config.class);
    private PixelMapService underTest = context.getBean(PixelMapService.class);

    @BeforeClass
    public static void turnLoggingOff() throws Exception {
        LogManager.getLogManager().reset();
    }

    @Test
    public void read() throws IOException {
        // GIVEN
        var is = getClass().getResourceAsStream("NY2.transform");
        var db = new PersistDB(is);
        // WHEN
        var actual = underTest.read(db, "transform.2.cannyEdge");
        // THEN
        assertEquals(1074, actual.width());
        assertEquals(1520, actual.height());
        assertEquals(3742, actual.pixelChains().size());
        assertEquals(88768, actual.data().size());
    }
}
