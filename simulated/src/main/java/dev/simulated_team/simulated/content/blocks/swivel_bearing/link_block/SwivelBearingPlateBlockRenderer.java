package dev.simulated_team.simulated.content.blocks.swivel_bearing.link_block;

import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlock;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class SwivelBearingPlateBlockRenderer extends KineticBlockEntityRenderer<SwivelBearingPlateBlockEntity> {

    public SwivelBearingPlateBlockRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final SwivelBearingPlateBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacing(SimPartialModels.SHAFT_SIXTEENTH, state, state.getValue(SwivelBearingBlock.FACING));
    }
}
