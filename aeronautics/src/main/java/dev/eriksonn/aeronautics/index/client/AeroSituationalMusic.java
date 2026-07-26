package dev.eriksonn.aeronautics.index.client;

import com.zurrtum.create.AllSoundEvents;
import dev.simulated_team.simulated.api.sound.SimSoundEntry;
import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.api.CustomSituationalMusic;
import dev.eriksonn.aeronautics.index.AeroSoundEvents;
import dev.ryanhcode.sable.mixinterface.entity.entity_sublevel_collision.EntityMovementExtension;
import dev.ryanhcode.sable.sublevel.SubLevel;
import foundry.veil.platform.registry.RegistryObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;

public class AeroSituationalMusic {
    public static RegistryObject<CustomSituationalMusic> CLEAR = create("clear", AeroSoundEvents.MUSIC_AIRSHIP_CLEAR, AeroSituationalMusic::testClear);
    public static RegistryObject<CustomSituationalMusic> RAIN = create("rain", AeroSoundEvents.MUSIC_AIRSHIP_RAIN, AeroSituationalMusic::testRain);

    public static boolean testClear(final ClientLevel level, final LocalPlayer player) {
        final SubLevel subLevel = ((EntityMovementExtension) player).sable$getTrackingSubLevel();

        return !(level.isRaining() || level.isThundering()) &&
                player.position().y() > 100 &&
                subLevel != null &&
                level.getRandom().nextFloat() > 0.25f;
    }

    private static boolean testRain(final ClientLevel level, final LocalPlayer player) {
        final SubLevel subLevel = ((EntityMovementExtension) player).sable$getTrackingSubLevel();

        return level.isRaining() || level.isThundering() &&
                player.position().y() > 100 &&
                subLevel != null &&
                level.getRandom().nextFloat() > 0.25f;
    }

    private static RegistryObject<CustomSituationalMusic> create(final String id, final SimSoundEntry soundEntry, final CustomSituationalMusic.Condition condition) {
        return create(id, soundEntry, 3, 4, false, condition);
    }

    private static RegistryObject<CustomSituationalMusic> create(final String id, final SimSoundEntry soundEntry, final int minDelayMinutes, final int maxDelayMinutes, final boolean replaceCurrent, final CustomSituationalMusic.Condition condition) {
        return AeroClientRegistries.CUSTOM_SITUATIONAL_MUSIC
                .register(Aeronautics.path(id), () -> new CustomSituationalMusic(new Music(soundEntry.registryObject().asHolder(), minDelayMinutes * 60000, maxDelayMinutes * 60000, replaceCurrent), condition));
    }

    public static void init() {
    }
}
