package dev.simulated_team.simulated.config.server;

import dev.simulated_team.simulated.config.server.blocks.SimAssembly;
import dev.simulated_team.simulated.config.server.blocks.SimBlockConfigs;
import dev.simulated_team.simulated.config.server.blocks.SimKinetics;
import dev.simulated_team.simulated.config.server.items.SimEquipment;
import dev.simulated_team.simulated.config.server.physics.SimPhysics;
import com.zurrtum.create.catnip.config.ConfigBase;

public class SimServer extends ConfigBase {
    public final SimKinetics kinetics = this.nested(0, SimKinetics::new, Comments.kinetics);
    public final SimAssembly assembly = this.nested(0, SimAssembly::new, Comments.assembly);
    public final SimPhysics physics = this.nested(0, SimPhysics::new, Comments.physics);
    public final SimBlockConfigs blocks = this.nested(0, SimBlockConfigs::new, Comments.blockConfig);
    public final SimEquipment equipment = this.nested(0, SimEquipment::new, Comments.equipmentConfig);

    @Override
    public String getName() {
        return "server";
    }

    private static class Comments {
        static String kinetics = "Parameters and abilities of Simulated's kinetic mechanisms";
        static String assembly = "Settings for sub-level assembly";
        static String physics = "Parameters related to the physics of Simulated Contraptions";
        static String blockConfig = "Parameters and abilities of Simulated Blocks";
        static String equipmentConfig = "Equipment and gadgets added by Simulated";
    }

}
