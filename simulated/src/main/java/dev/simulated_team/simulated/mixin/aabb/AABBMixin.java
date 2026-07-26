package dev.simulated_team.simulated.mixin.aabb;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AABB.class)
public interface AABBMixin {

    @Invoker
    static Direction invokeGetDirection(final AABB aABB, final Vec3 vec3, final double[] ds, @Nullable final Direction direction, final double d, final double e, final double f) {
        throw new AssertionError();
    }

}
