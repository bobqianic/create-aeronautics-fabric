package dev.simulated_team.simulated.content.blocks.directional_gearshift;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftRenderer;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class DirectionalGearshiftRenderer extends SplitShaftRenderer {
    public DirectionalGearshiftRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderSafe(final SplitShaftBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource,
                              final int light, final int overlay) {
        renderDynamicParts(be, partialTicks, ms, bufferSource, light);
    }

    public static void renderInSubLevel(final DirectionalGearshiftBlockEntity be, final float partialTicks,
                                        final PoseStack ms, final MultiBufferSource bufferSource,
                                        final int light, final int overlay) {
        renderDynamicParts(be, partialTicks, ms, bufferSource, light);
    }

    private static void renderDynamicParts(final SplitShaftBlockEntity be, final float partialTicks,
                                           final PoseStack ms, final MultiBufferSource bufferSource,
                                           final int light) {
        final BlockState blockState = be.getBlockState();
        final Direction.Axis axis = SimBlocks.DIRECTIONAL_GEARSHIFT.get().getRotationAxis(blockState);

        final float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float angle = (time * be.getSpeed() * 3f / 10) % 360;
        float shaftAngle = 0.0f;
        float modifier = 0.0f;
        float offset = 0.0f;

        if (be.hasSource() && !blockState.getValue(DirectionalGearshiftBlock.LEFT_POWERED) && blockState.getValue(DirectionalGearshiftBlock.RIGHT_POWERED)) {
            shaftAngle = angle;
        }

        if (be.hasSource() && blockState.getValue(DirectionalGearshiftBlock.LEFT_POWERED) && !blockState.getValue(DirectionalGearshiftBlock.RIGHT_POWERED)) {
            modifier = be.getRotationSpeedModifier(be.getSourceFacing().getOpposite());
            offset = be.getRotationAngleOffset(axis);
        }

        angle *= modifier;
        angle += offset;
        angle = angle / 180f * (float) Math.PI;
        shaftAngle = shaftAngle / 180f * (float) Math.PI;

        final Direction direction = blockState.getValue(DirectionalGearshiftBlock.FACING);
        final boolean vertical = axis.isVertical() || (direction.getAxis().isVertical() && !blockState.getValue(DirectionalGearshiftBlock.AXIS_ALONG_FIRST_COORDINATE));

        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        final SuperByteBuffer barrel = CachedBuffers.partial(SimPartialModels.DIRECTIONAL_GEARSHIFT_CENTER, blockState);

        final SuperByteBuffer barrelShaftA = CachedBuffers.partial(SimPartialModels.DIRECTIONAL_GEARSHIFT_BARREL_SHAFT, blockState);
        kineticRotationTransform(barrelShaftA, be, axis, angle, light);
        barrelShaftA.center().rotateToFace(direction).uncenter();
        if(vertical) {
            barrelShaftA.rotateZCenteredDegrees(90);
        }
        barrelShaftA.rotateZCentered((float) Math.PI);
        barrelShaftA.rotateYCentered(shaftAngle);
        barrelShaftA.light(light).renderInto(ms.last(), consumer);

        final SuperByteBuffer barrelShaftB = CachedBuffers.partial(SimPartialModels.DIRECTIONAL_GEARSHIFT_BARREL_SHAFT, blockState);
        kineticRotationTransform(barrelShaftB, be, axis, angle, light);
        barrelShaftB.center().rotateToFace(direction).uncenter();
        if(vertical) {
            barrelShaftB.rotateZCenteredDegrees(90);
        }
        barrelShaftB.rotateYCentered(shaftAngle);
        barrelShaftB.light(light).renderInto(ms.last(), consumer);

        kineticRotationTransform(barrel, be, axis, angle, light);
        barrel.center().rotateToFace(direction).uncenter();

        if(vertical) {
            barrel.rotateZCenteredDegrees(90);
        }

        barrel.light(light).renderInto(ms.last(), consumer);

    }

    private static SuperByteBuffer kineticRotationTransform(SuperByteBuffer buffer, SplitShaftBlockEntity blockEntity,
                                                             Direction.Axis axis, float angle, int light) {
        return buffer.light(light);
    }

}
