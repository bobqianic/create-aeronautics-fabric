package dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden;


import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlockEntity;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class WoodenPropellerBlock extends BasePropellerBlock {
    public WoodenPropellerBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntityType<? extends BasePropellerBlockEntity> getBlockEntityType() {
        return AeroBlockEntityTypes.WOODEN_PROPELLER.get();
    }
}
