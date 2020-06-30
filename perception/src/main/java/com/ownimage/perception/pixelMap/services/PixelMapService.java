package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.persist.IPersistDB;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.MyBase64;
import com.ownimage.framework.util.immutable.ImmutableMap2D;
import com.ownimage.perception.pixelMap.IPixelMapTransformSource;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelMapService {

    private final static Logger mLogger = Framework.getLogger();
    private final static long serialVersionUID = 1L;

    private static PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();


    public boolean canRead(IPersistDB pDB, String pId) {
        String pixelString = pDB.read(pId + ".data");
        return pixelString != null && !pixelString.isEmpty();
    }

    public PixelMap read(IPersistDB db, String id, IPixelMapTransformSource transformSource) {
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
        return pixelMap;
    }

    public void write(PixelMap pixelMap, IPersistDB db, String id) throws IOException {
        // note that write/read does not preserve the mAllNodes values
        Framework.logEntry(mLogger);

        db.write(id + ".width", String.valueOf(pixelMap.getWidth()));
        db.write(id + ".heioght", String.valueOf(pixelMap.getHeight()));

        // from http://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
        ByteArrayOutputStream baos;
        ObjectOutputStream oos;
        // mData
        {
            mLogger.finest("About to write mData");
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            for (int x = 0; x < pixelMap.getWidth(); x++) {
                byte[] buff = new byte[pixelMap.getHeight()];
                for (int y = 0; y < pixelMap.getHeight(); y++) {
                    buff[y] = pixelMap.getValue(x, y);
                }
                oos.write(buff);
            }
            oos.close();
            String pixelString = MyBase64.compressAndEncode(baos.toByteArray());
            db.write(id + ".data", pixelString);
            pixelString = null;
        }

        mLogger.info("node count = " + pixelMap.nodeCount());
        mLogger.info("pixelChain count = " + pixelMap.getPixelChains().size());
        mLogger.info("segment count = " + pixelMap.getSegmentCount());

        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        Collection<PixelChain> pixelChains = pixelMap.getPixelChains().toCollection();
        oos.writeObject(pixelChains);
        oos.close();
        String objectString = MyBase64.compressAndEncode(baos.toByteArray());
        db.write(id + ".objects", objectString);
        objectString = null;
        Framework.logExit(mLogger);
    }

}
