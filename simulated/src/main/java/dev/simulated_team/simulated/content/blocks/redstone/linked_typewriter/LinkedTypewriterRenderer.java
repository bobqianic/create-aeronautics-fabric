package dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Vector;


public class LinkedTypewriterRenderer extends SmartBlockEntityRenderer<LinkedTypewriterBlockEntity> {

    static Vector<LerpedFloat> keys = new Vector<>(14);

    public LinkedTypewriterRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    static {
        for (int i = 0; i < 14; ++i) {
            keys.add(LerpedFloat.linear().startWithValue(0.0));
        }
    }

    public static void tick() {
        if (Minecraft.getInstance()
                .isPaused())
            return;

        if (LinkedTypewriterInteractionHandler.getMode() == LinkedTypewriterInteractionHandler.Mode.IDLE) {
            return;
        }

        for (int i = 0; i < keys.size(); i++) {
            final LerpedFloat lerpedFloat = keys.get(i);
            lerpedFloat.chase(LinkedTypewriterInteractionHandler.getPressedKeys().contains(i) ? 1 : 0, .4f, LerpedFloat.Chaser.EXP);
            lerpedFloat.tickChaser();
        }
    }

    public static void resetKeys() {
        for (final LerpedFloat key : keys) {
            key.startWithValue(0.0);
        }
    }

    @Override
    protected void renderSafe(final LinkedTypewriterBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource buffer,
                              int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final VertexConsumer vb = buffer.getBuffer(RenderType.cutout());
        final BlockState blockState = be.getBlockState();
        final Direction facing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        final TransformStack<PoseTransformStack> ps = TransformStack.of(ms);

        final float pt = AnimationTickHolder.getPartialTicks();
        final float s = 0.0625F;
        final float b = s * -0.75F;
        int index = 0;

        // Account for block rotation
        ps.translate(0.5, 4 * s, 0.5);
        ps.rotateYDegrees(AngleHelper.horizontalAngle(facing));
        ps.pushPose();

        // Render Carriage (Might go unused)
        /*
        float carriagePos = be.useFloat.getValue(pt);
        ps.pushPose();
        ps.rotateY(180);
        float carriageAnimation = (float) Math.pow(carriagePos, 3);
        ps.translate(carriageAnimation * 0.375 + -3 * s, 3 * s, s);
        CachedBufferer.partial(CSimPartialModels.LINKED_TYPEWRITER_CARRIAGE, blockState).light(light).renderInto(ms.last(), vb);
        ms.popPose();
         */

        // Render Keys
        if (LinkedTypewriterInteractionHandler.getMode() == LinkedTypewriterInteractionHandler.Mode.BIND) {
            final int i = (int) Mth.lerp((Mth.sin(AnimationTickHolder.getRenderTime() / 4.0F) + 1.0F) / 2.0F, 5.0F, 15.0F);
            light = i << 20;
        }

        // Top Row
        ps.translate(-7 * s, s, 2 * s);
        ps.pushPose();
        for (int i = 0; i < 6; i++) {
            ps.translate(2 * s, 0.0, 0.0);
            renderKey(ms, vb, light, pt, blockState, be, b, index++, false);
        }
        ms.popPose();

        // Bottom Row
        ps.translate(-1 * s, -s, 2 * s);
        ps.pushPose();
        for (int i = 0; i < 7; i++) {
            ps.translate(2 * s, 0.0, 0.0);
            renderKey(ms, vb, light, pt, blockState, be, b, index++, false);
        }
        ms.popPose();

        // Space Bar
        ps.translate(8 * s, -s, 2 * s);
        ps.pushPose();
        renderKey(ms, vb, light, pt, blockState, be, b, index, true);
        ms.popPose();

        ms.popPose();
    }

    protected static void renderKey(final PoseStack ms, final VertexConsumer vb, final int light, final float pt, final BlockState blockState, final LinkedTypewriterBlockEntity be, final float b, final int index, final boolean isSpacebar) {
        ms.pushPose();

        float depression = 0;
        if (be.checkUser(Minecraft.getInstance().player.getUUID())) {
            depression = b * (keys.get(index)).getValue(pt);
        }

        ms.translate(0.0F, depression, 0.0F);

        if (!isSpacebar) {
            CachedBuffers.partial(SimPartialModels.LINKED_TYPEWRITER_KEY, blockState).light(light).renderInto(ms.last(), vb);
        } else {
            CachedBuffers.partial(SimPartialModels.LINKED_TYPEWRITER_KEY_SPACEBAR, blockState).light(light).renderInto(ms.last(), vb);
        }

        ms.popPose();
    }
}
