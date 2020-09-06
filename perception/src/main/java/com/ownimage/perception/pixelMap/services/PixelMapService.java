package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.math.Point;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.MyBase64;
import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.framework.util.immutable.ImmutableSet;
import com.ownimage.perception.pixelMap.immutable.ImmutableIXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelChain;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.Node;
import com.ownimage.perception.pixelMap.immutable.Pixel;
import com.ownimage.perception.pixelMap.immutable.PixelChain;
import com.ownimage.perception.pixelMap.immutable.Segment;
import com.ownimage.perception.pixelMap.immutable.XY;
import io.vavr.Tuple2;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ownimage.perception.pixelMap.PixelConstants.ALL;
import static com.ownimage.perception.pixelMap.PixelConstants.E;
import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.N;
import static com.ownimage.perception.pixelMap.PixelConstants.NE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static com.ownimage.perception.pixelMap.PixelConstants.NW;
import static com.ownimage.perception.pixelMap.PixelConstants.S;
import static com.ownimage.perception.pixelMap.PixelConstants.SE;
import static com.ownimage.perception.pixelMap.PixelConstants.SW;
import static com.ownimage.perception.pixelMap.PixelConstants.W;

@Service
public class PixelMapService {

    private final static Logger logger = Framework.getLogger();
    private static final int[][] eliminate = {{N, E, SW}, {E, S, NW}, {S, W, NE}, {W, N, SE}};
    private PixelMapChainGenerationService pixelMapChainGenerationService;
    private PixelMapApproximationService pixelMapApproximationService;
    private PixelMapUpgradeService pixelMapUpgradeService;
    private PixelChainService pixelChainService;
    private PixelService pixelService;

    @Autowired
    public void setPixelMapUpgradeService(PixelMapUpgradeService pixelMapUpgradeService) {
        this.pixelMapUpgradeService = pixelMapUpgradeService;
    }

    @Autowired
    public void setPixelMapChainGenerationService(PixelMapChainGenerationService pixelMapChainGenerationService) {
        this.pixelMapChainGenerationService = pixelMapChainGenerationService;
    }

    @Autowired
    public void setPixelMapApproximationService(PixelMapApproximationService pixelMapApproximationService) {
        this.pixelMapApproximationService = pixelMapApproximationService;
    }

    @Autowired
    public void setPixelChainService(PixelChainService pixelChainService) {
        this.pixelChainService = pixelChainService;
    }

    @Autowired
    public void setPixelService(PixelService pixelService) {
        this.pixelService = pixelService;
    }

    public boolean canRead(IPersistDB pDB, String pId) {
        String pixelString = pDB.read(pId + ".data");
        return pixelString != null && !pixelString.isEmpty();
    }

    public ImmutablePixelMap read(@NotNull IPersistDB db, @NotNull String id) {
        Framework.logEntry(logger);

        var width = Integer.parseInt(db.read(id + ".width"));
        var height = Integer.parseInt(db.read(id + ".height"));

        ImmutablePixelMap pixelMap = ImmutablePixelMap.builder().width(width).height(height).is360(false).build();
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
                var rawPixelChains = (Collection<PixelChain>) ois.readObject();
                // fix for the fact that many of the Vertexes will have a lazy evaluation of the position that needs
                // to be replaced with a constant value
                var pixelChains = rawPixelChains.stream()
                        .map(pc -> pixelMapUpgradeService.upgradePixelChain(pc, height))
                        .collect(Collectors.toList());
                pixelMap = pixelChainsClear(pixelMap);
                pixelMap = pixelChainsAddAll(pixelMap, pixelChains);
                pixelMap = pixelMapUpgradeService.ensureAllPixelChainsMappedToNodes(pixelMap);
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
        return pixelMap.withAutoTrackChanges(true);
    }

    // TODO this will dissappear when the concept of a Pixel dissappears and is replaced with an ImmutableIXY
    private ImmutableIXY getKey(XY pIntegerXY) {
        // this is because pIntegerXY might be a Node or Pixel
        return pIntegerXY.getClass() == ImmutableIXY.class ? (ImmutableIXY) pIntegerXY : ImmutableIXY.of(pIntegerXY.getX(), pIntegerXY.getY());
    }

    public @NotNull ImmutablePixelMap setNode(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull XY pixel,
            boolean pValue) {
        if (pixelService.isNode(pixelMap, pixel) && !pValue) {
            return nodeRemove(pixelMap, pixel);
        }
        if (!pixelService.isNode(pixelMap, pixel) && pValue) {
            return nodeAdd(pixelMap, pixel);
        }
        return pixelMap;
    }

