package com.ownimage.perception.pixelMap.services;

import com.ownimage.framework.util.Range2D;
import com.ownimage.framework.util.StrongReference;
import com.ownimage.perception.pixelMap.immutable.IXY;
import com.ownimage.perception.pixelMap.immutable.ImmutablePixelMap;
import com.ownimage.perception.pixelMap.immutable.IntegerXY;
import com.ownimage.perception.pixelMap.immutable.Node;
import io.vavr.Tuple2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.ownimage.perception.pixelMap.PixelConstants.EDGE;
import static com.ownimage.perception.pixelMap.PixelConstants.NODE;
import static com.ownimage.perception.pixelMap.PixelConstants.NONE;

public class PixelMapValidationService {

    public static <R> Predicate<R> not(Predicate<R> predicate) {
        return predicate.negate();
    }

    private PixelChainService pixelChainService;

    @Autowired
    public void setPixelChainService(PixelChainService pixelChainService) {
        this.pixelChainService = pixelChainService;
    }

    public boolean validate(@NotNull ImmutablePixelMap pixelMap) {
        var dataNodes = getDataNodes(pixelMap);
        var singletonNodes = getSingletonNodes(pixelMap, dataNodes);
        var pixelMapNodes = pixelMap.nodes().toHashMap();
        var dataEdges = getDataEdges(pixelMap);
        return checkAllDataNodesAreDataEdges(dataNodes, dataEdges)
                & checkPixelMapNodesKeyMatchesValue(pixelMapNodes)
                & checkAllDataNodesShouldBeNodes(pixelMap, dataNodes)
                & checkNoPixelMapNodesAreSingletons(pixelMapNodes, singletonNodes)
                & checkNoPixelMapNodesAreBristles(pixelMap, pixelMapNodes)
                & checkAllDataEdgesHave2Neighbours(pixelMap, dataEdges, dataNodes)
                & checkAllPixelsChainsHaveValidNodeEnds(pixelMap, pixelMapNodes)
                & checkPixelChainEndsReferenceNodesThatReferenceThePixelChain(pixelMap)
                & checkAllPixelMapNodesReferencePixelChainsInPixelMap(pixelMap, pixelMapNodes)
                & checkAllPixelMapNodesAreDataNodes(pixelMapNodes, dataNodes)
                & checkAllDataNodesArePixelMapNodesOrSingletons(dataNodes, pixelMapNodes, singletonNodes);
    }

    public boolean checkPixelChainEndsReferenceNodesThatReferenceThePixelChain(
            @NotNull ImmutablePixelMap pixelMap) {
        var result = pixelMap.pixelChains().stream()
                .flatMap(pc -> Stream.of(
                        new Tuple2<>(pc, pixelMap.nodes().get(pixelChainService.getStartNode(pixelMap, pc).get().toPixelMapGridPosition())),
                        new Tuple2<>(pc, pixelMap.nodes().get(pixelChainService.getEndNode(pixelMap, pc).get().toPixelMapGridPosition()))
                ))
                .filter(not(t2 -> t2._2.containsPixelChain(t2._1)))
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkPixelChainEndsReferenceNodesThatReferenceThePixelChain failure");
    }

    public boolean checkNoPixelMapNodesAreBristles(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Map<IntegerXY, Node> pixelMapNodes) {
        var result = pixelMapNodes.keySet().stream()
                .filter(ip -> countNonNullNeighbours(pixelMap, ip) == 1)
                .flatMap(ip -> stream8Neighbours(pixelMap, ip))
                .filter(pixelMapNodes::containsKey)
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkNoPixelMapNodesAreBristles failure");
    }

    public boolean checkAllPixelMapNodesReferencePixelChainsInPixelMap(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Map<IntegerXY, Node> pixelMapNodes) {
        var result = pixelMapNodes.values().stream()
                .flatMap(Node::streamPixelChains)
                .filter(not(pixelMap.pixelChains()::contains))
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkAllPixelMapNodesReferencePixelChainsInPixelMap failure");
    }

    /**
     * Both ends of the PixelChain should be in the PixelMap.nodes
     *
     * @param pixelMap
     * @param dataNodes
     * @return
     */
    public boolean checkAllPixelsChainsHaveValidNodeEnds(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Map<IntegerXY, Node> dataNodes) {
        var dataNodesKeySet = dataNodes.keySet();
        var result = pixelMap.pixelChains().stream()
                .map(pc -> pc.getPixels().firstElement().orElseThrow())
                .map(IntegerXY::new)
                .filter(not(dataNodesKeySet::contains))
                .findFirst()
                .isEmpty()
                &&
                pixelMap.pixelChains().stream()
                        .map(pc -> pc.getPixels().lastElement().orElseThrow())
                        .map(IntegerXY::new)
                        .filter(not(dataNodesKeySet::contains))
                        .findFirst()
                        .isEmpty();
        return throwErrorIfFalse(result, "checkAllPixelsChainsHaveValidNodeEnds failure");
    }

