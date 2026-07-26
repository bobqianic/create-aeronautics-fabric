package dev.simulated_team.simulated.content.blocks.spring;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.api.math.OrientedBoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimRenderTypes;
import dev.simulated_team.simulated.util.SimColors;
import dev.simulated_team.simulated.util.SimMathUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;
import java.util.List;

public class SpringRenderer extends SmartBlockEntityRenderer<SpringBlockEntity> {
    private final Vector3d controlPointA = new Vector3d();
    private final Vector3d controlPointB = new Vector3d();
    private final Vector3d segmentALerp = new Vector3d();
    private final Vector3d segmentBLerp = new Vector3d();
    private final Vector3d segmentCLerp = new Vector3d();
    private final Vector3d startUp = new Vector3d();
    private final Vector3d endUp = new Vector3d();
    private final Vector3d startLeft = new Vector3d();
    private final Vector3d endLeft = new Vector3d();
    private final Vector3d normalizedNormal = new Vector3d();
    private final Vector3d vertex = new Vector3d();

    public SpringRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Renders the dynamic coil from Sable's Sodium moving-block fallback.
     * The normal block-entity submission queue is bypassed for those blocks.
     */
    public static void renderInSubLevel(final SpringBlockEntity be, final float partialTicks, final PoseStack ps,
                                        final MultiBufferSource bufferSource, final int light, final int overlay) {
        final Object registeredRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
        if (registeredRenderer instanceof final SpringRenderer springRenderer) {
            springRenderer.renderSafe(be, partialTicks, ps, bufferSource, light, overlay);
        }
    }

    private static int getStressColor(final SpringBlockEntity be, final float partialTicks, final Vector3d otherCenter, final Vector3dc center, final Minecraft minecraft) {
        final double distance = otherCenter.distance(center);
        final double snapDistance = be.getSnappingDistance();

        // start flashing 70% to the snap distance
        final double flashingStartExtension = Mth.lerp(0.7, (be.getRenderLength(partialTicks) - 0.75), snapDistance);

        float stressAlpha = 0.0f;
        if (distance > flashingStartExtension) {
            final double renderTime = minecraft.player.tickCount + partialTicks;
            stressAlpha = Mth.clamp((float) ((distance - flashingStartExtension) / (snapDistance - flashingStartExtension)), 0.0f, 1.0f) * 0.3f;
            stressAlpha = stressAlpha * Mth.lerp(0.25f, (float) Math.sin(renderTime / 3.0f) * 0.5f + 0.5f, 1.0f);
        }
        // The original shader used alpha as the strength of a red stress overlay.
        // Standard render pipelines use it as opacity, so keep the mesh opaque and
        // approximate the overlay with an RGB tint instead.
        return ARGB.lerp(stressAlpha, 0xFFFFFFFF, SimColors.STRESSED_RED);
    }

    @Override
    protected void renderSafe(final SpringBlockEntity be, final float partialTicks, final PoseStack ps, final MultiBufferSource bufferSource, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ps, bufferSource, light, overlay);
        if (!be.isController()) {
            return;
        }

        final SpringBlockEntity other = be.getPairedSpring();
        if (other == null) {
            return;
        }

        final BlockState state = be.getBlockState();
        final SpringBlock.Size size = state.getValue(SpringBlock.SIZE);
        final String name = (size == SpringBlock.Size.MEDIUM ? "" : (size.getSerializedName() + "_")) + "spring";
        final VertexConsumer buffer = bufferSource.getBuffer(SimRenderTypes.spring(Simulated.path("textures/block/spring/" + name + ".png")));

        ps.pushPose();

        final Minecraft minecraft = Minecraft.getInstance();
        // Resolve both transforms from the actual block entities. The persisted
        // partner UUID can briefly lag behind an assembly packet on the client.
        final ClientSubLevel otherSubLevel = Sable.HELPER.getContainingClient(other);
        final ClientSubLevel subLevel = Sable.HELPER.getContainingClient(be);

        final BlockPos blockPos = be.getBlockPos();

        final Vector3dc center = be.getCenter();
        final Vector3d otherCenter = other.getCenter();

        final Direction facing = state.getValue(SpringBlock.FACING);
        final Direction otherFacing = other.getBlockState().getValue(SpringBlock.FACING);
        final Vector3dc normalA = JOMLConversion.atLowerCornerOf(facing.getUnitVec3i());
        final Vector3d normalB = JOMLConversion.atLowerCornerOf(otherFacing.getUnitVec3i());

