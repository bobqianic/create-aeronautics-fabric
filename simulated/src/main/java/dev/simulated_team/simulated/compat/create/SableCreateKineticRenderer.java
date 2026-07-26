package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.equipment.armor.BacktankRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.content.equipment.armor.BacktankBlock;
import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Immediate renderers for Create kinetic blocks in Sable's Sodium sublevel
 * pass. Flywheel instances are positioned in plot space and cannot be reused
 * after Sable applies the moving sublevel transform.
 */
public final class SableCreateKineticRenderer {
    private SableCreateKineticRenderer() {
    }

    public static void renderBracketedKinetic(
            final BlockEntity blockEntity,
            final float partialTick,
            final PoseStack poseStack,
            final MultiBufferSource bufferSource,
            final int light,
            final int overlay
    ) {
        if (!(blockEntity instanceof final BracketedKineticBlockEntity kinetic)) {
            return;
        }

        final BlockState state = kinetic.getBlockState();
        final Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(kinetic);
        final Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());
        final Color color = KineticBlockEntityRenderer.getColor(kinetic);

        if (state.is(AllBlocks.LARGE_COGWHEEL)) {
            renderRotating(
                    kinetic,
                    CachedBuffers.partialFacingVertical(
                            AllPartialModels.SHAFTLESS_LARGE_COGWHEEL,
                            state,
                            direction
                    ),
                    KineticBlockEntityRenderer.getAngleForBe(
                            kinetic,
                            kinetic.getBlockPos(),
                            axis
                    ),
                    direction,
                    color,
                    poseStack,
                    consumer,
                    light
            );
            renderRotating(
                    kinetic,
                    CachedBuffers.partialFacingVertical(
                            AllPartialModels.COGWHEEL_SHAFT,
                            state,
                            direction
                    ),
                    BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(
                            kinetic,
                            axis
                    ),
                    direction,
                    color,
                    poseStack,
                    consumer,
                    light
            );
            return;
        }

        renderRotating(
                kinetic,
                CachedBuffers.block(KineticBlockEntityRenderer.KINETIC_BLOCK, state),
                KineticBlockEntityRenderer.getAngleForBe(
                        kinetic,
                        kinetic.getBlockPos(),
                        axis
                ),
                direction,
                color,
                poseStack,
                consumer,
                light
        );
    }

    public static void renderBacktank(
            final BlockEntity blockEntity,
            final float partialTick,
            final PoseStack poseStack,
            final MultiBufferSource bufferSource,
            final int light,
            final int overlay
    ) {
        if (!(blockEntity instanceof final BacktankBlockEntity backtank)) {
            return;
        }

        final BlockState state = backtank.getBlockState();
        final Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(backtank);
        final Direction direction = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());

        renderRotating(
                backtank,
                CachedBuffers.partial(BacktankRenderer.getShaftModel(state), state),
                KineticBlockEntityRenderer.getAngleForBe(
                        backtank,
                        backtank.getBlockPos(),
                        axis
                ),
                direction,
                KineticBlockEntityRenderer.getColor(backtank),
                poseStack,
                consumer,
                light
        );

        final SuperByteBuffer cogs =
                CachedBuffers.partial(BacktankRenderer.getCogsModel(state), state);
        final float yRotation = Mth.DEG_TO_RAD * (
                180 + AngleHelper.horizontalAngle(
                        state.getValue(BacktankBlock.HORIZONTAL_FACING)
                )
        );
        final float cogRotation = AngleHelper.rad(
                backtank.getSpeed() / 4.0F
                        * AnimationTickHolder.getRenderTime(backtank.getLevel())
                        % 360
        );

        cogs.center()
                .rotateY(yRotation)
                .uncenter();
        cogs.translate(0, 0.40625F, 0.6875F)
                .rotate(cogRotation, Direction.EAST)
                .translate(0, -0.40625F, -0.6875F);
        cogs.light(light)
                .renderInto(poseStack.last(), consumer);
    }

    private static void renderRotating(
            final KineticBlockEntity blockEntity,
            final SuperByteBuffer model,
            final float angle,
            final Direction direction,
            final Color color,
            final PoseStack poseStack,
            final VertexConsumer consumer,
            final int light
    ) {
        model.light(light)
                .rotateCentered(angle, direction)
                .color(color)
                .renderInto(poseStack.last(), consumer);
    }
}
