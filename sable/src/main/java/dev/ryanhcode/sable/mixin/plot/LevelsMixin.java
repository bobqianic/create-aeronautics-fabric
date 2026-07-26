package dev.ryanhcode.sable.mixin.plot;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.mixinterface.plot.SubLevelContainerHolder;
import dev.ryanhcode.sable.platform.SableEventPublishPlatform;
import dev.ryanhcode.sable.platform.SablePlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds a {@link SubLevelContainer} to server levels
 */
@Mixin(ServerLevel.class)
public abstract class LevelsMixin extends Level implements SubLevelContainerHolder {
    @Unique
    private final SubLevelContainer sable$plotContainer = this.sable$createPlotContainer();

    // PORT-NOTE(mc26.1): Level's ctor lost the Supplier<ProfilerFiller> param (Profiler.get() is global now).
    protected LevelsMixin(final WritableLevelData writableLevelData, final ResourceKey<Level> resourceKey, final RegistryAccess registryAccess, final Holder<DimensionType> holder, final boolean bl, final boolean bl2, final long l, final int i) {
        super(writableLevelData, resourceKey, registryAccess, holder, bl, bl2, l, i);
    }


    @Unique
    private SubLevelContainer sable$createPlotContainer() {
        if (SablePlatform.INSTANCE.isWrappedLevel(this)) return null;
        final SubLevelContainer container = new ServerSubLevelContainer(this, SubLevelContainer.DEFAULT_LOG_SIZE_LENGTH, SubLevelContainer.DEFAULT_LOG_PLOT_SIZE, SubLevelContainer.DEFAULT_ORIGIN, SubLevelContainer.DEFAULT_ORIGIN);

        Sable.defaultSubLevelContainerInitializer(this, container);
        SableEventPublishPlatform.INSTANCE.onSubLevelContainerReady(this, container);

        return container;
    }

    @Override
    public SubLevelContainer sable$getPlotContainer() {
        return this.sable$plotContainer;
    }
}
