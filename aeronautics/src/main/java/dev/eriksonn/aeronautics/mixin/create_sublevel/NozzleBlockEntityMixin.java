package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.kinetics.fan.NozzleBlockEntity;
import dev.ryanhcode.sable.api.SubLevelEntityScope;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = NozzleBlockEntity.class, remap = false)
public abstract class NozzleBlockEntityMixin {

    @Shadow
    @Final
    private List<Entity> pushingEntities;

    @Unique
    @Nullable
    private SubLevelEntityScope aeronautics$entityScope;

    @WrapMethod(method = "tick")
    private void aeronautics$useLocalCoordinatesWhilePushing(final Operation<Void> original) {
        final NozzleBlockEntity self = (NozzleBlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level == null) {
            original.call();
            return;
        }

        try (final SubLevelEntityScope scope = SubLevelEntityScope.at(level, self.getBlockPos())) {
            scope.includeAll(this.pushingEntities);
            original.call();
        }
    }

    @WrapMethod(method = "lazyTick")
    private void aeronautics$useLocalCoordinatesWhileFindingEntities(final Operation<Void> original) {
        final NozzleBlockEntity self = (NozzleBlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level == null) {
            original.call();
            return;
        }

        try (final SubLevelEntityScope scope = SubLevelEntityScope.at(level, self.getBlockPos())) {
            this.aeronautics$entityScope = scope;
            scope.includeAll(this.pushingEntities);
            original.call();
        } finally {
            this.aeronautics$entityScope = null;
        }
    }

    @WrapOperation(
            method = "lazyTick",
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
