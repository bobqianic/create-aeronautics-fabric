package dev.simulated_team.simulated.service;

import net.minecraft.world.item.CreativeModeTab;

public interface SimTabService {

	SimTabService INSTANCE = ServiceUtil.load(SimTabService.class);

	CreativeModeTab getCreativeTab();
}
