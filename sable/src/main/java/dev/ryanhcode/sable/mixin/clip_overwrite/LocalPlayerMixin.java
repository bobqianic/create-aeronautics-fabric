package dev.ryanhcode.sable.mixin.clip_overwrite;

import dev.ryanhcode.sable.Sable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Changes the block picking distance checks to take into account sublevels.
 *
 * <p>Sable's clip returns hit locations in plot space (millions of blocks away from the
 * world-space eye position), so every distance comparison in the pick pipeline must be
 * sub-level aware: {@code filterHitResult} otherwise discards plot hits as out-of-range
 * misses, and {@code pick} otherwise derives a plot-space-sized max distance for the
 * entity-pick search box.
 */
@Mixin(GameRenderer.class)
public class LocalPlayerMixin {

    @Redirect(method = "filterHitResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z"))
    private static boolean sable$closerThan(final Vec3 a, final Position b, final double d) {
        return Sable.HELPER.distanceSquaredWithSubLevels(Minecraft.getInstance().level, a, new Vec3(b.x(), b.y(), b.z())) < d * d;
    }

    @Redirect(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private double sable$distanceToSqr(final Vec3 instance, final Vec3 other) {
        return Sable.HELPER.distanceSquaredWithSubLevels(Minecraft.getInstance().level, instance, other);
    }

    @Redirect(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 sable$getEyePosition(final Entity instance, final float partialTicks) {
        return Sable.HELPER.getEyePositionInterpolated(instance, partialTicks);
    }
}
