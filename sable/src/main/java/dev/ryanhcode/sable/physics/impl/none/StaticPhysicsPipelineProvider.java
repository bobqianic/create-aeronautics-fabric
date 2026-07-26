package dev.ryanhcode.sable.physics.impl.none;

import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineProvider;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

@PhysicsPipelineProvider.LoadPriority(900)
public final class StaticPhysicsPipelineProvider implements PhysicsPipelineProvider {

    @Override
    public @NotNull PhysicsPipeline createPipeline(@NotNull final ServerLevel level) {
        return new StaticPhysicsPipeline();
    }
}
