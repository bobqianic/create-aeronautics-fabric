package dev.simulated_team.simulated.mixin_interface;

import dev.simulated_team.simulated.content.entities.launched_plunger.LaunchedPlungerEntity;

public interface PlayerLaunchedPlungerExtension {
    void simulated$setLaunchedPlunger(LaunchedPlungerEntity plunger);
    LaunchedPlungerEntity simulated$getLaunchedPlunger();
}
