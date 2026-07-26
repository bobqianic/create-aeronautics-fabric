package dev.simulated_team.simulated.content.blocks.rope.strand.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import com.zurrtum.create.catnip.data.WorldAttached;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Contains and manages all ropes for a client level
 */
public class ClientLevelRopeManager {

    private static final WorldAttached<ClientLevelRopeManager> worldAttached = new WorldAttached<>(ClientLevelRopeManager::create);

    private final Level level;
    private final Map<UUID, ClientRopeStrand> ropeStrands = new Object2ObjectOpenHashMap<>();

    public ClientLevelRopeManager(final Level level) {
        this.level = level;
    }

    public static ClientLevelRopeManager getOrCreate(final Level level) {
        return worldAttached.get(level);
    }

    private static ClientLevelRopeManager create(final LevelAccessor level) {
        if (!(level instanceof final ClientLevel clientLevel)) return null;
        return new ClientLevelRopeManager(clientLevel);
    }

    public void addStrand(final ClientRopeStrand strand) {
        this.ropeStrands.put(strand.getUuid(), strand);
    }

    @Nullable
    public ClientRopeStrand getStrand(final UUID uuid) {
        return this.ropeStrands.get(uuid);
    }

    public void removeStrand(final UUID uuid) {
        this.ropeStrands.remove(uuid);
    }

    public Iterable<ClientRopeStrand> getAllStrands() {
        return this.ropeStrands.values();
    }

    public void tickInterpolation(final double interpolationTick) {
        for (final ClientRopeStrand strand : this.ropeStrands.values()) {
            strand.tickInterpolation(interpolationTick);
        }
    }
}
