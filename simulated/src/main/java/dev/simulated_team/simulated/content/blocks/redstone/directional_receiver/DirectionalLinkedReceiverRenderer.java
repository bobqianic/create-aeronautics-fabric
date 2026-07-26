package dev.simulated_team.simulated.content.blocks.redstone.directional_receiver;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;


public class DirectionalLinkedReceiverRenderer extends SmartBlockEntityRenderer<DirectionalLinkedReceiverBlockEntity> {
    public DirectionalLinkedReceiverRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(final DirectionalLinkedReceiverBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, bufferSource, light, overlay);
    }
}