    public boolean checkAllDataEdgesHave2Neighbours(
            @NotNull ImmutablePixelMap pixelMap,
            @NotNull Set<IntegerXY> dataEdges,
            @NotNull Set<IntegerXY> dataNodes) {
        var failure = StrongReference.of((IntegerXY) null);
        var result = dataEdges.stream()
                .filter(not(dataNodes::contains))
                .filter(ip -> !(countNonNullNeighbours(pixelMap, ip) == 2 || countEdgeNotNodeNeighbours(pixelMap, ip) == 2))
                .peek(ip -> failure.set(ip))
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkAllDataEdgesHave2Neighbours failure: " + failure.get());
    }

    /**
     * All items marked as NODE in the pixelMap.data should actually be NODEs.  This means that they
     * must be an edge and have other than two neighbors that are also edges.
     *
     * @param pixelMap
     * @param dataNodes
     * @return
     */
    public boolean checkAllDataNodesShouldBeNodes(
            @NotNull ImmutablePixelMap pixelMap, @NotNull Set<IntegerXY> dataNodes) {
        var failure = StrongReference.of((IntegerXY) null);
        var result = dataNodes.stream()
                .filter(n -> !shouldBeNode(pixelMap, n))
                .peek(ip -> failure.set(ip))
                .findFirst()
                .isEmpty();
        if (!result) { // check if it is a loop
            result = pixelMap.pixelChains().stream()
                    .filter(pc -> pc.getPixels().firstElement().orElseThrow().samePosition(failure.get()))
                    .filter(pc -> pc.getPixels().lastElement().orElseThrow().samePosition(failure.get()))
                    .findFirst()
                    .isPresent();
        }
        return throwErrorIfFalse(result, "checkAllDataNodesShouldBeNodes failure: " + failure.get());
    }

    /**
     * @param pixelMap
     * @return true if there are no edges or neighbours around the point
     */
    public boolean isSingleton(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY point) {
        return shouldBeNode(pixelMap, point)
                && stream8Neighbours(pixelMap, point)
                .filter(p -> pixelMap.data().get(p.getX(), p.getY()) != NONE)
                .findAny()
                .isEmpty();
    }

    /**
     * To be a node it must be an edge and not have 2 neighbours
     *
     * @param pixelMap
     * @param point
     * @return if the point is a node in pixelMap
     */
    public boolean shouldBeNode(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY point) {
        var edge = (pixelMap.data().get(point.getX(), point.getY()) & EDGE) == EDGE;
        if (!edge) {
            return false;
        }

        return countNonNullNeighbours(pixelMap, point) != 2;
    }

    public boolean isDataNode(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY point) {
        return (pixelMap.data().get(point.getX(), point.getY()) & NODE) == NODE;
    }

    public boolean isDataEdge(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY point) {
        return (pixelMap.data().get(point.getX(), point.getY()) & EDGE) == EDGE;
    }

    public int countNonNullNeighbours(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY point) {
        return (int) stream8Neighbours(pixelMap, point)
                .filter(p -> pixelMap.data().get(p.getX(), p.getY()) != NONE)
                .count();
    }

    public int countEdgeNotNodeNeighbours(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY point) {
        return (int) stream8Neighbours(pixelMap, point)
                .map(p -> pixelMap.data().get(p.getX(), p.getY()))
                .filter(b -> (byte)(b & EDGE) == EDGE)
                .filter(b -> (byte)(b & NODE) == 0)
                .count();
    }

    /**
     * A singleton node is one that has no neighbours.
     *
     * @param pixelMap
     * @param dataNodes
     * @return
     */
    public HashSet<IntegerXY> getSingletonNodes(@NotNull ImmutablePixelMap pixelMap, @NotNull Set<IntegerXY> dataNodes) {
        var singletonNODES = new HashSet<IntegerXY>();
        dataNodes.stream()
                .filter(ip -> isSingleton(pixelMap, ip))
                .forEach(singletonNODES::add);
        return singletonNODES;
    }

