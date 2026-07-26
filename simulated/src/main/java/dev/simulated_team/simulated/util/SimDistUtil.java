package dev.simulated_team.simulated.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for client behavior using common classes
 */
public class SimDistUtil {

    /**
     * @return the client player instance if it exists
     */
    @Nullable
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    public static HitResult getHitResult() {
        return Minecraft.getInstance().hitResult;
    }
}
