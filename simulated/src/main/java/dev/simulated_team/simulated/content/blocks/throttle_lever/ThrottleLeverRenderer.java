package dev.simulated_team.simulated.content.blocks.throttle_lever;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimPartialModels;
import net.minecraft.client.renderer.ShapeRenderer;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ThrottleLeverRenderer extends SafeBlockEntityRenderer<ThrottleLeverBlockEntity> {

    protected static final double ANGLE_LIMIT = 40.0;

    public ThrottleLeverRenderer(final BlockEntityRendererProvider.Context context) {
        
        super(context);
        
    }

    public static void transformHandleExternal(final ThrottleLeverBlockEntity blockEntity, final float partialTicks, final PoseStack ms) {
        final float state = blockEntity.clientAngle.getValue(partialTicks);
        final AttachFace face = blockEntity.getBlockState().getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
        float angle = (float) (((state / 15) * (ANGLE_LIMIT * 2) - ANGLE_LIMIT) / 180 * Math.PI);

        if (face == AttachFace.WALL) {
            angle = -angle;
        }

        final PoseTransformStack stack = TransformStack.of(ms);
        transform(stack, blockEntity.getBlockState());
        stack
                .translate(1 / 2f, 3.0 / 16.0, 1 / 2f)
                .rotateX(angle)
                .translateBack(1 / 2f, 3.0 / 16.0, 1 / 2f);
    }

    @Override
    protected void renderSafe(final ThrottleLeverBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource,
                              final int light, final int overlay) {
        final BlockState leverState = be.getBlockState();
        final float state = be.clientAngle.getValue(partialTicks);
        final AttachFace face = be.getBlockState().getValue(FaceAttachedHorizontalDirectionalBlock.FACE);
        float angle = (float) (((state / 15) * (ANGLE_LIMIT * 2) - ANGLE_LIMIT) / 180 * Math.PI);

        if (face == AttachFace.WALL) {
            angle = -angle;
        }

        if (isRenderingInSubLevel() || !VisualizationManager.supportsVisualization(be.getLevel())) {
            final VertexConsumer vb = bufferSource.getBuffer(RenderType.cutoutMipped());

            final SuperByteBuffer handle = CachedBuffers.partial(SimPartialModels.THROTTLE_LEVER_HANDLE, leverState);
            final SuperByteBuffer button = CachedBuffers.partial(SimPartialModels.THROTTLE_LEVER_BUTTON, leverState);

            final float signalStrength = Math.max(0, be.state / 15F);
            final SuperByteBuffer diode = CachedBuffers.partial(SimPartialModels.THROTTLE_LEVER_DIODE, leverState);
            final int color = SimColors.redstone(signalStrength);

            final double buttonAngle = be.clientPressedLerp.getValue(partialTicks) * -7f;

            transform(handle, leverState);
            transform(button, leverState);
            transform(diode, leverState);

            this.transformHandleExternal(handle, angle, face);
            handle
                    .light(light)
                    .renderInto(ms.last(), vb);

            this.transformHandleExternal(button, angle, face)
                    .translate(0, 14 / 16f, 8 / 16f)
                    .rotateXDegrees((float) buttonAngle)
                    .translateBack(0, 14 / 16f, 8 / 16f)
                    .light(light)
                    .renderInto(ms.last(), vb);

            diode.light(light)
                    .color(color)
                    .renderInto(ms.last(), vb);
        }

        final Minecraft minecraft = Minecraft.getInstance();

        if (!be.isVirtual() && minecraft.hitResult instanceof final BlockHitResult hitResult && hitResult.getBlockPos().equals(be.getBlockPos())) {
            renderOutline(be, ms, bufferSource, angle);
        }
    }

    private static void renderOutline(final ThrottleLeverBlockEntity be, final PoseStack ms, final MultiBufferSource bufferSource, final float angle) {
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
        final VoxelShape leverShape = SimBlocks.THROTTLE_LEVER.get().getHandleShape(SimBlocks.THROTTLE_LEVER.getDefaultState());

        ms.pushPose();
        final PoseTransformStack stack = TransformStack.of(ms);
        transform(stack, be.getBlockState());
        stack
                .translate(1 / 2f, 3.0 / 16.0, 1 / 2f)
                .rotateX(angle)
                .translateBack(1 / 2f, 3.0 / 16.0, 1 / 2f);
        ShapeRenderer.renderShape(ms, consumer, leverShape, 0.0, 0.0, 0.0, 0x66000000);
        ms.popPose();
    }

    private <T extends TransformStack<T>> TransformStack<T> transformHandleExternal(final TransformStack<T> buffer, final float angle, final AttachFace face) {
        return buffer
                .translate(1 / 2f, 3 / 16f, 1 / 2f)
                .rotateX(angle)
                .translateBack(1 / 2f, 3 / 16f, 1 / 2f)
                .rotateCentered(face == AttachFace.WALL ? (float) Math.PI : 0.0f, Direction.UP);
    }

    private static  <T extends TransformStack<T>> TransformStack<T> transform(final TransformStack<T> buffer, final BlockState leverState) {
        final AttachFace attached = leverState.getValue(AnalogLeverBlock.FACE);
        final Direction facing = leverState.getValue(AnalogLeverBlock.FACING);

        final float rX;
        switch (attached) {
            case FLOOR -> rX = 0;
            case WALL -> rX = 90;
            default -> rX = 180;
        }

        final float rY = AngleHelper.horizontalAngle(facing);
        buffer.rotateCentered((float) (rY / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) (rX / 180 * Math.PI), Direction.EAST);
        buffer.rotateCentered(attached == AttachFace.CEILING ? (float) Math.PI : 0.0f, Direction.UP);
        return buffer;
    }

}
