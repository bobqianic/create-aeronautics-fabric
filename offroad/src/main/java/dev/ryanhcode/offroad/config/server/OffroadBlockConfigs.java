package dev.ryanhcode.offroad.config.server;

import com.zurrtum.create.catnip.config.ConfigBase;

public class OffroadBlockConfigs extends ConfigBase {

	public final ConfigFloat boreheadBearingSearchRadius = this.f(1.5f, 0, 10, "borehead_bearing_search_radius", "The block gathering search radius of the borehead bearing");
	public final ConfigInt boreheadBearingStallRecoveryTicks = this.i(10, 0, "borehead_bearing_stall_recovery_ticks", "The amount of ticks it takes for the borehead bearing to recover from an item stall");
	public final ConfigBool boreheadBearingStallingEnabled = this.b(true, "borehead_bearing_stalling_enabled", "Whether the borehead bearing should stall when it doesn't have enough room to accept a mined block");

	public final ConfigFloat boreheadBearingRotationDivisor = this.f(4, 1, "borehead_bearing_rotation_divisor", "The divisor used to determine the max speed of the attached borehead contraption contraption");

	@Override
	public String getName() {
		return "blocks";
	}
}
