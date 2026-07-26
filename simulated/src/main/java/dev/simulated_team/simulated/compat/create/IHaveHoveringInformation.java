package dev.simulated_team.simulated.compat.create;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IHaveHoveringInformation {
    default boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return false;
    }
}
