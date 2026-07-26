package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph;

import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ClientBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ServerBalloon;
import dev.eriksonn.aeronautics.index.AeroTags;
import dev.ryanhcode.sable.util.LevelAccelerator;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class BalloonBuilder {

    protected static final Direction[] HORIZONTAL_DIRECTIONS = new Direction[]{
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    /**
     * @return if the given position is not airtight, and is not in an existing layer
     */
    public static boolean isCandidatePosition(final BlockPos pos,
                                              final BlockState state,
                                              final BalloonLayerGraph graph,
                                              final BalloonLayerGraph existingGraph,
                                              final BalloonLayerGraph mainGraph) {
        if (containsBlockAt(pos, graph)) return false;
        if (existingGraph != null && containsBlockAt(pos, existingGraph)) return false;
        if (mainGraph != null && containsBlockAt(pos, mainGraph)) return false;

        return state.isAir() || !state.is(AeroTags.BlockTags.AIRTIGHT);
    }

    public static boolean isCandidatePosition(final LevelAccelerator accelerator,
                                              final BlockPos pos,
                                              final BalloonLayerGraph graph,
                                              final BalloonLayerGraph existingGraph,
                                              final BalloonLayerGraph mainGraph) {
        final BlockState state = accelerator.getBlockState(pos);
        return isCandidatePosition(pos, state, graph, existingGraph, mainGraph);
    }

    private static boolean containsBlockAt(final BlockPos pos, final BalloonLayerGraph graph) {
        final List<BalloonLayerData> layers = graph.getLayersAtY(pos.getY());

        for (final BalloonLayerData layer : layers) {
            if (layer.getHotAirBlock(pos.getX(), pos.getZ())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fills a balloon into a layer-graph from a given start position
     */
    public static BalloonLayerGraph buildBalloon(final Level level,
                                                 final BlockPos startPos,
                                                 @Nullable final BalloonLayerGraph mainGraph) {
        final LevelAccelerator accelerator = new LevelAccelerator(level);
        final BalloonLayerGraph graph = new BalloonLayerGraph(startPos.getY());
        final boolean firstSafe = BalloonBuilder.upwardsBiasedFloodFill(accelerator, startPos, graph, null, mainGraph);

        if (!firstSafe) {
            return null;
        }

        completeBalloon(accelerator, graph, mainGraph);

        graph.rebuildConnections(startPos);
        return graph;
    }

    public static void completeBalloon(final LevelAccelerator accelerator, final BalloonLayerGraph graph, @Nullable final BalloonLayerGraph mainGraph) {
        final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        boolean progressMade = true;

        while (progressMade) {
            progressMade = false;

            final List<BalloonLayerData>[] layerMap = graph.getAllLayers();
            final int minY = graph.getMinY();
            final int maxY = minY + layerMap.length;

            for (int layerY = minY; layerY < maxY; layerY++) {
                final List<BalloonLayerData> layersAtY = layerMap[layerY - minY];

                for (final BalloonLayerData layer : new ObjectArrayList<>(layersAtY)) {
                    if (layer.getState() == BalloonLayerData.State.COMPLETE)
                        continue;

                    final LongLinkedOpenHashSet queue = new LongLinkedOpenHashSet();
                    final Iterator<BlockPos> candidates = layer.blockIterator();

                    while (candidates.hasNext()) {
                        queue.add(mutableBlockPos.set(candidates.next())
                                .move(0, -1, 0)
                                .asLong());
                    }
                    while (!queue.isEmpty()) {
                        final long l = queue.removeFirstLong();
                        final BlockPos candidate = mutableBlockPos.set(l);

                        final BalloonLayerGraph newGraph = new BalloonLayerGraph(candidate.getY());
                        if (!isCandidatePosition(accelerator, candidate, newGraph, graph, mainGraph)) {
                            continue;
                        }

                        final boolean safe = BalloonBuilder.upwardsBiasedFloodFill(accelerator, candidate, newGraph, graph, mainGraph);

                        if (!safe) {
                            removeReachable(accelerator, queue, mutableBlockPos);
                            continue;
                        }

                        final List<BalloonLayerData> newLayersAtY = newGraph.getLayersAtY(candidate.getY());
                        queue.removeIf(x -> {
                            for (final BalloonLayerData layerData : newLayersAtY) {
                                if (layerData.getHotAirBlock(BlockPos.getX(x), BlockPos.getZ(x))) {
                                    return true;
                                }
                            }
                            return false;
                        });

                        graph.addAll(newGraph);
                        progressMade = true;
                    }

                    layer.setState(BalloonLayerData.State.COMPLETE);
                }
            }
        }
    }

    /**
     * Removes all the blocks in a queue that can be horizontally reached by a certain block without travelling through airtight blocks
     */
    private static void removeReachable(final LevelAccelerator accelerator, final LongLinkedOpenHashSet queue, final BlockPos floodfillOrigin) {
        final LongLinkedOpenHashSet visited = new LongLinkedOpenHashSet();
        final LongLinkedOpenHashSet frontier = new LongLinkedOpenHashSet();

        final long startLong = floodfillOrigin.asLong();
        frontier.add(startLong);
        visited.add(startLong);
        queue.remove(startLong);

        final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos adjacentPos = new BlockPos.MutableBlockPos();

        while (!frontier.isEmpty()) {
            final long current = frontier.removeFirstLong();
            currentPos.set(current);

            for (final Direction dir : HORIZONTAL_DIRECTIONS) {
                adjacentPos.setWithOffset(currentPos, dir);
                final long neighborLong = adjacentPos.asLong();

                if (!queue.contains(neighborLong) || visited.contains(neighborLong)) {
                    continue;
                }

                final BlockState neighborState = accelerator.getBlockState(adjacentPos);

                if (!neighborState.isAir() && neighborState.is(AeroTags.BlockTags.AIRTIGHT)) {
                    continue;
                }

                queue.remove(neighborLong);
                frontier.add(neighborLong);
                visited.add(neighborLong);
            }
        }
    }

    /**
     * Does an upwards-biased flood-fill
     * - horizontally flood-fills to all available non-airtight space
     * - at every block, before we continue the flood-fill, we start another flood-fill at the block above if it is a
     * - candidate position (not in any other layer + not airtight)
     * - if that flood-fill is not safe, return non-safe
     */
    public static boolean upwardsBiasedFloodFill(final LevelAccelerator accelerator,
                                                 final BlockPos startPos,
                                                 final BalloonLayerGraph outputGraph,
                                                 final BalloonLayerGraph existingGraph,
                                                 final BalloonLayerGraph mainGraph) {
        if (accelerator.isOutsideBuildHeight(startPos))
            return false;

        final long startPosLong = startPos.asLong();

        if (!BalloonBuilder.isCandidatePosition(accelerator, startPos, outputGraph, existingGraph, mainGraph))
            return false;

        final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        final LongSet visited = new LongOpenHashSet();
        queue.enqueue(startPosLong);
        visited.add(startPosLong);

        final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
        final BalloonLayerData newLayer = new BalloonLayerData(startPos.getY());
        final int yLevel = startPos.getY();

        while (!queue.isEmpty()) {
            final long currentPosLong = queue.dequeueLastLong();
            currentPos.set(currentPosLong);
            final BlockState state = accelerator.getBlockState(currentPos);

            if (BalloonBuilder.isCandidatePosition(currentPos, state, outputGraph, existingGraph, mainGraph)) {
                newLayer.hotAirCount++;
                newLayer.addHotAirBlock(currentPos.getX(), currentPos.getZ());

                if (isSolid(state)) {
                    newLayer.solidCount++;
                    newLayer.addSolidBlock(currentPos.getX(), currentPos.getZ());
                }
            } else {
                continue;
            }

            final BlockPos posAbove = currentPos.above();
            final long posAboveLong = currentPosLong + 1;

            if (!visited.contains(posAboveLong) && BalloonBuilder.isCandidatePosition(accelerator, posAbove, outputGraph, existingGraph, mainGraph)) {
                if (!BalloonBuilder.upwardsBiasedFloodFill(accelerator, posAbove, outputGraph, existingGraph, mainGraph)) {
                    return false;
                }
            }

            for (final Direction direction : HORIZONTAL_DIRECTIONS) {
                final BlockPos neighborPos = currentPos.relative(direction);
                final long neighborPosLong = neighborPos.asLong();

                if (!visited.contains(neighborPosLong)) {
                    visited.add(neighborPosLong);
                    queue.enqueue(neighborPosLong);
                }
            }
        }

        outputGraph.addLayer(yLevel, newLayer);

//        if (fromLayer != null) {
//            fromLayer.outwardConnections.add(newLayer);
//            newLayer.inwardConnections.add(fromLayer);
//        }

        return true;
    }

    /**
     * Attempts to build a balloon starting from a heater (hot air source)
     */
    public static Balloon attemptBuildBalloon(final BlockEntityLiftingGasProvider heater, final BlockPos startPos) {
        final Level level = heater.getProviderLevel();
        final LevelAccelerator accelerator = new LevelAccelerator(level);

        final ObjectArrayList<BlockEntityLiftingGasProvider> heaters = new ObjectArrayList<>();
        heaters.add(heater);

        final BalloonLayerGraph graph = buildBalloon(level, startPos, null);

        if (graph == null) {
            return null;
        }

        if (level instanceof ServerLevel) {
            return new ServerBalloon(level, accelerator, startPos, graph, heaters);
        } else {
            return new ClientBalloon(level, accelerator, startPos, graph, heaters);
        }
    }

    public static boolean isSolid(final BlockState state) {
        return !state.isAir();
    }
}
