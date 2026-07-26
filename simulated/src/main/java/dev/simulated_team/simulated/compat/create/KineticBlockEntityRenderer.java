package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class KineticBlockEntityRenderer<T extends KineticBlockEntity> extends SmartBlockEntityRenderer<T> {
    public KineticBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected boolean shouldSkipLegacyRender(final T blockEntity) {
        return false;
    }

    @Override
    protected void renderSafe(final T blockEntity, final float partialTicks, final PoseStack poseStack,
                              final net.minecraft.client.renderer.MultiBufferSource bufferSource, final int light, final int overlay) {
        final BlockState state = getRenderedBlockState(blockEntity);
        renderRotatingBuffer(blockEntity, getRotatedModel(blockEntity, state), poseStack,
                bufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)), light);
    }

    protected SuperByteBuffer getRotatedModel(final T blockEntity, final BlockState state) {
        return CachedBuffers.block(com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KINETIC_BLOCK, state);
    }

    protected BlockState getRenderedBlockState(final T blockEntity) {
        return blockEntity.getBlockState();
    }

    protected RenderType getRenderType(final T blockEntity, final BlockState state) {
        return ItemBlockRenderTypes.getMovingBlockRenderType(state);
    }

    protected void renderRotatingBuffer(final T blockEntity, final SuperByteBuffer buffer, final PoseStack poseStack,
                                        final VertexConsumer consumer, final int light) {
        kineticRotationTransform(buffer, blockEntity, getRotationAxisOf(blockEntity),
                getAngleForBe(blockEntity, blockEntity.getBlockPos(), getRotationAxisOf(blockEntity)), light)
                .renderInto(poseStack.last(), consumer);
    }

    public static Direction.Axis getRotationAxisOf(final KineticBlockEntity blockEntity) {
        return com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
    }

    public static float getAngleForBe(final KineticBlockEntity blockEntity, final BlockPos pos, final Direction.Axis axis) {
        return com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getAngleForBe(blockEntity, pos, axis);
    }

    public static float getRotationOffsetForPosition(final KineticBlockEntity blockEntity, final BlockPos pos, final Direction.Axis axis) {
        return com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRotationOffsetForPosition(blockEntity, pos, axis);
    }

    public static BlockState shaft(final Direction.Axis axis) {
        return com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.shaft(axis);
    }

    public static SuperByteBuffer kineticRotationTransform(final SuperByteBuffer buffer, final KineticBlockEntity blockEntity,
                                                           final Direction.Axis axis, final float angle, final int light) {
        return buffer.light(light).rotateCentered(angle, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
    }

    public static void renderRotatingKineticBlock(final KineticBlockEntity blockEntity, final BlockState state,
                                                  final PoseStack poseStack, final VertexConsumer consumer, final int light) {
        kineticRotationTransform(CachedBuffers.block(com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KINETIC_BLOCK, state),
                blockEntity, getRotationAxisOf(blockEntity), getAngleForBe(blockEntity, blockEntity.getBlockPos(), getRotationAxisOf(blockEntity)), light)
                .renderInto(poseStack.last(), consumer);
    }
}
