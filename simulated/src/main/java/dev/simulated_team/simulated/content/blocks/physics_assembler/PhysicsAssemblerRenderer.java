package dev.simulated_team.simulated.content.blocks.physics_assembler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class PhysicsAssemblerRenderer extends SmartBlockEntityRenderer<PhysicsAssemblerBlockEntity> {

    public PhysicsAssemblerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final PhysicsAssemblerBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer, final int light, final int overlay) {
        renderHandle(be, partialTicks, ms, buffer, light);
    }

    /**
     * Renders the animated portion of the assembler for Sable's Sodium moving-
     * block fallback. The baked block model already supplies the base.
     */
    public static void renderInSubLevel(final PhysicsAssemblerBlockEntity be, final float partialTicks, final PoseStack ms,
                                        final MultiBufferSource buffer, final int light, final int overlay) {
        renderHandle(be, partialTicks, ms, buffer, light);
    }

    private static void renderHandle(final PhysicsAssemblerBlockEntity be, final float partialTicks, final PoseStack ms,
                                     final MultiBufferSource buffer, final int light) {
        final BlockState blockState = be.getBlockState();
        final VertexConsumer vb = buffer.getBuffer(RenderType.solid());

        // Render handle
        final SuperByteBuffer handle = CachedBuffers.partial(SimPartialModels.ASSEMBLER_LEVER, blockState);
        final float angle = getRenderAngle(be, partialTicks);
        transform(handle, blockState).translate(1 / 2f, 7 / 16f, 1 / 2f)
                .rotate(angle, Direction.EAST)
                .translate(-1 / 2f, -7 / 16f, -1 / 2f);
        handle.light(light)
                .renderInto(ms.last(), vb);
    }

    public static float getRenderAngle(final PhysicsAssemblerBlockEntity be, final float partialTicks) {
        if (!be.isVirtual()) {
            be.initializeLeverPosition();
        }

        return (float) Math.toRadians(be.getClientAngle(partialTicks));
    }

    private static SuperByteBuffer transform(final SuperByteBuffer buffer, final BlockState leverState) {
        final AttachFace face = leverState.getValue(AnalogLeverBlock.FACE);
        final float rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
        final float rY = AngleHelper.horizontalAngle(leverState.getValue(AnalogLeverBlock.FACING));
        buffer.rotateCentered((float) (rY / 180 * Math.PI), Direction.UP);
        buffer.rotateCentered((float) (rX / 180 * Math.PI), Direction.EAST);
        return buffer;
    }
}
