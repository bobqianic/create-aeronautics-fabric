package dev.simulated_team.simulated.content.blocks.rope.strand.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimMathUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Objects;

public class RopeStrandRenderer {
    public record RopeRenderPoint(Quaternionf orientation, Vector3d position) { }

    public static void render(final SmartBlockEntity be, final RopeStrandHolderBehavior ropeHolder, final float partialTick, final PoseStack ps, final MultiBufferSource buffer) {

        final Level level = be.getLevel();
        assert level != null;

        final BlockPos ownerPos = be.getBlockPos();

        final SuperByteBuffer middle = CachedBuffers.partialFacing(SimPartialModels.ROPE, AllBlocks.ROPE.defaultBlockState(), Direction.NORTH);
        final SuperByteBuffer knot = CachedBuffers.partialFacing(SimPartialModels.ROPE_KNOT, AllBlocks.ROPE.defaultBlockState(), Direction.NORTH);
        final VertexConsumer vb = buffer.getBuffer(RenderType.solid());

        final SubLevel subLevel = Sable.HELPER.getContaining(be);
        Pose3dc containingPose = null;

        if (subLevel instanceof final ClientSubLevel clientSubLevel) {
            containingPose = clientSubLevel.renderPose();
        }

        final ClientRopeStrand rope = ropeHolder.getClientStrand();

        final float rad = 3.0f / 16.0f;

        if (ropeHolder.ownsRope() && rope != null && ropeHolder.getClientStrand() != null) {
            final ClientRopeStrand clientStrand = ropeHolder.getClientStrand();

            final List<ClientRopePoint> points = clientStrand.getPoints();

            if (points.size() <= 1) {
                return;
            }

            final ObjectArrayList<RopeRenderPoint> ropeRenderPoints = buildRenderPoints(partialTick, points);

            if (ropeRenderPoints.isEmpty()) return;

            ps.pushPose();
            for (int i = 1; i < ropeRenderPoints.size(); i++) {
                final RopeRenderPoint renderPoint0 = ropeRenderPoints.get(i - 1);
                final RopeRenderPoint renderPoint1 = ropeRenderPoints.get(i);
                final Vector3d globalRenderPos = new Vector3d(renderPoint0.position());
                final Vector3d renderPos = renderPoint0.position();
                final Quaternionf orientation = renderPoint0.orientation();

                final double length = renderPoint1.position().distance(renderPoint0.position());

                if (containingPose != null) {
                    containingPose.transformPositionInverse(renderPos);
                    orientation.premul(new Quaternionf(containingPose.orientation()).conjugate());
                }

                ps.pushPose();
                ps.translate(renderPos.x - (ownerPos.getX()), renderPos.y - (ownerPos.getY()), renderPos.z - (ownerPos.getZ()));
                ps.mulPose(orientation);
                ps.translate(-0.5, -0.5, -0.5);
                final BlockPos pos = BlockPos.containing(globalRenderPos.x, globalRenderPos.y, globalRenderPos.z);
                final int worldLight = LevelRenderer.getLightColor(level, pos);

                if (i > 1) {
                    knot.light(worldLight)
                            .renderInto(ps.last(), vb);
                }
                ps.translate(0.0, 0.5, 0.0);
                ps.scale(1.0f, (float) length, 1.0f);

                middle.light(worldLight)
                        .renderInto(ps.last(), vb);
                ps.popPose();
            }
            ps.popPose();

            final RopeRenderPoint last = ropeRenderPoints.getLast();
            if (containingPose != null) {
                final Vector3d renderPos = last.position();
                final Quaternionf orientation = last.orientation();

                containingPose.transformPositionInverse(renderPos);
                orientation.premul(new Quaternionf(containingPose.orientation()).conjugate());
            }

            if (Objects.equals(ZiplineClientManager.hoveringRope, clientStrand.getUuid())) {
                renderOutline(ps, buffer, rad, ropeRenderPoints, ownerPos);
            }
        }
    }

