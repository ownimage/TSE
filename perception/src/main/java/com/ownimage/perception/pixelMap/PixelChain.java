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
import com.ownimage.perception.pixelMap.services.Services;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

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

    private Services services = Services.getDefaultServices();

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
        mVertexes = new ImmutableVectorClone<IVertex>().add(services.getVertexService().createVertex(pPixelMap, this, 0, 0));
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

    public PixelChain fixNullPositionVertexes(Services services, PixelMap pixelMap) {
        var context = ImmutablePixelChainContext.of(pixelMap, this);
        var vertexService = services.getVertexService();
        var mappedVertexes = mVertexes.stream()
                .map(v -> {
                    var p = v.getPosition();
                    if (p == null) {
//                        p = v.getPixel(this).getUHVWMidPoint(pixelMap);
                        p = vertexService.getPixel(services, context, v).getUHVWMidPoint(pixelMap);
                        return vertexService.createVertex(this, v.getVertexIndex(), v.getPixelIndex(), p);
                    }
                    return v;
                })
                .collect(Collectors.toList());
        var vertexes = new ImmutableVectorClone<IVertex>().addAll(mappedVertexes);
        return new PixelChain(mPixels, mSegments, vertexes, mLength, mThickness);
    }

    @Override
    public PixelChain clone() {
        try {
            return (PixelChain) super.clone();
        } catch (CloneNotSupportedException pE) {
            throw new RuntimeException("CloneNotSupportedException", pE);
        }
    }

    public PixelChain add(PixelMap pPixelMap, Pixel pPixel) {
        val builder = builder();
        builder.changePixels(p -> p.add(pPixel));
        return builder.build();
    }

    /**
     * Adds the two pixel chains together. It allocates all of the pixels from the pOtherChain to this, unattaches both chains from the middle node, and adds all of the segments from the second chain
     * to the first (joining at the appropriate vertex in the middle, and using the correct offset for the new vertexes). Note that the new segments that are copied from the pOtherChain are all
     * LineApproximations.
     *
     * @param pPixelMap   the pixelMap
     * @param pOtherChain the other chain
     */
    private PixelChain merge(PixelMap pPixelMap, PixelChain pOtherChain) {
        val builder = builder();

        pOtherChain.validate(pPixelMap, false, "add pOtherChain");
        mLogger.fine(() -> String.format("this.mPixels.size() = %s", builder.getPixels().size()));
        mLogger.fine(() -> String.format("pOtherChain.mPixels.size() = %s", pOtherChain.mPixels.size()));
        mLogger.fine(() -> String.format("this.mSegments.size() = %s", builder.getSegments().size()));
        mLogger.fine(() -> String.format("pOtherChain.mSegments.size() = %s", pOtherChain.mSegments.size()));
        if (mLogger.isLoggable(Level.FINE)) {
            builder.streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
            builder.streamSegments().forEach(s -> mLogger.fine(() -> String.format("this.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
            pOtherChain.mSegments.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartVertex(pOtherChain).getPixelIndex(), s.getEndVertex(pOtherChain).getPixelIndex())));
            pOtherChain.mSegments.forEach(s -> mLogger.fine(() -> String.format("pOtherChain.mSegment[%s, %s]", s.getStartIndex(pOtherChain), s.getEndIndex(pOtherChain))));
        }

        builder.build().validate(pPixelMap, false, "merge");
        pOtherChain.validate(pPixelMap, false, "merge");

        if (!builder.getPixels().lastElement().orElseThrow().equals(pOtherChain.firstPixel())) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + pOtherChain);
        }

        int offset = builder.getPixels().size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed
        builder.changePixels(p -> p.remove(builder.getPixels().size() - 1)); // need to remove the last pixel as it will be duplicated on the other chain;
        builder.changePixels(p -> p.addAll(pOtherChain.mPixels.toVector()));
        mLogger.fine(() -> String.format("offset = %s", offset));

        pOtherChain.mSegments.forEach(segment -> {
            IVertex end = services.getVertexService().createVertex(pPixelMap, builder.build(), builder.getVertexes().size(), segment.getEndIndex(pOtherChain) + offset);
            builder.changeVertexes(v -> v.add(end));
            StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder.build(), builder.getSegments().size());
            builder.changeSegments(s -> s.add(newSegment));
        });

        mLogger.fine(() -> String.format("copy.mPixels.size() = %s", builder.getPixels().size()));
        mLogger.fine(() -> String.format("copy.mSegments.size() = %s", builder.getPixels().size()));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("out.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("out.is.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
        return builder.build();
    }

    PixelChain approximate(
            PixelMap pPixelMap,
            double pTolerance
    ) {
        val builder = builder();
        builder.approximate(pPixelMap, pTolerance);
        return builder.build();
    }

    PixelChain approximateCurvesOnly(
            PixelMap pPixelMap,
            double pTolerance,
            double pLineCurvePreference
    ) {
        val builder = builder();
        builder.approximateCurvesOnly(pPixelMap, pTolerance, pLineCurvePreference);
        return builder.build();
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

    /**
     * @deprecated TODO: explain
     */
    @Deprecated
    Pixel firstPixel() {
        return mPixels.firstElement().orElseThrow();
    }

    Optional<Node> getEndNode(PixelMap pPixelMap) {
        return mPixels.lastElement().flatMap(pPixelMap::getNode);
    }

    @Override
    public double getLength() {
        return mLength;
    }

    private void setLength(double pLength) {
        mLength = pLength;
    }

    Optional<Node> getStartNode(PixelMap pPixelMap) {
        return pPixelMap.getNode(mPixels.firstElement().orElseThrow());
    }

    private IVertex getStartVertex() {
        return mVertexes.firstElement().orElse(null);
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
    public int length() {
        return mPixels.size();
    }

    /**
     * Merges two pixel chains together that share a common Node. The result is one PixelChain with a vertex where the Node was. The chain will have correctly attached itself to the node at either
     * end. This needs to be done before after the segments are generated so that the vertex for the node can be created.
     *
     * @param pPixelMap   the PixelMap
     * @param pOtherChain the other chain
     * @param pNode       the node
     */
    PixelChain merge(PixelMap pPixelMap, PixelChain pOtherChain, Node pNode) {
        mLogger.fine("merge");
//        if (!(getStartNode(pPixelMap) == pNode || getEndNode(pPixelMap) == pNode) || !(pOtherChain.getStartNode(pPixelMap) == pNode || pOtherChain.getEndNode(pPixelMap) == pNode)) {
//            throw new IllegalArgumentException("Either this PixelChain: " + this + ", and pOtherChain: " + pOtherChain + ", must share the following node:" + pNode);
//        }

        StrongReference<PixelChain> one = new StrongReference<>(this);
        getEndNode(pPixelMap).filter(n -> n == pNode).ifPresent(n -> one.set(reverse(pPixelMap)));

        StrongReference<PixelChain> other = new StrongReference<>(pOtherChain);
        pOtherChain.getStartNode(pPixelMap).filter(n -> n == pNode).ifPresent(n -> other.set(pOtherChain.reverse(pPixelMap)));

//        if (one.get().getEndNode(pPixelMap) != pNode || other.getStartNode(pPixelMap) != pNode) {
//            throw new RuntimeException("This PixelChain: " + this + " should end on the same node as the other PixelChain: " + pOtherChain + " starts with.");
//        }

        // TODO should recalculate thickness from source values
        mThickness = getPixelCount() > pOtherChain.getPixelCount() ? mThickness : pOtherChain.mThickness;
        return merge(pPixelMap, pOtherChain);
    }

    public PixelChain refine(PixelMap pPixelMap, double tolerance, double lineCurvePreference) {
        var builder = builder();
        // builder.refine(pPixelMap, pSource);
        builder.approximateCurvesOnly(pPixelMap, tolerance, lineCurvePreference);
        return builder.build();
    }

    @Override
    public Thickness getThickness() {
        if (mThickness == null) {
            mThickness = IPixelChain.Thickness.Normal;
        }
        return mThickness;
    }

    /**
     * Sets thickness.  If pThickness is null then it sets the thickness to None.  If there is no change to the thickness
     * then this method returns this object, otherwise it will return a new PixelChain with the new thickness.
     *
     * @param pThickness the p thickness
     * @return the PixeclChain
     */
    public PixelChain setThickness(Thickness pThickness) {
        Framework.logEntry(mLogger);
        var thickness = pThickness != null ? pThickness : Thickness.None;
        if (thickness == mThickness) {
            return this;
        }
        PixelChain clone = clone();
        clone.mThickness = thickness;
        return clone;
    }

    /**
     * Creates a copy of this PixelChain with the order of the pixels in the pixel chain.
     * The original PixelChain is not altered.
     * This means reversing the start and end nodes.
     * All of the segments in the line are also reversed and replaced with new straight line
     * segments.
     *
     * @param pPixelMap the PixelMap this chain belongs to
     * @return a new PixelChain with the elements reversed
     */
    public PixelChain reverse(PixelMap pPixelMap) {
        // note that this uses direct access to the data members as the public setters have other side effects
        //validate("reverse");
        val builder = builder();

        // reverse pixels
        Vector<Pixel> pixels = builder.getPixels().toVector();
        Collections.reverse(pixels);
        builder.changePixels(p -> p.clear().addAll(pixels));

        // reverse vertexes
        int maxPixelIndex = builder.getPixels().size() - 1;
        Vector<IVertex> vertexes = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            IVertex vertex = builder.getVertexes().get(i);
            IVertex v = services.getVertexService().createVertex(pPixelMap, builder, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
        }
        builder.changeVertexes(v -> v.clear().addAll(vertexes));

        // reverse segments
        Vector<ISegment> segments = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            if (i != mVertexes.size() - 1) {
                StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder.build(), segments.size());
                segments.add(newSegment);
            }
        }
        builder.changeSegments(s -> s.clear().addAll(segments));
        return builder.build();
    }

    PixelChain setEndNode(PixelMap pPixelMap, @NonNull Node pNode) {

        val builder = builder();
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

    public Thickness getThickness(int pThinLength, int pNormalLength, int pLongLength) {
        if (length() < pThinLength) {
            return Thickness.None;
        } else if (length() < pNormalLength) {
            return Thickness.Thin;
        } else if (length() < pLongLength) {
            return Thickness.Normal;
        }
        return Thickness.Thick;
    }

    PixelChain setThickness(int pThinLength, int pNormalLength, int pLongLength) {
        Thickness newThickness = getThickness(pThinLength, pNormalLength, pLongLength);

        PixelChain result;
        if (newThickness == mThickness) {
            result = this;
        } else {
            result = clone();
            result.mThickness = newThickness;
        }
        return result;
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
    void validate(PixelMap pPixelMap, boolean pFull, String pMethodName) {
        var context = ImmutablePixelChainContext.of(pPixelMap, this);
        var vertexServices = services.getVertexService();
        try {
            if (getStartVertex().getPixelIndex() != 0) {
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

            IVertex vertex = getStartVertex();
            int index = 0;
            while (vertex != null) {
                if (mVertexes.get(vertex.getVertexIndex()) != vertex) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                if (vertex.getVertexIndex() != index) {
                    throw new RuntimeException("############ VERTEX mismatch in " + pMethodName);
                }

                index++;
                vertex = vertexServices.getEndSegment(services, context, vertex) != null
                        ? vertexServices.getEndSegment(services, context, vertex).getEndVertex(this)
                        : null;
            }

            if (mVertexes.size() != 0) {
                if (vertexServices.getStartSegment(services, context, mVertexes.firstElement().orElseThrow()) != null) {
                    throw new RuntimeException("wrong start vertex");
                }
                if (vertexServices.getEndSegment(services, context, mVertexes.lastElement().orElseThrow()) != null) {
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
                if (i != 0 && vertexServices.getStartSegment(services, context, v) != mSegments.get(i - 1)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
                if (i != mVertexes.size() - 1 && vertexServices.getEndSegment(services, context, v) != mSegments.get(i)) {
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


    void setInChain(PixelMap pPixelMap, boolean pValue) {
        mPixels.forEach(p -> p.setInChain(pPixelMap, pValue));
    }

    private void setEdge(PixelMap pPixelMap) {
        mPixels.stream()
                .filter(p -> p != mPixels.firstElement().orElseThrow())
                .filter(p -> p != mPixels.lastElement().orElseThrow())
                .forEach(p -> p.setEdge(pPixelMap, false));
        mPixels.stream()
                .filter(pPixel -> pPixel.isNode(pPixelMap))
                .filter(p -> p.countEdgeNeighbours(pPixelMap) < 2 || p.countNodeNeighbours(pPixelMap) == 2)
                .forEach(p -> p.setEdge(pPixelMap, false));
    }

    void setVisited(PixelMap pPixelMap, boolean pValue) {
        mPixels.forEach(p -> p.setVisited(pPixelMap, pValue));
    }

    public void delete(PixelMap pPixelMap) {
        mLogger.fine("delete");
        setInChain(pPixelMap, false);
        setVisited(pPixelMap, false);
        setEdge(pPixelMap);
    }

}