        ps.translate(center.x() - (blockPos.getX()), center.y() - (blockPos.getY()), center.z() - (blockPos.getZ()));

        final double PI2 = Math.PI / 2.0;
        final double PI4 = PI2 / 2.0;
        final Pose3dc renderPose = subLevel != null ? subLevel.renderPose() : null;
        final Pose3dc otherRenderPose = otherSubLevel != null ? otherSubLevel.renderPose() : null;

        if (otherRenderPose != null) {
            otherRenderPose.transformNormal(normalB);
            otherRenderPose.transformPosition(otherCenter);
        }

        if (renderPose != null) {
            renderPose.transformNormalInverse(normalB);
            renderPose.transformPositionInverse(otherCenter);
        }

        final int color = getStressColor(be, partialTicks, otherCenter, center, minecraft);

        final List<SplinePoint> splinePoints = this.generateSpline(
                JOMLConversion.ZERO,
                otherCenter.sub(center, new Vector3d()),
                normalA,
                normalB,
                center.distance(otherCenter) / 5.0 + 0.25
        );

        final int totalPoints = splinePoints.size();

        final Vector3d pointNormal = new Vector3d();

        final Vector3d startUpDir = JOMLConversion.toJOML(this.getUpDirection(be, otherCenter.sub(center, new Vector3d())));

        pointNormal.set(splinePoints.getFirst().normal);

        final Matrix3d matrix = new Matrix3d(
                startUpDir,
                pointNormal,
                startUpDir.cross(pointNormal, new Vector3d())
        );

        // March the frame all the way through the points to get the final orientation with no twist
        double totalSpringLength = 0.0;
        for (int i = 0; i < totalPoints - 1; i++) {
            final SplinePoint point = splinePoints.get(i);
            final SplinePoint nextPoint = splinePoints.get(i + 1);

            totalSpringLength += point.point.distance(nextPoint.point);

            matrix.rotateLocal(
                    SimMathUtils.getQuaternionfFromVectorRotation(point.normal, nextPoint.normal)
            );
        }

        // we derive the twist we need to land at an increment of 90deg final twist
        final Quaterniond orientation = new Quaterniond();

        final Quaterniondc orientation1 = renderPose != null ? renderPose.orientation() : JOMLConversion.QUAT_IDENTITY;
        final Quaterniondc orientation2 = otherRenderPose != null ? otherRenderPose.orientation() : JOMLConversion.QUAT_IDENTITY;

        final Quaterniond blockOrientation1 = new Quaterniond(facing.getRotation());
        final Quaterniond blockOrientation2 = new Quaterniond(otherFacing.getRotation());
        blockOrientation2.premul(orientation2).premul(orientation1.conjugate(new Quaterniond()));

        final Quaterniond relativeBlockOrientation = new Quaterniond(blockOrientation1).div(blockOrientation2);

        orientation.mul(new Quaterniond(relativeBlockOrientation));
        orientation.mul(matrix.getNormalizedRotation(new Quaterniond()));

        // not sure how this happens. but we chillin
        if (Math.abs(OrientedBoundingBox3d.UP.dot(new Vector3d(orientation.x(), orientation.y(), orientation.z()))) < 1e-5) {
            orientation.rotateLocalX(Math.PI);
        }

        final double d = OrientedBoundingBox3d.UP.dot(new Vector3d(orientation.x(), orientation.y(), orientation.z()));
        final double deg = 2.0 * Math.atan2(-d, orientation.w());
        final double twist = Math.floor((deg + PI4) / PI2) * PI2 - deg;

        final float uvScale = (float) ((be.getRenderLength(partialTicks) - 0.75) / totalSpringLength);
        double runningSpringLength = 0.0;

        matrix.set(
                startUpDir,
                pointNormal,
                startUpDir.cross(pointNormal, new Vector3d())
        );

