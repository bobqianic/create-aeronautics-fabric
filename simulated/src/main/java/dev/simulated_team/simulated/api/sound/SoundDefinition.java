package dev.simulated_team.simulated.api.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record SoundDefinition(boolean replace, Optional<String> subtitle, List<SoundFile> sounds) {
	public static final Codec<SoundDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("replace", false).forGetter(SoundDefinition::replace),
			Codec.STRING.optionalFieldOf("subtitle").orElse(Optional.empty()).forGetter(SoundDefinition::subtitle),
			SoundFile.FULL_CODEC.listOf().optionalFieldOf("sounds", List.of()).forGetter(SoundDefinition::sounds)
	).apply(instance, SoundDefinition::new));
}
