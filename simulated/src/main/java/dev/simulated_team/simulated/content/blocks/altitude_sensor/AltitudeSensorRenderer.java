package dev.simulated_team.simulated.content.blocks.altitude_sensor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.ryanhcode.sable.util.SableDistUtil;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import org.joml.Matrix4f;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;


public class AltitudeSensorRenderer extends SmartBlockEntityRenderer<AltitudeSensorBlockEntity> {
    public AltitudeSensorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static float calculateLinearDial(final float minHeight, final float maxHeight, final float height) {
        final float fraction = (height - minHeight) / (maxHeight - minHeight);
        return Math.min(Math.max(fraction, 0), 1);
    }

    public static void render(final BlockState blockState, final int tickCount, final float dialValue, final float visualHeight,
                              final PoseStack poseStack, final PoseStack contraptionPose, final Matrix4f worldLight, final MultiBufferSource bufferSource, final int light) {
        final Level level = SableDistUtil.getClientLevel();
        final VertexConsumer vb = bufferSource.getBuffer(RenderType.cutout());
        final SuperByteBuffer indicator = CachedBuffers.partial(SimPartialModels.ALTITUDE_SENSOR_INDICATOR, blockState);

        PartialModel box = SimPartialModels.ALTITUDE_SENSOR_LINEAR_CASE;
        PartialModel dial = SimPartialModels.ALTITUDE_SENSOR_LINEAR_HAND;
        final boolean isRadial = blockState.getValue(AltitudeSensorBlock.DIAL) == AltitudeSensorBlock.FaceType.RADIAL;

        if (isRadial) {
            box = SimPartialModels.ALTITUDE_SENSOR_RADIAL_CASE;
            dial = SimPartialModels.ALTITUDE_SENSOR_RADIAL_HAND;
        }

        final SuperByteBuffer face = CachedBuffers.partial(box, blockState);
        final SuperByteBuffer dialBuffer = CachedBuffers.partial(dial, blockState);

        final Direction direction = blockState.getValue(HORIZONTAL_FACING);

        if (contraptionPose != null) {
            face.transform(contraptionPose);
            dialBuffer.transform(contraptionPose);
            indicator.transform(contraptionPose);
        }

        if (isRadial) {
            dialBuffer.rotateCentered(-(float) (visualHeight * Math.PI / 2.0), direction);
        } else {
            dialBuffer.translate(0, (dialValue * 8f - 4f) / 16f, 0);
        }

        final AttachFace attachFace = blockState.getValue(AltitudeSensorBlock.FACE);
        final float attachFaceAngle = attachFace == AttachFace.WALL ? 90 : attachFace == AttachFace.CEILING ? 180 : 0;

        final float time = tickCount + AnimationTickHolder.getPartialTicks();
        final float wobbleAngle = (float) (-Math.sin(time * 0.8) * Math.exp(-time / 3.5)) * 0.7f;

        final float yRot = !direction.getAxis().equals(Direction.Axis.Z) ?
                (float) Math.toRadians(blockState.getValue(HORIZONTAL_FACING).getOpposite().toYRot()) :
                (float) Math.toRadians(blockState.getValue(HORIZONTAL_FACING).toYRot());

        face.rotateCentered((float) (yRot + Math.PI), Direction.UP).rotateCentered(wobbleAngle, Direction.WEST);
        dialBuffer.rotateCentered((float) (yRot + Math.PI), Direction.UP).rotateCentered(wobbleAngle, Direction.WEST);
        indicator.rotateCentered((float) (yRot + Math.PI), Direction.UP).rotateCentered((float) Math.toRadians(attachFaceAngle), Direction.WEST);

        if (worldLight != null) {
            face.useLevelLight(level, new Matrix4f(worldLight));
            dialBuffer.useLevelLight(level, new Matrix4f(worldLight));
            indicator.useLevelLight(level, new Matrix4f(worldLight));
        }
        face.light(light);
        dialBuffer.light(light);
        indicator.light(light);

        final int color = SimColors.redstone(dialValue);
        indicator.color(color);

        face.renderInto(poseStack.last(), vb);
        dialBuffer.renderInto(poseStack.last(), vb);
        indicator.renderInto(poseStack.last(), vb);
    }

    @Override
    protected void renderSafe(final AltitudeSensorBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        render(be.getBlockState(), be.tickCount, be.getValue(), be.getVisualHeight(partialTicks),
                ms, null, null, buffer, light);
    }
}
