package dev.ryanhcode.sable.sublevel.system.ticket;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * A ticket for a chunk force-loaded by a sub-level in the {@link PhysicsChunkTicketManager}.
 *
 * <p>mc26.1 port: vanilla {@code Ticket}s no longer carry an identity payload,
 * so the single vanilla ticket per chunk is tracked by the manager itself and
 * this class only records which sub-level inhabits the chunk and when.
 */
@ApiStatus.Internal
public final class InhabitedChunkTicket {
    private final UUID uuid;
    private long lastInhabitedTick;

    /**
     * @param uuid              the sub-level inhabiting the chunk
     * @param lastInhabitedTick the last tick ({@link Level#getGameTime()}) the chunk was inhabited
     */
    public InhabitedChunkTicket(final UUID uuid, final long lastInhabitedTick) {
        this.uuid = uuid;
        this.lastInhabitedTick = lastInhabitedTick;
    }

    public long lastInhabitedTick() {
        return this.lastInhabitedTick;
    }

    public void setLastInhabitedTick(final long lastInhabitedTick) {
        this.lastInhabitedTick = lastInhabitedTick;
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final InhabitedChunkTicket other) {
            return this.uuid.equals(other.uuid);
        }

        return false;
    }
}
