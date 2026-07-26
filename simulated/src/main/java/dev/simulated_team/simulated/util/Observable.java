package dev.simulated_team.simulated.util;

import dev.simulated_team.simulated.network.packets.BlockEntityObservedPacket;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public interface Observable {

    /**
     * Called server-side when the client says it looked at this block entity.
     *
     * @param player The player that observed this block entity
     */
    default void onObserved(final Player player) {
    }

    /**
     * Called client-side to notify the server that the player has observed this block entity.
     *
     * @param pos The position this block entity was observed at
     */
    default void sendObserved(final BlockPos pos) {
        VeilPacketManager.server().sendPacket(new BlockEntityObservedPacket(pos));
    }
}
