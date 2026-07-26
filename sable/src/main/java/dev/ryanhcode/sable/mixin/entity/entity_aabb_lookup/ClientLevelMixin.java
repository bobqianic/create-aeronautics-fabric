package dev.ryanhcode.sable.mixin.entity.entity_aabb_lookup;

import dev.ryanhcode.sable.util.SubLevelInclusiveLevelEntityGetter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "getEntities()Lnet/minecraft/world/level/entity/LevelEntityGetter;", at = @At("RETURN"), cancellable = true)
    private void sable$postGetEntities(final CallbackInfoReturnable<LevelEntityGetter<Entity>> cir) {
        cir.setReturnValue(new SubLevelInclusiveLevelEntityGetter<>((Level) (Object) this, cir.getReturnValue()));
    }
}
