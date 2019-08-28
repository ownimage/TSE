package com.ownimage.perception.pixelMap.segment;


import com.ownimage.framework.math.IntegerPoint;
import lombok.val;

import java.util.HashMap;
import java.util.List;

public class ImmutableSegmentIndex {

    private HashMap<IntegerPoint, List<ISegment>> mPointToSegments = new HashMap();
    private HashMap<ISegment, List<IntegerPoint>> mSegmentToPoints = new HashMap();

    public ImmutableSegmentIndex() {
    }

    @Override
    public ImmutableSegmentIndex clone() {
        val clone = new ImmutableSegmentIndex();
        clone.mPointToSegments = (HashMap) mPointToSegments.clone();
        clone.mPointToSegments = (HashMap) mSegmentToPoints.clone();
        return clone;
    }

//    public ImmutableSegmentIndex addSegment(final ISegment pSegment, final IntegerPoint... pPoints) {
//        val clone = clone();
//
//        Stream.of(pPoints).forEach(point -> {
//            clone.mPointToSegments.
//        }
}
