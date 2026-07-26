package com.zurrtum.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class CustomRenderedItemModelRenderer {
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
    }
}
