package dev.simulated_team.simulated.content.blocks.auger_shaft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class AugerShaftRenderer extends KineticBlockEntityRenderer<AugerShaftBlockEntity> {

    public AugerShaftRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final AugerShaftBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              final int light, final int overlay) {
        final BlockState state = this.getRenderedBlockState(be);

        if (isRenderingInSubLevel() || !VisualizationManager.supportsVisualization(be.getLevel())) {
            final RenderType type = RenderType.solid();
            renderRotatingBuffer(be, this.getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
        }

        if (be.getBlockState().getBlock() instanceof AugerCogBlock) {
            final Direction facing = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AugerShaftBlock.AXIS));
            final VertexConsumer solid = buffer.getBuffer(RenderType.solid());

            for (int i = 0; i < 2; i++) {
                final SuperByteBuffer redstone = CachedBuffers.partialFacing(be.flowDirection == (i == 1 ? facing.getOpposite() : facing) && be.getSpeed() != 0 ? SimPartialModels.AUGER_REDSTONE_ON : SimPartialModels.AUGER_REDSTONE_OFF, state, facing);

                TransformStack.of(redstone.getTransforms())
                        .center()
                        .rotateToFace(facing)
                        .rotate(Axis.XN.rotationDegrees((facing.getAxis().isHorizontal() ? 90 : 0) + i * 180))
                        .uncenter();

                redstone.light(light)
                        .renderInto(ms.last(), solid);
            }
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final AugerShaftBlockEntity be, final BlockState state) {
        if (!(be.getBlockState().getBlock() instanceof AugerCogBlock)) {
            return super.getRotatedModel(be, state);
        }
        final Direction facing = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AugerShaftBlock.AXIS));
        return CachedBuffers.partialDirectional(
                SimPartialModels.AUGER_COG, state,
                facing, () -> {
                    final PoseStack poseStack = new PoseStack();
                    TransformStack.of(poseStack)
                            .center()
                            .rotateToFace(facing)
                            .rotate(Axis.XN.rotationDegrees(90))
                            .uncenter();
                    return poseStack;
                });
    }

    @Override
    protected BlockState getRenderedBlockState(final AugerShaftBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

}
