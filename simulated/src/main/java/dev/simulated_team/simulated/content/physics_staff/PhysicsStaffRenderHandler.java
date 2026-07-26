package dev.simulated_team.simulated.content.physics_staff;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.mixinterface.clip_overwrite.LevelPoseProviderExtension;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.index.SimRenderTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3dc;

import java.util.List;
import java.util.UUID;

public class PhysicsStaffRenderHandler {
    private static final int[][] BOX_EDGES = {
            {0, 1}, {0, 2}, {0, 4},
            {1, 3}, {1, 5},
            {2, 3}, {2, 6},
            {3, 7},
            {4, 5}, {4, 6},
            {5, 7},
            {6, 7}
    };
    private static final int[][] BOX_FACES = {
            {1, 0, 4, 5},
            {2, 3, 7, 6},
            {6, 4, 0, 2},
            {3, 1, 5, 7},
            {2, 0, 1, 3},
            {7, 5, 4, 6}
    };
    private static final Vec3[] BOX_FACE_NORMALS = {
            new Vec3(0, -1, 0),
            new Vec3(0, 1, 0),
            new Vec3(0, 0, -1),
            new Vec3(0, 0, 1),
            new Vec3(-1, 0, 0),
            new Vec3(1, 0, 0)
    };
    private static final int SELECTION_EDGE_COLOR = 0xffffffff;
    private static final int SELECTION_FACE_COLOR = 0xffffffff;
    private static final float SELECTION_LINE_WIDTH = 1.0f / 32.0f;
    private static final double SELECTION_FACE_OFFSET = 1.0 / 128.0;

    @Nullable
    private static BlockPos hoverBlockPos = null;
    @Nullable
    private static ClientSubLevel hoverSubLevel = null;

    /**
     * Renders the selection / hovering box for the staff
     */
    public static void renderSelectionBox(final MultiBufferSource bufferSource, final PoseStack ps, final Camera camera) {
        if (OptionalShaderMods.isShaderPackActive()) {
            return;
        }

        renderSelectionBoxContents(bufferSource, ps, camera);
    }

    /**
     * Iris composites the shader-pack frame after the normal world callbacks. Render the staff's
     * world-space overlay after that composite so it is not overwritten by the final shader pass.
     */
    public static boolean renderAfterShaderComposite(final MultiBufferSource bufferSource,
                                                     final PoseStack ps, final Camera camera) {
        if (!OptionalShaderMods.isShaderPackActive()) {
            return false;
        }

        renderSelectionBoxContents(bufferSource, ps, camera);
        return true;
    }

    private static void renderSelectionBoxContents(final MultiBufferSource bufferSource, final PoseStack ps,
                                                   final Camera camera) {

        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        ps.pushPose();
        SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.onRender(ps, bufferSource);
        ps.popPose();

        if (minecraft.options.hideGui) {
            return;
        }

        if (!PhysicsStaffItem.isHolding(player)) {
            return;
        }

        final Vec3 cameraPos = camera.getPosition();

        final Level level = player.level();
        renderAllLocks(bufferSource, ps, level, cameraPos);

        updateHoverPos(minecraft, player);

        if (hoverBlockPos != null && hoverSubLevel != null) {
            renderTransformedSelectionBox(bufferSource, ps, cameraPos, hoverSubLevel, hoverBlockPos,
                    minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        }
    }

    /**
     * Updates the hovered block position
     */
    private static void updateHoverPos(final Minecraft minecraft, final LocalPlayer player) {
        final ClientLevel level = minecraft.level;
        final float partialTicks = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);

        hoverBlockPos = null;
        hoverSubLevel = null;

        final PhysicsStaffClientHandler.ClientDragSession dragSession = SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.getDragSession();

        if (dragSession != null) {
            final Vector3dc localAnchor = dragSession.dragLocalAnchor();
            hoverBlockPos = BlockPos.containing(localAnchor.x(), localAnchor.y(), localAnchor.z());
            if (dragSession.dragSubLevel() instanceof final ClientSubLevel clientSubLevel) {
                hoverSubLevel = clientSubLevel;
            }
            return;
        }

        final LevelPoseProviderExtension extension = (LevelPoseProviderExtension) level;
        extension.sable$pushPoseSupplier(x -> ((ClientSubLevel) x).renderPose(partialTicks));
        final HitResult hit;
        try {
            hit = player.pick(PhysicsStaffItem.RANGE, partialTicks, false);
        } finally {
            extension.sable$popPoseSupplier();
        }

        if (!(hit instanceof final BlockHitResult blockHitResult) || blockHitResult.getType() == HitResult.Type.MISS) {
            return;
        }

        final SubLevel subLevel = Sable.HELPER.getContaining(level, blockHitResult.getBlockPos());
        if (!(subLevel instanceof final ClientSubLevel clientSubLevel)) {
            return;
        }

        hoverBlockPos = blockHitResult.getBlockPos();
        hoverSubLevel = clientSubLevel;
    }

