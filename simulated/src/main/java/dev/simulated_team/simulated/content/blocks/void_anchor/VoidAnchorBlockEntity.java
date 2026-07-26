package dev.simulated_team.simulated.content.blocks.void_anchor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class VoidAnchorBlockEntity extends BlockEntity {
    public VoidAnchorBlockEntity(final BlockEntityType<?> blockEntityType, final BlockPos blockPos, final BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }
}
