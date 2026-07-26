package dev.simulated_team.simulated.config.server.blocks;

import com.zurrtum.create.catnip.config.ConfigBase;

public class SimKinetics extends ConfigBase {
    public final SimStress stressValues = this.nested(1, SimStress::new, Comments.stress);

    @Override
    public String getName() {
        return "kinetics";
    }

    private static class Comments{
        static String stress = "Fine tune the kinetic stats of individual components";
    }
}
