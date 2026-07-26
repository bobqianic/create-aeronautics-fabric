package dev.simulated_team.simulated.content.blocks.torsion_spring;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class TorsionSpringRenderer extends KineticBlockEntityRenderer<TorsionSpringBlockEntity> {
    public TorsionSpringRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final TorsionSpringBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final Direction facing = be.getBlockState().getValue(TorsionSpringBlock.FACING);

        final SuperByteBuffer spring = CachedBuffers.partial(SimPartialModels.TORSION_SPRING, be.getBlockState());
        final float angle = be.interpolatedSpring(partialTicks);
        kineticRotationTransform(spring, be, facing.getAxis(), Mth.DEG_TO_RAD * angle, light);
        if (facing.getAxis().isHorizontal()) {
            spring.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())), Direction.UP);
        }
        spring.rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)), Direction.EAST);
        spring.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));

        final SuperByteBuffer shaftOut = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), facing);
        kineticRotationTransform(shaftOut, be, facing.getAxis(), getAngleForBe(be.getExtraKinetics(), be.getBlockPos(), facing.getAxis()), light);
        shaftOut.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final TorsionSpringBlockEntity be, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(BearingBlock.FACING)
                .getOpposite());
    }
}
