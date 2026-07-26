package dev.ryanhcode.sable.mixinhelpers.sublevel_render.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import dev.ryanhcode.sable.sublevel.render.dispatcher.SubLevelRenderDispatcher;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collection;
import java.util.SortedSet;

public class VanillaSubLevelBlockEntityRenderer implements SubLevelRenderDispatcher.BlockEntityRenderer {

    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final RenderBuffers renderBuffers;
    private final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    public VanillaSubLevelBlockEntityRenderer(final BlockEntityRenderDispatcher blockEntityRenderDispatcher, final RenderBuffers renderBuffers, final Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress) {
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.renderBuffers = renderBuffers;
        this.destructionProgress = destructionProgress;
    }

    @Override
    public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    @Override
    public void renderSingleBE(final BlockEntity blockEntity, final PoseStack poseStack, final float partialTick, final double cameraX, final double cameraY, final double cameraZ) {
    }
}