        for (int i = 0; i < totalPoints - 1; i++) {
            final SplinePoint point = splinePoints.get(i);
            final SplinePoint nextPoint = splinePoints.get(i + 1);

            final Vector3dc upDir = matrix.getColumn(0, new Vector3d());

            matrix.rotateLocal(
                    SimMathUtils.getQuaternionfFromVectorRotation(point.normal, nextPoint.normal)
            );
            matrix.rotateY(-twist / (totalPoints - 1));

            final Vector3dc nextUpDir = matrix.getColumn(0, new Vector3d());

            final double length = point.point.distance(nextPoint.point);

            final float width = switch (size) {
                case SMALL -> 6.0f;
                case MEDIUM -> 8.0f;
                case LARGE -> 10.0f;
            };

            final float textureWidth = switch (size) {
                case SMALL -> 16.0f;
                case MEDIUM -> 16.0f;
                case LARGE -> 32.0f;
            };

            this.renderSegment(ps,
                    point.normal,
                    nextPoint.normal,
                    upDir,
                    nextUpDir,
                    point.point,
                    nextPoint.point,
                    false,
                    (float) runningSpringLength * uvScale,
                    (float) (runningSpringLength + length) * uvScale,
                    light,
                    color,
                    buffer,
                    width,
                    textureWidth);

            // render inside
            this.renderSegment(ps,
                    point.normal.negate(new Vector3d()),
                    nextPoint.normal.negate(new Vector3d()),
                    upDir.negate(new Vector3d()),
                    nextUpDir.negate(new Vector3d()),
                    point.point,
                    nextPoint.point,
                    true,
                    0.0f - (float) runningSpringLength * uvScale,
                    0.0f - (float) (runningSpringLength + length) * uvScale,
                    light,
                    color,
                    buffer,
                    width,
                    textureWidth);
            runningSpringLength += length;
        }

