package dev.ryanhcode.sable.fabric.network;

import dev.ryanhcode.sable.network.tcp.SablePacketContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record FabricSablePacketContext(Player player) implements SablePacketContext {

    @Override
    public Level level() {
        return this.player.level();
    }
}
