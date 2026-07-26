package dev.simulated_team.simulated.mixin.spring_item_bounce;

import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.index.SimItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract void playSound(SoundEvent soundEvent, float f, float g);

    @Shadow private BlockPos blockPosition;

    @Shadow private Level level;

    @Shadow public abstract BlockPos blockPosition();

    @Shadow public abstract Level level();

    @Shadow public abstract Vec3 getPosition(float partialTicks);

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;updateEntityMovementAfterFallOn(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;)V"))
    private void updateEntityMovementAfterFallOn(final Block instance, final BlockGetter pLevel, final Entity entity) {

        if (entity instanceof final ItemEntity item && item.getItem().is(SimItems.SPRING.get())) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, -1, 1));
            return;
        }

        instance.updateEntityMovementAfterFallOn(pLevel, entity);
    }

    @Inject(method = "checkFallDamage", at = @At(value = "HEAD"))
    private void awardAdvancementBeforeFallReset(final double d, final boolean bl, final BlockState blockState, final BlockPos blockPos, final CallbackInfo ci) {
        if (bl && ((Entity)(Object)this) instanceof final ItemEntity item && item.getItem().is(SimItems.SPRING.get())) {
            if (item.fallDistance >= 128 && item.getOwner() instanceof final Player player) {
                SimAdvancements.MUST_COME_UP.awardTo(player);
            }
        }
    }
}
