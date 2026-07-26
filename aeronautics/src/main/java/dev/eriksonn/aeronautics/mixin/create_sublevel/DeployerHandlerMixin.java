package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.zurrtum.create.content.kinetics.deployer.DeployerHandler;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import dev.ryanhcode.sable.api.SubLevelEntityScope;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(value = DeployerHandler.class, remap = false)
public abstract class DeployerHandlerMixin {

    @Unique
    private static final ThreadLocal<SubLevelEntityScope> aeronautics$entityScope = new ThreadLocal<>();

    @WrapMethod(method = "activateInner")
    private static void aeronautics$interactInDeployerCoordinates(
            final DeployerPlayer player, final Vec3 rayOrigin, final BlockPos clickedPos,
            final Vec3 extensionVector, final Mode mode, final Operation<Void> original
    ) {
        final ServerLevel level = player.cast().level();
        try (final SubLevelEntityScope scope = SubLevelEntityScope.at(level, clickedPos)) {
            aeronautics$entityScope.set(scope);
            original.call(player, rayOrigin, clickedPos, extensionVector, mode);
        } finally {
            aeronautics$entityScope.remove();
        }
    }

    @WrapOperation(
            method = "activateInner",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
                    remap = true
            )
    )
    private static <T extends Entity> List<T> aeronautics$projectFoundEntities(
            final ServerLevel instance, final Class<T> entityClass, final AABB bounds,
            final Operation<List<T>> original
    ) {
        final List<T> entities = original.call(instance, entityClass, bounds);
        final SubLevelEntityScope scope = aeronautics$entityScope.get();
        if (scope != null) {
            scope.includeAll(entities);
        }
        return entities;
    }
}
