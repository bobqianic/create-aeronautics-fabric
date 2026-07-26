package dev.ryanhcode.sable.mixin.player_freezing;

import com.mojang.authlib.GameProfile;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.player_freezing.PlayerFreezeExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player implements PlayerFreezeExtension {

    public LocalPlayerMixin(final Level level, final GameProfile gameProfile) {
        super(level, gameProfile);
    }

    // PORT-NOTE(mc26.1): LocalPlayer.tick no longer gates on Level.hasChunkAt — the tick gate is now
    // ClientPacketListener.hasClientLoaded(); the freeze hook redirects that check instead.
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;hasClientLoaded()Z"))
    private boolean sable$freezeTicking(final ClientPacketListener instance) {
        this.sable$tickStopFreezing();

        final UUID uuid = this.sable$getFrozenToSubLevel();

        if (uuid != null) {
            final SubLevelContainer container = SubLevelContainer.getContainer(this.level());
            assert container != null;
            final ClientSubLevel subLevel = (ClientSubLevel) container.getSubLevel(uuid);

            if (subLevel == null || !subLevel.isFinalized()) {
                return false;
            }

            this.sable$teleport();
            this.sable$freezeTo(null, null);
        }

        return instance.hasClientLoaded();
    }
}
