package dev.simulated_team.simulated.compat.create;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface LegacyKineticTooltipProvider extends IHaveGoggleInformation, IHaveHoveringInformation {
    boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking);

    boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking);
}
