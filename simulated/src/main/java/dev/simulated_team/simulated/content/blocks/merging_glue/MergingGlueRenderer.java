package dev.simulated_team.simulated.content.blocks.merging_glue;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.content.blocks.spring.SpringBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class MergingGlueRenderer extends SmartBlockEntityRenderer<MergingGlueBlockEntity> {
    public MergingGlueRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final MergingGlueBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource, final int light, final int overlay) {
        if (!be.isController()) {
            return;
        }

        final MergingGlueBlockEntity other = be.getPartnerGlue();
        if (other == null) {
            return;
        }

        final SubLevel otherSubLevel = Sable.HELPER.getContainingClient(other);
        final SubLevel subLevel = Sable.HELPER.getContainingClient(be);

        final BlockPos blockPos = be.getBlockPos();

        final Vector3dc center = be.getCenter(new Vector3d());
        final Vector3d otherCenter = other.getCenter(new Vector3d());

        final BlockState state = be.getBlockState();
        final Direction facing = state.getValue(SpringBlock.FACING);
        final Direction otherFacing = other.getBlockState().getValue(SpringBlock.FACING);
        final Vector3dc normalA = JOMLConversion.atLowerCornerOf(facing.getUnitVec3i());
        final Vector3d normalB = JOMLConversion.atLowerCornerOf(otherFacing.getUnitVec3i());

        final VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutout(Simulated.path("textures/block/merging_glue/strand.png")));

        final Pose3dc renderPose = subLevel != null ? ((ClientSubLevel) subLevel).renderPose() : null;
        final Pose3dc otherRenderPose = otherSubLevel != null ? ((ClientSubLevel) otherSubLevel).renderPose() : null;

        // if one is horizontal, both glue must be horizontal
        final boolean horizontal = facing.getAxis().isHorizontal();

        final Vector3dc rightA = horizontal ?
                JOMLConversion.atLowerCornerOf(facing.getClockWise().getUnitVec3i(), new Vector3d()) :
                OrientedBoundingBox3d.FORWARD;

        final Vector3d rightB = horizontal ?
                JOMLConversion.atLowerCornerOf(otherFacing.getCounterClockWise().getUnitVec3i(), new Vector3d()) :
                new Vector3d(OrientedBoundingBox3d.FORWARD);

        final Vector3dc upA = horizontal ?
                new Vector3d(0.0, 1.0, 0.0)
                : OrientedBoundingBox3d.RIGHT;

        final Vector3d upB = horizontal ?
                new Vector3d(0.0, 1.0, 0.0)
                : new Vector3d(OrientedBoundingBox3d.RIGHT);

        if (otherRenderPose != null) {
            otherRenderPose.transformNormal(normalB);
            otherRenderPose.transformNormal(rightB);
            otherRenderPose.transformNormal(upB);
            otherRenderPose.transformPosition(otherCenter);
        }

        if (renderPose != null) {
            renderPose.transformNormalInverse(normalB);
            renderPose.transformNormalInverse(rightB);
            renderPose.transformNormalInverse(upB);
            renderPose.transformPositionInverse(otherCenter);
        }


        final Vector3d strandCenterA = center.sub(JOMLConversion.atLowerCornerOf(blockPos), new Vector3d());
        final Vector3d strandCenterB = otherCenter.sub(JOMLConversion.atLowerCornerOf(blockPos), new Vector3d());
        final Vector3d strandPosA = new Vector3d();
        final Vector3d strandPosB = new Vector3d();

        final Vector2d[] strandPositions = {
          new Vector2d(0.25, 0.25),
          new Vector2d(0.45, 0.3),
          new Vector2d(0.6, 0.6),
          new Vector2d(0.65, 0.7)
        };

        for (int i = 0; i < 2; i++) {
            final Vector2d strandA = strandPositions[i * 2].sub(0.5, 0.5, new Vector2d()).mul(0.75);
            final Vector2d strandB = strandPositions[i * 2 + 1].sub(0.5, 0.5, new Vector2d()).mul(0.75);

            renderGlueCross(
                    strandPosA.set(strandCenterA).fma(strandA.x, rightA).fma(strandA.y, upA),
                    upA,
                    rightA,

                    strandPosB.set(strandCenterB).fma(strandB.x, rightB).fma(strandB.y, upB),
                    upB,
                    rightB,

                    buffer, ms, light);
        }

    }

    private static VertexConsumer addVertex(final VertexConsumer buffer, final Matrix4f pose, final Vector3dc pos) {
        return buffer.addVertex(pose, (float) pos.x(), (float) pos.y(), (float) pos.z());
    }
    private static void renderGlueCross(final Vector3dc posA,
                                        final Vector3dc upA,
                                        final Vector3dc rightA,
                                        final Vector3dc posB,
                                        final Vector3dc upB,
                                        final Vector3dc rightB,
                                        final VertexConsumer buffer,
                                        final PoseStack ms,
                                        final int light) {
        final Matrix4f pose = ms.last().pose();
        final Vector3d vertex = new Vector3d();

        // vertical plane & backface
        addVertex(buffer, pose, posA.fma(-0.5, upA, vertex)).setColor(0xffffffff).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posA.fma(0.5, upA, vertex)).setColor(0xffffffff).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posB.fma(0.5, upB, vertex)).setColor(0xffffffff).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posB.fma(-0.5, upB, vertex)).setColor(0xffffffff).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);

        addVertex(buffer, pose, posB.fma(-0.5, upB, vertex)).setColor(0xffffffff).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posB.fma(0.5, upB, vertex)).setColor(0xffffffff).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posA.fma(0.5, upA, vertex)).setColor(0xffffffff).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posA.fma(-0.5, upA, vertex)).setColor(0xffffffff).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);

        // horizontal plane & backface
        addVertex(buffer, pose, posA.fma(-0.5, rightA, vertex)).setColor(0xffffffff).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posA.fma(0.5, rightA, vertex)).setColor(0xffffffff).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posB.fma(0.5, rightB, vertex)).setColor(0xffffffff).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posB.fma(-0.5, rightB, vertex)).setColor(0xffffffff).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);

        addVertex(buffer, pose, posB.fma(-0.5, rightB, vertex)).setColor(0xffffffff).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posB.fma(0.5, rightB, vertex)).setColor(0xffffffff).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posA.fma(0.5, rightA, vertex)).setColor(0xffffffff).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
        addVertex(buffer, pose, posA.fma(-0.5, rightA, vertex)).setColor(0xffffffff).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0.0f, 1.0f, 0.0f);
    }
}
