package dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite;

import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class AndesitePropellerBlock extends BasePropellerBlock {
    public AndesitePropellerBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends BasePropellerBlockEntity> getBlockEntityType() {
        return AeroBlockEntityTypes.ANDESITE_PROPELLER.get();
    }
}
