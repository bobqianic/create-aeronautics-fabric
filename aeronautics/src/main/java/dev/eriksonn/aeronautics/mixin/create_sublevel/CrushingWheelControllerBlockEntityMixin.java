package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import dev.ryanhcode.sable.api.SubLevelEntityScope;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = CrushingWheelControllerBlockEntity.class, remap = false)
public abstract class CrushingWheelControllerBlockEntityMixin {

    @Shadow
    public Entity processingEntity;

    @Unique
    @Nullable
    private SubLevelEntityScope aeronautics$entityScope;

    @WrapMethod(method = "tick")
    private void aeronautics$processEntityInControllerCoordinates(final Operation<Void> original) {
        final CrushingWheelControllerBlockEntity self = (CrushingWheelControllerBlockEntity) (Object) this;
        final Level level = self.getLevel();
        if (level == null) {
            original.call();
            return;
        }

        try (final SubLevelEntityScope scope = SubLevelEntityScope.at(level, self.getBlockPos())) {
            this.aeronautics$entityScope = scope;
            if (this.processingEntity != null) {
                scope.include(this.processingEntity);
            }
            original.call();
        } finally {
            this.aeronautics$entityScope = null;
        }
    }

    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
                    remap = true
            )
    )
    private List<Entity> aeronautics$projectRecoveredEntity(
            final Level instance, final Entity except, final AABB bounds,
            final Predicate<? super Entity> predicate, final Operation<List<Entity>> original
    ) {
        final List<Entity> entities = original.call(instance, except, bounds, predicate);
        if (this.aeronautics$entityScope != null) {
            this.aeronautics$entityScope.includeAll(entities);
        }
        return entities;
    }
}
