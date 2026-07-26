package dev.simulated_team.simulated.compat.create;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class ExtraKineticsTooltip {
    private ExtraKineticsTooltip() {
    }

    public static boolean append(final KineticBlockEntity blockEntity, final List<Component> tooltip,
                                 final boolean isPlayerSneaking, final boolean existingTooltip) {
        if (!(blockEntity instanceof final ExtraKinetics extraKinetics)) {
            return existingTooltip;
        }

        final KineticBlockEntity extraBlockEntity = extraKinetics.getExtraKinetics();
        if (!(extraBlockEntity instanceof final ExtraKinetics.ExtraKineticsBlockEntity namedExtra)) {
            return existingTooltip;
        }

        final ArrayList<Component> extraTooltip = new ArrayList<>();
        final boolean applied;
        if (extraBlockEntity instanceof final LegacyKineticTooltipProvider legacyTooltip) {
            applied = legacyTooltip.addToGoggleTooltip(extraTooltip, isPlayerSneaking);
        } else {
            final TooltipBehaviour<?> behaviour = extraBlockEntity.getBehaviour(TooltipBehaviour.TYPE);
            applied = behaviour instanceof final IHaveGoggleInformation information
                    && information.addToGoggleTooltip(extraTooltip, isPlayerSneaking);
        }

        if (!applied) {
            return existingTooltip;
        }
        if (existingTooltip) {
            tooltip.add(Component.empty());
        }

        SimLang.translate("extra_kinetics.information", SimLang.builder().add(namedExtra.getKey()).style(ChatFormatting.AQUA))
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);
        tooltip.addAll(extraTooltip);
        return true;
    }
}
