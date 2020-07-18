package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.MyBase64;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.Immutable2DArray;
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
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ownimage.perception.pixelMap.PixelConstants.ALL;
import static com.ownimage.perception.pixelMap.PixelConstants.E;
import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.IN_CHAIN;
import static com.ownimage.perception.pixelMap.PixelConstants.N;
import static com.ownimage.perception.pixelMap.PixelConstants.NE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static com.ownimage.perception.pixelMap.PixelConstants.NW;
import static com.ownimage.perception.pixelMap.PixelConstants.S;
import static com.ownimage.perception.pixelMap.PixelConstants.SE;
import static com.ownimage.perception.pixelMap.PixelConstants.SW;
import static com.ownimage.perception.pixelMap.PixelConstants.VISITED;
import static com.ownimage.perception.pixelMap.PixelConstants.W;

public class PixelMapService {

    private final static Logger logger = Framework.getLogger();

    private static PixelMapChainGenerationService pixelMapChainGenerationService = Services.getDefaultServices().pixelMapChainGenerationService();
    private static PixelMapApproximationService pixelMapApproximationService = Services.getDefaultServices().getPixelMapApproximationService();
    private static PixelMapMappingService pixelMapMappingService = Services.getDefaultServices().getPixelMapMappingService();
    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();
    private static PixelService pixelService = Services.getDefaultServices().getPixelService();

    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};


    public boolean canRead(IPersistDB pDB, String pId) {
        String pixelString = pDB.read(pId + ".data");
        return pixelString != null && !pixelString.isEmpty();
    }

    public ImmutablePixelMapData read(IPersistDB db, String id, IPixelMapTransformSource transformSource) {
        Framework.logEntry(logger);

        var width = Integer.parseInt(db.read(id + ".width"));
        var height = Integer.parseInt(db.read(id + ".height"));

        PixelMapData pixelMap = new PixelMap(width, height, false, transformSource);
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
                pixelMap = pixelChainsClear(pixelMap);
                pixelMap = pixelChainsAddAll(pixelMap, pixelChains);
                //TODO this will need to change
                bais = null;
                ois = null;
                objectString = null;
                objectBytes = null;
                logger.info("mAllNodes size() = " + pixelMap.nodes().size());
                logger.info("mPixelChains size() = " + pixelMap.pixelChains().size());
                logger.info("mSegmentCount = " + pixelMap.segmentCount());
            }
        } catch (Exception pEx) {
            logger.log(Level.SEVERE, "PixelMap.read()", pEx);
        }

        logger.info("node count = " + pixelMap.nodes().size());
        logger.info("pixelChain count = " + pixelMap.pixelChains().size());
        logger.info("segment count = " + pixelMap.segmentCount());

        Framework.logExit(logger);
        return pixelMapMappingService.toImmutablePixelMapData(pixelMap).withAutoTrackChanges(true);
    }

    // TODO this will dissappear when the concept of a Pixel dissappears and is replaced with an IntegerPoint
    private IntegerPoint getTrueIntegerPoint(IntegerPoint pIntegerPoint) {
        // this is because pIntegerPoint might be a Node or Pixel
        return pIntegerPoint.getClass() == IntegerPoint.class ? pIntegerPoint : new IntegerPoint(pIntegerPoint.getX(), pIntegerPoint.getY());
    }

    public @NotNull ImmutablePixelMapData setVisited(
            @NotNull PixelMapData pixelMap,
            @NotNull Pixel pixel,
            @NotNull boolean isVisited) {
        var oldValue = pixelMap.data().get(pixel.getX(), pixel.getY());
        var newValue = (byte) (isVisited ? oldValue | VISITED : oldValue & (ALL ^ VISITED));
        return pixelMap.withData(pixelMap.data().set(pixel.getX(), pixel.getY(), newValue));
    }

    public @NotNull ImmutablePixelMapData setInChain(
            @NotNull PixelMapData pixelMap,
            @NotNull Pixel pixel,
            @NotNull boolean isInChain) {
        var oldValue = pixelMap.data().get(pixel.getX(), pixel.getY());
        var newValue = (byte) (isInChain ? oldValue | IN_CHAIN : oldValue & (ALL ^ IN_CHAIN));
        return pixelMap.withData(pixelMap.data().set(pixel.getX(), pixel.getY(), newValue));
    }

    public @NotNull ImmutablePixelMapData setNode(
            @NotNull PixelMapData pixelMap,
            @NonNull Pixel pixel,
            boolean pValue) {
        var result = pixelMap;
        if (pixelService.isNode(pixelMap, pixel) && !pValue) {
            result = nodeRemove(result, pixel);
        }
        if (!pixelService.isNode(pixelMap, pixel) && pValue) {
            result = nodeAdd(result, pixel);
        }
        return setData(result, pixel, pValue, NODE);
    }

    public @NotNull ImmutablePixelMapData setData(
            @NotNull PixelMapData pixelMap,
            @NotNull Pixel pixel,
            boolean pState,
            byte pValue) {
        if (0 <= pixel.getY() && pixel.getY() < pixelMap.height()) {
            int x = modWidth(pixelMap, pixel.getX());
            var oldValue = pixelMap.data().get(x, pixel.getY());
            var newValue = (byte) (oldValue & (ALL ^ pValue));
            if (pState) {
                newValue |= pValue;
            }
            return pixelMap.withData(pixelMap.data().set(x, pixel.getY(), newValue));
        }
        return pixelMapMappingService.toImmutablePixelMapData(pixelMap);
    }

    public @NotNull ImmutablePixelMapData nodeAdd(
            @NotNull PixelMapData pixelMap,
            @NonNull Pixel pixel) {
        var x = pixel.getX();
        var y = pixel.getY();
        var oldValue = pixelMap.data().get(x, y);
        var newValue = (byte) (oldValue | NODE);
        return pixelMap.withNodes(
                pixelMap.nodes().put(getTrueIntegerPoint(pixel), new Node(pixel)))
                .withData(pixelMap.data().set(x, y, newValue));
    }

    public @NotNull ImmutablePixelMapData nodeRemove(
            @NotNull PixelMapData pixelMap,
            @NonNull Pixel pixel) {
        var x = pixel.getX();
        var y = pixel.getY();
        var oldValue = pixelMap.data().get(x, y);
        var newValue = (byte) (oldValue & (ALL ^ NODE));
        return pixelMap.withNodes(
                pixelMap.nodes().remove(getTrueIntegerPoint(pixel)))
                .withData(pixelMap.data().set(x, y, newValue));
    }

    public int countEdgeNeighboursTransitions(
            @NotNull PixelMapData pixelMap,
            @NonNull Pixel pixel) {
        int[] loop = new int[]{NW, N, NE, E, SE, S, SW, W, NW};

        int count = 0;
        boolean currentState = pixelService.isEdge(pixelMap, pixelService.getNeighbour(pixel, NW));

        for (int neighbour : loop) {
            var newState = pixelService.isEdge(pixelMap, pixelService.getNeighbour(pixel, neighbour));
            if (currentState != newState) {
                currentState = newState;
                count++;
            }
        }
        return count;
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

    public int modWidth(@NotNull PixelMapData pixelMapData, int pX) {
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
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Collection<Pixel> pixels) {
//        var mutable = pixelMapMappingService.toPixelMap(pixelMap, transformSource).actionPixelOn(pixels);
//        return pixelMapMappingService.toImmutablePixelMapData(mutable);


//        PixelMap clone = new PixelMap(this);
//        clone.mAutoTrackChanges = false;
//        pPixels.forEach(pixel -> pixel.setEdge(clone, true));
//        clone.mAutoTrackChanges = true;
//        clone.trackPixelOn(pPixels);
//        return clone;


        var result = StrongReference.of(pixelMap);
        result.update(r -> r.withAutoTrackChanges(false));
        pixels.forEach(pixel ->
                result.update(r -> pixelMapApproximationService.setEdge(r, transformSource, pixel, true)));
        result.update(r -> r.withAutoTrackChanges(true));
        result.update(r -> pixelMapApproximationService.trackPixelOn(r, transformSource, pixels));
        return result.get();
    }

    public ImmutablePixelMapData actionPixelOff(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel,
            int cursorSize) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, transformSource).actionPixelOff(pixel, cursorSize);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionDeletePixelChain(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Collection<Pixel> pixels) {
        var clone = StrongReference.of(pixelMap.withAutoTrackChanges(false));
        pixels.stream()
                .filter(p -> pixelService.isEdge(clone.get(), p))
                .forEach(p -> getPixelChains(clone.get(), p)
                        .forEach(pc -> {
                            // TODO in the implementation of the method below make the parameter immutable
                            clone.update(c -> clearInChainAndVisitedThenSetEdge(c, transformSource, pc));
                            pixelChainService.getStartNode(clone.get(), pc)
                                    .ifPresent(n -> clone.update(c -> nodeRemove(c, n)));
                            pixelChainService.getEndNode(clone.get(), pc)
                                    .ifPresent(n -> clone.update(c -> nodeRemove(c, n)));
                            clone.update(c -> c.withPixelChains(c.pixelChains().remove(pc)));
                            clone.update(c -> indexSegments(c, pc, false));
                            clone.update(c -> indexSegments(c, pc, false));
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
                        result.update(r -> pixelChainRemove(r, pc));
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
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel) {
        if (getPixelChains(pixelMap, pixel).isEmpty()) {
            return pixelMap;
        }
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        var clone = StrongReference.of(pixelMap);
        getPixelChains(clone.get(), pixel).forEach(pc -> {
            clone.update(c -> pixelChainRemove(c, pc));
            val pc2 = pixelChainService.approximateCurvesOnly(clone.get(), pc, tolerance, lineCurvePreference);
            clone.update(c -> pixelChainAdd(c, pc2));
        });
        //copy.indexSegments();
        return clone.get();
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

    public ImmutablePixelMapData pixelChainRemove(@NotNull PixelMapData pixelMap, @NotNull PixelChain chain) {
        val result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        result.update(r -> indexSegments(r, chain, false)
                .withPixelChains(r.pixelChains().remove(chain)));
        chain.getPixels()
                .firstElement()
                .filter(pixel -> getPixelChains(result.get(), pixel).size() == 1)
                .map(pixelService::pixelToIntegerPoint)
                .ifPresent(ip -> result.update(r -> r.withNodes(r.nodes().remove(ip))));
        chain.getPixels()
                .lastElement()
                .filter(pixel -> getPixelChains(result.get(), pixel).size() == 1)
                .map(pixelService::pixelToIntegerPoint)
                .ifPresent(ip -> result.update(r -> r.withNodes(r.nodes().remove(ip))));
        return result.get();
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

    public ImmutablePixelMapData clearInChainAndVisitedThenSetEdge(
            @NotNull PixelMapData pixelMapData,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull PixelChain pixelChain) {
        var result = StrongReference.of(pixelMapData);
        pixelChain.getPixels().forEach(p -> {
            result.update(r -> setInChain(r, p, false));
            result.update(r -> setVisited(r, p, false));
        });
        pixelChain.getPixels().stream()
                .filter(p -> p != pixelChain.getPixels().firstElement().orElseThrow())
                .filter(p -> p != pixelChain.getPixels().lastElement().orElseThrow())
                .forEach(p -> result.update(r -> setEdge(r, transformSource, p, false)));
        pixelChain.getPixels().stream()
                .filter(p -> pixelService.isNode(result.get(), p))
                .filter(p -> p.countEdgeNeighbours(result.get()) < 2 || p.countNodeNeighbours(result.get()) == 2)
                .forEach(p -> result.update(r -> setEdge(r, transformSource, p, false)));
        return pixelMapMappingService.toImmutablePixelMapData(result.get());
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
            node = new Node(point);
//            mNodes = mNodes.put(point, node);
            logger.severe("Found a node that is not in the node map");
            return Optional.of(node);
        }
        return Optional.empty();
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

    public Optional<Pixel> getOptionalPixelAt(@NotNull PixelMapData pixelMapData, double x, double y) {
        return Optional.ofNullable(getPixelAt(pixelMapData, x, y));
    }

    public Pixel getPixelAt(@NotNull PixelMapData pixelMapData, double xIn, double yIn) {
        int h = pixelMapData.height();
        int x = (int) (xIn * pixelMapData.width());
        int y = (int) (yIn * h);
        y = y == h ? h - 1 : y;
        x = modWidth(pixelMapData, x);
        return new Pixel(x, y);
    }

    public Pixel getPixelAt(@NotNull PixelMapData pixelMapData, int xIn, int yIn) {
        return getOptionalPixelAt(pixelMapData, xIn, yIn).orElse(null);
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

    public Tuple2<ImmutablePixelMapData, Boolean> calcIsNode(
            @NotNull PixelMapData pixelMap,
            @NotNull Pixel pixel) {
        boolean shouldBeNode = false;
        var pixelMapResult = pixelMap;
        if (pixelService.isEdge(pixelMap, pixel.toIntegerPoint())) {
            // here we use transitions to eliminate double counting connected neighbours
            // also note the the number of transitions is twice the number of neighbours
            int transitionCount = countEdgeNeighboursTransitions(pixelMap, pixel);
            if (transitionCount != 4) {
                shouldBeNode = true;
                pixelMapResult = setNode(pixelMap, pixel, true);
            }
        }
        return new Tuple2<>(setNode(pixelMapResult, pixel, shouldBeNode), shouldBeNode);
    }

    public Tuple2<ImmutablePixelMapData, Stream<PixelChain>> generateChainsAndApproximate(
            @NotNull PixelMapData pixelMap,
            @NotNull IPixelMapTransformSource transformSource,
            @NotNull Node pNode) {
        double tolerance = transformSource.getLineTolerance() / transformSource.getHeight();
        double lineCurvePreference = transformSource.getLineCurvePreference();
        var result = pixelMapChainGenerationService.generateChains(pixelMap, pNode);
        var stream = result._2.parallelStream()
                .map(pc -> pixelChainService.approximate(pixelMap, pc, tolerance))
                .map(pc -> pixelChainService.approximateCurvesOnly(pixelMap, pc, tolerance, lineCurvePreference));
        return new Tuple2<>(result._1, stream);
    }

    public ImmutablePixelMapData resetVisited(@NotNull PixelMapData pixelMap) {
        var data = pixelMap.data().forEach(v -> (byte) (v & (ALL ^ VISITED)));
        return pixelMap.withData(data);
    }

    public ImmutablePixelMapData resetNode(@NotNull PixelMapData pixelMap) {
        var data = pixelMap.data().forEach(v -> (byte) (v & (ALL ^ NODE)));
        return pixelMap.withData(data);
    }

    public ImmutablePixelMapData resetInChain(@NotNull PixelMapData pixelMap) {
        var data = pixelMap.data().forEach(v -> (byte) (v & (ALL ^ IN_CHAIN)));
        return pixelMap.withData(data);
    }

    public boolean getData(@NotNull PixelMapData pixelMap, @NotNull Pixel pixel, byte pValue) {
        if (0 <= pixel.getY() && pixel.getY() < pixelMap.height()) {
            int x = modWidth(pixelMap, pixel.getX());
            return (getValue(pixelMap, x, pixel.getY()) & pValue) != 0;
        } else {
            return false;
        }
    }

    public byte getValue(@NotNull PixelMapData pixelMap, int pX, int pY) {
        // TODO change these to Framework checks
        if (pX < 0) {
            throw new IllegalArgumentException("pX must be > 0.");
        }
        var width = pixelMap.width();
        if (pX > width - 1) {
            throw new IllegalArgumentException("pX must be less than width() -1. pX = " + pX + ", getWidth() = " + width);
        }
        if (pY < 0) {
            throw new IllegalArgumentException("pY must be > 0.");
        }
        var height = pixelMap.height();
        if (pY > height - 1) {
            throw new IllegalArgumentException("pX must be less than height() -1. pX = " + pX + ", getHeight() = " + height);
        }
        return pixelMap.data().get(pX, pY);
    }

    public ImmutablePixelMapData setValue(@NotNull PixelMapData pixelMap, int pX, int pY, byte pValue) {
        return pixelMap.withData(pixelMap.data().set(pX, pY, pValue));
    }

    /**
     * The removes a pixelChain from the PixelMap.  It also removes it from the Nodes that it was attached to.
     * This is different from deletePixelChain which can cause the nodes that it was attached to to be merged.
     *
     * @param pixelChain
     */
    public ImmutablePixelMapData removePixelChain(@NotNull PixelMapData pixelMap, @NotNull PixelChain pixelChain) {
        var result = StrongReference.of(pixelChainRemove(pixelMap,  pixelChain));
        pixelChainService.getStartNode(result.get(), pixelChain)
                .ifPresent(n -> result.update(r -> replaceNode(r, n.removePixelChain(pixelChain))));
        pixelChainService.getEndNode(result.get(), pixelChain)
                .ifPresent(n -> result.update(r -> replaceNode(r, n.removePixelChain(pixelChain))));
        return result.get();
    }

    public ImmutablePixelMapData addPixelChain(@NotNull PixelMapData pixelMap, @NotNull PixelChain pixelChain) {
        var result = pixelChainsAdd(pixelMap, pixelChain);
        result = replaceNode(result, pixelChainService.getStartNode(result, pixelChain).get().addPixelChain(pixelChain));
        result = replaceNode(result, pixelChainService.getEndNode(result, pixelChain).get().addPixelChain(pixelChain));
        return result;
    }

    public ImmutablePixelMapData replaceNode(@NotNull PixelMapData pixelMap, @NotNull Node node) {
        return pixelMap.withNodes(pixelMap.nodes().put(node.toIntegerPoint(), node));
    }

    public ImmutablePixelMapData pixelChainsAddAll(
            @NotNull PixelMapData pixelMap, @NotNull Collection<PixelChain> pixelChains) {
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        pixelChains.forEach(chain -> result.update(r -> pixelChainsAdd(r, chain)));
        return result.get();
    }

    public ImmutablePixelMapData pixelChainsAdd(@NotNull PixelMapData pixelMap, @NotNull PixelChain pChain) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null);
        var chain = pixelChainService.indexSegments(mutable, pChain, true);
        return mutable.withPixelChains(mutable.pixelChains().add(chain));
    }

    public ImmutablePixelMapData pixelChainsClear(@NotNull PixelMapData pixelMap) {
        return pixelMap
                .withPixelChains(pixelMap.pixelChains().clear())
                .withSegmentIndex(pixelMap.segmentIndex().clear());
    }

    public ImmutablePixelMapData addPixelChains(@NotNull PixelMapData pixelMap, @NotNull Collection<PixelChain> pixelChains) {
        var result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        pixelChains.forEach(pixelChain -> result.update(r-> addPixelChain(r, pixelChain)));
        return result.get();
    }


    /**
     * @deprecated TODO: explain
     */ // move to a stream
    @Deprecated
    public void forEachPixel(@NotNull PixelMapData pixelMap, @NotNull Consumer<Pixel> pFunction) {
        new Range2D(pixelMap.width(), pixelMap.height()).forEach((x, y) -> pFunction.accept(getPixelAt(pixelMap, x, y)));
    }

    /**
     * @deprecated TODO: explain
     */ // Move to a stream
    @Deprecated
    public void forEachPixelChain(@NotNull PixelMapData pixelMap, @NotNull Consumer<PixelChain> pFunction) {
        pixelMap.pixelChains().forEach(pFunction);
    }

    public Stream<PixelChain> streamPixelChains(@NotNull PixelMapData pixelMap) {
        return pixelMap.pixelChains().stream();
    }

    /**
     * Thin checks whether a Pixel should be removed in order to make the absolute single Pixel wide lines that are needed. If the
     * Pixel should not be an edge this method 1) does a setEdge(false) on the Pixel, and 2) returns true. Otherwise it returns
     * false.
     *
     * @param pixel the pixel
     * @return true, if the Pixel was thinned.
     */
    public Tuple2<ImmutablePixelMapData, Boolean> thin(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel) {
        if (!pixelService.isEdge(pixelMap, pixel)) {
            return new Tuple2<>(pixelMapMappingService.toImmutablePixelMapData(pixelMap), false);
        }
        var pixelMapResult = pixelMapMappingService.toImmutablePixelMapData(pixelMap);
        boolean canEliminate = false;
        for (int[] set : eliminate) {
            canEliminate |= pixelService.isEdge(pixelMap,pixel.getNeighbour(set[0]))
                    && pixelService.isEdge(pixelMap, pixel.getNeighbour(set[1]))
                    && !pixelService.isEdge(pixelMap, pixel.getNeighbour(set[2]));
        }
        if (canEliminate) {
            pixelMapResult = pixelMapApproximationService.setEdge(pixelMapResult, transformSource, pixel, false);
            pixelMapResult = nodeRemove(pixelMapResult, pixel);
        }
        return new Tuple2<>(pixelMapResult, canEliminate);
    }


    public ImmutablePixelMapData nodesRemoveAll(
            @NotNull PixelMapData pixelMap, @NotNull Collection<Pixel> pToBeRemoved) {
        var nodes = StrongReference.of(pixelMap.nodes());
        pToBeRemoved.forEach(p -> nodes.update(r -> r.remove(p.toIntegerPoint())));
        return pixelMap.withNodes(nodes.get());
    }

    public void validate(@NotNull PixelMapData pixelMap) {
//        mPixelChains.stream().parallel().forEach(pc -> pc.validate(pPixelMap, true, "PixelMap::validate"));
        Set segments = new HashSet<ISegment>();
        for (int x = 0; x < pixelMap.width(); x++) {
            for (int y = 0; y < pixelMap.height(); y++) {
                var list = pixelMap.segmentIndex().get(x, y);
                if (list != null) {
                    list.stream().forEach(t -> segments.add(t._2));
                }
            }
        }
//        if (mSegmentCount != segments.size()) {
//            String message = String.format("mSegmentCount mismatch: mSegmentCount=%s, segments.size()=%s", mSegmentCount, segments.size());
//            throw new IllegalStateException(message);
//        }
    }


    public ImmutablePixelMapData clearSegmentIndex(@NotNull PixelMapData pixelMap) {
        return pixelMap
                .withSegmentIndex(new Immutable2DArray<>(pixelMap.width(), pixelMap.height(), 20))
                .withSegmentCount(0);
    }


    public @NotNull ImmutablePixelMapData setEdge(
            @NotNull PixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NotNull Pixel pixel,
            @NotNull boolean isEdge) {
        val result = StrongReference.of(pixelMapMappingService.toImmutablePixelMapData(pixelMap));
        if (pixelService.isEdge(pixelMap, pixel) == isEdge) {
            return result.get();
        }
        if (pixelService.isNode(pixelMap, pixel) && !isEdge) {
            result.update(r -> setNode(r, pixel, false));
        }
        result.update(r -> setData(r, pixel, isEdge, EDGE));
        result.update(r -> calcIsNode(r, pixel)._1);
        pixel.getNeighbours().forEach(p -> {
            result.update(r -> thin(r, transformSource, p)._1);
            result.update(r -> calcIsNode(r, p)._1);
        });
        result.update(r -> thin(r, transformSource, pixel)._1);
        if (result.get().autoTrackChanges()) {
            var mutable = pixelMapMappingService.toPixelMap(result.get(), transformSource);
            if (isEdge) { // turning pixel on
                mutable.setValuesFrom(pixelMapApproximationService.trackPixelOn(mutable, transformSource, pixel));
            } else { // turning pixel off
                mutable.setValuesFrom(pixelMapApproximationService.trackPixelOff(mutable, transformSource, pixel));
            }
            result.set(pixelMapMappingService.toImmutablePixelMapData(mutable));
        }
        return result.get();
    }

}
