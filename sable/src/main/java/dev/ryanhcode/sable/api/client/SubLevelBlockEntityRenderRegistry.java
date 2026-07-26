package dev.ryanhcode.sable.api.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Client-side render hooks for block entities that still use legacy immediate
 * geometry. Sodium's moving-block fallback cannot submit those renderers through
 * the normal 1.21.10 block-entity extraction queue, so compatible mods can
 * provide the small immediate portion of their renderer here.
 */
public final class SubLevelBlockEntityRenderRegistry {
    private static final Map<BlockEntityType<?>, Renderer<?>> RENDERERS = new IdentityHashMap<>();

    private SubLevelBlockEntityRenderRegistry() {
    }

    public static <T extends BlockEntity> void register(final BlockEntityType<T> type, final Renderer<T> renderer) {
        RENDERERS.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> void render(
            final T blockEntity,
            final float partialTick,
            final PoseStack poseStack,
            final MultiBufferSource bufferSource,
            final int light,
            final int overlay
    ) {
        final Renderer<T> renderer = (Renderer<T>) RENDERERS.get(blockEntity.getType());
        if (renderer != null) {
            renderer.render(blockEntity, partialTick, poseStack, bufferSource, light, overlay);
        }
    }

    @FunctionalInterface
    public interface Renderer<T extends BlockEntity> {
        void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay);
    }
}
