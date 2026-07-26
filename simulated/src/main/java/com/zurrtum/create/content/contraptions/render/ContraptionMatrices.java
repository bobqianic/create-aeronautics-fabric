package com.zurrtum.create.content.contraptions.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

/** Minimal matrix carrier retained for movement behaviour source compatibility. */
public class ContraptionMatrices {
    private final PoseStack viewProjection = new PoseStack();
    private final PoseStack model = new PoseStack();
    private final Matrix4f world = new Matrix4f();

    public PoseStack getViewProjection() {
        return viewProjection;
    }

    public PoseStack getModel() {
        return model;
    }

    public Matrix4f getWorld() {
        return world;
    }
}
