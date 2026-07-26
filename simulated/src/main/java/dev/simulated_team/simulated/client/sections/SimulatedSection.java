package dev.simulated_team.simulated.client.sections;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.simulated_team.simulated.Simulated;
import foundry.veil.api.client.color.Color;
import foundry.veil.api.client.color.Colorc;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SimulatedSection(int priority, Title title, ResourceLocation sprite, boolean animateOnHover) implements Comparable<SimulatedSection> {
    private static final ResourceLocation DEFAULT_BANNER = Simulated.path("default_banner");

    public static final Codec<SimulatedSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("priority").orElse(0).forGetter(SimulatedSection::priority),
            Title.CODEC.fieldOf("title").forGetter(SimulatedSection::title),
            ResourceLocation.CODEC.fieldOf("sprite").orElse(DEFAULT_BANNER).forGetter(SimulatedSection::sprite),
            Codec.BOOL.fieldOf("only_animate_on_hover").orElse(false).forGetter(SimulatedSection::animateOnHover)
    ).apply(instance, SimulatedSection::new));

    @Override
    public int compareTo(@NotNull final SimulatedSection other) {
        return (int) Math.signum(this.priority() - other.priority());
    }

    public record Title(Component text, Colorc color, Optional<Colorc> secondaryColor, Colorc background) {
        public static final Codec<Colorc> COLOR_CODEC = Color.ARGB_INT_CODEC.xmap(i -> new Color(i, true), Colorc::argb);
        public static final Codec<Title> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("text").forGetter(Title::text),
            COLOR_CODEC.fieldOf("color").orElse(new Color(0xffffffff, true)).forGetter(Title::color),
            COLOR_CODEC.optionalFieldOf("secondary_color").forGetter(Title::secondaryColor),
            COLOR_CODEC.fieldOf("background").orElse(new Color(0xaa000000, true)).forGetter(Title::background)
        ).apply(instance, Title::new));
    }
}
