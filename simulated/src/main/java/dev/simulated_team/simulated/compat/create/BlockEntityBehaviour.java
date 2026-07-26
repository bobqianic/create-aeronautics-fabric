package dev.simulated_team.simulated.compat.create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class BlockEntityBehaviour<T extends com.zurrtum.create.foundation.blockEntity.SmartBlockEntity>
        extends com.zurrtum.create.api.behaviour.BlockEntityBehaviour<T> {

    protected BlockEntityBehaviour(final T blockEntity) {
        super(blockEntity);
    }

    @Override
    public void read(final ValueInput input, final boolean clientPacket) {
        super.read(input, clientPacket);
        final CompoundTag tag = input.read(legacyDataKey(), CompoundTag.CODEC).orElseGet(CompoundTag::new);
        read(tag, input.lookup(), clientPacket);
    }

    @Override
    public void write(final ValueOutput output, final boolean clientPacket) {
        super.write(output, clientPacket);
        final CompoundTag tag = new CompoundTag();
        write(tag, blockEntity.getLevel() != null ? blockEntity.getLevel().registryAccess() : RegistryAccess.EMPTY, clientPacket);
        if (!tag.isEmpty()) {
            output.store(legacyDataKey(), CompoundTag.CODEC, tag);
        }
    }

    private String legacyDataKey() {
        return "simulated_legacy_behaviour_" + getClass().getName();
    }

    public void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }

    public void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }
}