    private static void renderOutline(final PoseStack ps, final MultiBufferSource buffer, final float rad, final ObjectArrayList<RopeRenderPoint> ropeRenderPoints, final BlockPos ownerPos) {
        final Vector3d previousCorner = new Vector3d();
        final Vector3d currentCorner = new Vector3d();
        final Vector3d cornerDiff = new Vector3d();

        final Vector3d[] ropeCorners = {
                new Vector3d(-rad, 0, -rad),
                new Vector3d(-rad, 0, rad),
                new Vector3d(rad, 0, rad),
                new Vector3d(rad, 0, -rad),
        };

        final VertexConsumer linesVB = buffer.getBuffer(RenderType.lines());
        final Matrix4f pose = ps.last().pose();

        for (int i = 0; i < ropeRenderPoints.size() + 1; i++) {
            final RopeRenderPoint renderPoint0 = ropeRenderPoints.get(Math.max(0, i - 1));
            final RopeRenderPoint renderPoint1 = ropeRenderPoints.get(Math.min(ropeRenderPoints.size() - 1, i));
                final boolean start = i == 0;
                final boolean end = i == ropeRenderPoints.size();

            for (final Vector3d ropeCorner : ropeCorners) {
                renderPoint0.orientation().transform(start ? ropeCorner.rotateY(Math.PI / 2.0, previousCorner) : ropeCorner, previousCorner)
                        .add(renderPoint0.position())
                        .sub(ownerPos.getX(), ownerPos.getY(), ownerPos.getZ());

                renderPoint1.orientation().transform(end ? ropeCorner.rotateY(Math.PI / 2.0, ropeCorner) : ropeCorner, currentCorner)
                        .add(renderPoint1.position())
                        .sub(ownerPos.getX(), ownerPos.getY(), ownerPos.getZ());

                currentCorner.sub(previousCorner, cornerDiff).normalize();

                linesVB.addVertex(pose, (float) previousCorner.x, (float) previousCorner.y, (float) previousCorner.z)
                        .setColor(0f, 0f, 0f, .4f)
                        .setNormal(ps.last(), (float) cornerDiff.x, (float) cornerDiff.y, (float) cornerDiff.z);

                linesVB.addVertex(pose, (float) currentCorner.x, (float) currentCorner.y, (float) currentCorner.z)
                        .setColor(0f, 0f, 0f, .4f)
                        .setNormal(ps.last(), (float) cornerDiff.x, (float) cornerDiff.y, (float) cornerDiff.z);
            }
        }
    }

    private static @NotNull ObjectArrayList<RopeRenderPoint> buildRenderPoints(final float partialTick, final List<ClientRopePoint> inputPoints) {
        final ObjectArrayList<RopeRenderPoint> ropeRenderPoints = new ObjectArrayList<>();
        final ObjectArrayList<ClientRopePoint> points = new ObjectArrayList<>(inputPoints);

        while (points.size() >= 2 && points.getFirst().position().distanceSquared(points.get(1).position()) < 1e-3) {
            points.removeFirst();
        }

        if (points.size() <= 1) {
            return new ObjectArrayList<>();
        }

        final Vector3dc pointZeroPosition = points.get(0).renderPos(partialTick, new Vector3d());
        final Vector3dc pointOnePosition = points.get(1).renderPos(partialTick, new Vector3d());

        final Vector3d normal = pointOnePosition.sub(pointZeroPosition, new Vector3d()).normalize();

        final Quaternionf runningRotation;
        if (normal.dot(OrientedBoundingBox3d.UP) < 0) {
            runningRotation = SimMathUtils.getQuaternionfFromVectorRotation(new Vector3d(0, -1, 0), normal);
            runningRotation.rotateZ((float) Math.PI);
        } else {
            runningRotation = SimMathUtils.getQuaternionfFromVectorRotation(new Vector3d(0, 1, 0), normal);
        }

        ropeRenderPoints.add(new RopeRenderPoint(new Quaternionf(runningRotation), new Vector3d(pointZeroPosition)));

        final Vector3d runningNormal = new Vector3d();

        final Vector3d bPos = new Vector3d();
        final Vector3d aPos = new Vector3d();

        for (int i = 2; i < points.size(); i++) {
            final ClientRopePoint pointA = points.get(i - 1);
            final ClientRopePoint pointB = points.get(i);

            runningNormal.set(pointB.renderPos(partialTick, bPos))
                    .sub(pointA.renderPos(partialTick, aPos))
                    .normalize();

            if (runningNormal.dot(OrientedBoundingBox3d.UP) < -0.15) {
                runningRotation.set(SimMathUtils.getQuaternionfFromVectorRotation(new Vector3d(0, -1, 0), runningNormal));
                runningRotation.rotateZ((float) Math.PI);
            } else {
                runningRotation.set(SimMathUtils.getQuaternionfFromVectorRotation(new Vector3d(0, 1, 0), runningNormal));
            }

            ropeRenderPoints.add(new RopeRenderPoint(new Quaternionf(runningRotation), pointA.renderPos(partialTick, new Vector3d())));
            normal.set(runningNormal);
        }

        ropeRenderPoints.add(new RopeRenderPoint(new Quaternionf(runningRotation), points.getLast().renderPos(partialTick, new Vector3d())));
        return ropeRenderPoints;
    }
}
