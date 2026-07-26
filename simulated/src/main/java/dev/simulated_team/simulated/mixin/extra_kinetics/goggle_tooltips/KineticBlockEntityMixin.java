package dev.simulated_team.simulated.mixin.extra_kinetics.goggle_tooltips;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import dev.simulated_team.simulated.compat.create.ExtraKineticsTooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = KineticTooltipBehaviour.class, remap = false)
public class KineticBlockEntityMixin {

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
    public void simulated$addExtraKineticsInfo(final List<Component> tooltip, final boolean isPlayerSneaking, final CallbackInfoReturnable<Boolean> cir) {
        final KineticTooltipBehaviour<?> behaviour = (KineticTooltipBehaviour<?>) (Object) this;
        cir.setReturnValue(ExtraKineticsTooltip.append(behaviour.blockEntity, tooltip, isPlayerSneaking, cir.getReturnValue()));
    }
}
