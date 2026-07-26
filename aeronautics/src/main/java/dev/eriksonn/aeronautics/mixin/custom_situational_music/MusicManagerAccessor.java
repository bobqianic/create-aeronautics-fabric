package dev.eriksonn.aeronautics.mixin.custom_situational_music;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MusicManager.class)
public interface MusicManagerAccessor {

	@Accessor
	int getNextSongDelay();

	@Accessor
	void setNextSongDelay(int delay);

	@Accessor
	SoundInstance getCurrentMusic();
}
