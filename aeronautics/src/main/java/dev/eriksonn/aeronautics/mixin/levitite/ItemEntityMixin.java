package dev.eriksonn.aeronautics.mixin.levitite;

import dev.eriksonn.aeronautics.content.components.Levitating;
import dev.eriksonn.aeronautics.index.AeroDataComponents;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(final EntityType<?> entityType, final Level level) {
        super(entityType, level);
    }

    @Shadow public abstract ItemStack getItem();

    @Inject(method = "getDefaultGravity", at = @At("HEAD"), cancellable = true)
    private void aeronautics$levitatingGravity(final CallbackInfoReturnable<Double> cir) {
        final Levitating component = this.getItem().get(AeroDataComponents.LEVITATING);
        if (component != null) {
            cir.setReturnValue(0d);
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void aeronautics$levitatingDragAndSparkles(final CallbackInfo ci) {
        final Levitating component = this.getItem().get(AeroDataComponents.LEVITATING);
        if (component != null) {
            final float dragFraction = Math.clamp(component.dragFraction(), 0, 1);
            this.setDeltaMovement(this.getDeltaMovement().scale(dragFraction));

            if (this.level().isClientSide() && component.particle().isPresent()) {
                if (this.level().random.nextFloat() < Mth.clamp(this.getItem().getCount() - 10, 5, 100) / 64f) {
                    final Vec3 ppos = VecHelper.offsetRandomly(this.getPosition(0), this.getRandom(), 0.4f).add(0, 0.3, 0);
                    this.level().addParticle(component.particle().get(), ppos.x, ppos.y, ppos.z, 0, 0, 0);
                }
            }
        }
    }
}
