package dev.simulated_team.simulated.mixin.handle;

import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import dev.simulated_team.simulated.index.SimClickInteractions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExtendoGripRenderHandler.class)
public class ExtendoGripRenderHandlerMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private static void handleAnimate(final CallbackInfo ci) {
        if (SimClickInteractions.HANDLE_HANDLER.isActive()) {
            ExtendoGripRenderHandler.mainHandAnimation = 0.95f;
        }
    }
}
