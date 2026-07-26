package dev.simulated_team.simulated.mixin.handle;

import dev.simulated_team.simulated.index.SimClickInteractions;
import dev.simulated_team.simulated.util.SimDistUtil;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void simulated$useHoneyGlue(final CallbackInfo ci) {
        final var player = SimDistUtil.getClientPlayer();
        if (player != null && SimClickInteractions.HONEY_GLUE_MANAGER.tryUse(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/InteractionResult$Success;swingSource()Lnet/minecraft/world/InteractionResult$SwingSource;"))
    private void makeHandleHandlerCountUsing(final CallbackInfo ci) {
        SimClickInteractions.HANDLE_HANDLER.actuallyUsedBlockCountdown = 4;
    }
}
