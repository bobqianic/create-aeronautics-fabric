package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LegacyTooltipBehaviour<T extends SmartBlockEntity> extends TooltipBehaviour<T>
        implements com.zurrtum.create.client.api.goggles.IHaveGoggleInformation,
        com.zurrtum.create.client.api.goggles.IHaveHoveringInformation, IDisplayAssemblyExceptions {
    public LegacyTooltipBehaviour(final T blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return blockEntity instanceof final IHaveHoveringInformation information
                && information.addToTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return blockEntity instanceof final IHaveGoggleInformation information
                && information.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        if (blockEntity instanceof final dev.simulated_team.simulated.compat.create.IDisplayAssemblyExceptions display) {
            return display.getLastAssemblyException();
        }
        return null;
    }
}
