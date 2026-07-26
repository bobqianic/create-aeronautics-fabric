package dev.simulated_team.simulated.util;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderData;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.framebuffer.AdvancedFbo;
import foundry.veil.impl.client.render.perspective.LevelPerspectiveCamera;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;

import java.util.Collection;

public class SimpleSubLevelGroupRenderer {
    private static final LevelPerspectiveCamera CAMERA = new LevelPerspectiveCamera();
    private static final PerspectiveProjectionMatrixBuffer PROJECTION = new PerspectiveProjectionMatrixBuffer("Simulated diagram projection");
    private static final Matrix4f TRANSFORM = new Matrix4f();
    public static boolean RENDERING_SIMPLE = false;

    /**
     * @return the chain of sub-levels that should render with a given sub-level into a diagram
     */
    public static Collection<ClientSubLevel> getRenderedChain(final ClientSubLevel subLevel) {
        final ObjectOpenHashSet<ClientSubLevel> visited = new ObjectOpenHashSet<>();
        final ObjectOpenHashSet<ClientSubLevel> frontier = new ObjectOpenHashSet<>();

        frontier.add(subLevel);

        while (!frontier.isEmpty()) {
            final ClientSubLevel current = frontier.iterator().next();

            frontier.remove(current);
            visited.add(current);

            final Iterable<SubLevel> intersecting = Sable.HELPER.getAllIntersecting(current.getLevel(), new BoundingBox3d(current.boundingBox()));

            // Intersecting dependencies
            for (final SubLevel neighbor : intersecting) {
                final ClientSubLevel serverNeighbor = (ClientSubLevel) neighbor;

                if (!visited.contains(serverNeighbor)) {
                    frontier.add(serverNeighbor);
                }
            }
        }

        return visited;
    }

    public static void renderChain(final SubLevel subLevel, final AdvancedFbo fbo, final Matrix4f modelView, final Matrix4f projectionMat, final Vector3d cameraPosition, final Quaternionf orientation, final float partialTicks) {
        final ClientSubLevel clientSubLevel = (ClientSubLevel) subLevel;
        final ClientLevel level = clientSubLevel.getLevel();
        final Collection<ClientSubLevel> subLevels = SimpleSubLevelGroupRenderer.getRenderedChain(clientSubLevel);

        renderGroup(level, subLevels, fbo, modelView, projectionMat, cameraPosition, orientation, partialTicks, true);
    }

    public static void renderGroup(final ClientLevel level, final Collection<ClientSubLevel> subLevels, final AdvancedFbo fbo, final Matrix4f modelView, final Matrix4f projectionMat, final Vector3d cameraPosition, final Quaternionf orientation, final float partialTicks, final boolean renderPlayers) {
        // Finish anything previously being rendered for safety
        final MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource.endBatch();

        if (subLevels.isEmpty()) {
            AdvancedFbo.unbind();
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        final LightTexture lightTexture = minecraft.gameRenderer.lightTexture();
        final BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();

        CAMERA.setup(cameraPosition, null, minecraft.level, orientation, 0f);

        final PoseStack poseStack = new PoseStack();
        poseStack.mulPose(TRANSFORM.set(modelView));
        poseStack.mulPose(CAMERA.rotation());

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(PROJECTION.getBuffer(projectionMat), ProjectionType.ORTHOGRAPHIC);

        final Matrix4fStack matrix4fstack = RenderSystem.getModelViewStack();
        matrix4fstack.pushMatrix();
        matrix4fstack.identity();
        matrix4fstack.mul(poseStack.last().pose());

        final AdvancedFbo drawFbo = VeilRenderSystem.renderer().getDynamicBufferManger().getDynamicFbo(fbo);
        drawFbo.bind(true);

        try {
            lightTexture.turnOnLightLayer();
            SimpleSubLevelGroupRenderer.RENDERING_SIMPLE = true;

            for (final ClientSubLevel renderedSubLevel : subLevels) {
                final SubLevelRenderData renderData = renderedSubLevel.getRenderData();
                final Vector3d chunkOffset = renderData.getChunkOffset();
                final PoseStack blockPoseStack = new PoseStack();
                blockPoseStack.mulPose(renderData.getTransformation(cameraPosition.x, cameraPosition.y, cameraPosition.z));
                blockPoseStack.translate(chunkOffset.x, chunkOffset.y, chunkOffset.z);

                final var bounds = renderedSubLevel.getPlot().getBoundingBox();
                for (final BlockPos blockPos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
                    final BlockState blockState = renderedSubLevel.getLevel().getBlockState(blockPos);
                    if (blockState.isAir()) {
                        continue;
                    }

                    blockPoseStack.pushPose();
                    blockPoseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                    blockRenderer.renderSingleBlock(blockState, blockPoseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                    blockPoseStack.popPose();
                }
            }

            bufferSource.endBatch();
            SimpleSubLevelGroupRenderer.RENDERING_SIMPLE = false;
        } finally {
            SimpleSubLevelGroupRenderer.RENDERING_SIMPLE = false;

            matrix4fstack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
            AdvancedFbo.unbind();

            lightTexture.updateLightTexture(partialTicks);
        }
    }
}
