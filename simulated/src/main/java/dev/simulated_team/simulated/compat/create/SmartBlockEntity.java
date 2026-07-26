package dev.simulated_team.simulated.compat.create;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public abstract class SmartBlockEntity extends com.zurrtum.create.foundation.blockEntity.SmartBlockEntity {
    private static final String LEGACY_DATA_KEY = "simulated_legacy_data";

    protected SmartBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(final ValueOutput output, final boolean clientPacket) {
        super.write(output, clientPacket);
        writeLegacyData(output, clientPacket);
    }

    @Override
    protected void read(final ValueInput input, final boolean clientPacket) {
        super.read(input, clientPacket);
        final CompoundTag tag = input.read(LEGACY_DATA_KEY, CompoundTag.CODEC).orElseGet(CompoundTag::new);
        read(tag, input.lookup(), clientPacket);
    }

    @Override
    public void writeSafe(final ValueOutput output) {
        super.writeSafe(output);
        writeLegacyData(output, false);
    }

    private void writeLegacyData(final ValueOutput output, final boolean clientPacket) {
        final CompoundTag tag = new CompoundTag();
        write(tag, registryAccess(), clientPacket);
        if (!tag.isEmpty()) {
            output.store(LEGACY_DATA_KEY, CompoundTag.CODEC, tag);
        }
    }

    private HolderLookup.Provider registryAccess() {
        return level != null ? level.registryAccess() : RegistryAccess.EMPTY;
    }

    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }

    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
    }

    public boolean addToTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return false;
    }

    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        return false;
    }

    protected void addStressImpactStats(final List<Component> tooltip, final float stressImpact) {
    }
}
