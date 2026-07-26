package dev.simulated_team.simulated.service;

import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuCommon;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.screen.LinkedTypewriterMenuData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

public interface SimMenuService {

	SimMenuService INSTANCE = ServiceUtil.load(SimMenuService.class);

	<T extends LinkedTypewriterMenuCommon> T getLoaderLinkedTypewriter(MenuType<?> type, int id, Inventory inv, LinkedTypewriterMenuData data);

	<T extends LinkedTypewriterMenuCommon> T getLoaderLinkedTypewriter(MenuType<?> type, int id, Inventory inv, LinkedTypewriterBlockEntity be);

	void openScreen(ServerPlayer player, MenuProvider factory, LinkedTypewriterMenuData data);
}
