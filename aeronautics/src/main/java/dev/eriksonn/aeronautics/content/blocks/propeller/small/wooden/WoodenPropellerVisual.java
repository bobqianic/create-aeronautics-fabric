package dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.SimplePropellerVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerBlock;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static dev.eriksonn.aeronautics.content.blocks.propeller.small.BasePropellerBlock.REVERSED;

public class WoodenPropellerVisual extends SimplePropellerVisual<WoodenPropellerBlockEntity> {

    public WoodenPropellerVisual(final VisualizationContext context, final WoodenPropellerBlockEntity blockEntity, final float partialTick) {
        super(context, blockEntity, partialTick);
    }

    @Override
    public PartialModel getModel(final BlockState state) {
        return state.getValue(REVERSED) ? AeroPartialModels.WOODEN_PROPELLER_REVERSED : AeroPartialModels.WOODEN_PROPELLER;
    }

    @Override
    public float getAngle(final float partialTicks) {
        final BlockState state = this.blockEntity.getBlockState();
        final BlockPos pos = this.blockEntity.getBlockPos();
        return super.getAngle(partialTicks) + rotationOffset(state, state.getValue(AndesitePropellerBlock.FACING).getAxis(), pos);
    }
}
