package foundry.veil.api.network.handler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public interface ServerPacketContext extends PacketContext {
    default MinecraftServer server() {
        return this.player().level().getServer();
    }

    @Override
    ServerPlayer player();

    @Override
    default Level level() {
        return this.player().level();
    }
}
