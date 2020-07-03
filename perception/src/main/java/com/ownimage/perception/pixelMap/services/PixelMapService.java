package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.control.control.ProgressControl;
import com.ownimage.framework.math.IntegerPoint;
import com.ownimage.framework.math.Point;
import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.MyBase64;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.perception.pixelMap.EqualizeValues;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMapData;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.transform.CannyEdgeTransform;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelMapService {

    private final static Logger mLogger = Framework.getLogger();

    private static PixelMapMappingService pixelMapMappingService = Services.getDefaultServices().getPixelMapMappingService();
    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();

    public boolean canRead(IPersistDB pDB, String pId) {
        String pixelString = pDB.read(pId + ".data");
        return pixelString != null && !pixelString.isEmpty();
    }

    public ImmutablePixelMapData read(IPersistDB db, String id, IPixelMapTransformSource transformSource) {
        Framework.logEntry(mLogger);

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
                mLogger.info("mData cnt = " + cnt);
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
                mLogger.info("mAllNodes size() = " + pixelMap.nodeCount());
                mLogger.info("mPixelChains size() = " + pixelMap.getPixelChains().size());
                mLogger.info("mSegmentCount = " + pixelMap.getSegmentCount());
            }
        } catch (Exception pEx) {
            mLogger.log(Level.SEVERE, "PixelMap.read()", pEx);
        }

        mLogger.info("node count = " + pixelMap.nodeCount());
        mLogger.info("pixelChain count = " + pixelMap.getPixelChains().size());
        mLogger.info("segment count = " + pixelMap.getSegmentCount());

        Framework.logExit(mLogger);
        return pixelMapMappingService.toImmutablePixelMapData(pixelMap);
    }

    public void checkCompatibleSize(@NonNull com.ownimage.perception.pixelMap.immutable.PixelMapData one, @NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData other) {
        if (one.width() != other.width() || one.height() != other.height()) {
            throw new IllegalArgumentException("PixelMaps are of different sized.");
        }
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, IntegerPoint integerPoint) {
        return getOptionalPixelAt(pixelMapData, integerPoint.getX(), integerPoint.getY());
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, int x, int y) {
        if (0 > y || y >= pixelMapData.height()) {
            return Optional.empty();
        }
        if (!pixelMapData.is360() && (0 > x || x >= pixelMapData.width())) {
            return Optional.empty();
        }
        int newX = modWidth(pixelMapData, x);
        return Optional.of(new Pixel(newX, y));
    }

    private int modWidth(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, int pX) {
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

    public List<PixelChain> getPixelChains(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, @NonNull Pixel pPixel) {
        Framework.logEntry(mLogger);
        List<PixelChain> pixelChains = pixelMapData.pixelChains().stream()
                .filter(pc -> pixelChainService.contains(pc, pPixel))
                .collect(Collectors.toList());
        Framework.logExit(mLogger);
        return pixelChains;
    }

    public void write(PixelMapData pixelMap, IPersistDB db, String id) throws IOException {
        // note that write/read does not preserve the mAllNodes values
        Framework.logEntry(mLogger);

        db.write(id + ".width", String.valueOf(pixelMap.width()));
        db.write(id + ".heioght", String.valueOf(pixelMap.height()));

        // from http://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;
        // mData
        {
            mLogger.finest("About to write mData");
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

        mLogger.info("node count = " + pixelMap.nodes().size());
        mLogger.info("pixelChain count = " + pixelMap.pixelChains().size());
        mLogger.info("segment count = " + pixelMap.segmentCount());

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        Collection<PixelChain> pixelChains = pixelMap.pixelChains().toCollection();
        oos.writeObject(pixelChains);
        oos.close();
        String objectString = MyBase64.compressAndEncode(baos.toByteArray());
        db.write(id + ".objects", objectString);
        objectString = null;
        Framework.logExit(mLogger);
    }

    public ImmutablePixelMapData actionPixelOn(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<Pixel> pixels) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null).actionPixelOn(pixels);
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
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null).actionDeletePixelChain(pixels);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionSetPixelChainThickness(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<Pixel> pixels,
            @NotNull Function<PixelChain, IPixelChain.Thickness> mapper) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null).actionSetPixelChainThickness(pixels, mapper);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
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
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null).actionPixelChainDeleteAllButThis(pixel);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
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
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, transform).actionSetPixelChainDefaultThickness(transform);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionEqualizeValues(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull EqualizeValues values) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, null).actionEqualizeValues(values);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionProcess(
            @NotNull ImmutablePixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NotNull ProgressControl reset) {
        var mutable = pixelMapMappingService.toPixelMap(pixelMap, transformSource).actionProcess(reset);
        return pixelMapMappingService.toImmutablePixelMapData(mutable);
    }

    public Optional<Pixel> getOptionalPixelAt(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, @NotNull Point point) {
        return getOptionalPixelAt(pixelMapData, point.getX(), point.getY());
    }

    private Optional<Pixel> getOptionalPixelAt(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, double x, double y) {
        return Optional.ofNullable(getPixelAt(pixelMapData, x, y));
    }

    private Pixel getPixelAt(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMapData, double xIn, double yIn) {
        int h = pixelMapData.height();
        int x = (int) (xIn * pixelMapData.width());
        int y = (int) (yIn * h);
        y = y == h ? h - 1 : y;
        x = modWidth(pixelMapData, x);
        return new Pixel(x, y);
    }

    public Point toUHVW(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMap, @NotNull Point point) {
                return point.scaleX(aspectRatio(pixelMap));
    }
    /**
     * The Aspect ratio of the image. An aspect ration of 2 means that the image is twice a wide as it is high.
     *
     * @param pixelMap
     * @return
     */
    public double aspectRatio(@NotNull com.ownimage.perception.pixelMap.immutable.PixelMapData pixelMap) {
        return (double) pixelMap.width() / pixelMap.height();
    }
}
