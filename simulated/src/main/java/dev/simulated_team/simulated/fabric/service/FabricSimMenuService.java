package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuCommon;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuData;
import dev.simulated_team.simulated.service.SimMenuService;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class FabricSimMenuService implements SimMenuService {
    @Override
    @SuppressWarnings("unchecked")
    public <T extends LinkedTypewriterMenuCommon> T getLoaderLinkedTypewriter(
            final MenuType<?> type, final int id, final Inventory inventory,
            final LinkedTypewriterMenuData data) {
        return (T) new LinkedTypewriterMenuCommon(type, id, inventory, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends LinkedTypewriterMenuCommon> T getLoaderLinkedTypewriter(
            final MenuType<?> type, final int id, final Inventory inventory,
            final LinkedTypewriterBlockEntity blockEntity) {
        return (T) new LinkedTypewriterMenuCommon(type, id, inventory, blockEntity);
    }

    @Override
    public void openScreen(final ServerPlayer player, final MenuProvider factory,
                           final LinkedTypewriterMenuData data) {
        player.openMenu(new ExtendedScreenHandlerFactory<LinkedTypewriterMenuData>() {
            @Override
            public LinkedTypewriterMenuData getScreenOpeningData(final ServerPlayer serverPlayer) {
                return data;
            }

            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return factory.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(final int id, final Inventory inventory, final Player menuPlayer) {
                return factory.createMenu(id, inventory, menuPlayer);
            }
        });
    }
}
