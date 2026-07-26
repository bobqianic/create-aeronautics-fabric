package dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.ColoredOverlayBlockEntityRenderer;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RedstoneInductorRenderer extends ColoredOverlayBlockEntityRenderer<RedstoneInductorBlockEntity> {

    public RedstoneInductorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(final RedstoneInductorBlockEntity blockEntity,
                                   final ColoredOverlayRenderState state,
                                   final float tickProgress,
                                   final Vec3 cameraPos,
                                   @Nullable final ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.updateRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
    }

    @Override
    public void submit(final ColoredOverlayRenderState state, final PoseStack poseStack,
                       final SubmitNodeCollector queue, final CameraRenderState cameraState) {
        super.render(state, poseStack, queue, cameraState);
    }

    @Override
    protected int getColor(final RedstoneInductorBlockEntity te, final float partialTicks) {
        return getIndicatorColor(te, partialTicks);
    }

    @Override
    protected SuperByteBuffer getOverlayBuffer(final RedstoneInductorBlockEntity te,
                                               final ColoredOverlayRenderState state) {
        final SuperByteBuffer render = CachedBuffers.partial(SimPartialModels.REDSTONE_INDUCTOR_INDICATOR, te.getBlockState());
        final Direction facing = te.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return render.translate(0.5, 0, 0.5)
                .rotateYDegrees(AngleHelper.horizontalAngle(facing));
    }

    public static void renderInSubLevel(final RedstoneInductorBlockEntity be, final float partialTicks,
                                        final PoseStack poseStack, final MultiBufferSource bufferSource,
                                        final int light, final int overlay) {
        final Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        CachedBuffers.partial(SimPartialModels.REDSTONE_INDUCTOR_INDICATOR, be.getBlockState())
                .translate(0.5, 0, 0.5)
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .color(getIndicatorColor(be, partialTicks))
                .light(light)
                .renderInto(poseStack.last(), bufferSource.getBuffer(RenderType.solid()));
    }

    private static int getIndicatorColor(final RedstoneInductorBlockEntity be, final float partialTicks) {
        final float state = be.lerpedState.getValue(partialTicks);
        return SimColors.redstone(state / 15F);
    }
}
