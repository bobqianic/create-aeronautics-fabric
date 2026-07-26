package dev.simulated_team.simulated.api.sound;

import foundry.veil.platform.registry.RegistryObject;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record SimSoundEntry(ResourceLocation id, RegistryObject<SoundEvent> registryObject, SoundSource category) {


    public SoundEvent event() {
        return this.registryObject().get();
    }


    public void playFrom(final Entity entity) {
        this.playFrom(entity, 1, 1);
    }

    public void playFrom(final Entity entity, final float volume, final float pitch) {
        if (!entity.isSilent())
            this.play(entity.level(), null, entity.blockPosition(), volume, pitch);
    }

    public void play(final Level world, final Player entity, final Vec3i pos, final float volume, final float pitch) {
        this.play(world, entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, volume, pitch);
    }

    public void play(final Level world, final Player entity, final Vec3 pos, final float volume, final float pitch) {
        this.play(world, entity, pos.x(), pos.y(), pos.z(), volume, pitch);
    }

    public void playAt(final Level world, final Vec3i pos, final float volume, final float pitch, final boolean fade) {
        this.playAt(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, volume, pitch, fade);
    }

    public void playAt(final Level world, final Vec3 pos, final float volume, final float pitch, final boolean fade) {
        this.playAt(world, pos.x(), pos.y(), pos.z(), volume, pitch, fade);
    }

    public void play(final Level world, final Player entity, final double x, final double y, final double z, final float volume, final float pitch) {
        world.playSound(entity, x, y, z, this.event(), this.category, volume, pitch);
    }

    public void playAt(final Level world, final double x, final double y, final double z, final float volume, final float pitch, final boolean fade) {
        world.playLocalSound(x, y, z, this.event(), this.category, volume, pitch, fade);
    }
}
