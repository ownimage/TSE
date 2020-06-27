/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import com.ownimage.perception.pixelMap.services.PixelChainService;
import com.ownimage.perception.pixelMap.services.Services;
import com.ownimage.perception.pixelMap.services.VertexService;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The Class PixelChain. The following shows how a PixelChain would be constructed, populated with Pixels and ISegments
 * <p>
 * <code>
 * <br/>PixelChain chain = new PixelChain(this, pStartNode);
 * <br/>
 * <br/>for (Pixel pixel : ...) {
 * <br/>	chain.add(pixel);
 * <br/>}
 * <br/>
 * <br/>// then at the end
 * <br/>chain.setEndNode(getNode(endPixel));
 * <br/>
 * <br/>// then set up the segments
 * <br/>
 * <br/>mSegments = new Vector<ISegment>(); // mSegments is a private member of PixelChain
 * <br/>for each segment
 * <br/>// note that the first segment needs to be attached to getStartVertex()
 * <br/>// then each segment needs to be attached to the previous endVertex
 * <br/>{
 * <br/>    segment.attachToVertexes(false);
 * <br/>	mSegments.add(segment);
 * <br/>}
 * </code>
 */
public class PixelChain implements Serializable, Cloneable, IPixelChain {

    private final static Logger mLogger = Framework.getLogger();

    private final static long serialVersionUID = 2L;

    @Getter
    private final ImmutableVectorClone<Pixel> mPixels;
    @Getter
    private final ImmutableVectorClone<ISegment> mSegments;
    @Getter
    private final ImmutableVectorClone<IVertex> mVertexes;
    transient private double mLength;
    private Thickness mThickness;

    static private PixelChainService pixelChainService = Services.getDefaultServices().getPixelChainService();


    @Setter
    transient private @NotNull VertexService vertexService = Services.getDefaultServices().getVertexService();

    /**
     * Instantiates a new pixel chain.
     *
     * @param pPixelMap
     * @param pStartNode the start node
     */
    public PixelChain(PixelMap pPixelMap, Node pStartNode) {
        if (pStartNode == null) {
            throw new IllegalArgumentException("pStartNode must not be null");
        }
        mPixels = new ImmutableVectorClone<Pixel>().add(pStartNode);
        mSegments = new ImmutableVectorClone<>();
        mVertexes = new ImmutableVectorClone<IVertex>().add(vertexService.createVertex(pPixelMap, this, 0, 0));
        mThickness = IPixelChain.Thickness.Normal;
    }

    public PixelChain(
            @NonNull ImmutableVectorClone<Pixel> pPixels,
            @NonNull ImmutableVectorClone<ISegment> pSegments,
            @NonNull ImmutableVectorClone<IVertex> pVertexes,
            double pLength,
            @NonNull Thickness pThickness
    ) {
        mPixels = pPixels;
        mSegments = pSegments;
        mVertexes = pVertexes;
        mLength = pLength;
        mThickness = pThickness;

//        validate(pPixelMap, false, "PixelChain");
    }

    private PixelChainBuilder builder() {
        return new PixelChainBuilder(mPixels.toVector(), mVertexes.toVector(), mSegments.toVector(), mLength, mThickness);
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

    boolean contains(Pixel pPixel) {
//        if (mMinX > pPixel.getX() || pPixel.getX() > mMaxX || mMinY > pPixel.getY() || pPixel.getY() > mMaxY)
//            return false;

        return mPixels
                .stream()
                .anyMatch(p -> p.samePosition(pPixel));
    }

    @Override
    public boolean equals(Object pO) {
        if (this == pO) {
            return true;
        }
        if (pO == null || getClass() != pO.getClass()) {
            return false;
        }
        PixelChain that = (PixelChain) pO;
        return Double.compare(that.mLength, mLength) == 0 &&
                mPixels.equals(that.mPixels) &&
                mSegments.equals(that.mSegments) &&
                mVertexes.equals(that.mVertexes) &&
                mThickness == that.mThickness;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPixels, mSegments, mVertexes, mLength, mThickness);
    }

    @Override
    public double getLength() {
        return mLength;
    }



    PixelChain indexSegments(PixelMap pPixelMap, boolean pAdd) {
        if (pAdd) {
            val builder = builder();
            double[] startPosition = {0.0d};
            streamSegments().forEach(segment -> {
                ISegment segmentClone = segment.withStartPosition(startPosition[0]);
                builder.changeSegments(s -> s.set(segmentClone.getSegmentIndex(), segmentClone));
                startPosition[0] += segment.getLength(pPixelMap, builder);
            });
            builder.setLength(startPosition[0]);
            val newPixelChain = builder.build();
            newPixelChain.streamSegments().forEach(segment -> pPixelMap.index(newPixelChain, segment, true));
            return newPixelChain;
        } else {
            this.streamSegments().forEach(segment -> pPixelMap.index(this, segment, false));
            return this;
        }
    }

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    private Pixel lastPixel() {
        // happy for this to throw exception
        return mPixels.lastElement().orElseThrow();
    }

    /**
     * Length of the PixelChain. This is the number of Pixels that it contains.
     *
     * @return the number of Pixels in the PixelChain.
     */
    public int pixelLength() {
        return mPixels.size();
    }