    public @NotNull ImmutablePixelMap setData(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull XY pixel,
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
        return pixelMap;
    }

    /**
     * This adds the node to the PixelMap data and nodes.
     *
     * @param pixelMap
     * @param pixel
     * @return
     */
    public @NotNull ImmutablePixelMap nodeAdd(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull XY pixel) {
        var result = pixelMap;
        var x = pixel.getX();
        var y = pixel.getY();

        var oldValue = pixelMap.data().get(x, y);
        var newValue = (byte) (oldValue | NODE | EDGE);
        result = result.withData(result.data().set(x, y, newValue));

        var key = getKey(pixel);
        if (pixelMap.nodes().get(key) == null) {
            result = result.withNodes(result.nodes().put(key, Node.of(key)));
        }
        return result;
    }

    /**
     * This removes the node from the PixelMap data and nodes.
     *
     * @param pixelMap
     * @param pixel
     * @return
     */
    public @NotNull ImmutablePixelMap nodeRemove(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull XY pixel) {
        var x = pixel.getX();
        var y = pixel.getY();
        var oldValue = pixelMap.data().get(x, y);
        var newValue = (byte) (oldValue & (ALL ^ NODE));
        return pixelMap.withNodes(
                pixelMap.nodes().remove(getKey(pixel)))
                .withData(pixelMap.data().set(x, y, newValue));
    }

    public int countEdgeNeighboursTransitions(
            @NotNull ImmutablePixelMap pixelMap,
            @NonNull XY pixel) {
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

    public void checkCompatibleSize(@NonNull ImmutablePixelMap one, @NotNull ImmutablePixelMap other) {
        if (one.width() != other.width() || one.height() != other.height()) {
            throw new IllegalArgumentException("PixelMaps are of different sized.");
        }
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull ImmutablePixelMap pixelMapData, ImmutableIXY integerPoint) {
        return getOptionalPixelAt(pixelMapData, integerPoint.getX(), integerPoint.getY());
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull ImmutablePixelMap pixelMapData, int x, int y) {
        if (0 > y || y >= pixelMapData.height()) {
            return Optional.empty();
        }
        if (!pixelMapData.is360() && (0 > x || x >= pixelMapData.width())) {
            return Optional.empty();
        }
        int newX = modWidth(pixelMapData, x);
        return Optional.of(Pixel.of(newX, y, pixelMapData.height()));
    }

    public int modWidth(@NotNull ImmutablePixelMap pixelMapData, int pX) {
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

    public List<ImmutablePixelChain> getPixelChains(@NotNull ImmutablePixelMap pixelMapData, @NonNull XY pPixel) {
        Framework.logEntry(logger);
        var pixelChains = pixelMapData.pixelChains().stream()
                .filter(pc -> pixelChainService.contains(pc, pPixel))
                .collect(Collectors.toList());
        Framework.logExit(logger);
        return pixelChains;
    }

    public void write(ImmutablePixelMap pixelMap, IPersistDB db, String id) throws IOException {
        // note that write/read does not preserve the mAllNodes values
        Framework.logEntry(logger);

        db.write(id + ".width", String.valueOf(pixelMap.width()));
        db.write(id + ".height", String.valueOf(pixelMap.height()));

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
        var pixelChains = pixelMap.pixelChains().toCollection();
        oos.writeObject(pixelChains);
        oos.close();
        String objectString = MyBase64.compressAndEncode(baos.toByteArray());
        db.write(id + ".objects", objectString);
        objectString = null;
        Framework.logExit(logger);
    }


    public ImmutablePixelMap indexSegments(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull PixelChain pixelChain,
            boolean add) {
        var result = StrongReference.of(pixelMap);
        pixelChain.segments().forEach(s -> result.update(r -> indexSegments(r, pixelChain, s, add)));
        return result.get();
    }

    public ImmutablePixelMap indexSegments(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull PixelChain pixelChain,
            @NotNull Segment segment,
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
            }
        });

        var result = pixelMap
                .withSegmentCount(segmentCount)
                .withSegmentIndex(segmentIndex.get());

