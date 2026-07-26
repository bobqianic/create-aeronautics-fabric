package dev.simulated_team.simulated.content.blocks.rope.strand.server;

import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.zurrtum.create.catnip.data.WorldAttached;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Contains and manages all ropes for a client level
 */
public class ServerLevelRopeManager {
    private static final WorldAttached<ServerLevelRopeManager> worldAttached = new WorldAttached<>(ServerLevelRopeManager::create);

    private final Level level;
    private final Map<UUID, ServerRopeStrand> ropeStrands = new Object2ObjectOpenHashMap<>();

    public ServerLevelRopeManager(final Level level) {
        this.level = level;
    }

    @Nullable
    public static ServerLevelRopeManager getOrCreate(final Level level) {
        return worldAttached.get(level);
    }

    private static ServerLevelRopeManager create(final LevelAccessor level) {
        if (!(level instanceof final ServerLevel serverLevel)) return null;
        return new ServerLevelRopeManager(serverLevel);
    }

    public void addStrand(final ServerRopeStrand strand) {
        this.ropeStrands.put(strand.getUUID(), strand);
    }

    @Nullable
    public ServerRopeStrand getStrand(final UUID uuid) {
        return this.ropeStrands.get(uuid);
    }

    public void removeStrand(final UUID uuid) {
        this.ropeStrands.remove(uuid);
    }

    public Collection<ServerRopeStrand> getAllStrands() {
        return this.ropeStrands.values();
    }

    public void physicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        final ServerLevel level = physicsSystem.getLevel();

        for (final ServerRopeStrand strand : this.ropeStrands.values()) {
            if (!strand.isActive()) {
                continue;
            }

            if (!strand.isOwnerLoaded(level) || !strand.areAttachmentsLoaded(level)) {
                physicsSystem.removeObject(strand);
                continue;
            }

            strand.prePhysicsTick(physicsSystem, level, timeStep);
        }
    }
}
