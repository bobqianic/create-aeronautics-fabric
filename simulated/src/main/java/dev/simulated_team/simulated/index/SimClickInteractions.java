package dev.simulated_team.simulated.index;

import dev.simulated_team.simulated.content.blocks.handle.ClientHandleHandler;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerGUIHandler;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.ZiplineClientManager;
import dev.simulated_team.simulated.content.blocks.steering_wheel.SteeringWheelHandler;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverHandler;
import dev.simulated_team.simulated.content.entities.honey_glue.HoneyGlueClientHandler;
import dev.simulated_team.simulated.content.items.spring.SpringItemHandler;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffClientHandler;
import dev.simulated_team.simulated.util.click_interactions.InteractCallback;
import java.util.ArrayList;
import java.util.List;

public class SimClickInteractions {

    // Input handlers have priority. A hash set made the winning handler depend
    // on identity hash order, which could prevent Honey Glue from ever seeing a
    // right click. Keep a stable order and give held-item interactions priority.
    public static final List<InteractCallback> CLICK_INTERACTION_ENTRIES = new ArrayList<>();

    public static HoneyGlueClientHandler HONEY_GLUE_MANAGER = register(new HoneyGlueClientHandler());
    public static SpringItemHandler SPRING_INTERACTION = register(new SpringItemHandler());
    public static ZiplineClientManager ZIPLINE_INTERACTION = register(new ZiplineClientManager());
    public static PhysicsStaffClientHandler.PhysicsStaffMouseHandler PHYSICS_STAFF_MANAGER = register(new PhysicsStaffClientHandler.PhysicsStaffMouseHandler());
    public static ClientHandleHandler HANDLE_HANDLER = register(new ClientHandleHandler());
    public static PhysicsAssemblerGUIHandler PHYSICS_ASSEMBLER_MANAGER = register(new PhysicsAssemblerGUIHandler());
    public static SteeringWheelHandler STEERING_WHEEL_MANAGER = register(new SteeringWheelHandler());
    public static ThrottleLeverHandler THROTTLE_LEVER_MANAGER = register(new ThrottleLeverHandler());

    public static <T extends InteractCallback> T register(final T interaction) {
        CLICK_INTERACTION_ENTRIES.add(interaction);
        return interaction;
    }
}
