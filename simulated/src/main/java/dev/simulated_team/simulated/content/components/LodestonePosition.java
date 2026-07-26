package dev.simulated_team.simulated.content.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;

public record LodestonePosition(long id, GlobalPos associatedPos) {
    public static final Codec<LodestonePosition> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                    Codec.LONG.fieldOf("ID").forGetter(LodestonePosition::id),
                    GlobalPos.CODEC.fieldOf("POS").forGetter(LodestonePosition::associatedPos)
            ).apply(i, LodestonePosition::new));
}
