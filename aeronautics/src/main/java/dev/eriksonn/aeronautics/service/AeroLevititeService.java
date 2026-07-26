package dev.eriksonn.aeronautics.service;

import dev.simulated_team.simulated.service.ServiceUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public interface AeroLevititeService {
    AeroLevititeService INSTANCE = ServiceUtil.load(AeroLevititeService.class);

    Item getBucket();

    Fluid getFluid();
}
