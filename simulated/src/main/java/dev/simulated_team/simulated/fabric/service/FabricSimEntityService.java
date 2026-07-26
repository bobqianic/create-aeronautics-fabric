package dev.simulated_team.simulated.fabric.service;

import com.tterrag.registrate.builders.EntityBuilder;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimEntityTypes;
import dev.simulated_team.simulated.service.SimEntityService;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class FabricSimEntityService implements SimEntityService {
    private static final AttachmentType<CompoundTag> CUSTOM_DATA = AttachmentRegistry.create(
            Simulated.path("entity_custom_data"),
            builder -> builder.initializer(CompoundTag::new).persistent(CompoundTag.CODEC).copyOnDeath()
    );

    @Override
    public CompoundTag getCustomData(final Entity entity) {
        return ((AttachmentTarget) entity).getAttachedOrCreate(CUSTOM_DATA);
    }

    @Override
    public double getPlayerReach(final Player player) {
        return player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
    }

    @Override
    public boolean isFake(final Player player) {
        return player instanceof FakePlayer;
    }

    @Override
    public <T extends Entity, P> EntityBuilder<T, P> loaderEntityTransform(
            final EntityBuilder<T, P> builder,
            final SimEntityTypes.EntityLoaderData data) {
        return builder.properties(properties -> {
            if (data.immuneToFire()) {
                properties.fireImmune();
            }
            properties.dimensions(EntityDimensions.fixed(data.width(), data.height()));
            // EntityLoaderData stores a chunk-based range. Fabric's
            // trackable(...) overload takes blocks, which would reduce the
            // plunger's intended 10-chunk range to one chunk.
            properties.trackRangeChunks(data.clientTrackingRange());
            properties.trackedUpdateRate(data.updateFrequency());
            properties.forceTrackedVelocityUpdates(data.sendVelocity());
        });
    }
}
