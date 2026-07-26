package dev.eriksonn.aeronautics.index;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.eriksonn.aeronautics.Aeronautics;

public class AeroPartialModels {

	public static final PartialModel
			STEAM_VENT_REDSTONE = block("steam_vent/redstone"),
			STEAM_VENT_BASE = block("steam_vent/steam_base"),
			STEAM_VENT_JET = block("steam_vent/steam_jet"),
			BEARING_PLATE = block("propeller_bearing/bearing_plate"),
			BEARING_PLATE_METAL = block("gyroscopic_propeller_bearing/metal_bearing_plate"),
			GYRO_BEARING_PISTON_HEAD = block("gyroscopic_propeller_bearing/piston_head"),
			GYRO_BEARING_PISTON_POLE = block("gyroscopic_propeller_bearing/piston_pole"),
			HOT_AIR_BURNER_INDICATOR = block("adjustable_burner/redstone_indicator"),
			CANNON_BARREL = block("mounted_potato_cannon/partials/barrel"),
			CANNON_BELLOW = block("mounted_potato_cannon/partials/bellow"),
			CANNON_COG = block("mounted_potato_cannon/partials/cog"),
			ANDESITE_PROPELLER = block("andesite_propeller/propeller"),
			WOODEN_PROPELLER = block("wooden_propeller/propeller"),
			ANDESITE_PROPELLER_REVERSED = block("andesite_propeller/propeller_reversed"),
			WOODEN_PROPELLER_REVERSED = block("wooden_propeller/propeller_reversed"),
			SMART_PROPELLER = block("smart_propeller/propeller"),
			SMART_PROPELLER_REVERSED = block("smart_propeller/propeller_reversed"),
			SMART_PROPELLER_HINGE = block("smart_propeller/hinge");

	private static PartialModel block(final String path) {
		return PartialModel.of(Aeronautics.path("block/" + path));
	}
	private static PartialModel entity(final String path) {
		return PartialModel.of(Aeronautics.path("entity/" + path));
	}
	private static PartialModel item(final String path) {
		return PartialModel.of(Aeronautics.path("item/" + path));
	}

	public static void init() {
	}
}
