package dev.ryanhcode.sable.mixin.tracking_points;

import dev.ryanhcode.sable.mixinterface.player_freezing.PlayerFreezeExtension;
import dev.ryanhcode.sable.sublevel.tracking_points.SubLevelTrackingPointSavedData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private Level level;

    @Shadow public abstract void setPosRaw(double d, double e, double f);

    @Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setPosRaw(DDD)V", shift = At.Shift.AFTER))
    private void sable$load(final ValueInput input, final CallbackInfo ci) {
        // mc26.1: Entity#load takes a ValueInput now
        final java.util.Optional<java.util.UUID> loginPoint = input.read("LoginPoint", net.minecraft.core.UUIDUtil.CODEC);
        if (loginPoint.isPresent()) {
            final SubLevelTrackingPointSavedData data = SubLevelTrackingPointSavedData.getOrLoad((ServerLevel) this.level);
            final SubLevelTrackingPointSavedData.TakenLoginPoint point = data.take(loginPoint.get(), true);

            if (point != null) {
                final Vector3dc position = point.position();
                this.setPosRaw(position.x(), position.y(), position.z());

                if (point.subLevelId() != null && this instanceof final PlayerFreezeExtension extension) {
                    extension.sable$freezeTo(point.subLevelId(), point.localAnchor().add(0.0, 0.2, 0.0));
                }

                // PORT-NOTE(mc26.1): ValueInput is immutable — the old
                // remove("RootVehicle") vehicle-kick is no longer possible
                // here; the freeze extension re-anchors the player anyway.
            }
        }
    }
}
