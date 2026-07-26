package dev.ryanhcode.sable.mixin.compatibility.scalablelux;

import ca.spottedleaf.starlight.common.light.BlockStarLightEngine;
import ca.spottedleaf.starlight.common.light.SkyStarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.render.SubLevelDynamicLights;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ScalableLux may execute propagation on its own worker threads. Establish the
 * dynamic-light view around the actual work rather than around the scheduling
 * call on {@link LevelLightEngine}.
 */
@Mixin(value = StarLightInterface.class, remap = false)
public abstract class StarLightInterfaceMixin {
    @Shadow
    @Final
    protected Level world;

    @Shadow
    @Final
    public LevelLightEngine lightEngine;

    /**
     * ScalableLux bypasses both client and server chunk-source hooks here.
     * Resolve plot chunks directly so server packet serialization and client
     * light reads use the same plot storage.
     */
    @Inject(
            method = "getAnyChunkNow",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void sable$getPlotChunk(
            final int chunkX,
            final int chunkZ,
            final CallbackInfoReturnable<ChunkAccess> cir
    ) {
        if (this.world == null) {
            return;
        }

        final SubLevelContainer container = SubLevelContainer.getContainer(this.world);
        if (container != null && container.inBounds(chunkX, chunkZ)) {
            cir.setReturnValue(container.getChunk(new ChunkPos(chunkX, chunkZ)));
        }
    }

    @WrapMethod(method = "handleUpdateInternal")
    private void sable$withSubLevelLights(
            final StarLightInterface.LightQueue.ChunkTasks task,
            final SkyStarLightEngine skyEngine,
            final BlockStarLightEngine blockEngine,
            final Operation<Void> original
    ) {
        SubLevelDynamicLights.beginLightUpdates(this.lightEngine);
        try {
            original.call(task, skyEngine, blockEngine);
        } finally {
            SubLevelDynamicLights.endLightUpdates(this.lightEngine);
        }
    }
}
