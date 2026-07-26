package dev.simulated_team.simulated.compat.create;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ClipboardCloneable extends com.zurrtum.create.content.equipment.clipboard.ClipboardCloneable {
    String LEGACY_CLIPBOARD_KEY = "simulated_legacy_clipboard";

    default boolean writeToClipboard(final HolderLookup.Provider registries, final CompoundTag tag, final Direction side) {
        return false;
    }

    default boolean readFromClipboard(final HolderLookup.Provider registries, final CompoundTag tag, final Player player, final Direction side, final boolean simulate) {
        return false;
    }

    @Override
    default boolean writeToClipboard(final ValueOutput output, final Direction side) {
        final CompoundTag tag = new CompoundTag();
        final boolean written = writeToClipboard(RegistryAccess.EMPTY, tag, side);
        if (written && !tag.isEmpty()) {
            output.store(LEGACY_CLIPBOARD_KEY, CompoundTag.CODEC, tag);
        }
        return written;
    }

    @Override
    default boolean readFromClipboard(final ValueInput input, final Player player, final Direction side, final boolean simulate) {
        final CompoundTag tag = input.read(LEGACY_CLIPBOARD_KEY, CompoundTag.CODEC).orElseGet(CompoundTag::new);
        return readFromClipboard(input.lookup(), tag, player, side, simulate);
    }
}
