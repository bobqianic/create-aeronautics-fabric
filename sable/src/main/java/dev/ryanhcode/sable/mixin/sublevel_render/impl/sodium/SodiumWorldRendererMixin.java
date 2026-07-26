package dev.ryanhcode.sable.mixin.sublevel_render.impl.sodium;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.api.client.SubLevelBlockEntityRenderRegistry;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.plot.LevelPlot;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import dev.ryanhcode.sable.sublevel.render.SubLevelLightVertexConsumerProvider;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bridges Sable sub-levels into Sodium's terrain render phases.
 *
 * <p>The 1.21.10 Iris/Sodium pipeline requires its own extended vertex format.
 * Sable's vanilla compiled section buffers are accepted by the GPU but discarded
 * by that shader pipeline. Render the sub-level blocks through Minecraft's
 * immediate block-model path instead; Iris decorates this path with the active
 * shader-pack vertex format just as it does for moving blocks.</p>
 */
@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class SodiumWorldRendererMixin {

    @Shadow
    private @Nullable ClientLevel level;

    @Inject(method = "setupTerrain", at = @At("TAIL"))
    private void sable$compileSubLevelSections(
            final Camera camera,
            final Viewport viewport,
            final FogParameters fogParameters,
            final boolean spectator,
            final boolean updateImmediately,
            final ChunkRenderMatrices matrices,
            final CallbackInfo ci
    ) {
        if (this.level == null) {
            return;
        }

        final ClientSubLevelContainer container = SubLevelContainer.getContainer(this.level);
        if (container == null) {
            return;
        }

        final PrioritizeChunkUpdates chunkUpdates = Minecraft.getInstance().options.prioritizeChunkUpdates().get();
        final RenderRegionCache renderRegionCache = new RenderRegionCache();
        for (final ClientSubLevel subLevel : container.getAllSubLevels()) {
            subLevel.getRenderData().compileSections(chunkUpdates, renderRegionCache, camera);
        }
    }

    @Inject(method = "scheduleRebuildForChunk", at = @At("HEAD"), cancellable = true)
    private void sable$scheduleSubLevelRebuild(
            final int sectionX,
            final int sectionY,
            final int sectionZ,
            final boolean playerChanged,
            final CallbackInfo ci
    ) {
        if (this.level == null) {
            return;
        }

        final ClientSubLevelContainer container = SubLevelContainer.getContainer(this.level);
        if (container == null) {
            return;
        }

        final LevelPlot plot = container.getPlot(sectionX, sectionZ);
        if (plot == null) {
            return;
        }

        ((ClientSubLevel) plot.getSubLevel()).getRenderData().setDirty(sectionX, sectionY, sectionZ, playerChanged);
        ci.cancel();
    }

    @Inject(method = "drawChunkLayer", at = @At("TAIL"))
    private void sable$drawSubLevelSections(
            final ChunkSectionLayerGroup group,
            final ChunkRenderMatrices matrices,
            final double cameraX,
            final double cameraY,
            final double cameraZ,
            final CallbackInfo ci
    ) {
        if (this.level == null || group != ChunkSectionLayerGroup.OPAQUE) {
            return;
        }

        final ClientSubLevelContainer container = SubLevelContainer.getContainer(this.level);
        if (container == null) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        final BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        final Renderer fabricRenderer = Renderer.get();
        final float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        // This injection runs from Sodium's opaque terrain draw, just before
        // LevelRenderer normally selects the world-lighting UBO. The immediate
        // block model pipeline consumes that UBO for directional face shading,
        // so bind it here as well instead of inheriting stale GUI/item lights.
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);

        for (final ClientSubLevel subLevel : container.getAllSubLevels()) {
            final SubLevelLightVertexConsumerProvider blockBuffers = new SubLevelLightVertexConsumerProvider(
                    this.level,
                    subLevel,
                    cameraX,
                    cameraY,
                    cameraZ,
                    RenderLayerHelper.movingDelegate(bufferSource),
                    bufferSource
            );
            final SubLevelRenderData renderData = subLevel.getRenderData();
            final Pose3dc renderPose = subLevel.renderPose(partialTick);
            final Vector3dc rotationPoint = renderPose.rotationPoint();
            final PoseStack poseStack = new PoseStack();
            poseStack.mulPose(renderData.getTransformation(cameraX, cameraY, cameraZ));
            final var bounds = subLevel.getPlot().getBoundingBox();
            for (final BlockPos blockPos : BlockPos.betweenClosed(
                    bounds.minX(), bounds.minY(), bounds.minZ(),
                    bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                final BlockState blockState = this.level.getBlockState(blockPos);
                if (blockState.isAir()) {
                    continue;
                }

                poseStack.pushPose();
                // Subtract in double precision before storing the translation in
                // the float pose matrix; plot coordinates are tens of millions of
                // blocks away and two separate translations lose local precision.
                poseStack.translate(
                        blockPos.getX() - rotationPoint.x(),
                        blockPos.getY() - rotationPoint.y(),
                        blockPos.getZ() - rotationPoint.z()
                );
                if (blockState.getRenderShape() == RenderShape.MODEL) {
                    // The terrain-like renderer computes AO and packed light for
                    // every vertex. renderSingleBlock uses one light value for the
                    // entire model, which makes nearby light sources visibly step
                    // from one sub-level block to the next.
                    fabricRenderer.render(
                            blockRenderer.getModelRenderer(),
                            this.level,
                            blockRenderer.getBlockModel(blockState),
                            blockState,
                            blockPos,
                            poseStack,
                            blockBuffers,
                            true,
                            blockState.getSeed(blockPos),
                            OverlayTexture.NO_OVERLAY
                    );
                }

                final BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
                if (blockEntity != null && !blockEntity.isRemoved()) {
                    final int plotLight = LevelRenderer.getLightColor(this.level, blockPos);
                    SubLevelBlockEntityRenderRegistry.render(
                            blockEntity,
                            partialTick,
                            poseStack,
                            blockBuffers,
                            plotLight,
                            OverlayTexture.NO_OVERLAY
                    );
                }
                poseStack.popPose();
            }
        }

        bufferSource.endBatch();
    }

}
