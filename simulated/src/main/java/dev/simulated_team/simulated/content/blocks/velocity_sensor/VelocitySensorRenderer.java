package dev.simulated_team.simulated.content.blocks.velocity_sensor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.util.AbstractDirectionalAxisBlock;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class VelocitySensorRenderer extends SafeBlockEntityRenderer<VelocitySensorBlockEntity> {
    public VelocitySensorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final VelocitySensorBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        final VertexConsumer vb = buffer.getBuffer(RenderType.cutoutMipped());

        final BlockState state  = be.getBlockState();
        final SuperByteBuffer diode = CachedBuffers.partial(SimPartialModels.VELOCITY_SENSOR_DIODE, state);
        final SuperByteBuffer fan = CachedBuffers.partial(SimPartialModels.VELOCITY_SENSOR_FAN, state);

        boolean front = (state.getValue(VelocitySensorBlock.POWERED) == 1);
        // north + east always opposite
        // south + down second axis opposite, west first axis opposite
        final boolean axis = state.getValue(VelocitySensorBlock.AXIS_ALONG_FIRST_COORDINATE);
        front = switch (state.getValue(VelocitySensorBlock.FACING)) {
            case NORTH, EAST -> !front;
            case SOUTH, DOWN -> axis == front;
            case WEST -> axis != front;
            case UP -> front;
        };
        final float signalStrength = be.getRedstoneStrength() / 15F;
        final int color = SimColors.redstone(signalStrength);

        this.transform(diode, state);
        diode.light(light).color(front ? color : SimColors.REDSTONE_OFF).renderInto(ms.last(), vb);

        this.transform(diode, state);
        diode.rotateCenteredDegrees(180, Direction.Axis.Y);
        diode.light(light).color(front ? SimColors.REDSTONE_OFF : color).renderInto(ms.last(), vb);

        this.transform(fan.rotateCentered(be.getFanAngle(partialTicks), AbstractDirectionalAxisBlock.getDirectionOfAxis(state)), state);
        fan.light(light).renderInto(ms.last(), vb);
    }

    private void transform(final SuperByteBuffer diode, final BlockState state) {
        final Direction dir = state.getValue(VelocitySensorBlock.FACING);
        final boolean axis = state.getValue(VelocitySensorBlock.AXIS_ALONG_FIRST_COORDINATE);
        if (axis == (dir.getStepX() == 0)) {
            diode.rotateCenteredDegrees(90, state.getValue(VelocitySensorBlock.FACING));
        }

        diode.rotateCentered(dir.getRotation());
    }
}
