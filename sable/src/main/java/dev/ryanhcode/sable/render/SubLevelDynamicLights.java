package dev.ryanhcode.sable.render;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.joml.Vector3d;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Projects light-emitting blocks in moving sub-levels into their parent world's
 * light engine. On the server these are authoritative block-light sources, so
 * vanilla light packets and gameplay light checks use the propagated result.
 */
public final class SubLevelDynamicLights {
    private static final Map<LevelLightEngine, DynamicLightState> LIGHTS_BY_ENGINE =
            new IdentityHashMap<>();
    private static final DynamicLightState EMPTY_STATE = new DynamicLightState(
            new Long2IntOpenHashMap(),
            new Long2ObjectOpenHashMap<>(),
            new LongOpenHashSet(),
            new IdentityHashMap<>()
    );
    private static final ThreadLocal<ArrayDeque<DynamicLightState>> ACTIVE_STATES =
            ThreadLocal.withInitial(ArrayDeque::new);

    private SubLevelDynamicLights() {
    }

    public static void tick(final Level level) {
        final LevelLightEngine lightEngine = level.getLightEngine();
        final DynamicLightState nextState = collectState(level);
        final DynamicLightState previousState;

        synchronized (LIGHTS_BY_ENGINE) {
            previousState = LIGHTS_BY_ENGINE.put(lightEngine, nextState);
        }

        // A source can occupy an otherwise empty parent-world section. Keep
        // those sections active so the vanilla light engine will process it.
        for (final long packedSection : nextState.sections) {
            lightEngine.updateSectionStatus(SectionPos.of(packedSection), false);
        }

        if (previousState != null) {
            for (final long packedSection : previousState.sections) {
                if (!nextState.sections.contains(packedSection)) {
                    restoreSectionStatus(level, lightEngine, packedSection);
                }
            }
        }

        final LongOpenHashSet positionsToCheck = new LongOpenHashSet(nextState.lights.keySet());
        if (previousState != null) {
            positionsToCheck.addAll(previousState.lights.keySet());
        }

        for (final Long2ObjectMap.Entry<BlockState> entry : nextState.blocks.long2ObjectEntrySet()) {
            if (previousState == null || previousState.blocks.get(entry.getLongKey()) != entry.getValue()) {
                positionsToCheck.add(entry.getLongKey());
            }
        }
        if (previousState != null) {
            for (final long packedPos : previousState.blocks.keySet()) {
                if (!nextState.blocks.containsKey(packedPos)) {
                    positionsToCheck.add(packedPos);
                }
            }
        }

        if (positionsToCheck.isEmpty()) {
            return;
        }

        for (final long packedPos : positionsToCheck) {
            lightEngine.checkBlock(BlockPos.of(packedPos));
        }

        // ServerLevel uses ThreadedLevelLightEngine. Its check/update methods
        // enqueue work and its runner is invoked automatically on the chunk
        // lighting executor; calling runLightUpdates here deliberately throws.
        if (level.isClientSide()) {
            lightEngine.runLightUpdates();
        }
    }

    public static void clear(final Level level) {
        synchronized (LIGHTS_BY_ENGINE) {
            LIGHTS_BY_ENGINE.remove(level.getLightEngine());
        }
    }

    public static void beginLightUpdates(final LevelLightEngine engine) {
        final DynamicLightState state;
        synchronized (LIGHTS_BY_ENGINE) {
            state = LIGHTS_BY_ENGINE.get(engine);
        }
        ACTIVE_STATES.get().push(state == null ? EMPTY_STATE : state);
    }

    public static void endLightUpdates(final LevelLightEngine engine) {
        final ArrayDeque<DynamicLightState> activeStates = ACTIVE_STATES.get();
        if (!activeStates.isEmpty()) {
            activeStates.pop();
        }
        if (activeStates.isEmpty()) {
            ACTIVE_STATES.remove();
        }
    }

    public static int getLightEmission(final long packedPos) {
        final ArrayDeque<DynamicLightState> activeStates = ACTIVE_STATES.get();
        return activeStates.isEmpty() ? 0 : activeStates.peek().lights.get(packedPos);
    }

    public static BlockState getOccludingBlockState(final long packedPos) {
        final ArrayDeque<DynamicLightState> activeStates = ACTIVE_STATES.get();
        return activeStates.isEmpty() ? null : activeStates.peek().blocks.get(packedPos);
    }

    /**
     * Returns an upper-bound mask for light projected by one sub-level.
     *
     * <p>The parent light engine stores only the maximum contribution, so a
     * renderer cannot otherwise distinguish a stable world light from its own
     * voxel-snapped moving source. The mask lets sub-level geometry keep using
     * its plot-space light for its own sources while still accepting stronger
     * parent-world lights.</p>
     */
    public static OwnLightMask getOwnLightMask(
            final LevelLightEngine engine,
            final SubLevel subLevel
    ) {
        synchronized (LIGHTS_BY_ENGINE) {
            final DynamicLightState state = LIGHTS_BY_ENGINE.get(engine);
            if (state == null) {
                return OwnLightMask.EMPTY;
            }
            return state.ownLights.getOrDefault(subLevel, OwnLightMask.EMPTY);
        }
    }

    private static DynamicLightState collectState(final Level level) {
        final Long2IntOpenHashMap lights = new Long2IntOpenHashMap();
        final Long2ObjectOpenHashMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
        final LongOpenHashSet sections = new LongOpenHashSet();
        final Map<SubLevel, OwnLightMask> ownLights = new IdentityHashMap<>();
        lights.defaultReturnValue(0);

        final SubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) {
            return new DynamicLightState(lights, blocks, sections, ownLights);
        }

