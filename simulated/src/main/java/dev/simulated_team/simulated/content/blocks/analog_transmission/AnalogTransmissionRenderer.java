package dev.simulated_team.simulated.content.blocks.analog_transmission;

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

public class AnalogTransmissionRenderer extends KineticBlockEntityRenderer<AnalogTransmissionBlockEntity> {

    public AnalogTransmissionRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final AnalogTransmissionBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final BlockState state = be.getBlockState();
        final Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);

        final SuperByteBuffer cogwheel = kineticRotationTransform(
                CachedBuffers.partialFacingVertical(SimPartialModels.ANALOG_TRANSMISSION_COG, state, Direction.fromAxisAndDirection(state.getValue(AnalogTransmissionBlock.AXIS), Direction.AxisDirection.POSITIVE)),
                be.getExtraKinetics(),
                axis,
                getAngleForBe(be.getExtraKinetics(), be.getBlockPos(), axis),
                light);

        cogwheel.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));

        final VertexConsumer vb = buffer.getBuffer(RenderType.solid());
        KineticBlockEntityRenderer.renderRotatingKineticBlock(be, shaft(getRotationAxisOf(be)), ms, vb, light);
    }

    @Override
    protected BlockState getRenderedBlockState(final AnalogTransmissionBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
