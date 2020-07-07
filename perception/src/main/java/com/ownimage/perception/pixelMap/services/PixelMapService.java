package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.MyBase64;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.EqualizeValues;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.transform.CannyEdgeTransform;
import io.vavr.Tuple2;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelMapService {

    private final static Logger logger = Framework.getLogger();

    private static PixelMapMappingService pixelMapMappingService = Services.getDefaultServices().getPixelMapMappingService();
    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();
    private static PixelService pixelService = Services.getDefaultServices().getPixelService();

    public boolean canRead(IPersistDB pDB, String pId) {
        String pixelString = pDB.read(pId + ".data");
        return pixelString != null && !pixelString.isEmpty();
    }

    public ImmutablePixelMapData read(IPersistDB db, String id, IPixelMapTransformSource transformSource) {
        Framework.logEntry(logger);

        var width = Integer.parseInt(db.read(id + ".width"));
        var height = Integer.parseInt(db.read(id + ".height"));

        var pixelMap = new PixelMap(width, height, false, transformSource);
        var data = new ImmutableMap2D<>(width, height, (byte) 0);

        try {
            // pixel data
            {
                String pixelString = db.read(id + ".data");
                byte[] pixelBytes = MyBase64.decodeAndDecompress(pixelString);
                ByteArrayInputStream bais = new ByteArrayInputStream(pixelBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                for (int x = 0; x < width; x++) {
                    byte[] buff = new byte[height];
                    int cnt = 0;
                    while ((cnt += ois.read(buff, cnt, height - cnt)) < height) {
                    }
                    for (int y = 0; y < height; y++) {
                        data = data.set(x, y, buff[y]);
                    }
                }
                bais = null;
                ois = null;
                pixelString = null;
                pixelBytes = null;
                int cnt = 0;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (data.get(x, y) != 0) {
                            cnt++;
                        }
                    }
                }
                pixelMap = pixelMap.withData(data);
                logger.info("mData cnt = " + cnt);
            }
            // mPixelChains
            {
                String objectString = db.read(id + ".objects");
                byte[] objectBytes = MyBase64.decodeAndDecompress(objectString);
                ByteArrayInputStream bais = new ByteArrayInputStream(objectBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                Collection<PixelChain> pixelChains = (Collection<PixelChain>) ois.readObject();
                // fix for the fact that many of the Vertexes will have a lazy evaluation of the position that needs
                // to be replaced with a constant value
                Function<PixelChain, PixelChain> fixNullPositionVertexes =
                        pc -> pixelChainService.fixNullPositionVertexes(height, pc);
                pixelChains = pixelChains.stream().map(fixNullPositionVertexes).collect(Collectors.toList());
                pixelMap.pixelChainsClear();
                pixelMap.pixelChainsAddAll(pixelChains);
                //TODO this will need to change
                bais = null;
                ois = null;
                objectString = null;
                objectBytes = null;
                logger.info("mAllNodes size() = " + pixelMap.nodeCount());
                logger.info("mPixelChains size() = " + pixelMap.getPixelChains().size());
                logger.info("mSegmentCount = " + pixelMap.getSegmentCount());
            }
        } catch (Exception pEx) {
            logger.log(Level.SEVERE, "PixelMap.read()", pEx);
        }

        logger.info("node count = " + pixelMap.nodeCount());
        logger.info("pixelChain count = " + pixelMap.getPixelChains().size());
        logger.info("segment count = " + pixelMap.getSegmentCount());

        Framework.logExit(logger);
        return pixelMapMappingService.toImmutablePixelMapData(pixelMap);
    }

    public void checkCompatibleSize(@NonNull PixelMapData one, @NotNull PixelMapData other) {
        if (one.width() != other.width() || one.height() != other.height()) {
            throw new IllegalArgumentException("PixelMaps are of different sized.");
        }
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull PixelMapData pixelMapData, IntegerPoint integerPoint) {
        return getOptionalPixelAt(pixelMapData, integerPoint.getX(), integerPoint.getY());
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull PixelMapData pixelMapData, int x, int y) {
        if (0 > y || y >= pixelMapData.height()) {
            return Optional.empty();
        }
        if (!pixelMapData.is360() && (0 > x || x >= pixelMapData.width())) {
            return Optional.empty();
        }
        int newX = modWidth(pixelMapData, x);
        return Optional.of(new Pixel(newX, y));
    }

    private int modWidth(@NotNull PixelMapData pixelMapData, int pX) {
        int width = pixelMapData.width();
        if (0 <= pX && pX < width) {
            return pX;
        }
        if (pixelMapData.is360()) {
            if (pX < 0) {
                return modWidth(pixelMapData, pX + width);
            }
            return modWidth(pixelMapData, pX - width);
        } else {
            if (pX < 0) {
                return 0;
            }
            return width - 1;
        }
    }

    public List<PixelChain> getPixelChains(@NotNull PixelMapData pixelMapData, @NonNull Pixel pPixel) {
        Framework.logEntry(logger);
        List<PixelChain> pixelChains = pixelMapData.pixelChains().stream()
                .filter(pc -> pixelChainService.contains(pc, pPixel))
                .collect(Collectors.toList());
        Framework.logExit(logger);
        return pixelChains;
    }

    public void write(PixelMapData pixelMap, IPersistDB db, String id) throws IOException {
        // note that write/read does not preserve the mAllNodes values
        Framework.logEntry(logger);

        db.write(id + ".width", String.valueOf(pixelMap.width()));
        db.write(id + ".heioght", String.valueOf(pixelMap.height()));

        // from http://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;
        // mData
        {
            logger.finest("About to write mData");
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            for (int x = 0; x < pixelMap.width(); x++) {
                byte[] buff = new byte[pixelMap.height()];
                for (int y = 0; y < pixelMap.height(); y++) {
                    buff[y] = pixelMap.data().get(x, y);
                }
                oos.write(buff);
            }
            oos.close();
            String pixelString = MyBase64.compressAndEncode(baos.toByteArray());
            db.write(id + ".data", pixelString);
            pixelString = null;
        }

        logger.info("node count = " + pixelMap.nodes().size());
        logger.info("pixelChain count = " + pixelMap.pixelChains().size());
        logger.info("segment count = " + pixelMap.segmentCount());

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        Collection<PixelChain> pixelChains = pixelMap.pixelChains().toCollection();
        oos.writeObject(pixelChains);
        oos.close();
        String objectString = MyBase64.compressAndEncode(baos.toByteArray());
        db.write(id + ".objects", objectString);
        objectString = null;
        Framework.logExit(logger);
    }

    public ImmutablePixelMapData actionPixelOn(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource source,
            @NotNull Collection<Pixel> pixels) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, source).actionPixelOn(pixels);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionPixelOff(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel,
            int cursorSize) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null).actionPixelOff(pixel, cursorSize);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionDeletePixelChain(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<Pixel> pixels) {
        var pm = pixelMapMappingService.toPixelMap(pixelMap, null);
        var clone = StrongReference.of(pixelMap.withAutoTrackChanges(false));
        pixels.stream()
                .filter(p -> pixelService.isEdge(clone.get(), p))
                .forEach(p -> getPixelChains(clone.get(), p)
                        .forEach(pc -> {
                            // TODO in the implementation of the method below make the parameter immutable
                            clone.set(clearInChainAndVisitedThenSetEdge(clone.get(), pc));
                            pixelChainService.getStartNode(clone.get(), pc)
                                    .ifPresent(n -> clone.update(c -> c.withNodes(clone.get().nodes().remove(n))));
                            pixelChainService.getEndNode(clone.get(), pc)
                                    .ifPresent(n -> clone.update(c -> c.withNodes(clone.get().nodes().remove(n))));
                            clone.update(c -> c.withPixelChains(c.pixelChains().remove(pc)));
                            clone.update(c -> indexSegments(c, pc, false));
//                            pixelChainService.indexSegments(pm, pc, false);
                            clone.update(c -> indexSegments(c, pc, false));
                            int x = 0;
                        }));
        return clone.get().withAutoTrackChanges(true);
    }

    public ImmutablePixelMapData indexSegments(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            boolean add) {
        var result = StrongReference.of(pixelMap);
        pixelChain.getSegments().forEach(s -> result.update(r -> indexSegments(r, pixelChain, s, add)));
        return result.get();
    }

    public ImmutablePixelMapData indexSegments(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull PixelChain pixelChain,
            @NotNull ISegment segment,
            boolean add) {
        var segmentIndex = StrongReference.of(pixelMap.segmentIndex());
        var segmentCount = pixelMap.segmentCount() + 1;
        var width = pixelMap.width();
        var height = pixelMap.height();
        var aspectRatio = aspectRatio(pixelMap);

        int minX = (int) Math.floor(segment.getMinX(pixelMap, pixelChain) * width / aspectRatio) - 1;
        minX = Math.max(minX, 0);
        minX = Math.min(minX, width - 1);
        int maxX = (int) Math.ceil(segment.getMaxX(pixelMap, pixelChain) * width / aspectRatio) + 1;
        maxX = Math.min(maxX, width - 1);
        int minY = (int) Math.floor(segment.getMinY(pixelMap, pixelChain) * height) - 1;
        minY = Math.max(minY, 0);
        minY = Math.min(minY, height - 1);
        int maxY = (int) Math.ceil(segment.getMaxY(pixelMap, pixelChain) * height) + 1;
        maxY = Math.min(maxY, height - 1);

        new Range2D(minX, maxX, minY, maxY).stream().forEach(i -> {
            Pixel pixel = getPixelOptionalAt(pixelMap, i.getX(), i.getY()).orElseThrow();
            Point centre = pixel.getUHVWMidPoint(height);
            if (segment.closerThan(pixelMap, pixelChain, centre, getUHVWHalfPixel(pixelMap).length())) {
                var segments = StrongReference.of(
                        pixelMap.segmentIndex().getOptional(i.getX(), i.getY()).orElseGet(ImmutableSet::new));
                if (add) {
                    segments.update(s -> s.add(new Tuple2<>(pixelChain, segment)));
                } else {
                    segments.update(s -> s.remove(new Tuple2<>(pixelChain, segment)));
                }
                segmentIndex.update(si -> si.set(i.getX(), i.getY(), segments.get()));
                int x = 0;
            }
        });

        var result = pixelMap
                .withSegmentCount(segmentCount)
                .withSegmentIndex(segmentIndex.get());

        return result;
    }

    public Point getUHVWHalfPixel(PixelMapData pixelMap) {
        return new Point(0.5d * aspectRatio(pixelMap) / pixelMap.width(), 0.5d / pixelMap.height());
    }

    public ImmutablePixelMapData actionSetPixelChainThickness(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<Pixel> pixels,
            @NotNull Function<PixelChain, IPixelChain.Thickness> mapper) {
        var result = StrongReference.of(pixelMap);
        pixels.stream()
                .filter(p -> pixelService.isEdge(pixelMap, p))
                .flatMap(p -> getPixelChains(pixelMap, p).stream())
                .distinct()
                .forEach(pc -> {
                    var currentThickness = pc.getThickness();
                    var newThickness = mapper.apply(pc);
                    if (newThickness != currentThickness) {
                        result.update(r -> pixelChainsRemove(r, pc));
                        result.update(r -> pixelChainAdd(r, pixelChainService.withThickness(pc, newThickness)));
                    }
                });
        return result.get();
    }

    public ImmutablePixelMapData actionPixelToggle(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null).actionPixelToggle(pixel);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionPixelChainDeleteAllButThis(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel) {
        val pixelChains = getPixelChains(pixelMap, pixel);
        if (pixelChains.size() != 1) {
            return pixelMap;
        }

        var result = clearAllPixelChains(pixelMap);
        result = pixelChainsAddAll(result, pixelChains);
        return result;
    }


    public ImmutablePixelMapData actionPixelChainApproximateCurvesOnly(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel,
            @NotNull IPixelMapTransformSource source) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, source).actionPixelChainApproximateCurvesOnly(pixel);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionReapproximate(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource source) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, source).actionReapproximate();
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionRerefine(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource source) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, source).actionRerefine();
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionSetPixelChainDefaultThickness(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull CannyEdgeTransform transform) {
        int shortLength = transform.getShortLineLength();
        int mediumLength = transform.getMediumLineLength();
        int longLength = transform.getLongLineLength();
        Vector<PixelChain> updates = new Vector<>();
        pixelMap.pixelChains().forEach(chain -> updates.add(pixelChainService.withThickness(chain, shortLength, mediumLength, longLength)));
        var result = clearAllPixelChains(pixelMap);
        result = pixelChainsAddAll(result, updates);
        return result;
    }

    public ImmutablePixelMapData clearAllPixelChains(@NotNull ImmutablePixelMapData pixelMap) {
        return pixelMap
                .withPixelChains(pixelMap.pixelChains().clear())
                .withSegmentIndex(pixelMap.segmentIndex().clear());
    }

    public ImmutablePixelMapData pixelChainsAddAll(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<PixelChain> pixelChains) {
        var result = StrongReference.of(pixelMap);
        pixelChains.forEach(pc -> result.update(r -> pixelChainAdd(r, pc)));
        return result.get();
    }


    public ImmutablePixelMapData pixelChainAdd(@NotNull ImmutablePixelMapData pixelMap, @NotNull PixelChain chain) {
        return indexSegments(pixelMap, chain, true)
                .withPixelChains(pixelMap.pixelChains().add(chain));
    }

    public ImmutablePixelMapData pixelChainsRemove(@NotNull ImmutablePixelMapData pixelMap, @NotNull PixelChain chain) {
        return indexSegments(pixelMap, chain, false)
                .withPixelChains(pixelMap.pixelChains().remove(chain));
    }


    public ImmutablePixelMapData actionEqualizeValues(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull EqualizeValues values) {
        if (pixelMap.pixelChains().size() == 0) {
            return pixelMap;
        }
        // TODO do not like this mutable parameter
        var totalLength = new StrongReference<>(0);
        pixelMap.pixelChains().forEach(chain -> totalLength.update(len -> len + chain.getPixelCount()));
        Vector<PixelChain> sortedChains = getPixelChainsSortedByLength(pixelMap);
        int shortThreshold = (int) (totalLength.get() * values.getIgnoreFraction());
        int mediumThreshold = (int) (totalLength.get() * (values.getIgnoreFraction() + values.getShortFraction()));
        int longThreshold = (int) (totalLength.get() * (values.getIgnoreFraction() + values.getShortFraction() +
                values.getMediumFraction()));
        Integer shortLength = null;
        Integer mediumLength = null;
        Integer longLength = null;
        int currentLength = 0;
        for (PixelChain chain : sortedChains) {
            currentLength += chain.getPixelCount();
            if (shortLength == null && currentLength > shortThreshold) {
                shortLength = chain.getPixelCount();
            }
            if (mediumLength == null && currentLength > mediumThreshold) {
                mediumLength = chain.getPixelCount();
            }
            if (longLength == null && currentLength > longThreshold) {
                longLength = chain.getPixelCount();
                break;
            }
        }
        values.setReturnValues(shortLength, mediumLength, longLength);
        return pixelMap;
    }

    public Vector<PixelChain> getPixelChainsSortedByLength(ImmutablePixelMapData pixelMap) {
        var chains = new Vector<>(pixelMap.pixelChains().toCollection());
        chains.sort(Comparator.comparingInt(IPixelChain::getPixelCount));
        return chains;
    }

    // TODO need to make this immutable
    public ImmutablePixelMapData clearInChainAndVisitedThenSetEdge(ImmutablePixelMapData pixelMapData, PixelChain pixelChain) {
        var pixelMap = pixelMapMappingService.toPixelMap(pixelMapData, null);
        pixelChain.getPixels().forEach(p -> p.setInChain(pixelMap, false));
        pixelChain.getPixels().forEach(p -> p.setVisited(pixelMap, false));
        pixelChain.getPixels().stream()
                .filter(p -> p != pixelChain.getPixels().firstElement().orElseThrow())
                .filter(p -> p != pixelChain.getPixels().lastElement().orElseThrow())
                .forEach(p -> p.setEdge(pixelMap, false));
        pixelChain.getPixels().stream()
                .filter(pPixel -> pPixel.isNode(pixelMap))
                .filter(p -> p.countEdgeNeighbours(pixelMap) < 2 || p.countNodeNeighbours(pixelMap) == 2)
                .forEach(p -> p.setEdge(pixelMap, false));
        return pixelMapMappingService.toImmutablePixelMapData(pixelMap);
    }

    public Optional<Node> getNode(PixelMapData pixelMap, IntegerPoint pIntegerPoint) {
        // this is because pIntegerPoint might be a Node or Pixel
        IntegerPoint point = getTrueIntegerPoint(pIntegerPoint);
        Node node = pixelMap.nodes().get(point);
        if (node != null) {
            return Optional.of(node);
        }
        if (pixelService.isNode(pixelMap, point)) {
            // TODO it is not believed that this is needed as Nodes shuould be immutable 2020/07/04
//            node = new Node(point);
//            mNodes.put(point, node);
            logger.severe("Found a node that is not in the node map");
            return Optional.of(node);
        }
        return Optional.empty();
    }

    private IntegerPoint getTrueIntegerPoint(IntegerPoint pIntegerPoint) {
        // this is because pIntegerPoint might be a Node or Pixel
        return pIntegerPoint.getClass() == IntegerPoint.class
                ? pIntegerPoint
                : new IntegerPoint(pIntegerPoint.getX(), pIntegerPoint.getY());
    }

    public Optional<Pixel> getPixelOptionalAt(@NotNull PixelMapData pixelMapData, int x, int y) {
        if (0 > x || x >= pixelMapData.width() || 0 > y || y >= pixelMapData.height()) {
            return Optional.empty();
        }
        return Optional.of(new Pixel(x, y));
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull PixelMapData pixelMapData, @NotNull Point point) {
        return getOptionalPixelAt(pixelMapData, point.getX(), point.getY());
    }

    private Optional<Pixel> getOptionalPixelAt(@NotNull PixelMapData pixelMapData, double x, double y) {
        return Optional.ofNullable(getPixelAt(pixelMapData, x, y));
    }

    private Pixel getPixelAt(@NotNull PixelMapData pixelMapData, double xIn, double yIn) {
        int h = pixelMapData.height();
        int x = (int) (xIn * pixelMapData.width());
        int y = (int) (yIn * h);
        y = y == h ? h - 1 : y;
        x = modWidth(pixelMapData, x);
        return new Pixel(x, y);
    }

    public Point toUHVW(@NotNull PixelMapData pixelMap, @NotNull Point point) {
        return point.scaleX(aspectRatio(pixelMap));
    }

    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     *
     * @param pixelMap
     * @return
     */
    public double aspectRatio(@NotNull PixelMapData pixelMap) {
        return (double) pixelMap.width() / pixelMap.height();
    }
}