        for (final SubLevel subLevel : container.getAllSubLevels()) {
            final Pose3dc pose = subLevel.logicalPose();
            final List<LocalLightSource> localSources = new ArrayList<>();
            subLevel.getPlot().getLoadedChunks().forEach(holder ->
                    holder.getChunk().findBlockLightSources((sourcePos, state) -> {
                        final int emission = state.getLightEmission();
                        if (emission > 0) {
                            localSources.add(new LocalLightSource(sourcePos.immutable(), emission));
                        }
                    })
            );
            if (localSources.isEmpty()) {
                continue;
            }

            final Vector3d physicalCenter = new Vector3d();
            final BlockPos.MutableBlockPos physicalPos = new BlockPos.MutableBlockPos();
            final BlockPos.MutableBlockPos localPos = new BlockPos.MutableBlockPos();
            final LongOpenHashSet occlusionCandidates = new LongOpenHashSet();
            final List<ProjectedLightSource> projectedSources = new ArrayList<>();

            for (final LocalLightSource source : localSources) {
                if (!projectPosition(level, pose, source.pos, physicalCenter, physicalPos)) {
                    continue;
                }

                projectedSources.add(new ProjectedLightSource(
                        physicalPos.getX(),
                        physicalPos.getY(),
                        physicalPos.getZ(),
                        source.emission
                ));
                final long packedSourcePos = physicalPos.asLong();
                if (source.emission > lights.get(packedSourcePos)) {
                    lights.put(packedSourcePos, source.emission);
                }
                sections.add(SectionPos.blockToSection(packedSourcePos));

                final int radius = Math.min(15, source.emission);
                final int radiusSquared = radius * radius;
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            if (x * x + y * y + z * z > radiusSquared) {
                                continue;
                            }

                            final int candidateY = source.pos.getY() + y;
                            if (level.isOutsideBuildHeight(candidateY)) {
                                continue;
                            }
                            occlusionCandidates.add(BlockPos.asLong(
                                    source.pos.getX() + x,
                                    candidateY,
                                    source.pos.getZ() + z
                            ));
                        }
                    }
                }
            }

            for (final long packedLocalPos : occlusionCandidates) {
                localPos.set(packedLocalPos);
                final BlockState blockState = level.getBlockState(localPos);
                if (blockState.isAir()
                        || blockState.getLightBlock() <= 0 && !blockState.useShapeForLightOcclusion()) {
                    continue;
                }
                if (!projectPosition(level, pose, localPos, physicalCenter, physicalPos)) {
                    continue;
                }

                final long packedPhysicalPos = physicalPos.asLong();
                final BlockState existingState = blocks.get(packedPhysicalPos);
                if (existingState == null
                        || blockState.getLightBlock() > existingState.getLightBlock()
                        || blockState.getLightBlock() == existingState.getLightBlock()
                        && blockState.useShapeForLightOcclusion()
                        && !existingState.useShapeForLightOcclusion()) {
                    blocks.put(packedPhysicalPos, blockState);
                }
                sections.add(SectionPos.blockToSection(packedPhysicalPos));
            }

            if (!projectedSources.isEmpty()) {
                ownLights.put(subLevel, new OwnLightMask(List.copyOf(projectedSources)));
            }
        }

        return new DynamicLightState(lights, blocks, sections, ownLights);
    }

    private static boolean projectPosition(
            final Level level,
            final Pose3dc pose,
            final BlockPos localPos,
            final Vector3d physicalCenter,
            final BlockPos.MutableBlockPos physicalPos
    ) {
        pose.transformPosition(
                physicalCenter.set(
                        localPos.getX() + 0.5,
                        localPos.getY() + 0.5,
                        localPos.getZ() + 0.5
                ),
                physicalCenter
        );
        physicalPos.set(physicalCenter.x, physicalCenter.y, physicalCenter.z);
        return !level.isOutsideBuildHeight(physicalPos) && level.hasChunkAt(physicalPos);
    }

    private static void restoreSectionStatus(
            final Level level,
            final LevelLightEngine lightEngine,
            final long packedSection
    ) {
        final SectionPos sectionPos = SectionPos.of(packedSection);
        if (sectionPos.y() < level.getMinSectionY() || sectionPos.y() >= level.getMaxSectionY()) {
            return;
        }

        final BlockPos origin = sectionPos.origin();
        if (!level.hasChunkAt(origin)) {
            return;
        }

        final LevelChunk chunk = level.getChunkAt(origin);
        final int sectionIndex = level.getSectionIndexFromSectionY(sectionPos.y());
        lightEngine.updateSectionStatus(sectionPos, chunk.getSection(sectionIndex).hasOnlyAir());
    }

    private record DynamicLightState(
            Long2IntOpenHashMap lights,
            Long2ObjectOpenHashMap<BlockState> blocks,
            LongOpenHashSet sections,
            Map<SubLevel, OwnLightMask> ownLights
    ) {
    }

    public static final class OwnLightMask {
        private static final OwnLightMask EMPTY = new OwnLightMask(List.of());

        private final List<ProjectedLightSource> sources;

        private OwnLightMask(final List<ProjectedLightSource> sources) {
            this.sources = sources;
        }

        public int upperBound(final long packedPos) {
            final int x = BlockPos.getX(packedPos);
            final int y = BlockPos.getY(packedPos);
            final int z = BlockPos.getZ(packedPos);
            int upperBound = 0;

            for (final ProjectedLightSource source : this.sources) {
                final int distance = Math.abs(x - source.x)
                        + Math.abs(y - source.y)
                        + Math.abs(z - source.z);
                upperBound = Math.max(upperBound, source.emission - distance);
            }

            return upperBound;
        }
    }

    private record LocalLightSource(BlockPos pos, int emission) {
    }

    private record ProjectedLightSource(int x, int y, int z, int emission) {
    }
}
