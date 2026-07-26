package dev.ryanhcode.sable.sublevel.storage;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.BitSet;

/**
 * Stores the map for which plots are occupied
 */
public class SubLevelOccupancySavedData extends SavedData {
    public static final String FILE_ID = "sable_sub_level_occupancy";
    private final ServerLevel level;

    private SubLevelOccupancySavedData(final ServerLevel level) {
        this.level = level;
    }

    public static SubLevelOccupancySavedData getOrLoad(final ServerLevel level) {
        // PORT-NOTE(mc26.1): SavedData.Factory was replaced by codec-based SavedDataType. The CompoundTag
        // codec bridges to the legacy load/save NBT format. SavedDataType requires a non-null DataFixTypes;
        // SAVED_DATA_COMMAND_STORAGE is the conventional inert choice for modded data (no fixers apply).
        // File moves from data/sable_sub_level_occupancy.dat to data/sable/sable_sub_level_occupancy.dat (namespaced id).
        return level.getDataStorage().computeIfAbsent(
                new SavedDataType<>(
                        SubLevelOccupancySavedData.FILE_ID,
                        () -> new SubLevelOccupancySavedData(level),
                        CompoundTag.CODEC.xmap(
                                tag -> SubLevelOccupancySavedData.load(level, tag),
                                data -> data.save(new CompoundTag())
                        ),
                        DataFixTypes.SAVED_DATA_COMMAND_STORAGE
                ));
    }


    private static SubLevelOccupancySavedData load(final ServerLevel level, final CompoundTag tag) {
        final SubLevelOccupancySavedData data = new SubLevelOccupancySavedData(level);

        final long[] longArray = tag.getLongArray("sub_level_occupancy").orElseGet(() -> new long[0]);

        if (longArray.length > 0) {
            final BitSet occupancyData = BitSet.valueOf(longArray);
            final SubLevelContainer container = SubLevelContainer.getContainer(level);
            assert container != null : "Sub-level container is null";

            // clone into the container
            final BitSet occupancy = container.getOccupancy();
            occupancy.clear();
            occupancy.or(occupancyData);
        }

        return data;
    }

    public CompoundTag save(final CompoundTag compoundTag) {
        final SubLevelContainer container = SubLevelContainer.getContainer(this.level);
        assert container != null : "Sub-level container is null";

        final BitSet occupancy = container.getOccupancy();

        final long[] longArray = occupancy.toLongArray();

        compoundTag.putLongArray("sub_level_occupancy", longArray);

        return compoundTag;
    }
}
