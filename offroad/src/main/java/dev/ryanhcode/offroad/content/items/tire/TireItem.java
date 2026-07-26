package dev.ryanhcode.offroad.content.items.tire;

import dev.simulated_team.simulated.util.SimColors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class TireItem extends Item {
    public TireItem(final Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(final @NotNull UseOnContext context) {
        final Player player = context.getPlayer();

        if (player != null && player.level().isClientSide()) {
            player.displayClientMessage(Component.translatable("item.offroad.tire.placement_error")
                    .setStyle(Style.EMPTY.withColor(SimColors.NUH_UH_RED)), true);
        }

        return super.useOn(context);
    }
}
