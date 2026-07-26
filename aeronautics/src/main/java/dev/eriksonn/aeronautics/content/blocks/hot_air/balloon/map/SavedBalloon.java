package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasHolder;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import net.minecraft.core.BlockPos;

import java.util.List;

public record SavedBalloon(BoundingBox3i bounds, BlockPos controllerPos, List<LiftingGasHolder> gasData) {

    public static final Codec<SavedBalloon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BoundingBox3i.CODEC.fieldOf("bounds").forGetter(SavedBalloon::bounds),
            BlockPos.CODEC.fieldOf("pos").forGetter(SavedBalloon::controllerPos),
            LiftingGasHolder.CODEC.listOf().fieldOf("gasData").forGetter(SavedBalloon::gasData)
    ).apply(instance, SavedBalloon::new));

}
