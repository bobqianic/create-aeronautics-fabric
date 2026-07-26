package dev.ryanhcode.sable.mixin.tracking_points;

import dev.ryanhcode.sable.sublevel.tracking_points.SubLevelTrackingPointSavedData;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Shadow public abstract ServerLevel level();

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveData(final ValueOutput output, final CallbackInfo ci) {
        final SubLevelTrackingPointSavedData data = SubLevelTrackingPointSavedData.getOrLoad(this.level());
        final UUID loginPointUUID = data.generateTrackingPoint((ServerPlayer) (Object) this);
        if (loginPointUUID != null) {
            output.store("LoginPoint", net.minecraft.core.UUIDUtil.CODEC, loginPointUUID);
        }
    }

}
