package dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PropellerBearingRenderer extends KineticBlockEntityRenderer<PropellerBearingBlockEntity> {


    public PropellerBearingRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PropellerBearingBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
							  int light, int overlay) {

        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) return;

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final Direction facing = be.getBlockState()
                .getValue(BlockStateProperties.FACING);
        PartialModel top = AeroPartialModels.BEARING_PLATE;
        SuperByteBuffer superBuffer = CachedBuffers.partial(top, be.getBlockState());

        float interpolatedAngle = be.getInterpolatedAngle(partialTicks - 1);
        kineticRotationTransform(superBuffer, be, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), light);

        if (facing.getAxis()
                .isHorizontal())
            superBuffer.rotateCentered(
                    AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())),Direction.UP);
        superBuffer.rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)),Direction.EAST);
        superBuffer.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(PropellerBearingBlockEntity te, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(BearingBlock.FACING)
                .getOpposite());
    }
}
