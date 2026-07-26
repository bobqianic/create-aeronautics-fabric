package dev.simulated_team.simulated.mixin.extra_kinetics;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.Function;

@Mixin(value = SmartBlockEntity.class, remap = false)
public abstract class SmartBlockEntityMixin extends BlockEntity {
    protected SmartBlockEntityMixin(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @WrapOperation(
            method = "initialize",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/zurrtum/create/api/registry/SimpleRegistry$Multi;get(Ljava/lang/Object;)Ljava/util/List;",
                    ordinal = 1
            )
    )
    private List<Function<SmartBlockEntity, BlockEntityBehaviour<?>>> simulated$skipClientBehavioursForExtraKinetics(
            final SimpleRegistry.Multi<BlockEntityType<?>, Function<SmartBlockEntity, BlockEntityBehaviour<?>>> registry,
            final Object type,
            final Operation<List<Function<SmartBlockEntity, BlockEntityBehaviour<?>>>> original) {
        if (this.worldPosition instanceof ExtraBlockPos) {
            return List.of();
        }
        return original.call(registry, type);
    }
}
