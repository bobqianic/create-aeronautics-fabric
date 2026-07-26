package dev.ryanhcode.offroad.content.blocks.borehead_bearing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BoreheadBearingRenderer extends KineticBlockEntityRenderer<BoreheadBearingBlockEntity> {
    public BoreheadBearingRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final BoreheadBearingBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              final int light, final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        final BlockState state = be.getBlockState();

        final float time = AnimationTickHolder.getRenderTime(be.getLevel());
        final Direction.Axis rotationAxis = getRotationAxisOf(be);

        for (final Direction direction : Iterate.directionsInAxis(rotationAxis)) {
            final SuperByteBuffer dirShaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, direction);
            final float offset = getRotationOffsetForPosition(be, be.getBlockPos(), rotationAxis);

            float angle = 0;

            if (be.getSpeed() != 0) {
                angle = direction.getAxisDirection().getStep() * (time * be.getSpeed() * 3f / 10) % 360;
            }

            angle += offset;
            angle = angle / 180f * (float) Math.PI;
            kineticRotationTransform(dirShaft, be, rotationAxis, angle, light);
            dirShaft.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));
        }

        final Direction facing = state.getValue(BlockStateProperties.FACING);
        final SuperByteBuffer bearingTop = CachedBuffers.partial(AllPartialModels.BEARING_TOP, state);

        final float interpolatedAngle = be.getInterpolatedAngle(partialTicks - 1);
        kineticRotationTransform(bearingTop, be, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), light);

        if (facing.getAxis().isHorizontal()) {
            bearingTop.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())), Direction.UP);
        }

        bearingTop.rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)), Direction.EAST);
        bearingTop.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected BlockState getRenderedBlockState(final BoreheadBearingBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
