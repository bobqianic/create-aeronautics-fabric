package dev.eriksonn.aeronautics.config.server;

import com.zurrtum.create.catnip.config.ConfigBase;

public class AeroBlockConfigs extends ConfigBase {
	public final ConfigBool breakBlocksOnCrystallize = this.b(true, "break_blocks_on_levitite_crystallize", Comments.levititeBreaksBlocks);
	public final ConfigInt hotAirBurnerMaxHotAir = this.i(500, "hot_air_burner_max", Comments.hotAirBurnerHotAirAmount);
	public final ConfigInt hotAirBurnerMaxRange = this.i(80, "hot_air_burner_max_range", Comments.hotAirBurnerRange);
	public final ConfigInt steamVentMaxHotAir = this.i(5000, 0, "steam_vent_hot_air_amount", Comments.steamVentHotAirAmount);
	public final ConfigInt steamVentMaxRange = this.i(80, "steam_vent_max_range", Comments.steamVentRange);

	@Override
	public String getName() {
		return "blocks";
	}

	private static class Comments {
		private static final String levititeBreaksBlocks = "If Levitite Blend should break adjacent blocks with the appropriate tag";
		private static final String hotAirBurnerHotAirAmount = "The maximum hot air a Hot Air Burner can output";
		private static final String hotAirBurnerRange = "The maximum distance a Hot Air Burner is allowed to search to find a balloon";
		private static final String steamVentHotAirAmount = "The maximum steam a Steam Vent can output";
		private static final String steamVentRange = "The maximum distance a Steam Vent is allowed to search to find a balloon";

	}
}
