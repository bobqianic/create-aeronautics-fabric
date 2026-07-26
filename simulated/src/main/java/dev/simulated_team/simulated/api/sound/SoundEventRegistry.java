package dev.simulated_team.simulated.api.sound;

import com.zurrtum.create.AllSoundEvents;
import foundry.veil.platform.registry.RegistrationProvider;
import foundry.veil.platform.registry.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

public class SoundEventRegistry {
	private final Map<String, SoundDefinition> definitions = new LinkedHashMap<>();
	private final Map<String, String> subtitles = new LinkedHashMap<>();
	private final RegistrationProvider<SoundEvent> registry;
	private final String modId;

	public SoundEventRegistry(final String modId) {
		this.modId = modId;
		this.registry = RegistrationProvider.get(BuiltInRegistries.SOUND_EVENT, this.modId);
	}

	public SimSoundEntry create(final String name, final SoundSource category, final UnaryOperator<DefinitionBuilder> operator) {
		final ResourceLocation location = this.path(name);
		this.definitions.put(name, operator.apply(new DefinitionBuilder(name)).build());
		final RegistryObject<SoundEvent> registryObject = this.registry.register(name, () -> SoundEvent.createVariableRangeEvent(location));
		return new SimSoundEntry(location, registryObject, category);
	}

	public SimSoundEntry create(final String name, final UnaryOperator<DefinitionBuilder> operator) {
		return this.create(name, SoundSource.BLOCKS, operator);
	}


	public SoundsProvider getProvider(final PackOutput output) {
		return new SoundsProvider(this.modId, output, this.definitions);
	}

	private ResourceLocation path(final String path) {
		return ResourceLocation.fromNamespaceAndPath(this.modId, path);
	}

	public void provideLang(final BiConsumer<String, String> consumer) {
		this.subtitles.forEach(consumer);
	}

	public class DefinitionBuilder {
		private final String name;
		private String subtitle = null;
		private final List<SoundFile> sounds = new ArrayList<>();
		private DefinitionBuilder(final String name) {
			this.name = name;
		}

		public DefinitionBuilder defaultSubtitle(final String key) {
			this.subtitle = key;
			return this;
		}

		public DefinitionBuilder defaultSubtitle(final String subtitle, final String key) {
			SoundEventRegistry.this.subtitles.put(key, subtitle);
			return this.defaultSubtitle(key);
		}

		public DefinitionBuilder subtitle(final String subtitle) {
			final String id = SoundEventRegistry.this.modId + ".subtitle." + this.name;
			return this.defaultSubtitle(subtitle, id);
		}

		public DefinitionBuilder addFileVariant(final ResourceLocation path, final UnaryOperator<SoundBuilder> operator) {
			final SoundBuilder builder = SoundEventRegistry.this.new SoundBuilder(path);
			operator.apply(builder);
			this.sounds.add(builder.build());
			return this;
		}

		public DefinitionBuilder addFileVariant(final String path, final UnaryOperator<SoundBuilder> operator) {
			return this.addFileVariant(SoundEventRegistry.this.path(path), operator);
		}

		public DefinitionBuilder addFileVariant(final String path) {
			return this.addFileVariant(path, UnaryOperator.identity());
		}

		public DefinitionBuilder addFileVariants(final String path, final int count) {
			for (int i = 0; i < count; i++) {
				final int n = i + 1;
                this.addFileVariant(path + "_" + n);
			}
			return this;
		}

		public DefinitionBuilder addEventVariant(final SoundEvent event, final UnaryOperator<SoundBuilder> operator) {
                final SoundBuilder builder = SoundEventRegistry.this.new SoundBuilder(event.location())
					.setType(SoundFile.Type.EVENT);
			operator.apply(builder);
			this.sounds.add(builder.build());
			return this;
		}

		public DefinitionBuilder addEventVariant(final SimSoundEntry entry, final UnaryOperator<SoundBuilder> operator) {
			final SoundBuilder builder = SoundEventRegistry.this.new SoundBuilder(entry.id())
					.setType(SoundFile.Type.EVENT);
			operator.apply(builder);
			this.sounds.add(builder.build());
			return this;
		}

		public DefinitionBuilder addEventVariant(final AllSoundEvents.SoundEntry soundEntry, final UnaryOperator<SoundBuilder> operator) {
                final SoundBuilder builder = SoundEventRegistry.this.new SoundBuilder(soundEntry.getMainEvent().location())
					.setType(SoundFile.Type.EVENT);
			operator.apply(builder);
			this.sounds.add(builder.build());
			return this;
		}

		public DefinitionBuilder addEventVariant(final SoundEvent event) {
			return this.addEventVariant(event, UnaryOperator.identity());
		}

		public DefinitionBuilder addEventVariant(final SimSoundEntry entry) {
			return this.addEventVariant(entry, UnaryOperator.identity());
		}

		public DefinitionBuilder addEventVariant(final AllSoundEvents.SoundEntry soundEntry) {
			return this.addEventVariant(soundEntry, UnaryOperator.identity());
		}

		public SoundDefinition build() {
			return new SoundDefinition(false, Optional.ofNullable(this.subtitle), this.sounds);
		}
	}

	public class SoundBuilder {
		private final ResourceLocation name;
		private float volume = 1.0f;
		private float pitch = 1.0f;
		private int weight = 1;
		private boolean stream = false;
		private int attenuationDistance = 16;
		private boolean preload = false;
		private SoundFile.Type type = SoundFile.Type.FILE;

		private SoundBuilder(final ResourceLocation name) {
			this.name = name;
		}

		private SoundBuilder(final String name) {
			this(SoundEventRegistry.this.path(name));
		}

		public SoundBuilder setVolume(final float volume) {
			this.volume = volume;
			return this;
		}

		public SoundBuilder setPitch(final float pitch) {
			this.pitch = pitch;
			return this;
		}

		public SoundBuilder setWeight(final int weight) {
			this.weight = weight;
			return this;
		}

		public SoundBuilder setStream(final boolean stream) {
			this.stream = stream;
			return this;
		}

		public SoundBuilder setAttenuationDistance(final int attenuationDistance) {
			this.attenuationDistance = attenuationDistance;
			return this;
		}

		public SoundBuilder setPreload(final boolean preload) {
			this.preload = preload;
			return this;
		}

		public SoundBuilder setType(final SoundFile.Type type) {
			this.type = type;
			return this;
		}

		public SoundFile build() {
			return new SoundFile(this.name, this.volume, this.pitch, this.weight, this.stream, this.attenuationDistance, this.preload, this.type);
		}
	}

}
