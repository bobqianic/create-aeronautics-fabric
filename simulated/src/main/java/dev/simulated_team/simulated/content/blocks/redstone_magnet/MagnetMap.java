package dev.simulated_team.simulated.content.blocks.redstone_magnet;


import dev.simulated_team.simulated.util.SimMovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MagnetMap<T extends BlockEntity & SimMagnet> {
    public final Map<LevelAccessor, Map<SectionPos, HashSet<BlockPos>>> magnetMap = new WeakHashMap<>();
    public final Map<LevelAccessor, Map<MagnetPairIdentifier, MagnetPair<T>>> pairMap = new WeakHashMap<>();

    public void addMagnet(final LevelAccessor level, final SectionPos sectionPos, final BlockPos pos) {
        this.magnetMap.putIfAbsent(level, new HashMap<>());
        final Map<SectionPos, HashSet<BlockPos>> levelMap = this.magnetMap.get(level);

        levelMap.putIfAbsent(sectionPos, new HashSet<>());
        final HashSet<BlockPos> posSet = levelMap.get(sectionPos);

        posSet.add(pos);
    }


    public void removeMagnet(final LevelAccessor level, final SectionPos sectionPos, final BlockPos pos) {
        final Map<SectionPos, HashSet<BlockPos>> levelMap = this.magnetMap.get(level);
        if (levelMap == null) {
            return;
        }
        final HashSet<BlockPos> posSet = levelMap.get(sectionPos);
        if (posSet == null) {
            return;
        }
        posSet.remove(pos);
        if (posSet.isEmpty()) {
            levelMap.remove(sectionPos);
            if (levelMap.isEmpty()) {
                this.magnetMap.remove(level);
            }
        }
    }

    public List<SimMovementContext> findNearby(final SimMovementContext context) {
        final Map<SectionPos, HashSet<BlockPos>> sectionMap = this.magnetMap.get(context.level());
        if (sectionMap == null) {
            return List.of();
        }

        final int minX = Math.floorDiv((int) context.globalPosition().x - 8, 16);
        final int minY = Math.floorDiv((int) context.globalPosition().y - 8, 16);
        final int minZ = Math.floorDiv((int) context.globalPosition().z - 8, 16);
        final List<SimMovementContext> contexts = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    final SectionPos section = SectionPos.of(minX + i, minY + j, minZ + k);
                    final HashSet<BlockPos> posSet = sectionMap.get(section);
                    if (posSet == null) {
                        continue;
                    }
                    for (final BlockPos blockPos : posSet) {
                        if (blockPos.equals(context.localBlockPos())) {
                            continue;
                        }
                        final SimMovementContext otherContext = SimMovementContext.getMovementContext(context.level(), Vec3.atCenterOf(blockPos));
                        contexts.add(otherContext);
                    }
                }
            }
        }

        return contexts;
    }

    /**
     * Attempts to add a new pair to the map.
     *
     * @param level    The level to add the pair to
     * @param pos1     The first position to pair to
     * @param pos2     The second position to pair to
     * @param consumer The factory for a new pair
     * @return The old pair that was replaced or <code>null</code> if the pair is new
     */
    public @Nullable MagnetPair<T> tryAddPair(final Level level, final BlockPos pos1, final BlockPos pos2, final MagnetConsumer<T> consumer) {
        this.pairMap.putIfAbsent(level, new HashMap<>());
        final Map<MagnetPairIdentifier, MagnetPair<T>> levelMap = this.pairMap.get(level);

        final MagnetPairIdentifier id = new MagnetPairIdentifier(pos1, pos2);

        final MagnetPair<T> currentPair = levelMap.get(id);
        if (currentPair == null) {
            levelMap.put(id, consumer.apply(level, pos1, pos2));
        } else {
            currentPair.alive = true;
        }
        return currentPair;
    }

    public @Nullable MagnetPair<T> getPair(final Level level, final BlockPos pos1, final BlockPos pos2) {
        final Map<MagnetPairIdentifier, MagnetPair<T>> levelMap = this.pairMap.get(level);
        if (levelMap == null) {
            return null;
        }

        return levelMap.get(new MagnetPairIdentifier(pos1, pos2));
    }

    public void tick(final Level level) {
        final Map<MagnetPairIdentifier, MagnetPair<T>> map = this.pairMap.get(level);
        if (map != null) {
            map.entrySet().removeIf(x -> !x.getValue().alive);
            for (final MagnetPair<?> pair : map.values()) {
                pair.tick();
            }
        }
    }

    public void physicsTick(final double substepTimeStep, final Level level) {
        final Map<MagnetPairIdentifier, MagnetPair<T>> pairs = this.pairMap.get(level);

        if (pairs != null) {
            for (final MagnetPair<T> pair : pairs.values()) {
                pair.physicsTick(substepTimeStep);
            }
        }
    }
}
