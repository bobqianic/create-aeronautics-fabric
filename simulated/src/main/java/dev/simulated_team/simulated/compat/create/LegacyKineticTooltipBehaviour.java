package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.api.goggles.IHaveHoveringInformation;
import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LegacyKineticTooltipBehaviour extends TooltipBehaviour<KineticBlockEntity>
        implements IHaveGoggleInformation, IHaveHoveringInformation, IDisplayAssemblyExceptions {
    public LegacyKineticTooltipBehaviour(final KineticBlockEntity blockEntity) {
        super(blockEntity);
    }

    public static void installBridge() {
        LegacyKineticTooltipBridge.setDelegate(new LegacyKineticTooltipBridge.Delegate() {
            @Override
            public boolean addToTooltip(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                        final boolean isPlayerSneaking) {
                return new DelegateTooltipBehaviour(blockEntity).addToTooltip(tooltip, isPlayerSneaking);
            }

            @Override
            public boolean addToGoggleTooltip(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                              final boolean isPlayerSneaking) {
                return new DelegateTooltipBehaviour(blockEntity).addToGoggleTooltip(tooltip, isPlayerSneaking);
            }

            @Override
            public void addStressImpactStats(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                             final float stressAtBase) {
                new DelegateTooltipBehaviour(blockEntity).addLegacyStressImpactStats(tooltip, stressAtBase);
            }
        });
    }

    @Override
    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return ((LegacyKineticTooltipProvider) blockEntity).addToTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        // The delegated KineticTooltipBehaviour already receives the
        // extra-kinetics tooltip mixin, so appending here duplicates the section.
        return ((LegacyKineticTooltipProvider) blockEntity).addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        if (blockEntity instanceof final dev.simulated_team.simulated.compat.create.IDisplayAssemblyExceptions display) {
            return display.getLastAssemblyException();
        }
        return null;
    }

    private static final class DelegateTooltipBehaviour extends GeneratingKineticTooltipBehaviour<KineticBlockEntity> {
        private DelegateTooltipBehaviour(final KineticBlockEntity blockEntity) {
            super(blockEntity);
        }

        private void addLegacyStressImpactStats(final List<Component> tooltip, final float stressAtBase) {
            addStressImpactStats(tooltip, stressAtBase);
        }
    }
}
