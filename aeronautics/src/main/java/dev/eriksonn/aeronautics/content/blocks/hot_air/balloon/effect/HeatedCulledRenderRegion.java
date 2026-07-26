package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.effect;

import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.ryanhcode.sable.util.LevelAccelerator;
import org.joml.Matrix4f;
import org.lwjgl.system.NativeResource;

public class HeatedCulledRenderRegion implements NativeResource {
    public HeatedCulledRenderRegion(final LevelAccelerator accelerator, final Balloon balloon) {
    }

    public void render(final Matrix4f modelView, final Matrix4f projectionMatrix) {
    }

    @Override
    public void free() {
    }
}
