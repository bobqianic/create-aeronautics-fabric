package dev.simulated_team.simulated.config.server.items;

import com.zurrtum.create.catnip.config.ConfigBase;

public class SimEquipment extends ConfigBase {
    public final ConfigInt maxPlungerLauncherShots = this.i(100, 0, "maxPlungerLauncherShots", Comments.maxPlungerLauncherShots);
    public final ConfigInt maxPlungerLauncherRange = this.i(64, 0, "maxPlungerLauncherRange", Comments.maxPlungerLauncherRange);

    @Override
    public String getName() {
        return "equipment";
    }

    private static class Comments {
        static String maxPlungerLauncherShots = "Amount of free Plunger Launcher shots provided by one filled Backtank. Set to 0 makes Plunger Launchers unbreakable";
        static String maxPlungerLauncherRange = "The max range that launched plungers can be from each other";
    }
}
