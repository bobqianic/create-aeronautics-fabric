package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EjectorItemEntity.class, remap = false)
public abstract class EjectorItemEntityMixin {

    @Inject(method = "placeItemAtTarget", at = @At("RETURN"))
    private void aeronautics$leaveSubLevelAfterTrajectory(
            final boolean isClient, final float maxTime, final CallbackInfo ci
    ) {
        final EjectorItemEntity entity = (EjectorItemEntity) (Object) this;
        if (!entity.isAlive() || entity.isRemoved()) {
            return;
        }

        final SubLevel subLevel = Sable.HELPER.getContaining(entity);
        if (subLevel != null) {
            EntitySubLevelUtil.kickEntity(subLevel, entity);
        }
    }
}
