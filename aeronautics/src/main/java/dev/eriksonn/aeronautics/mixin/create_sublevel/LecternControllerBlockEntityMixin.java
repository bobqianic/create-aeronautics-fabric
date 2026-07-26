package dev.eriksonn.aeronautics.mixin.create_sublevel;

import com.zurrtum.create.content.redstone.link.controller.LecternControllerBlockEntity;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LecternControllerBlockEntity.class, remap = false)
public abstract class LecternControllerBlockEntityMixin {

    @Inject(method = "playerInRange", at = @At("HEAD"), cancellable = true)
    private static void aeronautics$playerInRangeAcrossSubLevels(
            final Player player, final Level world, final BlockPos pos,
            final CallbackInfoReturnable<Boolean> cir
    ) {
        final double reach = 0.4 * player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        cir.setReturnValue(
                Sable.HELPER.distanceSquaredWithSubLevels(world, player.getEyePosition(), Vec3.atCenterOf(pos))
                        < reach * reach
        );
    }
}
