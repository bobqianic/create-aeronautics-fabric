package dev.eriksonn.aeronautics.mixin.potato_cannon_projectiles;

import com.zurrtum.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import dev.eriksonn.aeronautics.index.AeroAdvancements;
import dev.eriksonn.aeronautics.mixinterface.PotatoProjectileEntityExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotatoProjectileEntity.class)
public class PotatoProjectileEntityMixin implements PotatoProjectileEntityExtension {

	@Shadow(remap = false) protected float additionalDamageMult;
	@Unique
	public boolean aeronautics$isFromMountedPotatoCannon = false;

	@Override
	public void aeronautics$setIsFromMountedPotatoCannon(boolean value) {
		this.aeronautics$isFromMountedPotatoCannon = value;
	}

	@Override
	public void aeronautics$setDamageMultiplier(float value) {
		this.additionalDamageMult = value;
	}

	@Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER))
	private void onHitEntity(final EntityHitResult ray, final CallbackInfo ci) {
		if(this.aeronautics$isFromMountedPotatoCannon && !ray.getEntity().isAlive() && ray.getEntity() instanceof final Phantom phantom) {
			AeroAdvancements.GHOSTBUSTER.awardToNearby(BlockPos.containing(phantom.position()), phantom.level(), 30);
		}
	}
}
