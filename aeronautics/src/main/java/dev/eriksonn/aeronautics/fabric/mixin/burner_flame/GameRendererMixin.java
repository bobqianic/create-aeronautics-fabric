package dev.eriksonn.aeronautics.fabric.mixin.burner_flame;

import dev.eriksonn.aeronautics.content.blocks.hot_air.hot_air_burner.IrisBurnerFlameRenderQueue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;" +
                            "renderLevel(" +
                            "Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;" +
                            "Lnet/minecraft/client/DeltaTracker;" +
                            "ZLnet/minecraft/client/Camera;" +
                            "Lorg/joml/Matrix4f;" +
                            "Lorg/joml/Matrix4f;" +
                            "Lorg/joml/Matrix4f;" +
                            "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;" +
                            "Lorg/joml/Vector4f;Z)V",
                    shift = At.Shift.AFTER
            )
    )
    private void aeronautics$renderBurnerFlamesAfterShaderComposite(
            final DeltaTracker deltaTracker,
            final CallbackInfo ci
    ) {
        final MultiBufferSource.BufferSource bufferSource =
                this.minecraft.renderBuffers().bufferSource();
        IrisBurnerFlameRenderQueue.drawAfterShaderComposite(bufferSource);
    }
}
