package dev.simulated_team.simulated.content.items.spring;

import dev.simulated_team.simulated.index.SimClickInteractions;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class SpringItem extends Item {
    public SpringItem(final Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        if (context.getPlayer().isLocalPlayer() && SimClickInteractions.SPRING_INTERACTION.tryStartPlacement(context)) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}
