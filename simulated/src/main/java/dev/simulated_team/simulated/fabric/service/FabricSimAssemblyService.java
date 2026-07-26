package dev.simulated_team.simulated.fabric.service;

import dev.simulated_team.simulated.service.SimAssemblyService;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricSimAssemblyService implements SimAssemblyService {
    @Override
    public boolean canStickTo(final BlockState state, final BlockState other) {
        final Block block = state.getBlock();
        if (block == Blocks.SLIME_BLOCK) {
            return !other.is(Blocks.HONEY_BLOCK);
        }
        if (block == Blocks.HONEY_BLOCK) {
            return !other.is(Blocks.SLIME_BLOCK);
        }
        return other.is(Blocks.SLIME_BLOCK) || other.is(Blocks.HONEY_BLOCK);
    }
}
