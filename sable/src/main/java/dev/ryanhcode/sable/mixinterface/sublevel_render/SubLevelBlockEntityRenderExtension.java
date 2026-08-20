package dev.ryanhcode.sable.mixinterface.sublevel_render;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.LevelRenderState;

public interface SubLevelBlockEntityRenderExtension {

    void sable$extractSubLevelBlockEntities(
            Camera camera,
            float partialTick,
            LevelRenderState levelRenderState
    );
}
