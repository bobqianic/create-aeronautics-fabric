package dev.eriksonn.aeronautics.mixin.custom_situational_music;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.eriksonn.aeronautics.api.CustomSituationalMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow @Nullable public LocalPlayer player;

	@Shadow @Nullable public ClientLevel level;

	@WrapOperation(method = "getSituationalMusic", at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/Musics;GAME:Lnet/minecraft/sounds/Music;", opcode = 178))
	private Music aeronautics$getSituationalMusic(Operation<Music> original) {
		Music music = original.call();
		Music customMusic = CustomSituationalMusic.getSituationalMusic(this.level, this.player);

		if(customMusic != null) {
			music = customMusic;
		}
		return music;
	}
}
