package dev.simulated_team.simulated.mixin.extra_kinetics;

import com.zurrtum.create.content.kinetics.KineticNetwork;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraBlockPos;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KineticNetwork.class)
public class KineticNetworkMixin {

   @Redirect(method = "calculateCapacity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity simulated$extraKineticsCapacity(final Level instance, final BlockPos blockPos) {
       final BlockEntity be = instance.getBlockEntity(blockPos);
       if (be instanceof final ExtraKinetics ek && blockPos instanceof ExtraBlockPos) {
           return ek.getExtraKinetics();
       }

       return be;
   }

    @Redirect(method = "calculateStress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    public BlockEntity simulated$extraKineticsStress(final Level instance, final BlockPos blockPos) {
        final BlockEntity be = instance.getBlockEntity(blockPos);
        if (be instanceof final ExtraKinetics ek && blockPos instanceof ExtraBlockPos) {
            return ek.getExtraKinetics();
        }

        return be;
    }


}
