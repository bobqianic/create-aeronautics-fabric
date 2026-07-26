package dev.simulated_team.simulated.compat.naturescompass;

import dev.simulated_team.simulated.content.blocks.nav_table.NavTableBlockEntity;
import dev.simulated_team.simulated.content.blocks.nav_table.navigation_target.NavigationTarget;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NaturesCompassNavigationTarget implements NavigationTarget {
	@Override
	public @Nullable Vec3 getTarget(final NavTableBlockEntity navBE, final ItemStack self) {
		return null;
	}
}
