package dev.simulated_team.simulated.mixin.quiet_use;

import dev.simulated_team.simulated.index.SimClickInteractions;
import dev.simulated_team.simulated.index.SimItems;
import dev.simulated_team.simulated.util.QuietUse;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    /**
     * Handle Honey Glue before Fabric's use-block event and before the target
     * block can consume the interaction. Mouse callbacks are not a reliable
     * source for this because the use key can be rebound or synthesized by a
     * launcher/controller.
     */
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void simulated$useHoneyGlue(final LocalPlayer player, final InteractionHand hand,
                                        final BlockHitResult result,
                                        final CallbackInfoReturnable<InteractionResult> cir) {
        final ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(SimItems.HONEY_GLUE.get())) {
            return;
        }

        final boolean selected = SimClickInteractions.HONEY_GLUE_MANAGER.selectPos(
                result.getBlockPos(), player, stack);
        cir.setReturnValue(selected ? InteractionResult.SUCCESS : InteractionResult.FAIL);
    }

    @Inject(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"
            ),
            cancellable = true
    )
    private void quietUseIntercept(final LocalPlayer player, final InteractionHand hand, final BlockHitResult result, final CallbackInfoReturnable<InteractionResult> cir) {
        final BlockState state = player.level().getBlockState(result.getBlockPos());
        if (state.getBlock() instanceof final QuietUse quietUse) {
            final InteractionResult useResult = quietUse.quietUse(player, hand, result.getBlockPos(), state);
            if (useResult != null) {
                cir.setReturnValue(useResult);
            }
        }
    }
}
