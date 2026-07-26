package dev.simulated_team.simulated.content.end_sea;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.network.packets.end_sea.ClientboundEndSeaPacket;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EndSeaPhysicsData {
    private static final HashMap<ResourceKey<Level>, EndSeaPhysics> END_SEA_PHYSICS_DATA = new HashMap<>();

    public static @Nullable EndSeaPhysics of(final Level level) {
        return END_SEA_PHYSICS_DATA.get(level.dimension());
    }

    public static void physicsTick(final double substepTimeStep, final ServerLevel level) {
        final EndSeaPhysics physics = EndSeaPhysicsData.of(level);
        if (physics != null) {
            physics.physicsTick(substepTimeStep, level);
        }
    }

    public static void addKeyWithPriority(final ResourceKey<Level> dimension, final EndSeaPhysics newPhysics) {
        final EndSeaPhysics existing = END_SEA_PHYSICS_DATA.get(dimension);
        if (existing != null) {
            if (existing.priority().isEmpty()) {
                END_SEA_PHYSICS_DATA.put(dimension, newPhysics);
            } else if (newPhysics.priority().isEmpty()) {
                // pass if existing has defined priority and new doesn't
            } else if (newPhysics.priority().get() > existing.priority().get()) {
                END_SEA_PHYSICS_DATA.put(dimension, newPhysics);
            }
        } else {
            END_SEA_PHYSICS_DATA.put(dimension, newPhysics);
        }
    }

    public static void syncDataPacket(final VeilPacketManager.PacketSink sink) {
        sink.sendPacket(new ClientboundEndSeaPacket(END_SEA_PHYSICS_DATA.entrySet().stream().map(Map.Entry::getValue).toList()));
    }

    public static void handleDataPacket(final ClientboundEndSeaPacket packet) {
        END_SEA_PHYSICS_DATA.clear();
        for (final EndSeaPhysics physics : packet.physics()) {
            addKeyWithPriority(ResourceKey.create(Registries.DIMENSION, physics.dimension()), physics);
        }
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener<EndSeaPhysics> {

        public static final ReloadListener INSTANCE = new ReloadListener();

        public static final String NAME = "end_sea";
        public static final ResourceLocation ID = Simulated.path(NAME);

        public ReloadListener() {
            super(EndSeaPhysics.CODEC, FileToIdConverter.json(NAME));
        }

        @Override
        protected void apply(final Map<ResourceLocation, EndSeaPhysics> map, final ResourceManager resourceManager, final ProfilerFiller profiler) {
            END_SEA_PHYSICS_DATA.clear();

            for (final EndSeaPhysics physics : map.values()) {
                final ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, physics.dimension());
                EndSeaPhysicsData.addKeyWithPriority(dimension, physics);
            }
        }

        @Override
        public String getName() {
            return NAME;
        }
    }
}
