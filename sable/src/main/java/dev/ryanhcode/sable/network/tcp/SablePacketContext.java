package dev.ryanhcode.sable.network.tcp;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Loader-agnostic packet handling context.
 *
 * <p>Replaces Veil's {@code foundry.veil.api.network.handler.PacketContext}
 * for the Fabric port. Only the two accessors Sable's handlers actually use
 * are exposed; Fabric networking adapts its payload context to this interface.
 */
public interface SablePacketContext {

    /**
     * @return the level of the handling player (client level for clientbound
     * packets, the sending player's server level for serverbound packets)
     */
    Level level();

    /**
     * @return the handling player (the local client player for clientbound
     * packets, the sending server player for serverbound packets)
     */
    Player player();
}