        return result;
    }

    public Point getUHVWHalfPixel(ImmutablePixelMap pixelMap) {
        return new Point(0.5d * aspectRatio(pixelMap) / pixelMap.width(), 0.5d / pixelMap.height());
    }

    public ImmutablePixelMap clearAllPixelChains(@NotNull ImmutablePixelMap pixelMap) {
        return pixelMap
                .withPixelChains(pixelMap.pixelChains().clear())
                .withSegmentIndex(pixelMap.segmentIndex().clear());
    }

    public ImmutablePixelMap pixelChainRemove(@NotNull ImmutablePixelMap pixelMap, @NotNull ImmutablePixelChain chain) {
        val result = StrongReference.of(pixelMap);
        result.update(r -> indexSegments(r, chain, false)
                .withPixelChains(r.pixelChains().remove(chain)));
        chain.pixels()
                .firstElement()
                .filter(pixel -> getPixelChains(result.get(), pixel).size() == 1)
                .map(pixelService::pixelToPixelMapGridPosition)
                .ifPresent(ip -> result.update(r -> r.withNodes(r.nodes().remove(ip))));
        chain.pixels()
                .lastElement()
                .filter(pixel -> getPixelChains(result.get(), pixel).size() == 1)
                .map(pixelService::pixelToPixelMapGridPosition)
                .ifPresent(ip -> result.update(r -> r.withNodes(r.nodes().remove(ip))));
        return result.get();
    }


    public Vector<ImmutablePixelChain> getPixelChainsSortedByLength(ImmutablePixelMap pixelMap) {
        var chains = new Vector<>(pixelMap.pixelChains().toCollection());
        chains.sort(Comparator.comparingInt(PixelChain::pixelCount));
        return chains;
    }

    public ImmutablePixelMap setEdge(
            @NotNull ImmutablePixelMap pixelMapData,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference) {
        var result = StrongReference.of(pixelMapData);
        pixelChain.pixels().stream()
                .filter(p -> p != pixelChain.pixels().firstElement().orElseThrow())
                .filter(p -> p != pixelChain.pixels().lastElement().orElseThrow())
                .forEach(p -> result.update(r -> setEdge(r, p, false, tolerance, lineCurvePreference)));
        pixelChain.pixels().stream()
                .filter(p -> pixelService.isNode(result.get(), p))
                .filter(p -> pixelService.countEdgeNeighbours(result.get(), p) < 2
                        || pixelService.countNodeNeighbours(result.get(), p) == 2)
                .forEach(p -> result.update(r -> setEdge(r, p, false, tolerance, lineCurvePreference)));
        return result.get();
    }

    public Optional<Node> getNode(ImmutablePixelMap pixelMap, XY pIntegerXY) {
        // this is because pIntegerXY might be a Node or Pixel
        ImmutableIXY key = getKey(pIntegerXY);
        Node node = pixelMap.nodes().get(key);
        return Optional.ofNullable(node);
    }

    public Optional<Pixel> getPixelOptionalAt(@NotNull ImmutablePixelMap pixelMapData, int x, int y) {
        if (0 > x || x >= pixelMapData.width() || 0 > y || y >= pixelMapData.height()) {
            return Optional.empty();
        }
        return Optional.of(Pixel.of(x, y, pixelMapData.height()));
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull ImmutablePixelMap pixelMapData, @NotNull Point point) {
        return getOptionalPixelAt(pixelMapData, point.getX(), point.getY());
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull ImmutablePixelMap pixelMapData, double x, double y) {
        return Optional.ofNullable(getPixelAt(pixelMapData, x, y));
    }

    public Pixel getPixelAt(@NotNull ImmutablePixelMap pixelMapData, double xIn, double yIn) {
        int h = pixelMapData.height();
        int x = (int) (xIn * pixelMapData.width());
        int y = (int) (yIn * h);
        y = y == h ? h - 1 : y;
        x = modWidth(pixelMapData, x);
        return Pixel.of(x, y, pixelMapData.height());
    }

    public Pixel getPixelAt(@NotNull ImmutablePixelMap pixelMapData, int xIn, int yIn) {
        return getOptionalPixelAt(pixelMapData, xIn, yIn).orElse(null);
    }

    public Point toUHVW(@NotNull ImmutablePixelMap pixelMap, @NotNull Point point) {
        return point.scaleX(aspectRatio(pixelMap));
    }

    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     *
     * @param pixelMap
     * @return
     */
    public double aspectRatio(@NotNull ImmutablePixelMap pixelMap) {
        return (double) pixelMap.width() / pixelMap.height();
    }

    /**
     * This method calculates whether the XY should be a node and sets the PixelMap (data and nodes) accordingly
     *
     * @param pixelMap
     * @param point
     * @return
     */
    public ImmutablePixelMap calcIsNode(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull XY point) {
        boolean shouldBeNode = false;
        if (pixelService.isEdge(pixelMap, point)) {
            // here we use transitions to eliminate double counting connected neighbours
            // also note the the number of transitions is twice the number of neighbours
            int transitionCount = countEdgeNeighboursTransitions(pixelMap, point);
            if (transitionCount != 4) {
                shouldBeNode = true;
            }
        }
        return setNode(pixelMap, point, shouldBeNode);
    }

    public Stream<ImmutablePixelChain> generateChainsAndApproximate(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Node pNode, double tolerance, double lineCurvePreference) {
        var result = pixelMapChainGenerationService.generateChains(pixelMap, pNode);
        var stream = result.parallelStream()
                .map(pc -> pixelChainService.approximate(pixelMap, pc, tolerance))
                .map(pc -> pixelChainService.approximateCurvesOnly(pixelMap, pc, tolerance, lineCurvePreference));
        return stream;
    }

    public ImmutablePixelMap resetNode(@NotNull ImmutablePixelMap pixelMap) {
        var data = pixelMap.data().forEach(v -> (byte) (v & (ALL ^ NODE)));
        return pixelMap.withData(data);
    }

    public boolean getData(@NotNull ImmutablePixelMap pixelMap, @NotNull Pixel pixel, byte pValue) {
        if (0 <= pixel.getY() && pixel.getY() < pixelMap.height()) {
            int x = modWidth(pixelMap, pixel.getX());
            return (getValue(pixelMap, x, pixel.getY()) & pValue) != 0;
        } else {
            return false;
        }
    }

    public byte getValue(@NotNull ImmutablePixelMap pixelMap, int pX, int pY) {
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

    public ImmutablePixelMap setValue(@NotNull ImmutablePixelMap pixelMap, int pX, int pY, byte pValue) {
        return pixelMap.withData(pixelMap.data().set(pX, pY, pValue));
    }

    /**
     * The removes a pixelChain from the PixelMap.  It also removes it from the Nodes that it was attached to.
     * This is different from deletePixelChain which can cause the nodes that it was attached to to be merged.
     *
     * @param pixelChain
     */
    public ImmutablePixelMap removePixelChain(@NotNull ImmutablePixelMap pixelMap, @NotNull ImmutablePixelChain pixelChain) {
        var result = StrongReference.of(pixelChainRemove(pixelMap, pixelChain));
        pixelChainService.getStartNode(result.get(), pixelChain)
                .ifPresent(n -> result.update(r -> replaceNode(r, n.removePixelChain(pixelChain))));
        pixelChainService.getEndNode(result.get(), pixelChain)
                .ifPresent(n -> result.update(r -> replaceNode(r, n.removePixelChain(pixelChain))));
        return result.get();
    }

    public ImmutablePixelMap addPixelChain(@NotNull ImmutablePixelMap pixelMap, @NotNull ImmutablePixelChain pixelChain) {
        var result = pixelChainAdd(pixelMap, pixelChain);
        result = replaceNode(result, pixelChainService.getStartNode(result, pixelChain).orElseGet(() -> Node.of(pixelChain.pixels().firstElement().get())).addPixelChain(pixelChain));
        result = replaceNode(result, pixelChainService.getEndNode(result, pixelChain).orElseGet(() -> Node.of(pixelChain.pixels().lastElement().get())).addPixelChain(pixelChain));
        return result;
    }

    public ImmutablePixelMap replaceNode(@NotNull ImmutablePixelMap pixelMap, @NotNull Node node) {
        return pixelMap.withNodes(pixelMap.nodes().put(node.toImmutableIXY(), node));
    }

    public ImmutablePixelMap pixelChainsAddAll(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Collection<ImmutablePixelChain> pixelChains) {
        var result = StrongReference.of(pixelMap);
        pixelChains.forEach(pc -> result.update(r -> pixelChainAdd(r, pc)));
        return result.get();
    }

    public ImmutablePixelMap pixelChainAdd(@NotNull ImmutablePixelMap pixelMap, @NotNull ImmutablePixelChain chain) {
        var withStartPositions = pixelChainService.setStartPositions(pixelMap, chain);
        var result = pixelMap.withPixelChains(pixelMap.pixelChains().add(withStartPositions));
        return indexSegments(result, withStartPositions, true);
    }

    public ImmutablePixelMap pixelChainsClear(@NotNull ImmutablePixelMap pixelMap) {
        return pixelMap
                .withPixelChains(pixelMap.pixelChains().clear())
                .withSegmentIndex(pixelMap.segmentIndex().clear());
    }

    public ImmutablePixelMap addPixelChains(@NotNull ImmutablePixelMap pixelMap, @NotNull Collection<ImmutablePixelChain> pixelChains) {
        var result = StrongReference.of(pixelMap);
        pixelChains.forEach(pixelChain -> result.update(r -> addPixelChain(r, pixelChain)));
        return result.get();
    }

    public ImmutablePixelMap mergePixelChains(@NotNull ImmutablePixelMap pixelMap, @NotNull Node node) {
        var result = StrongReference.of(pixelMap);
        int count = node.countPixelChains();
        logger.info(() -> String.format("Node::mergePixelChains Node=%s, count=%s", this, count));
        switch (count) {
            case 2:
                var chain0 = node.getPixelChain(0);
                var chain1 = node.getPixelChain(1);
                if (chain0 != chain1) {// this is to prevent trying to merge a simple loop with itself
                    var merged = pixelChainService.merge(result.get(), chain0, chain1, node);
                    result.update(r -> removePixelChain(r, chain0));
                    result.update(r -> removePixelChain(r, chain1));
                    result.update(r -> addPixelChain(r, merged));
                }
                break;
            case 3:
                break;
            case 4:
                break;
        }
        return result.get();
    }

    public void forEachPixel(@NotNull ImmutablePixelMap pixelMap, @NotNull Consumer<Pixel> pFunction) {
        new Range2D(pixelMap.width(), pixelMap.height()).forEach((x, y) -> pFunction.accept(getPixelAt(pixelMap, x, y)));
    }

    /**
     * @deprecated TODO: explain
     */ // Move to a stream
    @Deprecated
    public void forEachPixelChain(@NotNull ImmutablePixelMap pixelMap, @NotNull Consumer<ImmutablePixelChain> pFunction) {
        pixelMap.pixelChains().forEach(pFunction);
    }

    public Stream<ImmutablePixelChain> streamPixelChains(@NotNull ImmutablePixelMap pixelMap) {
        return pixelMap.pixelChains().stream();
    }

    public ImmutablePixelMap nodesRemoveAll(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Collection<ImmutableIXY> pToBeRemoved) {
        var nodes = StrongReference.of(pixelMap.nodes());
        pToBeRemoved.forEach(p -> nodes.update(r -> r.remove(p)));
        return pixelMap.withNodes(nodes.get());
    }

    public void validate(@NotNull ImmutablePixelMap pixelMap) {
        Set segments = new HashSet<Segment>();
        for (int x = 0; x < pixelMap.width(); x++) {
            for (int y = 0; y < pixelMap.height(); y++) {
                pixelMap.segmentIndex().getOptional(x, y)
                        .stream()
                        .flatMap(ImmutableSet::stream)
                        .forEach(t -> segments.add(t._2));
            }
        }
    }

    public ImmutablePixelMap clearSegmentIndex(@NotNull ImmutablePixelMap pixelMap) {
        return pixelMap
                .withSegmentIndex(pixelMap.segmentIndex().clear())
                .withSegmentCount(0);
    }

    public @NotNull ImmutablePixelMap setEdge(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull XY pixel,
            boolean isEdge,
            double tolerance,
            double lineCurvePreference) {
        val result = StrongReference.of(pixelMap);
        if (pixelService.isEdge(pixelMap, pixel) == isEdge) {
            return result.get();
        }
        if (pixelService.isNode(pixelMap, pixel) && !isEdge) {
            result.update(r -> setNode(r, pixel, false));
        }
        result.update(r -> setData(r, pixel, isEdge, EDGE));
        result.update(r -> calcIsNode(r, pixel));
        pixelService.getNeighbours(pixel)
                .forEach(p -> {
                    result.update(r -> pixelMapApproximationService.thin(r, p, tolerance, lineCurvePreference));
                    result.update(r -> calcIsNode(r, p));
                });
        result.update(r -> pixelMapApproximationService.thin(r, pixel, tolerance, lineCurvePreference));
        if (result.get().autoTrackChanges()) {
            if (isEdge) { // turning pixel on
                result.update(r -> pixelMapApproximationService.trackPixelOn(r, pixel, tolerance, lineCurvePreference));
            } else { // turning pixel of
                result.update(r -> pixelMapApproximationService.trackPixelOff(r, pixel, tolerance, lineCurvePreference));
            }
        }
        return result.get();
    }

    public Stream<ImmutableIXY> stream8Neighbours(@NotNull ImmutablePixelMap pixelMapData, @NotNull XY center) {
        return new Range2D(-1, 2, -1, 2).stream()
                .map(ip -> center.add(ip.getX(), ip.getY()))
                .filter(ip -> !ip.equals(center))
                .filter(ip -> isInBounds(pixelMapData, ip));
    }

    public boolean isInBounds(@NotNull ImmutablePixelMap pixelMapData, @NotNull ImmutableIXY point) {
        return point.getX() >= 0 && point.getY() >= 0
                && point.getX() < pixelMapData.width() && point.getY() < pixelMapData.height();
    }

}