    private static void renderTransformedSelectionBox(final MultiBufferSource bufferSource, final PoseStack ps,
                                                      final Vec3 cameraPos, final ClientSubLevel subLevel,
                                                      final BlockPos blockPos, final float partialTicks) {
        final double minX = blockPos.getX();
        final double minY = blockPos.getY();
        final double minZ = blockPos.getZ();
        final double maxX = minX + 1.0;
        final double maxY = minY + 1.0;
        final double maxZ = minZ + 1.0;
        final Pose3dc renderPose = subLevel.renderPose(partialTicks);
        final Vec3[] localCorners = createCorners(minX, minY, minZ, maxX, maxY, maxZ);

        final VertexConsumer faceBuffer = bufferSource.getBuffer(SimRenderTypes.staffSelectionFace());
        for (int faceIndex = 0; faceIndex < BOX_FACES.length; faceIndex++) {
            final Vec3 localNormal = BOX_FACE_NORMALS[faceIndex];
            final Vec3 faceOffset = localNormal.scale(SELECTION_FACE_OFFSET);
            final Vec3[] faceVertices = new Vec3[4];
            for (int vertex = 0; vertex < faceVertices.length; vertex++) {
                faceVertices[vertex] = renderPose
                        .transformPosition(localCorners[BOX_FACES[faceIndex][vertex]].add(faceOffset))
                        .subtract(cameraPos);
            }
            final Vec3 worldNormal = renderPose.transformNormal(localNormal);
            bufferFaceQuad(ps.last(), faceBuffer, faceVertices, worldNormal, SELECTION_FACE_COLOR);
        }

        final VertexConsumer edgeBuffer = bufferSource.getBuffer(SimRenderTypes.staffSelectionEdge());
        final double halfWidth = SELECTION_LINE_WIDTH * 0.5;
        for (final int[] edge : BOX_EDGES) {
            final Vec3 start = localCorners[edge[0]];
            final Vec3 end = localCorners[edge[1]];
            bufferCuboid(ps.last(), edgeBuffer, renderPose, cameraPos,
                    Math.min(start.x, end.x) - halfWidth,
                    Math.min(start.y, end.y) - halfWidth,
                    Math.min(start.z, end.z) - halfWidth,
                    Math.max(start.x, end.x) + halfWidth,
                    Math.max(start.y, end.y) + halfWidth,
                    Math.max(start.z, end.z) + halfWidth);
        }
    }

    private static Vec3[] createCorners(final double minX, final double minY, final double minZ,
                                        final double maxX, final double maxY, final double maxZ) {
        final Vec3[] corners = new Vec3[8];
        for (int corner = 0; corner < corners.length; corner++) {
            corners[corner] = new Vec3(
                    (corner & 4) == 0 ? minX : maxX,
                    (corner & 2) == 0 ? minY : maxY,
                    (corner & 1) == 0 ? minZ : maxZ);
        }
        return corners;
    }

