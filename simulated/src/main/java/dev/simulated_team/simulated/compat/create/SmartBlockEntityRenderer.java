package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SmartBlockEntityRenderer<T extends net.minecraft.world.level.block.entity.BlockEntity>
        implements BlockEntityRenderer<T, SmartBlockEntityRenderer.RenderState<T>> {
    private final com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer<com.zurrtum.create.foundation.blockEntity.SmartBlockEntity,
            com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.SmartRenderState> createRenderer;
    private boolean renderingInSubLevel;

    public SmartBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        createRenderer = new com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer<>(context);
    }

    @Override
    public RenderState<T> createRenderState() {
        return new RenderState<>();
    }

    @Override
    public void extractRenderState(final T blockEntity, final RenderState<T> state, final float partialTicks, final Vec3 cameraPos,
                                   @Nullable final ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        if (blockEntity instanceof final com.zurrtum.create.foundation.blockEntity.SmartBlockEntity smartBlockEntity) {
            createRenderer.updateRenderState(smartBlockEntity, state.createState, partialTicks, cameraPos, crumblingOverlay);
        }
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(final RenderState<T> state, final PoseStack poseStack, final SubmitNodeCollector queue, final CameraRenderState cameraState) {
        if (state.blockEntity instanceof com.zurrtum.create.foundation.blockEntity.SmartBlockEntity) {
            createRenderer.render(state.createState, poseStack, queue, cameraState);
        }
        if (state.blockEntity == null || state.blockEntity.isRemoved() || shouldSkipLegacyRender(state.blockEntity)) {
            return;
        }

        RenderBridge.submit(poseStack, queue, (legacyPose, buffers) -> renderSafe(state.blockEntity, state.partialTicks, legacyPose,
                buffers, state.lightCoords, OverlayTexture.NO_OVERLAY));
    }

    protected boolean shouldSkipLegacyRender(final T blockEntity) {
        return false;
    }

    protected void renderSafe(final T blockEntity, final float partialTicks, final PoseStack poseStack,
                              final MultiBufferSource bufferSource, final int light, final int overlay) {
    }

    /**
     * Runs only when an individual block-entity type has opted into Sable's
     * immediate moving-sublevel renderer.
     */
    public final void renderExplicitlyInSubLevel(final T blockEntity, final float partialTicks, final PoseStack poseStack,
                                                 final MultiBufferSource bufferSource, final int light, final int overlay) {
        final boolean previous = renderingInSubLevel;
        renderingInSubLevel = true;
        try {
            renderSafe(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        } finally {
            renderingInSubLevel = previous;
        }
    }

    protected final boolean isRenderingInSubLevel() {
        return renderingInSubLevel;
    }

    public boolean shouldRenderOffScreen(final T blockEntity) {
        return shouldRenderOffScreen();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class RenderState<T extends net.minecraft.world.level.block.entity.BlockEntity> extends BlockEntityRenderState {
        private final com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.SmartRenderState createState =
                new com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.SmartRenderState();
        private T blockEntity;
        private float partialTicks;
    }
}
