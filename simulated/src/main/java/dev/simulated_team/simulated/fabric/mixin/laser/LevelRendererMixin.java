package dev.simulated_team.simulated.fabric.mixin.laser;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.simulated_team.simulated.content.blocks.lasers.LateLaserRenderQueue;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Composites native lasers after vanilla clouds, weather, and fabulous
 * transparency have been merged back into the main target.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Shadow
    @Final
    private LevelTargetBundle targets;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;addLateDebugPass(" +
                            "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;" +
                            "Lnet/minecraft/world/phys/Vec3;" +
                            "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;" +
                            "Lnet/minecraft/client/renderer/culling/Frustum;)V"
            )
    )
    private void simulated$addLaserAfterClouds(final GraphicsResourceAllocator allocator,
                                                final DeltaTracker deltaTracker,
                                                final boolean renderBlockOutline,
                                                final Camera camera,
                                                final Matrix4f frustumMatrix,
                                                final Matrix4f projectionMatrix,
                                                final Matrix4f modelViewMatrix,
                                                final GpuBufferSlice fogBuffer,
                                                final Vector4f fogColor,
                                                final boolean renderSky,
                                                final CallbackInfo ci,
                                                @Local final FrameGraphBuilder frameGraphBuilder) {
        final FramePass laserPass = frameGraphBuilder.addPass("simulated_laser_after_clouds");
        this.targets.main = laserPass.readsAndWrites(this.targets.main);
        final ResourceHandle<RenderTarget> mainTarget = this.targets.main;

        laserPass.executes(() -> {
            RenderSystem.setShaderFog(fogBuffer);
            final RenderTarget target = mainTarget.get();
            RenderSystem.outputColorTextureOverride = target.getColorTextureView();
            RenderSystem.outputDepthTextureOverride = target.getDepthTextureView();
            try {
                LateLaserRenderQueue.drawAfterClouds(this.renderBuffers.bufferSource());
            } finally {
                RenderSystem.outputColorTextureOverride = null;
                RenderSystem.outputDepthTextureOverride = null;
            }
        });
    }
}
