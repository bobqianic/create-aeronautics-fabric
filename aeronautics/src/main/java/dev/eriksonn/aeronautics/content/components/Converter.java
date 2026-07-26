package dev.eriksonn.aeronautics.content.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eriksonn.aeronautics.index.AeroDataComponents;
import dev.eriksonn.aeronautics.index.AeroItems;
import dev.eriksonn.aeronautics.index.AeroSoundEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record Converter(ItemStack item, int ticks, Optional<ResourceLocation> sound, Optional<ResourceLocation> particle) {
	public static final Codec<Converter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.fieldOf("item").forGetter(Converter::item),
			Codec.INT.fieldOf("ticks").forGetter(Converter::ticks),
			ResourceLocation.CODEC.optionalFieldOf("sound").forGetter(Converter::sound),
			ResourceLocation.CODEC.optionalFieldOf("particle").forGetter(Converter::particle)
	).apply(instance, Converter::new));

	public static Converter cloudSkipper() {
		return new Converter(AeroItems.MUSIC_DISC_CLOUD_SKIPPER.asStack(), 60,
				Optional.of(AeroSoundEvents.CLOUD_SKIPPER_TRANSFORM.id()),
				Optional.of(ResourceLocation.withDefaultNamespace("white_smoke")));
	}

	public Converter(Converter converter, int ticks) {
		this(converter.item(), ticks, converter.sound(), converter.particle());
	}

	public static void tick(Level level, ItemEntity entity, ItemStack stack, Converter converter) {
		if(converter.item().isEmpty()) return;

		if(converter.ticks() > 0) {
			stack.set(AeroDataComponents.CONVERTER, new Converter(converter, converter.ticks() - 1));
		} else {
			int count = stack.getCount();
			entity.setItem(converter.item().copy());
			ItemStack newItem = entity.getItem();
			newItem.remove(AeroDataComponents.CONVERTER);
			newItem.setCount(count);

			if(converter.sound().isPresent()) {
				ResourceLocation soundLocation = converter.sound().get();
				SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(soundLocation);
				if(sound != null) {
					level.playSound(entity, entity.blockPosition(), sound, SoundSource.AMBIENT, 5.0f, 1.0f);
				}
			}

			if(converter.particle().isPresent() && level instanceof ServerLevel serverLevel) {
				ParticleType<?> particle = BuiltInRegistries.PARTICLE_TYPE.getValue(converter.particle().get());

				if(particle instanceof ParticleOptions particleOptions) {
					Vec3 pos = entity.position();
					float offset = entity.getBbHeight() + entity.getBbHeight() / 2.0f;
					serverLevel.sendParticles(particleOptions, pos.x(), pos.y() + offset, pos.z(), 20, 0.0, 0.0, 0.0, 0.05);
				}
			}
		}
	}
}
