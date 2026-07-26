package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LegacyCustomKineticTooltipBehaviour<T extends KineticBlockEntity>
        extends GeneratingKineticTooltipBehaviour<T> implements IDisplayAssemblyExceptions {
    public LegacyCustomKineticTooltipBehaviour(final T blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        boolean added = super.addToTooltip(tooltip, isPlayerSneaking);
        if (blockEntity instanceof final IHaveHoveringInformation information) {
            added |= information.addToTooltip(tooltip, isPlayerSneaking);
        }
        return added;
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (blockEntity instanceof final IHaveGoggleInformation information) {
            added |= information.addToGoggleTooltip(tooltip, isPlayerSneaking);
        }
        return added;
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        if (blockEntity instanceof final dev.simulated_team.simulated.compat.create.IDisplayAssemblyExceptions display) {
            return display.getLastAssemblyException();
        }
        if (blockEntity instanceof final MechanicalBearingBlockEntity bearing) {
            return bearing.getLastAssemblyException();
        }
        return null;
    }
}
