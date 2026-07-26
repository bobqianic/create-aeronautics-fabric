package dev.ryanhcode.offroad.config.server;

import com.zurrtum.create.catnip.config.ConfigBase;

public class OffroadKinetics extends ConfigBase {

	public final OffroadStress stressValues = this.nested(1, OffroadStress::new, Comments.stress);

	@Override
	public String getName() {
		return "kinetics";
	}

	private static class Comments {
		static String stress = "Fine tune the kinetic stats of individual components";
	}
}
