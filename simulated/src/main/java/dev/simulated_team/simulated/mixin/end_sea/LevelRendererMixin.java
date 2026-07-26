package dev.simulated_team.simulated.mixin.end_sea;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import dev.simulated_team.simulated.content.end_sea.EndSeaRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("RETURN"))
    public void renderLevel(final GraphicsResourceAllocator allocator, final DeltaTracker deltaTracker, final boolean renderBlockOutline,
                            final Camera camera, final Matrix4f frustumMatrix, final Matrix4f projectionMatrix,
                            final Matrix4f modelViewMatrix, final GpuBufferSlice fogBuffer, final Vector4f fogColor,
                            final boolean renderSky, final CallbackInfo ci) {
        EndSeaRenderer.render(camera, Minecraft.getInstance().gameRenderer);
    }
    
}
