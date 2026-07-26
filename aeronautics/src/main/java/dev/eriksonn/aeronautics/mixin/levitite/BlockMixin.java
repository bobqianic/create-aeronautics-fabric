package dev.eriksonn.aeronautics.mixin.levitite;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.eriksonn.aeronautics.index.AeroTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public class BlockMixin {

    @WrapOperation(method = "shouldRenderFace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/shapes/Shapes;joinIsNotEmpty(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/BooleanOp;)Z"))
    private static boolean shouldRenderFace(final VoxelShape shape1, final VoxelShape shape2, final BooleanOp ops, final Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) final BlockState blockstate1, @Local(ordinal = 1) final BlockState blockstate2) {
        if (original.call(shape1, shape2, ops))
            return true;

        final boolean l1 = blockstate1.is(AeroTags.BlockTags.LEVITITE);
        final boolean l2 = blockstate2.is(AeroTags.BlockTags.LEVITITE);

        return l1 ^ l2;
    }
}
