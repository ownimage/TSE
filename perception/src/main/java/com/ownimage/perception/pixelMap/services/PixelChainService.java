package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.IPixelChain;
import com.ownimage.perception.pixelMap.IVertex;
import com.ownimage.perception.pixelMap.Node;
import com.ownimage.perception.pixelMap.Pixel;
import com.ownimage.perception.pixelMap.PixelChain;
import com.ownimage.perception.pixelMap.PixelChainBuilder;
import com.ownimage.perception.pixelMap.PixelMap;
import com.ownimage.perception.pixelMap.immutable.PixelMapData;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import lombok.NonNull;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PixelChainService {

    private static PixelMapService pixelMapService = Services.getDefaultServices().getPixelMapService();
    private static VertexService vertexService = Services.getDefaultServices().getVertexService();
    private final static Logger mLogger = Framework.getLogger();

    public PixelChain fixNullPositionVertexes(int height, PixelChain pixelChain) {
        var mappedVertexes = pixelChain.getVertexes().stream()
                .map(v -> {
                    var p = v.getPosition();
                    if (p == null) {
                        p = vertexService.getPixel(pixelChain, v).getUHVWMidPoint(height);
                        return vertexService.createVertex(pixelChain, v.getVertexIndex(), v.getPixelIndex(), p);
                    }
                    return v;
                })
                .collect(Collectors.toList());
        var vertexes = new ImmutableVectorClone<IVertex>().addAll(mappedVertexes);
        return new PixelChain(pixelChain.getPixels(), pixelChain.getSegments(), vertexes, pixelChain.getLength(), pixelChain.getThickness());
    }

    private PixelChainBuilder builder(PixelChain pixelChain) {
        return new PixelChainBuilder(
                pixelChain.getPixels().toVector(),
                pixelChain.getVertexes().toVector(),
                pixelChain.getSegments().toVector(),
                pixelChain.getLength(),
                pixelChain.getThickness()
        );
    }

    public PixelChain add(PixelChain pixelChain, Pixel pPixel) {
        val builder = builder(pixelChain);
        builder.changePixels(p -> p.add(pPixel));
        return builder.build();
    }


    public PixelChain approximate(
            @NotNull PixelMap pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance
    ) {
        val builder = builder(pixelChain);
        builder.approximate(pixelMap, tolerance);
        return builder.build();
    }

    public PixelChain approximateCurvesOnly(
            @NotNull PixelMap pixelMap,
            @NotNull PixelChain pixelChain,
            double tolerance,
            double lineCurvePreference
    ) {
        val builder = builder(pixelChain);
        builder.approximateCurvesOnly(pixelMap, tolerance, lineCurvePreference);
        return builder.build();
    }

    /**
     * Creates a copy of this PixelChain with the order of the pixels in the pixel chain.
     * The original PixelChain is not altered.
     * This means reversing the start and end nodes.
     * All of the segments in the line are also reversed and replaced with new straight line
     * segments.
     *
     * @param pixelMap the PixelMap this chain belongs to
     * @return a new PixelChain with the elements reversed
     */
    public PixelChain reverse(PixelMap pixelMap, PixelChain pixelChain) {
        // note that this uses direct access to the data members as the public setters have other side effects
        //validate("reverse");
        val builder = builder(pixelChain);

        // reverse pixels
        Vector<Pixel> pixels = builder.getPixels().toVector();
        Collections.reverse(pixels);
        builder.changePixels(p -> p.clear().addAll(pixels));

        // reverse vertexes
        int maxPixelIndex = builder.getPixels().size() - 1;
        Vector<IVertex> vertexes = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            IVertex vertex = builder.getVertexes().get(i);
            IVertex v = vertexService.createVertex(pixelMap, builder, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
        }
        builder.changeVertexes(v -> v.clear().addAll(vertexes));

        // reverse segments
        Vector<ISegment> segments = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            if (i != pixelChain.getVertexes().size() - 1) {
                StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pixelMap, builder.build(), segments.size());
                segments.add(newSegment);
            }
        }
        builder.changeSegments(s -> s.clear().addAll(segments));
        return builder.build();
    }

    public PixelChain refine(PixelMap pixelMap, PixelChain pixelChain, double tolerance, double lineCurvePreference) {
        var builder = builder(pixelChain);
        // builder.refine(pixelMap, pSource);
        builder.approximateCurvesOnly(pixelMap, tolerance, lineCurvePreference);
        return builder.build();
    }

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    public Pixel firstPixel(PixelChain pixelChain) {
        return pixelChain.getPixels().firstElement().orElseThrow();
    }

    public Optional<Node> getEndNode(PixelMapData pixelMap, PixelChain pixelChain) {
        return pixelMapService.getNode(pixelMap, pixelChain.getPixels().lastElement().orElseThrow());
    }


    public Optional<Node> getStartNode(PixelMapData pixelMap, PixelChain pixelChain) {
        return pixelMapService.getNode(pixelMap, pixelChain.getPixels().firstElement().orElseThrow());
    }

    public IVertex getStartVertex(PixelChain pixelChain) {
        return pixelChain.getVertexes().firstElement().orElse(null);
    }

    /**
     * Sets thickness.  If pThickness is null then it sets the thickness to None.  If there is no change to the thickness
     * then this method returns this object, otherwise it will return a new PixelChain with the new thickness.
     *
     * @param thickness the p thickness
     * @return the PixeclChain
     */
    public PixelChain withThickness(@NotNull PixelChain pixelChain, @NotNull IPixelChain.Thickness thickness) {
        if (thickness == pixelChain.getThickness()) {
            return pixelChain;
        }
        // TODO what is the best way to do this
        return new PixelChain(pixelChain.getPixels(), pixelChain.getSegments(), pixelChain.getVertexes(), pixelChain.getLength(), thickness);
    }


    public PixelChain setEndNode(@NotNull PixelMap pPixelMap, @NotNull PixelChain pixelChain, @NonNull Node pNode) {

        val builder = builder(pixelChain);
        builder.changePixels(p -> p.add(pNode));

        // need to do a check here to see if we are clobbering over another chain
        // if pixel end-2 is a neighbour of pixel end then pixel end-1 needs to be set as notVisited and removed from the chain
        if (builder.getPixelCount() >= 3 && pNode.isNeighbour(builder.getPixel(builder.getPixelCount() - 3))) {
            var index = builder.getPixelCount() - 2;
            builder.getPixel(index).setVisited(pPixelMap, false);
            builder.changePixels(p -> p.remove(index));
        }
        return builder.build();
    }

    public IPixelChain.Thickness getThickness(
            @NotNull PixelChain pixelChain,  int thinLength, int normalLength, int longLength) {
        var pixelLength = getPixelLength(pixelChain);
        if (pixelLength < thinLength) {
            return IPixelChain.Thickness.None;
        } else if (pixelLength < normalLength) {
            return IPixelChain.Thickness.Thin;
        } else if (pixelLength < longLength) {
            return IPixelChain.Thickness.Normal;
        }
        return IPixelChain.Thickness.Thick;
    }

    public int getPixelLength(@NotNull PixelChain pixelChain) {
        return pixelChain.getPixels().size();
    }

    public PixelChain withThickness(
            @NotNull PixelChain pixelChain,  int thinLength, int normalLength, int longLength) {
        IPixelChain.Thickness thickness = getThickness(pixelChain, thinLength, normalLength, longLength);
        return withThickness(pixelChain, thickness);
    }

    /**
     * Adds the two pixel chains together. It allocates all of the pixels from the otherChain to this, unattaches both chains from the middle node, and adds all of the segments from the second chain
     * to the first (joining at the appropriate vertex in the middle, and using the correct offset for the new vertexes). Note that the new segments that are copied from the otherChain are all
     * LineApproximations.
     *
     * @param pPixelMap   the pixelMap
     * @param otherChain the other chain
     */
    public PixelChain merge(@NotNull PixelMap pPixelMap, @NotNull PixelChain thisChain, @NotNull PixelChain otherChain) {
        val builder = builder(thisChain);

        validate(thisChain, false, "add otherChain");
        mLogger.fine(() -> String.format("builder.getPixels().size() = %s", builder.getPixels().size()));
        mLogger.fine(() -> String.format("pixelLength(otherChain) = %s", pixelLength(otherChain)));
        mLogger.fine(() -> String.format("this.mSegments.size() = %s", builder.getSegments().size()));
        mLogger.fine(() -> String.format("otherChain.mSegments.size() = %s", otherChain.getSegments().size()));
        if (mLogger.isLoggable(Level.FINE)) {
            builder.streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartVertex(thisChain).getPixelIndex(), s.getEndVertex(thisChain).getPixelIndex())));
            builder.streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartIndex(thisChain), s.getEndIndex(thisChain))));
            otherChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("otherChain.mSegment[%s, %s]", s.getStartVertex(otherChain).getPixelIndex(), s.getEndVertex(otherChain).getPixelIndex())));
            otherChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("otherChain.mSegment[%s, %s]", s.getStartIndex(otherChain), s.getEndIndex(otherChain))));
        }

        validate(builder.build(), false, "merge");
        validate(otherChain, false, "merge");

        // TODO this should be a pixelChainService mergable
        if (!builder.getPixels().lastElement().orElseThrow().equals(firstPixel(otherChain))) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + otherChain);
        }

        int offset = builder.getPixels().size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed
        builder.changePixels(p -> p.remove(builder.getPixels().size() - 1)); // need to remove the last pixel as it will be duplicated on the other chain;
        builder.changePixels(p -> p.addAll(otherChain.getPixels()));
        mLogger.fine(() -> String.format("offset = %s", offset));

        otherChain.getSegments().forEach(segment -> {
            IVertex end = vertexService.createVertex(pPixelMap, builder.build(), builder.getVertexes().size(), segment.getEndIndex(otherChain) + offset);
            builder.changeVertexes(v -> v.add(end));
            StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder.build(), builder.getSegments().size());
            builder.changeSegments(s -> s.add(newSegment));
        });

        mLogger.fine(() -> String.format("copy.mPixels.size() = %s", builder.getPixels().size()));
        mLogger.fine(() -> String.format("copy.mSegments.size() = %s", builder.getPixels().size()));
        thisChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("out.mSegment[%s, %s]", s.getStartVertex(thisChain).getPixelIndex(), s.getEndVertex(thisChain).getPixelIndex())));
        thisChain.getSegments().forEach(s -> mLogger.fine(() -> String.format("out.is.mSegment[%s, %s]", s.getStartIndex(thisChain), s.getEndIndex(thisChain))));
        // TODO should recalculate thickness from source values
        var thickness = thisChain.getPixelCount() > otherChain.getPixelCount() ? thisChain.getThickness() : otherChain.getThickness();
        builder.setThickness(thickness);
        return builder.build();
    }

    public void validate(@NotNull  PixelChain pixelChain, boolean pFull, @NotNull String pMethodName) {
        try {
            if (getStartVertex(pixelChain).getPixelIndex() != 0) {
                throw new IllegalStateException("getStartVertex().getPixelIndex() != 0");
            }
            var vertexSize = pixelChain.getVertexes().size();
            var segmentSize = pixelChain.getSegments().size();
            var pixelSize = pixelChain.getPixels().size();

            if (vertexSize == 0 && segmentSize != 0) {
                throw new IllegalStateException(String.format("vertexSize = %s && segmentSize = %s", vertexSize, segmentSize));
            }

            if (vertexSize != 0 && segmentSize + 1 != vertexSize) {
                throw new IllegalStateException(String.format("vertexSize = %s && segmentSize = %s", vertexSize, segmentSize));
            }

            int nextStartIndex[] = {0};

            pixelChain.getSegments().forEach(segment -> {
                if (segment.getStartIndex(pixelChain) != nextStartIndex[0]) { //
                    throw new IllegalStateException("segments not linked properly");
                }
                nextStartIndex[0] = segment.getEndIndex(pixelChain);
            });

            if (segmentSize != 0 && pixelChain.getSegments().lastElement().orElseThrow().getEndIndex(pixelChain) != pixelSize - 1) { //
                throw new IllegalStateException(String.format("last segment not linked properly, %s, %s, %s", segmentSize, pixelChain.getSegments().lastElement().orElseThrow().getEndIndex(pixelChain), pixelSize - 1));
            }

            checkAllVertexesAttached();

            IVertex vertex = getStartVertex(pixelChain);
            int index = 0;
            while (vertex != null) {
                if (pixelChain.getVertexes().get(vertex.getVertexIndex()) != vertex) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                if (vertex.getVertexIndex() != index) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                index++;
                vertex = vertexService.getEndSegment( pixelChain, vertex) != null
                        ? vertexService.getEndSegment( pixelChain, vertex).getEndVertex(pixelChain)
                        : null;
            }

            if (vertexSize != 0) {
                if (vertexService.getStartSegment( pixelChain, pixelChain.getVertexes().firstElement().orElseThrow()) != null) {
                    throw new RuntimeException("wrong start vertex");
                }
                if (vertexService.getEndSegment( pixelChain, pixelChain.getVertexes().lastElement().orElseThrow()) != null) {
                    throw new RuntimeException("wrong end vertex");
                }
            }

            int currentMax = -1;
            for (int i = 0; i < vertexSize; i++) {
                IVertex v = pixelChain.getVertexes().get(i);
                if (i == 0 && v.getPixelIndex() != 0) {
                    throw new IllegalStateException("First vertex wrong)");
                }
                if (i == vertexSize - 1 && v.getPixelIndex() != pixelSize - 1 && pFull) {
                    throw new IllegalStateException("Last vertex wrong)");
                }
                if (v.getPixelIndex() <= currentMax) {
                    throw new IllegalStateException("Wrong pixel index order");
                }
                currentMax = v.getPixelIndex();
                if (i != 0 && vertexService.getStartSegment( pixelChain, v) != pixelChain.getSegments().get(i - 1)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
                if (i != vertexSize - 1 && vertexService.getEndSegment( pixelChain, v) != pixelChain.getSegments().get(i)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
            }
        } catch (Exception pT) {
            printVertexs();
            throw pT;
        }
    }


    private void printVertexs() {
//        StringBuilder sb = new StringBuilder()
//                .append(String.format("mVertexes.size() = %s, mSegments.size() = %s", mVertexes.size(), mSegments.size()))
//                .append("\nArrray\n");
//
//        for (int i = 0; i < mVertexes.size(); i++) {
//            sb.append(String.format("i = %s, mVertexes.get(i).getVertexIndex() = %s\n", i, mVertexes.get(i).getVertexIndex()));
//        }
//
//        sb.append("\n\nWalking\n");
//        IVertex vertex = getStartVertex();
//        int index = 0;
//        while (vertex != null) {
//            sb.append(String.format("index = %s, vertex.getVertexIndex() = %s\n", index, vertex.getVertexIndex()));
//            index++;
//            vertex = vertex.getEndSegment(this) != null
//                    ? vertex.getEndSegment(this).getEndVertex(this)
//                    : null;
//        }
//
//        mLogger.severe(sb::toString);
    }

    private void checkAllVertexesAttached() {
//        mSegments.forEach(segment -> {
//            try {
//                if (mLogger.isLoggable(Level.SEVERE)) {
//                    if (segment.getStartVertex(this).getEndSegment(this) != segment) {
//                        mLogger.severe("start Vertex not attached");
//                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
//                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
//                    }
//                    if (segment.getEndVertex(this).getStartSegment(this) != segment) {
//                        mLogger.severe("end Vertex not attached");
//                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
//                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
//                    }
//                }
//            } catch (Exception pT) {
//                mLogger.log(Level.SEVERE, "Unxepected error", pT);
//            }
//        });
    }

//    private PixelChainBuilder builder() {
//        return new PixelChainBuilder(mPixels.toVector(), mVertexes.toVector(), mSegments.toVector(), mLength, mThickness);
//    }
//
//
//
//
//
//
    public boolean contains(PixelChain pixelChain, Pixel pPixel) {
        return pixelChain.getPixels()
                .stream()
                .anyMatch(p -> p.samePosition(pPixel));
    }

    @Deprecated // this modifies the pixelmap there is a better version in
    public PixelChain indexSegments(PixelMap pixelMap, PixelChain pixelChain, boolean add) {
        if (add) {
            val builder = builder(pixelChain);
            double[] startPosition = {0.0d};
            pixelChain.getSegments().forEach(segment -> {
                ISegment segmentClone = segment.withStartPosition(startPosition[0]);
                builder.changeSegments(s -> s.set(segmentClone.getSegmentIndex(), segmentClone));
                startPosition[0] += segment.getLength(pixelMap, builder);
            });
            builder.setLength(startPosition[0]);
            val newPixelChain = builder.build();
            newPixelChain.streamSegments().forEach(segment -> pixelMap.index(newPixelChain, segment, true));
            return newPixelChain;
        } else {
            pixelChain.getSegments().forEach(segment -> pixelMap.index(pixelChain, segment, false));
            return pixelChain;
        }
    }
//
//    /**
//     * @deprecated TODO: explain
//     */
//    @Deprecated
//    private Pixel lastPixel() {
//        // happy for this to throw exception
//        return mPixels.lastElement().orElseThrow();
//    }
//
    /**
     * Length of the PixelChain. This is the number of Pixels that it contains.
     *
     * @return the number of Pixels in the PixelChain.
     */
    public int pixelLength(@NotNull PixelChain pixelChain) {
        return pixelChain.getPixels().size();
    }


    /**
     * Merges two pixel chains together that share a common Node. The result is one PixelChain with a vertex where the Node was. The chain will have correctly attached itself to the node at either
     * end. This needs to be done before after the segments are generated so that the vertex for the node can be created.
     *
     * @param pPixelMap   the PixelMap
     * @param otherChain the other chain
     * @param pNode       the node
     */
    public PixelChain merge(PixelMap pPixelMap, PixelChain thisChain, PixelChain otherChain, Node pNode) {
        mLogger.fine("merge");
//        if (!(getStartNode(pPixelMap) == pNode || getEndNode(pPixelMap) == pNode) || !(otherChain.getStartNode(pPixelMap) == pNode || otherChain.getEndNode(pPixelMap) == pNode)) {
//            throw new IllegalArgumentException("Either this PixelChain: " + this + ", and otherChain: " + otherChain + ", must share the following node:" + pNode);
//        }

        StrongReference<PixelChain> one = new StrongReference<>(otherChain);
        getEndNode(pPixelMap, otherChain).filter(n -> n == pNode).ifPresent(n -> one.set(reverse(pPixelMap, otherChain)));

        StrongReference<PixelChain> other = new StrongReference<>(otherChain);
        getStartNode(pPixelMap, otherChain).filter(n -> n == pNode).ifPresent(n -> other.set(reverse(pPixelMap, otherChain)));

//        if (one.get().getEndNode(pPixelMap) != pNode || other.getStartNode(pPixelMap) != pNode) {
//            throw new RuntimeException("This PixelChain: " + this + " should end on the same node as the other PixelChain: " + otherChain + " starts with.");
//        }

        return merge(pPixelMap, thisChain, otherChain);
    }
}
