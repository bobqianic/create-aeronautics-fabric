package dev.simulated_team.simulated.util.hold_interaction;

import dev.simulated_team.simulated.util.SimDistUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Manages client interactions that require holding use on a block
 */
public class HoldInteractionManager {
    @Nullable
    private static BlockHoldInteraction active = null;
    private static int crouchBlockTicks = 0;

    public static boolean isActive() {
        return active != null;
    }

    public static boolean isActive(final BlockHoldInteraction blockHoldInteraction) {
        return blockHoldInteraction == active;
    }

    public static void start(final BlockHoldInteraction blockHoldInteraction) {
        if (active != null) {
            active.stop();
        }

        active = blockHoldInteraction;
        active.start();
    }

    public static void stop() {
        if (active != null) {
            active.stop();
            active = null;
        }
    }

    public static void tick(final Level level, final LocalPlayer player) {
        if (crouchBlockTicks > 0) {
            if (!unblockedShift()) {
                crouchBlockTicks = 0;
            }
            crouchBlockTicks--;
        }

        if (active != null && level != null) {
            crouchBlockTicks = active.getCrouchBlockingTicks();
            if (active.activeTick(level, player)) {
                active.stop();
                active = null;
            }
        }
    }

    public static void renderOverlay(final GuiGraphics graphics, final int width, final int height) {
        if (active != null) {
            active.renderOverlay(graphics, width, height, Minecraft.getInstance().options.hideGui);
        }
    }

    public static boolean canCrouch() {
        return crouchBlockTicks <= 0;
    }

    public static boolean unblockedShift() {
        return ((LocalPlayer) SimDistUtil.getClientPlayer()).input.keyPresses.shift();
    }
}
