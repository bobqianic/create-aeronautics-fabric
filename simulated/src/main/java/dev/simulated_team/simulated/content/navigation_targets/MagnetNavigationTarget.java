package dev.simulated_team.simulated.content.navigation_targets;

import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MagnetNavigationTarget implements NavigationTarget {
	@Override
	public @Nullable Vec3 getTarget(final NavTableBlockEntity navBE, final ItemStack self) {
		final Vec3i normal = Direction.NORTH.getUnitVec3i().multiply(10);
		return navBE.getProjectedSelfPos().add(normal.getX(), normal.getY(), normal.getZ());
	}

}
