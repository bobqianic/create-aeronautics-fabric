package dev.simulated_team.simulated.content.blocks.rope.rope_winch;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyRenderer;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import dev.simulated_team.simulated.compat.create.FilteringRenderer;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.RopeStrandRenderer;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.index.SimSpriteShifts;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.zurrtum.create.content.kinetics.base.DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class RopeWinchRenderer extends SafeBlockEntityRenderer<RopeWinchBlockEntity> {

    public RopeWinchRenderer(final BlockEntityRendererProvider.Context context) {

        super(context);

    }

    private static SuperByteBuffer transform(final SuperByteBuffer buffer, final BlockState state, final boolean axisDirectionMatters) {
        final Direction facing = state.getValue(FACING);

        final float zRotLast =
                axisDirectionMatters && (state.getValue(AXIS_ALONG_FIRST_COORDINATE) ^ facing.getAxis() == Direction.Axis.Z) ? 90
                        : 0;
        final float yRot = AngleHelper.horizontalAngle(facing) + (state.getValue(AXIS_ALONG_FIRST_COORDINATE) || facing.getAxis()
                != Direction.Axis.Y ? 0.0f : 90.0f);
        final float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

        buffer.rotateCentered((float) ((zRot) / 180 * Math.PI), Direction.SOUTH);
        buffer.rotateCentered((float) ((yRot) / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) ((zRotLast) / 180 * Math.PI), Direction.SOUTH);
        return buffer;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(final RopeWinchBlockEntity pBlockEntity, final Vec3 pCameraPos) {
        return true;
    }

    protected void renderSafe(final RopeWinchBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        FilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);
        this.renderComponents(be, partialTicks, ms, buffer, light, overlay);
    }

    protected void renderComponents(final RopeWinchBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        ms.pushPose();
        final VertexConsumer vb = buffer.getBuffer(RenderType.solid());

        final BlockState state = be.getBlockState();
        final SuperByteBuffer shaft = CachedBuffers.partial(SimPartialModels.ROPE_WINCH_SHAFT, state);
        final SuperByteBuffer ropeCoil = CachedBuffers.partial(SimPartialModels.ROPE_WINCH_ROPE_COIL, state);

        final Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(be);
        final float angle = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(), axis);
        KineticBlockEntityRenderer.kineticRotationTransform(shaft, be, axis, angle, light);
        transform(shaft, state, true).renderInto(ms.last(), vb);

        if (be.getRopeHolder().isAttached() || (be.isVirtual() && be.getRopeHolder().renderAttached)) {
            ropeCoil.light(light);

            final Direction facing = state.getValue(FACING);
            final float speed;

            if (facing == Direction.DOWN) {
                speed = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 1.0f : -1.0f;
            } else {
                speed = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE == (state.getValue(AXIS_ALONG_FIRST_COORDINATE)) ? 1.0f : -1.0f;
            }

            final SpriteShiftEntry coilShift = this.getCoilShift();
            ropeCoil.shiftUVScrolling(coilShift,
                    AbstractPulleyRenderer.getCoilVScroll(coilShift, be.clientAngle.getValue(partialTicks), speed));

            transform(ropeCoil, state, true).renderInto(ms.last(), vb);
        }
        ms.popPose();
        RopeStrandRenderer.render(be, be.getRopeHolder(), partialTicks, ms, buffer);
    }

    protected SpriteShiftEntry getCoilShift() {
        return SimSpriteShifts.ROPE_WINCH_COIL;
    }
}
