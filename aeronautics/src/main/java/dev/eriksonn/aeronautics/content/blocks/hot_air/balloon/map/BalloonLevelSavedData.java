package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.Balloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.ServerBalloon;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BalloonLevelSavedData extends SavedData {
    public static final String ID = "aeronautics_unloaded_balloons";
    public static Codec<List<SavedBalloon>> CODEC = Codec.list(SavedBalloon.CODEC);

    private Level level;

    private static BalloonLevelSavedData create(final ServerLevel level, final CompoundTag tag, final HolderLookup.Provider registries) {
        final BalloonLevelSavedData sd = new BalloonLevelSavedData();

        if (tag.contains(ID)) {
            final DataResult<Pair<List<SavedBalloon>, Tag>> result = CODEC.decode(NbtOps.INSTANCE, tag.getListOrEmpty(ID));

            final BalloonMap map = BalloonMap.MAP.get(level);
            result.ifSuccess(x -> map.getUnloadedBalloons().addAll(x.getFirst()));
        }
        return sd;
    }

    public static BalloonLevelSavedData get(final ServerLevel level) {
        final BalloonLevelSavedData data = level.getChunkSource().getDataStorage().computeIfAbsent(
                new SavedDataType<>(
                        ID,
                        BalloonLevelSavedData::new,
                        CompoundTag.CODEC.xmap(
                                tag -> create(level, tag, level.registryAccess()),
                                BalloonLevelSavedData::save),
                        DataFixTypes.SAVED_DATA_COMMAND_STORAGE));
        data.level = level;

        return data;
    }

    private @NotNull CompoundTag save() {
        final CompoundTag tag = new CompoundTag();
        final BalloonMap map = BalloonMap.MAP.get(this.level);
        final ObjectArrayList<SavedBalloon> list = new ObjectArrayList<>(map.getUnloadedBalloons());

        for (final Balloon balloon : map.getBalloons()) {
            list.add(BalloonMap.saveBalloon((ServerBalloon) balloon));
        }

        final DataResult<Tag> result = CODEC.encodeStart(NbtOps.INSTANCE, list);
        result.ifSuccess(data -> tag.put(ID, data));

        return tag;
    }
}
