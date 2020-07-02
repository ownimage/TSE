package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.control.control.ProgressControl;
import com.ownimage.framework.math.IntegerPoint;
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
import com.ownimage.perception.render.ITransformResult;
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
    private final static long serialVersionUID = 1L;

    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();
    private PixelMap lastPixelMap;
    private PixelMapData lastPixelMapData;

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
        return toImmutablePixelMapData(pixelMap);
    }

    public void checkCompatibleSize(@NonNull PixelMapData one, @NotNull PixelMapData other) {
        if (one.width() != other.width() || one.height() != other.height()) {
            throw new IllegalArgumentException("PixelMaps are of different sized.");
        }
    }

    public ImmutablePixelMapData toImmutablePixelMapData(@NotNull PixelMap pixelMap) {
        return ImmutablePixelMapData.builder()
                .width(pixelMap.getWidth())
                .height(pixelMap.getHeight())
                .is360(pixelMap.is360())
                .data(pixelMap.getData())
                .nodes(pixelMap.getImmutableNodeMap())
                .pixelChains(pixelMap.getPixelChains())
                .segmentIndex(pixelMap.getSegmentIndex())
                .segmentCount(pixelMap.getSegmentCount())
                .autoTrackChanges(pixelMap.isAutoTrackChanges())
                .build();
    }

    public PixelMap toPixelMap(@NotNull PixelMapData pixelMapData, @Nullable IPixelMapTransformSource transformSource) {
        if (pixelMapData == lastPixelMapData) {
            return lastPixelMap;
        }
        lastPixelMapData = pixelMapData;
        lastPixelMap = new PixelMap(pixelMapData.width(), pixelMapData.height(), pixelMapData.is360(), transformSource)
                .withData(pixelMapData.data())
                .withNodes(pixelMapData.nodes())
                .withPixelChains(pixelMapData.pixelChains().toCollection())
                .withSegmentIndex(pixelMapData.segmentIndex())
                .withSegmentCount(pixelMapData.segmentCount())
                .withAutoTrackChanges(pixelMapData.autoTrackChanges());
        return lastPixelMap;
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
        var mutable = toPixelMap(pixelMap, null).actionPixelOn(pixels);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionPixelOff(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel,
            int cursorSize) {
        var mutable = toPixelMap(pixelMap, null).actionPixelOff(pixel, cursorSize);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionDeletePixelChain(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<Pixel> pixels) {
        var mutable = toPixelMap(pixelMap, null).actionDeletePixelChain(pixels);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionSetPixelChainThickness(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Collection<Pixel> pixels,
            @NotNull Function<PixelChain, IPixelChain.Thickness> mapper) {
        var mutable = toPixelMap(pixelMap, null).actionSetPixelChainThickness(pixels, mapper);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionPixelToggle(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel) {
        var mutable = toPixelMap(pixelMap, null).actionPixelToggle(pixel);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionPixelChainDeleteAllButThis(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel) {
        var mutable = toPixelMap(pixelMap, null).actionPixelChainDeleteAllButThis(pixel);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionPixelChainApproximateCurvesOnly(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull Pixel pixel,
            @NotNull IPixelMapTransformSource source) {
        var mutable = toPixelMap(pixelMap, source).actionPixelChainApproximateCurvesOnly(pixel);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionReapproximate(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource source) {
        var mutable = toPixelMap(pixelMap, source).actionReapproximate();
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionRerefine(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull IPixelMapTransformSource source) {
        var mutable = toPixelMap(pixelMap, source).actionRerefine();
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionSetPixelChainDefaultThickness(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull CannyEdgeTransform transform) {
        var mutable = toPixelMap(pixelMap, transform).actionSetPixelChainDefaultThickness(transform);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionEqualizeValues(
            @NotNull ImmutablePixelMapData pixelMap,
            @NotNull EqualizeValues values) {
        var mutable = toPixelMap(pixelMap, null).actionEqualizeValues(values);
        return toImmutablePixelMapData(mutable);
    }

    public ImmutablePixelMapData actionProcess(
            @NotNull ImmutablePixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NotNull ProgressControl reset) {
        var mutable = toPixelMap(pixelMap, transformSource).actionProcess(reset);
        return toImmutablePixelMapData(mutable);
    }

    public void transform(
            @NotNull ImmutablePixelMapData pixelMap,
            @Nullable IPixelMapTransformSource transformSource,
            @NotNull ITransformResult renderResult) {
        toPixelMap(pixelMap, transformSource).transform(renderResult);
    }
}