        ps.popPose();
    }

    private Vec3 getUpDirection(final SpringBlockEntity be, final Vector3dc directionToSpring) {
        final Direction facing = be.getBlockState().getValue(SpringBlock.FACING);

        final Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        final double dot = directionToSpring.dot(normal.x, normal.y, normal.z);
        final Vector3d dir = directionToSpring.sub(normal.x * dot, normal.y * dot, normal.z * dot, new Vector3d());

        if (dir.lengthSquared() < 1e-6) {
            // default to north or up if we're in a configuration where we can't pick anything
            return facing.getAxis().isHorizontal() ? new Vec3(0, 1, 0) : new Vec3(0, 0, -1);
        }

        return Vec3.atLowerCornerOf(Direction.getApproximateNearest(dir.x, dir.y, dir.z).getOpposite().getUnitVec3i());
    }

    /**
     * Generate a spline with equally spaced points (0.5 units)
     */
    private List<SplinePoint> generateSpline(final Vector3dc pointA, final Vector3dc pointB, final Vector3dc normalA, final Vector3dc normalB, final double controlPointLength) {
        final List<SplinePoint> list = new ObjectArrayList<>();

        final double influence = controlPointLength;
        pointA.fma(influence, normalA, this.controlPointA);
        pointB.fma(influence, normalB, this.controlPointB);

        final double len = pointA.distance(pointB);
        final int initialPointCount = Mth.clamp(Mth.ceil(len), 5, 8);
        for (int i = 0; i <= initialPointCount; i++) {
            final double t = (double) i / initialPointCount;
            pointA.lerp(this.controlPointA, t, this.segmentALerp);
            this.controlPointA.lerp(this.controlPointB, t, this.segmentBLerp);
            this.controlPointB.lerp(pointB, t, this.segmentCLerp);

            final Vector3d point = new Vector3d(this.segmentALerp
                    .lerp(this.segmentBLerp, t)
                    .lerp(this.segmentBLerp.lerp(this.segmentCLerp, t), t));

            final Vector3d normal = new Vector3d();

            if (list.isEmpty()) {
                normal.set(normalA);
            } else if (list.size() == initialPointCount) {
                normal.set(normalB).negate();
            } else {
                point.sub(list.get(list.size() - 1).point, normal).normalize();
            }

            list.add(new SplinePoint(point, normal));
        }

        return list;
    }

    private void renderSegment(final PoseStack ms,
                               final Vector3dc startDirection,
                               final Vector3dc endDirection,
                               final Vector3dc inputStartUp,
                               final Vector3dc inputEndUp,
                               final Vector3dc startPos,
                               final Vector3dc endPos,
                               final boolean second,
                               final float uvStart,
                               final float uvEnd,
                               final int light,
                               final int color,
                               final VertexConsumer a,
                               final float width,
                               final float textureWidth) {
        inputStartUp.cross(startDirection, this.startLeft).normalize();
        inputEndUp.cross(endDirection, this.endLeft).normalize();

        final float texW = width / textureWidth;
        final double scale = width / 16.0 / 2.0;

        this.startLeft.mul(scale);
        inputStartUp.mul(scale, this.startUp);
        this.endLeft.mul(scale);
        inputEndUp.mul(scale, this.endUp);

        final Vector3d startDown = this.startUp.negate(new Vector3d());
        final Vector3d endDown = this.endUp.negate(new Vector3d());
        final Vector3d startRight = this.startLeft.negate(new Vector3d());
        final Vector3d endRight = this.endLeft.negate(new Vector3d());

        final float uvScale = 16.0f / textureWidth;
        final float uvXOffset = second ? width / textureWidth : 0.0f;
        this.vert(ms, a, startPos.add(this.startLeft, this.vertex).sub(this.startUp), color, 0.0f + uvXOffset, uvStart * uvScale, startDown, light);
        this.vert(ms, a, endPos.add(this.endLeft, this.vertex).sub(this.endUp), color, 0.0f + uvXOffset, uvEnd * uvScale, endDown, light);
        this.vert(ms, a, endPos.sub(this.endLeft, this.vertex).sub(this.endUp), color, texW + uvXOffset, uvEnd * uvScale, endDown, light);
        this.vert(ms, a, startPos.sub(this.startLeft, this.vertex).sub(this.startUp), color, texW + uvXOffset, uvStart * uvScale, startDown, light);

        this.vert(ms, a, startPos.sub(this.startLeft, this.vertex).add(this.startUp), color, 0.0f + uvXOffset, uvStart * uvScale, this.startUp, light);
        this.vert(ms, a, endPos.sub(this.endLeft, this.vertex).add(this.endUp), color, 0.0f + uvXOffset, uvEnd * uvScale, this.endUp, light);
        this.vert(ms, a, endPos.add(this.endLeft, this.vertex).add(this.endUp), color, texW + uvXOffset, uvEnd * uvScale, this.endUp, light);
        this.vert(ms, a, startPos.add(this.startLeft, this.vertex).add(this.startUp), color, texW + uvXOffset, uvStart * uvScale, this.startUp, light);

        this.vert(ms, a, startPos.sub(this.startLeft, this.vertex).sub(this.startUp), color, 0.0f + uvXOffset, uvStart * uvScale, startRight, light);
        this.vert(ms, a, endPos.sub(this.endLeft, this.vertex).sub(this.endUp), color, 0.0f + uvXOffset, uvEnd * uvScale, endRight, light);
        this.vert(ms, a, endPos.sub(this.endLeft, this.vertex).add(this.endUp), color, texW + uvXOffset, uvEnd * uvScale, endRight, light);
        this.vert(ms, a, startPos.sub(this.startLeft, this.vertex).add(this.startUp), color, texW + uvXOffset, uvStart * uvScale, startRight, light);

        this.vert(ms, a, startPos.add(this.startLeft, this.vertex).add(this.startUp), color, 0.0f + uvXOffset, uvStart * uvScale, this.startLeft, light);
        this.vert(ms, a, endPos.add(this.endLeft, this.vertex).add(this.endUp), color, 0.0f + uvXOffset, uvEnd * uvScale, this.endLeft, light);
        this.vert(ms, a, endPos.add(this.endLeft, this.vertex).sub(this.endUp), color, texW + uvXOffset, uvEnd * uvScale, this.endLeft, light);
        this.vert(ms, a, startPos.add(this.startLeft, this.vertex).sub(this.startUp), color, texW + uvXOffset, uvStart * uvScale, this.startLeft, light);
    }

    private void vert(final PoseStack ms, final VertexConsumer a, final Vector3dc pos, final int color, final float u1, final float v1, final Vector3dc normal, final int light) {
        normal.normalize(this.normalizedNormal);
        a.addVertex(ms.last().pose(), (float) pos.x(), (float) pos.y(), (float) pos.z())
                .setColor(color)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(ms.last(), (float) this.normalizedNormal.x(), (float) this.normalizedNormal.y(), (float) this.normalizedNormal.z());
    }

    @Override
    public boolean shouldRender(final SpringBlockEntity blockEntity, final Vec3 vec3) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return super.shouldRenderOffScreen();
    }

    record SplinePoint(Vector3dc point, Vector3dc normal) {
    }
}
