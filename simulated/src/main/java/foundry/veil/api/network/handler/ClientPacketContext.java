package foundry.veil.api.network.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;

public interface ClientPacketContext extends PacketContext {
    Minecraft client();

    @Override
    @Nullable
    LocalPlayer player();
}