    /**
     * Merges two pixel chains together that share a common Node. The result is one PixelChain with a vertex where the Node was. The chain will have correctly attached itself to the node at either
     * end. This needs to be done before after the segments are generated so that the vertex for the node can be created.
     *
     * @param pPixelMap   the PixelMap
     * @param otherChain the other chain
     * @param pNode       the node
     */
    PixelChain merge(PixelMap pPixelMap, PixelChain otherChain, Node pNode) {
        mLogger.fine("merge");
//        if (!(getStartNode(pPixelMap) == pNode || getEndNode(pPixelMap) == pNode) || !(otherChain.getStartNode(pPixelMap) == pNode || otherChain.getEndNode(pPixelMap) == pNode)) {
//            throw new IllegalArgumentException("Either this PixelChain: " + this + ", and otherChain: " + otherChain + ", must share the following node:" + pNode);
//        }

        StrongReference<PixelChain> one = new StrongReference<>(this);
        pixelChainService.getEndNode(pPixelMap, this).filter(n -> n == pNode).ifPresent(n -> one.set(pixelChainService.reverse(pPixelMap, this)));

        StrongReference<PixelChain> other = new StrongReference<>(otherChain);
        pixelChainService.getStartNode(pPixelMap, otherChain).filter(n -> n == pNode).ifPresent(n -> other.set(pixelChainService.reverse(pPixelMap, otherChain)));

//        if (one.get().getEndNode(pPixelMap) != pNode || other.getStartNode(pPixelMap) != pNode) {
//            throw new RuntimeException("This PixelChain: " + this + " should end on the same node as the other PixelChain: " + otherChain + " starts with.");
//        }

        // TODO should recalculate thickness from source values
        mThickness = getPixelCount() > otherChain.getPixelCount() ? mThickness : otherChain.mThickness;
        return pixelChainService.merge(pPixelMap, this, otherChain);
    }



    @Override
    public Thickness getThickness() {
        if (mThickness == null) {
            mThickness = IPixelChain.Thickness.Normal;
        } // old serialisations might have mThickness null
        return mThickness;
    }



    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");
        sb.append(mPixels.stream().map(Pixel::toString).collect(Collectors.joining(", ")));
        sb.append(" ]\n");

        return sb.toString();
    }

    @SuppressWarnings("OverlyComplexMethod")
    public void validate(PixelMap pixelMap, boolean pFull, String pMethodName) {
        var vertexServices = vertexService;
        try {
            if (pixelChainService.getStartVertex(this).getPixelIndex() != 0) {
                throw new IllegalStateException("getStartVertex().getPixelIndex() != 0");
            }

            if ((mVertexes.size() == 0 && mSegments.size() != 0)) {
                throw new IllegalStateException(String.format("mVertexes.size() = %s && mSegments.size() = %s", mVertexes.size(), mSegments.size()));
            }

            if (mVertexes.size() != 0 && mSegments.size() + 1 != mVertexes.size()) {
                throw new IllegalStateException(String.format("mVertexes.size() = %s && mSegments.size() = %s", mVertexes.size(), mSegments.size()));
            }

            int nextStartIndex[] = {0};

            mSegments.forEach(segment -> {
                if (segment.getStartIndex(this) != nextStartIndex[0]) { //
                    throw new IllegalStateException("segments not linked properly");
                }
                nextStartIndex[0] = segment.getEndIndex(this);
            });

            if (mSegments.size() != 0 && mSegments.lastElement().orElseThrow().getEndIndex(this) != mPixels.size() - 1) { //
                throw new IllegalStateException(String.format("last segment not linked properly, %s, %s, %s", mSegments.size(), mSegments.lastElement().orElseThrow().getEndIndex(this), mPixels.size() - 1));
            }

            checkAllVertexesAttached();

            IVertex vertex = pixelChainService.getStartVertex(this);
            int index = 0;
            while (vertex != null) {
                if (mVertexes.get(vertex.getVertexIndex()) != vertex) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                if (vertex.getVertexIndex() != index) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                index++;
                vertex = vertexServices.getEndSegment( this, vertex) != null
                        ? vertexServices.getEndSegment( this, vertex).getEndVertex(this)
                        : null;
            }

            if (mVertexes.size() != 0) {
                if (vertexServices.getStartSegment( this, mVertexes.firstElement().orElseThrow()) != null) {
                    throw new RuntimeException("wrong start vertex");
                }
                if (vertexServices.getEndSegment( this, mVertexes.lastElement().orElseThrow()) != null) {
                    throw new RuntimeException("wrong end vertex");
                }
            }

            int currentMax = -1;
            for (int i = 0; i < mVertexes.size(); i++) {
                IVertex v = mVertexes.get(i);
                if (i == 0 && v.getPixelIndex() != 0) {
                    throw new IllegalStateException("First vertex wrong)");
                }
                if (i == mVertexes.size() - 1 && v.getPixelIndex() != mPixels.size() - 1 && pFull) {
                    throw new IllegalStateException("Last vertex wrong)");
                }
                if (v.getPixelIndex() <= currentMax) {
                    throw new IllegalStateException("Wrong pixel index order");
                }
                currentMax = v.getPixelIndex();
                if (i != 0 && vertexServices.getStartSegment( this, v) != mSegments.get(i - 1)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
                if (i != mVertexes.size() - 1 && vertexServices.getEndSegment( this, v) != mSegments.get(i)) {
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



}

