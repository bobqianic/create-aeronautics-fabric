package dev.eriksonn.aeronautics.config.server;

import com.zurrtum.create.catnip.config.ConfigBase;

public class AeroPhysics extends ConfigBase {

	public final ConfigFloat mountedPotatoCannonMagnitude = this.f(0.2f, 0, Float.MAX_VALUE, "recoil_magnitude", Comments.mountedPotatoCannonComment);
	public final ConfigFloat propellerBearingThrust = this.f(0.2f, 0, Float.MAX_VALUE, "propellerBearingThrust", Comments.propellerBearingThrust);
	public final ConfigFloat propellerBearingAirflowMult = this.f(0.05f, 0, Float.MAX_VALUE, "propellerBearingAirflow", Comments.propellerBearingAirflow);
	public final ConfigFloat woodenPropellerThrust = this.f(1.0f, 0, Float.MAX_VALUE, "woodenPropellerThrust", Comments.woodenPropellerThrust);
	public final ConfigFloat woodenPropellerAirflow = this.f(0.1f, 0, Float.MAX_VALUE, "woodenPropellerAirflow", Comments.woodenPropellerAirflow);
	public final ConfigFloat andesitePropellerThrust = this.f(1.0f, 0, Float.MAX_VALUE, "andesitePropellerThrust", Comments.andesitePropellerThrust);
	public final ConfigFloat andesitePropellerAirflow = this.f(0.1f, 0, Float.MAX_VALUE, "andesitePropellerAirflow", Comments.andesitePropellerAirflow);
	public final ConfigFloat smartPropellerThrust = this.f(1.0f, 0, Float.MAX_VALUE, "smartPropellerThrust", Comments.smartPropellerThrust);
	public final ConfigFloat smartPropellerAirflow = this.f(0.1f, 0, Float.MAX_VALUE, "smartPropellerAirflow", Comments.smartPropellerAirflow);
	public final ConfigFloat hotAirStrength = this.f(1.5f, 0, Float.MAX_VALUE, "hotAirStrength", Comments.hotAirStrength);
	public final ConfigFloat steamStrength = this.f(1.5f, 0, Float.MAX_VALUE, "steamStrength", Comments.steamStrength);

	@Override
	public String getName() {
		return "physics";
	}

	private static class Comments {
		static String mountedPotatoCannonComment = "The recoil magnitude used whenever the Mounted Potato Cannon shoots";
		static String propellerBearingThrust = "Thrust scaling for Propeller Bearings";
		static String woodenPropellerThrust = "Thrust scaling for Wooden Propellers";
		static String woodenPropellerAirflow = "Airflow scaling for Wooden Propellers";
		static String andesitePropellerThrust = "Thrust scaling for Andesite Propellers";
		static String andesitePropellerAirflow = "Airflow scaling for Andesite Propellers";
		static String smartPropellerThrust = "Thrust scaling for Smart Propellers";
		static String smartPropellerAirflow = "Airflow scaling for Smart Propellers";
		static String propellerBearingAirflow = "Airflow scaling for Propeller Bearings";
		static String hotAirStrength = "kpg lifted per cubic meter of Hot Air";
		static String steamStrength = "kpg lifted per cubic meter of Steam";
	}
}
