package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import dev.eriksonn.aeronautics.compat.create.SubLevelBeltPlayerHandler;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(value = BeltBlockEntity.class, remap = false)
public abstract class BeltBlockEntityMixin {

    /**
     * Vanilla discovers belt passengers through Block.stepOn/entityInside. A player
     * standing on a sub-level belt has air at the corresponding global block
     * position, so that callback is not guaranteed to reach Create. Refresh player
     * passengers from their physical position before Create advances the belt.
     *
     * <p>The server advances its authoritative players here. The local client player
     * is advanced from {@link LocalPlayerBeltMixin}, independently of whether the
     * remote sub-level's block entity ticker is active on that client.</p>
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void aeronautics$registerPhysicalPlayers(final CallbackInfo ci) {
        final BeltBlockEntity controller = (BeltBlockEntity) (Object) this;
        final Level level = controller.getLevel();
        if (level == null
                || !controller.isController()
                || Math.abs(controller.getSpeed()) < 1.0F) {
            return;
        }

        final SubLevel subLevel = Sable.HELPER.getContaining(level, controller.getBlockPos());
        if (subLevel == null) {
            return;
        }

        if (level.isClientSide()) {
            // Vanilla collision callbacks can still add the local player to this
            // map occasionally. Remove it so the dedicated local-player fallback
            // remains the one client-side transport path and cannot move twice.
            if (controller.passengers != null) {
                controller.passengers.keySet().removeIf(
                        entity -> entity instanceof Player player && player.isLocalPlayer()
                );
            }
            return;
        }

        for (final Player player : level.players()) {
            final SubLevelBeltPlayerHandler.BeltContact contact =
                    SubLevelBeltPlayerHandler.findContact(player, subLevel, controller);
            if (contact == null) {
                continue;
            }

            if (controller.passengers == null) {
                controller.passengers = new HashMap<>();
            }

            final TransportedEntityInfo info = controller.passengers.get(player);
            if (info == null) {
                controller.passengers.put(
                        player,
                        new TransportedEntityInfo(contact.pos(), contact.state())
                );
                player.setOnGround(true);
            } else {
                info.refresh(contact.pos(), contact.state());
            }
        }
    }
}
