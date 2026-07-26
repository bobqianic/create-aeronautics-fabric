package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler;
import com.zurrtum.create.content.kinetics.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BeltMovementHandler.class, remap = false)
public abstract class BeltMovementHandlerMixin {

    @Unique
    private static final ThreadLocal<BeltMovementContext> aeronautics$movementContext = new ThreadLocal<>();

    @WrapMethod(method = "transportEntity")
    private static void aeronautics$transportInBeltCoordinates(
            final BeltBlockEntity belt, final Entity entity, final TransportedEntityInfo info,
            final Operation<Void> original
    ) {
        final Level level = belt.getLevel();
        final SubLevel subLevel = level == null ? null : Sable.HELPER.getContaining(level, belt.getBlockPos());
        if (subLevel == null) {
            original.call(belt, entity, info);
            return;
        }

        final boolean projected = SubLevelHelper.pushEntityLocalIfNeeded(subLevel, entity);
        final BeltMovementContext previous = aeronautics$movementContext.get();
        aeronautics$movementContext.set(new BeltMovementContext(
                subLevel,
                entity,
                projected,
                belt.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis(),
                ((TransportedEntityInfoAccessor) info).aeronautics$getLastCollidedPos()
        ));
        try {
            original.call(belt, entity, info);
        } finally {
            if (previous == null) {
                aeronautics$movementContext.remove();
            } else {
                aeronautics$movementContext.set(previous);
            }
            SubLevelHelper.popEntityLocalIfNeeded(subLevel, entity, projected);
        }
    }

    /**
     * Create uses {@code blockPos + .5f} for this center. Sable plot positions
     * are large enough that float precision is two blocks, so that expression
     * can point at an edge or even the neighbouring block. Offset the entity
     * coordinate seen by Create so its existing subtraction yields the exact
     * double-precision distance from the belt center.
     */
    @ModifyExpressionValue(
            method = "transportEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D",
                    ordinal = 0,
                    remap = true
            )
    )
    private static double aeronautics$usePreciseXCenter(final double original) {
        final BeltMovementContext context = aeronautics$movementContext.get();
        if (context == null) {
            return original;
        }
        return aeronautics$offsetForPreciseCenter(original, context.collidedPos().getX());
    }

    @ModifyExpressionValue(
            method = "transportEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getZ()D",
                    ordinal = 0,
                    remap = true
            )
    )
    private static double aeronautics$usePreciseZCenter(final double original) {
        final BeltMovementContext context = aeronautics$movementContext.get();
        if (context == null) {
            return original;
        }
        return aeronautics$offsetForPreciseCenter(original, context.collidedPos().getZ());
    }

    @Unique
    private static double aeronautics$offsetForPreciseCenter(
            final double entityCoordinate,
            final int blockCoordinate
    ) {
        final double roundedFloatCenter = (float) blockCoordinate + 0.5F;
        final double preciseCenter = blockCoordinate + 0.5D;
        return entityCoordinate + roundedFloatCenter - preciseCenter;
    }

    /**
     * Belt calculations need the passenger in plot space, but Entity.move must still run
     * in physical world space so Sable's collision pipeline does not process a temporarily
     * projected entity as though it were a permanently plot-local entity.
     */
    @WrapOperation(
            method = "transportEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                    remap = true
            )
    )
    private static void aeronautics$movePassengerInWorldCoordinates(
            final Entity entity,
            final MoverType moverType,
            final Vec3 localMovement,
            final Operation<Void> original
    ) {
        final BeltMovementContext context = aeronautics$movementContext.get();
        if (context == null || context.entity() != entity || !context.projected()) {
            original.call(entity, moverType, localMovement);
            return;
        }

        SubLevelHelper.popEntityLocal(context.subLevel(), entity);
        try {
            final Vec3 adjustedMovement;
            if (entity instanceof Player) {
                // Preserve where a player stepped onto a sub-level belt instead
                // of applying Create's normal cross-belt centering correction.
                adjustedMovement = context.axis() == Direction.Axis.X
                        ? new Vec3(localMovement.x, localMovement.y, 0.0)
                        : new Vec3(0.0, localMovement.y, localMovement.z);
            } else {
                adjustedMovement = localMovement;
            }
            original.call(
                    entity,
                    moverType,
                    context.subLevel().logicalPose().transformNormal(adjustedMovement)
            );
        } finally {
            SubLevelHelper.pushEntityLocal(context.subLevel(), entity);
        }
    }

    @Unique
    private record BeltMovementContext(
            SubLevel subLevel,
            Entity entity,
            boolean projected,
            Direction.Axis axis,
            BlockPos collidedPos
    ) {
    }
}
