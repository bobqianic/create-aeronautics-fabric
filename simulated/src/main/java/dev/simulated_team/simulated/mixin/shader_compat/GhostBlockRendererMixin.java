package dev.simulated_team.simulated.mixin.shader_compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.SuperRenderTypeBuffer;
import com.zurrtum.create.client.flywheel.lib.util.ShadersModHelper;
import dev.simulated_team.simulated.index.SimRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps Create's translucent placement ghosts textured while shader packs are active.
 *
 * <p>Some shader packs discard vertex alpha in their translucent block program,
 * while their entity program can interpret a world-space ghost as a refractive
 * surface. The dedicated ghost layer uses the block atlas through the translucent
 * particle program, preserving both texture color and per-vertex alpha.</p>
 */
@Mixin(targets = "com.zurrtum.create.client.catnip.ghostblock.GhostBlockRenderer$TransparentGhostBlockRenderer")
public class GhostBlockRendererMixin {

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/zurrtum/create/client/catnip/render/SuperRenderTypeBuffer;getEarlyBuffer(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer simulated$useShaderCompatibleGhostLayer(final SuperRenderTypeBuffer buffer,
                                                                    final ChunkSectionLayer layer,
                                                                    final Operation<VertexConsumer> original) {
        if (layer == ChunkSectionLayer.TRANSLUCENT && ShadersModHelper.isShaderPackInUse()) {
            return buffer.getEarlyBuffer(SimRenderTypes.ghostBlock());
        }

        return original.call(buffer, layer);
    }
}
