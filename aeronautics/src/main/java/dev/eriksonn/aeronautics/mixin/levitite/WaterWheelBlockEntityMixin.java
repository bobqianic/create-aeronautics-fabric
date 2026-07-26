package dev.eriksonn.aeronautics.mixin.levitite;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import dev.eriksonn.aeronautics.service.AeroLevititeService;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WaterWheelBlockEntity.class)
public class WaterWheelBlockEntityMixin {

    @ModifyReturnValue(method = "getFlowVectorAtPosition", at = @At("RETURN"))
    private Vec3 aeronautics$getFlowVectorAtPosition(Vec3 original, @Local(name = "fluid") FluidState fluidstate) {
        if(fluidstate.getType().getBucket().equals(AeroLevititeService.INSTANCE.getBucket())) {
            original = original.reverse();
        }
        return original;
    }
}
