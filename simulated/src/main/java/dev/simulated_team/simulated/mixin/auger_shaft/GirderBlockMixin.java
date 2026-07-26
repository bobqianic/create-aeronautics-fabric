package dev.simulated_team.simulated.mixin.auger_shaft;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.decoration.girder.GirderBlock;
import dev.simulated_team.simulated.content.blocks.auger_shaft.AugerShaftBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GirderBlock.class)
public class GirderBlockMixin {
    @Inject(method = "isConnected", at = @At("TAIL"), cancellable = true)
    private static void connectToAugers(final BlockAndTintGetter world, final BlockPos pos, final BlockState state, final Direction side,
                                        final CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) final BlockState otherState) {
        if (otherState.getBlock() instanceof AugerShaftBlock) {
            cir.setReturnValue(true);
        }
    }
}
