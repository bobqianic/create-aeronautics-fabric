package dev.simulated_team.simulated.multiloader.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface NBTSerializable {
    CompoundTag write(HolderLookup.Provider provider);

    void read(HolderLookup.Provider provider, CompoundTag nbt);
}

