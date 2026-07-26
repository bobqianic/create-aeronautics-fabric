package dev.simulated_team.simulated.mixin.lodestone_compat;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability.ClientLodestonePositions;
import dev.simulated_team.simulated.index.SimDataComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(CompassItemPropertyFunction.class)
public abstract class CompassItemPropertyFunctionMixin {

    @Shadow protected abstract float getRotationTowardsCompassTarget(Entity entity, long ticks, BlockPos pos);

	@Shadow
	protected abstract float getRandomlySpinningRotation(int seed, long ticks);

	@WrapMethod(method = "getCompassRotation")
    private float simulated$prioritizeID(final ItemStack stack, final ClientLevel level, final int seed, final Entity entity, final Operation<Float> original) {
        if (stack.has(SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER)) {
	        final UUID trackerID = stack.get(SimDataComponents.LODESTONE_COMPASS_SUBLEVEL_TRACKER);
	        final ClientLodestonePositions positions = ClientLodestonePositions.clientPositions.get(level);

	        final Vector3d pos = positions.CLIENT_LODESTONE_MAP.get(trackerID);
			if (pos != null) {
				return this.getRotationTowardsCompassTarget(entity, level.getGameTime(), BlockPos.containing(pos.x, pos.y, pos.z));
			} else {
				return this.getRandomlySpinningRotation(seed, level.getGameTime());
			}
        }

        return original.call(stack, level, seed, entity);
    }
}
