package dev.simulated_team.simulated.data.advancements;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class SimAdvancementTriggers {
    private static final List<SimulatedCriterionTriggerBase<?>> TRIGGERS = new LinkedList<>();

    public static SimpleSimulatedTrigger addSimple(final String modid, final String id) {
        return add(new SimpleSimulatedTrigger(ResourceLocation.fromNamespaceAndPath(modid,id)));
    }

    private static <T extends SimulatedCriterionTriggerBase<?>> T add(final T instance) {
        TRIGGERS.add(instance);
        return instance;
    }

    public static void register() {
        TRIGGERS.forEach(trigger -> Registry.register(BuiltInRegistries.TRIGGER_TYPES, trigger.getId(), trigger));
    }

}