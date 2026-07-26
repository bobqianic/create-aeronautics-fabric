package dev.simulated_team.simulated.api.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.simulated_team.simulated.util.SimCodecUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public record SoundFile(ResourceLocation name, float volume, float pitch, int weight, boolean stream, int attenuationDistance, boolean preload, Type type) {
	public static final SoundFile DEFAULT = new SoundFile(ResourceLocation.withDefaultNamespace("default"), 1.0f, 1.0f, 1, false, 16, false, Type.FILE);

	public static final Codec<SoundFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("name").forGetter(SoundFile::name),
			Codec.FLOAT.optionalFieldOf("volume", DEFAULT.volume()).forGetter(SoundFile::volume),
			Codec.FLOAT.optionalFieldOf("pitch", DEFAULT.pitch()).forGetter(SoundFile::pitch),
			Codec.INT.optionalFieldOf("weight", DEFAULT.weight()).forGetter(SoundFile::weight),
			Codec.BOOL.optionalFieldOf("stream", DEFAULT.stream()).forGetter(SoundFile::stream),
			Codec.INT.optionalFieldOf("attenuation_distance", DEFAULT.attenuationDistance()).forGetter(SoundFile::attenuationDistance),
			Codec.BOOL.optionalFieldOf("preload", DEFAULT.preload()).forGetter(SoundFile::preload),
			StringRepresentable.fromEnum(Type::values).optionalFieldOf("type", DEFAULT.type()).forGetter(SoundFile::type)
	).apply(instance, SoundFile::new));

	public static final Codec<SoundFile> SIMPLE_CODEC = ResourceLocation.CODEC.flatXmap(
			rl -> DataResult.success(new SoundFile(rl)),
			sf -> sf.isDefault() ? DataResult.success(sf.name()) : DataResult.error(() -> "Object is not default"));

	public static final Codec<SoundFile> FULL_CODEC = SimCodecUtil.withAlternative(SIMPLE_CODEC, CODEC);

	public SoundFile(final ResourceLocation name) {
		this(name, DEFAULT.volume(), DEFAULT.pitch(), DEFAULT.weight(), DEFAULT.stream(), DEFAULT.attenuationDistance(), DEFAULT.preload(), DEFAULT.type());
	}

	public boolean isDefault() {
		return this.weight == DEFAULT.weight() &&
                this.pitch == DEFAULT.pitch() &&
                this.volume == DEFAULT.volume() &&
                this.stream == DEFAULT.stream() &&
                this.preload == DEFAULT.preload() &&
                this.attenuationDistance == DEFAULT.attenuationDistance() &&
                this.type == DEFAULT.type();
	}

	public enum Type implements StringRepresentable {
		FILE,
		EVENT;

		@Override
		public String getSerializedName() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}

}
