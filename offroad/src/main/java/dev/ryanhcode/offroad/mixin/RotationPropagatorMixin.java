package dev.ryanhcode.offroad.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingBlockEntity;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RotationPropagator.class)
public class RotationPropagatorMixin {

    @WrapMethod(method = "getAxisModifier")
    private static float offroad$boreheadBearingRotation(final KineticBlockEntity be, final Direction direction, final Operation<Float> original) {
        if (be.hasSource() && be instanceof final BoreheadBearingBlockEntity bhbe) {
            return bhbe.handleAxisModification(direction);
        }

        return original.call(be, direction);
    }

}
