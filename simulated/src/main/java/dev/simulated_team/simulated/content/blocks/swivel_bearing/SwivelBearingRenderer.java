package dev.simulated_team.simulated.content.blocks.swivel_bearing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.content.kinetics.base.IRotate;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class SwivelBearingRenderer extends KineticBlockEntityRenderer<SwivelBearingBlockEntity> {

    public SwivelBearingRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final SwivelBearingBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final BlockState state = be.getBlockState();
        final Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);

        final SuperByteBuffer cogwheel = kineticRotationTransform(
                CachedBuffers.partialFacingVertical(SimPartialModels.SWIVEL_BEARING_COG, state, state.getValue(SwivelBearingBlock.FACING).getOpposite()),
                be.getExtraKinetics(),
                axis,
                getAngleForBe(be.getExtraKinetics(), be.getBlockPos(), axis),
                light);

        final VertexConsumer vb = buffer.getBuffer(RenderType.solid());
        cogwheel.renderInto(ms.last(), vb);
        if (!be.isAssembled()) {
            renderRotatingBuffer(be, CachedBuffers.partialFacing(SimPartialModels.SHAFT_SIXTEENTH, state, state.getValue(SwivelBearingBlock.FACING)), ms, vb, light);
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final SwivelBearingBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacing(SimPartialModels.SHAFT_SIXTEENTH, state, state.getValue(SwivelBearingBlock.FACING).getOpposite());
    }
}
