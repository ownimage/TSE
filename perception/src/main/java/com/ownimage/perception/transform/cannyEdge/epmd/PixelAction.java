package com.ownimage.perception.transform.cannyEdge.epmd;

import com.ownimage.framework.view.event.IUIEvent;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.XY;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.BiFunction;

@Value.Immutable
public interface PixelAction extends Serializable {

    BiFunction<IUIEvent, Pixel, Boolean> defaultClickEvent = (event, xy) -> false;
    Runnable defaultControlVisibility = () -> {
    };
    BiFunction<IUIEvent, XY, Boolean> defaultDragEvent = (event, xy) -> false;
    BiFunction<IUIEvent, Collection<XY>, Boolean> defaultDragEndEvent = (event, xy) -> false;
    ImmutablePixelAction defaultPixelAction = ImmutablePixelAction.of("NoName", true,
            defaultClickEvent, defaultControlVisibility, defaultDragEvent, defaultDragEndEvent);

    static ImmutablePixelAction of(String name, boolean sizable, BiFunction<IUIEvent, Pixel, Boolean> clickEvent) {
        return defaultPixelAction.withName(name).withSizable(sizable).withClickEvent(clickEvent);
    }

    @Value.Parameter(order = 1)
    String name();

    @Value.Parameter(order = 2)
    boolean sizable();

    @Value.Parameter(order = 3)
    BiFunction<IUIEvent, Pixel, Boolean> clickEvent();

    @Value.Parameter(order = 4)
    Runnable controlVisibility();

    @Value.Parameter(order = 5)
    BiFunction<IUIEvent, XY, Boolean> dragEvent();

    @Value.Parameter(order = 6)
    BiFunction<IUIEvent, Collection<XY>, Boolean> dragEndEvent();
}
