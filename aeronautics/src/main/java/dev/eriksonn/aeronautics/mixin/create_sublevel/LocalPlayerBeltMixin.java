package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import dev.eriksonn.aeronautics.compat.create.SubLevelBeltPlayerHandler;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Predicts sub-level belt movement from the local player's ticker. A dedicated
 * multiplayer client is not guaranteed to tick a remote sub-level block entity
 * before it sends its player movement, while an integrated client usually does.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerBeltMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void aeronautics$transportOnTrackedSubLevelBelt(final CallbackInfo ci) {
        final LocalPlayer player = (LocalPlayer) (Object) this;
        final SubLevel subLevel = Sable.HELPER.getTrackingSubLevel(player);
        if (subLevel == null || subLevel.isRemoved()) {
            return;
        }

        final SubLevelBeltPlayerHandler.BeltContact contact =
                SubLevelBeltPlayerHandler.findContact(player, subLevel, null);
        if (contact == null) {
            return;
        }

        BeltMovementHandler.transportEntity(
                contact.controller(),
                player,
                new TransportedEntityInfo(contact.pos(), contact.state())
        );
    }
}
