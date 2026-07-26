package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import dev.ryanhcode.sable.api.SubLevelEntityScope;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = ChuteBlockEntity.class, remap = false)
public abstract class ChuteBlockEntityMixin {

    @Unique
    @Nullable
    private SubLevelEntityScope aeronautics$entityScope;

    @WrapMethod(method = "findEntities")
    private void aeronautics$insertEntityInChuteCoordinates(final float itemSpeed, final Operation<Void> original) {
        final ChuteBlockEntity self = (ChuteBlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level == null) {
            original.call(itemSpeed);
            return;
        }

        try (final SubLevelEntityScope scope = SubLevelEntityScope.at(level, self.getBlockPos())) {
            this.aeronautics$entityScope = scope;
            original.call(itemSpeed);
        } finally {
            this.aeronautics$entityScope = null;
        }
    }

    @WrapOperation(
            method = "findEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
                    remap = true
            )
    )
    private <T extends Entity> List<T> aeronautics$projectFoundEntities(
            final Level instance, final Class<T> entityClass, final AABB bounds,
            final Operation<List<T>> original
    ) {
        final List<T> entities = original.call(instance, entityClass, bounds);
        if (this.aeronautics$entityScope != null) {
            this.aeronautics$entityScope.includeAll(entities);
        }
        return entities;
    }
}
