package dev.simulated_team.simulated.compat.create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

final class LegacyKineticPersistence {
    private static final String LEGACY_DATA_KEY = "simulated_legacy_data";

    private LegacyKineticPersistence() {
    }

    static void write(final ValueOutput output, final Level level, final boolean clientPacket, final Writer writer) {
        final CompoundTag tag = new CompoundTag();
        writer.write(tag, level != null ? level.registryAccess() : RegistryAccess.EMPTY, clientPacket);
        if (!tag.isEmpty()) {
            output.store(LEGACY_DATA_KEY, CompoundTag.CODEC, tag);
        }
    }

    static void read(final ValueInput input, final boolean clientPacket, final Reader reader) {
        final CompoundTag tag = input.read(LEGACY_DATA_KEY, CompoundTag.CODEC).orElseGet(CompoundTag::new);
        reader.read(tag, input.lookup(), clientPacket);
    }

    @FunctionalInterface
    interface Writer {
        void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket);
    }

    @FunctionalInterface
    interface Reader {
        void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket);
    }
}
