package dev.ryanhcode.offroad.content.blocks.wheel_mount;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import dev.simulated_team.simulated.compat.create.FilteringRenderer;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.index.OffroadDataComponents;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public class WheelMountRenderer extends KineticBlockEntityRenderer<WheelMountBlockEntity> {
    public WheelMountRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final WheelMountBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        final BlockState state = this.getRenderedBlockState(be);
        final RenderType type = this.getRenderType(be, state);
        renderRotatingBuffer(be, this.getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
        final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        final Direction direction = be.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING)
                .getOpposite();
        final BlockState blockState = be.getBlockState();

        final SuperByteBuffer diodeLeft = CachedBuffers.partial(OffroadPartialModels.DIODE_LEFT, blockState);
        final SuperByteBuffer diodeRight = CachedBuffers.partial(OffroadPartialModels.DIODE_RIGHT, blockState);
        final SuperByteBuffer teleOuter = CachedBuffers.partial(OffroadPartialModels.TELE_OUTER, blockState);
        final SuperByteBuffer teleInner = CachedBuffers.partial(OffroadPartialModels.TELE_INNER, blockState);
        final SuperByteBuffer teleMount = CachedBuffers.partial(OffroadPartialModels.TELE_MOUNT, blockState);
        final SuperByteBuffer springTop = CachedBuffers.partial(OffroadPartialModels.SPRING_UPPER, blockState);
        final SuperByteBuffer springBottom = CachedBuffers.partial(OffroadPartialModels.SPRING_LOWER, blockState);
        final SuperByteBuffer springMiddle = CachedBuffers.partial(OffroadPartialModels.SPRING_MIDDLE, blockState);

        final double wheelPivotOffsetHor = 10.0 / 16.0;
        final double springWheelPivotOffsetHor = 12.0 / 16.0;
        final double springWheelPivotOffsetVer = -2.0 / 16.0;

        final double horizontalWheelPosition = 22.0 / 16.0;
        final double verticalWheelPosition = -be.getLerpedExtension(partialTicks);

        final double teleMountHor = 0.0 / 16.0;
        final double teleMountVer = -6.0 / 16.0;

        final double springMountHor = 7.0 / 16.0;
        final double springMountVer = 7.0 / 16.0;

        final double teleAngle = Math.atan2(verticalWheelPosition - teleMountVer, horizontalWheelPosition - wheelPivotOffsetHor - teleMountHor);
        final double teleDistance = new Vector2d(verticalWheelPosition - teleMountVer, horizontalWheelPosition - wheelPivotOffsetHor - teleMountHor).length();

        final double springAngle = Math.atan2(verticalWheelPosition - springWheelPivotOffsetVer - springMountVer, horizontalWheelPosition - springWheelPivotOffsetHor - springMountHor);
        final double springDistance = new Vector2d(verticalWheelPosition - springWheelPivotOffsetVer - springMountVer, horizontalWheelPosition - springWheelPivotOffsetHor - springMountHor).length();

        ms.pushPose();
        TransformStack.of(ms)
                .center()
                .rotateYDegrees(AngleHelper.horizontalAngle(direction))
                .rotateXDegrees(AngleHelper.verticalAngle(direction))
                .uncenter();

        // wheel & telescope
        ms.pushPose();
        ms.pushPose();
        ms.translate(0.0, -6.0 / 16.0, 0.0);
        ms.translate(0.5, 0.5, 0.5);
        ms.mulPose(Axis.XP.rotation((float) teleAngle));
        ms.translate(-0.5, -0.5, -0.5);
        teleOuter.light(light).renderInto(ms.last(), vb);
        ms.translate(0.0, 0.0, -(teleDistance - 1.0));
        teleInner.light(light).renderInto(ms.last(), vb);
        ms.popPose();

        ms.pushPose();
        ms.translate(0.0, verticalWheelPosition, 26.0 / 16.0 - horizontalWheelPosition);

        ms.translate(0.5, 0.5, 0.5);
        ms.rotateAround(Axis.YP.rotation((float) be.getLerpedYaw(partialTicks)), 0.0F, 0.0F, (float) (-horizontalWheelPosition + 6.0 / 16.0));
        ms.translate(-0.5, -0.5, -0.5);

        teleMount.light(light).renderInto(ms.last(), vb);

        ms.translate(0.5, 0.5, 0.5);
        ms.translate(0.0, 0.0, -26.0 / 16.0f);

        final double signMultiplier = -be.getLerpedAngle(partialTicks)
                * (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 : -1.0)
                * (direction.getAxis() == Direction.Axis.X ? 1.0 : -1.0);

        ms.mulPose(Axis.ZP.rotation((float) signMultiplier));

        final ItemStack itemStack = be.getHeldItem();
        final TireLike tireLike = itemStack.get(OffroadDataComponents.TIRE);
        if (tireLike != null) {
            final Vec3 rotation = tireLike.rotation();
            ms.mulPose(Axis.XP.rotation((float) Math.toRadians(rotation.x)));
            ms.mulPose(Axis.YP.rotation((float) Math.toRadians(rotation.y)));
            ms.mulPose(Axis.ZP.rotation((float) Math.toRadians(rotation.z)));

            if (tireLike.model().isPresent()) {
                final ResourceLocation model = tireLike.model().get();
                final OffroadPartialModels.MountedTireModel tireModel = OffroadPartialModels.getTireModel(model);
                if (tireModel != null) {
                    ms.translate(tireLike.offset().x, tireLike.offset().y, tireLike.offset().z);
                    final SuperByteBuffer wheel = CachedBuffers.partial(tireModel.partialModel(), state);
                    wheel.light(light)
                            .translate(-0.5f, tireModel.modelOffsetY(), -0.5f)
                            .renderInto(ms.last(), vb);
                }
            }
        }

        ms.popPose();

        ms.popPose();

        // spring
        ms.pushPose();
        ms.translate(0.5, 0.5 + springMountVer, 0.5 - springMountHor);
        ms.mulPose(Axis.XP.rotation((float) springAngle + Mth.PI / 2.0f));
        ms.translate(-0.5, -0.5 - springMountVer, -0.5 + springMountHor);

        final float springExtension = (float) springDistance;
        final float springSpan = springExtension - 4.0f / 16.0f;

        springTop.light(light).renderInto(ms.last(), vb);
        springMiddle.light(light)
                .translate(0.0f, 13.0f / 16.0f, 0.0f)
                .scale(1.0f, springSpan / (14.0f / 16.0f), 1.0f)
                .translateBack(0.0f, 13.0f / 16.0f, 0.0f)
                .renderInto(ms.last(), vb);
        springBottom.light(light)
                .translate(0.0, -(springSpan + -14.0 / 16.0), 0.0)
                .renderInto(ms.last(), vb);
        ms.popPose();

        diodeLeft.light(light)
                .color(SimColors.redstone(be.clientSteeringSignalLeft / 15.0f))
                .renderInto(ms.last(), vb);

        diodeRight.light(light)
                .color(SimColors.redstone(be.clientSteeringSignalRight / 15.0f))
                .renderInto(ms.last(), vb);
        ms.popPose();
    }

    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final WheelMountBlockEntity te, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, te.getBlockState(), te.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite());
    }
}
