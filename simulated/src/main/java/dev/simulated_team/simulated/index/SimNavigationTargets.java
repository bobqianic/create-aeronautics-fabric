package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.navigation_targets.CompassNavigationTarget;
import dev.simulated_team.simulated.content.navigation_targets.MagnetNavigationTarget;
import dev.simulated_team.simulated.content.navigation_targets.MapNavigationTarget;
import dev.simulated_team.simulated.content.navigation_targets.RecoveryCompassNavigationTarget;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public class SimNavigationTargets {
	private static final SimulatedRegistrate REGISTRATE = Simulated.getRegistrate();

	public static final Supplier<CompassNavigationTarget> COMPASS = REGISTRATE.navTarget("compass", CompassNavigationTarget::new, Items.COMPASS);
	public static final Supplier<RecoveryCompassNavigationTarget> RECOVERY_COMPASS = REGISTRATE.navTarget("recovery_compass", RecoveryCompassNavigationTarget::new, Items.RECOVERY_COMPASS);
	public static final Supplier<MapNavigationTarget> MAP = REGISTRATE.navTarget("map", MapNavigationTarget::new, Items.FILLED_MAP);
	public static final Supplier<MagnetNavigationTarget> MAGNET = REGISTRATE.navTarget("magnet", MagnetNavigationTarget::new, SimBlocks.REDSTONE_MAGNET);

	public static void register() {

	}
}