    private static void bufferCuboid(final PoseStack.Pose pose, final VertexConsumer buffer,
                                     final Pose3dc renderPose, final Vec3 cameraPos,
                                     final double minX, final double minY, final double minZ,
                                     final double maxX, final double maxY, final double maxZ) {
        final Vec3[] corners = createCorners(minX, minY, minZ, maxX, maxY, maxZ);
        for (int corner = 0; corner < corners.length; corner++) {
            corners[corner] = renderPose.transformPosition(corners[corner]).subtract(cameraPos);
        }
        for (final int[] face : BOX_FACES) {
            bufferEdgeQuad(pose, buffer, new Vec3[] {
                    corners[face[0]], corners[face[1]], corners[face[2]], corners[face[3]]
            }, SELECTION_EDGE_COLOR);
        }
    }

    private static void bufferFaceQuad(final PoseStack.Pose pose, final VertexConsumer buffer,
                                       final Vec3[] vertices, final Vec3 normal, final int color) {
        addSelectionVertex(pose, buffer, vertices[0], normal, color, 0.0f, 0.0f);
        addSelectionVertex(pose, buffer, vertices[1], normal, color, 0.0f, 1.0f);
        addSelectionVertex(pose, buffer, vertices[2], normal, color, 1.0f, 1.0f);
        addSelectionVertex(pose, buffer, vertices[3], normal, color, 1.0f, 0.0f);
    }

    private static void bufferEdgeQuad(final PoseStack.Pose pose, final VertexConsumer buffer,
                                       final Vec3[] vertices, final int color) {
        for (final Vec3 vertex : vertices) {
            buffer.addVertex(pose, (float) vertex.x, (float) vertex.y, (float) vertex.z)
                    .setColor(color);
        }
    }

    private static void addSelectionVertex(final PoseStack.Pose pose, final VertexConsumer buffer,
                                           final Vec3 position, final Vec3 normal, final int color,
                                           final float u, final float v) {
        buffer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    /**
     * Renders all the locks our client is aware about
     */
    private static void renderAllLocks(final MultiBufferSource bufferSource, final PoseStack ps, final Level level, final Vec3 cameraPos) {
        final Minecraft client = Minecraft.getInstance();
        final List<UUID> locks = SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.getLocks(level);
        final SubLevelContainer container = SubLevelContainer.getContainer(level);

        for (final UUID lock : locks) {
            final SubLevel subLevel = container.getSubLevel(lock);

            if (!(subLevel instanceof final ClientSubLevel clientSubLevel)) continue;

            ps.pushPose();
            final Vector3dc renderPos = clientSubLevel.renderPose().position();
            ps.translate(renderPos.x() - cameraPos.x(), renderPos.y() - cameraPos.y(), renderPos.z() - cameraPos.z());
            ps.mulPose(client.gameRenderer.getMainCamera().rotation());

            final VertexConsumer buffer = bufferSource.getBuffer(SimRenderTypes.lock());

            final PoseStack.Pose pose = ps.last();
            final int color = 0xffffffff;
            buffer.addVertex(pose, 0.0f - 0.5f, 0.0f - 0.5f, 0.0f).setColor(color).setUv(0.0f, 1.0f).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, 0.0f - 0.5f, 1.0f - 0.5f, 0.0f).setColor(color).setUv(0.0f, 0.0f).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, 1.0f - 0.5f, 1.0f - 0.5f, 0.0f).setColor(color).setUv(1.0f, 0.0f).setLight(LightTexture.FULL_BRIGHT);
            buffer.addVertex(pose, 1.0f - 0.5f, 0.0f - 0.5f, 0.0f).setColor(color).setUv(1.0f, 1.0f).setLight(LightTexture.FULL_BRIGHT);

            ps.popPose();
        }
    }

}
