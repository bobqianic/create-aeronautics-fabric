package dev.ryanhcode.offroad.index;

import com.zurrtum.create.AllSoundEvents;
import dev.simulated_team.simulated.api.sound.SoundEventRegistry;
import dev.ryanhcode.offroad.Offroad;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class OffroadSoundEvents {
    public static final SoundEventRegistry REGISTRY = new SoundEventRegistry(Offroad.MOD_ID);
    public static final Map<ResourceLocation, AllSoundEvents.SoundEntry> ALL = new HashMap<>();

    public static void init() {

    }
}
