package dev.simulated_team.simulated.mixin.torsion_spring;

import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MechanicalBearingBlockEntity.class)
public abstract class MechanicalBearingBlockEntityMixin {
    @Redirect(method = "onSpeedChanged", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/contraptions/ControlledContraptionEntity;isStalled()Z"))
    private boolean dontRoundTheAngle(final ControlledContraptionEntity instance) {
        return true;
    }
}
