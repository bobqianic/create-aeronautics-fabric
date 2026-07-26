package dev.simulated_team.simulated.data.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class SimpleSimulatedTrigger extends SimulatedCriterionTriggerBase<SimulatedCriterionTriggerBase.Instance> {
    public SimpleSimulatedTrigger(final ResourceLocation id) {
        super(id);
    }

    public void trigger(final ServerPlayer player) {
        super.trigger(player, null);
    }

    public Instance instance() {
        return new Instance(this.getId());
    }

    @Override
    public @NotNull Codec<SimulatedCriterionTriggerBase.Instance> codec() {
        return ResourceLocation.CODEC.xmap(Instance::new, SimulatedCriterionTriggerBase.Instance::getId);
    }

    public static class Instance extends SimulatedCriterionTriggerBase.Instance {

        public Instance(final ResourceLocation id) {
            super(id);
        }

        @Override
        protected boolean test(@Nullable final List<Supplier<Object>> suppliers) {
            return true;
        }

        @Override
        public void validate(@NotNull final CriterionValidator criterionValidator) {}
    }
}