    public boolean checkNoPixelMapNodesAreSingletons(Map<IntegerXY, Node> pixelMapNodes, @NotNull Set<IntegerXY> singletons) {
        var result = pixelMapNodes.keySet().stream()
                .filter(singletons::contains)
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkNoPixelMapNodesAreSingletons failure");
    }

    public Stream<IXY> stream8Neighbours(@NotNull ImmutablePixelMap pixelMapData, @NotNull IntegerXY center) {
        return new Range2D(-1, 2, -1, 2).stream()
                .map(IntegerXY::new)
                .map(ip -> center.add(ip))
                .filter(ip -> !ip.equals(center))
                .filter(ip -> isInBounds(pixelMapData, ip));
    }

    public boolean isInBounds(@NotNull ImmutablePixelMap pixelMapData, @NotNull IXY point) {
        return point.getX() >= 0 && point.getY() >= 0
                && point.getX() < pixelMapData.width() && point.getY() < pixelMapData.height();
    }


    public boolean checkAllDataNodesArePixelMapNodesOrSingletons(
            @NotNull Collection<IntegerXY> dataNodes,
            @NotNull Map<IntegerXY, Node> pixelMapNodes,
            @NotNull Set<IntegerXY> singletonNodes) {
        var failure = StrongReference.of((IntegerXY) null);
        var result = dataNodes.stream()
                .filter(n -> pixelMapNodes.get(n) == null)
                .filter(not(singletonNodes::contains))
                .peek(ip -> failure.set(ip))
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkAllDataNodesArePixelMapNodesOrSingletons failure: " + failure.get());
    }

    public boolean throwErrorIfFalse(boolean result, @NotNull String message) {
        if (!result) {
            throw new RuntimeException(message);
        }
        return result;
    }

    public boolean checkPixelMapNodesKeyMatchesValue(@NotNull Map<IntegerXY, Node> pixelMapNodes) {
        var result = pixelMapNodes.entrySet().stream()
                .filter(e -> !(e.getKey().getX() == e.getValue().getX() && e.getKey().getY() == e.getValue().getY()))
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkPixelMapNodesKeyMatchesValue failure");
    }

    public boolean checkAllPixelMapNodesAreDataNodes(@NotNull Map<IntegerXY, Node> pixelMapNodes, @NotNull Set<IntegerXY> dataNodes) {
        var result = pixelMapNodes.keySet().stream()
                .filter(ip -> !dataNodes.contains(ip))
                .findFirst()
                .isEmpty();
        return throwErrorIfFalse(result, "checkAllPixelMapNodesAreDataNodes failure");
    }

    public boolean checkAllDataNodesAreDataEdges(@NotNull Set<IntegerXY> dataNodes, @NotNull Set<IntegerXY> dataEdges) {
        var result = dataNodes.stream()
                .filter(n -> !dataEdges.contains(n))
                .findAny()
                .isEmpty();
        return throwErrorIfFalse(result, "checkAllDataNodesAreDataEdges failure");
    }

    public Set<IntegerXY> getDataNodes(@NotNull ImmutablePixelMap pixelMap) {
        return getDataByType(pixelMap, NODE);
    }

    public Set<IntegerXY> getDataEdges(@NotNull ImmutablePixelMap pixelMap) {
        return getDataByType(pixelMap, EDGE);
    }

    public Set<IntegerXY> getDataByType(@NotNull ImmutablePixelMap pixelMap, byte type) {
        var dataNodes = new HashSet<IntegerXY>();
        new Range2D(pixelMap.width(), pixelMap.height()).stream()
                .filter(ip -> (pixelMap.data().get(ip.getX(), ip.getY()) & type) == type)
                .map(IntegerXY::new)
                .forEach(dataNodes::add);
        return dataNodes;
    }

    public String pixelAreaToString(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY centre, int size) {

        var sb = new StringBuilder();
        for (int y = centre.getY() - size; y <= centre.getY() + size; y++) {
            for (int x = centre.getX() - size; x <= centre.getX() + size; x++) {
                var ip = new IntegerXY(x, y);
                sb.append(pixelToString(pixelMap, ip));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public String pixelToString(@NotNull ImmutablePixelMap pixelMap, @NotNull IntegerXY point) {
        if (!isInBounds(pixelMap, point)) {
            return "X";
        }
        if (isDataNode(pixelMap, point) && isDataEdge(pixelMap, point)) {
            return "N";
        }
        if (isDataNode(pixelMap, point) && !isDataEdge(pixelMap, point)) {
            return "n";
        }
        if (isDataEdge(pixelMap, point)) {
            return "E";
        }
        return " ";
    }
}
