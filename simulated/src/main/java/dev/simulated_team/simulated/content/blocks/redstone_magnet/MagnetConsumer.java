package dev.simulated_team.simulated.content.blocks.redstone_magnet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface MagnetConsumer<T extends BlockEntity & SimMagnet> {

    MagnetPair<T> apply(Level level, BlockPos pos1, BlockPos pos2);

}
