package dev.ryanhcode.sable.mixin.stop_lightning;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Supplier;

/**
 * Prevents lightning or skeleton traps from spawning inside plots to avoid inflating spawn rates
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    protected ServerLevelMixin(
            final WritableLevelData levelData,
            final ResourceKey<Level> dimension,
            final RegistryAccess registryAccess,
            final Holder<DimensionType> dimensionTypeRegistration,
            final Supplier<ProfilerFiller> profiler,
            final boolean isClientSide,
            final boolean isDebug,
            final long biomeZoomSeed,
            final int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isThundering()Z"))
    private boolean sable$preventLightningInPlot(final ServerLevel instance, final Operation<Boolean> original, @Local(argsOnly = true) final LevelChunk chunk) {
        final SubLevelContainer plotContainer = SubLevelContainer.getContainer(this);
        if (plotContainer != null && plotContainer.inBounds(chunk.getPos())) {
            return false;
        }
        return original.call(instance);
    }
}