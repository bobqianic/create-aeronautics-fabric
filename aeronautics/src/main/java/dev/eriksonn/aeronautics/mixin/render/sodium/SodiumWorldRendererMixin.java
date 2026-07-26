package dev.eriksonn.aeronautics.mixin.render.sodium;

import dev.eriksonn.aeronautics.content.blocks.levitite.LevititeShaderManager;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import foundry.veil.api.client.render.VeilRenderBridge;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.shader.program.ShaderProgram;
import foundry.veil.api.client.render.shader.uniform.ShaderUniform;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Priority <1000 to apply before sable
 */
@Mixin(value = SodiumWorldRenderer.class, priority = 990)
public class SodiumWorldRendererMixin {

    @Inject(method = "drawChunkLayer", at = @At(value = "HEAD"))
    public void aeronautics$setupLevititeShaders(final RenderType renderType, final ChunkRenderMatrices matrices, final double x, final double y, final double z, final CallbackInfo ci) {
        if (renderType == AeroRenderTypes.levitite()) {
            final ShaderProgram shader = VeilRenderSystem.setShader(AeroRenderTypes.LEVITITE_SHADER);
            if (shader == null) return;

            ShaderUniform time = shader.getUniform("time");
            if (time != null) {
                long ticks = Minecraft.getInstance().level.getGameTime();
                final float pt = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                ticks = ticks % 100000;

                time.setFloat(ticks + pt);
            }
            LevititeShaderManager.prepareShaderForWorld(VeilRenderBridge.toShaderInstance(shader), x, y, z);
        }
    }

    @Inject(method = "drawChunkLayer", at = @At(value = "TAIL"))
    public void aeronautics$cleanupLevititeShaders(final RenderType renderLayer, final ChunkRenderMatrices matrices, final double x, final double y, final double z, final CallbackInfo ci) {
        if (renderLayer == AeroRenderTypes.levitite()) {
            final ShaderProgram shader = VeilRenderSystem.setShader(AeroRenderTypes.LEVITITE_SHADER);
            if (shader == null) return;

            // reset back to world once rendering is done, for safety
            LevititeShaderManager.prepareShaderForWorld(VeilRenderBridge.toShaderInstance(shader), x, y, z);
        }
    }
}
