package dev.simulated_team.simulated.mixin.conditional_display_target;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import dev.simulated_team.simulated.api.ConditionalDisplayTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DisplayLinkBlockEntity.class, remap = false)
public class DisplayLinkBlockEntityMixin {
    @Shadow public DisplayTarget activeTarget;

    @Inject(method = "updateGatheredData", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/api/behaviour/display/DisplaySource;transferData(Lcom/zurrtum/create/content/redstone/displayLink/DisplayLinkContext;Lcom/zurrtum/create/api/behaviour/display/DisplayTarget;I)V", shift = At.Shift.BEFORE), cancellable = true)
    private void simulated$stopConditionalTransfer(final CallbackInfo ci, @Local(name = "context") final DisplayLinkContext context) {
        if (this.activeTarget instanceof final ConditionalDisplayTarget cdt && !cdt.allowsWriting(context)) {
            ci.cancel();
        }
    }
}
