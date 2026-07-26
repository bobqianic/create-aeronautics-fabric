package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public final class RenderBridge {
    private static final ThreadLocal<Boolean> DISCOVERING_LAYERS =
            ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<RenderType> ACTIVE_LAYER = new ThreadLocal<>();

    private static final VertexConsumer DISCARDING_CONSUMER = new VertexConsumer() {
        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z) {
            return this;
        }

        @Override
        public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int u, final int v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z) {
            return this;
        }
    };

    private RenderBridge() {
    }

    public static boolean isDiscoveringLayers() {
        return DISCOVERING_LAYERS.get();
    }

    public static boolean isReplayingLayer() {
        return ACTIVE_LAYER.get() != null;
    }

    public static boolean isReplayingLayer(final RenderType layer) {
        return ACTIVE_LAYER.get() == layer;
    }

    public static void submit(final PoseStack poseStack, final SubmitNodeCollector queue, final BiConsumer<PoseStack, MultiBufferSource> renderer) {
        final Set<RenderType> layers = new LinkedHashSet<>();
        DISCOVERING_LAYERS.set(true);
        try {
            renderer.accept(new PoseStack(), layer -> {
                layers.add(layer);
                return DISCARDING_CONSUMER;
            });
        } finally {
            DISCOVERING_LAYERS.remove();
        }

        for (final RenderType layer : layers) {
            queue.submitCustomGeometry(poseStack, layer, (pose, consumer) -> {
                final PoseStack legacyPose = new PoseStack();
                // Preserve the submitted normal matrix as well as the position matrix.
                // Rebuilding it from the position matrix folds Ponder's large uniform
                // GUI scale into the normals, making legacy geometry render too dark.
                legacyPose.last().set(pose);
                final RenderType previousLayer = ACTIVE_LAYER.get();
                ACTIVE_LAYER.set(layer);
                try {
                    renderer.accept(legacyPose,
                            requestedLayer -> layer.equals(requestedLayer) ? consumer : DISCARDING_CONSUMER);
                } finally {
                    if (previousLayer != null) {
                        ACTIVE_LAYER.set(previousLayer);
                    } else {
                        ACTIVE_LAYER.remove();
                    }
                }
            });
        }
    }
}
