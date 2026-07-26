package dev.simulated_team.simulated.content.blocks.rope.rope_connector;


import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.AllBlocks;
import dev.simulated_team.simulated.compat.create.SafeBlockEntityRenderer;
import dev.simulated_team.simulated.content.blocks.rope.RopeStrandHolderBehavior;
import dev.simulated_team.simulated.content.blocks.rope.strand.client.RopeStrandRenderer;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RopeConnectorRenderer extends SafeBlockEntityRenderer<RopeConnectorBlockEntity> {

    public RopeConnectorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(final RopeConnectorBlockEntity blockEntity, final Vec3 cameraPos) {
        return true;
    }

    @Override
    protected void renderSafe(final RopeConnectorBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        RopeStrandRenderer.render(be, be.getRopeHolder(), partialTicks, ms, buffer);

        final RopeStrandHolderBehavior holder = be.getRopeHolder();

        if ((!holder.isAttached()) && (!be.isVirtual() || !be.getRopeHolder().renderAttached)) {
            return;
        }
        final SuperByteBuffer knot = CachedBuffers.partialFacing(SimPartialModels.ROPE_CONNECTOR_KNOT, AllBlocks.ROPE.defaultBlockState(), Direction.NORTH);

        final BlockPos blockPos = be.getBlockPos();
        final BlockState state = be.getBlockState();

        final Vec3 attachmentPoint = be.getVisualAttachmentPoint(blockPos, state);
        final Direction facing = state.getValue(RopeConnectorBlock.FACING);

        final SuperByteBuffer knotBuffer = knot.light(light);

        final boolean axisAlongFirstCoordinate = state.getValue(RopeConnectorBlock.AXIS_ALONG_FIRST_COORDINATE);

        final float zRotLast = (axisAlongFirstCoordinate ^ facing.getAxis() == Direction.Axis.Z) ? 90 : 0;
        final float yRot = AngleHelper.horizontalAngle(facing) + (axisAlongFirstCoordinate || facing.getAxis() != Direction.Axis.Y ? 0.0f : 90.0f);
        final float zRot = facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0;

        knotBuffer.translate(attachmentPoint.subtract(blockPos.getCenter()));
        knotBuffer.rotateCentered((float) ((zRot) / 180 * Math.PI), Direction.SOUTH);
        knotBuffer.rotateCentered((float) ((yRot) / 180 * Math.PI), Direction.UP);
        knotBuffer.rotateCentered((float) ((zRotLast) / 180 * Math.PI), Direction.SOUTH);

        knotBuffer.rotateCentered((float) (Math.PI / 2.0), Direction.UP);
        knotBuffer.renderInto(ms.last(), buffer.getBuffer(RenderType.solid()));
    }
}
