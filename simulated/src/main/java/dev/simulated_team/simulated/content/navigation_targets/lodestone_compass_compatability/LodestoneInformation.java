package dev.simulated_team.simulated.content.navigation_targets.lodestone_compass_compatability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.UUIDUtil;
import org.joml.Vector3d;

import java.util.UUID;

public record LodestoneInformation(UUID id, Vector3d projectedPos) {
	public static final Codec<LodestoneInformation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			UUIDUtil.CODEC.fieldOf("trackerID").forGetter(LodestoneInformation::id)
	).apply(instance, id -> new LodestoneInformation(id, new Vector3d())));

	public CompoundTag saveAsCompound() {
		final CompoundTag data = new CompoundTag();
		data.store("trackerID", UUIDUtil.CODEC, this.id);

		return data;
	}

	public static LodestoneInformation loadFromCompound(final CompoundTag tag) {
		return new LodestoneInformation(tag.read("trackerID", UUIDUtil.CODEC).orElseThrow(), new Vector3d());
	}
}
