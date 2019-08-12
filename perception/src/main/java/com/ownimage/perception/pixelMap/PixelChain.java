/*
 *  This code is part of the Perception programme.
 *
 *  All code copyright (c) 2018 ownimage.co.uk, Keith Hart
 */
package com.ownimage.perception.pixelMap;

import com.ownimage.framework.math.Line;
import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.framework.util.immutable.ImmutableVectorClone;
import com.ownimage.perception.pixelMap.segment.ISegment;
import com.ownimage.perception.pixelMap.segment.SegmentFactory;
import com.ownimage.perception.pixelMap.segment.StraightSegment;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import java.io.Serializable;
import java.util.Collections;
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

    public final static Logger mLogger = Framework.getLogger();

    public final static long serialVersionUID = 2L;

    @Getter private final ImmutableVectorClone<Pixel> mPixels;
    @Getter private final ImmutableVectorClone<ISegment> mSegments;
    @Getter private final ImmutableVectorClone<IVertex> mVertexes;
    transient private double mLength;
    private Thickness mThickness;

    /**
     * Instantiates a new pixel chain.
     *
     * @param pStartNode the start node
     */
    public PixelChain(final Node pStartNode) {
        if (pStartNode == null) {
            throw new IllegalArgumentException("pStartNode must not be null");
        }
        mPixels = new ImmutableVectorClone<Pixel>().add(pStartNode);
        mSegments = new ImmutableVectorClone<>();
        mVertexes = new ImmutableVectorClone<IVertex>().add(Vertex.createVertex(this, 0, 0));;
        mThickness = IPixelChain.Thickness.Normal;
    }

    public PixelChain(
            final PixelMap pPixelMap,
            @NonNull final ImmutableVectorClone<Pixel> pPixels,
            @NonNull final ImmutableVectorClone<ISegment> pSegments,
            @NonNull final ImmutableVectorClone<IVertex> pVertexes,
            final double pLength,
            @NonNull final Thickness pThickness
    ) {
        mPixels = pPixels;
        mSegments = pSegments;
        mVertexes = pVertexes;
        mLength = pLength;
        mThickness = pThickness;

        validate(pPixelMap, false, "PixelChain");
    }

    private PixelChainBuilder builder() {
        return new PixelChainBuilder(mPixels, mVertexes, mSegments, mLength, mThickness);
    }

    @Override
    public PixelChain clone() {
        try {
            return (PixelChain) super.clone();
        } catch (CloneNotSupportedException pE) {
            throw new RuntimeException("CloneNotSupportedException", pE);
        }
    }

    public PixelChain add(final PixelMap pPixelMap, final Pixel pPixel) {
        val builder = builder();
        builder.changePixels(p -> p.add(pPixel));
        return builder.build(pPixelMap);
    }

     /**
     * Adds the two pixel chains together. It allocates all of the pixels from the pOtherChain to this, unattaches both chains from the middle node, and adds all of the segments from the second chain
     * to the first (joining at the appropriate vertex in the middle, and using the correct offset for the new vertexes). Note that the new segments that are copied from the pOtherChain are all
     * LineApproximations.
     *
     * @param pPixelMap   the pixelMap
     * @param pOtherChain the other chain
     */
    private PixelChain merge(final PixelMap pPixelMap, final PixelChain pOtherChain) {
        final val builder = builder();

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

        builder.build(pPixelMap).validate(pPixelMap, false, "merge");
        pOtherChain.validate(pPixelMap, false, "merge");

        if (!builder.getPixels().lastElement().get().equals(pOtherChain.firstPixel())) {
            throw new IllegalArgumentException("PixelChains not compatible, last pixel of this:" + this + " must be first pixel of other: " + pOtherChain);
        }

        final int offset = builder.getPixels().size() - 1; // this needs to be before the removeElementAt and addAll. The -1 is because the end element will be removed
        builder.changePixels(p -> p.remove(builder.getPixels().size() - 1)); // need to remove the last pixel as it will be duplicated on the other chain;
        builder.changePixels(p -> p.addAll(pOtherChain.mPixels.toVector()));
        mLogger.fine(() -> String.format("offset = %s", offset));

        pOtherChain.mSegments.forEach(segment -> {
            final IVertex end = Vertex.createVertex(builder.build(pPixelMap), builder.getVertexes().size(), segment.getEndIndex(pOtherChain) + offset);
            builder.changeVertexes(v -> v.add(end));
            final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder.build(pPixelMap), builder.getSegments().size());
            builder.changeSegments(s -> s.add(newSegment));
        });

        mLogger.fine(() -> String.format("clone.mPixels.size() = %s", builder.getPixels().size()));
        mLogger.fine(() -> String.format("clone.mSegments.size() = %s", builder.getPixels().size()));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("out.mSegment[%s, %s]", s.getStartVertex(this).getPixelIndex(), s.getEndVertex(this).getPixelIndex())));
        mSegments.forEach(s -> mLogger.fine(() -> String.format("out.is.mSegment[%s, %s]", s.getStartIndex(this), s.getEndIndex(this))));
        return builder.build(pPixelMap);
    }

    PixelChain approximate(final PixelMap pPixelMap, final IPixelMapTransformSource pTransformSource) {
        var tolerance = pTransformSource.getLineTolerance() / pTransformSource.getHeight();
        val builder = builder();
        builder.approximate(pPixelMap, tolerance);
        return builder.build(pPixelMap);
    }

    PixelChain approximateCurvesOnly(
            final PixelMap pPixelMap,
            final double pTolerance,
            final double pLineCurvePreference
    ) {
        val builder = builder();
        builder.approximateCurvesOnly(pPixelMap, pTolerance, pLineCurvePreference);
        return builder.build(pPixelMap);
    }

    private void checkAllVertexesAttached() {
        mSegments.forEach(segment -> {
            try {
                if (mLogger.isLoggable(Level.SEVERE)) {
                    if (segment.getStartVertex(this).getEndSegment(this) != segment) {
                        mLogger.severe("start Vertex not attached");
                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
                    }
                    if (segment.getEndVertex(this).getStartSegment(this) != segment) {
                        mLogger.severe("end Vertex not attached");
                        mLogger.severe("is start segment: " + (segment == getFirstSegment()));
                        mLogger.severe("is end segment: " + (segment == getLastSegment()));
                    }
                }
            } catch (final Throwable pT) {
                mLogger.log(Level.SEVERE, "Unxepected error", pT);
            }
        });
    }

    boolean contains(final Pixel pPixel) {
//        if (mMinX > pPixel.getX() || pPixel.getX() > mMaxX || mMinY > pPixel.getY() || pPixel.getY() > mMaxY)
//            return false;

        return mPixels
                .stream()
                .filter(p -> p.samePosition(pPixel))
                .findAny()
                .isPresent();
    }

    @Deprecated
    Pixel firstPixel() {
        // happy for this to throw exception if first element does not exist
        return mPixels.firstElement().get();
    }

    Optional<Node> getEndNode(PixelMap pPixelMap) {
        return mPixels.lastElement().flatMap(pPixelMap::getNode);
    }

    @Override
    public double getLength() {
        return mLength;
    }

    Optional<Node> getStartNode(final PixelMap pPixelMap) {
        return pPixelMap.getNode(mPixels.firstElement().get());
    }

    private IVertex getStartVertex() {
        return mVertexes.firstElement().orElse(null);
    }

    PixelChain indexSegments(final PixelMap pPixelMap) {
        val builder = builder();
        final double[] startPosition = {0.0d};
        streamSegments().forEach(segment -> {
            final ISegment segmentClone = segment.withStartPosition(startPosition[0]);
            builder.changeSegments(s -> s.set(segmentClone.getSegmentIndex(), segmentClone));
            startPosition[0] += segment.getLength(pPixelMap, builder);
        });
        builder.setLength(startPosition[0]);
        val newPixelChain = builder.build(pPixelMap);
        newPixelChain.streamSegments().forEach(segment -> pPixelMap.index(newPixelChain, segment));
        return newPixelChain;
    }

    @Deprecated
    private Pixel lastPixel() {
        // happy for this to throw exception
        return mPixels.lastElement().get();
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
    PixelChain merge(final PixelMap pPixelMap, final PixelChain pOtherChain, final Node pNode) {
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

    public PixelChain refine(final PixelMap pPixelMap, final IPixelMapTransformSource pSource) {
        var builder = builder();
        builder.refine(pPixelMap, pSource);
        return builder.build(pPixelMap);
    }

    public Thickness getThickness() {
        if (mThickness == null) {
            mThickness = IPixelChain.Thickness.Normal;
        }
        return mThickness;
    }

    private boolean containsStraightSegment() {
        return mSegments.stream()
                .filter(s -> (s instanceof StraightSegment))
                .findFirst()
                .isPresent();
    }







    private Line calcStartTangent(PixelMap pPixelMap, final ISegment pSegment, boolean pInitialRun) {
        var previousSegment = pSegment.getStartVertex(this).getStartSegment(this);
        return pInitialRun || !(previousSegment instanceof StraightSegment)
                ? pSegment.getStartVertex(this).calcTangent(this, pPixelMap)
                : previousSegment.getEndTangent(pPixelMap, this);
    }

    private Line calcEndTangent(PixelMap pPixelMap, final ISegment pSegment, boolean pInitialRun) {
        var nextSegment = pSegment.getEndVertex(this).getEndSegment(this);
        return pInitialRun || !(nextSegment instanceof StraightSegment)
                ? pSegment.getEndVertex(this).calcTangent(this, pPixelMap)
                : nextSegment.getStartTangent(pPixelMap, this);
    }

    private Line getDCStartTangent(final PixelMap pPixelMap, final ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex startVertex = pSegment.getStartVertex(this);
        if (startVertex.getStartSegment(this) instanceof StraightSegment) {
            return startVertex.getStartSegment(this).getEndTangent(pPixelMap, this);
        }

        return startVertex.calcTangent(this, pPixelMap);
    }

    private Line getDCEndTangent(final PixelMap pPixelMap, final ISegment pSegment) {
        if (pSegment == null) {
            throw new IllegalArgumentException("Segment must not be null.");
        }

        final IVertex endVertex = pSegment.getEndVertex(this);
        if (endVertex.getEndSegment(this) instanceof StraightSegment) {
            return endVertex.getEndSegment(this).getStartTangent(pPixelMap, this);
        }

        return endVertex.calcTangent(this, pPixelMap);
    }

    /**
     * Creates a copy of this PixelChain with the order of the pixels in the pixel chain.
     * The original PixelChain is not altered.
     * This means reversing the start and end nodes.
     * All of the segments in the line are also reversed and replaced with new straight line
     * segments.
     *
     * @param pPixelMap
     * @return a new PixelChain with the elements reversed
     */
    public PixelChain reverse(final PixelMap pPixelMap) {
        // note that this uses direct access to the data members as the public setters have other side effects
        //validate("reverse");
        val builder = builder();

        // reverse pixels
        Vector<Pixel> pixels = builder.getPixels().toVector();
        Collections.reverse(pixels);
        builder.changePixels(p -> p.clear().addAll(pixels));

        // reverse vertexes
        final int maxPixelIndex = builder.getPixels().size() - 1;
        final Vector<IVertex> vertexes = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            final IVertex vertex = builder.getVertexes().get(i);
            final IVertex v = Vertex.createVertex(builder, vertexes.size(), maxPixelIndex - vertex.getPixelIndex());
            vertexes.add(v);
        }
        builder.changeVertexes(v -> v.clear().addAll(vertexes));

        // reverse segments
        final Vector<ISegment> segments = new Vector<>();
        for (int i = builder.getVertexes().size() - 1; i >= 0; i--) {
            if (i != mVertexes.size() - 1) {
                final StraightSegment newSegment = SegmentFactory.createTempStraightSegment(pPixelMap, builder.build(pPixelMap), segments.size());
                segments.add(newSegment);
            }
        }
        builder.changeSegments(s -> s.clear().addAll(segments));
        return builder.build(pPixelMap);
    }

    PixelChain setEndNode(final PixelMap pPixelMap, @NonNull final Node pNode) {

        val builder = builder();
        builder.changePixels(p-> p.add(pNode));

        // need to do a check here to see if we are clobbering over another chain
        // if pixel end-2 is a neighbour of pixel end then pixel end-1 needs to be set as notVisited and removed from the chain
        if (builder.getPixelCount() >= 3 && pNode.isNeighbour(builder.getPixel(builder.getPixelCount() - 3))) {
            var index = builder.getPixelCount() - 2;
            builder.getPixel(index).setVisited(pPixelMap, false);
            builder.changePixels(p -> p.remove(index));
        }
        return builder.build(pPixelMap);
    }

    private void setLength(final double pLength) {
        mLength = pLength;
    }

    PixelChain setThickness(final int pThinLength, final int pNormalLength, final int pLongLength) {
        Framework.logEntry(mLogger);
        Framework.logParams(mLogger, "pNormalLength, pLongLength", pNormalLength, pLongLength);

        Thickness newThickness;
        if (length() < pThinLength) {
            newThickness = IPixelChain.Thickness.None;
        } else if (length() < pNormalLength) {
            newThickness = IPixelChain.Thickness.Thin;
        } else if (length() < pLongLength) {
            newThickness = IPixelChain.Thickness.Normal;
        } else {
            newThickness = IPixelChain.Thickness.Thick;
        }

        PixelChain result;
        if (newThickness == mThickness) result = this;
        else {
            result = clone();
            result.mThickness = newThickness;
        }
        return result;
    }

    public PixelChain setThickness(final Thickness pThickness) {
        Framework.logEntry(mLogger);
        if (pThickness == mThickness) return this;
        PixelChain clone = clone();
        clone.mThickness = pThickness;
        return clone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PixelChain[ ");
        sb.append(mPixels.stream().map(Pixel::toString).collect(Collectors.joining(", ")));
        sb.append(" ]\n");

        return sb.toString();
    }

    void validate(final PixelMap pPixelMap, boolean pFull, final String pMethodName) {
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

            final int nextStartIndex[] = {0};

            mSegments.forEach(segment -> {
                if (segment.getStartIndex(this) != nextStartIndex[0]) { //
                    throw new IllegalStateException("segments not linked properly");
                }
                nextStartIndex[0] = segment.getEndIndex(this);
            });

            if (mSegments.size() != 0 && mSegments.lastElement().get().getEndIndex(this) != mPixels.size() - 1) { //
                throw new IllegalStateException(String.format("last segment not linked properly, %s, %s, %s", mSegments.size(), mSegments.lastElement().get().getEndIndex(this), mPixels.size() - 1));
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
                vertex = vertex.getEndSegment(this) != null
                        ? vertex.getEndSegment(this).getEndVertex(this)
                        : null;
            }

            if (mVertexes.size() != 0) {
                if (mVertexes.firstElement().get().getStartSegment(this) != null)
                    throw new RuntimeException("wrong start vertex");
                if (mVertexes.lastElement().get().getEndSegment(this) != null) throw new RuntimeException("wrong end vertex");
            }

            int currentMax = -1;
            for (int i = 0; i < mVertexes.size(); i++) {
                final IVertex v = mVertexes.get(i);
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
                if (i != 0 && v.getStartSegment(this) != mSegments.get(i - 1)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
                if (i != mVertexes.size() - 1 && v.getEndSegment(this) != mSegments.get(i)) {
                    throw new RuntimeException(String.format("start segment mismatch i = %s", i));
                }
            }
        } catch (final Throwable pT) {
            printVertexs();
            throw pT;
        }
    }

    public void printVertexs() {
        final StringBuilder sb = new StringBuilder()
                .append(String.format("mVertexes.size() = %s, mSegments.size() = %s", mVertexes.size(), mSegments.size()))
                .append("\nArrray\n");

        for (int i = 0; i < mVertexes.size(); i++) {
            sb.append(String.format("i = %s, mVertexes.get(i).getVertexIndex() = %s\n", i, mVertexes.get(i).getVertexIndex()));
        }

        sb.append("\n\nWalking\n");
        IVertex vertex = getStartVertex();
        int index = 0;
        while (vertex != null) {
            sb.append(String.format("index = %s, vertex.getVertexIndex() = %s\n", index, vertex.getVertexIndex()));
            index++;
            vertex = vertex.getEndSegment(this) != null
                    ? vertex.getEndSegment(this).getEndVertex(this)
                    : null;
            ;
        }

        mLogger.severe(sb::toString);
    }



    void setInChain(final PixelMap pPixelMap, final boolean pValue) {
        mPixels.forEach(p -> p.setInChain(pPixelMap, pValue));
    }

    private void setEdge(final PixelMap pPixelMap) {
        mPixels.stream()
                .filter(p -> p != mPixels.firstElement().get())
                .filter(p -> p != mPixels.lastElement().get())
                .forEach(p -> p.setEdge(pPixelMap, false));
        mPixels.stream()
                .filter(pPixel -> pPixel.isNode(pPixelMap))
                .filter(p -> p.countEdgeNeighbours(pPixelMap) < 2 || p.countNodeNeighbours(pPixelMap) == 2)
                .forEach(p -> p.setEdge(pPixelMap, false));
    }

    void setVisited(final PixelMap pPixelMap, final boolean pValue) {
        mPixels.forEach(p -> p.setVisited(pPixelMap, pValue));
    }

    public void delete(final PixelMap pPixelMap) {
        mLogger.fine("delete");
        setInChain(pPixelMap, false);
        setVisited(pPixelMap, false);
        setEdge(pPixelMap);
    }

}

