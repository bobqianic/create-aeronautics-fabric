package dev.ryanhcode.offroad.mixin.borehead_collision;

import dev.ryanhcode.offroad.content.entities.BoreheadContraptionEntity;
import dev.ryanhcode.sable.api.sublevel.KinematicContraption;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BoreheadContraptionEntity.class)
public abstract class BoreheadContraptionEntityMixin implements KinematicContraption {

    @Override
    public boolean sable$shouldCollide() {
        return false;
    }
}
