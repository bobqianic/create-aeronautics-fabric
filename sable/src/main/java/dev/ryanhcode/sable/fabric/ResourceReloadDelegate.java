package dev.ryanhcode.sable.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ResourceReloadDelegate implements IdentifiableResourceReloadListener {
    private final ResourceLocation id;
    private final SimpleJsonResourceReloadListener delegate;

    public ResourceReloadDelegate(final ResourceLocation id, final SimpleJsonResourceReloadListener delegate) {
        this.id = id;
        this.delegate = delegate;
    }

    @Override
    public ResourceLocation getFabricId() {
        return this.id;
    }

    @Override
    public CompletableFuture<Void> reload(final SharedState sharedState, final Executor preparationExecutor, final PreparationBarrier preparationBarrier, final Executor applicationExecutor) {
        return this.delegate.reload(sharedState, preparationExecutor, preparationBarrier, applicationExecutor);
    }
}
