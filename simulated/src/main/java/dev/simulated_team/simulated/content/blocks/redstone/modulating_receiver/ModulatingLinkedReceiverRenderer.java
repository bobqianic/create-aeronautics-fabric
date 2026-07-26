package dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver;


import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import static dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkVisual.MAX_DISTANCE;
import static dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver.ModulatingLinkVisual.SMOOTHING;


public class ModulatingLinkedReceiverRenderer extends SmartBlockEntityRenderer<ModulatingLinkedReceiverBlockEntity> {

    public ModulatingLinkedReceiverRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final ModulatingLinkedReceiverBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, bufferSource, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        super.renderSafe(be, partialTicks, ms, bufferSource, light, overlay);
        final Direction facing = be.getBlockState()
                .getValue(BlockStateProperties.FACING);

        final Vec3 pixelNormal = new Vec3(facing.step()).scale(1 / 16.0);

        final float minPos = 5.5f * ((be.minRange - 1) * (SMOOTHING + MAX_DISTANCE - 1)) / ((MAX_DISTANCE - 1) * (SMOOTHING + be.minRange - 1));
        final float maxPos = 5.5f * ((be.maxRange - 1) * (SMOOTHING + MAX_DISTANCE - 1)) / ((MAX_DISTANCE - 1) * (SMOOTHING + be.maxRange - 1));

        for (final boolean bottom : Iterate.trueAndFalse) {
            final SuperByteBuffer superBuffer = CachedBuffers.partial(SimPartialModels.MODULATING_RECEIVER_PLATE, be.getBlockState());

            if (bottom) {
                superBuffer.translate(pixelNormal.scale(minPos));
            } else {
                superBuffer.translate(pixelNormal.scale(0.5 + maxPos));
            }

            if (facing.getAxis().isHorizontal()) {
                superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())), Direction.UP);
            }

            superBuffer.rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)), Direction.EAST);

            superBuffer.light(light);
            superBuffer.renderInto(ms.last(), bufferSource.getBuffer(RenderType.solid()));
        }
    }
}
