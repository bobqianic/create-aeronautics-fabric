package dev.eriksonn.aeronautics.config.server;

import com.zurrtum.create.catnip.config.ConfigBase;
import net.neoforged.neoforge.common.ModConfigSpec;

public class AeroServer extends ConfigBase {

	public final AeroPhysics physics = this.nested(0, AeroPhysics::new, Comments.physics);
	public final AeroBlockConfigs blocks = this.nested(0, AeroBlockConfigs::new, Comments.blockConfig);
	public final AeroKinetics kinetics = this.nested(0, AeroKinetics::new, Comments.kinetics);

	@Override
	public String getName() {
		return "server";
	}

	private static class Comments {
		static String kinetics = "Parameters and abilities of Aeronautics's kinetic mechanisms";
		static String physics = "Parameters related to the physics of Aeronautics blocks";
		static String blockConfig = "Parameters and abilities of Aeronautics Blocks";
	}
}
